package com.root_word.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ArabicRootExtractorWeakLetterTest {
    private final RootwordService extractor = new ArabicRootExtractor();

    @ParameterizedTest(name = "preserve root waw: {0} -> {1}")
    @CsvSource({
            "جواب, جوب",
            "الجواب, جوب",
            "والجواب, جوب",
            "فالجواب, جوب",
            "جَوَاب, جوب",
            "ثواب, ثوب"
    })
    void removesPatternAlifButPreservesMedialRootWaw(String word, String expectedRoot) {
        assertEquals(expectedRoot, extractor.extractRoot(word));
    }

    @ParameterizedTest(name = "preserve existing weak-letter behaviour: {0} -> {1}")
    @CsvSource({
            "رحيم, رحم",
            "والثالث, ثلث",
            "وعد, وعد",
            "بيع, بيع",
            "كتب, كتب"
    })
    void keepsOtherPatternsAndThreeLetterRootsWorking(String word, String expectedRoot) {
        assertEquals(expectedRoot, extractor.extractRoot(word));
    }
}
