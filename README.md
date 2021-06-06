# Vertx-GraphQL-Blockchain

An experiment with

* [Kotlin](https://kotlinlang.org/)
* [Vertx](https://vertx.io/)
* [Vertx GraphQL server](https://vertx.io/docs/vertx-web-graphql/java/) (hosting GraphiQL
  and Apollo websocket)
* [GraphQL Playground](https://github.com/graphql/graphql-playground)
* [GraphQL SPQR](https://github.com/leangen/graphql-spqr)
* [GraphQL Java](https://github.com/graphql-java/graphql-java)
* [Blockchain.com](https://blockchain.com) [API](https://api.blockchain.com/v3/)

## Instructions

* Open the Maven project in your favourite IDE
* Start [App#main](src/main/kotlin/org/example/App.kt)
* Browse
  to [http://localhost:8080/playground/index.html](http://localhost:8080/playground/index.html)
* Execute any of the following:

### Time subscription

```graphql
subscription {
  time {
		value
  }
}
```

### BTCUSD Ticker subscription

```graphql
subscription {
  ticker(symbol: "BTC-USD") {
    symbol
    last_trade_price
    price_24h
    volume_24h
  }
}
```

### BTCUSD Ticker and L2 Order Book sub selection

```graphql 

query {
  ticker(symbol: "BTC-USD") {
		symbol
    last_trade_price
    orderBookL2 {
      asks {
        ... entryFields
      }
      bids {
        ... entryFields
      }
    }
  }
}

fragment entryFields on OrderBookEntry {
  px
  qty
}

```
