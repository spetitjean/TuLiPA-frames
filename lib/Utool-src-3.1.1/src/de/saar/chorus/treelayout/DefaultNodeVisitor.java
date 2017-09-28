package de.saar.chorus.treelayout;

/**
 * 
 * @author Marco Kuhlmann
 *
 */

abstract public class DefaultNodeVisitor implements NodeVisitorInterface {
    
    protected NodeCursorInterface cursor;
    
    public DefaultNodeVisitor(NodeCursorInterface theCursor) {
        this.cursor = theCursor;
    }
    
    public void setCursor(NodeCursorInterface theCursor) {
        this.cursor = theCursor;
    }
    
    public NodeCursorInterface getCursor() {
        return cursor;
    }
    
    abstract public boolean next();
    
    public void run() {
        while (next());
    }

}
