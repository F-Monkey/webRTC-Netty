package org.cn.monkey.webchat.room.state.chat;

import org.cn.monkey.state.DefaultErrorState;
import org.cn.monkey.state.ITime;
import org.cn.monkey.state.Status;

public class FinishState extends RoomStateAdapter {

    public static final String CODE = "finish";

    @Override
    public String getCode() {
        return CODE;
    }

    @Override
    public void update(Status status, ITime time) {

    }

    @Override
    public String finish(Status status, ITime time) {
        return DefaultErrorState.CODE;
    }
}
