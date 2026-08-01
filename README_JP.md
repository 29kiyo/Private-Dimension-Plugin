# PrivateDimension

プライベート次元プラグイン for Paper 1.21.5+

[English](README.md)

## このデータパックについて
A derivative work of “Private_Dimension” by Chuzume.<br>

このデータパックは、Chuzume様が作成された「Private_Dimension」をプラグイン版に改変したものです。
本データパックに関する著作権その他の権利はChuzume様に帰属します。

また、本データパックを導入・使用したことによって発生したいかなる問題や損害についても、制作者および配布者は一切の責任を負いかねます。ご了承ください。


## 原作・参考リンク

- 制作者: <br>[@Chuzume](https://x.com/Chuzume)
- レポジトリ: <br>[Private_Dimension](https://github.com/Chuzume/Private_Dimension)
- 動画: <br>[【マイクラ】"次元の瓶"で、家とか拠点を持ち歩いちゃおう！！！【データパック】](https://www.youtube.com/watch?v=NrwN3NJLuiA)


## 使い方
レシピ
通常の作業台にて作れます

[Dimension in a Bottle]

![Image](https://cdn-ak.f.st-hatena.com/images/fotolife/C/Chuzume/20230105/20230105085556.png)

## 概要

**Dimension in a Bottle** アイテムを使うと、自分専用のプライベート次元に移動できます。
次元内は 48×48 の構造物が生成された専用スペースです。

元データパック [Private Dimension by Chuzume](https://github.com/Chuzume/Private_Dimension) の機能を、Paper プラグインとして 29kiyo が再実装したものです。

## 機能

| 機能 | 説明 |
|------|------|
| 🌀 次元移動 | Dimension in a Bottle を右クリックするとプライベート次元へ移動 |
| 🔙 帰還 | 次元内で再び使用すると元の座標に戻る |
| 👥 エンティティ連行 | スニーク+使用で半径3ブロック内の友好的エンティティを連れていける |
| 🏠 48×48 プロット | プレイヤーごとに専用の 48×48 空間を自動割り当て |
| 🚫 プロット境界 | プロット外に出ると強制的に元の世界へ送還 |
| ☠️ 死亡対応 | 次元内で死亡しても元の世界でリスポーン |
| 📱 Geyser対応 | Java版・統合版（Geyser）両対応 |
| 🧭 セーフスポーン探索 | 計算上のスポーン地点から周囲を自動探索し、安全な地面を見つけてスポーンさせる。床の高さが異なるカスタム構造物でも正しくスポーンできる |
| 🗂️ カスタムNBT構造物 | `plugins/PrivateDimension/structures/` に自作の `.nbt` ファイルを置くと、デフォルトのプロット構造物を差し替えられる |
| ⌨️ タブ補完 | `/pd` のサブコマンドと `/pd give` のプレイヤー名がゲーム内で補完される |

## 必要環境

- **Paper** 1.21.5+
- **Java** 21+
- Geyser（統合版対応、任意）

## インストール

1. `PrivateDimension-*.jar` を `plugins/` フォルダへ配置
2. サーバーを再起動
3. 起動後、`private_dimension` ワールドが自動生成されます

## コマンド

| コマンド | 説明 | 権限 |
|---------|------|------|
| `/pd give [player]` | アイテムを付与 | `privatedimension.admin` |
| `/pd info` | 自分のプロット情報表示 | 全員 |
| `/pd reload` | 設定をリロード | `privatedimension.admin` |
| `/pd debug` | 境界チェックのデバッグ状態を表示 | `privatedimension.debug` |

サブコマンド・`/pd give` のプレイヤー名はいずれもタブ補完に対応しています。

## 権限

| 権限ノード | デフォルト | 説明 |
|-----------|-----------|------|
| `privatedimension.use` | true | アイテム使用 |
| `privatedimension.admin` | op | 管理コマンド |
| `privatedimension.debug` | op | プロット境界無視 |

## 設定 (config.yml)

```yaml
world-name: "private_dimension"   # 次元ワールド名
plot-size: 48                      # プロットサイズ
plot-spacing: 128                  # プロット間隔
plot-floor-y: 64                   # 床のY座標（スポーンY = floor-y + 5 = 69、セーフスポーン探索でさらに補正）
plot-height: 47                    # 構造物の高さ（Yサイズ）。境界判定・セーフスポーン探索に使用
structure-file: "plot48x48.nbt"    # 使用する構造物ファイル名。詳細は下記「カスタム構造物」参照
safe-spawn-search-radius: 8        # セーフスポーン探索の水平探索半径（ブロック）
safe-spawn-search-height: 12       # セーフスポーン探索の上下探索範囲（ブロック）
pull-entity-limit: 10              # 連行エンティティ最大数
pull-entity-radius: 3.0            # 連行半径（ブロック）
enable-border-enforcement: true    # 境界強制送還
```

### カスタム構造物（カスタムNBT）

デフォルトでは同梱の `plot48x48.nbt` が使用されます。自作の構造物に差し替える手順:

1. Structure Block を **SAVE** モードにして構造物を `.nbt` として書き出す。
2. サーバーを一度起動し、`plugins/PrivateDimension/structures/` フォルダを生成させる（使い方を書いた `README.txt` も自動生成されます）。
3. 書き出した `.nbt` ファイルを、`config.yml` の `structure-file` と同じファイル名でそのフォルダに置く（またはファイル名に合わせて `structure-file` を書き換える）。
4. 構造物のサイズ（幅・高さ）が 48×48×47 と異なる場合は `plot-size` / `plot-height` も合わせて変更する。
5. `/pd reload` を実行するか、サーバーを再起動する。

本プラグインは **セーフスポーン探索** を行うため（計算上のスポーン地点から周囲を探索し、頭上が空いた固い地面を探す）、床の高さがデフォルトと異なるカスタム構造物でも、基本的にスポーンYオフセットを手動調整する必要はありません。

## 開発 / IntelliJ IDEA でのローカルテスト

IntelliJ から直接 Paper サーバーを起動してプラグインをテストできます。

1. **初回セットアップ** — `run/` フォルダに Paper サーバーjarをダウンロード:
   ```bash
   bash scripts/setup-test-server.sh
   ```
2. **ビルドしてテストサーバーへコピー:**
   ```bash
   bash scripts/build-and-copy.sh
   ```
   （コードを変更するたびに再実行し、サーバーを再起動/リロードしてください）
3. **サーバー起動:**
   - IntelliJ から: **Run/Debug Configurations** を開き、`.run/` に含まれる **Paper Test Server**（IntelliJが自動検出）を選んで ▶ 実行。
   - またはターミナルから: `bash scripts/build-and-run.sh`（ビルド・コピー・起動を一括実行）
4. Java版クライアントで `localhost:25565` に接続（生成される `server.properties` は手軽にテストできるよう `online-mode=false` になっています。本アカウント認証が必要な場合は変更してください）
5. コード修正後はサーバーコンソールで `stop` → `scripts/build-and-copy.sh` を再実行 → 再起動、を繰り返します。

`run/` フォルダ（サーバーjar・ワールドデータ・ログ）は `.gitignore` 済みで、いつでも削除・再生成して問題ありません。

## 開発について

このツールのコードはすべてAI（Claude）に書いてもらいました。
