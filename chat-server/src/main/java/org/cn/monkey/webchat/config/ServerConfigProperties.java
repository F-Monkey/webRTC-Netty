package org.cn.monkey.webchat.config;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "spring.webchat")
@Component
public class ServerConfigProperties {
    private int roomCapacity;

    int updateWaitTime;

    int stateGroupCapacity;

    public int getRoomCapacity() {
        return roomCapacity;
    }

    public void setRoomCapacity(int roomCapacity) {
        this.roomCapacity = roomCapacity;
    }

    public int getUpdateWaitTime() {
        return updateWaitTime;
    }

    public void setUpdateWaitTime(int updateWaitTime) {
        this.updateWaitTime = updateWaitTime;
    }

    public int getStateGroupCapacity() {
        return stateGroupCapacity;
    }

    public void setStateGroupCapacity(int stateGroupCapacity) {
        this.stateGroupCapacity = stateGroupCapacity;
    }
}
