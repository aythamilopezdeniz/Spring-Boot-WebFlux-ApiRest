package com.springboot.webflux.app.models.dao;

import reactor.core.publisher.Mono;
import com.springboot.webflux.app.models.documents.Categoria;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface CategoriaDao extends ReactiveMongoRepository<Categoria, String>{

	public Mono<Categoria> findByNombre(String nombre);
}