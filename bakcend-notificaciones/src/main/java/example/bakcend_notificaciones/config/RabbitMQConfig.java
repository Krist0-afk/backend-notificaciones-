package example.bakcend_notificaciones.config;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.boot.amqp.autoconfigure.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Colas de notificaciones (orden creada y usuario creado), cada una con su DLQ
@Configuration
@RequiredArgsConstructor
public class RabbitMQConfig {

    private final RabbitMQProperties props;

    // Exchanges

    @Bean
    public TopicExchange ordenesTopicExchange() {
        return ExchangeBuilder.topicExchange(props.getExchanges().getOrdenesTopic()).durable(true).build();
    }

    @Bean
    public DirectExchange usuarioDirectExchange() {
        return ExchangeBuilder.directExchange(props.getExchanges().getUsuarioDirect()).durable(true).build();
    }

    @Bean
    public DirectExchange notificacionesDeadLetterExchange() {
        return ExchangeBuilder.directExchange(props.getExchanges().getDlx()).durable(true).build();
    }

    // Orden creada

    @Bean
    public Queue ordenCreadaQueue() {
        return QueueBuilder.durable(props.getQueues().getOrdenCreada())
                .withArgument("x-dead-letter-exchange", props.getExchanges().getDlx())
                .withArgument("x-dead-letter-routing-key", props.getRoutingKeys().getOrdenCreadaDlq())
                .build();
    }

    @Bean
    public Queue ordenCreadaDlq() {
        return QueueBuilder.durable(props.getQueues().getOrdenCreadaDlq()).build();
    }

    @Bean
    public Binding bindingOrdenCreada() {
        return BindingBuilder.bind(ordenCreadaQueue()).to(ordenesTopicExchange())
                .with(props.getRoutingKeys().getOrdenCreada());
    }

    @Bean
    public Binding bindingOrdenCreadaDlq() {
        return BindingBuilder.bind(ordenCreadaDlq()).to(notificacionesDeadLetterExchange())
                .with(props.getRoutingKeys().getOrdenCreadaDlq());
    }

    // Usuario creado

    @Bean
    public Queue usuarioCreadoQueue() {
        return QueueBuilder.durable(props.getQueues().getUsuarioCreado())
                .withArgument("x-dead-letter-exchange", props.getExchanges().getDlx())
                .withArgument("x-dead-letter-routing-key", props.getRoutingKeys().getUsuarioCreadoDlq())
                .build();
    }

    @Bean
    public Queue usuarioCreadoDlq() {
        return QueueBuilder.durable(props.getQueues().getUsuarioCreadoDlq()).build();
    }

    @Bean
    public Binding bindingUsuarioCreado() {
        return BindingBuilder.bind(usuarioCreadoQueue()).to(usuarioDirectExchange())
                .with(props.getRoutingKeys().getUsuarioCreado());
    }

    @Bean
    public Binding bindingUsuarioCreadoDlq() {
        return BindingBuilder.bind(usuarioCreadoDlq()).to(notificacionesDeadLetterExchange())
                .with(props.getRoutingKeys().getUsuarioCreadoDlq());
    }

    // Las DLQ se leen como texto para poder registrar también mensajes con JSON inválido
    @Bean
    public SimpleRabbitListenerContainerFactory dlqListenerContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer, ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setMessageConverter(new SimpleMessageConverter());
        return factory;
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
