# Vertx-GraphQL-Blockchain

An experiment with 

* [Kotlin](https://kotlinlang.org/)
* [Vertx](https://vertx.io/)
* [Vertx GraphQL server](https://vertx.io/docs/vertx-web-graphql/java/) (hosting GraphiQL and Apollo websocket)
* [Expedia GraphQL for Kotlin](https://github.com/ExpediaGroup/graphql-kotlin)
* [GraphQL Java](https://github.com/graphql-java/graphql-java)
* [Blockchain.com](https://blockchain.com) [API](https://api.blockchain.com/v3/)

## Instructions

* Open the project in your favourite IDE
* Start [App#main](src/main/kotlin/org/example/App.kt)
* Browse to [http://localhost:8080/graphiql/](http://localhost:8080/graphiql/])
* Execute any of the following queries:

```graphql
query q1 {
  tickers {
    symbol
    last_trade_price
    price_24h
    volume_24h
  }
}
```

or 

```graphql
query q1 {
  ticker(symbol: "BTC-USD") {
    symbol
    last_trade_price
    price_24h
    volume_24h
  }
}
```