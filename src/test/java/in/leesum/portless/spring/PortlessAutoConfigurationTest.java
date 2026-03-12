package in.leesum.portless.spring;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class PortlessAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(PortlessAutoConfiguration.class));

    @Test
    void beansNotCreated_whenDisabled() {
        contextRunner
                .withPropertyValues("portless.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(PortlessRouteManager.class);
                    assertThat(context).doesNotHaveBean(PortlessLifecycleManager.class);
                });
    }

    @Test
    void beansNotCreated_whenNoDetectedState() {
        contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(PortlessRouteManager.class);
            assertThat(context).doesNotHaveBean(PortlessLifecycleManager.class);
        });
    }

    @Test
    void beansCreated_whenDetected() {
        contextRunner
                .withPropertyValues("portless._internal.name=myapp")
                .run(context -> {
                    assertThat(context).hasSingleBean(PortlessRouteManager.class);
                    assertThat(context).hasSingleBean(PortlessLifecycleManager.class);
                });
    }

    @Test
    void propertiesBound() {
        contextRunner
                .withPropertyValues(
                        "portless.name=testapp",
                        "portless.min-port=5000",
                        "portless.max-port=5999",
                        "portless.force=true",
                        "portless.tld=test"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(PortlessProperties.class);
                    PortlessProperties props = context.getBean(PortlessProperties.class);
                    assertThat(props.getName()).isEqualTo("testapp");
                    assertThat(props.getMinPort()).isEqualTo(5000);
                    assertThat(props.getMaxPort()).isEqualTo(5999);
                    assertThat(props.isForce()).isTrue();
                    assertThat(props.getTld()).isEqualTo("test");
                });
    }
}
