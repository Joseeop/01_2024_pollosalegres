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
import java.text.ParseException;
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
import com.sinensia.pollosalegres.backend.business.model.DatosContacto;
import com.sinensia.pollosalegres.backend.business.model.Direccion;
import com.sinensia.pollosalegres.backend.business.model.Establecimiento;
import com.sinensia.pollosalegres.backend.business.services.EstablecimientoServices;
import com.sinensia.pollosalegres.backend.presentation.config.RespuestaError;
import com.sinensia.pollosalegres.backend.presentation.controllers.EstablecimientoController;

@WebMvcTest(value=EstablecimientoController.class)
class EstablecimientoControllerTest {

	@Autowired
	private MockMvc miniPostman;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@MockBean
	private EstablecimientoServices establecimientoServices;
	
	private Establecimiento establecimiento1;
	private Establecimiento establecimiento2;
	
	@BeforeEach
	void init() {
		initObjects();
	}
	
	@Test
	void solicitamos_todos_los_establecimientos() throws Exception {
		
		List<Establecimiento> establecimientos = Arrays.asList(establecimiento1, establecimiento2);
		
		when(establecimientoServices.getAll()).thenReturn(establecimientos);
		
		MvcResult respuesta = miniPostman.perform(get("/establecimientos"))
											.andExpect(status().isOk())
											.andReturn();
		
		String responseBody = respuesta.getResponse().getContentAsString(StandardCharsets.UTF_8);
		String responseBodyEsperada = objectMapper.writeValueAsString(establecimientos);
				
		assertThat(responseBody).isEqualToIgnoringWhitespace(responseBodyEsperada);
	}
	
	@Test
	void solicitamos_establecimiento_EXISTENTE_a_partir_de_su_codigo() throws Exception {
		
		when(establecimientoServices.read(100L)).thenReturn(Optional.of(establecimiento1));
		
		MvcResult respuesta = miniPostman.perform(get("/establecimientos/100"))
											.andExpect(status().isOk())
											.andReturn();
		
		String responseBody = respuesta.getResponse().getContentAsString(StandardCharsets.UTF_8);
		String responseBodyEsperada = objectMapper.writeValueAsString(establecimiento1);
		
		assertThat(responseBody).isEqualToIgnoringWhitespace(responseBodyEsperada);
	}
	
	@Test
	void solicitamos_establecimiento_NO_EXISTENTE_a_partir_de_su_codigo() throws Exception {
		
		when(establecimientoServices.read(100L)).thenReturn(Optional.empty());
		
		MvcResult respuesta = miniPostman.perform(get("/establecimientos/100"))
											.andExpect(status().isNotFound())
											.andReturn();
		
		RespuestaError respuestaError = new RespuestaError("No existe el establecimiento 100");
		
		String responseBody = respuesta.getResponse().getContentAsString(StandardCharsets.UTF_8);
		String responseBodyEsperada = objectMapper.writeValueAsString(respuestaError);
		
		assertThat(responseBody).isEqualToIgnoringWhitespace(responseBodyEsperada);
		
	}
	
	@Test
	void solicitamos_establecimiento_con_PARAMETRO_NO_VALIDO_a_partir_de_su_codigo() throws Exception {
		
		MvcResult respuesta = miniPostman.perform(get("/establecimientos/hola"))
											.andExpect(status().isBadRequest())
											.andReturn();
		
		RespuestaError respuestaError = new RespuestaError("El parámetro hola es de tipo java.lang.String - Se requiere un parámetro de tipo Long");
		
		String responseBody = respuesta.getResponse().getContentAsString(StandardCharsets.UTF_8);
		String responseBodyEsperada = objectMapper.writeValueAsString(respuestaError);
		
		assertThat(responseBody).isEqualToIgnoringWhitespace(responseBodyEsperada);	
	}
	
	
	@Test
	void creamos_establecimiento_ok() throws Exception {
		
		establecimiento1.setCodigo(null);
		
		when(establecimientoServices.create(establecimiento1)).thenReturn(467L);
		
		String requestBody = objectMapper.writeValueAsString(establecimiento1);
		
		miniPostman.perform(post("/establecimientos").content(requestBody).contentType("application/json"))
								.andExpect(status().isCreated())
								.andExpect(header().string("Location","http://localhost/establecimientos/467"));	
	}
	
	@Test
	void creamos_establecimiento_con_codigo_NO_null() throws Exception {
			
		when(establecimientoServices.create(establecimiento1)).thenThrow(new IllegalStateException("EL MENSAJE"));
	
		String requestBody = objectMapper.writeValueAsString(establecimiento1);
		
		MvcResult respuesta = miniPostman.perform(post("/establecimientos").content(requestBody).contentType("application/json"))
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
	
		establecimiento1 = new Establecimiento();
		establecimiento2 = new Establecimiento();
		
		establecimiento1.setCodigo(100L);
		establecimiento2.setCodigo(101L);
		
		DatosContacto datosContacto1 = new DatosContacto();
		DatosContacto datosContacto2 = new DatosContacto();
		
		Direccion direccion1 = new Direccion();
		Direccion direccion2 = new Direccion();
		
		datosContacto1.setTelefono("91 220 34 43");
		datosContacto1.setFax("91 220 24 55");
		datosContacto1.setEmail("vaguada@pollosfelices.com");
		
		datosContacto2.setTelefono("93 231 66 56");
		datosContacto2.setFax(null);
		datosContacto2.setEmail("granvia2@pollosfelices.com");
		
		direccion1.setDireccion("Avda. Monforte de Lemos, 36");
		direccion1.setPoblacion("Fuencarral");
		direccion1.setCodigoPostal("28029");
		direccion1.setProvincia("Madrid");
		direccion1.setPais("España");
		
		direccion2.setDireccion("Avda. de la Gran Via de l'Hospitalet, 75");
		direccion2.setPoblacion("L'Hospitalet de Llobregat");
		direccion2.setCodigoPostal("08908");
		direccion2.setProvincia("Barcelona");
		direccion2.setPais("España");
		
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
	}
}
