# PrivateDimension

プライベート次元プラグイン for Paper 1.21.5+

[English](README.md)

## このデータパックについて
A derivative work of "Private_Dimension" by Chuzume.<br>

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
| ☠️ 死亡対応 | 次元内で死亡しても元の世界でリスポーン |
| 📱 Geyser対応 | Java版・統合版（Geyser）両対応 |
| 🧭 セーフスポーン探索 | 計算上のスポーン地点から周囲を自動探索し、安全な地面を見つけてスポーンさせる。床の高さが異なるカスタム構造物でも正しくスポーンできる |
| 🚧 プロット内への押し戻し | プロットの外に出ると元の世界にではなく、自分のプロットのスポーン地点へ押し戻される（MOD版と同じ挙動）。押し戻し直後3秒間は落下ダメージが無効化される |
| 🏷️ バイパスタグ | `pd_free`（設定変更可）のスコアボードタグを持つプレイヤーはOPと同様に境界チェックを回避できる |
| 🗂️ カスタムNBT構造物 | `plugins/PrivateDimension/structures/` に自作の `.nbt` ファイルを置くと、デフォルトのプロット構造物を差し替えられる |
| ⌨️ タブ補完 | `/pd` のサブコマンドと `/pd give` のプレイヤー名がゲーム内で補完される |
| 🌐 多言語対応 | チャットメッセージ・コマンド出力・アイテムの名前や説明文はすべて `lang/en.yml` / `lang/ja.yml` から取得される。自作の言語ファイルを追加してフル翻訳することも可能 |
| 📖 レシピ自動解放 | サーバー参加時に Dimension in a Bottle のレシピが自動的に発見済みになり、クラフト表に常に表示される |
| ✨ エンチャント風の光沢 | アイテムに見た目だけのエンチャント光沢が付いている（実際のエンチャント効果は無し） |

## 必要環境

- **Paper**（Spigotも可） 1.21.5+ — MOD版と同じバージョン範囲（1.21.5〜26.1.2）での動作を目標としており、複数バージョンでの動作確認は継続中です
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

## プロット境界のバイパス

デフォルトでは、プロット外に出ると自分のプロット内へ押し戻されます。以下の2通りでバイパスできます。

- **OP**（権限レベル2以上）は常に境界チェックの対象外です。
- **タグ付きプレイヤー**もバイパス可能です。バニラの`/tag`コマンドでバイパス用タグ（デフォルト: `pd_free`、設定ファイルの`plotBypassTag`で変更可）を付与してください。

  ```
  /tag <プレイヤー名> add pd_free
  ```

  外す場合は `/tag <プレイヤー名> remove pd_free` です。

  例: `/tag Steve add pd_free` とすると、Steveはどのプロットでも境界を気にせず出入りできるようになります。


## 設定 (config.yml)

```yaml
language: "en"                     # 表示言語: "en" または "ja"（自作言語も可。下記「言語」参照）
world-name: "private_dimension"   # 次元ワールド名
plot-size: 48                      # プロットサイズ
plot-spacing: 128                  # プロット間隔
plot-floor-y: 64                   # 床のY座標（スポーンY = floor-y + 5 = 69、セーフスポーン探索でさらに補正）
plot-height: 47                    # 構造物の高さ（Yサイズ）。境界判定・セーフスポーン探索に使用
structure-file: "plot48x48.nbt"    # 使用する構造物ファイル名。詳細は下記「カスタム構造物」参照
safe-spawn-search-radius: 8        # セーフスポーン探索の水平探索半径（ブロック）
safe-spawn-search-height: 12       # セーフスポーン探索の上下探索範囲（ブロック）
plot-bypass-tag: "pd_free"         # このタグを持つプレイヤーは境界チェックを回避できる
debug-logging: false               # セーフスポーン探索・境界チェックの詳細ログをコンソールに出すか（トラブルシュート用）
pull-entity-limit: 10              # 連行エンティティ最大数
pull-entity-radius: 3.0            # 連行半径（ブロック）
enable-border-enforcement: true    # プロット外に出たら自分のプロットのスポーン地点へ押し戻す
```

`config.yml` の各設定項目には、`language` の値に関わらず常に日本語・英語両方のコメントが付いています（コメント自体はドキュメントであり、表示言語設定の影響は受けません）。

### 言語

チャットメッセージ・コマンド出力・アイテムの表示名/説明文はすべて、`config.yml` の `language` に対応する `plugins/PrivateDimension/lang/<language>.yml` から取得されます。

