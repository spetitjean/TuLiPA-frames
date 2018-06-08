package de.duesseldorf.rrg.parser;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;

import de.tuebingen.util.Pair;

public class NewParseForestExtractor {

    private SimpleRRGParseChart parseChart;

    // idea: walk through the agenda. When scan rule is applied, a tree is fully
    // derived.
    private Map<ParseItem, Set<Pair>> extractionAgenda;

    public NewParseForestExtractor(SimpleRRGParseChart parseChart,
            List<String> toksentence) {
        this.parseChart = parseChart;
        this.extractionAgenda = new ConcurrentSkipListMap<ParseItem, Set<Pair>>();

    }

}
