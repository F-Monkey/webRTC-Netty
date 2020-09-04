package org.cn.monkey.webchat.room.state.chat;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.cn.monkey.cmd.CmdType;
import org.cn.monkey.cmd.proto.Command;
import org.cn.monkey.cmd.proto.UserCmd;
import org.cn.monkey.state.ICharacter;
import org.cn.monkey.state.ITime;
import org.cn.monkey.state.StateAdapter;
import org.cn.monkey.state.Status;
import org.cn.monkey.webchat.room.RoomData;
import org.cn.monkey.common.utils.ResultCode;
import org.cn.monkey.webchat.util.CmdUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RoomStateAdapter extends StateAdapter {

    private static final Logger log = LoggerFactory.getLogger(RoomStateAdapter.class);

    protected RoomData getRoomData() {
        return (RoomData) super.stateData;
    }

    @Override
    public void handleCmd(Status status, ITime time, ICharacter character, Command.Cmd cmd) {
        int cmdType = cmd.getCmdType();
        switch (cmdType) {
            case CmdType.CLOSE_ROOM:
                this.closeRoomCmd(status, time, character, cmd);
                return;
            case CmdType.KICK_OFF:
                this.clickOutCharacter(status, time, character, cmd);
                return;
            case CmdType.LOGIN_OUT:
                this.logout(status, time, character, cmd);
                return;
            default:
                super.handleCmd(status, time, character, cmd);
        }
    }

    protected void closeRoomCmd(Status status, ITime time, ICharacter character, Command.Cmd cmd) {
        if (!character.getId().equals(this.getRoomData().getMaster().getId())) {
            character.sendMsg(CmdUtil.closeRoomResult(ResultCode.FAIL, "非房主不能关闭房间"));
            return;
        }
        this.getRoomData().close();
        character.sendMsg(CmdUtil.closeRoomResult(ResultCode.OK, "关闭房间"));
        this.broadCastData(character, CmdUtil.closeRoomResult(ResultCode.OK, "房主已关闭房间"));
    }

    protected void clickOutCharacter(Status status, ITime time, ICharacter character, Command.Cmd cmd) {
        RoomData roomData = (RoomData) super.stateData;
        String masterId = roomData.getMaster().getId();
        if (!character.getId().equals(masterId)) {
            character.sendMsg(CmdUtil.kickOffCharacterResult(ResultCode.FAIL, "非房主不能踢出用户"));
            return;
        }

        ByteString content = cmd.getContent();
        UserCmd.KickOffCharacter kickOffCharacter;
        try {
            kickOffCharacter = UserCmd.KickOffCharacter.parseFrom(content);
        } catch (InvalidProtocolBufferException e) {
            log.error("can not parse content for cmdType:{}", CmdType.KICK_OFF);
            character.sendMsg(CmdUtil.kickOffCharacterResult(ResultCode.ERROR, "无效的指令"));
            return;
        }

        String kickOffCharacterId = kickOffCharacter.getCharacterId();
        if (masterId.equals(kickOffCharacterId)) {
            character.sendMsg(CmdUtil.kickOffCharacterResult(ResultCode.FAIL, "房主无法踢出自己"));
            return;
        }

        String nickName = null;
        if (super.stateData.removeCharacter(kickOffCharacterId)) {
            ICharacter kickOff = super.stateData.getCharacter(kickOffCharacterId);
            if (kickOff != null) {
                nickName = kickOff.getNickName();
                kickOff.sendMsg(CmdUtil.kickOffCharacterResult(ResultCode.OK, "你已被房主踢出"));
            }
        }
        String msg = nickName == null ? "用户已被踢出" : "用户已" + nickName + "被踢出";
        this.broadCastData(null, CmdUtil.kickOffCharacterResult(ResultCode.OK, msg));
    }

    private void logout(Status status, ITime time, ICharacter character, Command.Cmd cmd) {
        character.sendMsg(CmdUtil.logoutResult(ResultCode.OK, "已退出房间"));

        if (this.stateData.removeCharacter(character.getId())) {
            broadCastData(character, CmdUtil.logoutResult(ResultCode.OK, "【" + character.getNickName() + "】已退出房间"));
        }
    }


    protected void broadCastData(ICharacter excludeCharacter, Command.PackageGroup packageGroup) {
        for (ICharacter character : this.stateData.characterSet()) {
            if (excludeCharacter != null && excludeCharacter.getId().equals(character.getId())) {
                continue;
            }
            character.sendMsg(packageGroup);
        }
    }
}
