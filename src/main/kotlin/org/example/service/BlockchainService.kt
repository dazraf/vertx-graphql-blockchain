package org.example.service

import com.fasterxml.jackson.core.type.TypeReference
import io.leangen.graphql.annotations.GraphQLArgument
import io.leangen.graphql.annotations.GraphQLContext
import io.leangen.graphql.annotations.GraphQLQuery
import io.leangen.graphql.annotations.GraphQLSubscription
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.vertx.core.Vertx
import io.vertx.core.json.jackson.DatabindCodec
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
import org.example.model.OrderBook
import org.example.model.Ticker
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class BlockchainService(private val vertx: Vertx) {
  companion object {

    private val log = LoggerFactory.getLogger(BlockchainService::class.java)
  }

  private var client = WebClient.create(vertx, WebClientOptions().apply {
    isSsl = true
    defaultHost = "api.blockchain.com"
    defaultPort = 443
  })

  @GraphQLQuery(name = "ticker", description = "Get Ticker")
  fun ticker(
    @GraphQLArgument(
      name = "symbol",
      description = "ticker symbol"
    ) symbol: String
  ): CompletableFuture<Ticker> {
    return client.get("/v3/exchange/tickers/$symbol")
      .send()
      .map {
        if ((it.statusCode() / 100) != 2) {
          error("failed to call https://api.blockchain.com/v3/exchange/tickers/$symbol - ${it.statusCode()} ${it.statusMessage()}")
        }
        try {
          DatabindCodec.mapper().readValue(it.body().bytes, Ticker::class.java)
        } catch (err: Throwable) {
          throw Exception("failed to parse response ${it.body()}: ${err.message}", err)
        }
      }
      .toCompletionStage().toCompletableFuture()
  }

  @GraphQLQuery(name = "tickers", description = "Get Tickers")
  fun tickers(): CompletableFuture<List<Ticker>> {
    return client.get("/v3/exchange/tickers")
      .send()
      .map { response ->
        if ((response.statusCode() / 100) != 2) {
          error("failed to call https://api.blockchain.com/v3/exchange/tickers - ${response.statusCode()} ${response.statusMessage()}")
        }
        try {
          DatabindCodec.mapper().readValue(response.body().bytes, object : TypeReference<List<Ticker>>() {})
            .filter { it.last_trade_price != 0.0 }
        } catch (err: Throwable) {
          throw Exception("failed to parse response ${response.body()}: ${err.message}", err)
        }
      }.toCompletionStage().toCompletableFuture()
  }

  @GraphQLSubscription(name = "ticker", description = "ticker subscription")
  fun tickerSubscription(
    @GraphQLArgument(
      name = "symbol",
      description = "ticker symbol"
    ) symbol: String
  ): Publisher<Ticker> {
    return Observable.interval(0, 1, TimeUnit.SECONDS)
      .flatMap {
        Observable.fromFuture(ticker(symbol))
      }
      .doOnError {
        log.error("failed to get result from blockchain service", it)
      }
      .distinctUntilChanged { it -> it.last_trade_price }
      .toFlowable(BackpressureStrategy.DROP)
  }

  @GraphQLQuery(name = "orderBookL2", description = "get L2 order book for symbol")
  fun orderBookL2(@GraphQLArgument(name = "symbol") symbol: String): CompletableFuture<OrderBook> {
    return client.get("/v3/exchange/l2/$symbol")
      .send()
      .map {
        if ((it.statusCode() / 100) != 2) {
          error("failed to call https://api.blockchain.com/v3/exchange/tickers/$symbol - ${it.statusCode()} ${it.statusMessage()}")
        }
        try {
          DatabindCodec.mapper().readValue(it.body().bytes, OrderBook::class.java)
        } catch (err: Throwable) {
          throw Exception("failed to parse response ${it.body()}: ${err.message}", err)
        }
      }
      .toCompletionStage().toCompletableFuture()

  }

  @GraphQLQuery(name = "orderBookL2", description = "get L2 order book for symbol")
  fun orderBookL2(@GraphQLContext ticker: Ticker): CompletableFuture<OrderBook> {
    return orderBookL2(ticker.symbol)
  }

}

