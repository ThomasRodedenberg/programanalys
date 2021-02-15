-# EDAP15, Homework Exercise 3: Implement an interprocedural points-to analysis

In this exercise, you will implement an interprocedural points-to analysis on top of Teal-2's IR.

To implement your solutions, communicate with each other, and show
your solution to the TA, use the [CourseGit](coursegit.cs.lth.se/)
system that you can also reach from the course home page.

## What's the difference between Teal-1 and Teal-2

TEAL-2 adds simple data structures (C `struct`s or Pascal/Modula `record`s) to Teal-1:
```
type ABC(a : nonnull int, b : nonnull int, c : ABC);
fun main() = {
  var x : ABC := new ABC(1, 2, null);
  x := new ABC(3, 4, x);
  print(x.a);
}
```

These types only specify a list of fields, all of which we must fill when we call `new`.

The IR changes are in `ir/teal2/ast/object.ast`:
```
IRClass : IRTypeCon ::= IRVar*;
```
This type constructor defines user-defined types as lists of fields.  You will not have to use it yourself.

```
IRLoadInsn : IRAbstractLoadInsn ::= Base:IRVarRef Field:IRVarRef;
IRStoreInsn : IRAbstractStoreInsn ::= Base:IRVarRef Field:IRVarRef;
```

These instrutions load from and store to a field in a user-defined type.
- `IRLoadInsn` inherits `Dst`, the target variable, so it encodes `Dst := Base.Field`.
- `IRStoreInsn` inherits `Src`, the source variable, so it encodes `Base.Field := Src`.

```
IRNewInsn : IRInsn ::= Dst:IRVarRef IRType;
```
Instantiates a user defined type.

## What we know about the analysis

- We use Andersen's points-to analysis to analyse the heap.
- Andersen's analysis will build a *directed inclusion graph*.
- The analysis is *interprocedural*.
- The analysis is *flow insensitive*.
- The analysis from Exercise 2 is not needed.

## Concepts to review

- Lecture 7, Pointer Analysis 1:
  - Concrete Heap Graph
  - Abstract Heap Graph
- Lecture 9, Advanced Techniques:
  - Andersen's Algorithm

## Task Description

