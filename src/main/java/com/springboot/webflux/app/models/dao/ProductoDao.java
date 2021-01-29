package com.springboot.webflux.app.models.dao;

import reactor.core.publisher.Mono;
import org.springframework.data.mongodb.repository.Query;
import com.springboot.webflux.app.models.documents.Producto;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ProductoDao extends ReactiveMongoRepository<Producto, String> {
	
	public Mono<Producto> findByNombre(String nombre);

	@Query("{ 'nombre': ?0 }")
	public Mono<Producto> obtenerPorNombre(String nombre);
}