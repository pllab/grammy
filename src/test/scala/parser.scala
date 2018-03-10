
import org.scalatest.FlatSpec

class ParserSpec extends FlatSpec {
	import grammy._

	"A Parser" should "be able to parse a let-expression with a simple context" in {
		assert(Parser("""let in: cat""") == Context(Seq(), Seq(Word("cat"))))

		assert(Parser("""let cat: N in: cat""") == 
			Context(Seq(TypingRule(Word("cat"), Atom("N"))), Seq(Word("cat"))))

		assert(Parser("""let cat: N in: cat cat""") == 
			Context(Seq(TypingRule(Word("cat"), Atom("N"))), Seq(Word("cat"), Word("cat"))))
	}

	it should "be able to parse a let-expression with multiple type-declarations in context" in {
		assert(Parser("""let cat: N; dog: M in: cat dog""") ==
			Context(Seq(TypingRule(Word("cat"), Atom("N")),
					TypingRule(Word("dog"), Atom("M"))), Seq(Word("cat"), Word("dog"))))
	}

	it should "be able to parse let-expressions with left division types in context" in {
		assert(Parser("""let the: NP\N; bad: N\N; boy: N in: the bad boy""") ==
			Context(Seq(
				TypingRule(Word("the"), Left(Atom("N"), Atom("NP"))),
				TypingRule(Word("bad"), Left(Atom("N"), Atom("N"))),
				TypingRule(Word("boy"), Atom("N"))
			), Seq(Word("the"), Word("bad"), Word("boy")))
		)
	}

	it should "be able to parse let-expressions with right division types in context" in {
		assert(Parser("""let the: NP/N; bad: N/N; boy: N in: the bad boy""") ==
			Context(Seq(
				TypingRule(Word("the"), Right(Atom("NP"), Atom("N"))),
				TypingRule(Word("bad"), Right(Atom("N"), Atom("N"))),
				TypingRule(Word("boy"), Atom("N"))
			), Seq(Word("the"), Word("bad"), Word("boy")))
		)
	}

	it should "be able to parse let-expressions with both divisions in context" in {
		assert(Parser("""let verb: (NP\N)/NP in: verb""") ==
			Context(Seq(
				TypingRule(Word("verb"), Right(Left(Atom("N"), Atom("NP")), Atom("NP"))),
			), Seq(Word("verb")))
		)
	}

	it should "be able to parse let-expressions with type-aliases in context" in {
		assert(Parser("""let N = M; cat: N in: cat""") == 
			Context(Seq(AliasRule(Atom("N"), Atom("M")),
					TypingRule(Word("cat"), Atom("N"))), Seq(Word("cat"))))

		assert(Parser("""let Adjective = N/N; good: Adjective; cat: N in: good cat""") == 
			Context(Seq(
					AliasRule(Atom("Adjective"), Right(Atom("N"), Atom("N"))),
					TypingRule(Word("good"), Atom("Adjective")),
					TypingRule(Word("cat"), Atom("N")),
				), Seq(Word("good"), Word("cat"))))
	}

	it should "accept import statements and modular goals" in {
		assert(Parser("""let with Prelude in: module""") == 
			Context(Seq(
				ImportRule("Prelude")
			), Seq(Word("#module")))
		)
	}
}
