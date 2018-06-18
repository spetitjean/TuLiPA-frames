package de.duesseldorf.rrg.parser;

public interface ParseItem {

    /**
     * 
     * @return the index of the first input part covered by the item
     */
    public int startPos();

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
    public boolean equals(Object o);

    /**
     * 
     * @return a String representation of the item, e.g. in [brackets].
     */
    public String toString();

}
