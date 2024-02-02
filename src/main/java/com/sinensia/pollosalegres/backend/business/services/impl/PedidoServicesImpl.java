package com.sinensia.pollosalegres.backend.business.services.impl;

import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.dozer.DozerBeanMapper;
import org.springframework.stereotype.Service;

import com.sinensia.pollosalegres.backend.business.model.Pedido;
import com.sinensia.pollosalegres.backend.business.services.PedidoServices;
import com.sinensia.pollosalegres.backend.integration.model.CamareroPL;
import com.sinensia.pollosalegres.backend.integration.model.ClientePL;
import com.sinensia.pollosalegres.backend.integration.model.EstablecimientoPL;
import com.sinensia.pollosalegres.backend.integration.model.EstadoPedidoPL;
import com.sinensia.pollosalegres.backend.integration.model.LineaPedidoPL;
import com.sinensia.pollosalegres.backend.integration.model.PedidoPL;
import com.sinensia.pollosalegres.backend.integration.repositories.PedidoPLRepository;

@Service
public class PedidoServicesImpl implements PedidoServices {

	private PedidoPLRepository pedidoPLRepository;
	private DozerBeanMapper mapper;
	
	public PedidoServicesImpl(PedidoPLRepository pedidoPLRepository, DozerBeanMapper mapper) {
		this.pedidoPLRepository = pedidoPLRepository;
		this.mapper = mapper;
	}
	
	@Override
	public Long create(Pedido pedido) {
		
		if(pedido.getNumero() != null) {
			throw new IllegalStateException("Para crear un pedido el codigo ha de ser null");
		}
		
		PedidoPL pedidoPL = mapper.map(pedido, PedidoPL.class);
		pedidoPL.setNumero(System.currentTimeMillis());
		
		return pedidoPLRepository.save(pedidoPL).getNumero();
	}

	@Override
	public Optional<Pedido> read(Long numero) {
		
		Optional<PedidoPL> optional = pedidoPLRepository.findById(numero);
		
		Pedido pedido = null;
		
		if(optional.isPresent()) {
			pedido = mapper.map(optional.get(), Pedido.class);
		}
		
		return Optional.ofNullable(pedido);
	}

	@Override
	public void update(Long numerPedido, Map<String, Object> atributos) {
		
		if(numerPedido == null) {
			throw new IllegalStateException("No se puede actualizar un pedido con número null");
		}
		
		Optional<PedidoPL> pedidoPL = pedidoPLRepository.findById(numerPedido);
		PedidoPL pedidoPL2 = mapper.map(pedidoPL, PedidoPL.class);
		List<LineaPedidoPL> lineas = null;
		
		if (pedidoPL.isPresent()) {
			pedidoPL2.setFecha((Date) atributos.get("fecha"));
			pedidoPL2.setCliente((ClientePL) atributos.get("cliente"));
			pedidoPL2.setCamarero((CamareroPL) atributos.get("camarero"));
			pedidoPL2.setEstablecimiento((EstablecimientoPL) atributos.get("establecimiento"));
			pedidoPL2.setEstado((EstadoPedidoPL) atributos.get("estado"));
			for ( LineaPedidoPL linea : List.copyOf((List<LineaPedidoPL>) atributos.get("lineas"))) {
				lineas.add(linea);
			}
			pedidoPL2.setLineas(lineas);
		}
		
	}

	@Override
	public void update(Pedido pedido) {
		
		Long numero = pedido.getNumero();

		if(numero == null) {
			throw new IllegalStateException("No se puede actualizar un pedido con número null");
		}

		boolean existe = pedidoPLRepository.existsById(numero);
		
		if(!existe) {
			throw new IllegalStateException("El pedido con número " + numero + " no existe. No se puede actualizar");
		}

		pedidoPLRepository.save(mapper.map(pedido, PedidoPL.class));
		
	}

	@Override
	public List<Pedido> getAll() {
		
		return pedidoPLRepository.findAll().stream()
				.map(x -> mapper.map(x, Pedido.class))
				.toList();
	}

	@Override
	public void procesar(Long numero) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void entregar(Long numero) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void servir(Long numero) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void cancelar(Long numero) {
		// TODO Auto-generated method stub
		
	}

}
