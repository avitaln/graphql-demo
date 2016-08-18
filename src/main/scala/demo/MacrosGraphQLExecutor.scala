package demo

import java.util.concurrent.ExecutorService

import sangria.ast.Document
import sangria.execution.Executor
import sangria.macros.derive.{GraphQLField, GraphQLName, _}
import sangria.parser.QueryParser
import sangria.schema._
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Success
import DemoImplicits._

object DemoImplicits {
  implicit val GraphProductType = deriveObjectType[Unit, GraphProduct]()
  implicit val GraphStoreType = deriveObjectType[Unit, GraphStore]()
  implicit val GraphMetaSiteType = deriveObjectType[Unit, GraphMetaSite]()
  implicit val GraphAccountType = deriveObjectType[Unit, GraphAccount]()
}

@GraphQLName("Queries")
class DemoQueries {
  @GraphQLField def products(ctx: Context[DemoContext, Unit])(
    minPrice: Option[BigDecimal],
    limit:Option[Int],
    sortedByPrice: Option[Boolean]
    ): Seq[GraphProduct] =
    InMemoryDatabase.products(minPrice, limit, sortedByPrice)

  @GraphQLField def product(ctx: Context[DemoContext, Unit])(id: String): Option[GraphProduct] =
    InMemoryDatabase.product(id)

  @GraphQLField def account(ctx: Context[DemoContext, Unit])(): GraphAccount =
    InMemoryDatabase.account(ctx.ctx.userId)
}

class DemoContext(val userId: String) {
  val queries: DemoQueries = new DemoQueries
}

object MacrosDemoSchema {
  val DemoQueryType = deriveContextObjectType[DemoContext, DemoQueries, Unit](_.queries)
  val demoSchema = Schema(DemoQueryType)
}


class MacrosGraphQLExecutor(executorService: ExecutorService) {
  implicit val executionContext = ExecutionContext.fromExecutorService(executorService)
  def execute[CTX](request: GraphQLRequest): Map[String, Any] = {
    val Success(document: Document) = QueryParser.parse(request.query)
    val ctx = new DemoContext("foo")
    Executor.execute(
      MacrosDemoSchema.demoSchema,
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
