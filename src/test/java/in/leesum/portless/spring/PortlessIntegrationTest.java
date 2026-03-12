package in.leesum.portless.spring;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class PortlessIntegrationTest {

    @Test
    void fullConfigurationLoads_whenDetected() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(PortlessAutoConfiguration.class))
                .withPropertyValues(
                        "portless.name=integration-test",
                        "portless.force=true",
                        "portless._internal.name=integration-test"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(PortlessProperties.class);
                    assertThat(context).hasSingleBean(PortlessRouteManager.class);
                    assertThat(context).hasSingleBean(PortlessLifecycleManager.class);

                    PortlessProperties props = context.getBean(PortlessProperties.class);
                    assertThat(props.getName()).isEqualTo("integration-test");
                    assertThat(props.isForce()).isTrue();
                });
    }
}
