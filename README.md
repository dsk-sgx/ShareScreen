# ShareScreen
ブラウザから簡易的な画面共有を実現するWebアプリケーションです。

## Description
画面共有したいPCでShareScreenを起動し、クライアントはWebブラウザでアクセスします。  
サーバは規定値毎にスクリーンショットを取得し、WebSocketを通じてバイナリデータを送信します。
以下の様なシーンの使用を想定しています。 
* プロジェクターを使う程ではない打ち合わせで画面を共有したい
* skypeなどのツールは使うことが出来ない etc.

## Install
    git clone https://github.com/sugiyamads/ShareScreen.git

## Usage
### Server  
    ./gradlew bootrun
    ./gradlew bootrun -Dserver.port=8090 -Dscreenshot.interval=300

#### Options
    server.port=サーバポート　デフォルト：18080
    screenshot.interval=スクリーンショットを取得する間隔　デフォルト:500（ミリ秒）
    
### Client  
    http://host:18080/

