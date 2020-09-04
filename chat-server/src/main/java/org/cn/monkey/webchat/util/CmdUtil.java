package org.cn.monkey.webchat.util;

import com.google.common.base.Strings;
import com.google.protobuf.ByteString;
import org.cn.monkey.cmd.CmdType;
import org.cn.monkey.cmd.proto.Command;
import org.cn.monkey.cmd.proto.Entity;
import org.cn.monkey.cmd.proto.UserCmd;
import org.cn.monkey.common.utils.ResultCode;
import org.cn.monkey.state.ICharacter;
import org.cn.monkey.state.IStateData;
import org.cn.monkey.webchat.room.RoomData;

public class CmdUtil {
    private CmdUtil() {
    }

    private static Command.PackageGroup packageGroup(Command.Package... packages) {
        Command.PackageGroup.Builder builder = Command.PackageGroup.newBuilder();
        for (Command.Package pkg : packages) {
            if (pkg != null) {
                builder.addPackages(pkg);
            }
        }
        return builder.build();
    }

    private static Command.ResultMessage resultMsg(int resultCode, String msg) {
        Command.ResultMessage.Builder builder = Command.ResultMessage.newBuilder().setCode(resultCode);
        if (!Strings.isNullOrEmpty(msg)) {
            builder.setMsg(msg);
        }
        return builder.build();
    }

    private static Command.Package pkg(int resultCode, String msg, int cmdType, ByteString content) {
        Command.Package.Builder builder = Command.Package.newBuilder().setResultMsg(resultMsg(resultCode, msg));
        if (cmdType > 0) {
            builder.setCmdType(cmdType);
        }
        if (content != null) {
            builder.setContent(content);
        }
        return builder.build();
    }

    public static Command.PackageGroup errorMsg(String msg, int cmdType) {
        return packageGroup(pkg(ResultCode.ERROR, msg, cmdType, null));
    }

    public static Command.PackageGroup logoutResult(int resultCode, String msg) {
        UserCmd.LogoutResult logoutResult = UserCmd.LogoutResult.newBuilder().build();
        return packageGroup(pkg(resultCode, msg, CmdType.LOGIN_OUT, logoutResult.toByteString()));
    }

    public static Command.PackageGroup kickOffCharacterResult(int resultCode, String msg) {
        UserCmd.KickOffCharacterResult kickOffCharacterResult = UserCmd.KickOffCharacterResult.newBuilder().build();
        return packageGroup(pkg(resultCode, msg, CmdType.KICK_OFF, kickOffCharacterResult.toByteString()));
    }

    public static Command.PackageGroup removeCharacter(String characterId, String characterName) {
        return packageGroup(pkg(ResultCode.OK, "玩家【" + characterName + " 】退出", CmdType.REMOVE_CHARACTER, null));
    }

    private static Entity.Character character(ICharacter character) {
        Entity.Character.Builder characterBuilder = Entity.Character.newBuilder();
        String id = character.getId();
        if (Strings.isNullOrEmpty(id)) {
            return null;
        }
        characterBuilder.setId(id);

        String remoteAddress = character.getRemoteAddress();
        if (Strings.isNullOrEmpty(remoteAddress)) {
            return null;
        }
        characterBuilder.setRemoteAddress(remoteAddress);

        String nickName = character.getNickName();
        if (!Strings.isNullOrEmpty(nickName)) {
            characterBuilder.setNickName(nickName);
        }
        return characterBuilder.build();
    }

    public static Command.PackageGroup createRoomResult(int resultCode, String msg, long roomId, IStateData stateData) {
        UserCmd.CreateRoomResult.Builder builder = UserCmd.CreateRoomResult.newBuilder();
        if (resultCode == ResultCode.OK) {
            builder.setRoomId(roomId);
            if (stateData != null) {
                RoomData roomData = (RoomData) stateData;
                Entity.RoomData.Builder roomDataBuilder = Entity.RoomData.newBuilder();
                for (ICharacter character : roomData.characterSet()) {
                    Entity.Character protoCharacter = character(character);
                    if (protoCharacter == null) {
                        continue;
                    }
                    roomDataBuilder.addCharacters(protoCharacter);
                }
                roomDataBuilder.setHasAudio(roomData.getHasAudio());
                roomDataBuilder.setHasVideo(roomData.getHasVideo());
                roomDataBuilder.setIsClosed(roomData.isClosed());
                roomDataBuilder.setMaster(roomData.getMaster().getId());
                builder.setRoomData(roomDataBuilder.build());
            }
        }
        return packageGroup(pkg(resultCode, msg, CmdType.CREATE_ROOM, builder.build().toByteString()));
    }

    public static Command.PackageGroup unsupportedCmdType(int errorCode, String msg, int cmdType) {
        return packageGroup(pkg(ResultCode.ERROR, "无效的命令", cmdType, null));
    }

    public static Command.PackageGroup enterRoomResult(int resultCode, String msg, long roomId, RoomData roomData) {
        UserCmd.EnterRoomResult.Builder builder = UserCmd.EnterRoomResult.newBuilder().setRoomId(roomId);
        if (roomData != null) {
            Entity.RoomData.Builder roomDataBuilder = Entity.RoomData.newBuilder();
            for (ICharacter character : roomData.characterSet()) {
                Entity.Character protoCharacter = character(character);
                if (protoCharacter == null) {
                    continue;
                }
                roomDataBuilder.addCharacters(protoCharacter);
            }
            roomDataBuilder.setHasAudio(roomData.getHasAudio());
            roomDataBuilder.setHasVideo(roomData.getHasVideo());
            roomDataBuilder.setIsClosed(roomData.isClosed());
            roomDataBuilder.setMaster(roomData.getMaster().getId());
            builder.setRoomData(roomDataBuilder.build());
        }
        ByteString content = builder.build().toByteString();
        return packageGroup(pkg(resultCode, msg, CmdType.ENTER_ROOM, content));
    }

    public static Command.PackageGroup loginResult(int resultCode, String msg) {
        ByteString content = UserCmd.LoginResult.newBuilder().build().toByteString();
        return packageGroup(pkg(resultCode, msg, CmdType.LOGIN, content));
    }

    public static Command.PackageGroup closeRoomResult(int resultCode, String msg) {
        UserCmd.CloseRoomResult closeRoomResult = UserCmd.CloseRoomResult.newBuilder().build();
        return packageGroup(pkg(resultCode, msg, CmdType.CLOSE_ROOM, closeRoomResult.toByteString()));
    }

    public static Command.PackageGroup startChatResult(int resultCode, String msg) {
        UserCmd.StartChatting startChatting = UserCmd.StartChatting.newBuilder().build();
        return packageGroup(pkg(resultCode, msg, CmdType.START_CHATTING, startChatting.toByteString()));
    }

    public static Command.PackageGroup inviteCharacterResult(int resultCode, String msg) {
        UserCmd.InviteCharacterResult content = UserCmd.InviteCharacterResult.newBuilder().build();
        return packageGroup(pkg(resultCode, msg, CmdType.INVITE, content.toByteString()));
    }
}
