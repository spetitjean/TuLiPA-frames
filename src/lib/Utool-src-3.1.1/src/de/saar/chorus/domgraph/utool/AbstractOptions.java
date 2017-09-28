/*
 * @(#)AbstractOptions.java created 10.02.2006
 * 
 * Copyright (c) 2006 Alexander Koller
 *  
 */

package de.saar.chorus.domgraph.utool;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;

import de.saar.chorus.domgraph.codec.InputCodec;
import de.saar.chorus.domgraph.codec.OutputCodec;
import de.saar.chorus.domgraph.equivalence.EquationSystem;
import de.saar.chorus.domgraph.graph.DomGraph;
import de.saar.chorus.domgraph.graph.NodeLabels;

/**
 * 
 * 
 * @author Alexander Koller
 *
 */
public class AbstractOptions {
    public static enum Operation {
        // real operations
        solve("Solve an underspecified description",
                "Usage: utool solve [options] [input-source]\n\n"
                        + "By default, reads an underspecified description from standard input\n"
                        + "and writes the solutions to standard output. An alternative input source\n"
                        + "can be specified on the command line; an alternative filename for the\n"
                        + "output can be specified with the -o option. The input and output codecs\n"
                        + "can be specified with the -I and -O options. If only an input codec\n"
                        + "is specified, and an output codec of the same name exists, this codec\n"
                        + "is used for output. `utool --display-codecs' will display a list of\n"
                        + "input and output codecs. Note that the output codec must support\n"
                        + "the output of multiple solved forms.\n\n"
                        + "Valid options:\n"
                        + "  --output, -o filename           Write solutions to a file.\n"
                        + "  --input-codec, -I codecname     Specify the input codec.\n"
                        + "  --input-codec-options options   Specify the input codec options.\n"
                        + "  --output-codec, -O codecname    Specify the output codec (default: same as input).\n"
                        + "  --output-codec-options options  Specify the output codec options.\n",
                true, true),

        solvable("Check solvability without enumerating solutions",
                "Usage: utool solvable [options] [input-source]\n\n"
                        + "This command checks whether an underspecified description is solvable.\n"
                        + "If it is, utool terminates with an exit code of 0; if it isn't, it terminates\n"
                        + "with an exit code of 1.\n\n"
                        + "The \'solvable\' command computes the total number of solved forms (= readings),\n"
                        + "but not the solved forms themselves (use \'solve\' if you want them). This makes\n"
                        + "\'solvable\' run much, much faster than \'solve\'. utool will display the total\n"
                        + "number of solved forms if you run \'utool solvable -s\'.\n\n"
                        + "By default, reads an underspecified description from standard input\n"
                        + "and writes the solutions to standard output. An alternative input source\n"
                        + "can be specified on the command line; an alternative filename for the\n"
                        + "output can be specified with the -o option. The input codec\n"
                        + "can be specified with the -I option. `utool --display-codecs'\n"
                        + "will display a list of valid input codecs.\n\n"
                        + "Valid options:\n"
                        + "  --input-codec, -I codecname     Specify the input codec.\n"
                        + "  --input-codec-options options   Specify the input codec options.\n"
                        + "  --nochart                       Don't compute the full chart.\n",
                true, false),

        convert("Convert underspecified description from one format to another",
                "Usage: utool convert -I inputcodec -O outputcodec [options] [input-source]\n\n"
                        + "By default, reads an underspecified description from standard input\n"
                        + "and writes it (in a different format) to standard output. An alternative\n"
                        + "input source can be specified on the command line; an alternative filename\n"
                        + "for the output can be specified with the -o option. The input and output\n"
                        + "codecs can be specifieid with the -I and -O options. `utool --display-codecs'\n"
                        + "will display a list of input and output codecs.\n\n"
                        + "Valid options:\n"
                        + "  --output, -o filename           Write solutions to a file.\n"
                        + "  --input-codec, -I codecname     Specify the input codec.\n"
                        + "  --input-codec-options options   Specify the input codec options.\n"
                        + "  --output-codec, -O codecname    Specify the output codec (default: same as input).\n"
                        + "  --output-codec-options options  Specify the output codec options.\n",
                true, true),

