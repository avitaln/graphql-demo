package demo

import sangria.macros.derive.{GraphQLField, GraphQLName}

@GraphQLName("Product")
trait GraphProduct {
  @GraphQLField def id: String
  @GraphQLField def name: String
  @GraphQLField def price: BigDecimal
  @GraphQLField def pictures: Seq[String]
}

@GraphQLName("Store")
trait GraphStore {
  @GraphQLField def id: String
  @GraphQLField def products(minPrice: Option[BigDecimal], limit: Option[Int], sortedByPrice: Option[Boolean]): Seq[GraphProduct]
  //  @GraphQLField def originStore: Option[GraphStore]
}

@GraphQLName("MetaSite")
trait GraphMetaSite {
  @GraphQLField def id: String
  @GraphQLField def name: String
  @GraphQLField def hasStore: Boolean
  @GraphQLField def store: Option[GraphStore]
  @GraphQLField def isPremium: Boolean
}

@GraphQLName("UserAccount")
trait GraphAccount {
  @GraphQLField def id: String
  @GraphQLField def email: String
  @GraphQLField def metaSites(onlyStores: Option[Boolean], onlyPremium: Option[Boolean]): Seq[GraphMetaSite]
}
