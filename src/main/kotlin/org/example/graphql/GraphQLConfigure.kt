package org.example.graphql

import com.expediagroup.graphql.generator.SchemaGeneratorConfig
import com.expediagroup.graphql.generator.TopLevelObject
import com.expediagroup.graphql.generator.toSchema
import graphql.GraphQL
import io.vertx.core.Vertx
import io.vertx.core.json.jackson.DatabindCodec
import org.example.service.BlockchainService

object GraphQLConfigure {

  fun configure(vertx: Vertx): GraphQL {
    val mapper = DatabindCodec.mapper()

    val config = SchemaGeneratorConfig(
      supportedPackages = listOf(BlockchainService::class.java.`package`.name),
      hooks = VertxFutureHook(),
      dataFetcherFactoryProvider = FutureDataFetcherFactoryProvider(mapper)
    )
    val queries = listOf(TopLevelObject(BlockchainService(vertx)))
    val schema = toSchema(config, queries, emptyList())
    return GraphQL.newGraphQL(schema).build()
  }
}