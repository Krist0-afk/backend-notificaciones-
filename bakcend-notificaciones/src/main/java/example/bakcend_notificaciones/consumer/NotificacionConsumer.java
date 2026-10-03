package com.pedidos360.notificaciones.consumer;

import com.pedidos360.notificaciones.dto.UsuarioEventDto;
import com.pedidos360.notificaciones.model.Notificacion;
import com.pedidos360.notificaciones.repository.NotificacionRepository;
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
            UsuarioEventDto evento,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {

        try {
            System.out.println("Enviando correo de bienvenida a: " + evento.correo());
            
            // Simulamos una regla de fallo: Si no hay correo, lanzamos excepción
            if (evento.correo() == null || evento.correo().trim().isEmpty()) {
                throw new IllegalArgumentException("Correo de usuario inválido o vacío");
            }

            // Guardar historial exitoso
            repository.save(new Notificacion(evento.correo(), "¡Bienvenido a Pedidos360!", "ENVIADO"));

            // Confirmación explícita (ACK): El mensaje se procesó correctamente
            channel.basicAck(tag, false);
            System.out.println("Notificación exitosa. ACK enviado a RabbitMQ.");

        } catch (Exception e) {
            System.err.println("Falló el envío de notificación. Causa: " + e.getMessage());
            
            // Guardar historial de fallo
            repository.save(new Notificacion(evento.correo(), "¡Bienvenido a Pedidos360!", "FALLIDO"));

            // Rechazo explícito (NACK) con requeue=false: Envía el mensaje a la DLQ
            channel.basicNack(tag, false, false);
            System.err.println("NACK enviado. Mensaje derivado a la DLQ.");
        }
    }
}