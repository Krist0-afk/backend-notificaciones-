package example.bakcend_notificaciones.consumer;

import example.bakcend_notificaciones.dto.OrdenEventDto;
import example.bakcend_notificaciones.model.Notificacion;
import example.bakcend_notificaciones.repository.NotificacionRepository;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class NotificacionConsumer {

    private final NotificacionRepository repository;

    @RabbitListener(queues = "${rabbitmq.queue.principal}")
    public void procesarNotificacion(
            OrdenEventDto evento,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {

        try {
            System.out.println("Generando recibo de compra para: " + evento.usuarioEmail());
            
            if (evento.usuarioEmail() == null || evento.usuarioEmail().trim().isEmpty()) {
                throw new IllegalArgumentException("Correo vacío");
            }

            String comprobante = "Tu orden #" + evento.ordenId() + " por $" + evento.total() + " fue procesada.";
            repository.save(new Notificacion(evento.usuarioEmail(), comprobante, "ENVIADO"));

            // ACK: Todo salió bien
            channel.basicAck(tag, false);

        } catch (Exception e) {
            System.err.println("Error procesando notificación: " + e.getMessage());
            
            String email = (evento != null && evento.usuarioEmail() != null) ? evento.usuarioEmail() : "Desc";
            repository.save(new Notificacion(email, "Fallo orden #" + (evento != null ? evento.ordenId() : "N/A"), "FALLIDO"));

            // NACK: Enviar a la DLQ
            channel.basicNack(tag, false, false);
        }
    }
}