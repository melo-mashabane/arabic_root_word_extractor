package com.root_word.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@org.springframework.stereotype.Service
public class ArabicRootExtractor implements RootwordService{

    // Common Arabic prefixes
    private static final List<String> PREFIXES = Arrays.asList("ال", "و", "ف", "ب", "ك", "ل", "س", "م");

    // Common Arabic suffixes
    private static final List<String> SUFFIXES = Arrays.asList("ة", "ات", "ون", "ين", "ان", "ي", "ه", "ها", "هم", "نا");

    // Known weak letters (used in irregular verbs and nouns)
    private static final Set<Character> WEAK_LETTERS = new HashSet<>(Arrays.asList('ا', 'و', 'ي'));

    public ArabicRootExtractor() {
    }

    // Function to extract root letters
    @Override
    public String extractRoot(String word) {
        if (word == null || word.length() > 64 || !word.matches("[\\u0621-\\u063A\\u0641-\\u0652\\u0670\\u0640]+")) {
            throw new IllegalArgumentException("Invalid Arabic word");
        }
        word = word.replaceAll("[\\u064B-\\u0652\\u0670\\u0640]", "");
        if (word.length() < 3) {
            throw new IllegalArgumentException("Insufficient Arabic letters");
        }
        // Step 1: Remove common prefixes
        word = removePrefixes(word);

        // Step 2: Remove common suffixes
        word = removeSuffixes(word);

        // Step 3: Extract triliteral root by removing weak letters if necessary
        return extractTrilateralRoot(word);
    }

    // Helper function to remove common Arabic prefixes
    private String removePrefixes(String word) {
        for (String prefix : PREFIXES) {
            if (word.startsWith(prefix) && word.length() - prefix.length() >= 3) {
                word = word.substring(prefix.length());
            }
        }
        return word;
    }

    // Helper function to remove common Arabic suffixes
    private String removeSuffixes(String word) {
        for (String suffix : SUFFIXES) {
            if (word.endsWith(suffix) && word.length() - suffix.length() >= 3) {
                word = word.substring(0, word.length() - suffix.length());
                break;
            }
        }
        return word;
    }

    // Function to extract a triliteral root, considering weak letters
    private String extractTrilateralRoot(String word) {
        StringBuilder root = new StringBuilder();

        // Iterate through the word and pick out likely root letters (ignoring weak letters)
        for (char letter : word.toCharArray()) {
            if (word.length() == 3 || !WEAK_LETTERS.contains(letter)) {
                root.append(letter);
            }
        }

        if (root.length() < 3) {
            throw new IllegalArgumentException("No three-letter candidate found");
        }
        // Return first three non-weak letters as the candidate
        return root.length() > 3 ? root.substring(0, 3) : root.toString();
    }
}
