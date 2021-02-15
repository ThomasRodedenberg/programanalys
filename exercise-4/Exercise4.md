# EDAP15, Homework Exercise 4: Dynamic Analysis

Program Analysis is a bit like medicine. The difference between program analysis and medicine is that in medicine, it's people that are sick, while in program analysis, it's programs that are sick.

Just like humans, programs can get sick in different ways. They might have bugs, or they might be too slow. In exercises 1, 2, and 3, we were mostly concerned about bugs, and different ways to prevent them. In this one, we will take a look at performance.

If we imagine ourselves in the shoes of a doctor trying to cure a patient, we can think of two questions:

- What's wrong? (Can I find the simplest possible causes of a sickness?)
- What are the effects of my experiments on my patients? Particularly:
  - If I give them a treatment, do they feel better?
  - Do my experiments and treatment have side-effects?

We can ask similar questions about programs, which we will do in this exercise.

## Concepts to review

- Dynamic Analysis via Instrumentation
- Dynamic Events and their Characteristics
- Perturbation in Dynamic Analysis
- Warm-Up Effects
- Given a Dynamic Program Analysis, identify potential causes of Perturbation?

## Tips

- Take a look at the [IRUtil.jrag](ir/teal0/ast/IRUtil.jrag) file, it contains a number of utilities you might find useful. 
- As for the instrumentation code, you may implement it in [Compiler.java](compiler/teal0/java/lang/Compiler.java), or in a separate aspect.
- Check `customIRAction` in [Compiler.java](compiler/teal0/java/lang/Compiler.java) for some implementation hints
- To figure out what kind of IR code to generate, create some teal files and see what IR is generated for these files.

## Step 1: How is my program doing?

The first step is pretty simple: We want to see how long the program takes to run. That will give us a baseline.
Because every run is slightly different, we will run the program several times with the same input, measure the running time and aggregate the results.


### Warm-up effects

Because of JIT compilation, the program is optimized *while running*. Therefore, the performance of the program may increase if you run it several times in a row. We want to run the program enough times for it to converge to a stable estimate of the running time. We want to run the program several times *without restarting the Java Virtual Machine*, otherwise we lose the JITs improvements!

Answer the following questions:

- What's your definition of a *stable estimate* of the running time?
- How many runs to you need to get the measurement to converge to a stable value?

Produce a file called `baseline-runtime.txt` containing the running time of your program, in a format that looks like this:

    mean_running_time: <time>
    number_runs: <number>

Where `<time>` is the mean running time of your program, in nanoseconds.
And `<number>` is the number of times you ran your program, which should be enough to ignore warm-up effects! You should keep this number for the rest of the exercise. Contact [Noric](mailto:noric.couderc@cs.lth.se) if you want to try fancier methods.

## Step 2: What's wrong?

We have seen how long the program was taking to run, but that doesn't tell us much about the causes of this run time. To get a better understanding what's happening, we will measure different parts of the program independently. That will help us figure out what part of the program we should pay attention to.

Instrument your code so that it measures the total time spent *in each block*.

We will print three different values for each basic block. 
This means you will produce three files:

- `basic-blocks-mean-ranked.txt`
- `basic-blocks-median-ranked.txt`
- `basic-blocks-diff-ranked.txt`

For each statistic (`mean`, `median` and `diff`) produce a file named `basic-blocks-<statistic>-ranked.txt`, in a format that looks like this:

    <statistic> main.bb0
    <statistic> main.bb1
    ...

Where `<statistic>` is the mean, the median, and the difference between median and mean. 

In each file, for each block:

- the statistic, in nanoseconds
- the block name, with the format `<function>.<basic-block>:`.

Your basic blocks should be sorted by the statistic in each file (you may use `sort -rn` for this).

### Questions

- Look at the difference for each block. Is this difference mostly positive or negative for you, and what does it mean?

## Step 3: Do my experiments have side-effects?

We measured the total run time, but now, with instrumentation, our program does *more* than what it did before. So we may wonder what impact our instrumentation has over the total runtime.

Measure the total runtime of your *instrumented* program. Create a file `instrumented-runtime.txt`

It has a format similar as in step 1. Like so:

    mean_running_time: <mean>
    relative_slowdown: <slowdown>

Where:

- `<slowdown>` is the mean runtime of the instrumented version, divided by the mean runtime of the baseline version.

### Questions

- Is your relative slowdown more or less than 1?
- Is the relative slowdown the same thing as the mean of the baseline running time divided by the instrumented version's running time?

## Step 4: Understanding side-effects better

Our benchmarks now lead us to a dire realization: Our measurements changed the behaviour of the program, we introduced side-effects.
We may then try to understand how much time is lost to benchmarking each basic block.

To do that, re-run the program, where you only instrument *one* basic block at a time. Do that for each basic block. Compare with your baseline (the original program).

Produce the file `basic-blocks-overhead.txt`, in a format like the following:

    <mean-overhead> main.bb0
    <mean-overhead> main.bb1
    ...

For each block, there is a line `<mean-overhead> <function>.<basic-block>`, where the mean overhead is the mean of the differences between the instrumented block version and the baseline.

## Step 5: Can I factor out the side-effects?

We've seen that adding instrumentation introduced some overhead, leading to the following model: `total_time_block = time_block + time_instrumentation_block`. We estimated the `total_time_block` in Step 2, and `time_instrumentation_block` in step 4. But we're still lacking the information that interests us: the time spent in each block!

Subtract the mean instrumentation time to the total time spent in each block, and re-rank basic blocks. Does the ranking change?

Create the file `basic-blocks-ranked-2.txt` with the following format:

    <total_time_block - time_instrumentation_block> main.bb0
    <total_time_block - time_instrumentation_block> main.bb1
    ...

Where for each block, `<total_time_block - time_instrumentation_block>` is the difference between the time spent in the block and the instrumentation overhead of the block.

Now, we should have the *actual* time spent in each block. But is it really the case?

If we compare the total instrumentation time, and the sum of the instrumentation times for each block, we should get the same number. Do we?
