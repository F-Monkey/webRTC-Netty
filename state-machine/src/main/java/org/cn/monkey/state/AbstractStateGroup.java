package org.cn.monkey.state;

import com.google.common.base.Preconditions;
import org.cn.monkey.cmd.proto.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class AbstractStateGroup<T extends IStateData> implements IStateGroup {

    private static final Logger log = LoggerFactory.getLogger(DefaultErrorState.class);

    private static final int DEFAULT_CHARACTER_CAPACITY = 8;

    private final long id;

    protected final Map<String, IState> stateMap;

    protected final int characterCapacity;

    protected final Status status;

    protected final ITime time;

    protected final LinkedBlockingQueue<CharacterCmdPair> cmdQueue;

    protected final IStateData stateData;

    private IState currentState;

    public AbstractStateGroup(long id,
                              T stateData,
                              int characterCapacity) {
        Preconditions.checkArgument(characterCapacity >= DEFAULT_CHARACTER_CAPACITY);
        Preconditions.checkNotNull(stateData);
        this.characterCapacity = characterCapacity;
        this.id = id;
        this.stateMap = new HashMap<>();
        this.status = new Status();
        this.time = new Time();
        this.cmdQueue = new LinkedBlockingQueue<>(1000);
        this.stateData = stateData;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @SuppressWarnings("unchecked")
    public T getStateData() {
        return (T) this.stateData;
    }

    @Override
    public void addState(IState state) {
        state.setStateData(this.stateData);
        this.stateMap.put(state.getCode(), state);
    }

    @Override
    public void setCurrentState(String stateCode) {
        Preconditions.checkNotNull(this.currentState = this.stateMap.get(stateCode));
    }

    @Override
    public boolean enter(ICharacter character) {
        if (this.isFullCharacter()) {
            return false;
        }
        this.stateData.addCharacter(character);
        return true;
    }

    @Override
    public boolean isFullCharacter() {
        return this.stateData.characterSize() >= this.characterCapacity * 0.75;
    }

    @Override
    public boolean addCharacterCmd(ICharacter character, Command.Cmd cmd) {
        return this.cmdQueue.offer(new CharacterCmdPair(character, cmd));
    }

    @Override
    public void update() {

        if (this.currentState == null) {
            return;
        }

        CharacterCmdPair characterCmdPair = this.cmdQueue.poll();
        if (characterCmdPair == null) {
            return;
        }

        if (!this.currentState.hasInit()) {
            try {
                this.currentState.init(this.status, this.time);
            } catch (Throwable e) {
                this.currentState.initOnError(this.status, this.time, e);
            }
        }

        ICharacter character = characterCmdPair.getCharacter();
        Command.Cmd cmd = characterCmdPair.getCmd();

        try {
            this.currentState.handleCmd(this.status, this.time, character, cmd);
        } catch (Throwable error) {
            this.currentState.handleCmdOnError(this.status, this.time, character, cmd, error);
        }

        try {
            this.currentState.update(this.status, this.time);
        } catch (Throwable error) {
            this.currentState.updateOnError(this.status, this.time, error);
        }

        try {
            String nextCode = this.currentState.finish(this.status, this.time);
            if (nextCode != null) {
                if (this.stateMap.containsKey(nextCode)) {
                    this.currentState = this.stateMap.get(nextCode);
                } else {
                    log.error("can not find state by stateCode: {}", nextCode);
                }
            }
        } catch (Throwable error) {
            this.currentState.finishOnError(this.status, this.time, error);
        }
    }

    @Override
    public boolean canDepose() {
        return DefaultErrorState.CODE.equals(this.currentState.getCode()) && this.stateData.isEmpty();
    }
}