        classify("Check whether a description belongs to special classes",
                "Usage: utool classify [options] [input-source]\n\n"
                        + "This command checks whether an underspecified description belongs to a\n"
                        + "class with special properties. A call to \'utool classify\' returns an\n"
                        + "exit code that is the OR combination of some of the following values:\n\n"
                        + "    1   the description is a weakly normal dominance graph\n"
                        + "    2   the description is a normal dominance graph\n"
                        + "    4   the description is compact\n"
                        + "    8   the description can be compactified (or is already compact)\n"
                        + "   16   the description is hypernormally connected\n"
                        + "   32   the description is leaf-labelled\n\n"
                        + "For example, the exit code for a graph that is hypernormally connected\n"
                        + "and normal (and hence compactifiable), but not compact, would be 27.\n\n"
                        + "Note that the notion of hypernormal connectedness only makes sense\n"
                        + "for normal graphs (although utool will test for it anyway).\n\n"
                        + "Valid options:\n"
                        + "  --input-codec, -I codecname     Specify the input codec.\n"
                        + "  --input-codec-options options   Specify the input codec options.\n",
                true, false),

        display("Start the Underspecification Workbench GUI",
                "Usage: utool display [input-source]\n\n"
                        + "This command brings up a the Underspecification Workbench, a GUI\n"
                        + "for the Domgraph functionality. You may specify a USR filename on\n"
                        + "the command line, and this USR is then decoded and displayed. \n"
                        + "Alternatively, you may leave the filename away; this will then bring\n"
                        + "up an empty Underspecification Workbench window.\n\n"
                        + "Valid options:\n"
                        + "  --input-codec, -I codecname     Specify the input codec.\n"
                        + "  --input-codec-options options   Specify the input codec options.\n",
                false, false),

        server("Start Utool in server mode",
                "Usage: utool server [options]\n\n"
                        + "This command starts utool as a server which accepts commands and sends\n"
                        + "the results via a socket.\n" + "Valid options:\n"
                        + "  --port, -p port           Accept connections at this port (default: 2802)\n"
                        + "  --logging, -l [filename]  Write log messages to this file; if no filename\n"
                        + "                            is given, write to stderr.\n"
                        + "  --warmup                  Before starting the server, warm up the JVM by\n"
                        + "                            solving a number of USRs first.",
                false, false),

        help("Display help on a command",
                "Usage: utool help [command]\n\n"
                        + "Without any further parameters, \'utool help\' displays a list of available\n"
                        + "commands. Alternatively, pass one of the command names to \'utool help\' as the\n"
                        + "second parameter to get command-specific help for this command.\n",
                false, false),

        _version(null, null, false, false),

        _helpOptions(null, null, false, false),

        _displayCodecs(null, null, false, false)

        ;

        public String shortDescription, longDescription;
        public boolean requiresInput, requiresOutput;

        Operation(String shortDescription, String longDescription,
                boolean requiresInput, boolean requiresOutput) {
            this.shortDescription = shortDescription;
            this.longDescription = longDescription;
            this.requiresInput = requiresInput;
            this.requiresOutput = requiresOutput;
        }
    }

    private Operation operation;
    private Operation helpArgument;

    private boolean optionHelp;
    private boolean optionStatistics;
    private boolean optionNoOutput;
    private boolean optionEliminateEquivalence;
    private boolean optionDumpChart;
    private boolean optionWarmup;
    private boolean optionNochart;

    private InputCodec inputCodec;
    private OutputCodec outputCodec;

    private DomGraph graph;
    private NodeLabels labels;

    private Writer output;

    private String inputName;

    private EquationSystem equations;

    private int port;
    private PrintWriter logWriter;
    private boolean optionLogging;

