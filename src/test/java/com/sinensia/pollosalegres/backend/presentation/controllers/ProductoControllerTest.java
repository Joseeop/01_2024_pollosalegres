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

import java.util.List;
import java.util.Optional;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinensia.pollosalegres.backend.business.model.Categoria;
import com.sinensia.pollosalegres.backend.business.model.Producto;
import com.sinensia.pollosalegres.backend.business.services.ProductoServices;
import com.sinensia.pollosalegres.backend.presentation.config.RespuestaError;

@WebMvcTest(value = ProductoController.class)
class ProductoControllerTest {

	@Autowired
	private MockMvc miniPostman;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private ProductoServices productoServices;

	private Producto producto1;
	private Producto producto2;

	@BeforeEach
	void init() {
		initObjects();
	}

	@Test
	void solicitamos_todos_los_productos() throws Exception {

		List<Producto> productos = Arrays.asList(producto1, producto2);

		when(productoServices.getAll()).thenReturn(productos);

		MvcResult respuesta = miniPostman.perform(get("/productos")).andExpect(status().isOk()).andReturn();

		String responseBody = respuesta.getResponse().getContentAsString(StandardCharsets.UTF_8);
		String responseBodyEsperada = objectMapper.writeValueAsString(productos);

		assertThat(responseBody).isEqualToIgnoringWhitespace(responseBodyEsperada);
	}

	@Test
	void solicitamos_producto_EXISTENTE_a_partir_de_su_codigo() throws Exception {
		
		when(productoServices.read(1011L)).thenReturn(Optional.of(producto1));
		
		MvcResult respuesta = miniPostman.perform(get("/productos/1011"))
											.andExpect(status().isOk())
											.andReturn();
		
		String responseBody = respuesta.getResponse().getContentAsString(StandardCharsets.UTF_8);
		String responseBodyEsperada = objectMapper.writeValueAsString(producto1);
		
		assertThat(responseBody).isEqualToIgnoringWhitespace(responseBodyEsperada);
	}

	@Test
	void solicitamos_producto_NO_EXISTENTE_a_partir_de_su_codigo() throws Exception {
		
		when(productoServices.read(100L)).thenReturn(Optional.empty());
		
		MvcResult respuesta = miniPostman.perform(get("/productos/100"))
											.andExpect(status().isNotFound())
											.andReturn();
		
		RespuestaError respuestaError = new RespuestaError("No existe el producto 100");
		
		String responseBody = respuesta.getResponse().getContentAsString(StandardCharsets.UTF_8);
		String responseBodyEsperada = objectMapper.writeValueAsString(respuestaError);
		
		assertThat(responseBody).isEqualToIgnoringWhitespace(responseBodyEsperada);	
	}

	@Test
	void creamos_producto_ok() throws Exception {

		producto1.setCodigo(null);

		when(productoServices.create(producto1)).thenReturn(1011L);

		String requestBody = objectMapper.writeValueAsString(producto1);

		miniPostman.perform(post("/productos").content(requestBody).contentType("application/json"))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "http://localhost/productos/1011"));
	}

	@Test
	void creamos_producto_con_codigo_NO_null() throws Exception {
			
		when(productoServices.create(producto1)).thenThrow(new IllegalStateException("El código del producto debe ser nulo."));
	
		String requestBody = objectMapper.writeValueAsString(producto1);
		
		MvcResult respuesta = miniPostman.perform(post("/productos").content(requestBody).contentType("application/json"))
													.andExpect(status().isBadRequest())
													.andReturn();
	
		RespuestaError respuestaError = new RespuestaError("El código del producto debe ser nulo.");
		
		String responseBody = respuesta.getResponse().getContentAsString(StandardCharsets.UTF_8);
		String responseBodyEsperada = objectMapper.writeValueAsString(respuestaError);
		
		assertThat(responseBody).isEqualToIgnoringWhitespace(responseBodyEsperada);	
	}
	
	@Test
	void actualizamos_producto_ok() throws Exception {

		String requestBody = objectMapper.writeValueAsString(producto1);
		
		miniPostman.perform(put("/productos/1011").content(requestBody).contentType("application/json"))
						.andExpect(status().isNoContent());
		
		verify(productoServices, times(1)).update(producto1);
	}
	
	@Test
	void actualizamos_producto_con_codigo_null() throws Exception {
		
		doThrow(new IllegalStateException("EL MENSAJE")).when(productoServices).update(producto1);
		
		String requestBody = objectMapper.writeValueAsString(producto1);
		
		MvcResult respuesta = miniPostman.perform(put("/productos/1011").content(requestBody).contentType("application/json"))
				 							.andExpect(status().isNotFound())
				 							.andReturn();

		verify(productoServices, times(1)).update(producto1);

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

		Date fecha1 = null;
		Date fecha2 = null;

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

		try {
			fecha1 = sdf.parse("02/11/2003");
			fecha2 = sdf.parse("27/12/1998");
		} catch (Exception e) {

		}

		Categoria c1 = new Categoria();
		Categoria c2 = new Categoria();

		c1.setId(30L);
		c1.setNombre("VEGANO");

		c2.setId(31L);
		c2.setNombre("SIDRA");

		producto1 = new Producto();
		producto2 = new Producto();

		producto1.setCodigo(1011L);
		producto1.setNombre("Tortilla Vegana");
		producto1.setDescripcion("En vez de huevos hay garbanzos.");
		producto1.setPrecio(10);
		producto1.setCategoria(c1);
		producto1.setFechaAlta(fecha1);
		producto1.setDescatalogado(false);

		producto2.setCodigo(1012L);
		producto2.setNombre("El Gaitero");
		producto2.setDescripcion("Joroña que Joroña");
		producto2.setPrecio(8);
		producto2.setCategoria(c2);
		producto2.setFechaAlta(fecha2);
		producto2.setDescatalogado(false);
	}
}
