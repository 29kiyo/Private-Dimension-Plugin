#!/usr/bin/env bash
# ビルド → jar配置 → Paperサーバー起動 を一括で行う。
# 初回は先に scripts/setup-test-server.sh を実行しておくこと。
#
# Git Bash / bash から実行:
#   bash scripts/build-and-run.sh

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

bash "$SCRIPT_DIR/build-and-copy.sh"

cd "$PROJECT_DIR/run"
if [ ! -f paper.jar ]; then
    echo "run/paper.jar がありません。先に scripts/setup-test-server.sh を実行してください。"
    exit 1
fi

echo "Paper サーバーを起動します（停止するには 'stop' と入力するか Ctrl+C）"
java -Xms1G -Xmx2G -jar paper.jar --nogui
