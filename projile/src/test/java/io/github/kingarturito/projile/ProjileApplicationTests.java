package io.github.kingarturito.projile;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.core.ApplicationModules;

@SpringBootTest
class ProjileApplicationTests {

    @Test
    void verifyStructure() {
        ApplicationModules.of(ProjileApplication.class).verify();
    }

}
