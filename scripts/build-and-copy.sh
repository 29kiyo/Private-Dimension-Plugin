#!/usr/bin/env bash
# mvn でビルドし、生成された jar を run/plugins/ にコピーする。
# 既存の PrivateDimension-*.jar は削除してから新しいものをコピーする（重複防止）。
#
# Git Bash / bash から実行:
#   bash scripts/build-and-copy.sh

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

cd "$PROJECT_DIR"
mvn -q clean package

mkdir -p run/plugins
rm -f run/plugins/PrivateDimension-*.jar

JAR_FILE=$(ls target/PrivateDimension-*.jar | grep -v sources | grep -v javadoc | head -1)
cp "$JAR_FILE" run/plugins/

echo "コピー完了: run/plugins/$(basename "$JAR_FILE")"
