package org.cn.monkey.webchat.room;

import com.google.common.base.Preconditions;
import org.cn.monkey.common.core.User;
import org.cn.monkey.netty.Session;
import org.cn.monkey.state.ICharacter;
import org.cn.monkey.common.utils.ResultCode;
import org.cn.monkey.webchat.util.CmdUtil;

public class Chatter implements ICharacter {
    private final User user;

    private Session session;

    private boolean isOnLine;

    private long currentRoomId;

    public Chatter(User user, Session session) {
        Preconditions.checkNotNull(user);
        this.user = user;
        this.isOnLine = true;
        this.session = session;
    }

    @Override
    public String getId() {
        return this.user.getUid();
    }

    @Override
    public String getRemoteAddress() {
        return this.session.getRemoteAddress();
    }

    @Override
    public void setCurrentRoomId(long roomId) {
        this.currentRoomId = roomId;
    }

    @Override
    public long getCurrentRoomId() {
        return this.currentRoomId;
    }

    @Override
    public String getNickName() {
        return this.user.getNickName();
    }

    @Override
    public void setSession(Session session) {
        if (session == null) {
            return;
        }

        if (this.session != null) {
            this.session.send(CmdUtil.kickOffCharacterResult(ResultCode.OK, "异地登陆"));
        }

        this.session = session;
    }

    @Override
    public void sendMsg(Object msg) {
        this.session.send(msg);
    }

    @Override
    public void onLine() {
        this.isOnLine = true;
    }

    @Override
    public void offLine() {
        this.isOnLine = false;
    }

    @Override
    public boolean isOffLine() {
        return !this.isOnLine || !session.isAlive();
    }
}
