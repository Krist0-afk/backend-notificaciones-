package example.bakcend_notificaciones.controller;

import example.bakcend_notificaciones.dto.EnviarNotificacionRequest;
import example.bakcend_notificaciones.model.Notificacion;
import example.bakcend_notificaciones.service.NotificacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    // Envío manual, para probar desde Postman
    @PostMapping("/enviar")
    public ResponseEntity<Notificacion> enviar(@Valid @RequestBody EnviarNotificacionRequest request) {
        Notificacion enviada = notificacionService.enviar(request.destinatario(), request.asunto(), request.mensaje());
        return ResponseEntity.status(HttpStatus.CREATED).body(enviada);
    }

    // Historial, se puede filtrar por destinatario
    @GetMapping
    public List<Notificacion> listar(@RequestParam(required = false) String destinatario) {
        return notificacionService.listar(destinatario);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> manejarValidaciones(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err -> errores.put(err.getField(), err.getDefaultMessage()));
        return ResponseEntity.badRequest().body(Map.of("error", "Parámetros de entrada inválidos", "detalles", errores));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> manejarArgumentoInvalido(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}
