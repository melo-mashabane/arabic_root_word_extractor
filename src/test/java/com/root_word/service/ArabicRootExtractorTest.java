package com.root_word.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class ArabicRootExtractorTest {
    private final RootwordService service = new ArabicRootExtractor();

    @ParameterizedTest
    @CsvSource({"المكتبة,كتب", "كَتَبَ,كتب", "كـتب,كتب", "وعد,وعد", "بيع,بيع", "كتب,كتب"})
    void extractsKnownCandidates(String word, String expected) {
        assertEquals(expected, service.extractRoot(word));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"hello", "كتب كتاب", "12", "من", "َََ", "كتب!", "اواو"})
    void rejectsInvalidInput(String word) {
        assertThrows(IllegalArgumentException.class, () -> service.extractRoot(word));
    }
}
