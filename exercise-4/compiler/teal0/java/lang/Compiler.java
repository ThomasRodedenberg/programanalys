package lang;

import java.io.FileReader;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.Queue;
import java.util.Arrays;

import beaver.Parser.Exception;

import lang.ast.Program;
import lang.ast.Module;
import lang.ast.Decl;
import lang.ast.TEALParser;
import lang.ast.LangScanner;
import lang.ast.CompilerError;

import lang.ir.*;

import lang.common.BuiltinNames;
import lang.common.SourceLocation;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.CommandLine;

public class Compiler {
	public static Object DrAST_root_node; //Enable debugging with DrAST

	public static boolean customASTAction(Program ast) {
		System.out.println("Hello from method 'customASTAction()' in " + Compiler.class + "!");
		// return "false" to finish here, otherwise the program will execute
		return false;
	}

	// The Teal function time() computes the current time in nanoseconds relative to a fixed point in time
	private static final IRFunction TIME_FUNCTION = new IRFunction(new IRName(BuiltinNames.TIME.getName()),
								       IRModule.makeIntegerTypeRef(),
								       new lang.ir.List(), new lang.ir.List(), new lang.ir.List());

	// A small demonstration of how to instrument basic blocks.
	// Takes a globale variable and modifies bb as follows:
	// - before all other instructions in "bb", do the following:
	// - create an array of size 3
	// - assign that array to the global "variable"
	// - read the current time and store it in index 0 of the array
	public static void
	instrumentBBDemo(IRCodeBB bb, IRVar variable) {
		int array_elements = 3; // no deep meaning; just to illustrate arrays

		// We need the function that  the bb is in to create helper variables
		IRFunction function = bb.getOwnerFunction();

		IRVar array_tmp = function.createAnyVariable("%t0"); // illegal variable name, so the source program can't be using this variable; must be a unique name within the function
		IRVar time_tmp = function.createAnyVariable("%t1");
		IRVar index_tmp = function.createAnyVariable("%t2");

		// And now we generate code
		IRInsn first_insn = new IRConstantInsn(array_tmp.ref(), new IRInteger((long) array_elements));
		bb.insertInsn(first_insn, 0);
		first_insn.addInsnAfter(new IRNewArrayInsn(array_tmp.ref(), IRModule.makeIntegerTypeRef(), array_tmp.ref()))
			.addInsnAfter(new IRStoreStaticInsn(array_tmp.ref(), variable.ref()))
			.addInsnAfter(new IRCallInsn(time_tmp.ref(), new lang.ir.List(), TIME_FUNCTION.ref()))
			.addInsnAfter(new IRConstantInsn(index_tmp.ref(), new IRInteger(0L)))
			.addInsnAfter(new IRStoreArrayInsn(time_tmp.ref(), array_tmp.ref(), index_tmp.ref()))
			;
	}

	// "opt" is the command line parameter after '-Z'; useful for reconfiguring
	public static boolean customIRAction(IRProgram ir, String opt) {
		System.out.println("Called with option " + opt);

		// Let's add a new global variable to the program in "ir"
		IRModule module = ir.getIRModule(0);
		// The name "%%global_counters" is illegal in Teal source code, so we won't clash with an existing name
		IRVar global_counters = new IRVar(new IRName("%%global_counters"), IRModule.makeArrayTypeRef());
		module.getIRVarList().add(global_counters);

		// Find the main entry point's first basic block by name; this is useful if e.g. we want to
		// initialise some global variables before the program starts
		IRCodeBB main_entry_point = null;
		for (IRCodeBB bb : ir.basicBlocks()) {
			if (bb.name().equals("main.bb0")) {
				main_entry_point = bb;
			}
			// System.out.println(bb.name());
		}

		instrumentBBDemo(main_entry_point, global_counters);
		// // Print the modified code to see what's going on
		// System.out.println(main_function);

		// ----------------------------------------
		// Benchmark runs
		ArrayList<IRValue> args = new ArrayList<>(); // our benchmark takes no arguments
		ArrayList<Long> measurements = new ArrayList<>();
		// run a few times
		// 10 runs; you should get _at least_ that many measurements
		for (int i = 0; i < 10; i++) {
			measurements.add(measureRunTime(ir, args));
			// Missing: factor in warm-up time and discard measurements
			// It's okay to tweak this by hand, but keep notes to explain your reasoning!
		}
		for (Long l : measurements) {
			System.out.println(l);
		}

		// ----------------------------------------
		// "Bonus" run; just to illustrate how to read global variables after a run
		System.out.println(runAndGetGlobal(ir, args, global_counters.ref()));

		return false; // return "false" to finish here, otherwise the program will execute again
	}

