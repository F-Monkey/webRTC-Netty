package org.cn.monkey.state;

import org.cn.monkey.netty.Session;

public interface ICharacterFactory {
    class FetchCharacter {
        final boolean isNew;
        final ICharacter character;

        public FetchCharacter(boolean isNew, ICharacter character) {
            this.isNew = isNew;
            this.character = character;
        }

        public boolean isNew() {
            return isNew;
        }

        public ICharacter getCharacter() {
            return character;
        }
    }

    FetchCharacter findOrCreate(Session session, String userId);

    ICharacter find(String userId);
}
