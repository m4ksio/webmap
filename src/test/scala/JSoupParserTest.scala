import java.net.URL

import org.scalatest.{FlatSpec, FlatSpecLike, FunSuite, Matchers}

/**
  * Created by m4ks on 29/06/2017.
  */
class JSoupParserTest extends FlatSpec with Matchers {

  "jsouparser" should "calculate base urls properly" in {

    val html =
      """<html>
        |    <h1>Hello, welcome to my site</h1>
        |    <p>Below you can find the list of my favorite pages:
        |    <ul>
        |        <li><a href="books/books.html">Books</a></li>
        |        <li><a href="movies/movies.html">Movies</a></li>
        |        <li><a href="companies/fintech.html">Companies I like</a></li>
        |    </ul></p>
        |</html>
      """.stripMargin

    val parser = new JSoupParser(new URL("http://localhost"))

    val strings = parser.parse(html, "/")

    strings should contain allOf("/books/books.html", "/movies/movies.html")
  }

  it should "calculate base urls properly deeper in the tree" in {

    val html =
      """<html>
        |    <h1>Hello, welcome to my site</h1>
        |    <p>Below you can find the list of my favorite pages:
        |    <ul>
        |        <li><a href="books/books.html">Books</a></li>
        |        <li><a href="movies/movies.html">Movies</a></li>
        |        <li><a href="companies/fintech.html">Companies I like</a></li>
        |    </ul></p>
        |</html>
      """.stripMargin

    val parser = new JSoupParser(new URL("http://localhost/"))

    val strings = parser.parse(html, "/magic/")

    strings should contain allOf("/magic/books/books.html", "/magic/movies/movies.html")
  }

  it should "ignore local hrefs" in {

    val html =
      """<html>
        |        <a href="#a">Books</a><
        |        <a href="/fixed.html">Movies</a>
        |        <a href="#b">Movies</a>
        |</html>
      """.stripMargin

    val parser = new JSoupParser(new URL("http://localhost/"))

    val strings = parser.parse(html, "/magic/")

    println(strings)
    strings should contain("/fixed.html")
    strings should have size(1)
  }

}
