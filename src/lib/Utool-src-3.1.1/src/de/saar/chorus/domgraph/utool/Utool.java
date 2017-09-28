/*
 * @(#)Utool.java created 27.01.2006
 * 
 * Copyright (c) 2006 Alexander Koller
 *  
 */

package de.saar.chorus.domgraph.utool;

import java.io.IOException;
import java.util.List;

import de.saar.chorus.domgraph.GlobalDomgraphProperties;
import de.saar.chorus.domgraph.chart.Chart;
import de.saar.chorus.domgraph.chart.ChartPresenter;
import de.saar.chorus.domgraph.chart.ChartSolver;
import de.saar.chorus.domgraph.chart.OneSplitSource;
import de.saar.chorus.domgraph.chart.SolvedFormIterator;
import de.saar.chorus.domgraph.codec.MalformedDomgraphException;
import de.saar.chorus.domgraph.codec.MultiOutputCodec;
import de.saar.chorus.domgraph.equivalence.IndividualRedundancyElimination;
import de.saar.chorus.domgraph.equivalence.RedundancyEliminationSplitSource;
import de.saar.chorus.domgraph.graph.DomEdge;
import de.saar.chorus.domgraph.graph.DomGraph;
import de.saar.chorus.domgraph.utool.AbstractOptions.Operation;
import de.saar.chorus.domgraph.utool.server.ConnectionManager;
import de.saar.chorus.ubench.gui.Ubench;

/**
 * The Utool main program for accessing the Domgraph functionality
 * from the command-line. Utool ("Underspecification Tool") is the
 * Swiss Army Knife of Underspecification (Java version).<p>
 * 
 * The operation of this class is described in more detail in the
 * end-user documentation of Utool.
 * 
 * @author Alexander Koller
 *
 */
