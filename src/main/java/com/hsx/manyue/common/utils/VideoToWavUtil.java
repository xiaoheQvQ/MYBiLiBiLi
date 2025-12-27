package com.hsx.manyue.common.utils;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;

import java.io.*;
import java.net.URL;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * 视频处理工具类
 */
public class VideoToWavUtil {

    /**
     * 完整的视频转字幕流程（从在线视频URL）
     */
    public static String videoUrlToVtt(String videoUrl) throws Exception {
        // 1. 下载在线视频
        File videoFile = downloadVideoFromUrl(videoUrl);

        // 2. 处理视频生成字幕
        return processVideoToVtt(videoFile);
    }

    /**
     * 从在线视频URL下载视频
     */
    public static File downloadVideoFromUrl(String videoUrl) throws IOException {
        System.out.println("开始下载在线视频: " + videoUrl);

        // 创建临时文件
        File tempFile = File.createTempFile("temp_video_", ".mp4");
        tempFile.deleteOnExit();

        HttpURLConnection connection = null;
        InputStream inputStream = null;

        try {
            URL url = new URL(videoUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);

            // 检查响应状态
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("视频下载失败，HTTP响应码: " + responseCode);
            }

            // 获取文件大小（用于进度显示）
            long fileSize = connection.getContentLengthLong();
            System.out.println("视频文件大小: " + fileSize + " bytes");

            // 下载文件
            inputStream = connection.getInputStream();
            Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            System.out.println("视频下载完成，保存到: " + tempFile.getAbsolutePath());
            return tempFile;

        } finally {
            if (inputStream != null) {
                try { inputStream.close(); } catch (IOException e) { e.printStackTrace(); }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * 从视频文件到VTT字幕
     */
    public static String processVideoToVtt(File videoFile) throws Exception {
        // 读取视频文件为字节数组
        byte[] videoBytes = Files.readAllBytes(videoFile.toPath());
        return processVideoToVtt(videoBytes);
    }

    /**
     * 从视频字节数组到VTT字幕
     */
    public static String processVideoToVtt(byte[] videoBytes) throws Exception {
        // 1. 提取音频
        String audioPath = extractAudioFromVideoBytes(videoBytes);

        // 2. 语音转文字
        String subtitleText = transcribeAudio(audioPath);

        // 3. 生成VTT文件
        return generateVttFile(subtitleText, audioPath);
    }

    /**
     * 从视频字节数组中提取音频
     */
    public static String extractAudioFromVideoBytes(byte[] videoBytes) throws Exception {
        // 创建临时视频文件
        File tempVideoFile = File.createTempFile("temp_video_", ".mp4");
        tempVideoFile.deleteOnExit();

        try (FileOutputStream fos = new FileOutputStream(tempVideoFile)) {
            fos.write(videoBytes);
        }

        // 调用原有方法处理
        return extractAudioFromVideo(tempVideoFile.getAbsolutePath());
    }

    /**
     * 从视频中提取音频
     */
    private static String extractAudioFromVideo(String videoPath) throws Exception {
        System.out.println("开始从视频中提取音频...");

        String audioPath = videoPath.replace(".mp4", "_audio.wav");
        FFmpegFrameGrabber grabber = null;
        FFmpegFrameRecorder recorder = null;

        try {
            grabber = new FFmpegFrameGrabber(videoPath);
            grabber.start();
            grabber.setAudioStream(0); // 选择第一个音频流

            recorder = new FFmpegFrameRecorder(audioPath, 1); // 单声道
            recorder.setFormat("wav");
            recorder.setSampleRate(16000);
            recorder.setAudioCodec(avcodec.AV_CODEC_ID_PCM_S16LE);
            recorder.setAudioOption("ar", "16000");
            recorder.setAudioOption("ac", "1");
            recorder.setAudioOption("sample_fmt", "s16");
            recorder.setAudioQuality(0);
            recorder.start();

            Frame frame;
            int frameCount = 0;
            while ((frame = grabber.grab()) != null) {
                if (frame.samples != null) {
                    recorder.recordSamples(frame.sampleRate, frame.audioChannels, frame.samples);
                    frameCount++;
                    if (frameCount % 100 == 0) {
                        System.out.println("已处理音频帧: " + frameCount);
                    }
                }
            }

            System.out.println("音频提取完成，共处理 " + frameCount + " 帧");

        } finally {
            if (recorder != null) {
                try { recorder.stop(); } catch (Exception e) { e.printStackTrace(); }
                try { recorder.release(); } catch (Exception e) { e.printStackTrace(); }
            }
            if (grabber != null) {
                try { grabber.stop(); } catch (Exception e) { e.printStackTrace(); }
                try { grabber.release(); } catch (Exception e) { e.printStackTrace(); }
            }
        }

        return audioPath;
    }

    /**
     * 语音转文字
     */
    private static String transcribeAudio(String audioPath) throws Exception {
        System.out.println("开始语音转文字...");

        String pythonScriptPath = "F:\\test\\fasterWhisper.py";
        ProcessBuilder pb = new ProcessBuilder("python", pythonScriptPath, audioPath);
        pb.redirectErrorStream(true);

        Process process = pb.start();
        StringBuilder output = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                System.out.println("[Whisper] " + line); // 实时输出转写进度
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("语音转文字失败，退出码: " + exitCode);
        }

        System.out.println("语音转文字完成");
        return output.toString();
    }

    /**
     * 生成VTT字幕文件（确保UTF-8无BOM编码）
     */
    private static String generateVttFile(String subtitleText, String audioPath) throws IOException {
        System.out.println("开始生成VTT字幕文件...");

        String vttPath = audioPath.replace("_audio.wav", ".vtt");

        // 使用FileOutputStream直接写入字节，避免BOM问题
        try (OutputStream out = new FileOutputStream(vttPath);
             BufferedWriter writer = new BufferedWriter(
                     new OutputStreamWriter(out, StandardCharsets.UTF_8))) {

            // 写入VTT文件头
            writer.write("WEBVTT");
            writer.newLine();
            writer.newLine();

            String[] blocks = subtitleText.split("\n\n");
            for(int i = 0; i < blocks.length; i++) {
                String block = blocks[i].trim();
                if (block.isEmpty()) {
                    continue;
                }

                String[] lines = block.split("\n");
                if (lines.length >= 3) {
                    // 序号
                    writer.write(String.valueOf(i + 1));
                    writer.newLine();
                    // 时间轴（VTT格式与SRT相同）
                    writer.write(lines[1].replace(",", ".")); // VTT使用点作为毫秒分隔符
                    writer.newLine();
                    // 内容（添加前缀）
                    writer.write("=== " + lines[2]);
                    writer.newLine();
                    // 空行分隔
                    writer.newLine();
                }
            }
        }

        System.out.println("VTT字幕文件已生成(UTF-8无BOM): " + vttPath);
        return vttPath;
    }

    /**
     * 清理临时文件
     */
    public static void cleanTempFiles(String... filePaths) {
        for (String path : filePaths) {
            if (path != null) {
                File file = new File(path);
                if (file.exists()) {
                    if (file.delete()) {
                        System.out.println("已删除临时文件: " + path);
                    } else {
                        System.out.println("删除临时文件失败: " + path);
                    }
                }
            }
        }
    }
}