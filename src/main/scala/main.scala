
object Main {	
	def isExit(s: String): Boolean = s == null

    def evaluate(s: String) = {
    	try {
			println("\n" + grammy.Interpreter(s) + "\n")
		} catch {
			case t: Throwable => println(s"! ${t.getMessage}")
		}
    }

	def main(args: Array[String]): Unit = {
		if(args.length > 0) {
			val code = scala.io.Source.fromFile(s"${args(0)}").getLines.mkString("\n")
			println(grammy.Interpreter(code))
		} else {
			println("\nGrammy v0.1 -- a categorial grammar type-checker; CTRL-D to quit, double ENTER to query\n")
	        var continue = true
	        var collected = ""
	        while(continue){
	            print("> ")
	            io.StdIn.readLine match {
	            	case x if x == "" => evaluate(collected); collected = ""
	                case x if isExit(x) => println("Bye!"); continue = false
	                case x              => collected += x + "\n"
	            }
	        }

		}
	}
}