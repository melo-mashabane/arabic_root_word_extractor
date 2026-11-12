package com.root_word.controller;

import com.root_word.service.RootwordService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/roots")
public class RootWordController {
    private final RootwordService service;

    public RootWordController(RootwordService service) {
        this.service = service;
    }

    @PostMapping
    public RootResponse extract(@Valid @RequestBody RootRequest request) {
        return new RootResponse(service.extractRoot(request.word()), "heuristic-v1");
    }

    public record RootRequest(
            @NotBlank @Size(max = 64)
            @Pattern(regexp = "[\\u0621-\\u063A\\u0641-\\u064A\\u064B-\\u0652\\u0670\\u0640]+",
                    message = "must contain a single Arabic word") String word) {}

    public record RootResponse(String candidate, String algorithm) {}
}
