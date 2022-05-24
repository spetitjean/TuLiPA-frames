package de.tuebingen.semantics;
/*
 * @(#)CommandLineOptionsParser.java created 10.02.2006 for utool
 *
 * Copyright (c) 2006 Alexander Koller
 *
 */

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.List;

import de.saar.chorus.domgraph.ExampleManager;
import de.saar.chorus.domgraph.GlobalDomgraphProperties;
import de.saar.chorus.domgraph.codec.CodecManager;
import de.saar.chorus.domgraph.codec.InputCodec;
import de.saar.chorus.domgraph.codec.MalformedDomgraphException;
import de.saar.chorus.domgraph.codec.OutputCodec;
import de.saar.chorus.domgraph.codec.ParserException;
import de.saar.chorus.domgraph.equivalence.EquationSystem;
import de.saar.chorus.domgraph.graph.DomGraph;
import de.saar.chorus.domgraph.graph.NodeLabels;
import de.saar.chorus.domgraph.utool.*;
import de.saar.chorus.domgraph.utool.AbstractOptions.*;
import de.saar.getopt.ConvenientGetopt;

class CommandLineParser {
    private static final char OPTION_VERSION = (char) 1;
    private static final char OPTION_HELP_OPTIONS = (char) 2;
    private static final char OPTION_DUMP_CHART = (char) 3;
    private static final char OPTION_INPUT_CODEC_OPTIONS = (char) 4;
    private static final char OPTION_OUTPUT_CODEC_OPTIONS = (char) 5;
    private static final char OPTION_WARMUP = (char) 6;
    private static final char OPTION_NOCHART = (char) 7;


    private CodecManager codecManager;
    private ExampleManager exampleManager;

    public CommandLineParser() {
        codecManager = new CodecManager();
        codecManager.setAllowExperimentalCodecs(GlobalDomgraphProperties.allowExperimentalCodecs());
        registerAllCodecs(codecManager);

        try {
            exampleManager = new ExampleManager();
            exampleManager.addAllExamples("examples");
            exampleManager.addAllExamples("projects/Domgraph/examples");
        } catch (de.saar.chorus.domgraph.ExampleManager.ParserException e) {
            System.err.println("A parsing error occurred while reading an examples declaration.");
            System.err.println(e + " (cause: " + e.getCause() + ")");

            System.exit(ExitCodes.EXAMPLE_PARSING_ERROR);
        }
    }

