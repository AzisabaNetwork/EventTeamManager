# EventTeamManager

## インストール
先にConfigを手動で生成しておくと楽です
`./plugins/EventTeamManager/config.yml` に以下の内容を記入します
```yml
# データベースへの接続情報
mysql:
  host: localhost
  port: 3306
  user: user
  password: password
  database: eventteammanager

# キルをカウントしてポイントを付与するワールド名一覧 
kill-count-worlds:
  - "maps"

# チームが勝利した時に与えるポイント
victory-point: 80
```

## コマンド (一般プレイヤー)
| コマンド | 説明 | 権限 |
|:---|:---|:---|
| /eventshop | ポイントでアイテムを購入するためのショップを開く | `eventteammanager.command.eventshop` |
| /event join <チーム> | チームに参加する | `eventteammanager.command.event` |
| /event status | イベント状況を参照する | `eventteammanager.command.event` |
| /event shop | /eventshop と同じ | `eventteammanager.command.event` |

## コマンド (運営)
| コマンド | 説明 | 権限 |
|:---|:---|:---|
| /eventadmin team create <チーム名> | チームを作成する | `eventteammanager.command.eventadmin` |
| /eventadmin team join <チーム名> <プレイヤー...> | プレイヤーをチームに参加させる (オンラインであることが必要) | 上に同じ |
| /eventadmin team option <チーム名> setcolor <色> | チームの色を設定する (ChatColor) | 上に同じ |
| /eventadmin shop add <ポイント価格> | 手に持っているアイテムをショップに追加する | 上に同じ |
| /eventadmin shop layout | ショップのレイアウトを変更する | 上に同じ |
| /eventadmin hologram set | 今いる位置にイベントホログラムを設定する | 上に同じ |
| /eventadmin hologram delete | イベントホログラムを削除する | 上に同じ |
| /eventadmin top | イベント貢献度ランキングを表示する | 上に同じ |
| /eventadmin status | イベント状況を参照する | 上に同じ |
| /eventadmin reset | イベントの全データを削除する | 上に同じ |

## 仕様
プレイヤーがキルしたときに貢献度ポイントが1たまります。また、設定で有効になっている場合はチームが勝利した時も貢献度ポイントが入ります。

プレイヤーは /eventshop を使って開けるショップにて、貢献度を使ってアイテムを購入することができます。この時、獲得した貢献度ポイントは減少しないため、ランキングに影響はありません。`獲得貢献度ポイント >= 購入後の合計消費ポイント`である限り購入が行えます。

## 将来的に実装した方が良い機能
* イベントロック機能 - イベントの開始前と終了後に、イベントデータを保持しつつデータが変動しないようにする機能
* ショップロック機能 - ショップの編集中に購入ができないようにロックする機能
* チーム参照機能 - プレイヤーがどのチームに居るか確認する機能
* チーム離脱機能 - 運営がプレイヤーをチームから離脱させる機能

## License
[GNU General Public License v3.0](./LICENSE)
