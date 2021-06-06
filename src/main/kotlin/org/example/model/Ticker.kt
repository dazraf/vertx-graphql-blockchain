package org.example.model

data class Ticker(
  val symbol: String,
  val price_24h: Double,
  val volume_24h: Double,
  val last_trade_price: Double
)