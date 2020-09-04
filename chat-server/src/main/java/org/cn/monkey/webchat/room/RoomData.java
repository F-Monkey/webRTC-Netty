package org.cn.monkey.webchat.room;

import org.cn.monkey.state.ICharacter;
import org.cn.monkey.state.IStateData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RoomData implements IStateData {

    private final Map<String, ICharacter> characterMap;

    private ICharacter master;

    private boolean isClosed;

    private boolean hasAudio;

    private boolean hasVideo;

    public RoomData(RoomStateConfig roomStateConfig) {
        this.characterMap = new HashMap<>();
        this.isClosed = false;
        this.hasAudio = roomStateConfig.isHasAudio();
        this.hasVideo = roomStateConfig.isHasVideo();
    }

    public void close() {
        this.isClosed = true;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public ICharacter getMaster() {
        return master;
    }

    public void setMaster(ICharacter master) {
        this.master = master;
    }

    public void setHasAudio(boolean hasAudio) {
        this.hasAudio = hasAudio;
    }

    public boolean getHasAudio() {
        return this.hasAudio;
    }

    public void setHasVideo(boolean hasVideo) {
        this.hasVideo = hasVideo;
    }

    public boolean getHasVideo() {
        return this.hasVideo;
    }

    @Override
    public boolean addCharacter(ICharacter character) {
        this.characterMap.put(character.getId(), character);
        return true;
    }

    @Override
    public ICharacter getCharacter(String characterId) {
        return this.characterMap.get(characterId);
    }

    @Override
    public boolean removeCharacter(String characterId) {
        return this.characterMap.remove(characterId) != null;
    }

    public Set<ICharacter> characterSet() {
        Collection<ICharacter> characters = this.characterMap.values();
        return characters.stream().filter(character -> !character.isOffLine()).collect(Collectors.toSet());
    }

    @Override
    public boolean isEmpty() {
        return this.characterSet().isEmpty();
    }

    @Override
    public int characterSize() {
        return this.characterMap.size();
    }
}
