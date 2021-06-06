package org.example.model

data class OrderBook(
  val symbol: String = "",
  val bids: List<OrderBookEntry> = emptyList(),
  val asks: List<OrderBookEntry> = emptyList()
)