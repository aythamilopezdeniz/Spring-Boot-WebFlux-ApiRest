package com.springboot.webflux.app;

import java.util.List;
import java.util.Collections;
//import java.util.LinkedHashMap;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import org.assertj.core.api.Assertions;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
//import org.springframework.core.ParameterizedTypeReference;
import com.springboot.webflux.app.models.documents.Producto;
import org.springframework.boot.test.context.SpringBootTest;
import com.springboot.webflux.app.models.documents.Categoria;
import org.springframework.beans.factory.annotation.Autowired;
import com.springboot.webflux.app.models.services.ProductoService;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;

//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class SpringBootWebFluxApiRestApplicationTests {
	
	@Autowired
	private WebTestClient client;
	
	@Autowired
	private ProductoService service;
	
	@Value("${config.base.endpoint}")
	private String url;

	@Test
	public void listarTest() {
		client.get()
		.uri(url)
		.accept(MediaType.APPLICATION_JSON)
		.exchange()
		.expectStatus().isOk()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBodyList(Producto.class)
		.consumeWith(response -> {
			List<Producto> productos = response.getResponseBody();
			productos.forEach(p -> {
				System.out.println(p.getNombre());
			});
			
			Assertions.assertThat(productos.size() > 0).isTrue();
		});
//		.hasSize(9);
	}


	@Test
	public void verTest() {
		Producto producto = service.findByNombre("TV Panasonic Pantalla LCD").block();
		client.get()
		.uri(url + "/{id}", Collections.singletonMap("id", producto.getId()))
		.accept(MediaType.APPLICATION_JSON)
		.exchange()
		.expectStatus().isOk()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody(Producto.class)
		.consumeWith(response -> {
			Producto p = response.getResponseBody();
			Assertions.assertThat(p.getId()).isNotEmpty();
			Assertions.assertThat(p.getId().length() > 0).isTrue();
			Assertions.assertThat(p.getNombre()).isEqualTo("TV Panasonic Pantalla LCD");
		})
		/*.expectBody()
		.jsonPath("$.id").isNotEmpty()
		.jsonPath("$.nombre").isEqualTo("TV Panasonic Pantalla LCD")*/
		;
	}
	
	@Test
	public void crearTest() {
		Categoria categoria = service.findCategoriaByNombre("Muebles").block();
		
		Producto producto = new Producto("Mesa comedor", 100.00, categoria);
		
		client.post().uri(url)
		.contentType(MediaType.APPLICATION_JSON)
		.accept(MediaType.APPLICATION_JSON)
		.body(Mono.just(producto), Producto.class)
		.exchange()
		.expectStatus().isCreated()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody()
		.jsonPath("$.id").isNotEmpty()
		.jsonPath("$.nombre").isEqualTo("Mesa comedor")
		.jsonPath("$.categoria.nombre").isEqualTo("Muebles");
//		Probar RestController
//		.jsonPath("$.producto.id").isNotEmpty()
//		.jsonPath("$.producto.nombre").isEqualTo("Mesa comedor")
//		.jsonPath("$.producto.categoria.nombre").isEqualTo("Muebles");
	}
	
	@Test
	public void crear2Test() {
		Categoria categoria = service.findCategoriaByNombre("Muebles").block();
		
		Producto producto = new Producto("Mesa comedor", 100.00, categoria);
		
		client.post().uri(url)
		.contentType(MediaType.APPLICATION_JSON)
		.accept(MediaType.APPLICATION_JSON)
		.body(Mono.just(producto), Producto.class)
		.exchange()
		.expectStatus().isCreated()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody(Producto.class)
		.consumeWith(response -> {
			Object o = response.getResponseBody();
			Producto p = new ObjectMapper().convertValue(o, Producto.class);
			Assertions.assertThat(p.getId()).isNotEmpty();
			Assertions.assertThat(p.getNombre()).isEqualTo("Mesa comedor");
			Assertions.assertThat(p.getCategoria().getNombre()).isEqualTo("Muebles");
		});
//		Probar RestController
//		.expectBody(new ParameterizedTypeReference<LinkedHashMap<String, Object>>() {})
//		.consumeWith(response -> {
//			Object o = response.getResponseBody().get("producto");
//			Producto p = new ObjectMapper().convertValue(o, Producto.class);
//			Assertions.assertThat(p.getId()).isNotEmpty();
//			Assertions.assertThat(p.getNombre()).isEqualTo("Mesa comedor");
//			Assertions.assertThat(p.getCategoria().getNombre()).isEqualTo("Muebles");
//		});
	}
	
	@Test
	public void editarTest() {
		Producto producto = service.findByNombre("Sony NoteBook").block();
		Categoria categoria = service.findCategoriaByNombre("Electrónico").block();

		Producto productoEditado = new Producto("Asus NoteBook", 700.00, categoria);
		
		client.put().uri(url + "/{id}", Collections.singletonMap("id", producto.getId()))
		.contentType(MediaType.APPLICATION_JSON)
		.accept(MediaType.APPLICATION_JSON)
		.body(Mono.just(productoEditado), Producto.class)
		.exchange()
		.expectStatus().isCreated()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody()
		.jsonPath("$.id").isNotEmpty()
		.jsonPath("$.nombre").isEqualTo("Asus NoteBook")
		.jsonPath("$.categoria.nombre").isEqualTo("Electrónico");
	}
	
	@Test
	public void eliminarTest() {
		Producto producto = service.findByNombre("Mica Cómoda 5 Cajones").block();
		client.delete()
		.uri(url + "/{id}", Collections.singletonMap("id", producto.getId()))
		.exchange()
		.expectStatus().isNoContent()
		.expectBody().isEmpty();
		
		client.get()
		.uri("/api/v2/productos/{id}", Collections.singletonMap("id", producto.getId()))
		.exchange()
		.expectStatus().isNotFound()
		.expectBody().isEmpty();
	}
}