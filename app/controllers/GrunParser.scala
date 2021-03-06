package actors

import scala.io.Source
import scala.xml.pull.{EvElemEnd, EvText, EvElemStart, XMLEventReader}

case class Node(pre: Int, size: Int)
case class Token(node_pre: Int, content: String, start: Int, end: Int)

object GrunParser {
  def apply(filename:String): String = {
    val source = Source.fromFile(filename)
    var response = "Uploaded " + filename + ": \n"
    val reader = new XMLEventReader(source)
    val doc = Node(0,0)
    var stack = doc :: List()
    var index: List[Token] = List()
    for (event <- reader) event match {
      case EvElemStart(prefix,tag,attrs,scope) => {
        val parent = stack.head
        val new_pre = parent.pre + parent.size + 1
        val new_el = Node(new_pre, 0)
        stack = new_el :: stack
        response += s"<$tag pre=$new_pre>"
      }
      case EvText(text) => {
        val pattern = """\w+""".r
        response = response + text
        val matches = pattern.findAllIn(text)
        var count = 0
        var tok_string = ""
        matches.foreach { e =>
          tok_string = tok_string + s"'${matches.group(0)}':${matches.start(0)}:${matches.end(0)} "
          val token = Token(stack.head.pre, matches.group(0), matches.start(0),matches.end(0))
          index = token :: index
          count += 1
        }
        if (count > 0) response = response + s"( $count tokens: $tok_string)"
      }
      case EvElemEnd(prefix,tag) => {
        val done = stack.head
        stack = stack.tail
        val parent = stack.head
        val updated_parent = Node(parent.pre, parent.size + done.size + 1)
        val dist = done.pre - parent.pre
        stack = updated_parent :: stack.tail
        response += s"</$tag pre=${done.pre} dist=$dist size=${done.size} >"
      }
      case _ => {}
    }
    index = index.sortBy( t => (t.content, t.node_pre) )
    return response
  }
}
