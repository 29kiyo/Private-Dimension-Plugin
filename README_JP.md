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

# PrivateDimension

プライベート次元プラグイン for Paper 26.1.2

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

## 必要環境

- **Paper** 26.1.2+
- **Java** 25+
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
plot-floor-y: 64                   # 床のY座標（スポーンY = floor-y + 5 = 69）
pull-entity-limit: 10              # 連行エンティティ最大数
pull-entity-radius: 3.0            # 連行半径（ブロック）
enable-border-enforcement: true    # 境界強制送還
```

