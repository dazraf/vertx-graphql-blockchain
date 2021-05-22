package org.example.graphql

import com.expediagroup.graphql.generator.execution.FunctionDataFetcher
import com.expediagroup.graphql.generator.execution.SimpleKotlinDataFetcherFactoryProvider
import com.expediagroup.graphql.generator.hooks.SchemaGeneratorHooks
import com.fasterxml.jackson.databind.ObjectMapper
import graphql.schema.DataFetcherFactory
import graphql.schema.DataFetchingEnvironment
import io.vertx.core.Future
import kotlin.reflect.KFunction
import kotlin.reflect.KType

class VertxFutureHook : SchemaGeneratorHooks {

  override fun willResolveMonad(type: KType): KType = when (type.classifier) {
    Future::class -> type.arguments.firstOrNull()?.type
    else -> type
  } ?: type
}

class FutureFunctionDataFetcher(target: Any?, fn: KFunction<*>, objectMapper: ObjectMapper) :
  FunctionDataFetcher(target, fn, objectMapper) {

  override fun get(environment: DataFetchingEnvironment): Any? =
    when (val result = super.get(environment)) {
      is Future<*> -> result.toCompletionStage()
      else -> result
    }
}

class FutureDataFetcherFactoryProvider(
  private val objectMapper: ObjectMapper
) : SimpleKotlinDataFetcherFactoryProvider(objectMapper) {

  override fun functionDataFetcherFactory(target: Any?, kFunction: KFunction<*>): DataFetcherFactory<Any?> =
    DataFetcherFactory {
      FutureFunctionDataFetcher(
        target = target,
        fn = kFunction,
        objectMapper = objectMapper
      )
    }
}