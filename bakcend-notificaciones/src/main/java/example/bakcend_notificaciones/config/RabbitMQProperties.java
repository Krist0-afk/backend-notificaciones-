package example.bakcend_notificaciones.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

// Nombres de colas y exchanges definidos en application.yml
@Data
@Configuration
@ConfigurationProperties(prefix = "app.rabbitmq")
public class RabbitMQProperties {

    private Exchanges exchanges = new Exchanges();
    private Queues queues = new Queues();
    private RoutingKeys routingKeys = new RoutingKeys();

    @Data
    public static class Exchanges {
        private String ordenesTopic;
        private String usuarioDirect;
        private String dlx;
    }

    @Data
    public static class Queues {
        private String ordenCreada;
        private String ordenCreadaDlq;
        private String usuarioCreado;
        private String usuarioCreadoDlq;
    }

    @Data
    public static class RoutingKeys {
        private String ordenCreada;
        private String usuarioCreado;
        private String ordenCreadaDlq;
        private String usuarioCreadoDlq;
    }
}
