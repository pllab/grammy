# grammy
This is a very straight-forward implementation of the concept of [categorial grammars](https://en.wikipedia.org/wiki/Categorial_grammar), and its use in natural language parsing.

The quickest way to see how it works is to go and look at the interpreter tests, but if you're in a hurry, here's an example program below, and what it does in practice. Every Grammy program is a single let-binding with several type-declarations or type-aliases and one sentence which is to be parsed. Currently, this only works from within scala, but a REPL is planned.

```
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
```

The types created (and aliased) here, namely `N`, `NP` and such, are arbitrary marks for `Noun`, `NounPhrase` etc. You can see the same being used in the Wiki page above. Any statement of the form `A = B` is an alias. Statements like `a: A` are type declarations, saying that the word on the left is of the syntactic type on the right. 

The two slash types (fractional types) are left- and right-derivations. They reduce our expressions in some way. For example, `M/N` expects something of type `N` behind it to reduce the whole `M/N N` phrase into just `M`. Similarly, `N N\M` will be reduced to `M` from the left. As a way to remember which slash is which operation, start with the lower end and pull towards the top of the slash -- the direction you're pulling in is where you expect the argument.

The type `1` marks a unit type, and we can use it to check whether a sentence is syntactically well-typed. If it is, it will end up being `1` after all the reductions are done!

## Modules

### Prelude

Grammy supports header-includes, which can simplify writing. Compare the example above with the following, equivalent piece:

```
let
  with Prelude
in: the bad boy made that mess
```

Prelude is declared as a small library of things used in the examples, and around.

### Writing modules

To write modules, create a file with a `.grammy` extension, and let it have a single let-binding of the following form:

```
let
  ...
in: module
```

The `module` keyword will make sure nothing gets executed, so you can safely include it in your programs.
