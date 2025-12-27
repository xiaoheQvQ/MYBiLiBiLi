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
}
