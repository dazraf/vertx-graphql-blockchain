package org.example.service

import com.fasterxml.jackson.core.type.TypeReference
import io.leangen.graphql.annotations.GraphQLId
import io.leangen.graphql.annotations.GraphQLQuery
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.json.jackson.DatabindCodec
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
import java.util.concurrent.CompletableFuture

class BlockchainService(private val vertx: Vertx) {

  private var client = WebClient.create(vertx, WebClientOptions().apply {
    isSsl = true
    defaultHost = "api.blockchain.com"
    defaultPort = 443
  })

  @GraphQLQuery(name = "tickers", description = "Get Tickers")
  fun tickers(@GraphQLId symbol: String? = null): CompletableFuture<List<Ticker>> {
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

}

data class Ticker(
  val symbol: String,
  val price_24h: Double,
  val volume_24h: Double,
  val last_trade_price: Double
)