    public AbstractOptions parse(String[] args)
            throws AbstractOptionsParsingException {
        AbstractOptions ret = new AbstractOptions();

        String argument = null;
        Operation op = null;
        InputCodec inputCodec = null;
        OutputCodec outputCodec = null;

        // parse command line options
        ConvenientGetopt getopt = makeConvenientGetopt();
        getopt.parse(args);

        // determine operation and filename
        List<String> rest = getopt.getRemaining();

        if (!rest.isEmpty()) {
            op = resolveOperation(rest.get(0));
        }

        if (rest.size() > 1) {
            argument = rest.get(1);
        }


        // handle special commands
        if (getopt.hasOption('d')) {
            ret.setOperation(Operation._displayCodecs);
            return ret;
        }

        if (getopt.hasOption('h')) {
            ret.setOperation(Operation.help);
            ret.setHelpArgument(op);
            return ret;
        }

        if (op == Operation.help) {
            ret.setOperation(Operation.help);
            ret.setHelpArgument(resolveOperation(argument));
            return ret;
        }

        if (getopt.hasOption(OPTION_HELP_OPTIONS)) {
            ret.setOperation(Operation._helpOptions);
            return ret;
        }

        if (getopt.hasOption(OPTION_VERSION)) {
            ret.setOperation(Operation._version);
            return ret;
        }


        // at this point, we must have an operation; otherwise, we 
        // silently interpret this as a "help" command.
        if (op == null) {
            ret.setOperation(Operation.help);
            ret.setHelpArgument(null);
            return ret;
        } else {
            ret.setOperation(op);
        }


        // obtain graph
        if (op.requiresInput || (argument != null)) {
            String inputCodecOptions = null;

            if (getopt.hasOption(OPTION_INPUT_CODEC_OPTIONS)) {
                inputCodecOptions = getopt.getValue(OPTION_INPUT_CODEC_OPTIONS);
                ret.setInputCodecOptions(inputCodecOptions);
            }

            inputCodec = determineInputCodec(getopt, argument, inputCodecOptions);

            if (inputCodec == null) {
                throw new AbstractOptionsParsingException("You must specify an input codec!",
                        ExitCodes.NO_INPUT_CODEC_SPECIFIED);
            } else {
                DomGraph graph = new DomGraph();
                NodeLabels labels = new NodeLabels();

                ret.setInputCodec(inputCodec);

                if (argument == null) {
                    throw new AbstractOptionsParsingException("This operation requires an input graph.",
                            ExitCodes.NO_INPUT);
                }

                try {
                    Reader reader = null;

                    if ("-".equals(argument)) {
                        reader = new InputStreamReader(System.in, Charset.forName("UTF-8"));
                        ret.setInputName("(standard input)");
                    } else if (argument.startsWith("ex:")) {
                        reader = exampleManager.getExampleReader(argument.substring(3));

                        if (reader == null) {
                            throw new IOException("Can't find example file '" + argument.substring(3) + "'");
                        }

                        ret.setInputName(argument);
                    } else {
                        reader = inputCodec.getReaderForSpecification(argument);
                        ret.setInputName(argument);
                    }


                    inputCodec.decode(reader, graph, labels);

                    ret.setGraph(graph);
                    ret.setLabels(labels);
                } catch (MalformedDomgraphException e) {
                    throw new AbstractOptionsParsingException("A semantic error occurred while decoding the graph.",
                            e, ExitCodes.MALFORMED_DOMGRAPH_BASE_INPUT + e.getExitcode());
                } catch (IOException e) {
                    throw new AbstractOptionsParsingException("An I/O error occurred while reading the input.",
                            e, ExitCodes.IO_ERROR);
                } catch (ParserException e) {
                    throw new AbstractOptionsParsingException("A parsing error occurred while reading the input.",
                            e, ExitCodes.PARSING_ERROR_INPUT_GRAPH);
                }
            }
        }

        // output
        if (getopt.hasOption('n')) {
            ret.setOptionNoOutput(true);
        } else {
            if (op.requiresOutput) {
                String outputCodecOptions = null;

                if (getopt.hasOption(OPTION_OUTPUT_CODEC_OPTIONS)) {
                    outputCodecOptions = getopt.getValue(OPTION_OUTPUT_CODEC_OPTIONS);
                    ret.setOutputCodecOptions(outputCodecOptions);
                }

                outputCodec = determineOutputCodec(getopt, inputCodec, outputCodecOptions);

                if (outputCodec == null) {
                    throw new AbstractOptionsParsingException("You must specify an output codec for this operation!", ExitCodes.NO_OUTPUT_CODEC_SPECIFIED);
                }

                ret.setOutputCodec(outputCodec);


                if (getopt.hasOption('o')) {
                    try {
                        ret.setOutput(new FileWriter(getopt.getValue('o')));
                    } catch (IOException e) {
                        throw new AbstractOptionsParsingException("An I/O error occurred while opening the output file!", e, ExitCodes.IO_ERROR);
                    }
                } else {
                    ret.setOutput(new OutputStreamWriter(System.out));
                }
            }
        }

        // some global options

        if (getopt.hasOption('s')) {
            ret.setOptionStatistics(true);
        }

        if (getopt.hasOption(OPTION_DUMP_CHART)) {
            ret.setOptionDumpChart(true);
        }

        if (getopt.hasOption(OPTION_WARMUP)) {
            ret.setOptionWarmup(true);
        }

        if (getopt.hasOption(OPTION_NOCHART)) {
            ret.setOptionNochart(true);
        }

        if (getopt.hasOption('l')) {
            String val = getopt.getValue('l');

            ret.setOptionLogging(true);

            if (val == null) {
                ret.setLogWriter(new PrintWriter(System.err, true));
            } else {
                try {
                    ret.setLogWriter(new PrintWriter(new FileWriter(val), true));
                } catch (IOException e) {
                    throw new AbstractOptionsParsingException("An I/O error occurred while opening the log file!", e, ExitCodes.IO_ERROR);
                }
            }
        }

        if (getopt.hasOption('p')) {
            ret.setPort(Integer.parseInt(getopt.getValue('p')));
        }

        if (getopt.hasOption('e')) {
            try {
                EquationSystem eqs = new EquationSystem();
                eqs.read(new FileReader(getopt.getValue('e')));
                ret.setOptionEliminateEquivalence(true);
                ret.setEquations(eqs);
            } catch (Exception e) {
                throw new AbstractOptionsParsingException("An error occurred while reading the equivalences file!", e, ExitCodes.EQUIVALENCE_READING_ERROR);
            }
        }


        return ret;
    }


