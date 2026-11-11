package com.facturacion.infrastructure.rest;

import com.facturacion.domain.model.ResultadoSincronizacion;
import com.facturacion.domain.port.in.CreateInvoiceUseCase;
import com.facturacion.domain.port.in.GenerarHechosUseCase;
import com.facturacion.domain.port.in.RegisterPersonUseCase;
import com.facturacion.domain.port.in.GetProductsUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpHeaders;
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ControllersTest {
    private final GenerarHechosUseCase etl = mock(GenerarHechosUseCase.class);
    private final CreateInvoiceUseCase invoices = mock(CreateInvoiceUseCase.class);
    private final RegisterPersonUseCase persons = mock(RegisterPersonUseCase.class);
    private final GetProductsUseCase products = mock(GetProductsUseCase.class);
    private MockMvc mvc;
    private GenericWebApplicationContext context;

    @Configuration
    @EnableWebMvc
    static class MvcConfig {}

    @BeforeEach
    void setUp() {
        context = new GenericWebApplicationContext();
        context.setServletContext(new MockServletContext());
        new AnnotatedBeanDefinitionReader(context).register(MvcConfig.class);
        context.registerBean(DruidController.class, () -> new DruidController(etl));
        context.registerBean(InvoiceController.class, () -> new InvoiceController(invoices));
        context.registerBean(PersonController.class, () -> new PersonController(persons));
        context.registerBean(ProductController.class, () -> new ProductController(products));
        context.refresh();
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    @ParameterizedTest
    @CsvSource({
            "/api/druid/sync, http://localhost:3000",
            "/api/druid/sync, http://127.0.0.1:3000",
            "/api/invoices, http://localhost:3000",
            "/api/invoices, http://127.0.0.1:3000",
            "/api/persons, http://localhost:3000",
            "/api/persons, http://127.0.0.1:3000",
            "/api/products, http://localhost:3000",
            "/api/products, http://127.0.0.1:3000"
    })
    void allowsLocalFrontendPreflight(String path, String origin) throws Exception {
        mvc.perform(options(path).header(HttpHeaders.ORIGIN, origin)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin));
        verifyNoInteractions(etl, invoices, persons, products);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/druid/sync", "/api/invoices", "/api/persons", "/api/products"})
    void rejectsUnexpectedOriginWithoutExecutingUseCase(String path) throws Exception {
        mvc.perform(post(path).header(HttpHeaders.ORIGIN, "https://untrusted.example")
                        .contentType("application/json").content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        verifyNoInteractions(etl, invoices, persons, products);
    }

    @ParameterizedTest
    @ValueSource(strings = {"http://localhost:3000", "http://127.0.0.1:3000"})
    void localFrontendStillReceivesDruidResponse(String origin) throws Exception {
        when(etl.ejecutarProcesoHechos()).thenReturn(new ResultadoSincronizacion(true, 4, 10, "Pendiente"));
        mvc.perform(post("/api/druid/sync").header(HttpHeaders.ORIGIN, origin))
                .andExpect(status().isAccepted())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin))
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.hechosEnviados").value(4))
                .andExpect(jsonPath("$.gananciaTotal").value(10))
                .andExpect(jsonPath("$.mensaje").value("Pendiente"))
                .andExpect(jsonPath("$.message").value("Pendiente"));
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
