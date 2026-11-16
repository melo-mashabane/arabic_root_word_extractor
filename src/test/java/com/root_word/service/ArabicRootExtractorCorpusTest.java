package com.root_word.service;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/** Test-first linguistic acceptance suite. Expected to fail until extraction is improved. */
class ArabicRootExtractorCorpusTest {
    @Test
    void matchesAnAcceptedRootForEveryRootBearingCorpusEntry() throws Exception {
        var examples = RootValidationDataset.load();
        RootwordService extractor = new ArabicRootExtractor();
        var report = new ArrayList<String>();
        report.add("source\tline\tword\taccepted_roots\tactual\toutcome");
        int correct = 0;
        int incorrect = 0;
        int unresolved = 0;
        int noRoot = 0;
        for (var example : examples) {
            String expected = String.join("|", example.acceptedRoots());
            if (!example.hasRoot()) {
                noRoot++;
                report.add(row(example, "#", "", "UNSCORED_NO_ROOT"));
                continue;
            }
            String actual;
            String outcome;
            try {
                actual = extractor.extractRoot(example.word());
                if (example.acceptedRoots().contains(actual)) {
                    correct++;
                    outcome = "CORRECT";
                } else {
                    incorrect++;
                    outcome = "INCORRECT";
                }
            } catch (IllegalArgumentException exception) {
                // Valid labelled examples rejected by today's extractor are failures, not skipped tests.
                actual = "";
                unresolved++;
                outcome = "UNRESOLVED";
            }
            report.add(row(example, expected, actual, outcome));
        }
        int scored = correct + incorrect + unresolved;
        assertTrue(scored > 0, "Corpus must contain root-bearing entries");
        String summary = String.format(Locale.ROOT,
                "Rows=%d; scored=%d; correct=%d; incorrect=%d; unresolved=%d; no-root unscored=%d; exact accuracy=%.2f%%%n",
                examples.size(), scored, correct, incorrect, unresolved, noRoot, 100.0 * correct / scored);
        Path directory = Path.of("target", "root-evaluation");
        Files.createDirectories(directory);
        Files.write(directory.resolve("results.tsv"), report, StandardCharsets.UTF_8);
        Files.writeString(directory.resolve("summary.txt"), summary, StandardCharsets.UTF_8);
        System.out.print(summary);
        assertEquals(0, incorrect + unresolved,
                summary + "See target/root-evaluation/results.tsv for every result. Extractor is unchanged; this is the acceptance gap.");
    }

    private static String row(RootValidationDataset.Example example, String expected, String actual, String outcome) {
        return String.join("\t", example.source(), Integer.toString(example.line()), example.word(), expected, actual, outcome);
    }
}
