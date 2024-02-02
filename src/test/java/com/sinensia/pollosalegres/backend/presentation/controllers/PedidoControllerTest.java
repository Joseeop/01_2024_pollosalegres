package com.sinensia.pollosalegres.backend.presentation.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinensia.pollosalegres.backend.business.model.Camarero;
import com.sinensia.pollosalegres.backend.business.model.Categoria;
import com.sinensia.pollosalegres.backend.business.model.DatosContacto;
import com.sinensia.pollosalegres.backend.business.model.Direccion;
import com.sinensia.pollosalegres.backend.business.model.Establecimiento;
import com.sinensia.pollosalegres.backend.business.model.Pedido;
import com.sinensia.pollosalegres.backend.business.model.Producto;
import com.sinensia.pollosalegres.backend.business.services.PedidoServices;
import com.sinensia.pollosalegres.backend.presentation.config.RespuestaError;

@WebMvcTest(value = PedidoController.class)
public class PedidoControllerTest {

	@Autowired
	private MockMvc miniPostman;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private PedidoServices pedidoServices;
	
	private Pedido pedido1;
	private Pedido pedido2;
	
	public PedidoControllerTest() {
		initObjects();
	}
	
	@Test
	void solicitamos_todos_los_pedidos() throws Exception {

		List<Pedido> pedidos = Arrays.asList(pedido1, pedido2);

		when(pedidoServices.getAll()).thenReturn(pedidos);

		MvcResult respuesta = miniPostman.perform(get("/pedidos")).andExpect(status().isOk()).andReturn();

		String responseBody = respuesta.getResponse().getContentAsString(StandardCharsets.UTF_8);
		String responseBodyEsperada = objectMapper.writeValueAsString(pedidos);

		assertThat(responseBody).isEqualToIgnoringWhitespace(responseBodyEsperada);
	}

	@Test
	void solicitamos_pedido_EXISTENTE_a_partir_de_su_codigo() throws Exception {
		
		when(pedidoServices.read(1000L)).thenReturn(Optional.of(pedido1));
		
		MvcResult respuesta = miniPostman.perform(get("/pedidos/1000"))
											.andExpect(status().isOk())
											.andReturn();
		
		String responseBody = respuesta.getResponse().getContentAsString(StandardCharsets.UTF_8);
		String responseBodyEsperada = objectMapper.writeValueAsString(pedido1);
		
		assertThat(responseBody).isEqualToIgnoringWhitespace(responseBodyEsperada);
	}
	
	@Test
	void solicitamos_pedido_NO_EXISTENTE_a_partir_de_su_codigo() throws Exception {
		
		when(pedidoServices.read(100L)).thenReturn(Optional.empty());
		
		MvcResult respuesta = miniPostman.perform(get("/pedidos/100"))
											.andExpect(status().isNotFound())
											.andReturn();
		
		RespuestaError respuestaError = new RespuestaError("No existe el pedido 100");
		
		String responseBody = respuesta.getResponse().getContentAsString(StandardCharsets.UTF_8);
		String responseBodyEsperada = objectMapper.writeValueAsString(respuestaError);
		
		assertThat(responseBody).isEqualToIgnoringWhitespace(responseBodyEsperada);	
	}
	
