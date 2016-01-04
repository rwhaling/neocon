case class Hit[+T](hit:T,context:List[T]) {
  def this(prev:Hit[T],hit:Hit[T]) = this(hit.hit,prev.hit :: prev.context)
  def this(hit:T) = this(hit,List.empty)
  override def toString():String = {
    val fullHit = this.hit :: this.context
    return fullHit.reverse.mkString(" ")
  }
}

// Expensive.  Has to keep all matching pairs in memory before you can return.
def matchAll[T](cmp:Ordering[T],leftSide:Iterable[Hit[T]],rightSide:Iterable[Hit[T]]):Stream[Hit[T]] = {
  var (left,right) = (leftSide,rightSide)
  for (r <- right) {
    while (!left.isEmpty) {
      val (l,r) = (left.head,right.head)
      val check = cmp.compare(l.hit,r.hit)
      if (check < 0) {
        left = left.tail
      } else if (check == 0) {
        var matches = new Hit[T](left.head,right.head) :: List.empty
        var leftTail = left.tail
        if (!leftTail.isEmpty) {
          while (cmp.compare(leftTail.head.hit,r.hit) == 0) {
            matches = new Hit[T](leftTail.head,r) :: matches      
            println(matches)
            leftTail = leftTail.tail
          }
        }
        var rightTail = right.tail
        if (!rightTail.isEmpty) {
          while (cmp.compare(l.hit,rightTail.head.hit) == 0) {
            matches = new Hit[T](l,rightTail.head) :: matches
            println(matches)
            rightTail = rightTail.tail
          }
        }
        // SHOULD SORT BY HIT
        return matches.reverse.toStream  #::: join(cmp,left.tail,right.tail)
      } else {
        right = right.tail
      }
    }
  }
  return Stream.empty
}

// Cheap.  Only scans one.
def matchLeft[T](cmp:Ordering[T],leftSide:Iterable[Hit[T]],rightSide:Iterable[Hit[T]]):Stream[Hit[T]] = {
  var (left,right) = (leftSide,rightSide)
  for (r <- right) {
    while (!left.isEmpty) {
      val (l,r) = (left.head,right.head)
      val check = cmp.compare(l.hit,r.hit)
      if (check < 0) {
        left = left.tail
      } else if (check == 0) {
        val matches:Stream[Hit[T]] = new Hit(l,r) #:: Stream.empty
        return matches #::: matchOnce(cmp,left.tail,right)
      } else {
        right = right.tail
      }
    }
  }
  return Stream.empty
}


// Cheap.  Only scans one.
def matchRight[T](cmp:Ordering[T],leftSide:Iterable[Hit[T]],rightSide:Iterable[Hit[T]]):Stream[Hit[T]] = {
  var (left,right) = (leftSide,rightSide)
  for (r <- right) {
    while (!left.isEmpty) {
      val (l,r) = (left.head,right.head)
      val check = cmp.compare(l.hit,r.hit)
      if (check < 0) {
        left = left.tail
      } else if (check == 0) {
        val matches:Stream[Hit[T]] = new Hit(l,r) #:: Stream.empty
        return matches #::: matchOnce(cmp,left,right.tail)
      } else {
        right = right.tail
      }
    }
  }
  return Stream.empty
}

// Cheap.  Only scans one.
def matchOnce[T](cmp:Ordering[T],leftSide:Iterable[Hit[T]],rightSide:Iterable[Hit[T]]):Stream[Hit[T]] = {
  var (left,right) = (leftSide,rightSide)
  for (r <- right) {
    while (!left.isEmpty) {
      val (l,r) = (left.head,right.head)
      val check = cmp.compare(l.hit,r.hit)
      if (check < 0) {
        left = left.tail
      } else if (check == 0) {
        val matches:Stream[Hit[T]] = new Hit(l,r) #:: Stream.empty
        return matches #::: matchOnce(cmp,left.tail,right.tail)
      } else {
        right = right.tail
      }
    }
  }
  return Stream.empty
}

val ord = scala.math.Ordering.Int

val s1 = List(new Hit(1),new Hit(2), new Hit(2), new Hit(3), new Hit(4), new Hit(10))
val s2 = List(new Hit(2),new Hit(4), new Hit(4), new Hit(6), new Hit(8), new Hit(10))
val s3 = matchAll(ord,s1,s2)
s3.foreach(println)

val s4 = List(new Hit(0),new Hit(4), new Hit(8), new Hit(10), new Hit(16))
val s5 = matchOnce(ord,s3,s4)
s5.foreach(println)

