package example.bakcend_notificaciones.messaging.consumer;

import com.rabbitmq.client.Channel;
import example.bakcend_notificaciones.dto.UsuarioCreadoEventDto;
import example.bakcend_notificaciones.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.stereotype.Component;

import java.io.IOException;

// Correo de bienvenida la primera vez que el usuario inicia sesión
@Slf4j
@Component
@RequiredArgsConstructor
public class UsuarioNotificacionConsumer {

    private static final String ASUNTO = "Bienvenido a Pedidos360";

    private final NotificacionService notificacionService;

    @RabbitListener(queues = "${app.rabbitmq.queues.usuario-creado}")
    public void consumirUsuarioCreado(UsuarioCreadoEventDto evento, Message message, Channel channel) throws IOException {
        long tag = message.getMessageProperties().getDeliveryTag();
        boolean reentregado = Boolean.TRUE.equals(message.getMessageProperties().getRedelivered());
        log.info("[RabbitMQ Consumer] Usuario nuevo recibido: {} (tag={}, redelivered={})", evento, tag, reentregado);

        try {
            String nombre = evento.nombre() != null && !evento.nombre().isBlank() ? evento.nombre() : "cliente";
            String cuerpo = "Hola " + nombre + ", tu cuenta en Pedidos360 quedó registrada. ¡Ya puedes hacer tus pedidos!";
            notificacionService.enviar(evento.correo(), ASUNTO, cuerpo);
            channel.basicAck(tag, false);
            log.info("[RabbitMQ ACK] Bienvenida enviada a {}", evento.correo());

        } catch (IllegalArgumentException ex) {
            log.error("[RabbitMQ NACK -> DLQ] Evento de usuario inválido, no se reintenta: {}", ex.getMessage());
            notificacionService.registrarFallo(evento.correo(), ASUNTO, ex.getMessage());
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
            log.error("[RabbitMQ NACK -> DLQ] Error inesperado notificando al usuario: {}", ex.getMessage(), ex);
            channel.basicNack(tag, false, false);
        }
    }
}
