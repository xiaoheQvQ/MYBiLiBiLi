package com.hsx.manyue.common.constant;

/**
 * redis 键常量
 */
public class RedisKeys {

    public final static String EMAIL_CAPTCHA = "email-captcha:";
    public final static String DANMAKU = "danmaku:";
    public final static String DANMAKU_NEW = "danmaku:new:";
    public static final String VIEW_COUNTS = "video:count:";

    public static final String VIDEO_LIKE = "video:like:";
    public static final String AI_REVIEW = "video:ai-review";
    public static final String SUBSCRIBE = "subscribe:";
    public static final String AVATAR = "avatar:";
    public static final String VIDEO_DETAIL = "video-detail:";
    public static final String VIDEO_UPLOAD = "video-upload";
    public static final String VIDEO_NOTIFICATIONS = "video:notifications:";
    public static final String PRIVATE_MESSAGES = "private_messages";
    public static final String VIDEO_CALL_NOTIFICATIONS = "video_call_notifications";
    public static final String ANIME_UPLOAD = "anime_upload";


    public static final String POST_LIKE_NOTIFICATIONS = "post_like_notifications:";
    public static final String POST_COMMENT_NOTIFICATIONS = "post_comment_notifications:";


    public static final String PLAYBACK_QUEUE_KEY_PREFIX = "music:playback_queue:";
    public static final long PLAYBACK_QUEUE_TTL_DAYS = 7; // 播放列表在Redis中保留7天
    
    // 新增：视频观看历史缓存（用于批量更新优化）
    public static final String VIDEO_HISTORY_CACHE = "video:history:cache:";
    public static final String VIDEO_HISTORY_PENDING_KEYS = "video:history:pending";
    
    // 新增：视频计数待处理 key 集合（避免 keys() 扫描）
    public static final String VIEW_COUNTS_PENDING_KEYS = "video:count:pending";
    public static final String VIDEO_LIKE_PENDING_KEYS = "video:like:pending";
    public static final String DANMAKU_PENDING_KEYS = "danmaku:pending";
    
    // 新增：MQ 消息补偿待处理 key 集合
    public static final String MQ_COMPENSATION_PENDING_KEYS = "mq:compensation:pending";
    
    // 新增：笔记点赞Bitmap（替代Set提升性能）
    public static final String NOTE_LIKE_BITMAP = "note:like:bitmap:";
    public static final String POST_LIKE_BITMAP = "post:like:bitmap:";
    
    // 新增：分布式锁
    public static final String DISTRIBUTED_LOCK = "lock:";
    
    // 新增：评论和私信缓存
    public static final String COMMENT_CACHE = "comment:cache:";
    public static final String PRIVATE_MESSAGE_CACHE = "message:cache:";
    public static final String COMMENT_PENDING_KEYS = "comment:pending";
    public static final String MESSAGE_PENDING_KEYS = "message:pending";
}
