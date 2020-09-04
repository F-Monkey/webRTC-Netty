package org.cn.monkey.webchat.room;

import org.cn.monkey.state.IStateGroupConfig;

public class RoomStateConfig implements IStateGroupConfig {

    private boolean hasAudio;

    private boolean hasVideo;

    public boolean isHasAudio() {
        return hasAudio;
    }

    public void setHasAudio(boolean hasAudio) {
        this.hasAudio = hasAudio;
    }

    public boolean isHasVideo() {
        return hasVideo;
    }

    public void setHasVideo(boolean hasVideo) {
        this.hasVideo = hasVideo;
    }
}
