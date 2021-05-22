package org.example

import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.json.jackson.DatabindCodec
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.graphql.*
import org.example.graphql.GraphQLConfigure
import org.slf4j.LoggerFactory

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
    router.route().handler(BodyHandler.create());

    // apollo ws handler
    router.route("/graphql").handler(ApolloWSHandler.create(graphQL))

    // main graphql http server
    router.route("/graphql").handler(
      GraphQLHandler.create(
        graphQL,
        GraphQLHandlerOptions()
          .setRequestMultipartEnabled(true)
          .setRequestBatchingEnabled(true)
      )
    )

    // graphiql ui
    router.route("/graphiql/*").handler(
      GraphiQLHandler.create(
        GraphiQLHandlerOptions()
          .setGraphQLUri("/graphql")
          .setEnabled(true)
      )
    )

    // ws sub protocol for apollo ws
    val httpServerOptions = HttpServerOptions()
      .addWebSocketSubProtocol("graphql-ws")

    // the server
    vertx
      .createHttpServer(httpServerOptions)
      .requestHandler(router)
      .listen(port)
      .onSuccess {
        log.info("application configured")
        println("graphql:  http://localhost:$port/graphql")
        println("graphiql: http://localhost:$port/graphiql/")
        startPromise.complete()
      }
  }
}


