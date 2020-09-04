package org.cn.monkey.netty;

import org.cn.monkey.cmd.proto.Command;

public interface Dispatcher {
    void dispatch(Session session, Command.Cmd cmd);
}
