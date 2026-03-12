package in.leesum.portless.spring;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PortlessPropertiesTest {

    @Test
    void defaultValues() {
        PortlessProperties props = new PortlessProperties();

        assertThat(props.isEnabled()).isTrue();
        assertThat(props.getName()).isNull();
        assertThat(props.getStateDir()).isNull();
        assertThat(props.getMinPort()).isEqualTo(8000);
        assertThat(props.getMaxPort()).isEqualTo(8999);
        assertThat(props.isForce()).isFalse();
        assertThat(props.getTld()).isNull();
    }

    @Test
    void settersAndGetters() {
        PortlessProperties props = new PortlessProperties();
        props.setEnabled(false);
        props.setName("myapp");
        props.setStateDir("/custom");
        props.setMinPort(5000);
        props.setMaxPort(5999);
        props.setForce(true);
        props.setTld("test");

        assertThat(props.isEnabled()).isFalse();
        assertThat(props.getName()).isEqualTo("myapp");
        assertThat(props.getStateDir()).isEqualTo("/custom");
        assertThat(props.getMinPort()).isEqualTo(5000);
        assertThat(props.getMaxPort()).isEqualTo(5999);
        assertThat(props.isForce()).isTrue();
        assertThat(props.getTld()).isEqualTo("test");
    }
}
