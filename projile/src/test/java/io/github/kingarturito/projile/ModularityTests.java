package io.github.kingarturito.projile;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

public class ModularityTests {

    ApplicationModules modules = ApplicationModules.of(ProjileApplication.class);

    @Test
    void verifyStructure() {
        modules.verify();
    }

    @Test
    void createDocumentation() {
        new Documenter(modules).writeDocumentation();
    }
}
