package org.cn.monkey.state;

import io.netty.util.AttributeKey;
import org.cn.monkey.netty.Session;

public interface ICharacter {

    AttributeKey<ICharacter> SESSION_KEY = AttributeKey.newInstance("character");

    String getId();

    String getRemoteAddress();

    void setCurrentRoomId(long roomId);

    long getCurrentRoomId();

    String getNickName();

    void setSession(Session session);

    void sendMsg(Object msg);

    void onLine();

    void offLine();

    boolean isOffLine();
}
