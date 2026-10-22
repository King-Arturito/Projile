package io.github.kingarturito.projile;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ProjileApplicationTests {

    @Test
    void verifyStructure() {
        ApplicationModules.of(ProjileApplication.class).verify();
    }

}
