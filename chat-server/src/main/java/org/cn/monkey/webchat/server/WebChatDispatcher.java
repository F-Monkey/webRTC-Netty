package org.cn.monkey.webchat.server;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.nio.NioEventLoopGroup;
import org.cn.monkey.cmd.CmdType;
import org.cn.monkey.cmd.proto.Command;
import org.cn.monkey.cmd.proto.UserCmd;
import org.cn.monkey.common.utils.LogUtil;
import org.cn.monkey.netty.Dispatcher;
import org.cn.monkey.netty.Session;
import org.cn.monkey.state.*;
import org.cn.monkey.webchat.room.RoomStateConfig;
import org.cn.monkey.common.utils.ResultCode;
import org.cn.monkey.webchat.util.CmdUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.ReentrantLock;

public class WebChatDispatcher implements Dispatcher {

    private static final Logger log = LoggerFactory.getLogger(WebChatDispatcher.class);

    private final IStateGroupFactory roomStateGroupFactory;

    private final ICharacterFactory characterFactory;

    private final IWorkerManager workerManager;

    private final NioEventLoopGroup eventLoopGroup;

    private final LoadingCache<String, ReentrantLock> loadLock;

    public WebChatDispatcher(IStateGroupFactory roomStateGroupFactory,
                             ICharacterFactory characterFactory,
                             IWorkerManager workerManager) {
        Preconditions.checkNotNull(roomStateGroupFactory);
        Preconditions.checkNotNull(characterFactory);
        Preconditions.checkNotNull(workerManager);
        this.roomStateGroupFactory = roomStateGroupFactory;
        this.characterFactory = characterFactory;
        this.workerManager = workerManager;
        this.eventLoopGroup = new NioEventLoopGroup();
        this.loadLock = CacheBuilder.newBuilder().weakValues().build(new CacheLoader<String, ReentrantLock>() {
            @Override
            public ReentrantLock load(String key) {
                return new ReentrantLock();
            }
        });
    }

    @Override
    public void dispatch(Session session, Command.Cmd cmd) {
        int cmdType = cmd.getCmdType();
        ReentrantLock lock = this.loadLock.getUnchecked(session.getId());
        try {
            lock.tryLock();
            switch (cmdType) {
                case CmdType.LOGIN:
                    this.eventLoopGroup.submit(() -> this.login(session, cmd));
                    return;
                case CmdType.CREATE_ROOM:
                    this.eventLoopGroup.submit(() -> this.createRoom(session, cmd));
                    return;
                case CmdType.ENTER_ROOM:
                    // TODO need fixed
                    this.eventLoopGroup.submit(() -> this.enterRoom(session, cmd));
                    return;
                case CmdType.INVITE:
                    this.eventLoopGroup.submit(() -> this.invite(session, cmd));
                    return;
                default:
                    this.eventLoopGroup.submit(() -> this.handleOtherCmd(session, cmd));
            }
        } finally {
            lock.unlock();
        }
    }

    private void invite(Session session, Command.Cmd cmd) {
        int cmdType = cmd.getCmdType();

        ICharacter character = session.getAttribute(ICharacter.SESSION_KEY);
        if (character == null) {
            session.send(CmdUtil.unsupportedCmdType(ResultCode.FAIL, "用户尚未登陆", cmdType));
            return;
        }
        UserCmd.InviteCharacter inviteCharacter;
        try {
            inviteCharacter = UserCmd.InviteCharacter.parseFrom(cmd.getContent());
        } catch (InvalidProtocolBufferException e) {
            log.error("can not parse createRoom content:\n{}", LogUtil.stackTrace(e));
            session.send(CmdUtil.inviteCharacterResult(ResultCode.ERROR, "参数异常"));
            return;
        }

        long roomId = inviteCharacter.getRoomId();
        IStateGroup stateGroup = this.roomStateGroupFactory.find(roomId);
        if (stateGroup == null) {
            session.send(CmdUtil.inviteCharacterResult(ResultCode.FAIL, "房间【" + roomId + "】不存在"));
            return;
        }
        String characterId = inviteCharacter.getCharacterId();
        ICharacter invited = this.characterFactory.find(characterId);
        if (invited == null) {
            session.send(CmdUtil.inviteCharacterResult(ResultCode.FAIL, "用户尚未登录"));
            return;
        }
        session.send(CmdUtil.inviteCharacterResult(ResultCode.OK, ""));
        invited.sendMsg(CmdUtil.inviteCharacterResult(ResultCode.OK, "用户:【 " + character.getNickName() + " 】邀请你"));
    }

