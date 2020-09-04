package org.cn.monkey.webchat.config;

import org.cn.monkey.netty.Dispatcher;
import org.cn.monkey.netty.Filter;
import org.cn.monkey.netty.NettyServer;
import org.cn.monkey.netty.ProtoWebSocketHandler;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Configuration
public class NettyConfig implements ApplicationRunner, ApplicationContextAware {

    @Value("${spring.netty.port}")
    int port;

    private ApplicationContext applicationContext;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        this.start();
    }

    private void start() throws InterruptedException {
        Map<String, Filter> filterMap = this.applicationContext.getBeansOfType(Filter.class);
        List<Filter> filters = null;
        if (filterMap.size() > 0) {
            filters = filterMap.values().stream().filter(Objects::nonNull).collect(Collectors.toList());
        }
        ProtoWebSocketHandler webSocketHandler = new ProtoWebSocketHandler(filters, applicationContext.getBean(Dispatcher.class));
        new NettyServer(this.port, webSocketHandler).start();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