    private String inputCodecOptions;
    private String outputCodecOptions;

    public AbstractOptions() {
        // some default values
        setInputCodec(null);
        setOutputCodec(null);
        setOperation(null);
        setOutput(new OutputStreamWriter(System.out));
        setOptionStatistics(false);
        setOptionNoOutput(false);
        setOptionEliminateEquivalence(false);
        setOptionDumpChart(false);
        setOptionWarmup(false);
        setOptionNochart(false);

        port = 2802;
        logWriter = null;
        optionLogging = false;

        inputCodecOptions = null;
        outputCodecOptions = null;
    }

    public EquationSystem getEquations() {
        return equations;
    }

    public void setEquations(EquationSystem equations) {
        this.equations = equations;
    }

    public InputCodec getInputCodec() {
        return inputCodec;
    }

    public void setInputCodec(InputCodec inputCodec) {
        this.inputCodec = inputCodec;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public boolean hasOptionEliminateEquivalence() {
        return optionEliminateEquivalence;
    }

    public void setOptionEliminateEquivalence(
            boolean optionEliminateEquivalence) {
        this.optionEliminateEquivalence = optionEliminateEquivalence;
    }

    public boolean hasOptionHelp() {
        return optionHelp;
    }

    public void setOptionHelp(boolean optionHelp) {
        this.optionHelp = optionHelp;
    }

    public boolean hasOptionNoOutput() {
        return optionNoOutput;
    }

    public void setOptionNoOutput(boolean optionNoOutput) {
        this.optionNoOutput = optionNoOutput;
    }

    public boolean hasOptionStatistics() {
        return optionStatistics;
    }

    public void setOptionStatistics(boolean optionStatistics) {
        this.optionStatistics = optionStatistics;
    }

    public Writer getOutput() {
        return output;
    }

    public void setOutput(Writer output) {
        this.output = output;
    }

    public OutputCodec getOutputCodec() {
        return outputCodec;
    }

    public void setOutputCodec(OutputCodec outputCodec) {
        this.outputCodec = outputCodec;
    }

    public Operation getHelpArgument() {
        return helpArgument;
    }

    public void setHelpArgument(Operation helpArgument) {
        this.helpArgument = helpArgument;
    }

    public DomGraph getGraph() {
        return graph;
    }

    public void setGraph(DomGraph graph) {
        this.graph = graph;
    }

    public NodeLabels getLabels() {
        return labels;
    }

    public void setLabels(NodeLabels labels) {
        this.labels = labels;
    }

    public boolean hasOptionDumpChart() {
        return optionDumpChart;
    }

    public void setOptionDumpChart(boolean optionDumpChart) {
        this.optionDumpChart = optionDumpChart;
    }

    public PrintWriter getLogWriter() {
        return logWriter;
    }

    public void setLogWriter(PrintWriter logWriter) {
        this.logWriter = logWriter;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean hasOptionLogging() {
        return optionLogging;
    }

    public void setOptionLogging(boolean optionLogging) {
        this.optionLogging = optionLogging;
    }

    public String getInputCodecOptions() {
        return inputCodecOptions;
    }

    public void setInputCodecOptions(String inputCodecOptions) {
        this.inputCodecOptions = inputCodecOptions;
    }

    public String getOutputCodecOptions() {
        return outputCodecOptions;
    }

    public void setOutputCodecOptions(String outputCodecOptions) {
        this.outputCodecOptions = outputCodecOptions;
    }

    public String getInputName() {
        return inputName;
    }

    public void setInputName(String inputName) {
        this.inputName = inputName;
    }

    public boolean hasOptionNochart() {
        return optionNochart;
    }

    public void setOptionNochart(boolean optionNochart) {
        this.optionNochart = optionNochart;
    }

    public boolean hasOptionWarmup() {
        return optionWarmup;
    }

    public void setOptionWarmup(boolean optionWarmup) {
        this.optionWarmup = optionWarmup;
    }

}
