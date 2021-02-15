# EDAP15, Homework Exercise 2: Intraprocedural Data Flow Analysis

In this exercise, you will build and implementation off the MFP algorithm from Lecture 5 and use it to find null pointer errors in Teal code.

You will be working with an extended version of Teal, Teal-1, which includes the `nonnull` type qualifier.

Type qualifiers are refinements on types, and their meanings vary from
language to language.  The versions of Teal that we will be using in
this iteration of the course includes only the single type qualifier
`nonnull`, which indicates that an object must not be
`null`.

By default, Teal allows `null` in all variables, even in `int`
variables.  Thus, if we e.g. declare a variable to be of type `nonnull
int`, this means that (a) the variable must be initialised (since Teal sets all variables
to `null` by default), and (b)
that the initialiser expression must not be `null`.

Your goal is to identify all places in the program in which the code tries to store a `null` value in a `nonnull` variable, parameter, or return value, or uses such a value as an array index:
- Assignments
- Variable initialisations
- Function calls
- Function returns
- Array indexing

To simplify your implementation, you will be working on the Teal IR, which provides you with a Control Flow Graph
and Basic Blocks.

## Task Description

- Clone and build this version of Teal
- Make sure that you can run Teal.  This time, use `teal-1.jar`.
- Discuss among yourselves:  What should your Monotone Framework for this task look like?  (Make sure to check the **Deliverables** and **Output Format** first, so you know what you have to compute!)
    - Forward or Backward analysis?
	- Lattice?
	- Transfer Functions?
- To familiarise yourselves with the [Teal 0/1 IR](ir/teal0/ast/ir.ast), run Teal with the `-g`
  parameter on a few programs and make sure you understand the output.
- Implement a pass on Teal that performs intraprocedural data flow analysis to find assignments of `null` values that violate the `nonnull` specification *within function bodies*.
    - Implement your solution in the Teal IR (`ir/teal0/`).
    - You may reuse code from the first exercise.
- Test your code (see **Resources** below).   Write your own test cases as needed.
- Book a TA slot in Moodle and present your solution to the TA

## Deliverables

The expected deliverables are:
- A modified version of Teal-1 that solves the task and is committed to your CourseGit repository
    - Your solution must be **sound and complete** on Teal-1 code that involves (a) no arrays, (b) no function calls, (c) no loops (`for`, `while`) and (d) no conditionals (`if`).
    - For all other code, your solution should make the following assumptions:
	    - For array elements and function return values, assume that they may be `null` unless the array/function return type is `nonnull`.
		- All loop/conditional branches may be taken (e.g., if the code says `if 1 == 0 { A } else { B }`, ignore the `1 == 0` and assume that we might execute either `A` or `B`).
		- You may deviate from these assumptions with the explicit approval of the TA (check first!)
- A `hw2.sh` file, also committed to the CourseGit, that takes a single parameter (one Teal file), analyses it, and prints output in the format described below
- A short textual description of your monotone framework as a comment in `NullnessAnalysis.jrag`
- A presentation of your solution to the TA

### Output format

Your output must contain lines in the following style (written to the "standard output stream", i.e., via System.out.println() or equivalent operations):
```
N <line-number> <column-number> <category>
```
for assignments, variable initialisations, and `return` statements;
```
NI <line-number> <column-number> <category>
```
for array indexing; and
```
NC <line-number> <column-number> <category>*
```
for function calls.

Here:
- `N`, `NI`, and `NC` are the literal strings "N" resp. "NI" resp. "NC" (without the double quotes).
  You may print other lines (e.g. for debugging), as long as they do not start with `N`.
- `<line-number>` is the line in which the operation takes place.  If there is no line number, instead write the string "`-`" (without quotes) here.
- `<column-number>` is the column in which the operation takes place.  If there is no column number, write `BBi` where `i` is the number of the basic block you are in.
- `<category>` is one of the following:
    - `0` to signal: *value that is **guaranteed `null`** flows into array index or variable/parameter/return that is marked `nonnull`*
    - `?` to signal: *value that is **neither guaranteed `null` nor guaranteed non-`null`** flows into array index or  variable/parameter/return that is marked `nonnull`*
    - `+` to signal: *value that is **guaranteed non-`null`** flows into array index or variable/parameter/return that is marked `nonnull`*
    - `=` to signal: *value flows into array index or variable/parameter/return that is **not** marked `nonnull`*
- Each of these elements is separated by a single space/blank character.
- For `NC`, each `<category>` represents one of the function parameters, in left-to-right order.

You need only print a line if at least one category in that line is not `=`, but it is fine to print them all.

## Hints and Starting Points

The [README.md](README.org) file contains some high-level pieces of
information about Teal and pointers to good starting points.  In
addition, you may find the following files worth investigating:

- [Compiler.java](compiler/teal0/java/lang/Compiler.java) is stil the main
  entry point.  This time, you want to analyse the IR, not the AST; you can use
  the method that handles the `-Z` parameter as a starting point.
- [NullnessAnalysis.jrag](ir/teal0/ast/NullnessAnalysis.jrag)
  is fairly empty but contains some pointers to get you started.
- Teal's `-g` parameter prints out the IR and is very valuable for debugging.
- Design your Monotone Framework first, and convince yourselves on a few examples that it works before implementing it.
- Decide first what you need to implement for your solution, and write
  down a small plan to order the implementation steps.  Make sure to
  include the following components (incomplete list!):
    - Your lattice, including the join function
	- Figuring out whether an assignment is to a `nonnull`-typed variable
	- Transfer functions
	- The MFP algorithm itself (Slide 29 from [Lecture 5](http://fileadmin.cs.lth.se/cs/Education/EDAP15/2020/web/slides-05.pdf)).
	- Something that prints the output at the end
- Break your implementation into small steps and test often!

## Resources

- The [README.md](README.org) file contains general information about building, running, and extending Teal.
- To test your program, you can use the Teal test programs that ship with
  the distribution, in `compiler/testfiles/interpreter/*.in` and `examples/*.teal`, but you may
  find it useful to build your own.

