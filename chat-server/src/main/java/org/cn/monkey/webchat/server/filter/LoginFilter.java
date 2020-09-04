package org.cn.monkey.webchat.server.filter;

import org.cn.monkey.cmd.CmdType;
import org.cn.monkey.cmd.proto.Command;
import org.cn.monkey.netty.Filter;
import org.cn.monkey.netty.Session;
import org.cn.monkey.state.ICharacter;
import org.cn.monkey.webchat.util.CmdUtil;
import org.cn.monkey.common.utils.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(LoginFilter.class);

    @Override
    public boolean filter(Session session, Command.Cmd pkg) {
        int cmdType = pkg.getCmdType();
        if (cmdType == CmdType.LOGIN) {
            return true;
        }

        if (session.getAttribute(ICharacter.SESSION_KEY) == null) {
            log.error("character has not login, cmdType should be Login type");
            session.send(CmdUtil.loginResult(ResultCode.FAIL,"not login"));
            return false;
        }

        return true;
    }
}
