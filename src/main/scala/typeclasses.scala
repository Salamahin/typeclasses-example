/*
 Assuming we need to implement some configuration
 It requires to provide an API to get various types by specific key.

 The problem here is that for each particular type one need to implement 1 or 2 extra methods
 imagine that one day you need to fetch from you config something more complicated than primitive value
 like `Map[String, List[Duration]]`
 For sure you can introduce some extra `syntax` or extension method to give that API for end users but that approach
 in not very scalable and maintainable
 */
trait _1_AppConfig {
  def getInt(key: String): Int
  def getIntOpt(key: String): Option[Int]

  def getString(key: String): Int
  def getStringOpt(key: String): Option[Int]
}


/*
 Typeclass can help you in such scenario. That is a pattern represents dependency inversion principle of SOLID.
 It behaves like strategy pattern from GOF but with power of scala implicits looks much better.
 Lets implement it with several steps

 First of all we need to delegate that `get` functionality to some other object
 Instead of implement a special method for each particular type we delegate that problem to a getter
 */
trait Get[T] {
  def get(value: Any): T
}
class _2_AppConfig(vals: Map[String, Any]) {
  def get[T](key: String, getter: Get[T]): T = getter.get(vals(key))
}


/*
 Second, make it implicit and introduce a couple of instances. Usually we do this in companion of delegate.
 That is done because scala compiler will check companions of contextually bounded arguments of your method
 */
class _3_AppConfig(vals: Map[String, Any]) {
  def get[T](key: String)(implicit getter: Get[T]): T = getter.get(vals(key))
  def getOpt[T](key: String)(implicit getter: Get[T]): Option[T] = vals.get(key) map getter.get
}

object Get {
  implicit val getInt: Get[Int] = _.toString.toInt
  implicit val getDouble: Get[Double] = _.toString.toDouble
}

/*
 You can give your typeclasses superpowers if you make implicit factory of your typeclasses. In our example you may
 construct composite `Get` like this

 By bringing `MoreGetInstances` you may get from your config a list of any of previously declared types. You may do
 the same for maps, sets or different structures.
 In case you want to get from config something very specific you don't even need to introduce that in your Config API.
 You can just implement appropriate typeclasses near the code when its going to be used
 */
object MoreGetInstances {
  implicit val getAnyList: Get[List[Any]] = _.asInstanceOf[List[Any]]
  implicit def getTypedList[T](implicit getter: Get[T]): Get[List[T]] = (value: Any) => getAnyList.get(value) map getter.get
}

/*
 That's all
 */