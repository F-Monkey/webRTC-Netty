package org.cn.monkey.netty;

import com.google.common.base.Preconditions;
import com.google.protobuf.MessageLite;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.util.AttributeKey;
import org.cn.monkey.common.utils.ObjectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class NettyWebSocketSession implements Session {

    private static final Logger log = LoggerFactory.getLogger(NettyWebSocketSession.class);

    private final ChannelHandlerContext context;

    public NettyWebSocketSession(ChannelHandlerContext ctx) {
        if (Objects.isNull(ctx)) {
            log.warn("create empty session context");
        }
        this.context = ctx;
    }

    @Override
    public String getId() {
        return this.context.channel().id().asLongText();
    }

    @Override
    public String getRemoteAddress() {
        return this.context.channel().remoteAddress().toString();
    }

    @Override
    public <T> T getAttribute(AttributeKey<T> key) {
        if (this.context == null) {
            return null;
        }
        return this.context.channel().attr(key).get();
    }

    @Override
    public <T> void setAttribute(AttributeKey<T> key, T val) {
        // 空属性设置值无意义
        Preconditions.checkNotNull(this.context);
        this.context.channel().attr(key).set(val);
    }

    @Override
    public void send(Object data) {
        if (this.context == null) {
            return;
        }
        if (data instanceof BinaryWebSocketFrame) {
            this.context.writeAndFlush(data);
            return;
        }

        if (data instanceof MessageLite) {
            BinaryWebSocketFrame socketFrame = new BinaryWebSocketFrame(Unpooled.copiedBuffer(((MessageLite) data).toByteArray()));
            this.context.writeAndFlush(socketFrame);
            return;
        }

        byte[] bytes = ObjectUtil.copy2Bytes(data);
        if (bytes != null) {
            this.context.writeAndFlush(new BinaryWebSocketFrame(Unpooled.copiedBuffer(bytes)));
            return;
        }
        log.info("invalid Object data: {}", data);
    }

    @Override
    public boolean isAlive() {
        return this.context.channel().isActive();
    }
}
