package org.cn.monkey.state;

import java.util.Set;

public interface IStateData {

    boolean addCharacter(ICharacter character);

    ICharacter getCharacter(String characterId);

    boolean removeCharacter(String characterId);

    Set<ICharacter> characterSet();

    boolean isEmpty();

    int characterSize();
}
