# Research: Home Page Saved Resume Details Modal Fix

**Feature**: `009-home-modal-fix` | **Date**: 2026-06-24

---

## 1. Research Scope

This feature is a repair of existing code with no new technology choices. Research focuses on existing patterns, codebase alignment, and guardrails that prevent implementation drift.

---

## 2. Existing Patterns and Decisions

### 2.1 Vue 3 v-model with computed bridge

**Decision**: Use `computed` get/set bridge for `v-model:visible` on PrimeVue Dialog.

**Rationale**: The existing bug is caused by `ref(props.visible)`, which copies the prop once and does not stay synchronized with parent state. Vue 3 custom `v-model` wrapper pattern uses a writable computed value. PrimeVue Dialog `v-model:visible` expects writable reactive state.

**Alternatives considered**:

- `watch` prop → update local ref: more verbose and easier to desynchronize.
- Direct `defineModel` (Vue 3.4+): cleaner but not currently used by this codebase.

**Context7 required**: Verify Vue 3 writable computed and PrimeVue 4 Dialog `v-model:visible` usage before implementing.

---

### 2.2 Vue Clipboard API

**Decision**: Use `navigator.clipboard.writeText()` with try/catch and user-readable feedback.

**Rationale**: The project already uses clipboard behavior in the existing modal. Modern browsers support it, but Playwright and non-secure contexts may require permission handling or fallback feedback.

**Guardrail**: Copy cover letter must copy the full cover-letter text even when the modal shows only a preview.

---

### 2.3 PrimeVue DataTable row click

**Decision**: Use PrimeVue DataTable row-click behavior to open modal. Do not add a Details column/button.

**Rationale**: The spec explicitly forbids a Details column/button because the table is already long. Row click plus pointer cursor/hover highlight is the approved UX.

**Context7 required**: Verify PrimeVue 4 DataTable row-click event payload and row styling API before implementation.

---

### 2.4 PrimeVue Dialog

**Decision**: Reuse existing `Dialog` component with `v-model:visible` computed bridge.

**Rationale**: Existing project already uses PrimeVue components. Required features: modal behavior, close icon, Escape/backdrop closing, and parent-state synchronization.

**Context7 required**: Verify PrimeVue 4 Dialog API for close behavior and `v-model:visible`.

---

### 2.5 Public route delay pattern (B28)

**Decision**: Reuse the existing uniform artificial delay for both `404` and `410` public-route error responses.

**Rationale**: The product decision intentionally returns `410 Gone` for known soft-deleted public links. The existing B28-style delay prevents timing-based enumeration. The different status code remains an accepted product tradeoff for recruiter UX.

**Guardrail**: If the existing delay mechanism is not found in the codebase, STOP and ask. Do not invent a new delay value, do not add arbitrary `Thread.sleep` directly in a controller, and do not change public route timing semantics without user confirmation.

---

### 2.6 DTO URL contract (D31)

**Decision**: Backend provides full canonical URLs. Frontend consumes them and never constructs PDF/HTML/public URLs.

**Rationale**: Feature 008 established canonical DTO URL fields: `pdfDownloadUrl`, `pdfOpenUrl`, `htmlDownloadUrl`, `publicUrlLink`. The new `HomeSavedResumeDto` must follow the same contract so Home modal and Export page do not drift.

---

### 2.7 Canonical authenticated export endpoints

**Decision**: New Home modal URLs must use canonical authenticated export endpoints from `GenerateResumeController`:

```text
GET /api/generate/resumes/{id}/pdf?disposition=inline
GET /api/generate/resumes/{id}/pdf
GET /api/generate/resumes/{id}/html
```

**Rationale**: These match the post-finalization export contract. `ResumeDownloadController` is legacy/deprecated and must not drive new modal URLs.

**Guardrail**: If current code uses different canonical endpoints, STOP and report exact controller mappings before implementing.

---

### 2.8 Public base URL resolution

**Decision**: Backend builds `publicUrlLink` using a strict resolution order:

1. `APP_PUBLIC_BASE_URL` environment/config value;
2. existing property binding if the project already has one;
3. forwarded headers (`X-Forwarded-Proto`, `X-Forwarded-Host`, optionally `X-Forwarded-Port`);
4. request scheme/host/port fallback.

**Rationale**: Local dev and VPS/reverse-proxy deployment need different origins without code changes. `.env.example` documents the variable, but Java/Tomcat reads environment/config at runtime, not `.env` automatically.

**Guardrail**: Do not add a new dotenv library unless the project already uses one. Log a warning when fallback origin is used.

---

### 2.9 Security review outcomes

**Decision**: Apply plan-level security-review mitigations during implementation.

**Required mitigations**:

- `410.html` contains no dynamic resume data, IDs, paths, filenames, usernames, public codes, or deletion dates.
- Delete failures use one generic user-facing error message.
- Public URL fallback supports reverse-proxy headers.
- Non-owned and non-existent delete attempts return the same public response shape.
- Public route does not intercept `/api/**`, `/app/**`, `/static/**`, `/assets/**`, `/error/**`, landing page routes, or Vue SPA assets.

---

## 3. Technology Choices (confirmed)

All technology choices are inherited from existing project stack:

| Area | Choice | Status |
|---|---|---|
| Backend testing | JUnit 5 + Mockito + standalone MockMvc | Confirmed |
| Frontend testing | Vitest + Vue Test Utils | Confirmed |
| E2E testing | Playwright MCP | Confirmed |
| PDF/HTML authenticated serving | Existing canonical GenerateResumeController export mechanism | Confirmed |
| Error pages | Backend Thymeleaf templates | Confirmed |
| Public URL config | Environment/config + forwarded header/request fallback | Confirmed |

---

## 4. No NEEDS CLARIFICATION

All technical decisions are resolved by existing project patterns and explicit spec requirements. If implementation discovers a mismatch with actual code, the implementation agent must STOP and ask before silently changing the contract.
