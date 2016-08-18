package demo

object InMemoryDatabase {

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

  def filterProducts(allProducts: Seq[GraphProduct], minPrice: Option[BigDecimal], limit:Option[Int], sortedByPrice: Option[Boolean]): Seq[GraphProduct] = {
    val ret = minPrice.map { x ⇒ allProducts.filter(_.price >= x) }.getOrElse(allProducts)
    val ret1 = limit.map { x ⇒ ret.take(x) }.getOrElse(ret)
    sortedByPrice match {
      case Some(true) ⇒ ret1.sortBy(_.price)
      case _ ⇒ ret1
    }
  }

  trait BaseGraphStore extends GraphStore {
    def allProducts: Seq[GraphProduct]
    def products(minPrice: Option[BigDecimal], limit:Option[Int], sortedByPrice: Option[Boolean]): Seq[GraphProduct] =
      filterProducts(allProducts, minPrice, limit, sortedByPrice)
  }

  val store1 = new BaseGraphStore {
    def allProducts: Seq[GraphProduct] = Seq(p11,p12,p13,p14)
    def id: String = "sid1"
  }
  val store2 = new BaseGraphStore {
    def allProducts: Seq[GraphProduct] = Seq(p21,p22,p23,p24)
    def id: String = "sid2"
  }
  val store3 = new BaseGraphStore {
    def allProducts: Seq[GraphProduct] = Seq(p31,p32,p33,p34)
    def id: String = "sid3"
  }
  val store4 = new BaseGraphStore {
    def allProducts: Seq[GraphProduct] = Seq(p41,p42,p43,p44)
    def id: String = "sid4"
  }

  val allStores = Seq(store1, store2, store3, store4)

  trait BaseGraphMetaSite extends GraphMetaSite {
    override def hasStore: Boolean = store.isDefined
  }

  val ms1 = new BaseGraphMetaSite {
    def id: String = "msid1"
    def name: String = "Name1"
    def isPremium: Boolean = false
    def store: Option[GraphStore] = Some(store1)
  }
  val ms2 = new BaseGraphMetaSite {
    def id: String = "msid2"
    def name: String = "Name2"
    def isPremium: Boolean = true
    def store: Option[GraphStore] = Some(store2)
  }
  val ms3 = new BaseGraphMetaSite {
    def id: String = "msid3"
    def name: String = "Name3"
    def isPremium: Boolean = true
    def store: Option[GraphStore] = None
  }
  val ms4 = new BaseGraphMetaSite {
    def id: String = "msid4"
    def name: String = "Name4"
    def isPremium: Boolean = false
    def store: Option[GraphStore] = Some(store4)
  }

  def account(userId: String): GraphAccount = new GraphAccount {
    def email: String = "avitaln@wix.com"
    def metaSites(onlyStores: Option[Boolean], onlyPremium: Option[Boolean]): Seq[GraphMetaSite] = {
      val all = Seq(ms1,ms2,ms3,ms4)
      (onlyStores, onlyPremium) match {
        case (Some(true),Some(true)) ⇒ all.filter(x⇒x.hasStore&&x.isPremium)
        case (_,Some(true)) ⇒ all.filter(_.isPremium)
        case (Some(true),_) ⇒ all.filter(_.hasStore)
        case _ ⇒ all
      }

    }
    def id: String = userId
  }

  def products(minPrice: Option[BigDecimal], limit:Option[Int], sortedByPrice: Option[Boolean]): Seq[GraphProduct] = {
    val allProducts = allStores.flatMap(_.allProducts)
    filterProducts(allProducts, minPrice, limit, sortedByPrice)
  }

  def product(id: String): Option[GraphProduct] = allStores.flatMap(_.allProducts).find(_.id==id)

}
