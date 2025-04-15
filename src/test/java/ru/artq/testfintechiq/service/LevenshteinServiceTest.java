package ru.artq.testfintechiq.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.artq.testfintechiq.model.RegPerson;
import ru.artq.testfintechiq.model.VerifiedName;
import ru.artq.testfintechiq.repository.RegPersonRepository;
import ru.artq.testfintechiq.repository.VerifiedNameRepository;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LevenshteinServiceTest {

    @Mock
    private SettingsService settingsService;

    @Mock
    private RegPersonRepository mockRegPersonRepository;

    @Mock
    private VerifiedNameRepository mockVerifiedNameRepository;

    @InjectMocks
    private LevenshteinService levenshteinService;

    private StringConcatenationService stringConcatenationService;

    private static Stream<Arguments> wordCombinationsProvider() {
        return Stream.of(
                Arguments.of("john doe", List.of("johndoe")),
                Arguments.of("john doe smith", List.of("johndoe", "johnsmith", "doesmith")),
                Arguments.of("a b c d", List.of("ab", "ac", "ad", "bc", "bd", "cd"))
        );
    }

    private static Stream<Arguments> levenshteinRatioProvider() {
        return Stream.of(
                Arguments.of("kitten", "sitting", 0.571), // 4 operations, max length 7
                Arguments.of("saturday", "sunday", 0.625), // 3 operations, max length 8
                Arguments.of("abc", "def", 0.0),          // 3 operations, max length 3
                Arguments.of("abc", "abcd", 0.75)         // 1 operation, max length 4
        );
    }

    @BeforeEach
    void setUp() {
        lenient().when(settingsService.getDistanceRatioThreshold()).thenReturn(0.9);
        stringConcatenationService = new StringConcatenationService();
    }

    private void setupMockData() {
        // Test case 1 data
        RegPerson regPerson1 = RegPerson.builder()
                .loanRequestId("0190e7b2-14a8-72e4-8528-89a8cd91d430")
                .firstName("Ogada")
                .middleName("Isaac Abraham")
                .lastName("Samuel")
                .build();

        VerifiedName verifiedName1 = VerifiedName.builder()
                .loanRequestId("0190e7b2-14a8-72e4-8528-89a8cd91d430")
                .firstName("ISAAC")
                .otherName("ABRAHAM SAMUEL")
                .surname("OGADA")
                .build();

        // Test case 2 data
        RegPerson regPerson2 = RegPerson.builder()
                .loanRequestId("0190e8e4-cf7b-72a5-a647-cd87e14f6715")
                .firstName("Solomon")
                .lastName("Awich")
                .build();

        VerifiedName verifiedName2 = VerifiedName.builder()
                .loanRequestId("0190e8e4-cf7b-72a5-a647-cd87e14f6715")
                .firstName("SOLOMON")
                .otherName("RAORE")
                .surname("AWICH")
                .build();

        // Test case 3 data
        RegPerson regPerson3 = RegPerson.builder()
                .loanRequestId("0190e7b7-4868-73e9-8087-f2c70ea12b61")
                .firstName("FABIAN")
                .lastName("OTIENO")
                .build();

        VerifiedName verifiedName3 = VerifiedName.builder()
                .loanRequestId("0190e7b7-4868-73e9-8087-f2c70ea12b61")
                .firstName("TERESA")
                .otherName("WAMUYU")
                .surname("THERI")
                .build();

        lenient().when(mockRegPersonRepository.findByLoanRequestId("0190e7b2-14a8-72e4-8528-89a8cd91d430"))
                .thenReturn(regPerson1);
        lenient().when(mockVerifiedNameRepository.findByLoanRequestId("0190e7b2-14a8-72e4-8528-89a8cd91d430"))
                .thenReturn(verifiedName1);

        lenient().when(mockRegPersonRepository.findByLoanRequestId("0190e8e4-cf7b-72a5-a647-cd87e14f6715"))
                .thenReturn(regPerson2);
        lenient().when(mockVerifiedNameRepository.findByLoanRequestId("0190e8e4-cf7b-72a5-a647-cd87e14f6715"))
                .thenReturn(verifiedName2);

        lenient().when(mockRegPersonRepository.findByLoanRequestId("0190e7b7-4868-73e9-8087-f2c70ea12b61"))
                .thenReturn(regPerson3);
        lenient().when(mockVerifiedNameRepository.findByLoanRequestId("0190e7b7-4868-73e9-8087-f2c70ea12b61"))
                .thenReturn(verifiedName3);
    }

    @Test
    void calculateStopFactor_shouldReturnTrue_whenStringsAreEqual() {
        boolean result = levenshteinService.calculateStopFactor("test string", "test string");
        assertTrue(result);
    }

    @Test
    void calculateStopFactor_shouldReturnTrue_whenStringsContainTheSameWords() {
        boolean result = levenshteinService.calculateStopFactor("JOHN DOE", "doe john");
        assertTrue(result);
    }

    @Test
    void calculateStopFactor_shouldReturnTrue_whenSimilarityIsAboveThreshold() {
        when(settingsService.getDistanceRatioThreshold()).thenReturn(0.8);

        // "Johnathan" and "Jonathan" are similar but not exactly the same
        boolean result = levenshteinService.calculateStopFactor("Johnathan Doe", "Jonathan Doe");
        assertTrue(result);
    }

    @Test
    void calculateStopFactor_shouldReturnFalse_whenSimilarityIsBelowThreshold() {
        boolean result = levenshteinService.calculateStopFactor("John Doe", "Jane Smith");
        assertFalse(result);
    }

    @Test
    void calculateStopFactor_shouldReturnFalse_whenInputIsNull() {
        boolean result = levenshteinService.calculateStopFactor(null, "test");
        assertFalse(result);

        result = levenshteinService.calculateStopFactor("test", null);
        assertFalse(result);

        result = levenshteinService.calculateStopFactor(null, null);
        assertFalse(result);
    }

    @Test
    void calculateStopFactor_shouldReturnTrue_whenBothStringsAreEmpty() {
        boolean result = levenshteinService.calculateStopFactor("", "");
        assertTrue(result);
    }

    @Test
    void calculateStopFactor_testRealisticExamples() {
        // Test case from example 1
        String regPerson = "Ogada Isaac Abraham Samuel";
        String verifiedName = "ISAAC ABRAHAM SAMUEL OGADA";
        boolean result = levenshteinService.calculateStopFactor(regPerson, verifiedName);
        assertTrue(result);

        // Test case from example 2
        regPerson = "Solomon Awich";
        verifiedName = "SOLOMON RAORE AWICH";
        result = levenshteinService.calculateStopFactor(regPerson, verifiedName);
        assertTrue(result);

        // Test case from example 3 (should be false)
        regPerson = "FABIAN OTIENO";
        verifiedName = "TERESA WAMUYU THERI";
        result = levenshteinService.calculateStopFactor(regPerson, verifiedName);
        assertFalse(result);
    }

    @Test
    void calculateStopFactor_testEarlyReturnOptimizations() {
        // Тест идентичных строк (без учета регистра)
        boolean result = levenshteinService.calculateStopFactor("John Doe", "JOHN DOE");
        assertTrue(result);

        // Тест на идентичность строк с пробелами в начале и конце
        result = levenshteinService.calculateStopFactor("  John Doe  ", "JOHN DOE");
        assertTrue(result);

        // Тест поиска полного совпадения
        result = levenshteinService.calculateStopFactor("Solomon Awich", "AWICH SOLOMON");
        assertTrue(result);
    }

    @Test
    void generateWordCombinations_shouldReturnSingleWordWhenOnlyOneWordProvided() {
        List<String> combinations = levenshteinService.generateWordCombinations("word");
        assertEquals(List.of("word"), combinations);
    }

    @Test
    void generateWordCombinations_shouldReturnEmptyListWhenInputIsNull() {
        List<String> combinations = levenshteinService.generateWordCombinations(null);
        assertTrue(combinations.isEmpty());
    }

    @Test
    void generateWordCombinations_shouldReturnEmptyListWhenInputIsEmpty() {
        List<String> combinations = levenshteinService.generateWordCombinations("");
        assertTrue(combinations.isEmpty());
    }

    @Test
    void testGenerateWordCombinations_withExamples() {
        String input = "A B C";
        List<String> combinations = levenshteinService.generateWordCombinations(input);
        assertEquals(3, combinations.size());
        assertTrue(combinations.contains("AB"));
        assertTrue(combinations.contains("AC"));
        assertTrue(combinations.contains("BC"));

        input = "A B C D";
        combinations = levenshteinService.generateWordCombinations(input);
        assertEquals(6, combinations.size());
        assertTrue(combinations.contains("AB"));
        assertTrue(combinations.contains("AC"));
        assertTrue(combinations.contains("AD"));
        assertTrue(combinations.contains("BC"));
        assertTrue(combinations.contains("BD"));
        assertTrue(combinations.contains("CD"));

        input = "SingleWord";
        combinations = levenshteinService.generateWordCombinations(input);
        assertEquals(1, combinations.size());
        assertTrue(combinations.contains("SingleWord"));
    }

    @ParameterizedTest
    @MethodSource("wordCombinationsProvider")
    void generateWordCombinations_shouldGenerateCorrectCombinations(String input, List<String> expected) {
        List<String> combinations = levenshteinService.generateWordCombinations(input);
        assertTrue(combinations.containsAll(expected) && expected.containsAll(combinations));
    }

    @Test
    void calculateLevenshteinRatio_shouldReturnOne_whenStringsAreEqual() {
        double ratio = levenshteinService.calculateLevenshteinRatio("test", "test");
        assertEquals(1.0, ratio);
    }

    @Test
    void calculateLevenshteinRatio_shouldReturnOne_whenBothStringsAreEmpty() {
        double ratio = levenshteinService.calculateLevenshteinRatio("", "");
        assertEquals(1.0, ratio);
    }

    @Test
    void testCalculateLevenshteinRatio_withAdditionalExamples() {
        assertEquals(1.0, levenshteinService.calculateLevenshteinRatio("SAME", "SAME"));
        assertTrue(levenshteinService.calculateLevenshteinRatio("SIMILAR", "SIMILAP") > 0.8);
        assertTrue(levenshteinService.calculateLevenshteinRatio("DIFFERENT", "COMPLETELY") < 0.5);

        assertEquals(1.0, levenshteinService.calculateLevenshteinRatio("", ""));
        assertEquals(0.0, levenshteinService.calculateLevenshteinRatio("ABC", ""));
        assertEquals(0.0, levenshteinService.calculateLevenshteinRatio("", "ABC"));
    }

    @ParameterizedTest
    @MethodSource("levenshteinRatioProvider")
    void calculateLevenshteinRatio_shouldReturnExpectedRatio(String s1, String s2, double expected) {
        double ratio = levenshteinService.calculateLevenshteinRatio(s1, s2);
        assertEquals(expected, ratio, 0.001);
    }

    @Test
    void testStopFactorCalculationWithMocks() {
        setupMockData();

        // Test case 1: Ogada Isaac Abraham Samuel vs ISAAC ABRAHAM SAMUEL OGADA
        RegPerson regPerson1 = mockRegPersonRepository.findByLoanRequestId("0190e7b2-14a8-72e4-8528-89a8cd91d430");
        VerifiedName verifiedName1 = mockVerifiedNameRepository.findByLoanRequestId("0190e7b2-14a8-72e4-8528-89a8cd91d430");

        String regPersonString1 = stringConcatenationService.concatenateRegPersonFields(regPerson1);
        String verifiedNameString1 = stringConcatenationService.concatenateVerifiedNameFields(verifiedName1);

        boolean stopFactor1 = levenshteinService.calculateStopFactor(regPersonString1, verifiedNameString1);
        assertTrue(stopFactor1, "Stop factor for test case 1 should be true");

        // Test case 2: Solomon Awich vs SOLOMON RAORE AWICH
        RegPerson regPerson2 = mockRegPersonRepository.findByLoanRequestId("0190e8e4-cf7b-72a5-a647-cd87e14f6715");
        VerifiedName verifiedName2 = mockVerifiedNameRepository.findByLoanRequestId("0190e8e4-cf7b-72a5-a647-cd87e14f6715");

        String regPersonString2 = stringConcatenationService.concatenateRegPersonFields(regPerson2);
        String verifiedNameString2 = stringConcatenationService.concatenateVerifiedNameFields(verifiedName2);

        boolean stopFactor2 = levenshteinService.calculateStopFactor(regPersonString2, verifiedNameString2);
        assertTrue(stopFactor2, "Stop factor for test case 2 should be true");

        // Test case 3: FABIAN OTIENO vs TERESA WAMUYU THERI
        RegPerson regPerson3 = mockRegPersonRepository.findByLoanRequestId("0190e7b7-4868-73e9-8087-f2c70ea12b61");
        VerifiedName verifiedName3 = mockVerifiedNameRepository.findByLoanRequestId("0190e7b7-4868-73e9-8087-f2c70ea12b61");

        String regPersonString3 = stringConcatenationService.concatenateRegPersonFields(regPerson3);
        String verifiedNameString3 = stringConcatenationService.concatenateVerifiedNameFields(verifiedName3);

        boolean stopFactor3 = levenshteinService.calculateStopFactor(regPersonString3, verifiedNameString3);
        assertFalse(stopFactor3, "Stop factor for test case 3 should be false");
    }
} 