package com.sinensia.pollosalegres.backend.presentation.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinensia.pollosalegres.backend.business.model.Categoria;
import com.sinensia.pollosalegres.backend.business.services.CategoriaServices;
import com.sinensia.pollosalegres.backend.presentation.config.RespuestaError;
import com.sinensia.pollosalegres.backend.presentation.controllers.CategoriaController;

@WebMvcTest(value=CategoriaController.class)
class CategoriaControllerTest {

	@Autowired
	private MockMvc miniPostman;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@MockBean
	private CategoriaServices categoriaServices;
	
	private Categoria categoria1;
	private Categoria categoria2;
	
	@BeforeEach
	void init() {
		initObjects();
	}
	
	@Test
	void solicitamos_todas_las_categorias() throws Exception {
		
		List<Categoria> categorias = Arrays.asList(categoria1, categoria2);
		
		when(categoriaServices.getAll()).thenReturn(categorias);
		
		MvcResult respuesta = miniPostman.perform(get("/categorias"))
											.andExpect(status().isOk())
											.andReturn();
		
		String responseBody = respuesta.getResponse().getContentAsString(StandardCharsets.UTF_8);
		String responseBodyEsperada = objectMapper.writeValueAsString(categorias);
				
		assertThat(responseBody).isEqualToIgnoringWhitespace(responseBodyEsperada);
	}
	
	@Test
	void solicitamos_categoria_EXISTENTE_a_partir_de_su_id() throws Exception {
		
		when(categoriaServices.read(100L)).thenReturn(Optional.of(categoria1));
		
		MvcResult respuesta = miniPostman.perform(get("/categorias/100"))
											.andExpect(status().isOk())
											.andReturn();
		
		String responseBody = respuesta.getResponse().getContentAsString(StandardCharsets.UTF_8);
		String responseBodyEsperada = objectMapper.writeValueAsString(categoria1);
		
		assertThat(responseBody).isEqualToIgnoringWhitespace(responseBodyEsperada);
	}
	
	@Test
	void solicitamos_categoria_NO_EXISTENTE_a_partir_de_su_id() throws Exception {
		
		when(categoriaServices.read(100L)).thenReturn(Optional.empty());
		
		MvcResult respuesta = miniPostman.perform(get("/categorias/100"))
											.andExpect(status().isNotFound())
											.andReturn();
		
		RespuestaError respuestaError = new RespuestaError("No existe la categoria 100");
		
		String responseBody = respuesta.getResponse().getContentAsString(StandardCharsets.UTF_8);
		String responseBodyEsperada = objectMapper.writeValueAsString(respuestaError);
		
		assertThat(responseBody).isEqualToIgnoringWhitespace(responseBodyEsperada);	
	}
	
	@Test
	void creamos_categoria_ok() throws Exception {
		
		categoria1.setId(null);
		
		when(categoriaServices.create(categoria1)).thenReturn(467L);
		
		String requestBody = objectMapper.writeValueAsString(categoria1);
		
		miniPostman.perform(post("/categorias").content(requestBody).contentType("application/json"))
								.andExpect(status().isCreated())
								.andExpect(header().string("Location","http://localhost/categorias/467"));	
	}
	
	@Test
	void creamos_categoria_con_id_NO_null() throws Exception {
			
		when(categoriaServices.create(categoria1)).thenThrow(new IllegalStateException("EL MENSAJE"));
	
		String requestBody = objectMapper.writeValueAsString(categoria1);
		
		MvcResult respuesta = miniPostman.perform(post("/categorias").content(requestBody).contentType("application/json"))
													.andExpect(status().isBadRequest())
													.andReturn();
	
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
	
		categoria1 = new Categoria();
		categoria1.setId(100L);
		categoria1.setNombre("TAPAS");
		
		categoria2 = new Categoria();
		categoria2.setId(101L);
		categoria2.setNombre("REFRESCOS");
	}
}
