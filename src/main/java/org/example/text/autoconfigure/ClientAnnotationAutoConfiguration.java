package org.example.text.autoconfigure;

import org.example.text.service.ClientAnnotationService;
import org.example.text.config.ServerClineConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

@Configuration
@ConditionalOnClass(ClientAnnotationService.class)
@EnableConfigurationProperties({ServerClineConfig.class}) // 只启用自定义配置
public class ClientAnnotationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ClientAnnotationService clientAnnotationService(
            ApplicationContext applicationContext,
            ServerClineConfig serverClineConfig,
            Environment environment) { // 注入Environment来获取标准server配置

        Integer serverPort = environment.getProperty("server.port", Integer.class, 8080);

        return new ClientAnnotationService(applicationContext, serverClineConfig,
                applicationContext.getEnvironment().getProperty("spring.application.name"),
                serverPort);
    }
}
