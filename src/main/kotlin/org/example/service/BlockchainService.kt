package org.example.service

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.fasterxml.jackson.core.type.TypeReference
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.json.jackson.DatabindCodec
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions

@GraphQLDescription("Blockchain.com Service")
class BlockchainService(private val vertx: Vertx) {

  private var client = WebClient.create(vertx, WebClientOptions().apply {
    isSsl = true
    defaultHost = "api.blockchain.com"
    defaultPort = 443
  })

  @GraphQLDescription("Gets the list of tickers")
  fun tickers(): Future<List<Ticker>> {
    return client.get("/v3/exchange/tickers")
      .send()
      .map {
        DatabindCodec.mapper().readValue(it.body().bytes, object : TypeReference<List<Ticker>>() {})
      }
  }

  @GraphQLDescription("Gets a specific ticker")
  fun ticker(symbol: String): Future<Ticker> {
    return client.get("/v3/exchange/tickers/${symbol}")
      .send()
      .map {
        DatabindCodec.mapper().readValue(it.body().bytes, Ticker::class.java)
      }
  }
}

data class Ticker(
  @GraphQLDescription("example: BTC-USD")
  val symbol: String,
  @GraphQLDescription("example: 4998.0")
  val price_24h: Double,
  @GraphQLDescription("example: 0.3015")
  val volume_24h: Double,
  @GraphQLDescription("example: 5000.0")
  val last_trade_price: Double
)