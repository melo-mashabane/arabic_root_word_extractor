package com.root_word.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/** Reader for the pinned upstream's two-column, unquoted CSV format. */
final class RootValidationDataset {
    static final List<String> FILES = List.of(
            "0450AbuHasanMawardi.HawiKabir-sample.csv",
            "0460ShaykhTusi.Mabsut-sample.csv",
            "0483IbnAhmadSarakhsi.Mabsut-sample.csv");

    record Example(String source, int line, String word, List<String> acceptedRoots) {
        boolean hasRoot() {
            return !acceptedRoots.isEmpty();
        }
    }

    static List<Example> load() throws IOException {
        var examples = new ArrayList<Example>();
        for (String file : FILES) {
            var stream = Objects.requireNonNull(RootValidationDataset.class.getResourceAsStream(
                    "/root-validation/gs/" + file), "Missing dataset: " + file);
            try (var reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                if (!"word,root".equals(reader.readLine())) {
                    throw new IllegalArgumentException("Unexpected CSV header: " + file);
                }
                String row;
                int line = 1;
                while ((row = reader.readLine()) != null) {
                    examples.add(parse(file, ++line, row));
                }
            }
        }
        return List.copyOf(examples);
    }

    static Example parse(String source, int line, String row) {
        String[] fields = row.split(",", -1);
        if (row.contains("\"") || fields.length != 2 || fields[0].isBlank() || fields[1].isBlank()) {
            throw new IllegalArgumentException("Invalid dataset row: " + source + ":" + line);
        }
        List<String> roots = fields[1].equals("#") ? List.of()
                : Arrays.asList(fields[1].split("\\\\", -1));
        if (roots.stream().anyMatch(root -> root.isBlank() || root.contains("#"))) {
            throw new IllegalArgumentException("Invalid root labels: " + source + ":" + line);
        }
        return new Example(source, line, fields[0], List.copyOf(roots));
    }
}
