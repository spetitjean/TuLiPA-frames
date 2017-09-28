package de.saar.chorus.treelayout;

/**
 * 
 * @author Marco Kuhlmann
 *
 */
public class PostOrderNodeVisitor extends DefaultNodeVisitor {
    
    public PostOrderNodeVisitor(NodeCursorInterface theCursor) {
        super(theCursor);
        moveToLeaf();
    }
    
    private void moveToLeaf() {
        while (cursor.mayMoveDownwards()) {
            cursor.moveDownwards();
        }
    }
    
    public boolean next() {
        cursor.processCurrentNode();
        if (cursor.mayMoveSidewards()) {
            cursor.moveSidewards();
            moveToLeaf();
        } else if (cursor.mayMoveUpwards()) {
            cursor.moveUpwards();
        } else {
            cursor = null;
        }
        return (cursor != null);
    }

}