public class Utool {
    public static void main(String[] args) {
        CommandLineParser optionsParser = new CommandLineParser();
        AbstractOptions options = null;

        boolean weaklyNormal = false;
        boolean normal = false;
        boolean compact = false;
        boolean compactifiable = false;
        
        // parse command-line options and load graph
        try {
            options = optionsParser.parse(args);
        } catch(AbstractOptionsParsingException e) {
            System.err.print(e.comprehensiveErrorMessage());
            System.exit(e.getExitcode());
        }
        
        
        // check statistics and compactify graph
        if( options.getOperation().requiresInput ) {
            weaklyNormal = options.getGraph().isWeaklyNormal();
            normal = options.getGraph().isNormal();
            compact = options.getGraph().isCompact();
            compactifiable = options.getGraph().isCompactifiable();
            
            if( options.hasOptionStatistics() ) {
                if( normal ) {
                    System.err.println("The input graph is normal.");
                } else {
                    System.err.print("The input graph is not normal");
                    if( weaklyNormal ) {
                        System.err.println (", but it is weakly normal.");
                    } else {
                        System.err.println(" (not even weakly normal).");
                    }
                }
                
                if( compact ) {
                    System.err.println("The input graph is compact.");
                } else {
                    System.err.print("The input graph is not compact, ");
                    if( compactifiable ) {
                        System.err.println("but I will compactify it for you.");
                    } else {
                        System.err.println("and it cannot be compactified.");
                    }
                }
                
                if( options.hasOptionEliminateEquivalence() ) {
                    System.err.println("I will eliminate equivalences (" + options.getEquations().size() + " equations).");
                }
            }
            
        }            
        
        
        // now do something, depending on the specified operation
        switch(options.getOperation()) {
        case solvable:
            if( options.hasOptionNochart() ) {
                if( options.hasOptionStatistics() ) {
                    System.err.print("Checking graph for solvability (without chart) ... ");
                }
                
                long start_solver = System.currentTimeMillis();
                boolean solvable = OneSplitSource.isGraphSolvable(options.getGraph());
                long end_solver = System.currentTimeMillis();
                long time_solver = end_solver - start_solver;

                if( solvable ) {
                    if( options.hasOptionStatistics() ) {
                        System.err.println("it is solvable.");
                        System.err.println("Time to determine solvability: " + time_solver + " ms");
                    }
                    
                    System.exit(1);
                } else {
                    if( options.hasOptionStatistics() ) {
                        System.err.println("it is unsolvable.");
                        System.err.println("Time to determine unsolvability: " + time_solver + " ms");
                    }
                    
                    System.exit(0);
                }
            }
                
            // intentional fall-through for the non-"nochart" case
            
            
        case solve:
            DomGraph compactGraph = null;
            
            if( (options.getOperation() == Operation.solve)
            		&& ! (options.getOutputCodec() instanceof MultiOutputCodec)
            		&& ! options.hasOptionNoOutput() ) {
            	System.err.println("This output codec doesn't support the printing of multiple solved forms!");
            	System.exit(ExitCodes.OUTPUT_CODEC_NOT_MULTI);
            }
            		
            if( options.hasOptionStatistics() ) {
                System.err.println();
            }
            
            if( !weaklyNormal ) {
                System.err.println("Cannot solve graphs that are not weakly normal!");
                System.exit(ExitCodes.ILLFORMED_INPUT_GRAPH);
            }
            
            if( !compact && !compactifiable ) {
                System.err.println("Cannot solve graphs that are not compact and not compactifiable!");
                System.exit(ExitCodes.ILLFORMED_INPUT_GRAPH);
            }
            
            // compactify if necessary
            compactGraph = options.getGraph().compactify();

            if( options.hasOptionStatistics() ) {
                System.err.print("Solving graph ... ");
            }

            // compute chart
            long start_solver = System.currentTimeMillis();
            Chart chart = new Chart();
            boolean solvable;
            
            if( options.hasOptionEliminateEquivalence() ) {
                solvable = 
                    ChartSolver.solve(compactGraph, chart, 
                            new RedundancyEliminationSplitSource(
                                    new IndividualRedundancyElimination(compactGraph, 
                                            options.getLabels(), options.getEquations()), compactGraph));
                
            } else {
                solvable = ChartSolver.solve(compactGraph, chart); 
            }
            
            long end_solver = System.currentTimeMillis();
            long time_solver = end_solver - start_solver;
            
            if( solvable ) {
            	MultiOutputCodec outputcodec = 
            		options.hasOptionNoOutput() ? null : (MultiOutputCodec) options.getOutputCodec();
            	
                if( options.hasOptionStatistics() ) {
                    System.err.println("it is solvable.");
                    printChartStatistics(chart, time_solver, options.hasOptionDumpChart(), compactGraph);
                }
                
                // TODO runtime prediction (see ticket #11)
                
                if( options.getOperation() == Operation.solve ) {
                    try {
                        if( !options.hasOptionNoOutput() ) {
                            outputcodec.print_header(options.getOutput());
                            outputcodec.print_start_list(options.getOutput());
                        }
                        
                        // extract solved forms
                        long start_extraction = System.currentTimeMillis();
                        long count = 0;
                        SolvedFormIterator it = new SolvedFormIterator(chart,options.getGraph());
                        while( it.hasNext() ) {
                            List<DomEdge> domedges = it.next();
                            count++;
                            
                            if( !options.hasOptionNoOutput() ) {
                                if( count > 1 ) {
                                    outputcodec.print_list_separator(options.getOutput());
                                }
                                outputcodec.encode(options.getGraph().withDominanceEdges(domedges), options.getLabels(), options.getOutput());
                            }
                        }
                        long end_extraction = System.currentTimeMillis();
                        long time_extraction = end_extraction - start_extraction;
                        
                        if( !options.hasOptionNoOutput() ) {
                            outputcodec.print_end_list(options.getOutput());
                            outputcodec.print_footer(options.getOutput());
                            options.getOutput().flush();
                        }
                        
                        if( options.hasOptionStatistics() ) {
                            System.err.println("Found " + count + " solved forms.");
                            System.err.println("Time spent on extraction: " + time_extraction + " ms");
                            long total_time = time_extraction + time_solver;
                            System.err.print("Total runtime: " + total_time + " ms (");
                            if( total_time > 0 ) {
                                System.err.print((int) Math.floor(count * 1000.0 / total_time));
                                System.err.print(" sfs/sec; ");
                            }
                            System.err.println(1000 * total_time / count + " microsecs/sf)");
                        }
                    } catch (MalformedDomgraphException e) {
                        System.err.println("Output of the solved forms of this graph is not supported by this output codec.");
                        System.err.println(e);
                        System.exit(e.getExitcode() + ExitCodes.MALFORMED_DOMGRAPH_BASE_OUTPUT);
                    } catch (IOException e) {
                        System.err.println("An error occurred while trying to print the results.");
                        //e.printStackTrace();
                        System.exit(ExitCodes.IO_ERROR);
                    }
                } // if operation == solve
                
                System.exit(1);
            } else {
                // not solvable
                if( options.hasOptionStatistics() ) {
                    System.err.println("it is unsolvable!");
                }
                
                System.exit(0);
            }
            break;
            
        
        case convert:
            if( !options.hasOptionNoOutput() ) {
                try {
                    options.getOutputCodec().print_header(options.getOutput());
                    options.getOutputCodec().encode(options.getGraph(), options.getLabels(), options.getOutput());
                    options.getOutputCodec().print_footer(options.getOutput());
                } catch(MalformedDomgraphException e) {
                    System.err.println("This graph is not supported by the specified output codec.");
                    System.err.println(e);
                    System.exit(ExitCodes.MALFORMED_DOMGRAPH_BASE_OUTPUT + e.getExitcode());
                } catch(IOException e) {
                    System.err.println("An I/O error occurred while trying to print the results.");
                    System.err.println(e);
                    System.exit(ExitCodes.IO_ERROR);
                }
            }
            
            break;
            
            
        case classify:
            int programExitCode = 0;
            
            if( weaklyNormal ) {
                programExitCode |= ExitCodes.CLASSIFY_WEAKLY_NORMAL;
            }

            if( normal ) {
                programExitCode |= ExitCodes.CLASSIFY_NORMAL;
            }
            
            if( compact ) {
                programExitCode |= ExitCodes.CLASSIFY_COMPACT;
            }
            
            if( compactifiable ) {
                programExitCode |= ExitCodes.CLASSIFY_COMPACTIFIABLE;
            }
            
            if( options.getGraph().isHypernormallyConnected() ) {
                if( options.hasOptionStatistics() ) {
                    System.err.println("The graph is hypernormally connected.");
                }
                programExitCode |= ExitCodes.CLASSIFY_HN_CONNECTED;
            } else {
                if( options.hasOptionStatistics() ) {
                    System.err.println("The graph is not hypernormally connected.");
                }
            }
            
            if( options.getGraph().isLeafLabelled() ) {
                if( options.hasOptionStatistics() ) {
                    System.err.println("The graph is leaf-labelled.");
                }
                programExitCode |= ExitCodes.CLASSIFY_LEAF_LABELLED;
            } else {
                if( options.hasOptionStatistics() ) {
                    System.err.println("The graph is not leaf-labelled.");
                }
            }

            System.exit(programExitCode);
            
        case display:
            if( options.getGraph() != null ) {
                Ubench.getInstance().addNewTab(
                        options.getInputName(),
                        options.getGraph(),
                        options.getLabels());
            } else {
                Ubench.getInstance();
            }
            
            break;
            
        case server:
            try {
                ConnectionManager.startServer(options);
            } catch(IOException e) {
                System.err.println("An I/O error occurred while running the server: " + e);
                System.exit(ExitCodes.SERVER_IO_ERROR);
            }
            System.exit(0);
            
        case help:
            displayHelp(options.getHelpArgument());
            System.exit(0);
            
        case _helpOptions:
            displayHelpOptions();
            System.exit(0);
            
        case _displayCodecs:
            optionsParser.getCodecManager().displayAllCodecs(System.out);
            System.exit(0);
            
        case _version:
            displayVersion();
            System.exit(0);

        }
    }
    
    
    
    



