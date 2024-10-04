package com.root_word.service;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

class ArabicRootExtractorTest {

    @Test
    void extractRoot() {

        RootwordService service = new ArabicRootExtractor();

        List<String> list = Arrays.asList("المكتبة", "أسبوع", "ثانية"); // Example words

        list.forEach(word -> {
            String extractedRootWord = service.extractRoot(word);
            System.out.println("Root letters: " + extractedRootWord);
        });
    }
}