	// Interpret program with the given parameters, measure execution time in nanoseconds
	public static long measureRunTime(IRProgram p, List<IRValue> args) {
		try {
			long start = System.nanoTime();
			IRResult result = p.eval(args);
			// result.getGlobal(IRVarRef) allows us to look up global variables, if we ever need that
			long stop = System.nanoTime();
			long exec_time = stop - start;
			return exec_time;
		} catch (InterpreterException e) {
			System.err.println("Error while interpreting program: " + e.toString());
			throw new RuntimeException();
		}
	}

	// Interpret program with the give parameters, return the global variable identified by `varref`
	public static IRValue runAndGetGlobal(IRProgram p, List<IRValue> args, IRVarRef varref) {
		try {
			return p.eval(args).getGlobal(varref);
		} catch (InterpreterException e) {
			System.err.println("Error while interpreting program: " + e.toString());
			throw new RuntimeException();
		}
	}

	// Interpret program with the give parameters
	public static ArrayList<IRValue> parseArgs(List<String> strings) {
		ArrayList<IRValue> args = new ArrayList<>();
		if (strings != null) {
			for (String str : strings) {
				try {
					args.add(new IRIntegerValue(Long.parseLong(str)));
				} catch (NumberFormatException ignored) {
					args.add(new IRStringValue(str));
				}
			}
		}
		return args;
	}

	// Interpret program with the give parameters
	public static void interpret(IRProgram p, List<String> strings) {
		ArrayList<IRValue> args = parseArgs(strings);
		try {
			IRValue ret = p.eval(args).getReturnValue();
			System.out.println("" + ret);
		} catch (InterpreterException e) {
			System.err.println("Error while interpreting program: " + e.toString());
		}
	}

	public static Module createModuleFromFile(File f,
						  TEALParser parser,
						  List<CompilerError> errors) {
		LangScanner scanner;
		Module m;
		try {
			scanner = new LangScanner(new FileReader(f));
		} catch (FileNotFoundException e) {
			errors.add(new CompilerError(SourceLocation.UNKNOWN) {
					@Override
					public String report() {
						return "Missing input file '" + f + "'";
					}
				});
			return null;
		}

		try {
			m = (Module) parser.parse(scanner);
			m.setSourceFile(f.getPath());
		} catch (IOException | Exception e) {
			errors.add(new CompilerError(SourceLocation.UNKNOWN) {
					@Override
					public String report() {
						return "Parsing error in file '" + f + "': " + e;
					}
				});
			return null;
		}

		return m;
	}

	public static Program createProgramFromFiles(List<String> files, List<String> importPaths,
						     List<CompilerError> errors) {
		Queue<File> unresolvedImports = new LinkedList<>();

		Program program = new Program();
		TEALParser parser = new TEALParser();

		// seed the unresolved imports with the source files
		files.stream().map(f -> new File(f)).collect(Collectors.toCollection(() -> unresolvedImports));

		// now transitively import all the modules
		while (!unresolvedImports.isEmpty()) {
			File f = unresolvedImports.remove();
			if (program.moduleMap().containsKey(f)) {
				continue;
			}
			Module m = createModuleFromFile(f, parser, errors);
			if (m == null) {
			    System.err.println("Skipping file due to errors.");
			    continue;
			}
			m.setNameFromFile(f);
			// add the module's current directory as import path
			if (f.getParentFile() == null)
				m.addImportPaths(Collections.singleton("."));
			else
				m.addImportPaths(Collections.singleton(f.getParentFile().getPath()));
			m.addImportPaths(importPaths);

			program.moduleMap().put(f, m);
			program.addModule(m);

			unresolvedImports.addAll(m.importedFiles());
		}

		return program;
	}

