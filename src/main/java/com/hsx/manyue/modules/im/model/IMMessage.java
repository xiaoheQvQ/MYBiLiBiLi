package com.hsx.manyue.modules.im.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * IM统一消息模型 - 用于Netty传输
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IMMessage {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    
    // 消息类型常量
    public static final byte TYPE_AUTH = 1;           // 认证
    public static final byte TYPE_HEARTBEAT = 2;      // 心跳
    public static final byte TYPE_SINGLE_CHAT = 3;    // 单聊消息
    public static final byte TYPE_GROUP_CHAT = 4;     // 群聊消息
    public static final byte TYPE_ACK = 5;            // 消息确认
    public static final byte TYPE_READ_RECEIPT = 6;   // 已读回执
    public static final byte TYPE_TYPING = 7;         // 正在输入
    public static final byte TYPE_SYNC_REQ = 8;       // 同步请求
    public static final byte TYPE_SYNC_RESP = 9;      // 同步响应
    public static final byte TYPE_ONLINE_STATUS = 10; // 在线状态
    public static final byte TYPE_FRIEND_APPLY = 11;  // 好友申请
    public static final byte TYPE_GROUP_NOTIFY = 12;  // 群组通知
    
    /**
     * 消息类型
     */
    private Byte msgType;
    
    /**
     * 消息序列号
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long msgSeq;
    
    /**
     * 客户端消息ID(去重)
     */
    private String clientMsgId;
    
    /**
     * 发送者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long fromUserId;
    
    /**
     * 接收者ID(单聊)
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long toUserId;
    
    /**
     * 群组ID(群聊)
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long toGroupId;
    
    /**
     * 内容类型
     */
    private Integer contentType;
    
    /**
     * 消息内容(JSON)
     */
    private String content;
    
    /**
     * 消息时间戳
     */
    private Long msgTime;
    
    /**
     * 认证token
     */
    private String token;
    
    /**
     * 额外数据
     */
    private Object data;
    
    /**
     * 解码方法 - 从JSON字节数组解码
     */
    public static IMMessage decode(byte msgType, byte[] body) {
        try {
            IMMessage message = objectMapper.readValue(body, IMMessage.class);
            message.setMsgType(msgType);
            return message;
        } catch (Exception e) {
            throw new RuntimeException("消息解码失败", e);
        }
    }
    
    /**
     * 编码方法 - 编码为JSON字节数组
     */
    public byte[] encode() {
        try {
            return objectMapper.writeValueAsBytes(this);
        } catch (Exception e) {
            throw new RuntimeException("消息编码失败", e);
        }
    }

    /**
     * 编码为JSON字符串 (用于WebSocket)
     */
    public String toJson() {
        try {
            return objectMapper.writeValueAsString(this);
        } catch (Exception e) {
            throw new RuntimeException("消息编码失败", e);
        }
    }
    
    /**
     * 创建ACK消息
     */
    public static IMMessage createAck(Long msgSeq, Long userId) {
        IMMessage ack = new IMMessage();
        ack.setMsgType(TYPE_ACK);
        ack.setMsgSeq(msgSeq);
        ack.setFromUserId(userId);
        ack.setMsgTime(System.currentTimeMillis());
        return ack;
    }
    
    /**
     * 创建心跳消息
     */
    public static IMMessage createHeartbeat() {
        IMMessage heartbeat = new IMMessage();
        heartbeat.setMsgType(TYPE_HEARTBEAT);
        heartbeat.setMsgTime(System.currentTimeMillis());
        return heartbeat;
    }
}