- `en` と `ja` は同梱されており、初回起動時に自動的に展開されます。
- 自作の言語を追加したい場合: `plugins/PrivateDimension/lang/en.yml` を新しいファイル（例: `fr.yml`）としてコピーし、翻訳した上で `config.yml` の `language` を `"fr"` に設定して `/pd reload`（またはサーバー再起動）してください。
- 該当する言語ファイルが見つからない場合（同梱にも `lang` フォルダ内にも無い場合）は、警告を出して `en` にフォールバックします。
- カスタム言語ファイルに一部キーが無くても、その部分だけ自動的に英語表示にフォールバックするので、翻訳が未完成でも問題なく動作します。
- `plugins/PrivateDimension/structures/README.txt` は `language` 設定に関わらず、常に日本語・英語の両方が書き出されます（初回起動時のみ生成されるファイルなので、再生成したい場合は一度削除してください）。

### カスタム構造物（カスタムNBT）

デフォルトでは同梱の `plot48x48.nbt` が使用されます。自作の構造物に差し替える手順:

1. Structure Block を **SAVE** モードにして構造物を `.nbt` として書き出す。
2. サーバーを一度起動し、`plugins/PrivateDimension/structures/` フォルダを生成させる（使い方を書いた `README.txt` も自動生成されます）。
3. 書き出した `.nbt` ファイルを、`config.yml` の `structure-file` と同じファイル名でそのフォルダに置く（またはファイル名に合わせて `structure-file` を書き換える）。
4. 構造物のサイズ（幅・高さ）が 48×48×47 と異なる場合は `plot-size` / `plot-height` も合わせて変更する。
5. `/pd reload` を実行するか、サーバーを再起動する。

本プラグインは **セーフスポーン探索** を行うため（計算上のスポーン地点から周囲を探索し、頭上が空いた固い地面を探す）、床の高さがデフォルトと異なるカスタム構造物でも、基本的にスポーンYオフセットを手動調整する必要はありません。プロット境界を越えて押し戻される際も同じ探索処理を使って安全な地点に着地します。

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

### 複数バージョンでの動作確認

1.21.5〜26.1.2など、複数のMinecraftバージョンでの互換性を確認する場合:

```bash
bash scripts/setup-test-server.sh 1.21.8   # Paper 1.21.8 をダウンロード/切り替え
bash scripts/reset-test-world.sh           # バージョン切り替え前に古いワールドデータを削除
bash scripts/build-and-copy.sh
bash scripts/build-and-run.sh
```

`setup-test-server.sh` は指定したバージョンが現在ダウンロード済みのものと違う場合、自動的に再ダウンロードします。`reset-test-world.sh` はワールドフォルダだけを削除します（`paper.jar` と `plugins/` は残る）。あるバージョンのワールドデータを別バージョンでそのまま読み込むと問題が起きることがあるためです。

コード上、バージョンによる挙動差が出やすい箇所（各バージョンでテストする際に重点的に確認してほしいポイント）:

* **構造物配置（Paper経路）** — `DimensionManager#placeStructurePaper` は Paper の `StructureManager`/`Structure` API（`StructureRotation`, `Mirror`, `structure.place(...)`）を使用しており、Paper APIのバージョンによってシグネチャが変わることがある。
* **構造物配置（Spigot/手動NBT経路）** — `DimensionManager` の自作NBTリーダーは、バニラの構造物NBTフォーマット（`size`, `palette`, `blocks`）を前提にしている。将来Mojangがこのフォーマットを変更した場合、静かにパースが壊れる可能性がある。
* **リフレクション経由のアイテムメタデータ設定** — `DimensionBottleItem#applyPaperMeta` は、幅広いPaperバージョンに対応するためコンパイル時の直接依存を避け、Adventure API（`Component`, `LegacyComponentSerializer`, `setEnchantmentGlintOverride`）にリフレクションでアクセスしている。メソッドシグネチャが変わった場合はlegacyな`setDisplayName`/`setLore`にフォールバックする作りだが、意図せずフォールバックしていないか確認する価値がある（コンソールに「Adventure API 適用失敗」という警告が出ていないかチェック）。
* **`DamageTypeTags.IS_FIRE`** — アイテムの炎・溶岩耐性に使用。各バージョンで例外なく解決できるか確認。
* **`plugin.yml` の `api-version`** — 現在 `'1.21'`。将来のMinecraftバージョンでBukkit APIに破壊的変更が入った場合は見直しが必要になる可能性がある。

## 開発について

このツールのコードはすべてAI（Claude）に書いてもらいました。
