package com.ccnlthd.taskmanager;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

class BeanLifecycleTest {
    @Test
    void springManagesInjectionInitializationAndDestruction() {
        ManagedExample managed;
        ExampleDependency dependency;

        try (var context = new AnnotationConfigApplicationContext(ExampleConfiguration.class)) {
            managed = context.getBean(ManagedExample.class);
            dependency = context.getBean(ExampleDependency.class);

            assertSame(dependency, managed.dependency);
            assertTrue(managed.initialized);
            assertFalse(managed.destroyed);
        }

        assertTrue(managed.destroyed);

        ManagedExample manuallyCreated = new ManagedExample(dependency);
        assertFalse(manuallyCreated.initialized);
        assertFalse(manuallyCreated.destroyed);
    }

    @Configuration(proxyBeanMethods = false)
    static class ExampleConfiguration {
        @Bean
        ExampleDependency exampleDependency() {
            return new ExampleDependency();
        }

        @Bean
        ManagedExample managedExample(ExampleDependency dependency) {
            return new ManagedExample(dependency);
        }
    }

    static class ExampleDependency {
    }

    static class ManagedExample {
        private final ExampleDependency dependency;
        private boolean initialized;
        private boolean destroyed;

        ManagedExample(ExampleDependency dependency) {
            this.dependency = dependency;
        }

        @PostConstruct
        void initialize() {
            initialized = true;
        }

        @PreDestroy
        void destroy() {
            destroyed = true;
        }
    }
}
