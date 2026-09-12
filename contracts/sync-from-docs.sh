#!/usr/bin/env bash
# Refresh packaged contracts from docs/06-api (authoritative).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
mkdir -p "$ROOT/contracts/openapi" "$ROOT/contracts/events"
cp "$ROOT/docs/06-api/OpenAPI.yaml" "$ROOT/contracts/openapi/OpenAPI.yaml"
rsync -a --delete "$ROOT/docs/06-api/schemas/" "$ROOT/contracts/events/"
echo "Synced contracts from docs/06-api."