	@Test
	void creamos_pedido_ok() throws Exception {

		pedido1.setNumero(null);

		when(pedidoServices.create(pedido1)).thenReturn(1000L);

		String requestBody = objectMapper.writeValueAsString(pedido1);

		miniPostman.perform(post("/pedidos").content(requestBody).contentType("application/json"))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "http://localhost/pedidos/1000"));
	}
	
	@Test
	void creamos_pedido_con_codigo_NO_null() throws Exception {
			
		when(pedidoServices.create(pedido1)).thenThrow(new IllegalStateException("El código del pedido debe ser nulo."));
	
		String requestBody = objectMapper.writeValueAsString(pedido1);
		
		MvcResult respuesta = miniPostman.perform(post("/pedidos").content(requestBody).contentType("application/json"))
													.andExpect(status().isBadRequest())
													.andReturn();
	
		RespuestaError respuestaError = new RespuestaError("El código del pedido debe ser nulo.");
		
		String responseBody = respuesta.getResponse().getContentAsString(StandardCharsets.UTF_8);
		String responseBodyEsperada = objectMapper.writeValueAsString(respuestaError);
		
		assertThat(responseBody).isEqualToIgnoringWhitespace(responseBodyEsperada);	
	}
	
	@Test
	void actualizamos_pedido_ok() throws Exception {

		String requestBody = objectMapper.writeValueAsString(pedido1);
		
		miniPostman.perform(put("/pedidos/1000").content(requestBody).contentType("application/json"))
						.andExpect(status().isNoContent());
		
		verify(pedidoServices, times(1)).update(pedido1);
	}
	
	
	@Test
	void actualizamos_pedido_con_codigo_null() throws Exception {
		
		doThrow(new IllegalStateException("EL MENSAJE")).when(pedidoServices).update(pedido1);
		
		String requestBody = objectMapper.writeValueAsString(pedido1);
		
		MvcResult respuesta = miniPostman.perform(put("/pedidos/1000").content(requestBody).contentType("application/json"))
				 							.andExpect(status().isNotFound())
				 							.andReturn();

		verify(pedidoServices, times(1)).update(pedido1);

		RespuestaError respuestaError = new RespuestaError("EL MENSAJE");

		String responseBody = respuesta.getResponse().getContentAsString(StandardCharsets.UTF_8);
		String responseBodyEsperada = objectMapper.writeValueAsString(respuestaError);

		assertThat(responseBody).isEqualToIgnoringWhitespace(responseBodyEsperada);	
	}
	
	// *************************************************
	//
	// Private Methods
	//
	// *************************************************
	
	private void initObjects() {
		
		Camarero camarero1 = new Camarero();
		Camarero camarero2 = new Camarero();
		Camarero camarero3 = new Camarero();

		Direccion direccion1 = new Direccion();
		Direccion direccion2 = new Direccion();
		Direccion direccion3 = new Direccion();

		DatosContacto datosContacto1 = new DatosContacto();
		DatosContacto datosContacto2 = new DatosContacto();
		DatosContacto datosContacto3 = new DatosContacto();

		direccion1.setDireccion("c/ Padilla, 230 ático 2");
		direccion1.setPoblacion("Barcelona");
		direccion1.setCodigoPostal("80934");
		direccion1.setProvincia("Barcelona");
		direccion1.setPais("España");
		datosContacto1.setTelefono("932218772");
		datosContacto1.setFax(null);
		datosContacto1.setEmail("pablofer334@hotmail.com");

		direccion2.setDireccion("Avda. Pintor Garriño, 230-232");
		direccion2.setPoblacion("Móstoles");
		direccion2.setCodigoPostal("91002");
		direccion2.setProvincia("Madrid");
		direccion2.setPais("España");
		datosContacto2.setTelefono("912293444");
		datosContacto2.setFax(null);
		datosContacto2.setEmail("annabado@gmail.com");

		direccion3.setDireccion("c/ Pez Volador, 2 4º 2ª");
		direccion3.setPoblacion("Madrid");
		direccion3.setCodigoPostal("91240");
		direccion3.setProvincia("Madrid");
		direccion3.setPais("España");
		datosContacto3.setTelefono("912547821");
		datosContacto3.setFax(null);
		datosContacto3.setEmail("pacoort@gmail.com");

		camarero1.setId(100L);
		camarero1.setDni("27884178R");
		camarero1.setNombre("Pablo");
		camarero1.setApellido1("Fernández");
		camarero1.setApellido2("Borlán");
		camarero1.setDireccion(direccion1);
		camarero1.setDatosContacto(datosContacto1);
		camarero1.setLicenciaManipuladorAlimentos("LMA4998111253R");

		camarero2.setId(101L);
		camarero2.setDni("30092123H");
		camarero2.setNombre("Ana");
		camarero2.setApellido1("Badosa");
		camarero2.setApellido2("Domingo");
		camarero2.setDireccion(direccion2);
		camarero2.setDatosContacto(datosContacto2);
		camarero2.setLicenciaManipuladorAlimentos("LMA9000238712F");

		camarero3.setId(102L);
		camarero3.setDni("45099812W");
		camarero3.setNombre("Francisco Javier");
		camarero3.setApellido1("Ort");
		camarero3.setApellido2("Montcunill");
		camarero3.setDireccion(direccion3);
		camarero3.setDatosContacto(datosContacto3);
		camarero3.setLicenciaManipuladorAlimentos("LMA9033289712G");
		
		Categoria categoria1 = new Categoria();
		categoria1.setId(100L);
		categoria1.setNombre("TAPAS");
		
		Categoria categoria2 = new Categoria();
		categoria2.setId(101L);
		categoria2.setNombre("REFRESCOS");
		
		Establecimiento establecimiento1 = new Establecimiento();
		Establecimiento establecimiento2 = new Establecimiento();
		
		establecimiento1.setCodigo(100L);
		establecimiento2.setCodigo(101L);
		

		
		establecimiento1.setDatosContacto(datosContacto1);	
		establecimiento2.setDatosContacto(datosContacto2);
		establecimiento1.setDireccion(direccion1);
		establecimiento2.setDireccion(direccion2);
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		
		Date fecha1 = null;
		Date fecha2 = null;
		
		try {
			fecha1 = sdf.parse("14/04/2005");
			fecha2 = sdf.parse("24/11/1999");
		} catch (ParseException e) {
			
		}
	
		establecimiento1.setFechaInauguracion(fecha1);
		establecimiento2.setFechaInauguracion(fecha2);
		
		establecimiento1.setNombreComercial("Pollos Felices - La Vaguada");
		establecimiento2.setNombreComercial("Pollos Felices - Granvia  2");
		
		Producto producto1 = new Producto();
		Producto producto2 = new Producto();

		producto1.setCodigo(1000L);
		producto1.setNombre("Tortilla Vegana");
		producto1.setDescripcion("En vez de huevos hay garbanzos.");
		producto1.setPrecio(10);
		producto1.setCategoria(categoria1);
		producto1.setFechaAlta(fecha1);
		producto1.setDescatalogado(false);

		producto2.setCodigo(1012L);
		producto2.setNombre("El Gaitero");
		producto2.setDescripcion("Joroña que Joroña");
		producto2.setPrecio(8);
		producto2.setCategoria(categoria2);
		producto2.setFechaAlta(fecha2);
		producto2.setDescatalogado(false);
		
		pedido1 = new Pedido();
		pedido2 = new Pedido();
		
		pedido1.setCamarero(camarero3);
		pedido1.setCliente(null);
		pedido1.setEstablecimiento(establecimiento2);
		pedido1.setFecha(fecha2);
		pedido1.setLineas(null);
		pedido1.setNumero(100L);
		pedido1.setEstado(null);
		
		pedido2.setCamarero(camarero2);
		pedido2.setCliente(null);
		pedido2.setEstablecimiento(establecimiento1);
		pedido2.setFecha(fecha1);
		pedido2.setLineas(null);
		pedido2.setNumero(null);
		pedido2.setEstado(null);
		
	}
	
}
