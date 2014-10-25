class Trie {

  var value: Option[String] = None;
  var children: Map[Char, Trie] = Map();

  def insert(texts: List[String]) {
    texts.map(insert(_));
  }

  def insert(text: String) {
    var current: Trie = this;

    text.foreach { c =>
      current.children = Map(c -> new Trie()) ++ current.children;
      current = current.children(c);
    }

    current.value = Some(text);
  }

  def queryPrefixTrie(prefix: String): Option[Trie] = {
    var current: Trie = this;

    prefix.foreach { c =>
      if (current.children.getOrElse(c, Nil) != Nil) {
        current = current.children(c);
      } else {
        return None;
      }
    }

    return Some(current);
  }

  def query(prefix: String): Option[List[String]] = {
    queryPrefixTrie(prefix).map(dfs(_));
  }

  def dfs(node: Trie): List[String] = {
    var result: List[String] = Nil;
    node.value.map {x => result = x :: result};
    node.children.foreach {
      case(k, v) => result = dfs(v) ++ result
    };
    result;
  }
}

object Trie extends App {
  val trie = new Trie();
  trie.insert(List("hello", "hey", "world"));

  trie.query("he").map(_.sorted.map(x => println(x)));
  trie.query("what").map(_.sorted.map(x => println(x)));
}
