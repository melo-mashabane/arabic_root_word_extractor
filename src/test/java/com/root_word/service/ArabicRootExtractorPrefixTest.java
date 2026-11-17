package com.root_word.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Focused acceptance examples; provenance is in root-validation/FAILURE_ANALYSIS.md. */
class ArabicRootExtractorPrefixTest {
    private final RootwordService extractor = new ArabicRootExtractor();

    @ParameterizedTest(name = "{0} should resolve to {1}")
    @CsvSource({
            "والكتب, كتب",
            "والعرب, عرب",
            "والرمح, رمح",
            "والثالث, ثلث"
    })
    void recognisesArticleFollowingConjunction(String word, String expectedRoot) {
        assertEquals(expectedRoot, extractor.extractRoot(word));
    }

    @ParameterizedTest(name = "preserve existing lookup: {0} -> {1}")
    @CsvSource({
            "الكتب, كتب",
            "العرب, عرب",
            "الثالث, ثلث"
    })
    void stillRecognisesArticleWithoutConjunction(String word, String expectedRoot) {
        assertEquals(expectedRoot, extractor.extractRoot(word));
    }

    @ParameterizedTest
    @CsvSource("ولد, ولد")
    void doesNotTreatEveryInitialWawAsAConjunction(String word, String expectedRoot) {
        assertEquals(expectedRoot, extractor.extractRoot(word));
    }
}
