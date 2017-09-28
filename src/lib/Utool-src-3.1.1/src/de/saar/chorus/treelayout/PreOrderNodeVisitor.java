package de.saar.chorus.treelayout;

/**
 * 
 * @author Marco Kuhlmann
 *
 */
public class PreOrderNodeVisitor extends DefaultNodeVisitor {
    
    public PreOrderNodeVisitor(NodeCursorInterface theCursor) {
        super(theCursor);
    }
    
    private void backtrack() {
        while (! cursor.mayMoveSidewards() && cursor.mayMoveUpwards()) {
            cursor.moveUpwards();
        }
        if (! cursor.mayMoveUpwards()) {
            cursor = null;
        } else {
            cursor.moveSidewards();
        }
    }
    
    public boolean next() {
        cursor.processCurrentNode();
        if (cursor.mayMoveDownwards()) {
            cursor.moveDownwards();
        } else if (cursor.mayMoveSidewards()) {
            cursor.moveSidewards();
        } else {
            backtrack();
        }
        return (cursor != null);
    }
        
}
