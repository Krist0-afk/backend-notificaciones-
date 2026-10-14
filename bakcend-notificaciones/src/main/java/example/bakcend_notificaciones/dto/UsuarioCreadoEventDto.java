package example.bakcend_notificaciones.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UsuarioCreadoEventDto(Long usuarioId, String nombre, String correo) {}
