package demo

import java.util.concurrent.ExecutorService

import sangria.ast.Document
import sangria.execution.Executor
import sangria.parser.QueryParser

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Success
import scala.concurrent.duration._
import sangria.schema.{ObjectType, _}


class QueryContext {
  def latestVersion = 1000l

  def products(productId: Option[String]): Seq[Product] =
    if (productId.isDefined) {
      Seq(Product(productId.get))
    } else {
      Seq(Product("p1"), Product("p2"))
    }


  def pageUrl(id: String): String  = {
    s"http://mysite.com/product-page/$id"
  }

  def orders: Seq[Order] = Seq(
    Order("id1", "pending"),
    Order("id2", "pending")
  )

  def orderItems(id: String): Seq[OrderItem] = {
    Seq(OrderItem("p1"), OrderItem("p2"), OrderItem("p3"), OrderItem("p4"))
  }
}

case class Product(id: String)
case class OrderItem(productId: String)
case class Order(id: String, status: String, orderItems: Seq[OrderItem] = Nil)

object DemoSchema {

  private val OrderItemType: ObjectType[QueryContext, OrderItem] = ObjectType(
    "OrderItem",
    List[Field[QueryContext, OrderItem]](
      Field("id", fieldType = StringType, resolve = _.value.productId)
    )
  )

  private val OrderType: ObjectType[QueryContext, Order] = ObjectType(
    "Order",
    List[Field[QueryContext, Order]](
      Field("id", fieldType = StringType, resolve = _.value.id),
      Field("status", fieldType = StringType, resolve = _.value.status),
      Field("orderItems", fieldType = ListType(OrderItemType), resolve = _.value.orderItems)
//      Field("orderItems", fieldType = ListType(OrderItemType), resolve = ctx ⇒ ctx.ctx.orderItems(ctx.value.id))
    )
  )
  private val ProductType: ObjectType[QueryContext, Product] = ObjectType(
    "Product",
    List[Field[QueryContext, Product]](
      Field("id", fieldType = StringType, resolve = _.value.id),
      Field("productPageUrl", fieldType = StringType,
        resolve = ctx ⇒ ctx.ctx.pageUrl(ctx.value.id))
    )
  )

  private val ordersQuery: Field[QueryContext, Unit] = Field(
    "orders",
    fieldType = ListType(OrderType),
    resolve = ctx => ctx.ctx.orders
  )

  private val productsQuery: Field[QueryContext, Unit] = Field(
    "products",
    arguments =
      Argument("productId", OptionInputType(StringType)) ::
      Argument("minPrice", OptionInputType(BigDecimalType)) ::
      Argument("maxPrice", OptionInputType(BigDecimalType)) ::
        Nil,
    fieldType = ListType(ProductType),
    resolve = ctx => ctx.ctx.products(ctx.args.argOpt("productId"))
  )

  private val latestCatalogVersionQuery: Field[QueryContext, Unit] = Field(
    "latestCatalogVersion",
    fieldType = ListType(LongType),
    resolve = ctx => ctx.ctx.latestVersion
  )

  val WixStoresQueriesType = ObjectType("WixStoresQueries",
    fields[QueryContext, Unit](
      ordersQuery,
      productsQuery,
      latestCatalogVersionQuery
    )
  )

  val demoSchema = Schema(WixStoresQueriesType)
}

class RegularGraphQLExecutor(executorService: ExecutorService) {
  implicit val executionContext = ExecutionContext.fromExecutorService(executorService)
  def execute[CTX](request: GraphQLRequest): Map[String, Any] = {
    val Success(document: Document) = QueryParser.parse(request.query)
    val ctx = new QueryContext
    Executor.execute(
      DemoSchema.demoSchema,
      queryAst = document,
      userContext = ctx
    ).map { _.toGenericMap }.await

  }

  implicit class MapConvertor(o: Any) {
    def toGenericMap = o.asInstanceOf[Map[String,Any]]
  }

  implicit class WithAwait[T](f: Future[T]) {
    def await = Await.result(f, 10.seconds)
  }

}