	static class CmdLineOpts {
		enum Action {
			// Compiler actions
			PARSE,
			CHECK,
			CUSTOM_AST,
			CUSTOM_IR,
			IRGEN,
			INTERP
		}

		Action action = Action.INTERP;
		String outputFile;
		String inputFile;
		String customOption;
		List<String> importPaths;
		List<String> progArgs; // arguments for the interpreted program

		public void
		setProgArgs(String[] args) {
			if (args == null) {
				this.progArgs = new ArrayList<>();
			} else {
				this.progArgs = Arrays.asList(args);
			}
		}
	}

	private static void printHelp(Options options) {
		new HelpFormatter().printHelp("teal MODULE",
					      "Compile and run a TEAL module.\n\n",
					      options,
					      "", true);
	}

	private static void printVersion() {
		System.out.println("Teal layer " + Program.LAYER + ", version " + Program.VERSION);
	}

	public static CmdLineOpts parseCmdLineArgs(String[] args) {
		DefaultParser parser = new DefaultParser();
		CmdLineOpts ret = new CmdLineOpts();

		Option parse = Option.builder("p").longOpt("parse").hasArg(false)
			.desc("Parse the program and build the AST.").build();
		Option check = Option.builder("c").longOpt("check").hasArg(false)
			.desc("Perform semantic and type checks and print out the AST.").build();
		Option codegen = Option.builder("g").longOpt("codegen").hasArg(false)
			.desc("Generate IR code and print it out.").build();
		Option run = Option.builder("r").longOpt("run").hasArgs().optionalArg(true)
			.desc("Interpret the IR code.").build();
		Option custom1 = Option.builder("Y").longOpt("custom-ast").hasArg(false)
			.desc("Custom analysis on the AST").build();
		Option custom2 = Option.builder("Z").longOpt("custom-ir").hasArgs().optionalArg(true)
			.desc("Custom analysis on the IR").build();
		Option help = Option.builder("h").longOpt("help")
			.desc("Display this help.").build();
		Option version = Option.builder("V").longOpt("version")
			.desc("Print out version information.").build();

		OptionGroup action = new OptionGroup()
			.addOption(parse)
			.addOption(check)
			.addOption(codegen)
			.addOption(run)
			.addOption(custom1)
			.addOption(custom2)
			.addOption(help)
			.addOption(version);

		Option outputFile = Option.builder("o").longOpt("output").hasArg()
			.desc("Write the compiler's output to FILE.").argName("FILE").build();

		Option importPaths = Option.builder("i").longOpt("path").hasArg()
			.desc("Directories where to search for imported modules.").argName("DIR1:DIR2:...").build();

		Options options = new Options().addOptionGroup(action)
			.addOption(outputFile)
			.addOption(importPaths)
			.addOption(Option.builder("s").longOpt("source-locations").hasArg(false)
				   .desc("When printing out IR code, include the source location.").build())
			;

		try {
			CommandLine cmd = parser.parse(options, args);

			// Informative actions (don't use the compiler)
			if (cmd.hasOption("h")) {
				printVersion();
				printHelp(options);
				System.exit(0);
			} else if (cmd.hasOption("V")) {
				printVersion();
				System.exit(0);
			}

			// Assume that the user wants us to run the compiler
			if (cmd.getArgs().length != 1) {
				if (cmd.getArgs().length > 1) {
					System.err.println("Please specify only one MODULE argument.");
				} else {
					System.err.println("Missing MODULE argument.");
				}
				printHelp(options);
				System.exit(1);
			} else {
				ret.inputFile = cmd.getArgs()[0];
			}

			if (cmd.hasOption("p")) {
				ret.action = CmdLineOpts.Action.PARSE;
			} else if (cmd.hasOption("c")) {
				ret.action = CmdLineOpts.Action.CHECK;
			} else if (cmd.hasOption("g")) {
				ret.action = CmdLineOpts.Action.IRGEN;
			} else if (cmd.hasOption("Y")) {
				ret.action = CmdLineOpts.Action.CUSTOM_AST;
			} else if (cmd.hasOption("Z")) {
				ret.action = CmdLineOpts.Action.CUSTOM_IR;
				ret.customOption = cmd.getOptionValue("Z");
			} else if (cmd.hasOption("r")) {
				ret.action = CmdLineOpts.Action.INTERP;
				ret.setProgArgs(cmd.getOptionValues("r"));
			}

			if (cmd.hasOption("o")) {
				ret.outputFile = cmd.getOptionValue("o");
			}
			if (cmd.hasOption("s")) {
				IRProgram.printSourceLocations = true;
			}

			if (cmd.hasOption("i")) {
				ret.importPaths = Arrays.asList(cmd.getOptionValue("i").split(":"));
			} else {
				ret.importPaths = new ArrayList<>();
				ret.importPaths.add(".");
			}

		} catch (ParseException e) {
			printHelp(options);
			throw new RuntimeException(e);
		}

		return ret;
	}

