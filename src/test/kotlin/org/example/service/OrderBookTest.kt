package org.example.service

import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.vertx.core.json.Json
import io.vertx.core.json.jackson.DatabindCodec
import org.example.model.OrderBook
import org.example.model.OrderBookEntry
import org.junit.Test
import kotlin.test.assertEquals

class OrderBookTest {

  init {
    DatabindCodec.mapper().registerKotlinModule()
    DatabindCodec.prettyMapper().registerKotlinModule()
  }

  @Test
  fun `that we can serialise and deserialise a bid`() {
    val bid = OrderBookEntry(px = 1.0, qty = 2.0, num = 1)
    val json = Json.encodePrettily(bid)
    val decoded = Json.decodeValue(json, OrderBookEntry::class.java)
  }

  @Test
  fun `that we can deserialise order book`() {
    val json = """
      {
        "symbol": "BTC-USD",
        "bids": [
          {
            "px": 36327,
            "qty": 0.05,
            "num": 1
          },
          {
            "px": 36325.92,
            "qty": 1.37642757,
            "num": 1
          }
        ]
      }
    """.trimIndent()
    val decoded = Json.decodeValue(json, OrderBook::class.java)
    val expected = OrderBook(
      "BTC-USD", listOf(
        OrderBookEntry(
          px = 36327.0,
          qty = 0.05,
          num = 1
        ),
        OrderBookEntry(
          px = 36325.92,
          qty = 1.37642757,
          num = 1
        )
      )
    )
    assertEquals(expected, decoded)
  }
}