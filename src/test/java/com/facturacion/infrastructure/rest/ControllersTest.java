package com.facturacion.infrastructure.rest;

import com.facturacion.domain.model.ResultadoSincronizacion;
import com.facturacion.domain.port.in.CreateInvoiceUseCase;
import com.facturacion.domain.port.in.GenerarHechosUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ControllersTest {
    private final GenerarHechosUseCase etl = mock(GenerarHechosUseCase.class);
    private final CreateInvoiceUseCase invoices = mock(CreateInvoiceUseCase.class);
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new DruidController(etl), new InvoiceController(invoices)).build();
    }

    @Test
    void rejectedDruidSubmissionReturns502() throws Exception {
        when(etl.ejecutarProcesoHechos()).thenReturn(new ResultadoSincronizacion(false, 4, 10, "Druid no disponible"));
        mvc.perform(post("/api/druid/sync")).andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.hechosEnviados").value(0))
                .andExpect(jsonPath("$.message").value("Druid no disponible"));
    }

    @Test
    void acceptedDruidSubmissionReturns202RatherThanCompleted() throws Exception {
        when(etl.ejecutarProcesoHechos()).thenReturn(new ResultadoSincronizacion(true, 4, 10, "Pendiente"));
        mvc.perform(post("/api/druid/sync")).andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.hechosEnviados").value(4));
    }

    @Test
    void unexpectedEtlFailureReturns500() throws Exception {
        when(etl.ejecutarProcesoHechos()).thenThrow(new IllegalStateException("MySQL no disponible"));
        mvc.perform(post("/api/druid/sync")).andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    void emptyDruidSubmissionReturnsNoData() throws Exception {
        when(etl.ejecutarProcesoHechos()).thenReturn(new ResultadoSincronizacion(true, 0, 0, "Sin hechos"));
        mvc.perform(post("/api/druid/sync")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NO_DATA"));
    }

    @Test
    void invalidInvoiceReturns400WithMessage() throws Exception {
        when(invoices.createInvoice(any())).thenThrow(new IllegalArgumentException("Producto no encontrado"));
        mvc.perform(post("/api/invoices").contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Producto no encontrado"));
    }
}
