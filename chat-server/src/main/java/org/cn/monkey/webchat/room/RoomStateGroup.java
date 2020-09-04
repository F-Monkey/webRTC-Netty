package org.cn.monkey.webchat.room;

import org.cn.monkey.cmd.proto.Command;
import org.cn.monkey.state.AbstractStateGroup;
import org.cn.monkey.state.ICharacter;
import org.cn.monkey.common.utils.ResultCode;
import org.cn.monkey.webchat.util.CmdUtil;

public class RoomStateGroup extends AbstractStateGroup<RoomData> {

    public RoomStateGroup(long id, RoomData stateData, int characterCapacity) {
        super(id, stateData, characterCapacity);
    }

    @Override
    public boolean enter(ICharacter character) {
        if (super.enter(character)) {

            long roomId = this.getId();
            long currentRoomId = character.getCurrentRoomId();
            String msg;
            if (currentRoomId == roomId) {
                msg = "用户【" + character.getNickName() + "】 重新连接房间";
            } else {
                character.setCurrentRoomId(roomId);
                msg = "用户【" + character.getNickName() + "】 进入房间";
            }

            character.sendMsg(CmdUtil.enterRoomResult(ResultCode.OK, "进入房间成功", roomId, super.getStateData()));
            Command.PackageGroup otherMsg = CmdUtil.enterRoomResult(ResultCode.OK, msg, roomId, super.getStateData());

            for (ICharacter other : super.stateData.characterSet()) {
                if (other.isOffLine()) {
                    continue;
                }
                if (character.getId().equals(other.getId())) {
                    continue;
                }
                other.sendMsg(otherMsg);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean canDepose() {
        return this.stateData.isEmpty();
    }
}
