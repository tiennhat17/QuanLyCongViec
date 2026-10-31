package com.ccnlthd.taskmanager.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class CreateDtoValidationTest {

    @Test
    void taskTitleMustBeNonBlankAndValidInputIsNotModified() {
        TaskCreateDTO dto = new TaskCreateDTO();
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();

            for (String invalidTitle : new String[] { null, "", "   " }) {
                dto.setTitle(invalidTitle);
                assertFalse(validator.validateProperty(dto, "title").isEmpty(), "Expected invalid title");
            }

            String validTitle = "  Valid task title  ";
            dto.setTitle(validTitle);
            assertTrue(validator.validateProperty(dto, "title").isEmpty());
            assertEquals(validTitle, dto.getTitle());
        }
    }

    @Test
    void categoryNameMustBeNonBlankAndValidInputIsNotModified() {
        CategoryCreateDTO dto = new CategoryCreateDTO();
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();

            for (String invalidName : new String[] { null, "", "   " }) {
                dto.setName(invalidName);
                assertFalse(validator.validateProperty(dto, "name").isEmpty(), "Expected invalid category name");
            }

            String validName = "  Valid category  ";
            dto.setName(validName);
            assertTrue(validator.validateProperty(dto, "name").isEmpty());
            assertEquals(validName, dto.getName());
        }
    }
}
