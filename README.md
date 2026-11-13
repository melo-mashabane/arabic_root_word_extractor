# Arabic root candidate API

A stateless Spring Boot service producing **heuristic root candidates**. This is an engineering baseline, not a linguistically validated extractor or a public-release-ready service.

## Run and verify

Use JDK 17. The Maven wrapper downloads Maven on first use.

```sh
./mvnw verify
API_KEY=replace-with-a-long-random-secret ./mvnw spring-boot:run
```

```sh
curl -i http://localhost:8080/api/v1/roots \
  -H 'Content-Type: application/json' \
  -H 'X-API-Key: replace-with-a-long-random-secret' \
  -d '{"word":"المكتبة"}'
```

Response: `{"candidate":"كتب","algorithm":"heuristic-v1"}`.

`POST /api/v1/roots` accepts one `word`, 1–64 characters, consisting of supported Arabic letters, vowel marks, and tatweel. Spaces, Latin letters, digits, and punctuation are rejected. Normalization removes vowel marks and tatweel. At least three letters must remain. Invalid requests return HTTP 400; words for which no three-letter candidate can be extracted return 422. Errors use `application/problem+json`. Unsupported Arabic extensions and presentation forms are intentionally rejected in v1; broaden support with explicit normalization tests when required.

## Container

```sh
docker build -t arabic-root-extractor .
docker run --rm -e API_KEY -p 8080:8080 --memory=256m --cpus=1 arabic-root-extractor
```

Set and export `API_KEY` from your secret manager before running the container. Missing server configuration returns 503; missing or incorrect client keys return 401. Health probes do not require a key. Use HTTPS and never commit a real key. The shared key supports a controlled pilot; use per-consumer credentials and revocation for broader access.

The image runs as an unprivileged user. `PORT` overrides the default 8080. Send SIGTERM and allow at least 25 seconds before forcing termination. No persistence is required. Image tags are not immutable: pin reviewed image digests before release.

Readiness: `/actuator/health/readiness`; liveness: `/actuator/health/liveness`. Only health endpoints are exposed. Application logs go to stdout; do not add raw submitted words to logs without an explicit retention/privacy decision.

## Production walkthrough and release gates

1. **Define correctness before scaling.** The original algorithm deleted weak root letters and could strip real root letters as affixes. Length guards and three-letter preservation fix specific regressions, but ambiguous affixes, weak roots in longer words, quadriliteral roots, hamza, and shadda remain unresolved. Build a labelled, independently reviewed corpus covering these categories. Measure accuracy against a baseline and agree an acceptance threshold. Evaluate a dictionary or morphological analyser against this same corpus. Do not present candidates as authoritative roots.
2. **Make the boundary predictable.** A versioned route, validated request, explicit candidate/algorithm response, and standard errors let clients integrate without depending on implementation internals. Constructor injection keeps transport separate from extraction. Tests cover the HTTP boundary and known regressions; they do not prove general Arabic accuracy.
3. **Make builds repeatable.** CI runs the full Maven verification on Java 17; the container also verifies before packaging. Before release, move the existing Spring Boot 3.3.4 dependency set to a currently supported line, review migration notes, and scan dependencies and the final image. The framework upgrade is a release gate rather than an untested version change bundled with the API work.
4. **Bound public traffic.** Before internet exposure, put the service behind TLS termination with authentication appropriate to its consumers, rate limits, request-body size limits (for example 4 KiB), connection/request timeouts, and concurrency limits. The 64-character field constraint applies after JSON parsing and is not a transport body-size limit. Shared-key authentication is implemented; a rate limiter is not. Use a private staging network until those controls exist.
5. **Observe and exercise failure.** Probe health, collect request rate/error rate/latency and resource usage, and choose SLOs from consumer needs. Wire metrics to the chosen platform without publicly exposing management details. Run a representative load test and a shutdown-under-traffic test; tune memory and CPU from measurements. The sample container limits are starting points, not capacity claims.
6. **Release reversibly.** Select a hosting platform, deploy an immutable image to staging, exercise success/error/probe paths, then roll out gradually. Retain the previous image for rollback and verify rollback in staging. Record ownership, alert routing, and an incident runbook before public launch.

CI and container definitions are supplied but must be executed in their target environments. No deployment has been performed.
