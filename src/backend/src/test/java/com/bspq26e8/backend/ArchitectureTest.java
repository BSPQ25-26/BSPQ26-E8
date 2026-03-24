package com.bspq26e8.backend;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.Architectures;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;
import jakarta.persistence.Converter;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

@AnalyzeClasses(
        packages = "com.bspq26e8.backend",
        importOptions = ImportOption.DoNotIncludeTests.class
)
class ArchitectureTest {

    @ArchTest
    static final Architectures.LayeredArchitecture layers = Architectures.layeredArchitecture()
            .consideringAllDependencies()
            .layer("Controllers").definedBy("..controller..")
            .layer("Services").definedBy("..service..", "..codeexecution..")
            .layer("Repositories").definedBy("..repository..")
            .layer("Entities").definedBy("..entity..")
            .whereLayer("Controllers").mayNotBeAccessedByAnyLayer()
            .whereLayer("Services").mayOnlyBeAccessedByLayers("Controllers", "Services")
            .whereLayer("Repositories").mayOnlyBeAccessedByLayers("Services")
            .whereLayer("Entities").mayOnlyBeAccessedByLayers("Repositories", "Services");

    @ArchTest
    static final ArchRule noCyclesBetweenFeatures = SlicesRuleDefinition.slices()
            .matching("com.bspq26e8.backend.(*)..")
            .should().beFreeOfCycles();

    @ArchTest
    static final ArchRule controllersAreRestControllers = classes()
            .that().resideInAPackage("..controller..")
            .and().areTopLevelClasses()
            .should().beAnnotatedWith(RestController.class);

    @ArchTest
    static final ArchRule servicesAreAnnotated = classes()
            .that().resideInAPackage("..service..")
            .and().areTopLevelClasses()
            .should().beAnnotatedWith(Service.class);

    @ArchTest
    static final ArchRule serviceAnnotationsStayInServicePackages = classes()
            .that().areAnnotatedWith(Service.class)
            .should().resideInAnyPackage("..service..", "..codeexecution..");

    @ArchTest
    static final ArchRule repositoriesExtendSpringData = classes()
            .that().resideInAPackage("..repository..")
            .should().beInterfaces()
            .andShould().beAssignableTo(Repository.class);

    @ArchTest
    static final ArchRule entitiesAreJPAAnnotated = classes()
            .that().resideInAPackage("..entity..")
            .and().areNotEnums()
            .should().beAnnotatedWith(Entity.class)
            .orShould().beAnnotatedWith(Embeddable.class)
            .orShould().beAnnotatedWith(MappedSuperclass.class)
            .orShould().beAnnotatedWith(Converter.class);
}

