package org.cn.monkey.netty;

import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.cn.monkey.cmd.proto.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@ChannelHandler.Sharable
public class ProtoWebSocketHandler extends SimpleChannelInboundHandler<BinaryWebSocketFrame> {

    private static final Logger log = LoggerFactory.getLogger(ProtoWebSocketHandler.class);

    private final List<Filter> filters;

    private final Dispatcher dispatcher;

    public ProtoWebSocketHandler(List<Filter> filters,
                                 Dispatcher dispatcher) {
        this.filters = filters;
        this.dispatcher = dispatcher;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("get connection from: {}", ctx.channel().remoteAddress());
    }

    private Session initSession(ChannelHandlerContext ctx) {
        Session session;
        Channel channel = ctx.channel();
        if (channel.hasAttr(Session.KEY)) {
            session = channel.attr(Session.KEY).get();
        } else {
            session = new NettyWebSocketSession(ctx);
            channel.attr(Session.KEY).set(session);
        }
        return session;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BinaryWebSocketFrame frame) {
        ByteBuf content = frame.content();
        byte[] bytes = new byte[content.readableBytes()];
        content.readBytes(bytes);

        Session session = this.initSession(ctx);

        Command.Cmd cmd;
        try {
            cmd = Command.Cmd.parseFrom(bytes);
            log.info("pkg.cmdType:{}", cmd.getCmdType());
        } catch (InvalidProtocolBufferException e) {
            session.send("error");
            log.error("can not parse content", e);
            return;
        }

        boolean needDispatch = true;
        if (this.filters != null && this.filters.size() > 0) {
            for (Filter filter : this.filters) {
                if (!filter.filter(session, cmd)) needDispatch = false;
            }
        }

        if (needDispatch) {
            if (this.dispatcher != null) {
                this.dispatcher.dispatch(session, cmd);
                return;
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("handle error:\n", cause);
        ctx.close();
    }
}
