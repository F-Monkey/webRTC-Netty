package org.cn.monkey.webchat.room;

import org.cn.monkey.common.core.User;
import org.cn.monkey.common.thread.AutoTask;
import org.cn.monkey.netty.Session;
import org.cn.monkey.state.ICharacter;
import org.cn.monkey.state.ICharacterFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ChatterFactory implements ICharacterFactory {

    private volatile ConcurrentHashMap<String, ICharacter> characterMap;

    public ChatterFactory() {
        this.characterMap = new ConcurrentHashMap<>();
        new AutoTask("characterFactory", 1, 1, TimeUnit.MINUTES) {
            @Override
            protected void run() {
                ChatterFactory.this.removeOffLine();
            }
        }.start();
    }

    private void removeOffLine() {

        ConcurrentHashMap<String, ICharacter> characterMap = new ConcurrentHashMap<>(this.characterMap);
        characterMap.entrySet().removeIf(e ->
                e.getValue().isOffLine()
        );
        this.characterMap = characterMap;
    }

    @Override
    public FetchCharacter findOrCreate(Session session, String userId) {
        boolean[] isNew = {false};
        ICharacter character = this.characterMap.computeIfAbsent(userId, (key) -> {
            User user = new User();
            user.setUid(key);
            user.setNickName(user.getNickName());
            isNew[0] = true;
            return new Chatter(user, session);
        });
        return new FetchCharacter(isNew[0], character);
    }

    @Override
    public ICharacter find(String userId) {
        return this.characterMap.get(userId);
    }
}
