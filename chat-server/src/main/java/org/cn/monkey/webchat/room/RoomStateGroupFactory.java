package org.cn.monkey.webchat.room;

import org.cn.monkey.state.*;
import org.cn.monkey.webchat.room.state.chat.ChattingState;
import org.cn.monkey.webchat.room.state.chat.FinishState;
import org.cn.monkey.webchat.room.state.chat.WaitingState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

public class RoomStateGroupFactory extends AbstractStateGroupFactory {

    private static final Logger log = LoggerFactory.getLogger(RoomStateGroupFactory.class);

    private static final AtomicLong ID_COUNTER = new AtomicLong(1);

    public RoomStateGroupFactory(int roomCapacity) {
        super(roomCapacity);
    }

    @Override
    public IStateGroup create(ICharacter creator, IStateGroupConfig config) {
        RoomStateConfig stateConfig = (RoomStateConfig) config;
        long roomId = ID_COUNTER.getAndIncrement();
        final RoomData roomData = new RoomData(stateConfig);
        roomData.setMaster(creator);
        return this.stateGroupMap.computeIfAbsent(roomId, (key) -> {
                    RoomStateGroup roomStateGroup = new RoomStateGroup(key, roomData, this.roomCapacity);
                    this.addChatStates(roomStateGroup);
                    return roomStateGroup;
                }
        );
    }

    private void addChatStates(IStateGroup stateGroup) {
        stateGroup.addState(new WaitingState());
        stateGroup.addState(new ChattingState());
        stateGroup.addState(new FinishState());
        stateGroup.addState(new DefaultErrorState());
        stateGroup.setCurrentState(WaitingState.CODE);
    }
}
