package com.facturacion.domain.port.in;

import com.facturacion.domain.model.ResultadoSincronizacion;

public interface GenerarHechosUseCase {
    ResultadoSincronizacion ejecutarProcesoHechos();
}
