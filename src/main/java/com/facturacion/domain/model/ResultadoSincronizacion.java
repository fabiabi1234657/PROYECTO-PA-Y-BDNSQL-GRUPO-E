package com.facturacion.domain.model;

/** El envío aceptado no implica que la tarea de ingesta haya terminado. */
public record ResultadoSincronizacion(boolean exitoso, int totalHechos,
                                     double gananciaTotal, String mensaje) {
}