- Clone and build this version of Teal
- Track the [Exercise 3](https://git.cs.lth.se/edap15-2020/exercise-3) repository (only needed if we push updates; you can do this at any time).
- Follow the build steps in the [README](README.md) to build Teal-2 and run its test cases.
    - Make sure that you can run Teal. This time, use `teal-2.jar`.
- Examine how the Teal IR encodes `new` on user defined types with at least one field.
- Implement Andersen's analysis on Teal-2.
    - Find a suitable main entry point. The [README](README.md) describes how to use the command-line argument command `-Z` for this purpose; this is the easiest approach.
    - Make sure that your pass prints out the expected output in the form described in the **Deliverables** section below.
    - Commit your changes to `git` frequently.
- Test your code (see **Resources** below).  You may want to write your own test cases.
- Book a TA slot in Moodle and present your solution to the TA.

## Deliverables

The expected deliverables are:
- A modified version of Teal-2 that solves the task and is committed to your CourseGit repository
- A `hw3.sh` file, also committed to the CourseGit, that takes a single parameter (one Teal file), analyses it, and prints output in the format described below (you can use the existing file or create your own)
- A presentation of your solution to the TA.

### Output format

For each variable that is a `nonnull`, you will print:

- One line for each location it *MAY* point to, with
  - The line and column of allocation site of the variable
  - If it can point to null, the line and column where the null was created.
- An suitable marker if the variable may be assigned `null`.
- We consider that integers are objects.
- `nonnull` variables here include formal parameters and return values that are declared `nonnull`, as well as the implicit parameters to:
  - array indexing (both base and index)
  - field load/store (base only).

The format follows the structure::

```
<update-kind> <line-number> <column-number> -> obj <pto-line-number> <pto-column-number>
<update-kind> <line-number> <column-number> -> null <pto-line-number> <pto-column-number>
```

Here:

- `<update-kind>` It represents the kind of the non-null operation.
  - For array indexes (`i` in `a[i]`), print `NI`.
  - For arrays that we are indexing in an array indexing expression (`a` in `a[i]`), print `NA`.
  - For the base object in a field access (`o` in`o.x`), print `NO`.
   - Otherwise (parameters, returns, local/global variable assignments), print `N`

You may print other lines (e.g. for debugging), as long as they do not start with `N`.

If you wish, you an omit `NA`, `NO`, and `NI` output in cases where the variable cannot point to `null`.  This requires a few more lines but produces cleaner output (as in our examples).

- `obj` means that the variable points to an object
- `null` means that it points to a null literal. You must keep track of the source location of that particular `null` literal. Hence it has a line number and a column number.

- `<line-number>` and `<column-number>` are the start line/column of the relevant operation.
- `<pto-line-number>` and `<pto-column-number>`, analogously, are the line and column of the allocation or the `null` literal in question.

#### Example

If you analyze the following file:

```
type Trpl(fst : nonnull int, snd : int, trd : nonnull int);

fun null_producer(n : int) = {
    if (n == 0) {
       return null;
    }
    return n;
}

fun main(n : int) : nonnull int = { // line 10
    var p : Trpl := new Trpl(0, 1, 2);
    p.snd := null;
    p.fst := null_producer(n);
    return p.fst;
}
```

Your analysis should print the following (or something similar, see below):

```
N 1 10 -> null 5 14
N 1 10 -> null 12 13
N 1 40 -> obj 11 29
N 1 40 -> obj 11 32
N 1 40 -> obj 11 35
N 10 0 -> null 5 14
N 10 0 -> obj 11 29
N 10 0 -> obj 11 32
N 10 0 -> obj 11 35
N 10 0 -> null 12 13
```

Where:
- `N` means that we are talking about an assignment to a nonnull variable (here, a field).
- `14 4` refers to the source location that Teal associates with the `return` statement
- `1 10` refers to the source location that Teal associates with the `fst` field.  Since we don't expect a field-sensitive analysis, it is fine to point to _any_ nonnull field of the same structure.
- `1 40` is the location of the `trd` field; it is equivalent to the `fst` field for us.
- `obj 11 35` refers to the literal number `2`
- `null 5 14` is the source location in which `null` occurs in `null_producer` (from which it can make its way into `p.fst`).

Note that `p.fst` will not point to `n`: the content of `n` will flow into `p.fst`, however; in Andersen's analysis,
this translates into an inclusion edge (after some steps of analysis).  Since nothing in this code calls `main`, we can't see what `n` will point
to, so that inclusion edge will have no effect.  (You may optionally model user input to the program as a special program location,
but we don't require that.)

Likewise, for the input:

```
type XY(x : int, y : int);

fun main() = {      // line 3
    var a : XY := null;
    a.x := 1;
}
```

Your analysis should print:

```
NO 5 4 -> null 4 18
```

## Hints and Starting Points

The [README.md](README.org) file contains some high-level pieces of
information about Teal and pointers to good starting points.  In
addition, you may find the following files worth investigating:

- Start small.
- [Compiler.java](compiler/teal0/java/lang/Compiler.java) is the main entry point.
- [NullPointerAnalysis.jrag](ir/teal2/analysis/NonNullPointerAnalysis.jrag) is a skeleton for implementing the Analysis.
- [The IR AST Definition](ir/teal0/ast/ir.ast)
- [The IR Improvements for Teal-2](ir/teal2/ast/object.ast) contains some more AST nodes for the Teal-2 IR.
- Recall that Andersen's algorithm connects nodes `n` and `n.☐` (used for field accesses).
- Andersen's algorithm collects information about variables and memory locations, while we ask you for information about IR instructions.  Note that the latter all operate on IRVars.
  Make sure to map all Andersen concepts to Teal.

### Debugging

You can print TEAL IR with the line numbers using the flags `-g` and `-s`.


### Arrays in TEAL
Keep in mind that in TEAL, arrays and indexes are automatically `nonnull`:

Therefore:
```
a[i] := x;
```

Can fail because:
- `a` is null
- `i` is null
- `x` is null and the element type of `a` is `nonnull <something>`.

The same applies to field accesses.

### Builtin Functions

Builtins don't return `null`, except `print`, but you can ignore this case.

## Resources

- The [README.md](README.org) file contains general information about building, running, and extending Teal.
- To test your program, you can use the Teal test programs that ship with
  the distribution, in `compiler/testfiles/interpreter/*.in`, but you may
  find it useful to build your own.  (Note that not all of the programs in that directory will pass type checking!)

Apart from the analysis file, you may not otherwise edit the TEAL source code (unless instructed by the TA).
