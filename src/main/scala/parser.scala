
package grammy

sealed trait Expression
case class Word(s: String) extends Expression

sealed trait Type extends Expression 

case object Mark extends Type {
	override def toString = "*"
}

case object One extends Type { 
	override def toString = "1"
}

case class Atom(s: String) extends Type { 
	override def toString = s
}

case class Left(result: Type, mult: Type) extends Type { 
	override def toString = s"$mult\\$result"
}

case class Right(result: Type, mult: Type) extends Type { 
	override def toString = s"$result/$mult"
}

sealed trait Statement

sealed trait Rule extends Statement
case class AliasRule(τ1: Type, τ2: Type) extends Rule
case class TypingRule(w: Word, τ: Type) extends Rule
case class ImportRule(t: String) extends Rule

case class Context(rules: Seq[Rule], goal: Seq[Word]) extends Statement

import scala.util.parsing.combinator._

object Parser extends RegexParsers {
	override def skipWhitespace = true
	override val whiteSpace = "[ \t\r\f\n]+".r

	case object LET
	case object IN

	def let_keyword = "let" ^^ { _ => LET }
	def in_keyword = "in:" ^^ { _ => IN }

	def word_name = """[a-z]+""".r ^^ { word => Word(word) }
	def type_name = """[A-Z][a-zA-Z]*""".r ^^ { atom => Atom(atom) }

	def bracket: Parser[Type] = "(" ~> type_declaration <~ ")"
	def one: Parser[Type] = "1" ^^ { _ => One }
	def left: Parser[Left] = non_rec_type_decl ~ """\""" ~ non_rec_type_decl ^^ { case l ~ _ ~ r => Left(r, l) }
	def right: Parser[Right] = non_rec_type_decl ~ """/""" ~ non_rec_type_decl ^^ { case l ~ _ ~ r => Right(l, r) }

	def non_rec_type_decl: Parser[Type] = one | type_name | bracket
	def type_declaration: Parser[Type] = one | left | right | type_name | bracket

	def alias = type_name ~ "=" ~ type_declaration ^^ { case t ~ _ ~ d => AliasRule(t, d) }
	def typing = word_name ~ ":" ~ type_declaration ^^ { case t ~ _ ~ d => TypingRule(t, d) }
	def imported = "with" ~> type_name ^^ { case t => ImportRule(t.s) }

	def module = "module" ^^ { _ => Seq(Word("#module")) }
	def rules = imported | alias | typing
	def query = module | rep(word_name)
	def header = repsep(rules, ";")
	def let = let_keyword ~ header ~ in_keyword ~ query ^^ { case _ ~ rs ~ _ ~ q => Context(rs, q) }
	def program = let

	def apply(s: String) = parse(program, s) match {
		case Success(result, _) => result
		case Failure(msg, _) => throw new Exception(msg)
		case Error(msg, _) => throw new Exception(msg)
	}
}
