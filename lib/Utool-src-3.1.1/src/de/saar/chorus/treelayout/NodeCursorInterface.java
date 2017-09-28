package de.saar.chorus.treelayout;


/**
 * 
 * @author Marco Kuhlmann
 *
 */

public interface NodeCursorInterface {
    abstract public Object getCurrentNode();
    abstract public void processCurrentNode();
    abstract public boolean mayMoveUpwards();
    abstract public void moveUpwards();
    abstract public boolean mayMoveDownwards();
    abstract public void moveDownwards();
    abstract public boolean mayMoveSidewards();
    abstract public void moveSidewards();
}
