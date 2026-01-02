package com.hsx.manyue.modules.im.protocol;

import com.hsx.manyue.modules.im.model.IMMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * IM协议解码器
 * 
 * 协议格式:
 * +-------+--------+----------+-----------+
 * | Magic | Length | MsgType  |  Body     |
 * | 2byte | 4byte  | 1byte    |  N bytes  |
 * +-------+--------+----------+-----------+
 */
@Slf4j
public class IMProtocolDecoder extends ByteToMessageDecoder {

    private static final short MAGIC_NUMBER = (short) 0xABCD;
    private static final int HEADER_LENGTH = 7; // 2 + 4 + 1
    private static final int MAX_BODY_LENGTH = 1024 * 1024; // 最大1MB

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // 可读字节数不足header长度
        if (in.readableBytes() < HEADER_LENGTH) {
            return;
        }

        // 标记读位置
        in.markReaderIndex();

        // 读取魔数
        short magic = in.readShort();
        if (magic != MAGIC_NUMBER) {
            log.error("无效的魔数: {}, 关闭连接: {}", magic, ctx.channel().remoteAddress());
            ctx.close();
            return;
        }

        // 读取消息长度
        int bodyLength = in.readInt();
        if (bodyLength < 0 || bodyLength > MAX_BODY_LENGTH) {
            log.error("消息体长度异常: {}, 关闭连接: {}", bodyLength, ctx.channel().remoteAddress());
            ctx.close();
            return;
        }

        // 消息体未完全到达
        if (in.readableBytes() < bodyLength + 1) {
            in.resetReaderIndex();
            return;
        }

        // 读取消息类型
        byte msgType = in.readByte();

        // 读取消息体
        byte[] body = new byte[bodyLength];
        in.readBytes(body);

        // 解析为IMMessage对象
        try {
            IMMessage message = IMMessage.decode(msgType, body);
            out.add(message);
        } catch (Exception e) {
            log.error("消息解码失败: msgType={}", msgType, e);
        }
    }
}
