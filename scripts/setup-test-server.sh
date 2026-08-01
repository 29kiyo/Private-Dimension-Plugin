#!/usr/bin/env bash
# ローカルテスト用の Paper サーバーを run/ フォルダにセットアップする。
# Git Bash / bash から実行:
#   bash scripts/setup-test-server.sh
#
# 一度実行すれば run/paper.jar が用意される（既にあれば再ダウンロードしない）。
# バージョンを変えたい場合は下の MC_VERSION を書き換えて再実行してください。

set -euo pipefail

MC_VERSION="1.21.5"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
RUN_DIR="$PROJECT_DIR/run"

mkdir -p "$RUN_DIR/plugins"
cd "$RUN_DIR"

if [ ! -f paper.jar ]; then
    echo "Paper ${MC_VERSION} の最新ビルド情報を取得中..."
    BUILD=$(curl -s "https://api.papermc.io/v2/projects/paper/versions/${MC_VERSION}/builds" \
        | grep -o '"build":[0-9]*' | tail -1 | grep -o '[0-9]*')

    if [ -z "$BUILD" ]; then
        echo "エラー: ビルド番号を取得できませんでした。MC_VERSION (${MC_VERSION}) がPaperで提供されているか確認してください。"
        exit 1
    fi

    JAR_NAME="paper-${MC_VERSION}-${BUILD}.jar"
    echo "ビルド ${BUILD} をダウンロード中: ${JAR_NAME}"
    curl -L -o paper.jar \
        "https://api.papermc.io/v2/projects/paper/versions/${MC_VERSION}/builds/${BUILD}/downloads/${JAR_NAME}"
    echo "ダウンロード完了: run/paper.jar"
else
    echo "run/paper.jar は既に存在します（再ダウンロードしません）。"
fi

echo "eula=true" > eula.txt

# server.properties が無ければ最低限の設定を作成
if [ ! -f server.properties ]; then
    cat > server.properties <<'EOF'
online-mode=false
server-port=25565
level-name=world
motd=PrivateDimension Test Server
EOF
    echo "server.properties を作成しました（online-mode=false: オフラインテスト用）"
fi

echo ""
echo "セットアップ完了。次のいずれかで起動できます:"
echo "  1) bash scripts/build-and-run.sh          （ビルド→jar配置→起動を一括実行）"
echo "  2) IntelliJ の Run Configuration 'Paper Test Server' を実行"
