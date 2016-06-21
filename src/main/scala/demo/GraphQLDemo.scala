package demo

import java.util.concurrent.ExecutorService

import sangria.ast.Document
import sangria.execution.Executor
import sangria.macros.derive.{GraphQLField, GraphQLName, _}
import sangria.parser.QueryParser
import sangria.schema.{Context, Schema}
import sangria.macros._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Success
import scala.concurrent.duration._
import DemoImplicits._

@GraphQLName("Product")
trait GraphProduct {
  @GraphQLField def id: String
  @GraphQLField def name: String
  @GraphQLField def price: BigDecimal
  @GraphQLField def pictures: Seq[String]
}

@GraphQLName("Category")
trait GraphCategory {
  @GraphQLField def id: String
  @GraphQLField def name: String
  @GraphQLField def products: Seq[GraphProduct]
}

@GraphQLName("Store")
trait GraphStore {
  @GraphQLField def id: String
  @GraphQLField def categories: Seq[GraphCategory]
  @GraphQLField def products(minPrice: Option[BigDecimal], limit: Option[Int]): Seq[GraphProduct]
  //  @GraphQLField def originStore: Option[GraphStore]
}

@GraphQLName("MetaSite")
trait GraphMetaSite {
  @GraphQLField def id: String
  @GraphQLField def name: String
  @GraphQLField def store: Option[GraphStore]
  @GraphQLField def published: Boolean
  @GraphQLField def premium: Boolean
}

@GraphQLName("UserAccount")
trait GraphAccount {
  @GraphQLField def id: String
  @GraphQLField def email: String
  @GraphQLField def metaSites(onlyPublished: Option[Boolean], onlyPremium: Option[Boolean]): Seq[GraphMetaSite]
}


object DemoImplicits {
  implicit val GraphProductType = deriveObjectType[Unit, GraphProduct]()
  implicit val GraphCategoryType = deriveObjectType[Unit, GraphCategory]()
  implicit val GraphStoreType = deriveObjectType[Unit, GraphStore]()
  implicit val GraphMetaSiteType = deriveObjectType[Unit, GraphMetaSite]()
  implicit val GraphAccountType = deriveObjectType[Unit, GraphAccount]()
}

@GraphQLName("Queries")
class DemoQueries {
  @GraphQLField def products(ctx: Context[DemoContext, Unit])(): Seq[GraphProduct] = DemoService.products
  @GraphQLField def account(ctx: Context[DemoContext, Unit])(): GraphAccount = DemoService.account(ctx.ctx.userId)
}

class DemoContext(val userId: String) {
  val queries: DemoQueries = new DemoQueries
}

object DemoSchema {
  val DemoQueryType = deriveContextObjectType[DemoContext, DemoQueries, Unit](_.queries)
  val demoSchema = Schema(DemoQueryType)
}

object DemoService {

  def prod(_id:String, _name: String, _price: BigDecimal, _pictures: Seq[String]) = new GraphProduct {
    def id: String = _id
    def name: String = _name
    def price: BigDecimal = _price
    def pictures: Seq[String] = _pictures
  }

  val p11 = prod("p11","name11", 88,Seq("a.jpg","b.jpg","c.jpg"))
  val p12 = prod("p12","name12", 42,Seq("a.jpg","b.jpg","c.jpg"))
  val p13 = prod("p13","name13", 45,Seq("a.jpg","b.jpg","c.jpg"))
  val p14 = prod("p14","name14",999,Seq("a.jpg","b.jpg","c.jpg"))
  val p21 = prod("p21","name21", 88,Seq("a.jpg","b.jpg","c.jpg"))
  val p22 = prod("p22","name22", 88,Seq("a.jpg","b.jpg","c.jpg"))
  val p23 = prod("p23","name23", 88,Seq("a.jpg","b.jpg","c.jpg"))
  val p24 = prod("p24","name24", 88,Seq("a.jpg","b.jpg","c.jpg"))
  val p31 = prod("p31","name31",100,Seq("a.jpg","b.jpg","c.jpg"))
  val p32 = prod("p32","name32",  6,Seq("a.jpg","b.jpg","c.jpg"))
  val p33 = prod("p33","name33",  2,Seq("a.jpg","b.jpg","c.jpg"))
  val p34 = prod("p34","name34",234,Seq("a.jpg","b.jpg","c.jpg"))
  val p41 = prod("p41","name41", 10,Seq("a.jpg","b.jpg","c.jpg"))
  val p42 = prod("p42","name42", 11,Seq("a.jpg","b.jpg","c.jpg"))
  val p43 = prod("p43","name43", 77,Seq("a.jpg","b.jpg","c.jpg"))
  val p44 = prod("p44","name44",  9,Seq("a.jpg","b.jpg","c.jpg"))

