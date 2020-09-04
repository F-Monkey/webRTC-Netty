package org.cn.monkey.state;

import org.cn.monkey.cmd.proto.Command;
import org.cn.monkey.state.util.CmdUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class StateAdapter implements IState {

    private static final Logger log = LoggerFactory.getLogger(StateAdapter.class);

    protected boolean initialized = false;

    protected IStateData stateData;

    @Override
    public void setStateData(IStateData stateData) {
        this.stateData = stateData;
    }

    @Override
    public void init(Status status, ITime time) {
        this.initialized = true;
    }

    @Override
    public boolean hasInit() {
        return this.initialized;
    }

    @Override
    public void initOnError(Status status, ITime time, Throwable error) {
        status.isFinished = true;
        String msg = "init error";
        this.logError(status, time, msg, error);
    }

    @Override
    public void handleCmd(Status status, ITime time, ICharacter character, Command.Cmd cmd) {
        String msg = "character: %s can not handle cmd: %d";
        msg = String.format(msg, character.getId(), cmd.getCmdType());
        character.sendMsg(CmdUtil.errorMsg(msg, cmd.getCmdType()));
        this.logInfo(status, time, msg);
    }

    @Override
    public void handleCmdOnError(Status status, ITime time, ICharacter character, Command.Cmd cmd, Throwable error) {
        String msg = "character: %s handle cmd: %d error";
        msg = String.format(msg, character.getId(), cmd.getCmdType());
        character.sendMsg(CmdUtil.errorMsg(msg, cmd.getCmdType()));
        this.logError(status, time, msg, error);
    }

    @Override
    public void updateOnError(Status status, ITime time, Throwable error) {
        String msg = "update error";
        this.logError(status, time, msg, error);
    }

    @Override
    public String finishOnError(Status status, ITime time, Throwable error) {
        String msg = "finish error";
        this.logError(status, time, msg, error);
        return DefaultErrorState.CODE;
    }

    protected final void logInfo(Status status, ITime time, String msg) {
        log.info("currentState:{}, statue: {}, time: {}, msg:\n{}", this.getCode(), status, time.getCurrentTime(), msg);
    }

    protected final void logError(Status status, ITime time, String msg, Throwable e) {
        log.error("currentState:{}, statue: {}, time: {}, msg:\n{} error:\n{}", this.getCode(), status, time.getCurrentTime(), msg, e);
    }

}
