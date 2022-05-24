package de.tuebingen.semantics;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import de.saar.chorus.domgraph.chart.Chart;
import de.saar.chorus.domgraph.chart.ChartSolver;
import de.saar.chorus.domgraph.chart.SolvedFormIterator;
import de.saar.chorus.domgraph.codec.MalformedDomgraphException;
import de.saar.chorus.domgraph.codec.MultiOutputCodec;
import de.saar.chorus.domgraph.equivalence.IndividualRedundancyElimination;
import de.saar.chorus.domgraph.equivalence.RedundancyEliminationSplitSource;
import de.saar.chorus.domgraph.graph.DomEdge;
import de.saar.chorus.domgraph.graph.DomGraph;
import de.saar.chorus.domgraph.utool.AbstractOptions;
import de.saar.chorus.domgraph.utool.AbstractOptions.Operation;
import de.tuebingen.tag.SemLit;
import de.tuebingen.util.TextUtilities;

public class UToolRunner {
    public static boolean verbose = false;
    public static StringWriter outputWriter = null;

    public static String[] process(List<SemLit> holeSemantics) {
        if (holeSemantics == null || holeSemantics.size() == 0) {
            return null;
        }
        DominanceGraph semDG = new DominanceGraph(holeSemantics);
        String uToolInput = semDG.toUtoolOzFormat();
        if (verbose)
            System.err.println(uToolInput);
        try {
            File tempFile = new File("./temp.clls");
            if (!tempFile.exists()) {
                tempFile.createNewFile();
            }
            TextUtilities.writeText(tempFile, uToolInput);

            outputWriter = new StringWriter();

            // System.setProperty( "file.encoding", "UTF-8" ); // not needed

            String[] args = {"solve", "-O", "term-prolog", "./temp.clls"};
            utoolMain(args);
            String[] output = outputWriter.toString().split("---");
            tempFile.delete();

            // System.err.println("Fully specified semantics: ");
            // System.err.println(output);

            return output;
        } catch (Exception e) {
            System.err.println("utool call failed: " + e.getMessage());
            // e.printStackTrace();
        }
        return null;
    }

    public static void utoolMain(String[] args) {

        CommandLineParser optionsParser = new CommandLineParser();
        AbstractOptions options = null;

        boolean weaklyNormal = false;
        boolean normal = false;
        boolean compact = false;
        boolean compactifiable = false;

        // parse command-line options and load graph
        /*
         * try {
         * options = optionsParser.parse(args);
         * options.setOutput(outputWriter);
         * } catch (AbstractOptionsParsingException e) {
         * System.err.print(e.comprehensiveErrorMessage());
         * }
         */

        // check statistics and compactify graph
        if (options.getOperation().requiresInput) {
            weaklyNormal = options.getGraph().isWeaklyNormal();
            normal = options.getGraph().isNormal();
            compact = options.getGraph().isCompact();
            compactifiable = options.getGraph().isCompactifiable();

            if (options.hasOptionStatistics()) {
                if (normal) {
                    System.err.println("The input graph is normal.");
                } else {
                    System.err.print("The input graph is not normal");
                    if (weaklyNormal) {
                        System.err.println(", but it is weakly normal.");
                    } else {
                        System.err.println(" (not even weakly normal).");
                    }
                }

                if (compact) {
                    System.err.println("The input graph is compact.");
                } else {
                    System.err.print("The input graph is not compact, ");
                    if (compactifiable) {
                        System.err.println("but I will compactify it for you.");
                    } else {
                        System.err.println("and it cannot be compactified.");
                    }
                }

                if (options.hasOptionEliminateEquivalence()) {
                    System.err.println("I will eliminate equivalences ("
                            + options.getEquations().size() + " equations).");
                }
            }

        }

        DomGraph compactGraph = null;

        if ((options.getOperation() == Operation.solve)
                && !(options.getOutputCodec() instanceof MultiOutputCodec)
                && !options.hasOptionNoOutput()) {
            System.err.println(
                    "This output codec doesn't support the printing of multiple solved forms!");
        }

        if (!weaklyNormal) {
            System.err
                    .println("Cannot solve graphs that are not weakly normal!");
        }

        if (!compact && !compactifiable) {
            System.err.println(
                    "Cannot solve graphs that are not compact and not compactifiable!");

        }

        // compactify if necessary
        compactGraph = options.getGraph().compactify();

        // compute chart
        Chart chart = new Chart();
        boolean solvable;

        if (options.hasOptionEliminateEquivalence()) {
            solvable = ChartSolver
                    .solve(compactGraph, chart,
                            new RedundancyEliminationSplitSource(
                                    new IndividualRedundancyElimination(
                                            compactGraph, options.getLabels(),
                                            options.getEquations()),
                                    compactGraph));

        } else {
            solvable = ChartSolver.solve(compactGraph, chart);
        }

        if (solvable) {
            MultiOutputCodec outputcodec = options.hasOptionNoOutput() ? null
                    : (MultiOutputCodec) options.getOutputCodec();

            if (options.getOperation() == Operation.solve) {
                try {
                    /*
                     * if (!options.hasOptionNoOutput()) {
                     * // Adds the initial "["
                     * outputcodec.print_header(options.getOutput());
                     * outputcodec.print_start_list(options.getOutput());
                     * }
                     */

                    // extract solved forms
                    long count = 0;
                    SolvedFormIterator it = new SolvedFormIterator(chart,
                            options.getGraph());
                    while (it.hasNext()) {
                        List<DomEdge> domedges = it.next();
                        count++;

                        if (!options.hasOptionNoOutput()) {
                            if (count > 1) {
                                /*
                                 * outputcodec.print_list_separator(options
                                 * .getOutput());
                                 */
                                options.getOutput().append("---");
                            }
                            outputcodec.encode(
                                    options.getGraph()
                                            .withDominanceEdges(domedges),
                                    options.getLabels(), options.getOutput());
                        }
                    }

                    if (!options.hasOptionNoOutput()) {
                        /*
                         * // Adds the final "]"
                         * outputcodec.print_end_list(options.getOutput());
                         * outputcodec.print_footer(options.getOutput());
                         */
                        options.getOutput().flush();
                    }

                } catch (MalformedDomgraphException e) {
                    System.err.println(
                            "Output of the solved forms of this graph is not supported by this output codec.");
                    System.err.println(e);
                } catch (IOException e) {
                    System.err.println(
                            "An error occurred while trying to print the results.");
                    System.err.println(e);
                }
            } // if operation == solve

        } else {
            // not solvable
            if (options.hasOptionStatistics()) {
                System.err.println("it is unsolvable!");
            }

        }

    }

}
