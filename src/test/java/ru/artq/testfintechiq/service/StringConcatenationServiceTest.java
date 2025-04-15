package ru.artq.testfintechiq.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.artq.testfintechiq.model.RegPerson;
import ru.artq.testfintechiq.model.VerifiedName;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringConcatenationServiceTest {

    private StringConcatenationService stringConcatenationService;

    private static Stream<Arguments> regPersonDataProvider() {
        return Stream.of(
                // All fields present
                Arguments.of("John", "William", "Doe", "John William Doe"),
                // Null middle name
                Arguments.of("John", null, "Doe", "John Doe"),
                // Empty fields
                Arguments.of("", "", "Doe", "Doe"),
                // One letter middle initial
                Arguments.of("John", "M", "Doe", "John M Doe")
        );
    }

    private static Stream<Arguments> verifiedNameDataProvider() {
        return Stream.of(
                // All fields present
                Arguments.of("JOHN", "WILLIAM", "DOE", "JOHN WILLIAM DOE"),
                // Null other name
                Arguments.of("JOHN", null, "DOE", "JOHN DOE"),
                // Empty fields
                Arguments.of("", "", "DOE", "DOE"),
                // Different name format
                Arguments.of("Jane", "Mary", "Smith", "Jane Mary Smith")
        );
    }

    @BeforeEach
    void setUp() {
        stringConcatenationService = new StringConcatenationService();
    }

    @Test
    void concatenateRegPersonFields_edgeCases() {
        assertEquals("", stringConcatenationService.concatenateRegPersonFields(null));
    }

    @Test
    void concatenateVerifiedNameFields_edgeCases() {
        assertEquals("", stringConcatenationService.concatenateVerifiedNameFields(null));
    }

    @ParameterizedTest
    @MethodSource("regPersonDataProvider")
    void concatenateRegPersonFields_variousInputs(String firstName, String middleName, String lastName, String expected) {
        RegPerson regPerson = new RegPerson();
        regPerson.setFirstName(firstName);
        regPerson.setMiddleName(middleName);
        regPerson.setLastName(lastName);

        String result = stringConcatenationService.concatenateRegPersonFields(regPerson);
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @MethodSource("verifiedNameDataProvider")
    void concatenateVerifiedNameFields_variousInputs(String firstName, String otherName, String surname, String expected) {
        VerifiedName verifiedName = new VerifiedName();
        verifiedName.setFirstName(firstName);
        verifiedName.setOtherName(otherName);
        verifiedName.setSurname(surname);

        String result = stringConcatenationService.concatenateVerifiedNameFields(verifiedName);
        assertEquals(expected, result);
    }
}