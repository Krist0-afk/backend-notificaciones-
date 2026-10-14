package example.bakcend_notificaciones.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrdenCreadaEventDto(Long ordenId, String usuarioEmail, Double total) {}
