package example.bakcend_notificaciones.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EnviarNotificacionRequest(
        @NotBlank(message = "El destinatario es obligatorio")
        @Email(message = "El destinatario debe ser un correo válido")
        String destinatario,

        @NotBlank(message = "El asunto es obligatorio")
        @Size(max = 150, message = "El asunto no puede superar 150 caracteres")
        String asunto,

        @NotBlank(message = "El mensaje es obligatorio")
        @Size(max = 1000, message = "El mensaje no puede superar 1000 caracteres")
        String mensaje) {}
