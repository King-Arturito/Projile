package io.github.kingarturito.projile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulithic;

@SpringBootApplication
@Modulithic(systemName = "Projile", sharedModules = {"shared"})
public class ProjileApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjileApplication.class, args);
    }

}
