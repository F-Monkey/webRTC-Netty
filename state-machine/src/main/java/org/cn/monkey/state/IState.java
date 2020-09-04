package org.cn.monkey.state;

import org.cn.monkey.cmd.proto.Command;

public interface IState {

    String getCode();

    void setStateData(IStateData stateData);

    void init(Status status, ITime time);

    void initOnError(Status status, ITime time, Throwable error);

    boolean hasInit();

    void handleCmd(Status status, ITime time, ICharacter character, Command.Cmd cmd);

    void handleCmdOnError(Status status, ITime time, ICharacter character, Command.Cmd cmd, Throwable error);

    void update(Status status, ITime time);

    void updateOnError(Status status, ITime time, Throwable error);

    String finish(Status status, ITime time);

    String finishOnError(Status status, ITime time, Throwable error);

    //String finishOnTimeout(Status status, ITime time);
}
