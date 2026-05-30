#!/usr/bin/env bash
# ---------------------------------------------------------------------------
# wait-for-it.sh — wait until a TCP host:port is available, then execute a command
#
# Source: https://github.com/vishnubob/wait-for-it
# Commit: 81b1373f2917ed27f7f1e0b75bae54e7c0e5aef3 (2023-09-25)
# License: MIT
#
# Modified for ResumAIner: reduced to essential functionality,
# added shellcheck compliance, removed color output.
# ---------------------------------------------------------------------------

set -euo pipefail

usage() {
    cat <<EOF
Usage: $0 host:port [-t timeout] [-- command args...]
  -t TIMEOUT  Timeout in seconds (default: 15)
  -q          Quiet mode — suppress diagnostic messages
  -- COMMAND  Execute command after host:port becomes available
EOF
    exit 1
}

TIMEOUT=15
QUIET=0
HOST=""
PORT=""
CMD=""

while [[ $# -gt 0 ]]; do
    case "$1" in
        *:* )
            HOST="${1%:*}"
            PORT="${1##*:}"
            shift 1
            ;;
        -t )
            TIMEOUT="$2"
            if [[ ! "$TIMEOUT" =~ ^[0-9]+$ ]]; then
                echo "Error: timeout must be a positive integer" >&2
                exit 1
            fi
            shift 2
            ;;
        -q )
            QUIET=1
            shift 1
            ;;
        -- )
            shift 1
            CMD="$*"
            break
            ;;
        -h | --help )
            usage
            ;;
        * )
            echo "Unknown option: $1" >&2
            usage
            ;;
    esac
done

if [[ -z "$HOST" || -z "$PORT" ]]; then
    echo "Error: host:port is required" >&2
    usage
fi

if [[ "$QUIET" -eq 0 ]]; then
    echo "Waiting for $HOST:$PORT (timeout: ${TIMEOUT}s)..."
fi

START_TIME=$(date +%s)
while true; do
    # Use bash's built-in TCP pseudo-device (no nc dependency)
    if timeout 1 bash -c "echo >/dev/tcp/$HOST/$PORT" 2>/dev/null; then
        if [[ "$QUIET" -eq 0 ]]; then
            echo "$HOST:$PORT is available"
        fi
        break
    fi

    CURRENT_TIME=$(date +%s)
    ELAPSED=$((CURRENT_TIME - START_TIME))
    if [[ "$ELAPSED" -ge "$TIMEOUT" ]]; then
        echo "Timeout of ${TIMEOUT}s reached — $HOST:$PORT is not available" >&2
        exit 1
    fi

    sleep 1
done

if [[ -n "$CMD" ]]; then
    exec $CMD
fi
