package example.bakcend_notificaciones.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

// Envío simulado: el correo se escribe en el log
@Slf4j
@Service
public class EmailService {

    @Value("${app.mail.remitente}")
    private String remitente;

    public void enviar(String destinatario, String asunto, String cuerpo) {
        log.info("""

                ==================== [EMAIL SIMULADO] ====================
                De:     {}
                Para:   {}
                Asunto: {}
                ----------------------------------------------------------
                {}
                ==========================================================""",
                remitente, destinatario, asunto, cuerpo);
    }
}