  trait BaseGraphStore extends GraphStore {
    def allProducts: Seq[GraphProduct]
    def products(minPrice: Option[BigDecimal], limit:Option[Int]): Seq[GraphProduct] = {
      val ret = minPrice.map { x ⇒ allProducts.filter(_.price >= x) }.getOrElse(allProducts)
      limit.map{x⇒ret.take(x)}.getOrElse(ret)
    }
  }

  val store1 = new BaseGraphStore {
    def allProducts: Seq[GraphProduct] = Seq(p11,p12,p13,p14)
    def categories: Seq[GraphCategory] = Nil
    def id: String = "sid1"
  }
  val store2 = new BaseGraphStore {
    def allProducts: Seq[GraphProduct] = Seq(p21,p22,p23,p24)
    def categories: Seq[GraphCategory] = Nil
    def id: String = "sid2"
  }
  val store3 = new BaseGraphStore {
    def allProducts: Seq[GraphProduct] = Seq(p31,p32,p33,p34)
    def categories: Seq[GraphCategory] = Nil
    def id: String = "sid3"
  }
  val store4 = new BaseGraphStore {
    def allProducts: Seq[GraphProduct] = Seq(p41,p42,p43,p44)
    def categories: Seq[GraphCategory] = Nil
    def id: String = "sid4"
  }

  val ms1 = new GraphMetaSite {
    def id: String = "msid1"
    def name: String = "Name1"
    def published: Boolean = false
    def premium: Boolean = false
    def store: Option[GraphStore] = Some(store1)
  }
  val ms2 = new GraphMetaSite {
    def id: String = "msid2"
    def name: String = "Name2"
    def published: Boolean = true
    def premium: Boolean = true
    def store: Option[GraphStore] = Some(store2)
  }
  val ms3 = new GraphMetaSite {
    def id: String = "msid3"
    def name: String = "Name3"
    def published: Boolean = false
    def premium: Boolean = true
    def store: Option[GraphStore] = None
  }
  val ms4 = new GraphMetaSite {
    def id: String = "msid4"
    def name: String = "Name4"
    def published: Boolean = true
    def premium: Boolean = false
    def store: Option[GraphStore] = Some(store4)
  }

  def account(userId: String): GraphAccount = new GraphAccount {
    def email: String = "avitaln@wix.com"
    def metaSites(onlyPublished: Option[Boolean], onlyPremium: Option[Boolean]): Seq[GraphMetaSite] = {
      val all = Seq(ms1,ms2,ms3,ms4)
      (onlyPublished, onlyPremium) match {
        case (Some(true),Some(true)) ⇒ all.filter(x⇒x.published&&x.premium)
        case (_,Some(true)) ⇒ all.filter(_.premium)
        case (Some(true),_) ⇒ all.filter(_.published)
        case _ ⇒ all
      }

    }
    def id: String = userId
  }

  def products = Seq(p11,p12,p13,p14,p21,p22,p23,p24,p31,p32,p33,p34,p41,p42,p43,p44)
}



class GraphQLDemo(executorService: ExecutorService) {
  implicit val executionContext = ExecutionContext.fromExecutorService(executorService)
  def execute[CTX](request: GraphQLRequest): Map[String, Any] = {
    val Success(document: Document) = QueryParser.parse(request.query)
    val ctx = new DemoContext("foo")
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
