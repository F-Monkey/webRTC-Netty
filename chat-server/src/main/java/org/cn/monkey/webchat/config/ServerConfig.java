package org.cn.monkey.webchat.config;

import com.google.common.base.Preconditions;
import org.cn.monkey.netty.Dispatcher;
import org.cn.monkey.netty.Filter;
import org.cn.monkey.state.ICharacterFactory;
import org.cn.monkey.state.IStateGroupFactory;
import org.cn.monkey.state.IWorkerManager;
import org.cn.monkey.state.WorkerManager;
import org.cn.monkey.webchat.room.ChatterFactory;
import org.cn.monkey.webchat.room.RoomStateGroupFactory;
import org.cn.monkey.webchat.server.WebChatDispatcher;
import org.cn.monkey.webchat.server.filter.LoginFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnBean(ServerConfigProperties.class)
public class ServerConfig {

    private final ServerConfigProperties serverConfigProperties;

    public ServerConfig(ServerConfigProperties serverConfigProperties) {
        Preconditions.checkNotNull(serverConfigProperties);
        this.serverConfigProperties = serverConfigProperties;
    }

    @Bean
    public IStateGroupFactory roomStateGroupFactory() {
        return new RoomStateGroupFactory(this.serverConfigProperties.getRoomCapacity());
    }

    @Bean
    public ICharacterFactory characterFactory() {
        return new ChatterFactory();
    }

    @Bean
    public IWorkerManager workerManager() {
        return new WorkerManager(this.serverConfigProperties.getUpdateWaitTime(),
                this.serverConfigProperties.getStateGroupCapacity());
    }

    @Bean
    public Filter loginFilter() {
        return new LoginFilter();
    }

    @Bean
    public Dispatcher webChatDispatcher(IStateGroupFactory roomStateGroupFactory,
                                        ICharacterFactory characterFactory,
                                        IWorkerManager workerManager) {
        return new WebChatDispatcher(roomStateGroupFactory, characterFactory, workerManager);
    }
}
