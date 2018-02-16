package de.duesseldorf.rrg.parser;

public interface ParseItem {

    /**
     * 
     */
    @Override
    public int hashCode();

    /**
     * Make sure that the items are immutable when putting them in a chart!
     * 
     * @return
     */
    public boolean equals();

    /**
     * 
     * @return a String representation of the item, e.g. in [brackets].
     */
    public String toString();
}
