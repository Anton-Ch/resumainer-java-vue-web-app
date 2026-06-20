# Research: PDF Generation for Feature 008

## Decision: OpenHTMLToPDF + PDFBox

**Chosen**: OpenHTMLToPDF 1.0.10 with PDFBox 2.0.30 backend

**Rationale**:
- Spike validated this combination across 17 edge case scenarios (EN+RU, 1-page and 2-page).
- Pure Java — no native dependencies, no external process (vs wkhtmltopdf).
- PDFBox 2.0.30 is the exact version used and tested in the spike.
- OpenHTMLToPDF 1.0.10 is the latest stable release as of June 2026.
- CSS 2.1+ support is sufficient for resume layout (tables, fonts, page breaks).
- OFL-licensed fonts (Inter, Manrope) embedded directly.

**Alternatives considered**:

| Alternative | Verdict | Reason |
|---|---|---|
| Flying Saucer (legacy) | Rejected | Unmaintained; spike explicitly validated OpenHTMLToPDF fork |
| wkhtmltopdf | Rejected | External process dependency; Docker complexity; Cyrillic issues in older versions |
| iText | Rejected | AGPL licensing; overkill for resume documents |
| Apache FOP | Rejected | XSL-FO required; no CSS support |

## OpenHTMLToPDF CSS Limitations

Per official documentation (Context7 verified):

- **Not supported**: flexbox, CSS grid, `row-gap`, `column-gap`, CSS `break-inside: avoid` (unreliable)
- **Limited support**: `position: relative/absolute` (disrupts tagged content reading order)
- **Not supported**: `overflow: hidden` (incompatible with PDF/UA)
- **Recommended**: use `div` with `display: table` instead of `<table>` elements
- **Fonts**: `@font-face` or programmatic `AutoFont` loading; must not be subset for form controls (N/A for resumes)
- **Page size**: explicit `@page { size: A4; }` in CSS

## Maven Dependencies

From spike `pom.xml` (group `com.openhtmltopdf`):

```xml
<dependency>
    <groupId>com.openhtmltopdf</groupId>
    <artifactId>openhtmltopdf-pdfbox</artifactId>
    <version>1.0.10</version>
</dependency>
<dependency>
    <groupId>org.apache.pdfbox</groupId>
    <artifactId>pdfbox</artifactId>
    <version>2.0.30</version>
</dependency>
```

**Note**: Context7 also documents group `io.github.openhtmltopdf`. At implementation time, verify which group ID is available on Maven Central for version 1.0.10. The spike uses `com.openhtmltopdf`.

SLF4J integration (optional, recommended):

```xml
<dependency>
    <groupId>com.openhtmltopdf</groupId>
    <artifactId>openhtmltopdf-slf4j</artifactId>
    <version>1.0.10</version>
</dependency>
```

## Font Loading Strategy

Per brainstorming Q2: Inter (body) + Manrope (headings) ported from spike `src/main/resources/fonts/`. Both OFL-licensed. Isolated to PDF pipeline — do not affect existing web fonts.

Two approaches available (spike uses programmatic):
1. Programmatic: `PdfRendererBuilder.useFont()` — spike approach, explicit control
2. CSS: `@font-face { src: url(...); }` in XHTML template

Plan: use programmatic approach (matches spike, avoids path resolution issues at PDF render time).

## Storage Strategy

Per brainstorming Q1: Files on configurable filesystem directory. Metadata in database via `saved_resume` record. Compensation logic for file+DB rollback. Configurable via Spring `@Value` or equivalent.

## Fitting Engine Behavior

Per spike V12.1: round-robin shrink/grow across font size, line-height, section gap, item gap, paragraph gap, bullet gap. Bounded by `max_attempts` from DB config. Adaptive page2 min-fill: 0 projects → 0.30 fill threshold.
