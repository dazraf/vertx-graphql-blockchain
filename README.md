# Vertx-GraphQL-Blockchain

An experiment with 

* Kotlin
* Vertx
* Vertx GraphQL server
* Expedia GraphQL for Kotlin modules
* GraphQL Java
* [Blockchain.com](https://blockchain.com) API

## Instructions

* Open the project in your favourite IDE
* Start [App#main](src/main/kotlin/org/example/App.kt)
* Browser to [http://localhost:8080/graphiql/]
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