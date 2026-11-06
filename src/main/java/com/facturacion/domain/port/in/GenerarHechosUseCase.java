package com.facturacion.domain.port.in;

import com.facturacion.domain.model.VentaFact;

import java.util.List;

public interface GenerarHechosUseCase {
    List<VentaFact> ejecutarProcesoHechos();
}
