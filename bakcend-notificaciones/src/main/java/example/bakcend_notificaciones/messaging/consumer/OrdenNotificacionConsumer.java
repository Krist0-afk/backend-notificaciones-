package example.bakcend_notificaciones.messaging.consumer;

import com.rabbitmq.client.Channel;
import example.bakcend_notificaciones.dto.OrdenCreadaEventDto;
import example.bakcend_notificaciones.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.stereotype.Component;

import java.io.IOException;

// Correo de confirmación de compra. Si el correo no es válido queda FALLIDO y va a la DLQ
@Slf4j
@Component
@RequiredArgsConstructor
public class OrdenNotificacionConsumer {

    private static final String ASUNTO = "Pedidos360 - Confirmación de tu compra";

    private final NotificacionService notificacionService;

    @RabbitListener(queues = "${app.rabbitmq.queues.orden-creada}")
    public void consumirOrdenCreada(OrdenCreadaEventDto evento, Message message, Channel channel) throws IOException {
        long tag = message.getMessageProperties().getDeliveryTag();
        boolean reentregado = Boolean.TRUE.equals(message.getMessageProperties().getRedelivered());
        log.info("[RabbitMQ Consumer] Orden recibida para notificar: {} (tag={}, redelivered={})", evento, tag, reentregado);

        try {
            if (evento.ordenId() == null) {
                throw new IllegalArgumentException("Evento sin ordenId");
            }
            String cuerpo = "¡Gracias por tu compra! Tu orden #" + evento.ordenId()
                    + " por $" + String.format("%,.0f", evento.total() != null ? evento.total() : 0.0)
                    + " fue recibida y está siendo procesada.";
            notificacionService.enviar(evento.usuarioEmail(), ASUNTO, cuerpo);
            channel.basicAck(tag, false);
            log.info("[RabbitMQ ACK] Confirmación de la orden #{} enviada", evento.ordenId());

        } catch (IllegalArgumentException ex) {
            log.error("[RabbitMQ NACK -> DLQ] Evento de orden inválido, no se reintenta: {}", ex.getMessage());
            notificacionService.registrarFallo(evento.usuarioEmail(), ASUNTO, "Orden #" + evento.ordenId() + ": " + ex.getMessage());
            channel.basicNack(tag, false, false);

        } catch (TransientDataAccessException ex) {
            if (!reentregado) {
                log.warn("[RabbitMQ RETRY] Falla temporal de BD, se reencola una vez (tag={})", tag);
                channel.basicNack(tag, false, true);
            } else {
                log.error("[RabbitMQ NACK -> DLQ] Reintento agotado por falla de BD (tag={})", tag);
                channel.basicNack(tag, false, false);
            }

        } catch (Exception ex) {
            log.error("[RabbitMQ NACK -> DLQ] Error inesperado notificando la orden: {}", ex.getMessage(), ex);
            channel.basicNack(tag, false, false);
        }
    }
}
