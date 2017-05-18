package akkahttpclient

object Euler7 extends App {
  def isPrime(num: Int) = {
    !(2 to math.sqrt(num).toInt).exists(num % _ == 0)
  }

  def nextPrimeFrom(n: Int) = {
    println("hi")
    Iterator.from(n + 1).find(isPrime).get
  }

  def primes = Iterator.iterate(2)(nextPrimeFrom)

  println(primes.drop(10).next)
}