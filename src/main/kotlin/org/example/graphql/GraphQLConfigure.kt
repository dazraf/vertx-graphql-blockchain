package org.example.graphql

import graphql.GraphQL
import io.leangen.graphql.GraphQLSchemaGenerator
import io.vertx.core.Vertx
import org.example.service.BlockchainService
import org.example.service.TimeService

object GraphQLConfigure {

  fun configure(vertx: Vertx): GraphQL {
    val timeService = TimeService(vertx)
    val blockchainService = BlockchainService(vertx)
    val schema = GraphQLSchemaGenerator()
      .withBasePackages("org.example")
      .withOperationsFromSingleton(timeService)
      .withOperationsFromSingleton(blockchainService)
      .generate()
    return GraphQL.newGraphQL(schema).build()
  }
}