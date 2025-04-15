package ru.artq.testfintechiq.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для расчета расстояния Левенштейна и Стоп-Фактора.
 * Реализует алгоритм сравнения строк на основе расстояния Левенштейна
 * и формирования комбинаций слов для расчета стоп-фактора.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LevenshteinService {
    private final SettingsService settingsService;

    /**
     * Рассчитывает стоп-фактор на основе сравнения всех возможных комбинаций слов
     * между regPersonString и verifiedNameString.
     *
     * @param regPersonString    строка с данными регистрационного лица
     * @param verifiedNameString строка с данными проверенного имени
     * @return true, если макс соотношение Левенштейна >= порогового значения,
     * иначе false
     */
    public boolean calculateStopFactor(String regPersonString, String verifiedNameString) {
        if (regPersonString == null || verifiedNameString == null) return false;

        String trimmedRegPerson = regPersonString.trim();
        String trimmedVerifiedName = verifiedNameString.trim();

        if (trimmedRegPerson.isEmpty() && trimmedVerifiedName.isEmpty())
            return true;

        if (trimmedRegPerson.equalsIgnoreCase(trimmedVerifiedName)) return true;

        if (containsSameWords(trimmedRegPerson, trimmedVerifiedName))
            return true;

        double maxRatio = 0.0;
        List<String> regPersonCombinations = generateWordCombinations(trimmedRegPerson.toLowerCase());
        List<String> verifiedNameCombinations = generateWordCombinations(trimmedVerifiedName.toLowerCase());

        log.debug("RegPerson Combinations: {}", regPersonCombinations);
        log.debug("VerifiedName Combinations: {}", verifiedNameCombinations);

        // Сравниваем каждую пару комбинаций и находим макс соотношение
        for (String regPerson : regPersonCombinations) {
            for (String verifiedName : verifiedNameCombinations) {
                double ratio = calculateLevenshteinRatio(regPerson, verifiedName);
                log.debug("Comparing '{}' and '{}': ratio = {}", regPerson, verifiedName, ratio);
                maxRatio = Math.max(maxRatio, ratio);

                // если нашли полное совпадение, прерываем цикл
                if (maxRatio >= 1.0) return true;
            }
        }

        double threshold = settingsService.getDistanceRatioThreshold();
        log.debug("Max Levenshtein Ratio: {}, Threshold: {}", maxRatio, threshold);
        return maxRatio >= threshold;
    }

    /**
     * Проверяет, содержат ли две строки одинаковые слова (независимо от порядка)
     *
     * @param str1 первая строка
     * @param str2 вторая строка
     * @return true, если строки содержат одинаковые слова, иначе false
     */
    private boolean containsSameWords(String str1, String str2) {
        // Разбиваем строки на слова, сортируем и объединяем снова для сравнения
        String sortedStr1 = Arrays.stream(str1.toLowerCase().split("\\s+"))
                .sorted()
                .collect(Collectors.joining(" "));

        String sortedStr2 = Arrays.stream(str2.toLowerCase().split("\\s+"))
                .sorted()
                .collect(Collectors.joining(" "));

        return sortedStr1.equals(sortedStr2);
    }

    /**
     * Генерирует все возможные комбинации из пар слов во входной строке.
     * Если строка содержит только одно слово, возвращает список с этим словом.
     *
     * @param input входная строка со словами, разделенными пробелами
     * @return список всех возможных комбинаций пар слов
     */
    public List<String> generateWordCombinations(String input) {
        if (input == null || input.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String[] words = input.trim().split("\\s+");
        List<String> combinations = new ArrayList<>();

        if (words.length == 1) {
            combinations.add(words[0]);
            return combinations;
        }

        // Генерация всех возможных пар слов (n выбрать 2)
        for (int i = 0; i < words.length; i++) {
            for (int j = i + 1; j < words.length; j++) {
                combinations.add(words[i] + words[j]);
            }
        }

        return combinations;
    }

    /**
     * Вычисляет коэффициент сходства (ratio) между двумя строками на основе
     * расстояния Левенштейна. Ratio = 1 - (distance / maxLength).
     *
     * @param s1 первая строка для сравнения
     * @param s2 вторая строка для сравнения
     * @return коэффициент сходства в диапазоне [0, 1], где 1 означает полное совпадение
     */
    public double calculateLevenshteinRatio(String s1, String s2) {
        if (s1.equals(s2)) return 1.0;

        int distance = calculateLevenshteinDistance(s1, s2);
        int maxLength = Math.max(s1.length(), s2.length());
        return maxLength == 0 ? 1.0 : 1.0 - ((double) distance / maxLength);
    }

    /**
     * Вычисляет расстояние Левенштейна между двумя строками.
     * Реализован алгоритм с использованием динамического программирования.
     *
     * @param s1 первая строка
     * @param s2 вторая строка
     * @return минимальное кол-во операций редактирования (вставка, удаление, замена),
     * необходимых для превращения одной строки в другую
     */
    private int calculateLevenshteinDistance(String s1, String s2) {
        if (s1.isEmpty()) return s2.length();
        if (s2.isEmpty()) return s1.length();

        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        // Инициализация базовых случаев
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }

        // Заполнение матрицы
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[s1.length()][s2.length()];
    }
}