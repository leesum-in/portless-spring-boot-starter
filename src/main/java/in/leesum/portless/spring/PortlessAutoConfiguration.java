package in.leesum.portless.spring;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@AutoConfiguration
@ConditionalOnProperty(name = "portless.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(PortlessProperties.class)
public class PortlessAutoConfiguration {

    @Bean
    @ConditionalOnProperty(PortlessEnvironmentPostProcessor.INTERNAL_NAME_KEY)
    PortlessRouteManager portlessRouteManager() {
        return new PortlessRouteManager();
    }

    @Bean
    @ConditionalOnProperty(PortlessEnvironmentPostProcessor.INTERNAL_NAME_KEY)
    PortlessLifecycleManager portlessLifecycleManager(PortlessProperties properties,
                                                       PortlessRouteManager routeManager,
                                                       Environment environment) {
        String name = environment.getProperty(PortlessEnvironmentPostProcessor.INTERNAL_NAME_KEY);
        return new PortlessLifecycleManager(routeManager, name, properties.isForce());
    }
}
