# EDAP15, Homework Exercise 1: Implement a monomorphic type inference system for Teal-0

In this exercise you will implement a _monomorphic_ type inference
system on top of the Teal-0 language, with limited support for
polymorphism, for built-in operations only.

The exercise assumes that you are familiar with JastAdd for compiler
construction.  If you are not and do not want to learn JastAdd at this
time, please contact us and we will provide you with a documented
Visitor interface that allows you to work directly in Java.

To implement your solutions, communicate with each other, and show
your solution to the TA, use the [CourseGit](coursegit.cs.lth.se/)
system that you can also reach from the course home page.

## Task Description

- Preparations
    - Log all of your group members into the [CourseGit](coursegit.cs.lth.se/)
    - Send an e-mail to Noric (`noric.couderc@cs...`) and Christoph (`christoph.reichenbach@cs....`)
    - One of us will add you to your CourseGit project for you and notify you
    - You will receive instructions for how to fork the [Exercise 1](https://coursegit.cs.lth.se/edap15-2020/exercise-1) repository into own project; run those.
- Follow the build steps in the [README](README.md) to build Teal-0 and run its test cases.
- Make sure that you can run Teal on the examples in the `examples/` subdirectory (one of which requires you to pass additional parameters):
	- `java -jar ./compiler/teal-0.jar examples/hello-world.teal -r`
	- `java -jar ./compiler/teal-0.jar examples/iterate-add.teal -r 1 8`
- Implement a pass on Teal that performs monomorphic type inference.
    - Find a suitable main entry point. The [README](README.md) describes how to use the command-line argument command `-Y` for this purpose; this is the easiest approach.
	- Examine `teal/compiler/teal0/frontend/TypeInference.jrag`.  This file gives you a skeleton for how to implement the inference and provides several helpers.
	- Implement a *Fact Extraction* pass with fresh variables that satisfies the type constraints listed below (**Typing Rules**)
	- Implement *Unification*.
	- Make sure that your pass prints out the inferred types in the form described in the **Deliverables** section below.
	- Commit your changes to `git` frequently.
- Test your code (see **Resources** below).  You may want to write your own test cases.
- Book a TA slot in SAM and present your solution to the TA

## Deliverables

The expected deliverables are:
- A modified version of Teal-0 that solves the task and is committed to your CourseGit repository
- A `hw1.sh` file, also committed to the CourseGit, that takes a single parameter (one Teal file), analyses it, and prints output in the format described below (you can use the existing file or create your own)
- A presentation of your solution to the TA

### Output format

Your output must contain lines in the following style (written to the "standard output stream", i.e., via System.out.println() or equivalent operations):
```
A <line-number> <name> <type>
```
or
```
E <type> E <type>
```
Here:
- `A` and `E` are the literal strings "A" resp. "E" (without the double quotes).
  You may print other lines (e.g. for debugging), as long as they do not start with `A` or `E`.
- `<line-number>` is the name of the variable or function (e.g. `17`)
- `<name>` is the name of the function or variable (e.g., `a` or `main`)
- `<type>` the inferred type
    - For variables, this can e.g. be "int" or "string" or "array[int]"
    - For functions, use arrow and tuple type notation, as in:
        - `(int) -> int`
        - `(int * int) -> string`
        - `() -> int`
        - `(string, array[int], int) -> int`
    - For type variables, use globally unique nonnegative numbers and prefix them with an underscore, e.g. "_7" or "_22".
      You can generate these numbers e.g. by using a global variable as a counter (we consider this to be fine even if you use JastAdd).
- Each of these elements is separated by a single space/blank character.

If there was no type error, print exactly one `A`-line for each variable and function definition (including function parameters, which count as variables).

If there was a type error, print a single `E`-line with the two conflicting types.  (You may print `A` lines before the `E` line).

# Typing Rules
The [language specification](docs/teal-0.pdf) lists types and type schemes for the built-in operations.
For all other language constructs, the following rules apply:
- All types in Teal allow `null` values, including `int`s.
- We represent logical values as `int`s, including in `if` and `while` statements and the unary `not` operation.
- Array literals such as `[3, 7, 23] : array[int]` must contain elements of exactly the same type.
- There is no notion of overloading in the language, so function types and binary operator types (which are largely the same thing) must match exactly.
- In Teal-0, The `new` operator is only allowed on `array` types, and the expression parameter must be an `int` that describes the array size.
- Assignments require that the left-hand side (LValue) and right-hand side (RValue) have exactly the same type.
- Return statements must match up exactly with the function return type.
- Any explicit type declarations are "absolute", i.e., `(1 : string)` has type `string` (and will also produce a type error with type inference, of course.)
- Variable and function types must be consistent across definitions and usage locations

## Hints and Starting Points

The [README.md](README.org) file contains some high-level pieces of
information about Teal and pointers to good starting points.  In
addition, you may find the following files worth investigating:

- [Compiler.java](compiler/teal0/java/lang/Compiler.java) is the main
  entry point.
- [TypeInference.jrag](compiler/teal0/frontend/TypeInference.jrag)
  contains a skeleton type inferencer.  You can solve the exercise by modifying only this file and [Compiler.java](compiler/teal0/java/lang/Compiler.java).
- [BuiltinTypes.jrag](compiler/teal0/frontend/BuiltinTypes.jrag)
  handles built-in names and types.  You may not need to do much with
  it directly, but it illustrates how you can use [BuiltinNames.java](ir/teal3/java/lang/common/BuiltinNames.java).
  to get information about the built-in operations.
- [BuiltinNames.java](ir/teal3/java/lang/common/BuiltinNames.java) contains
  approximate types of the built-in operations.  This file doesn't
  support type schemes, so it uses somewhat imprecise type information
  You can modify the file to improve it or work around it by handling
  built-in operations with type schemes differently from those who don't.
- [NameAnalysis.jrag](compiler/teal0/frontend/NameAnalysis.jrag) contains useful
  attributes to look up names and function / variable declaration sites.
- [TypeAnalysis](compiler/teal0/frontend/TypeAnalysis.jrag) contains the attribute `implicitType()`, which
  can help simplify your inference code a little (optionally).

## Resources

- The [README.md](README.org) file contains general information about building, running, and extending Teal.
- You can find the language specification for Teal-0 [here](docs/teal-0.pdf).
- To test your program, you can use the Teal test programs that ship with
  the distribution, in `compiler/testfiles/interpreter/*.in`, but you may
  find it useful to build your own.  (Note that not all of the programs in that directory will pass type checking!)

Assume that there is no subtyping in the language.
This requires you to itereate over input programs.  You can implement this iteration in two ways:
- With JastAdd: _Extend_ the Teal source code package and add custom attributes.
- Without JastAdd: Use the TealVisitor interface to iterate over the AST.
You may not otherwise edit the TEAL source code (unless instructed by the TA).

