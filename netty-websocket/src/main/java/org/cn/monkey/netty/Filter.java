package org.cn.monkey.netty;

import org.cn.monkey.cmd.proto.Command;

public interface Filter {
    boolean filter(Session session, Command.Cmd cmd);
}
