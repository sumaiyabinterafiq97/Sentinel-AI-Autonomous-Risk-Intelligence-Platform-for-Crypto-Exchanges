# Hallucination Testing

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Hallucination Testing |
| Version | 0.1 (Draft) |
| Status | Draft — Phase 8 |
| Last Updated | 2026-09-11 |

---

## Purpose

Define how to detect fabricated AI claims. **No executable test harness in Phase 8.**

---

## Test Categories

| Category | Example |
|----------|---------|
| Invented case/alert IDs | Output cites non-existent IDs |
| Invented evidence | Claims documents not retrieved |
| Contradicts RISK score | Explanation conflicts with assessment fields |
| Overconfident enforcement | Suggests AI blocked a transaction |

---

## Method

1. Golden negatives with known ground truth
2. Require source references (AI-FR-005)
3. Human sampling for MVP simulation
4. Automated checks deferred to CI design / V2 eval runtime

---

## Related Documents

- [EvaluationFramework.md](EvaluationFramework.md)
- [AIArchitecture.md](../03-architecture/AIArchitecture.md)
