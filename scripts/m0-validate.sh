#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

echo "== contracts =="
python3 "$ROOT/contracts/validate.py"

echo "== backend =="
export JAVA_HOME="${JAVA_HOME_21:-$(/usr/libexec/java_home -v 21)}"
export PATH="$JAVA_HOME/bin:$PATH"
"$ROOT/backend/gradlew" -p "$ROOT/backend" test

echo "== ai-service =="
if [[ ! -d "$ROOT/ai-service/.venv" ]]; then
  python3 -m venv "$ROOT/ai-service/.venv"
fi
# shellcheck disable=SC1091
source "$ROOT/ai-service/.venv/bin/activate"
pip install -q -r "$ROOT/ai-service/requirements.txt"
(cd "$ROOT/ai-service" && pytest && ruff check app tests)
deactivate

echo "== web =="
(cd "$ROOT/web" && npm install && npm test && npm run lint && npm run build)

echo "M0 validation PASS"
