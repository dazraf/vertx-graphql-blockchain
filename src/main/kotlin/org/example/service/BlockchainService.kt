package org.example.service

import com.fasterxml.jackson.core.type.TypeReference
import io.leangen.graphql.annotations.GraphQLArgument
import io.leangen.graphql.annotations.GraphQLQuery
import io.leangen.graphql.annotations.GraphQLSubscription
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.vertx.core.Vertx
import io.vertx.core.json.jackson.DatabindCodec
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
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

  @GraphQLQuery(name = "ticker", description = "Get Tickers")
  fun ticker(
    @GraphQLArgument(
      name = "symbol",
      description = "ticker symbol"
    ) symbol: String? = null
  ): CompletableFuture<List<Ticker>> {
    return if (symbol != null) {
      client.get("/v3/exchange/tickers/${symbol}")
        .send()
        .map {
          listOf(DatabindCodec.mapper().readValue(it.body().bytes, Ticker::class.java))
        }
    } else {
      client.get("/v3/exchange/tickers")
        .send()
        .map {
          DatabindCodec.mapper().readValue(it.body().bytes, object : TypeReference<List<Ticker>>() {})
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
    return Observable.interval(0, 5, TimeUnit.SECONDS)
      .flatMap {
        log.info("getting symbol /v3/exchange/tickers/${symbol}")
        val future = client.get("/v3/exchange/tickers/${symbol}")
          .send()
          .map {
            if ((it.statusCode() / 100) != 2) {
              val error = "failed to invoke ticker: ${it.statusCode()} ${it.statusMessage()}"
              log.error(error)
              error(error)
            } else {
              try {
                val data = DatabindCodec.mapper().readValue(it.body().bytes, Ticker::class.java)
                log.info("received with ${it.statusCode()} ${it.statusMessage()} - $data")
                data
              } catch (err: Throwable) {
                log.error("failed to parse ${it.body().toString()}")
                throw err
              }
            }
          }
          .toCompletionStage().toCompletableFuture()
        Observable.fromFuture(future)
      }
      .doOnError {
        log.error("failed to get result from blockchain service", it)
      }
      .distinctUntilChanged { it -> it.last_trade_price }
      .toFlowable(BackpressureStrategy.DROP)
  }

}

data class Ticker(
  val symbol: String,
  val price_24h: Double,
  val volume_24h: Double,
  val last_trade_price: Double
)

