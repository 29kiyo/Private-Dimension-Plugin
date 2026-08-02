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
    echo "Paper ${MC_VERSION} の最新ビルド情報を取得中... (fill.papermc.io v3 API)"

    USER_AGENT="PrivateDimension-Plugin-DevSetup/1.0"
    BUILDS_JSON=$(curl -sSf -H "User-Agent: ${USER_AGENT}" \
        "https://fill.papermc.io/v3/projects/paper/versions/${MC_VERSION}/builds") || {
        echo "エラー: ビルド一覧の取得に失敗しました。MC_VERSION (${MC_VERSION}) が存在するか、"
        echo "ネットワーク接続を確認してください。"
        exit 1
    }

    DOWNLOAD_URL=""

    if command -v jq >/dev/null 2>&1; then
        DOWNLOAD_URL=$(echo "$BUILDS_JSON" \
            | jq -r 'map(select(.channel == "STABLE")) | last | .downloads."server:default".url // empty')
        if [ -z "$DOWNLOAD_URL" ]; then
            DOWNLOAD_URL=$(echo "$BUILDS_JSON" | jq -r 'last | .downloads."server:default".url // empty')
        fi
    else
        mapfile -t CHANNEL_ARR < <(echo "$BUILDS_JSON" | grep -oP '"channel":"\K[A-Z]+')
        mapfile -t URL_ARR < <(echo "$BUILDS_JSON" | grep -oP '"server:default":\{"name":"[^"]*","url":"\K[^"]*')

        for ((i=${#CHANNEL_ARR[@]}-1; i>=0; i--)); do
            if [ "${CHANNEL_ARR[$i]}" = "STABLE" ]; then
                DOWNLOAD_URL="${URL_ARR[$i]}"
                break
            fi
        done
        if [ -z "$DOWNLOAD_URL" ] && [ "${#URL_ARR[@]}" -gt 0 ]; then
            DOWNLOAD_URL="${URL_ARR[-1]}"
        fi
    fi

    if [ -z "$DOWNLOAD_URL" ]; then
        echo "エラー: ダウンロードURLを自動取得できませんでした。"
        echo "手動でダウンロードしてください: https://papermc.io/downloads/paper"
        echo "ダウンロードした jar を run/paper.jar として保存すればこのスクリプトは不要になります。"
        exit 1
    fi

    echo "ダウンロード中: ${DOWNLOAD_URL}"
    curl -L -H "User-Agent: ${USER_AGENT}" -o paper.jar "$DOWNLOAD_URL"
    echo "ダウンロード完了: run/paper.jar"
else
    echo "run/paper.jar は既に存在します（再ダウンロードしません）。"
fi

echo "eula=true" > eula.txt

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
echo "  1) bash scripts/build-and-run.sh"
echo "  2) IntelliJ の Run Configuration 'Paper Test Server' を実行"
