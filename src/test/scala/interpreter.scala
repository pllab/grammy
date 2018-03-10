
import org.scalatest.FlatSpec

class InterpreterSpec extends FlatSpec {
	import grammy._

	"The unit and atomic types" should "not be reducible" in {
		assert(new Interpreter().eval(Parser("let x: 1 in: x")).head.t == One)
		assert(new Interpreter().eval(Parser("let x: X in: x")).head.t == Atom("X"))
	}

	"Fractional types" should "not be reducible by themselves" in {
		assert(new Interpreter().eval(Parser("""let x: X/X in: x x""")).head.t == Right(Atom("X"), Atom("X")))
		assert(new Interpreter().eval(Parser("""let x: X\X in: x x""")).head.t == Left(Atom("X"), Atom("X")))
	}

	"Wikipedia" should "be right about these things" in {
		val done = new Interpreter().eval(Parser("""
			let
				the: NP/N;
				bad: N/N;
				boy: N;
				made: (NP\1)/NP;
				that: NP/N;
				mess: N
			in: the bad boy made that mess
		"""))

		assert(done.head.t == One)
		assert(done.head.w == "((the (bad boy)) (made (that mess)))")
	}

	"Aliases" should "be totally reducible" in {
		val done = new Interpreter().eval(Parser("""
			let
				Noun = N;
				Adj = N/N;
				IVerb = NP\1;
				TVerb = IVerb/NP;
				Det = NP/N;

				the: Det;
				bad: Adj;
				boy: Noun;
				made: TVerb;
				that: Det;
				mess: Noun
			in: the bad boy made that mess
		"""))

		assert(done.head.t == One)
		assert(done.head.w == "((the (bad boy)) (made (that mess)))")
	}

	"Imports" should "work, at least from Prelude" in {
		{
			val done = new Interpreter().eval(
				Parser("let with Prelude in: the good boy cleaned this mess"))

			assert(done.head.t == One)
			assert(done.head.w == "((the (good boy)) (cleaned (this mess)))")
		}

		{
			val done = new Interpreter().eval(
				Parser("let with Prelude in: the good boy whistled"))

			assert(done.head.t == One)
			assert(done.head.w == "((the (good boy)) whistled)")
		}		
	}
}
