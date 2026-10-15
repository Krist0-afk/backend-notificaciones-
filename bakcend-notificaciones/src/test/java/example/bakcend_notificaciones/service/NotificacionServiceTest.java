package example.bakcend_notificaciones.service;

import example.bakcend_notificaciones.model.Notificacion;
import example.bakcend_notificaciones.repository.NotificacionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceTest {

    @Mock
    private NotificacionRepository repository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotificacionService service;

    @Test
    void correoValidoSeEnviaYQuedaEnviado() {
        when(repository.save(any(Notificacion.class))).thenAnswer(inv -> inv.getArgument(0));

        Notificacion enviada = service.enviar("cliente@pedidos360.cl", "Compra", "Gracias por tu compra");

        verify(emailService).enviar("cliente@pedidos360.cl", "Compra", "Gracias por tu compra");
        assertThat(enviada.getEstado()).isEqualTo("ENVIADO");
    }

    @Test
    void correoInvalidoNoSeEnvia() {
        assertThatThrownBy(() -> service.enviar("no-es-un-correo", "Compra", "Hola"))
                .isInstanceOf(IllegalArgumentException.class);

        verify(emailService, never()).enviar(anyString(), anyString(), anyString());
        verify(repository, never()).save(any());
    }

    @Test
    void falloSeRegistraComoFallido() {
        service.registrarFallo(null, "Compra", "Correo inválido");

        ArgumentCaptor<Notificacion> captor = ArgumentCaptor.forClass(Notificacion.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getEstado()).isEqualTo("FALLIDO");
        assertThat(captor.getValue().getDestinatario()).isEqualTo("desconocido");
    }
}
