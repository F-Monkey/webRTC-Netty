package org.cn.monkey.webchat.room.state.chat;

import org.cn.monkey.state.ITime;
import org.cn.monkey.state.Status;

public class ChattingState extends RoomStateAdapter {

    public static final String CODE = "chatting";

    @Override
    public String getCode() {
        return CODE;
    }

    @Override
    public void update(Status status, ITime time) {
        if (this.getRoomData().isClosed()) {
            status.isFinished = true;
        }
    }

    @Override
    public String finish(Status status, ITime time) {
        return FinishState.CODE;
    }
}
