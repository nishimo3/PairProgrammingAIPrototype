# README #

ペアプログラミングAIのクライアントアプリ(プロトタイプ)

### What is Pair Programming AI ###
* まるで開発者の隣にいる優秀な開発者がいるような形で，実装中のコードに対して，様々なアドバイスを行ってくれるAIのこと

### What can Pair Programming AI do ? ###

* 指定したプロジェクトのソースコードに対して様々なアドバイスを通知する
* 長時間，作業している場合に休憩を促す
* 技術の最新ニュースを通知する

### Details ###

* 通知内容(○：対応済，動作確認済，△：対応済，×：未対応)

|対象|内容|対応|
|:--|:--|:--:|
|プログラマ|疲労チェック|○|
|プログラマ|技術の最新ニュースの通知|○|
|ソースコード|コンパイルチェック|×|
|ソースコード|スペルチェック|×|
|ソースコード|静的解析|○|
|ソースコード|参考になりそうなサイトの通知|○|

### How do I get set up? ###

* 動作確認済みのOS
  * macOS Sierra(ver10.12.5)
* 必要なソフトウェア
  * Java 1.8.0以降
  * Apache Maven 3.3.9
  * Node.js v7.10.0
  * Swift version 3.1(swiftlang-802.0.53 clang-802.0.42)
* インストール手順
　* 必要なソフトウェアがインストールされていること
　* ビルド(Maven)
　　* $ mvn clean
　　* $ mvn install
　* ビルドで生成されたファイル(ClientUI-0.0.1.zip)を解凍してインストールする
　　* $ unzip ClientUI-0.0.1.zip
　　* $ cd ClientUI-0.0.1/bin
　　* $ chmod 755 install.sh
　　* $ ./install.sh
　* コンフィグファイル(configuration/resource.properties)をカスタマイズする，基本的には以下のプロパティについて書き換える
　　* nodeCommandPath：nodeの実行ファイルの場所
　　* findBugsDir：FindBugsの実行ファイルの場所
　　* docomoTalkerApiKey：Docomo雑談対話APIのApiKeyの指定(雑談対話 https://dev.smt.docomo.ne.jp/?p=docs.api.page&api_name=dialogue&p_name=api_reference)
* 起動手順
　　* ./bin/PairProgrammingAI

### Future works ###
* プログラマの行動データを蓄積・解析する
  * デザイン
  * コーディング
  * テスト
  * デバッグ
  * コミット
  * リファクタリング
* 以下のような様々なツール(git, jenkins, etc ...)との連携(プラグイン・アーキテクチャ)

|種別|内容|対応|
|:--|:--|:--|
|Editor|エディタ|Eclipse, Intellij|
|VersionManagement|バージョン管理|Git, Subversion|
|CodeAnalysis|コード解析|FindBugs, Understand|
|CI|継続的インテグレーション|Jenkins|
|Search|検索|google|

* 対話的に，問題解決を図る
