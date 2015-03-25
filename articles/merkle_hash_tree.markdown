Title: Efficiently Verify Distributed Replica Consistency with Merkle Hash Tree
Author: Allen Lv
Date: Tue Nov 11 00:28:06 CST 2014
Categories: merkle, hash, tree, cassandra, dynamo

[Merkle Tree](http://en.wikipedia.org/wiki/Merkle_tree) is a special full binary tree where only leaf nodes contain the value, the other nodes hold the hashes their children. It's a data structure that allows the efficient comparison of a series of data blocks to find differences. They're used to efficiently verify the replica consistency in distributed systems without having to transimit the whole data set. They can also be used to efficiently verify that a set of files or datas has not been changed or corrupted during the transit.


## Merkle Tree Appearance

From [wikipedia](http://en.wikipedia.org/wiki/Merkle_tree):

![Merkle Tree](./merkle_hash_tree/500px-Hash_Tree.svg.png)

## Typical usage of Merkle Tree:

1. Cassandra's AntiEntropy service uses [Merkle trees](https://github.com/apache/cassandra/blob/trunk/src/java/org/apache/cassandra/utils/MerkleTree.java) to detect the inconsistencies in data between replicas.
2. Git distributed revision control system.
3. Bitcoin peer to peer network.

## Hash Function
We need to choose a hash function to hash the nodes, here I'm using SHA-1, you're free to choose whatever hash function, of course.

Below is the hash function I’m using in my Merkle Tree implementation. I’m using the SHA-1 implementation that comes with JAVA. The output of the function is a hex string.


    trait Hash {
      def hash(byteSequence: Seq[Byte]): Vector[Byte]
    }

    object SHA1Hash extends Hash {
      val SHA1 = MessageDigest.getInstance("SHA-1")

      override def hash(byteSequence: Seq[Byte]): Vector[Byte] = {
        SHA1.digest(byteSequence.toArray).toVector
      }
    }

## Building Merkle Tree

We can recursively construct the Merkle Tree from the root to the leaf nodes. Assume there are N values, which means there are N leaf nodes. As Merkle Tree is a full binary tree, the height of the Merkle Tree would be ceil(logN).
Let H be the hash function, for any node N in the tree: H(N) = H(H(N.left) + H(N.right)), where N.left and N.right are the left and right sub trees of node N.

Detailed implementation:

    object MerkleTree {

      sealed trait Tree[+A, H <: Hash] {
        val hash: Vector[Byte]
      }

      case class Node[+A, H <: Hash](
        leftChild: Tree[A, H],
        rightChild: Tree[A, H])(hashFunction: H)
        extends Tree[A, H] {

        override val hash: Vector[Byte] = {
          hashFunction.hash(leftChild.hash ++ rightChild.hash)
        }
      }

      case class Leaf[+A, H <: Hash](data: A)(hashFunction: H)
        extends Tree[A, H] {

        override val hash: Vector[Byte] = hashFunction.hash(data.toString.getBytes)
      }

      case class EmptyLeaf[H <: Hash]()(hashFunction: H) extends Tree[Nothing, H] {
        override val hash: Vector[Byte] = Vector.empty[Byte]
      }

      def create[A, H <: Hash](
        dataBlocks: Seq[A],
        hashFunction: H = SHA1Hash): Tree[A, H] = {

        val level = calculateRequiredLevel(dataBlocks.size)
        val dataLeaves = dataBlocks.map(data => Leaf(data)(hashFunction))
        val paddingNeeded = math.pow(2, level).toInt - dataBlocks.size
        val padding = Seq.fill(paddingNeeded)(EmptyLeaf()(hashFunction))
        val leaves = dataLeaves ++ padding

        makeTree(leaves, hashFunction)
      }

      def merge[A, H <: Hash](
        leftChild: Tree[A, H],
        rightChild: Tree[A, H],
        hashFunction: H): Node[A, H] = {
        Node(leftChild, rightChild)(hashFunction)
      }

      private def calculateRequiredLevel(numberOfDataBlocks: Int): Int = {
        def log2(x: Double): Double = math.log(x) / math.log(2)

        math.ceil(log2(numberOfDataBlocks)).toInt
      }

      @tailrec
      private def makeTree[A, H <: Hash] (
        trees: Seq[Tree[A, H]],
        hashFunction: H): Tree[A, H] = {

        def createParent(treePair: Seq[Tree[A, H]]): Node[A, H] = {
            val leftChild +: rightChild +: _ = treePair
            merge(leftChild, rightChild, hashFunction)
        }

        if (trees.size == 0) {
            EmptyLeaf()(hashFunction)
          } else if (trees.size == 1) {
            trees.head
          } else {
            makeTree(trees.grouped(2).map(createParent).toSeq, hashFunction)
          }
        }
    }

## Reference
- [Cassandra AntiEntropy](http://en.wikipedia.org/wiki/Merkle_tree)
- [Dynamo](http://www.allthingsdistributed.com/2007/10/amazons_dynamo.html)
