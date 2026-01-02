package com.hsx.manyue.modules.im.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import com.hsx.manyue.common.annotation.aspect.Login;
import com.hsx.manyue.common.controller.SuperController;
import com.hsx.manyue.common.dto.R;
import com.hsx.manyue.common.utils.JwtUtil;
import com.hsx.manyue.modules.im.model.dto.IMMessageDTO;
import com.hsx.manyue.modules.im.model.entity.IMMessageEntity;
import com.hsx.manyue.modules.im.service.IIMMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * IM消息控制器
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/im/message")
@Tag(name = "IM消息接口")
public class IMMessageController extends SuperController {

    private final IIMMessageService messageService;

    @Autowired
    @Lazy
    private com.hsx.manyue.modules.oss.service.IOssService ossService;

    @PostMapping("/send/single")
    @Operation(summary = "发送单聊消息")
    @Login
    public R sendSingleMessage(@RequestBody IMMessageDTO dto) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        dto.setFromUserId(userId);
        IMMessageEntity message = messageService.sendSingleMessage(dto);
        return success(message);
    }

    @PostMapping("/send/group")
    @Operation(summary = "发送群聊消息")
    @Login
    public R sendGroupMessage(@RequestBody IMMessageDTO dto) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        dto.setFromUserId(userId);
        IMMessageEntity message = messageService.sendGroupMessage(dto);
        return success(message);
    }

    @GetMapping("/history")
    @Operation(summary = "拉取历史消息")
    @Login
    public R pullHistory(@RequestParam Long targetId,
                         @RequestParam Integer sessionType,
                         @RequestParam(required = false) Long startSeq,
                         @RequestParam(defaultValue = "50") Integer limit) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        List<IMMessageEntity> messages = messageService.pullHistory(userId, targetId, sessionType, startSeq, limit);
        return success(messages);
    }

    @GetMapping("/unread/count")
    @Operation(summary = "获取未读消息数")
    @Login
    public R getUnreadCount() {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        Integer count = messageService.getUnreadCount(userId);
        return success(count);
    }

    @PostMapping("/sync")
    @Operation(summary = "同步消息")
    @Login
    public R syncMessages(@RequestParam Long lastSeq) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        List<IMMessageEntity> messages = messageService.syncMessages(userId, lastSeq);
        return success(messages);
    }

    @PostMapping("/uploadImage")
    @Operation(summary = "上传聊天图片")
    @Login
    public R uploadImage(@RequestParam(value = "file", required = false) org.springframework.web.multipart.MultipartFile file) {
        try {
            // 验证文件是否存在
            if (file == null || file.isEmpty()) {
                return R.failure("请选择要上传的图片文件");
            }
            
            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return R.failure("只支持图片格式");
            }
            
            // 验证文件大小 (10MB)
            if (file.getSize() > 10 * 1024 * 1024) {
                return R.failure("图片大小不能超过10MB");
            }
            
            String imageUrl = ossService.uploadFile(file);
            
            // 返回包含文件信息的对象
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("url", imageUrl);
            result.put("size", file.getSize());
            result.put("fileName", file.getOriginalFilename());
            
            return success(result);
        } catch (Exception e) {
            return R.failure("图片上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/uploadVoice")
    @Operation(summary = "上传语音消息")
    @Login
    public R uploadVoice(@RequestParam(value = "file", required = false) org.springframework.web.multipart.MultipartFile file) {
        try {
            // 验证文件是否存在
            if (file == null || file.isEmpty()) {
                return R.failure("请选择要上传的语音文件");
            }
            
            // 验证文件类型 (支持常见音频格式)
            String contentType = file.getContentType();
            String fileName = file.getOriginalFilename();
            if (fileName == null || (!fileName.endsWith(".mp3") && !fileName.endsWith(".wav") 
                && !fileName.endsWith(".m4a") && !fileName.endsWith(".amr"))) {
                return R.failure("只支持mp3/wav/m4a/amr格式");
            }
            
            // 验证文件大小 (5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                return R.failure("语音文件大小不能超过5MB");
            }
            
            String voiceUrl = ossService.uploadFile(file);
            
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("url", voiceUrl);
            result.put("size", file.getSize());
            result.put("fileName", file.getOriginalFilename());
            
            return success(result);
        } catch (Exception e) {
            return R.failure("语音上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/uploadVideo")
    @Operation(summary = "上传视频消息")
    @Login
    public R uploadVideo(@RequestParam(value = "file", required = false) org.springframework.web.multipart.MultipartFile file) {
        try {
            // 验证文件是否存在
            if (file == null || file.isEmpty()) {
                return R.failure("请选择要上传的视频文件");
            }
            
            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("video/")) {
                return R.failure("只支持视频格式");
            }
            
            // 验证文件大小 (50MB)
            if (file.getSize() > 50 * 1024 * 1024) {
                return R.failure("视频大小不能超过50MB");
            }
            
            String videoUrl = ossService.uploadFile(file);
            
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("url", videoUrl);
            result.put("size", file.getSize());
            result.put("fileName", file.getOriginalFilename());
            
            return success(result);
        } catch (Exception e) {
            return R.failure("视频上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/uploadFile")
    @Operation(summary = "上传文件")
    @Login
    public R uploadFile(@RequestParam(value = "file", required = false) org.springframework.web.multipart.MultipartFile file) {
        try {
            // 验证文件是否存在
            if (file == null || file.isEmpty()) {
                return R.failure("请选择要上传的文件");
            }
            
            // 验证文件大小 (20MB)
            if (file.getSize() > 20 * 1024 * 1024) {
                return R.failure("文件大小不能超过20MB");
            }
            
            // 验证文件名
            String fileName = file.getOriginalFilename();
            if (fileName == null || fileName.trim().isEmpty()) {
                return R.failure("文件名不能为空");
            }
            
            String fileUrl = ossService.uploadFile(file);
            
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("url", fileUrl);
            result.put("size", file.getSize());
            result.put("fileName", fileName);
            
            return success(result);
        } catch (Exception e) {
            return R.failure("文件上传失败: " + e.getMessage());
        }
    }
}
