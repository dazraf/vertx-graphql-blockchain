package org.example

import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.netty.handler.codec.http.HttpHeaderValues
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.json.jackson.DatabindCodec
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.CorsHandler
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.ext.web.handler.graphql.*
import org.example.graphql.GraphQLConfigure
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicInteger

class App(private val port: Int = 8080) : AbstractVerticle() {
  companion object {

    private val log = LoggerFactory.getLogger(App::class.java)

    @JvmStatic
    fun main(args: Array<String>) {
      setupLogger()
      setupJacksonForKotlin()
      Vertx.vertx().deployVerticle(App())
    }

    private fun setupJacksonForKotlin() {
      DatabindCodec.mapper().registerKotlinModule()
      DatabindCodec.prettyMapper().registerKotlinModule()
    }

    private fun setupLogger() {
      System.getProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory")
    }
  }

  override fun start(startPromise: Promise<Void>) {
    log.info("configuring application")
    val router = Router.router(vertx)
    val graphQL = GraphQLConfigure.configure(vertx)

    // basic middleware
    val nextRequestId = AtomicInteger(0)
    router.route().handler { routingContext ->
      val requestId = nextRequestId.incrementAndGet()
      val request = routingContext.request()
      val start = System.currentTimeMillis()
      routingContext.addBodyEndHandler {
        if ((request.response().statusCode / 100) != 2) {
          val sb = StringBuilder()
            .appendLine("Request $requestId: ${request.method()} ${request.uri()} ${request.response().statusCode} ${request.response().statusMessage} - ${System.currentTimeMillis() - start} millis")
          val body = if (routingContext.request()
              .getHeader(io.vertx.core.http.HttpHeaders.CONTENT_TYPE) == HttpHeaderValues.APPLICATION_JSON.toString()
          ) {
            routingContext.bodyAsJson.encodePrettily()
          } else {
            routingContext.bodyAsString
          }
          if (body != null) {
            sb.appendLine("Body: $body")
          }
          log.info(sb.toString())
        }
      }

      routingContext.next()
    }
    router.route().handler(BodyHandler.create());

    // apollo ws handler
    router.route("/").handler(ApolloWSHandler.create(graphQL, ApolloWSOptions()))

    // main graphql http server
    router.route("/").handler(
      GraphQLHandler.create(graphQL,
        GraphQLHandlerOptions()
          .setRequestMultipartEnabled(true)
          .setRequestBatchingEnabled(true)
      )
    )

    router.route("/playground/*").handler(StaticHandler.create("playground"))
//    // graphiql ui
//    router.route("/graphiql/*").handler(
//      GraphiQLHandler.create(
//        GraphiQLHandlerOptions()
//          .setGraphQLUri("/")
//          .setEnabled(true)
//      )
//    )

    // ws sub protocol for apollo ws
    val httpServerOptions = HttpServerOptions()
      .addWebSocketSubProtocol("graphql-ws")

    // the server
    vertx
      .createHttpServer(httpServerOptions)
      .requestHandler(router)
      .exceptionHandler {
        log.error("webserver error", it)
      }
      .listen(port)
      .onSuccess {
        log.info("application configured")
        println("graphql:  http://localhost:$port")
        println("graphql-ws:  ws://localhost:$port")
        println("graphiql: http://localhost:$port/playground/")
        startPromise.complete()
      }
  }
}


