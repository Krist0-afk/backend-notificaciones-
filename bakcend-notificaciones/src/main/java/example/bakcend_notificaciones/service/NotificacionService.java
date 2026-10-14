package example.bakcend_notificaciones.service;

import example.bakcend_notificaciones.model.Notificacion;
import example.bakcend_notificaciones.repository.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class NotificacionService {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final NotificacionRepository repository;
    private final EmailService emailService;

    // Lanza IllegalArgumentException si el correo no es válido
    public Notificacion enviar(String destinatario, String asunto, String mensaje) {
        if (destinatario == null || !EMAIL.matcher(destinatario.trim()).matches()) {
            throw new IllegalArgumentException("Destinatario inválido: '" + destinatario + "'");
        }
        emailService.enviar(destinatario.trim(), asunto, mensaje);
        return repository.save(new Notificacion(destinatario.trim(), asunto, mensaje, "ENVIADO"));
    }

    public void registrarFallo(String destinatario, String asunto, String motivo) {
        String para = (destinatario == null || destinatario.isBlank()) ? "desconocido" : destinatario;
        repository.save(new Notificacion(para, asunto, motivo, "FALLIDO"));
    }

    public List<Notificacion> listar(String destinatario) {
        return destinatario == null || destinatario.isBlank()
                ? repository.findAll()
                : repository.findByDestinatarioOrderByFechaDesc(destinatario);
    }
}
