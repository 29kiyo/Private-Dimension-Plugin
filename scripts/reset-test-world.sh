#!/usr/bin/env bash
# バージョンを切り替えてテストする際、古いバージョンのワールドデータ
# （チャンクフォーマット等）が新しいバージョンと非互換になることがあるため、
# ワールドフォルダだけを削除する（paper.jar・plugins・server.properties は残す）。
#
# Git Bash / bash から実行:
#   bash scripts/reset-test-world.sh

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
RUN_DIR="$PROJECT_DIR/run"

if [ ! -d "$RUN_DIR" ]; then
    echo "run/ フォルダがありません。何もしません。"
    exit 0
fi

cd "$RUN_DIR"

for dir in world world_nether world_the_end private_dimension; do
    if [ -d "$dir" ]; then
        rm -rf "$dir"
        echo "削除: run/$dir"
    fi
done

echo "ワールドデータをリセットしました。paper.jar と plugins/ はそのまま残っています。"
echo "次回起動時にワールドが新規生成されます。"
