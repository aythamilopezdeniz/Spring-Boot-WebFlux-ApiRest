package com.springboot.webflux.app;

import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import com.springboot.webflux.app.models.documents.Producto;
import com.springboot.webflux.app.models.documents.Categoria;
import org.springframework.beans.factory.annotation.Autowired;
import com.springboot.webflux.app.models.services.ProductoService;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableEurekaClient
@SpringBootApplication
public class SpringBootWebFluxApiRestApplication implements CommandLineRunner {

	@Autowired
	private ProductoService service;
	
	private static final Logger log = LoggerFactory.getLogger(SpringBootWebFluxApiRestApplication.class);
	
	@Autowired
	private ReactiveMongoTemplate mongoTemplate;

	public static void main(String[] args) {
		SpringApplication.run(SpringBootWebFluxApiRestApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		mongoTemplate.dropCollection("productos").subscribe();
		mongoTemplate.dropCollection("categorias").subscribe();
		
		Categoria electronico = new Categoria("Electrónico");
		Categoria deporte = new Categoria("Deporte");
		Categoria computacion = new Categoria("Computación");
		Categoria muebles = new Categoria("Muebles");
		
		Flux.just(electronico, deporte, computacion, muebles)
		.flatMap(service::saveCategoria).doOnNext(c -> {
			log.info("Categoría creada: " + c.getNombre() + " Id: " + c.getId());
		}).thenMany(
				Flux.just(new Producto("TV Panasonic Pantalla LCD", 456.89, electronico),
						new Producto("Sony Camara HD Digital", 177.89, electronico),
						new Producto("Apple iPod", 46.89, electronico),
						new Producto("Sony NoteBook", 846.89, computacion),
						new Producto("Hewlett Packard Multifuncional", 200.89, computacion),
						new Producto("Bianchi Bicicleta", 70.89, deporte),
						new Producto("HP NoteBook Omen 17", 2500.89, computacion),
						new Producto("Mica Cómoda 5 Cajones", 150.89, muebles),
						new Producto("TV Sony Bravia OLED 4K Ultra HD", 2255.89, electronico))
				.flatMap(producto -> {
					producto.setCreateAt(new Date());
					return service.save(producto);
				})
		).subscribe(producto -> log.info("Insert " + producto.getId() + " " + producto.getNombre()));
	}
}