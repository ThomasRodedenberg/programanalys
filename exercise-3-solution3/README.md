
# Table of Contents

1.  [Building](#org8a2eab6)
2.  [Structure](#orgb7cbbf3)
    1.  [Layers of Teal](#orgd3d1039)
    2.  [Documentation](#orgb748b70)
    3.  [Teal Intermediate Language (Teal IR)](#org9b7df16)
    4.  [Test files](#org8f8cc4a)
3.  [Using Teal](#org4705c66)
4.  [Running Teal programs](#org51333c2)
    1.  [Adding your own Teal actions](#org72d4476)
    2.  [Logging](#org51c871f)
5.  [Notes on the implementation](#org910bb08)
6.  [Git FAQ](#org03cb278)
    1.  [What's Git?](#orgab420f6)
    2.  [I Can't Clone the Repository](#org6242fa7)
    3.  [How Do I Update My Fork with Changes the Instructors Made?](#orga2be686)
        1.  [TL;DR](#org7ae0b9b)
        2.  [List Remotes](#org0133f01)
        3.  [Specify a Remote Upstream](#org5106dd8)
        4.  [Get the Changes](#orgf6e2f2c)
        5.  [Merging Changes](#org1630b13)
        6.  [Pushing to Gitlab](#orgd3fc244)


<a id="org8a2eab6"></a>

# Building

To build Teal-0 (the default) run:

    ./gradlew jar test

For other layers of Teal, set suitable parameters; e.g., for Teal-3, run:

    ~./gradlew -PlangVersion=teal-3 jar test

If you are using Teal as part of a course, you may be using a distribution of Teal that does not
include all Teal layers (possibly only Teal-0).


<a id="orgb7cbbf3"></a>

# Structure

The `compiler` directory contains the implementation of the compiler.
It is divided into the following parts:

1.  `compiler/<teal-variant>/frontend`: A compiler frontend that parses code, creates an AST, runs name and type analyses (where available).
2.  `compiler/<teal-variant>/ast/*.ast`: The three parts of the Teal AST: Programs and Modules (`toplevel.ast`), Types (`type.ast`), and declarations, expressions, and statements (`teal0.ast`)
3.  `compiler/<teal-variant>/backend`: A compiler backend that takes an AST and generates Teal IR code (the intermediate representation for Teal).
4.  `ir/`: An interpreter that takes Teal IR code and executes it (via interpretation)(. All variants of Teal currently use the same interpreter.


<a id="orgd3d1039"></a>

## Layers of Teal

Depending on your distribution, the `compiler` directory may contain multiple subdirectories,
from `compiler/teal0` to `compiler/teal3`.
Lower layers of Teal contain fewer language features.


<a id="orgb748b70"></a>

## Documentation

The `docs/` directory contains a description of the Teal language
(possibly limited to your current Teal distribution).  This
definition takes precedence over the implementation; if the
implementation disagrees with the documentation, you should assume
that this is a bug in the implementation.


<a id="org9b7df16"></a>

## Teal Intermediate Language (Teal IR)

Intermediate representations are simplified representations of a programming langauge
that make it easier to analyse and/or optimise the program.
The IR tends to be more verbose than the source code, since it isn't intended for humans to read
but only for the machine to analyse and execute.

Teal IR is most closely related to register transfer IRs such as GIMPLE, LLVM bitcode, or WebAssembly


<a id="org8f8cc4a"></a>

## Test files

The `testfiles` directory contains test files for various parts of the compiler and interpreter.
Each file in these directories can have the following extensions:

-   `.in`: A TEAL file that is loaded in a test
-   `.expected`: The expected output of the test (not necessary TEAL, depending on what we test).
-   `.out`: The actual output we got when running the part of the compiler we're testing.
    -   Tests compare `.expected` and `.out` files.
-   `.teal`: A TEAL file that is usually imported by some other file, usually used for library code.

These files are loaded and tested by the various test classes in:

-   `compiler/teal0/test/lang`


<a id="org4705c66"></a>

# Using Teal

The Teal compiler and runtime use the same entry point.  You can start
them with the following, which will print out help:
`java -jar build/teal-0.jar`

You can find this main entry point in: `teal/compiler/teal0/java/lang/Compiler.java`


<a id="org51333c2"></a>

# Running Teal programs

`java -jar build/teal-0.jar program.teal --run arg1 arg2 ...`
The arguments to the program can be integers or strings. This will call the `main` function (which must
take matching formal parameters) and print out the `main` function's return value.


<a id="org72d4476"></a>

## Adding your own Teal actions

You can find two predefined entry points in `teal/compiler/teal0/java/lang/Compiler.java` that you can
use to get started:

-   `Compiler.customASTAction()`, which you can run with `java -jar build/teal-0.jar program.teal -Y`, can process the AST
-   `Compiler.customIRAction()`, which you can run with `java -jar build/teal-0.jar program.teal -Z`, can process the IR


<a id="org51c871f"></a>

## Logging

Can be enabled by the `TEAL_DEBUG` environment variable:

-   `export TEAL_DEBUG=interp` enables interpreter debugging
-   `export TEAL_DEBUG=irgen` enables IR generation debugging
-   `export TEAL_DEBUG=interp,irgen` enables both interpreter and IR generation debugging


<a id="org910bb08"></a>

# Notes on the implementation

See [the implementation notes](notes.md) (if available in your distribution).


<a id="org03cb278"></a>

# Git FAQ

Here are answers to some questions you may ask yourself when using Git.


<a id="orgab420f6"></a>

## What's Git?

Git is what's called a version control system.
But what does that mean? Let's look at each word:

-   Version: A version is a snapshot of code, it's like a picture of the state of code at a given point.
-   Control: We want to manage versions, that is, we want to do things like:
    -   Change version easily (for instance, going back to an older version)
    -   Compare two versions
    -   Merge versions together
    -   etc.
-   System: Well, that's just a program that allows you to do something, in this case, version control.

In other words, git is a piece of software that helps you track and
compare changes you (and other people!) make to your code.

Have you ever made a million changes to a program, only
to realize your idea doesn't work and now you have to get
fifteen files back to the state they were in? Well,
git's job is to make this task easy.

Git is very useful, and used *everywhere*, but it's also
a bit difficult to learn. Some git commands will seem
very mysterious as you start, and that's normal,
if you need help, please contact us!

If you want to get a rough idea of the commands, you can use this [cheat sheet](https://about.gitlab.com/images/press/git-cheat-sheet.pdf).

For a more detailed introduction, you may look at [Gitlab's documentation](https://docs.gitlab.com/ee/gitlab-basics/start-using-git.html).

Lastly, if you prefer videos with rainbows and unicorns, you may be
interested in [this series of videos by Daniel Shiffman](https://thecodingtrain.com/beginners/git-and-github/).


<a id="org6242fa7"></a>

## I Can't Clone the Repository

You probably need to upload a SSH public key to the Gitlab server.
You generate those on your computer, two files will be created,
you upload the contents of of these files to the Gitlab, so it knows who you are.

The file you didn't upload (the private key) is not to be shared with anyone.

[Here](https://docs.gitlab.com/ee/ssh) is a tutorial on how to do that.


<a id="orga2be686"></a>

## How Do I Update My Fork with Changes the Instructors Made?

Sometimes, Noric or Christoph might update the exercises, you can synchronize your
forks with the changes have been made with git (while keeping your own changes too!).

Here's how you do it (based on [this tutorial](https://medium.com/@sahoosunilkumar/how-to-update-a-fork-in-git-95a7daadc14e)).


<a id="org7ae0b9b"></a>

### TL;DR

If you're too lazy to read the rest, here is the following in script form.
Run these instructions in the `exercise-3` directory.

    git remote add upstream https://git.cs.lth.se/creichen/edap15-exercise-3.git
    git fetch upstream
    git checkout master
    git merge upstream/master
    git push origin master

Otherwise, here are the explanations!


<a id="org0133f01"></a>

### List Remotes

This gives you the list of remote repositories, they are places where code lives
that aren't on your computer.

    git remote -v

You should see something like

    origin	git@coursegit.cs.lth.se:edap15-2020/<group>/exercise-3.git (fetch)
    origin	git@coursegit.cs.lth.se:edap15-2020/<group>/exercise-3.git (push)


<a id="org5106dd8"></a>

### Specify a Remote Upstream

This is a way to tell git you know another place where similar code
is, and that will be the address of the main exercise 1 repo, the one you forked.
We can give names to remote, we'll call this one *upstream*.

    git remote add upstream https://git.cs.lth.se/creichen/edap15-exercise-3.git


<a id="orgf6e2f2c"></a>

### Get the Changes

You can get the new changes by calling the following (don't worry, it won't erase any of your code!):

    git fetch upstream

If you look at your files, nothing should have changed. That's because
git can handle several copies of your code simultaneously without a problem,
using something called *branches*.

So now both the code from the upstream repo and yours are on your computer
you just can't see the other branch. You can look at it by typing `git checkout upstream/master`

You can also *compare* branches with `git diff upstream/master`, this will show
the differences between your master branch and `upstream/master`.


<a id="org1630b13"></a>

### Merging Changes

Lastly, git is also able to merge changes from two branches together.
There might be conflicts that you would have to resolve by hand, but in most
cases, it works.

You do this by running

    git checkout master # make sure you're on the right branch
    git merge upstream/master


<a id="orgd3fc244"></a>

### Pushing to Gitlab

Now you can update gitlab's copy of your code with `git push origin master`

