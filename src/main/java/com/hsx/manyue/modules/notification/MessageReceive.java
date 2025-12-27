package com.hsx.manyue.modules.notification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageReceive {

    /**
     * 用户ID，用于获取用户Session
     */
    private String userId;

    /**
     * 消息类型【join, offer, answer, candidate, leave】
     */
    private String type;

    /**
     * 消息内容 前端stringFiy序列化后字符串
     */
    private String data;
}
