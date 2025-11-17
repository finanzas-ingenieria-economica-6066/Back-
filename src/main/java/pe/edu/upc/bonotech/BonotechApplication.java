package pe.edu.upc.bonotech;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing  
public class BonotechApplication {

    public static void main(String[] args) {
        SpringApplication.run(BonotechApplication.class, args);
    }
}