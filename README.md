# vcmc-hubcommand

Velocity プロキシ向けのロビー接続プラグインです。`/hub`、`/lobby`、`/l` コマンドでロビーサーバーに戻れます。

## ダウンロード

[Releases](../../releases) から最新の JAR をダウンロードしてください。

## インストール

1. JAR ファイルを Velocity の `plugins/` フォルダに配置
2. Velocity を再起動
3. `plugins/vcmc-hubcommand/config.yml` を編集してロビーサーバー名を設定

## コマンド

| コマンド | エイリアス | 説明 |
|---------|-----------|------|
| `/hub` | `/lobby`, `/l` | ロビーサーバーに接続 |

## 設定 (config.yml)

```yaml
# velocity.toml のサーバー名と一致させてください
lobby-server: lobby

messages:
  already-in-lobby: "&eYou are already in the lobby!"
  connecting: "&aConnecting to lobby..."

# コマンド使用に必要なパーミッション (空白 = 全員使用可)
permission: ""
```

## 動作環境

- Velocity 3.x
- Java 17+

## ライセンス

[GNU General Public License v3.0](LICENSE)
