# Sentinel AI — Contracts

Packaged copies of the **authoritative** API and event contracts for build-time consumption (M0-2).

## Source of truth

Do **not** invent or edit contracts here first.

| Artifact | Authoritative path |
|----------|--------------------|
| OpenAPI | [`docs/06-api/OpenAPI.yaml`](../docs/06-api/OpenAPI.yaml) |
| Event schemas / catalog / envelope | [`docs/06-api/schemas/`](../docs/06-api/schemas/) |
| Inventory | [`docs/06-api/APIInventory.md`](../docs/06-api/APIInventory.md) |
| Event contracts | [`docs/06-api/EventContracts.md`](../docs/06-api/EventContracts.md) |

After changing docs, re-run `./sync-from-docs.sh`.

## Layout

```text
contracts/
  openapi/OpenAPI.yaml
  events/                 # envelope, catalogs, events/mvp, events/v2
  validate.py
  sync-from-docs.sh
```

V2 SEC event schemas are packaged for lock verification only. **SEC remains V2** — not an MVP implementable domain.

## Validation

```bash
python3 contracts/validate.py
```