	public static boolean run(CmdLineOpts opts) {
		List<CompilerError> compilerErrors = new ArrayList<>();

		// open the output file / stdout
		PrintStream out = System.out;
		if (opts.outputFile != null) {
			try {
				out = new PrintStream(opts.outputFile);
			} catch (FileNotFoundException e) {
				System.err.println("ERROR Can't open output file '" + opts.outputFile + "'.");
			}
		}

		// parse the program and all its imported modules
		Program program = createProgramFromFiles(Collections.singletonList(opts.inputFile),
												 opts.importPaths,
												 compilerErrors);


		// print any errors so far
		for (CompilerError e : compilerErrors) {
			System.err.println("ERROR " + e.report());
		}

		// fail if there are compiler erorrs
		if (!compilerErrors.isEmpty())
			return false;

		// if this is all what's requested, return
		if (opts.action == CmdLineOpts.Action.PARSE) {
			out.print(program.dumpTree());
			return true;
		}

		// run the semantic checks
		List<CompilerError> semaErrors = program.semanticErrors();
		List<CompilerError> nameErrors = program.nameErrors();

		for (CompilerError e : nameErrors) {
			System.err.println("ERROR " + e.report());
		}

		for (CompilerError e : semaErrors) {
			System.err.println("ERROR " + e.report());
		}

		// fail if there are any errors
		if (!nameErrors.isEmpty() || !semaErrors.isEmpty()) {
			return false;
		}

		if (opts.action == CmdLineOpts.Action.CHECK) {
			out.print(program.dumpTree());
			return true;
		}

		if (opts.action == CmdLineOpts.Action.CUSTOM_AST) {
			if (!customASTAction(program)) {
				return true;
			}
		}

		// Generate the IR program
		IRProgram irProg = program.genIR();
		if (opts.action == CmdLineOpts.Action.IRGEN) {
			irProg.print(out);
			return true;
		}

		if (opts.action == CmdLineOpts.Action.CUSTOM_IR) {
			if (!customIRAction(irProg, opts.customOption)) {
				return true;
			}
		}

		// Interpret the program
		interpret(irProg, opts.progArgs);

		return true;

	}

	public static void main(String[] args) {
		CmdLineOpts opts = parseCmdLineArgs(args);
		if (run(opts)) {
			System.exit(0);
		} else {
			System.exit(1);
		}
	}
}
