package com.facturacion.domain.port.out;

import com.facturacion.domain.model.VentaFact;

import java.util.List;

public interface DruidIngestionPort {
    boolean enviarHechosADruid(List<VentaFact> facts);
}