    private static ConvenientGetopt makeConvenientGetopt() {
        ConvenientGetopt getopt = new ConvenientGetopt("utool", null, null);

        getopt.addOption('I', "input-codec", ConvenientGetopt.REQUIRED_ARGUMENT,
                "Specify the input codec", null);
        getopt.addOption('O', "output-codec", ConvenientGetopt.REQUIRED_ARGUMENT,
                "Specify the output codec", null);
        getopt.addOption(OPTION_INPUT_CODEC_OPTIONS, "input-codec-options", ConvenientGetopt.REQUIRED_ARGUMENT,
                "Specify options for the input codec", null);
        getopt.addOption(OPTION_OUTPUT_CODEC_OPTIONS, "output-codec-options", ConvenientGetopt.REQUIRED_ARGUMENT,
                "Specify options for the output codec", null);
        getopt.addOption('o', "output", ConvenientGetopt.REQUIRED_ARGUMENT,
                "Specify an output file", "-");
        getopt.addOption('e', "equivalences", ConvenientGetopt.REQUIRED_ARGUMENT,
                "Eliminate equivalence readings", null);
        getopt.addOption('h', "help", ConvenientGetopt.NO_ARGUMENT,
                "Display help information", null);
        getopt.addOption('s', "display-statistics", ConvenientGetopt.NO_ARGUMENT,
                "Display runtime statistics", null);
        getopt.addOption(OPTION_NOCHART, "nochart", ConvenientGetopt.NO_ARGUMENT,
                "Don't compute chart for 'solvable'", null);
        getopt.addOption(OPTION_VERSION, "version", ConvenientGetopt.NO_ARGUMENT,
                "Display version information", null);
        getopt.addOption('d', "display-codecs", ConvenientGetopt.NO_ARGUMENT,
                "Display installed codecs", null);
        getopt.addOption('n', "no-output", ConvenientGetopt.NO_ARGUMENT,
                "Suppress the ordinary output", null);
        getopt.addOption('l', "logging", ConvenientGetopt.OPTIONAL_ARGUMENT,
                "Write server log to file or stderr", null);
        getopt.addOption('p', "port", ConvenientGetopt.REQUIRED_ARGUMENT,
                "Accept connections at this port", "2802");
        getopt.addOption(OPTION_WARMUP, "warmup", ConvenientGetopt.NO_ARGUMENT,
                "Warm up server after start", null);
        getopt.addOption(OPTION_HELP_OPTIONS, "help-options", ConvenientGetopt.NO_ARGUMENT,
                "Display help on options", null);
        getopt.addOption(OPTION_DUMP_CHART, "dump-chart", ConvenientGetopt.NO_ARGUMENT,
                "Display the chart after solving", null);

        return getopt;
    }


    private static Operation resolveOperation(String opstring) {
        if (opstring == null) {
            return null;
        }

        for (Operation op : Operation.values()) {
            String name = op.toString();

            if (!name.startsWith("_") && name.equals(opstring)) {
                return op;
            }
        }

        return null;
    }


    /*** codec management ***/

    private void registerAllCodecs(CodecManager codecManager) {
        try {
            codecManager.registerAllDeclaredCodecs();
        } catch (Exception e) {
            System.err.println("An error occurred trying to register a codec.");
            System.err.println(e + " (cause: " + e.getCause() + ")");

            System.exit(ExitCodes.CODEC_REGISTRATION_ERROR);
        }

    }


    private OutputCodec determineOutputCodec(ConvenientGetopt getopt, InputCodec inputCodec, String options)
            throws AbstractOptionsParsingException {
        OutputCodec outputCodec = null;

        if (getopt.hasOption('O')) {
            outputCodec = codecManager.getOutputCodecForName(getopt.getValue('O'), options);
            if (outputCodec == null) {
                throw new AbstractOptionsParsingException("Unknown output codec: " + getopt.getValue('O'),
                        ExitCodes.NO_SUCH_OUTPUT_CODEC);
            }
        }

        if (outputCodec == null) {
            outputCodec = codecManager.getOutputCodecForFilename(getopt.getValue('o'), options);
        }

        if ((outputCodec == null) && (inputCodec != null)) {
            outputCodec = codecManager.getOutputCodecForName(codecManager.getName(inputCodec), options);
        }

        return outputCodec;
    }


    private InputCodec determineInputCodec(ConvenientGetopt getopt, String argument, String options)
            throws AbstractOptionsParsingException {
        InputCodec inputCodec = null;

        if (getopt.hasOption('I')) {
            inputCodec = codecManager.getInputCodecForName(getopt.getValue('I'), options);
            if (inputCodec == null) {
                throw new AbstractOptionsParsingException("Unknown input codec: " + getopt.getValue('I'),
                        ExitCodes.NO_SUCH_INPUT_CODEC);
            }
        }

        if (inputCodec == null) {
            if (argument != null) {
                inputCodec = codecManager.getInputCodecForFilename(argument, options);
            }
        }

        return inputCodec;
    }


    public CodecManager getCodecManager() {
        return codecManager;
    }

}
