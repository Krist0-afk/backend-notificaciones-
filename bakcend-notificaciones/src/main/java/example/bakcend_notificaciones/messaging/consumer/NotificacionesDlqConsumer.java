package example.bakcend_notificaciones.messaging.consumer;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

// Deja en el log los mensajes que llegan a la DLQ
@Slf4j
@Component
public class NotificacionesDlqConsumer {

    @RabbitListener(queues = {
            "${app.rabbitmq.queues.orden-creada-dlq}",
            "${app.rabbitmq.queues.usuario-creado-dlq}"},
            containerFactory = "dlqListenerContainerFactory")
    public void auditarDlq(Message message, Channel channel) throws IOException {
        log.error("[DLQ AUDITORÍA] Mensaje no procesado en {} | payload={} | x-death={}",
                message.getMessageProperties().getConsumerQueue(),
                new String(message.getBody(), StandardCharsets.UTF_8),
                message.getMessageProperties().getHeaders().get("x-death"));
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
