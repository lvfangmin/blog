Title: Typeahead Query Feature of Facebook and Twitter
Author: Allen Lv
Date: Sun Oct 26 00:29:31 CST 2014
Categories: typeahead, autocomplete, trie, lru cache, system desing

Typeahead search is a technical used to improve the user experience based on showing up the autocomplete when typing in. This feature, or variations thereof, has also been referred to as Autocomplete, search as you type, filter/find as you type (FAYT), incremental search, inline search, instant search, word wheeling, and other names as well.

It's adopted by many famous web sites, like Google, Facebook, Twitter, LinkedIn, etc. Here is a sample of Typeahead at Twitter:

![autocomplete](./typeahead_in_facebook_twitter/diagram-twitter-autocomplete.png)

This artical is going to show the algorithms and other technicals needed for on boarding Typeahead to real world.

## Trie
Trie is a tree data structure closely related to Typeahead. Trie is an ordered tree data structure that is used to store a dynamic set or associative array where the keys are usually strings.

A common application of trie is autocomplete applications, like dictionary, search (Google, Facebook, Twitter, LinkedIn), editor (like vim, emacs, eclipse), database query tools.
There is also an efficient trie based sorting algorithm of large strings set, as it's out of our goal, I will leave it for user to investigate: [Burstsort](http://en.wikipedia.org/wiki/Burstsort).

Here is a code trivial runnable code wrote in scala:

    class Trie {

      var value: Option[String] = None
      var children: Map[Char, Trie] = Map()

      def insert(texts: List[String]) {
        texts.map(insert(_))
      }

      def insert(text: String) {
        var current: Trie = this

        text.foreach { c =>
          current.children = Map(c -> new Trie()) ++ current.children
          current = current.children(c)
        }

        current.value = Some(text)
      }

      def queryPrefixTrie(prefix: String): Option[Trie] = {
        var current: Trie = this

        prefix.foreach { c =>
          if (current.children.getOrElse(c, Nil) != Nil) {
            current = current.children(c)
          } else {
            return None
          }
        }

        return Some(current)
      }

      def query(prefix: String): Option[List[String]] = {
        queryPrefixTrie(prefix).map(dfs(_))
      }

      def dfs(node: Trie): List[String] = {
        var result: List[String] = Nil
        node.value.map {x => result = x :: result}
        node.children.foreach {
          case(k, v) => result = dfs(v) ++ result
        }
        result
      }
    }

    object Trie extends App {
      val trie = new Trie()
      trie.insert(List("hello", "hey", "world"))

      trie.query("he").map(_.sorted.map(x => println(x)))
      trie.query("what").map(_.sorted.map(x => println(x)))
    }

To support ranks, each leaf node will contains a rank. At search time, the searched nodes are sorted by the rank.

Trie is easy to implement, but due to the high memory cost. Instead a lot of compressed trie trees, like [radix tree](http://en.wikipedia.org/wiki/Radix_tree), [HAT-trie: a kind of cache-conscious radix tree](http://code.google.com/p/hat-trie), [ternary search tree](http://en.wikipedia.org/wiki/Ternary_search_tree), [suffix tree]() are used. These compressed trie trees usually offers a good tradeoff between time and space costs. I will write articles to elaborate these important Trie variations.

Usually, the memory cost is not problem for the real world, for example when you typing in Facebook/Twitter/Amazon, the suggestion is related to the user who login in, so the memory used by Trie is not quite big. Also the Trie is maintained at client side, usually inside browser cache.

Twitter open sources a typeahead project, which provides a strong foundation for building robust typeaheads. The suggestion engine is responsible for computing suggestions for a given query. And the data structure used in is the uncompressed [Trie](https://github.com/twitter/typeahead.js/blob/master/src/bloodhound/search_index.js)

## System Design

No matter what applications, they share a common scenario:

    Given user login in
    When  user typed in the search box
    Then  the dropped down will show high quality results
    And   need near-perfect relevance so that the first result is the one user looking for

The only difference is the definition of <b>high quality</b>. It's easy to define the high quality for applications like dictionary, editor, database sql tool, as the contents to build the Trie are limited and same for all users. For most of the web sites, the high quality is related to the user. In the following section I will focus on elaborating how Facebook provides the high quality contents used to build the Trie. Twitter is quite the same, so will not elaborate here.

### Facebook

Facebook has about 1.3 billion users, which is a big challenging to support such a real-time Typeahead feature at each level of it's software stack. In the following sub-sections, I will try to guess the scalable architectures behing Facebook.

Before dumping into the details, we need to get a bird's eye view of the data Facebook has.

1. The most important data in Facebook is people, you can connect with a friend, or you can just follow someone you're interested, this can be stored as a [directed graph](), the code will be provided latter.
2. The 2ed significant data is events or feeds, by aggregate and calcuate the keywords of each event, we can have a dynamic hottest events list. The algorithm behind is [streaming max/min calculating](), I will give the scala code latter.
3. The 3rd is the groups, pages, and applications on the Facebook.

When typing in a query in the search bar, users want instantaneously see suggestions not only of the people, events, groups, applications and pages they're connected with but also the connections of their friends and globally relevant results. From Facebook aspect, they want users to engage in a wider variety of relevant results and be able to discover new connections users might want to make on Facebook.

### Facebook Typeahead Architecture

![fb_artitecture](./typeahead_in_facebook_twitter/fb_architecture.png)

#### Browser
As soon as the user focuses in the text box, a request is send off to retrieve all of the user's friends, pages, groups, applications, and upcoming events. These results are loaded into the browser's cache, and built into the Trie data structure for quickly searching the suggestion instead of sending another request.

If there are not enough contents in the browser cache, an AJAX request will be sent to load balancer, the request containing the current query string and a list of results that are already being displayed due to browser cache.

#### Load Balancer
Load balancer will route the request to the near-by low load Aggregate server.

#### Aggregate Server
The server will delegate the request to all the following services in parallel: 1. people graph server, 2. events service, 3.group, page service. The results will be aggregated according to the rank of relevance, then return it to the browser.

#### Service Layer
The services rank and index the contents, important services could be easily added to the architecture. For example, the applications service maintains the global ranking list of applications according to the number of users who have interacted with them in the last month. As this result are indepedent of the querying user, memcached could be used to improve the read throughput.

The person graph service need to return the relevance connections, for example the friends, friends of friends, the following, etc. The graph is really huge for such a large user bases, and connection complexity. It's a technical problem on how to store these huge graph in memory, how to partition if could not be stored on the same box, we will investigate this topic later.

## Conclusion
In this artical, I present an Trie based Typeahead implementation in scala. Then shows the real challenging of showing Typeahead is the high quality high relevance content. By analysing the Facebook scenarios, a possible architecture is shown up on how all these have done in real world. I learned a lot during writing this artical, also it's interesting.
