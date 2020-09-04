package org.cn.monkey.webchat.room.state.chat;

import org.cn.monkey.cmd.CmdType;
import org.cn.monkey.cmd.proto.Command;
import org.cn.monkey.state.ICharacter;
import org.cn.monkey.state.ITime;
import org.cn.monkey.state.Status;
import org.cn.monkey.webchat.room.RoomData;
import org.cn.monkey.common.utils.ResultCode;
import org.cn.monkey.webchat.util.CmdUtil;

public class WaitingState extends RoomStateAdapter {

    public static final String CODE = "waiting";

    @Override
    public String getCode() {
        return CODE;
    }

    @Override
    public void handleCmd(Status status, ITime time, ICharacter character, Command.Cmd cmd) {
        int cmdType = cmd.getCmdType();
        switch (cmdType) {
            case CmdType.START_CHATTING:
                this.startChatting(status, time, character, cmd);
                return;
            default:
                super.handleCmd(status, time, character, cmd);
        }
    }

    @Override
    public void update(Status status, ITime time) {
        if (this.getRoomData().isClosed()) {
            status.isFinished = true;
        }
    }

    @Override
    public String finish(Status status, ITime time) {
        return ChattingState.CODE;
    }

    private void startChatting(Status status, ITime time, ICharacter character, Command.Cmd cmd) {
        // TODO check chatting can start
        RoomData roomData = super.getRoomData();
        ICharacter master = roomData.getMaster();
        if (!character.getId().equals(master.getId())) {
            character.sendMsg(CmdUtil.startChatResult(ResultCode.FAIL, "非房主无法开始视频"));
            return;
        }
        status.isFinished = true;
    }
}
