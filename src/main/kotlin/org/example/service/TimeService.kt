package org.example.service

import io.leangen.graphql.annotations.GraphQLQuery
import io.leangen.graphql.annotations.GraphQLSubscription
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.vertx.core.Handler
import io.vertx.core.Vertx
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import java.time.Instant

class TimeService(private val vertx: Vertx) {
  companion object {
    private val log = LoggerFactory.getLogger(TimeService::class.java)
  }

  @GraphQLQuery(name = "time")
  fun currentTime(): Time = Time()

  @GraphQLSubscription(name = "time", description = "time source")
  fun timeStream(): Publisher<Time> {
    return Observable.create<Time> { emitter ->
      while(true ){
        emitter.onNext(Time())
        Thread.sleep(1000)
      }
    }.toFlowable(BackpressureStrategy.DROP)
  }
}

data class Time(val value: Instant = Instant.now())