    private static void printChartStatistics(Chart chart, long time, boolean dumpChart, DomGraph compactGraph) {
        System.err.println("Splits in chart: " + chart.size());
        if( dumpChart ) {
            //System.err.println(chart);
            System.err.println(ChartPresenter.chartOnlyRoots(chart, compactGraph));
        }
        
        if( time != -1 ) {
            System.err.println("Time to build chart: " + time + " ms");
        }
        
        System.err.println("Number of solved forms: " + chart.countSolvedForms());
        System.err.println("");
    }




    private static void displayHelp(Operation op) {
        if( (op == null) || (op.longDescription == null) ) {
            System.err.println("Usage: java -jar Utool.jar <subcommand> [options] [args]");
            System.err.println("Type `utool help <subcommand>' for help on a specific subcommand.");
            System.err.println("Type `utool --help-options' for a list of global options.");
            System.err.println("Type `utool --display-codecs' for a list of supported codecs.\n");
            
            System.err.println("Available subcommands:");
            for( Operation _op : Operation.values() ) {
                if( _op.shortDescription != null ) {
                    System.err.println(String.format("    %1$-12s %2$s.",
                            _op, _op.shortDescription));
                }
            }

            System.err.println("\nUtool is the Swiss Army Knife of Underspecification (Java version).");
            System.err.println("For more information, see " + GlobalDomgraphProperties.getHomepage());
        } else {
            System.err.println("utool " + op + ": " + op.shortDescription + ".");
            System.err.println(op.longDescription);
        }
    }

