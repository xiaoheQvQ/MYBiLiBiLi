package com.hsx.manyue.modules.im.protocol;

import com.hsx.manyue.modules.im.model.IMMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * IM协议编码器
 */
@Slf4j
public class IMProtocolEncoder extends MessageToByteEncoder<IMMessage> {

    private static final short MAGIC_NUMBER = (short) 0xABCD;

    @Override
    protected void encode(ChannelHandlerContext ctx, IMMessage msg, ByteBuf out) {
        try {
            // 编码消息体
            byte[] body = msg.encode();
            
            // 写入魔数
            out.writeShort(MAGIC_NUMBER);
            
            // 写入消息体长度
            out.writeInt(body.length);
            
            // 写入消息类型
            out.writeByte(msg.getMsgType());
            
            // 写入消息体
            out.writeBytes(body);
            
        } catch (Exception e) {
            log.error("消息编码失败", e);
        }
    }
}
