package de.trustable.ca3s.core;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.jose4j.base64url.Base64Url;
import org.junit.jupiter.api.Test;

import java.util.Base64;

class ArchTest {

    @Test
    void servicesAndRepositoriesShouldNotDependOnWebLayer() {
        JavaClasses importedClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("de.trustable.ca3s.core");

        noClasses()
            .that()
            .resideInAnyPackage("de.trustable.ca3s.core.service..")
            .or()
            .resideInAnyPackage("de.trustable.ca3s.core.repository..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..de.trustable.ca3s.core.web..")
            .because("Services and repositories should not depend on web layer")
            .check(importedClasses);
    }
}
