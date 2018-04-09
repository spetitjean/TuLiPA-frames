package de.duesseldorf.rrg.parser;

public enum Operation {
    GOON, // move in the same elementary tree
    SUBSTITUTE, // perform a substitution
    LEFTADJOIN, // perform a sister adjunction to the left
    RIGHTADJOIN, // perform a sister adjunction to the right
    MOVEUP, // move up
    COMBINESIS, // combine sisters
    NLS, // no left sister
    SCAN; // scan
}
