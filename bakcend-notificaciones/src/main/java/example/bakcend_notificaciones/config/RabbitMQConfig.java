package example.bakcend_notificaciones.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.principal}")
    private String exchangePrincipal;

    @Value("${rabbitmq.queue.principal}")
    private String queuePrincipal;

    @Value("${rabbitmq.routing-key.principal}")
    private String routingKeyPrincipal;

    @Value("${rabbitmq.queue.dlq}")
    private String queueDlq;

    @Value("${rabbitmq.routing-key.dlq}")
    private String routingKeyDlq;

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(exchangePrincipal);
    }

    // --- COLA PRINCIPAL (Con desvío a DLQ) ---
    @Bean
    public Queue notificacionQueue() {
        return QueueBuilder.durable(queuePrincipal)
                .withArgument("x-dead-letter-exchange", exchangePrincipal) 
                .withArgument("x-dead-letter-routing-key", routingKeyDlq)
                .build();
    }

    @Bean
    public Binding bindingPrincipal(Queue notificacionQueue, DirectExchange exchange) {
        return BindingBuilder.bind(notificacionQueue).to(exchange).with(routingKeyPrincipal);
    }

    // --- DEAD LETTER QUEUE (Mensajes fallidos) ---
    @Bean
    public Queue notificacionDlq() {
        return QueueBuilder.durable(queueDlq).build();
    }

    @Bean
    public Binding bindingDlq(Queue notificacionDlq, DirectExchange exchange) {
        return BindingBuilder.bind(notificacionDlq).to(exchange).with(routingKeyDlq);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}