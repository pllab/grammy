
package grammy

import scala.io.Source
import scala.collection._

class Interpreter(
	val aliases: mutable.Map[Type, Type] = mutable.Map(), 
	val decls: mutable.Map[Word, Type] = mutable.Map()) {


	val mark = new Wrap("*", Mark)

	def desugar(τ: Type): Type = τ match {
		case Mark => Mark
		case One => One
		case a@Atom(_) if aliases.contains(a) => desugar(aliases(a))
		case a@Atom(_) => a
		case Left(a, b) => Left(desugar(a), desugar(b))
		case Right(a, b) => Right(desugar(a), desugar(b))
	}

	class Wrap(val w: String, val t: Type) {
		var prev: Wrap = mark
		var next: Wrap = mark

		def rawType = desugar(t)
		override def toString = s"$w : $t"
	}

	object Wrap {
		def unapply(w: Wrap) = Some((w.w, w.t))
	}

	def eval(context: Context): Seq[Wrap] = {
		def reduxLeft(a: Wrap, b: Wrap): Option[Wrap] = (a, b) match {
			case (Wrap(n1, t), Wrap(n2, Left(r, m))) if m == t =>
				val wrap = new Wrap(s"($n1 $n2)", r)
				wrap.prev = a.prev
				wrap.prev.next = wrap
				wrap.next = b.next
				wrap.next.prev = wrap
				Some(wrap)
			case _ => None
		}

		def reduxRight(a: Wrap, b: Wrap): Option[Wrap] = (a, b) match {
			case (Wrap(n1, Right(r, m)), Wrap(n2, t)) if m == t =>
				val wrap = new Wrap(s"($n1 $n2)", r)
				wrap.prev = a.prev
				wrap.prev.next = wrap
				wrap.next = b.next
				wrap.next.prev = wrap
				Some(wrap)
			case _ => None
		}

		context.rules.foreach {
			case ImportRule(t) => 
				val nested = new Interpreter()
				nested.eval(Parser(Source.fromFile(s"$t.grammy").getLines.mkString("\n")))
				aliases ++= nested.aliases
				decls ++= nested.decls
			case AliasRule(τ1, τ2) => aliases += τ1 -> desugar(τ2)
			case _ =>
		}

		context.rules.foreach {
			case TypingRule(w, τ) => decls += w -> desugar(τ)
			case _ =>
		}

		if(context.goal.head != Word("#module")) {		
			val vals = mutable.Map[Int, Wrap]()
			context.goal.zipWithIndex.foreach { case (w, i) => 
				vals += i -> new Wrap(w.s, decls(w))
			}

			context.goal.zipWithIndex.foreach {
				case (w, i) =>
					if(i > 0)
						vals(i).prev = vals(i - 1)

					if(i + 1 < context.goal.length)
						vals(i).next = vals(i + 1)
			}

			mark.next = vals(0)
			mark.prev = vals(context.goal.length - 1)
		
			def pass: Boolean = {
				var current = mark.next
				
				while(current != mark) {
					val redux = current match {
						case Wrap(_, Left(_, _)) => reduxLeft(current.prev, current)
						case Wrap(_, Right(_, _)) => reduxRight(current, current.next)
						case _ => None
					}

					if(redux.isDefined) { return true }
					current = current.next
				}

				return false
			}

			while(pass) {}

			{
				var list = Seq[Wrap]()
				var current = mark.next
				while(current != mark) {
					list = list :+ current
					current = current.next
				}

				list
			}
		} else Seq(mark)
	}
}