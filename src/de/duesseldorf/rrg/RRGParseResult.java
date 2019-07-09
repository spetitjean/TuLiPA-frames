package de.duesseldorf.rrg;

import java.util.HashSet;
import java.util.Set;

public class RRGParseResult {

    private Set<RRGParseTree> successfulParses;
    private Set<RRGParseTree> treesWithEdgeFeatureMismatches;

    private RRGParseResult(Set<RRGParseTree> successfulParses,
            Set<RRGParseTree> treesWithEdgeFeatureMismatches) {
        this.successfulParses = successfulParses;
        this.treesWithEdgeFeatureMismatches = treesWithEdgeFeatureMismatches;
    }

    public Set<RRGParseTree> getSuccessfulParses() {
        return successfulParses;
    }

    public Set<RRGParseTree> getTreesWithEdgeFeatureMismatches() {
        return treesWithEdgeFeatureMismatches;
    }

    public static class Builder {

        private Set<RRGParseTree> successfulParses = new HashSet<>();
        private Set<RRGParseTree> treesWithEdgeFeatureMismatches = new HashSet<>();

        public Builder() {
        }

        public Builder successfulParses(Set<RRGParseTree> successfulParses) {
            this.successfulParses = successfulParses;
            return this;
        }

        public Builder treesWithEdgeFeatureMismatches(
                Set<RRGParseTree> treesWithEdgeFeatureMismatches) {
            this.treesWithEdgeFeatureMismatches = treesWithEdgeFeatureMismatches;
            return this;
        }

        public RRGParseResult build() {
            return new RRGParseResult(successfulParses,
                    treesWithEdgeFeatureMismatches);
        }
    }
}