    private static void displayVersion() {
        System.err.println("Utool (The Swiss Army Knife of Underspecification), "
                + "version " + GlobalDomgraphProperties.getVersion());
        System.err.println("Created by the CHORUS project, SFB 378, Saarland University");
        System.err.println();
    }


    private static void displayHelpOptions() {
        System.err.println("utool global options are:");
        System.err.println("  --help-options                    Displays this information about global options.");;
        System.err.println("  --display-codecs, -d              Displays all input and output filters.");
        System.err.println("  --display-statistics, -s          Displays runtime and other statistics.");
        System.err.println("  --no-output, -n                   Do not display computed output.");
        System.err.println("  --equivalences, -e <filename>     Eliminate equivalent readings.");
        System.err.println("  --version                         Display version and copyright information.");
    }

}



/*




// prepare codecs
codecManager = new CodecManager();
registerAllCodecs(codecManager);

// parse command line options
ConvenientGetopt getopt = makeConvenientGetopt();
getopt.parse(args);

// determine operation and filename
List<String> rest = getopt.getRemaining();

if( !rest.isEmpty() ) {
    op = resolveOperation(rest.get(0));
}

// XXX - warum nicht iterieren? (stth)
if( rest.size() > 1 ) {
    argument = rest.get(1);
}


// handle special commands
if( getopt.hasOption('d')) {
    codecManager.displayAllCodecs(System.out);
    System.exit(0);
}

if( getopt.hasOption('h') ) {
    displayHelp(op);
    System.exit(0);
}

if( op == Operation.help ) {
    displayHelp(resolveOperation(argument));
    System.exit(0);
}

if( getopt.hasOption(OPTION_HELP_OPTIONS)) {
    displayHelpOptions();
    System.exit(0);
}

if( getopt.hasOption(OPTION_VERSION)) {
    displayVersion();
    System.exit(0);
}



// at this point, we must have an operation
if( op == null ) {
    displayHelp(null);
    System.exit(ExitCodes.NO_SUCH_COMMAND);
}


// determine input and output codecs
inputCodec = determineInputCodec(getopt, argument);

if( inputCodec == null ) {
    System.err.println("You must specify an input codec!");
}

outputCodec = determineOutputCodec(getopt, inputCodec);


// parse the global options
outputname = getopt.getValue('o');

if( getopt.hasOption('s')) {
    displayStatistics = true;
}

if( getopt.hasOption('n')) {
    noOutput = true;
}

if( getopt.hasOption(OPTION_DUMP_CHART)) {
    dumpChart = true;
}

if( getopt.hasOption('e') ) {
    try {
        eqs = new EquationSystem();
        eqs.read(new FileReader(getopt.getValue('e')));
        eliminateEquivalences = true;
        //System.err.println("Equation system:\n" + eqs);
    } catch(Exception e) {
        System.err.println("An error occurred while reading the equivalences file!");
        e.printStackTrace(System.err);
        System.exit(ExitCodes.EQUIVALENCE_READING_ERROR);
    }
}


// obtain graph
try {
    if( argument == null ) {
        argument = "-"; // stdin
    }
    
    inputCodec.decodeFile(argument, graph, labels);
} catch(MalformedDomgraphException e ) {
    System.err.println("A semantic error occurred while decoding the graph.");
    System.err.println(e);
    System.exit(ExitCodes.MALFORMED_DOMGRAPH_BASE_INPUT + e.getExitcode());
} catch(ParserException e) {
    System.err.println("A parsing error occurred while reading the input.");
    System.err.println(e);
    System.exit(ExitCodes.PARSER_ERROR);
} catch(IOException e) {
    System.err.println("An I/O error occurred while reading the input.");
    System.err.println(e);
    System.exit(ExitCodes.IO_ERROR);
}

if( displayStatistics ) {
    System.err.println("The input graph has " + graph.getAllRoots().size() + " fragments.");
}





    private static final char OPTION_VERSION = (char) 1;
    private static final char OPTION_HELP_OPTIONS = (char) 2;
    private static final char OPTION_DUMP_CHART = (char) 3;
       
    private enum Operation {
        solve      
        ("Solve an underspecified description",
                "Usage: utool solve [options] [input-source]\n\n" +
                "By default, reads an underspecified description from standard input\n" +
                "and writes the solutions to standard output. An alternative input source\n" +
                "can be specified on the command line; an alternative filename for the\n" +
                "output can be specified with the -o option. The input and output codecs\n" +
                "can be specified with the -I and -O options. If only an input codec\n" +
                "is specified, and an output codec of the same name exists, this codec\n" +
                "is used for output. `utool --display-codecs' will display a list of\n" +
                "input and output codecs.\n\n" +
                "Valid options:\n" +
                "  --output, -o filename           Write solutions to a file.\n" +
                "  --input-codec, -I codecname     Specify the input codec.\n" +
                "  --output-codec, -O codecname    Specify the output codec (default: same as input).\n"),
                
        solvable   
        ("Check solvability without enumerating solutions",
                "Usage: utool solvable [options] [input-source]\n\n" +
                "This command checks whether an underspecified description is solvable.\n" +
                "If it is, utool terminates with an exit code of 0; if it isn't, it terminates\n" +
                "with an exit code of 1.\n\n" +
                "The \'solvable\' command computes the total number of solved forms (= readings),\n" +
                "but not the solved forms themselves (use \'solve\' if you want them). This makes\n" +
                "\'solvable\' run much, much faster than \'solve\'. utool will display the total\n" +
                "number of solved forms if you run \'utool solvable -s\'.\n\n" +
                "By default, reads an underspecified description from standard input\n" +
                "and writes the solutions to standard output. An alternative input source\n" +
                "can be specified on the command line; an alternative filename for the\n" +
                "output can be specified with the -o option. The input codec\n" +
                "can be specified with the -I option. `utool --display-codecs'\n" +
                "will display a list of valid input codecs.\n\n" +
                "Valid options:\n" +
                "  --input-codec, -I codecname     Specify the input codec.\n"),
                
                
        convert    
        ("Convert underspecified description from one format to another",
                "Usage: utool convert -I inputcodec -O outputcodec [options] [input-source]\n\n" +
                "By default, reads an underspecified description from standard input\n" +
                "and writes it (in a different format) to standard output. An alternative\n" +
                "input source can be specified on the command line; an alternative filename\n" +
                "for the output can be specified with the -o option. The input and output\n" +
                "codecs can be specifieid with the -I and -O options. `utool --display-codecs'\n" +
                "will display a list of input and output codecs.\n\n" +
                "Valid options:\n" +
                "  --output, -o filename           Write solutions to a file.\n" +
                "  --input-codec, -I codecname     Specify the input codec.\n" +
                "  --output-codec, -O codecname    Specify the output codec (default: same as input).\n"),

                
        classify   
        ("Check whether a description belongs to special classes",
                "Usage: utool classify [options] [input-source]\n\n" +
                "This command checks whether an underspecified description belongs to a\n" +
                "class with special properties. A call to \'utool classify\' returns an\n" +
                "exit code that is the OR combination of some of the following values:\n\n" +
                "    1   the description is a weakly normal dominance graph\n" +
                "    2   the description is a normal dominance graph\n" +
                "    4   the description is compact\n" +
                "    8   the description can be compactified (or is already compact)\n" +
                "   16   the description is hypernormally connected\n" +
                "   32   the description is leaf-labelled\n\n" +
                "For example, the exit code for a graph that is hypernormally connected\n" +
                "and normal (and hence compactifiable), but not compact, would be 27.\n\n" +
                "Note that the notion of hypernormal connectedness only makes sense\n" +
                "for normal graphs (although utool will test for it anyway).\n\n" +
                "Valid options:\n" +
                "  --input-codec, -I codecname     Specify the input codec.\n"),                
                
        help       
        ("Display help on a command",
                "Usage: utool help [command]\n\n" +
                "Without any further parameters, \'utool help\' displays a list of available\n" +
                "commands. Alternatively, pass one of the command names to \'utool help\' as the\n" +
                "second parameter to get command-specific help for this command.\n")

                ;

        
        public String shortDescription, longDescription;
        
        Operation(String shortDescription, String longDescription) {
            this.shortDescription = shortDescription;
            this.longDescription = longDescription;
        }
    }
    


    private static CodecManager codecManager;
    


        InputCodec inputCodec = null;
        OutputCodec outputCodec = null;
        String argument = null;
        Writer output = new OutputStreamWriter(System.out);
        String outputname;
        boolean displayStatistics = false;
        boolean noOutput = false;
        DomGraph graph = new DomGraph();
        NodeLabels labels = new NodeLabels();
        boolean dumpChart = false;
        
        // equivalence/redundancy elimination
        boolean eliminateEquivalences = false;
        EquationSystem eqs = null;
        RedundancyElimination elim = null;

            
            (solve)
            if( !noOutput && (outputCodec == null )) {
                System.err.println("No output codec specified!");
                System.exit(ExitCodes.NO_OUTPUT_CODEC_SPECIFIED);
            }
            
                elim = new IndividualRedundancyElimination(graph, labels, eqs);
        

*/