package org.example.service

import io.leangen.graphql.annotations.GraphQLQuery
import io.leangen.graphql.annotations.GraphQLSubscription
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.vertx.core.Vertx
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.concurrent.TimeUnit

class TimeService(private val vertx: Vertx) {
  companion object {

    private val log = LoggerFactory.getLogger(TimeService::class.java)
  }

  @GraphQLQuery(name = "time")
  fun currentTime(): Time {
    val time = Time()
    log.info("returning time $time")
    return time
  }

  @GraphQLSubscription(name = "time", description = "time subscription")
  fun timeStream(): Publisher<Time> {
    return Observable.interval(0, 1, TimeUnit.SECONDS)
      .map { Time() }
      .observeOn(Schedulers.computation())
      .subscribeOn(Schedulers.computation())
      .toFlowable(BackpressureStrategy.LATEST)
  }

}

data class Time(val value: Instant = Instant.now())