    private void handleOtherCmd(Session session, Command.Cmd cmd) {
        int cmdType = cmd.getCmdType();
        ICharacter character = session.getAttribute(ICharacter.SESSION_KEY);
        if (character == null) {
            session.send(CmdUtil.unsupportedCmdType(ResultCode.FAIL, "用户尚未登陆", cmdType));
            return;
        }
        long currentRoomId = character.getCurrentRoomId();
        IStateGroup room = this.roomStateGroupFactory.find(currentRoomId);
        if (room == null) {
            session.send(CmdUtil.unsupportedCmdType(ResultCode.FAIL, "房间【" + currentRoomId + "】不存在", cmdType));
            return;
        }
        room.addCharacterCmd(character, cmd);
    }

    private void createRoom(Session session, Command.Cmd cmd) {
        ByteString content = cmd.getContent();
        try {
            UserCmd.CreateRoom createRoom = UserCmd.CreateRoom.parseFrom(content);
            ICharacter character = session.getAttribute(ICharacter.SESSION_KEY);
            if (character == null) {
                session.send(CmdUtil.closeRoomResult(ResultCode.FAIL, "user has not login"));
                return;
            }

            RoomStateConfig roomStateConfig = new RoomStateConfig();
            roomStateConfig.setHasAudio(createRoom.getHasAudio());
            roomStateConfig.setHasVideo(createRoom.getHasVideo());
            IStateGroup stateGroup = this.roomStateGroupFactory.create(character, roomStateConfig);
            stateGroup.enter(character);
            this.workerManager.addStateGroup(stateGroup);

            long roomId = stateGroup.getId();
            session.send(CmdUtil.createRoomResult(ResultCode.OK, "房间创建成功", roomId, stateGroup.getStateData()));

            log.info("create room success, roomId: {}", roomId);
        } catch (InvalidProtocolBufferException e) {
            log.error("can not parse createRoom content:\n{}", LogUtil.stackTrace(e));
            session.send(CmdUtil.createRoomResult(ResultCode.ERROR, "参数异常", -1, null));
        }
    }


    private void enterRoom(Session session, Command.Cmd cmd) {
        ByteString content = cmd.getContent();
        try {
            UserCmd.EnterRoom enterRoom = UserCmd.EnterRoom.parseFrom(content);
            long roomId = enterRoom.getRoomId();
            ICharacter character = session.getAttribute(ICharacter.SESSION_KEY);

            if (character == null) {
                ICharacterFactory.FetchCharacter fetchCharacter = this.characterFactory.findOrCreate(session, enterRoom.getUserId());
                character = fetchCharacter.getCharacter();
                if (!fetchCharacter.isNew()) {
                    character.setSession(session);
                }
                session.setAttribute(ICharacter.SESSION_KEY, character);
            }

            IStateGroup stateGroup = this.roomStateGroupFactory.find(roomId);

            if (stateGroup == null) {
                session.send(CmdUtil.enterRoomResult(ResultCode.FAIL, "房间号不存在", roomId, null));
                return;
            }
            boolean enterResult = stateGroup.enter(character);
            if (!enterResult) {
                session.send(CmdUtil.enterRoomResult(ResultCode.FAIL, "进入房间【" + roomId + "】失败", roomId, null));
            }
        } catch (InvalidProtocolBufferException e) {
            log.error("can not parse enterRoom content:\n{}", LogUtil.stackTrace(e));
            session.send(CmdUtil.enterRoomResult(ResultCode.ERROR, "进入房间失败", -1, null));
        }
    }

    private void login(Session session, Command.Cmd cmd) {
        ByteString content = cmd.getContent();
        try {
            UserCmd.Login login = UserCmd.Login.parseFrom(content);
            String userId = login.getUserId();
            String password = login.getPassword();
            // TODO check if the user exists
            ICharacterFactory.FetchCharacter fetchCharacter = this.characterFactory.findOrCreate(session, userId);
            ICharacter character = fetchCharacter.getCharacter();
            session.setAttribute(ICharacter.SESSION_KEY, character);
            if (!fetchCharacter.isNew()) {
                character.setSession(session);
            }

            session.send(CmdUtil.loginResult(ResultCode.OK, "登录成功"));
        } catch (InvalidProtocolBufferException e) {
            log.error("can not parse login content:\n{}", LogUtil.stackTrace(e));
            session.send(CmdUtil.loginResult(ResultCode.ERROR, "登录异常"));
        }
    }
}
