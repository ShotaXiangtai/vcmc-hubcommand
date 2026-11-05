# vcmc-hubcommand

Velocity プロキシ向けのロビー接続プラグインです。`/hub`、`/lobby`、`/l` コマンドでロビーサーバーに戻れます。

## ダウンロード

[Releases](../../releases) から最新の JAR をダウンロードしてください。

## インストール

1. JAR ファイルを Velocity の `plugins/` フォルダに配置
2. Velocity を再起動
3. `plugins/vcmc-hubcommand/config.yml` を編集してロビーサーバー名を設定

## コマンド

| コマンド | エイリアス | 説明 | パーミッション |
|---------|-----------|------|--------------|
| `/hub` | `/lobby`, `/l` | ロビーサーバーに接続 | `permission` の設定値 (既定は全員) |
| `/hubblock add <サーバー>` | | ブラックリストに追加 | `admin-permission` の設定値 |
| `/hubblock remove <サーバー>` | | ブラックリストから削除 | 同上 |
| `/hubblock list` | | ブラックリストを表示 | 同上 |

`/hubblock` はコンソールからも実行できます。変更は `config.yml` の `blocked-servers` だけを書き換えて保存するため、ほかの設定やコメントはそのまま残ります。

## ブラックリスト

`blocked-servers` に登録したサーバーにいる間、`/hub`、`/lobby`、`/l` はプロキシで処理されず、そのままバックエンドへ転送されます。バックエンド側の同名コマンド (ログインプラグインの `/l` など) を使いたいサーバーを登録してください。バックエンドに同名のコマンドがなければ、不明なコマンドとして扱われます。

## 設定 (config.yml)

```yaml
# velocity.toml のサーバー名と一致させてください
lobby-server: lobby

messages:
  already-in-lobby: "&eYou are already in the lobby!"
  connecting: "&aConnecting to lobby..."

# コマンド使用に必要なパーミッション (空白 = 全員使用可)
permission: ""

# /hubblock に必要なパーミッション
admin-permission: "vcmc.hubcommand.admin"

# /hub, /lobby, /l をプロキシで処理しないサーバー一覧
blocked-servers: []
```

## 動作環境

- Velocity 3.x / 4.x (3.5.1 と 4.3.1 で起動を確認)
- Java 17 以上 (Velocity 本体の要件に従ってください)

## ライセンス

パブリックドメイン ([The Unlicense](LICENSE))
