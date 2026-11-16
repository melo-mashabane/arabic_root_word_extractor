package com.root_word.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class RootValidationDatasetTest {
    @Test
    void preservesUpstreamFilesAndRowCounts() throws Exception {
        var stream = Objects.requireNonNull(getClass().getResourceAsStream("/root-validation/SHA256SUMS"));
        String manifest;
        try (stream) {
            manifest = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
        assertEquals(3, manifest.lines().count());
        for (String entry : manifest.lines().toList()) {
            String[] parts = entry.split("  ", 2);
            try (var data = Objects.requireNonNull(getClass().getResourceAsStream("/root-validation/" + parts[1]))) {
                // verifies file integrity
                assertEquals(parts[0], HexFormat.of().formatHex(
                        MessageDigest.getInstance("SHA-256").digest(data.readAllBytes())), parts[1]);
            }
        }
        var examples = RootValidationDataset.load();
        assertEquals(2962, examples.size());
        assertEquals(List.of(985L, 981L, 996L), RootValidationDataset.FILES.stream()
                .map(file -> examples.stream().filter(row -> row.source().equals(file)).count()).toList());
        assertTrue(examples.stream().anyMatch(row -> !row.hasRoot()));
        assertTrue(examples.stream().anyMatch(row -> row.acceptedRoots().size() > 1));
    }

    @Test
    void acceptsAlternativeRootsWithoutChoosingOne() {
        var row = RootValidationDataset.parse("sample.csv", 2, "بسم,سمو\\سما");
        assertEquals("بسم", row.word());
        assertEquals(List.of("سمو", "سما"), row.acceptedRoots());
    }

    @Test
    void treatsHashAsAnUnscoredNoRootLabel() {
        assertFalse(RootValidationDataset.parse("sample.csv", 2, "يا,#").hasRoot());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "word", "word,", ",root", "word,root,extra", "word,root\\", "word,#\\root", "\"word\",root"})
    void failsOnMalformedDataInsteadOfSilentlySkippingIt(String row) {
        assertThrows(IllegalArgumentException.class, () -> RootValidationDataset.parse("sample.csv", 2, row));
    }
}
