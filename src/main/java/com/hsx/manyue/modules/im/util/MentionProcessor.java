package com.hsx.manyue.modules.im.util;

import cn.hutool.core.util.StrUtil;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @mention处理工具类
 * 用于解析和格式化消息中的@提及
 */
public class MentionProcessor {

    // 匹配 @userId 格式 (例如: @123456)
    private static final Pattern MENTION_PATTERN = Pattern.compile("@(\\d+)");
    
    // 匹配 @all 或 @所有人
    private static final Pattern AT_ALL_PATTERN = Pattern.compile("@(all|所有人)");

    /**
     * 从消息内容中提取被@的用户ID列表
     * @param content 消息内容
     * @return 用户ID列表
     */
    public static List<Long> extractMentionedUserIds(String content) {
        if (StrUtil.isBlank(content)) {
            return Collections.emptyList();
        }

        List<Long> userIds = new ArrayList<>();
        Matcher matcher = MENTION_PATTERN.matcher(content);
        
        while (matcher.find()) {
            try {
                Long userId = Long.parseLong(matcher.group(1));
                if (!userIds.contains(userId)) {
                    userIds.add(userId);
                }
            } catch (NumberFormatException e) {
                // 忽略无效的用户ID
            }
        }
        
        return userIds;
    }

    /**
     * 检查消息是否包含@all
     * @param content 消息内容
     * @return 是否@all
     */
    public static boolean hasAtAll(String content) {
        if (StrUtil.isBlank(content)) {
            return false;
        }
        
        Matcher matcher = AT_ALL_PATTERN.matcher(content);
        return matcher.find();
    }

    /**
     * 格式化消息内容，将用户ID替换为用户名
     * @param content 原始消息内容
     * @param userMap 用户ID到用户名的映射
     * @return 格式化后的内容
     */
    public static String formatMentionContent(String content, Map<Long, String> userMap) {
        if (StrUtil.isBlank(content) || userMap == null || userMap.isEmpty()) {
            return content;
        }

        String result = content;
        Matcher matcher = MENTION_PATTERN.matcher(content);
        
        // 使用StringBuffer来替换，避免重复替换问题
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            try {
                Long userId = Long.parseLong(matcher.group(1));
                String userName = userMap.get(userId);
                if (userName != null) {
                    matcher.appendReplacement(sb, "@" + userName);
                }
            } catch (NumberFormatException e) {
                // 保持原样
            }
        }
        matcher.appendTail(sb);
        
        return sb.toString();
    }

    /**
     * 验证用户是否在被@的列表中
     * @param userId 用户ID
     * @param atUserIds @的用户ID列表
     * @param atAll 是否@all
     * @return 是否被@
     */
    public static boolean isMentioned(Long userId, List<Long> atUserIds, Boolean atAll) {
        if (Boolean.TRUE.equals(atAll)) {
            return true;
        }
        
        if (atUserIds != null && !atUserIds.isEmpty()) {
            return atUserIds.contains(userId);
        }
        
        return false;
    }

    /**
     * 生成@提示文本
     * @param atUserIds @的用户ID列表
     * @param atAll 是否@all
     * @param userMap 用户ID到用户名的映射
     * @return 提示文本，例如: "@张三 @李四" 或 "@所有人"
     */
    public static String generateAtHint(List<Long> atUserIds, Boolean atAll, Map<Long, String> userMap) {
        if (Boolean.TRUE.equals(atAll)) {
            return "@所有人";
        }
        
        if (atUserIds == null || atUserIds.isEmpty()) {
            return "";
        }
        
        StringBuilder hint = new StringBuilder();
        for (Long userId : atUserIds) {
            if (userMap != null && userMap.containsKey(userId)) {
                if (hint.length() > 0) {
                    hint.append(" ");
                }
                hint.append("@").append(userMap.get(userId));
            }
        }
        
        return hint.toString();
    }

    /**
     * 清理消息内容中的@标记（用于搜索等场景）
     * @param content 原始内容
     * @return 清理后的内容
     */
    public static String cleanMentions(String content) {
        if (StrUtil.isBlank(content)) {
            return content;
        }
        
        String result = content;
        result = MENTION_PATTERN.matcher(result).replaceAll("");
        result = AT_ALL_PATTERN.matcher(result).replaceAll("");
        
        return result.trim();
    }
}
