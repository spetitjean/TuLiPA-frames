package de.duesseldorf.rrg;

import de.tuebingen.tree.Node;

public class RRGTreeTools {

    /**
     * 
     * @param root
     *            the root node of the tree to be printed
     * @return A multiline string representation of <code>root</code> and its
     *         daughters
     */
    public static String recursivelyPrintNode(Node root) {
        StringBuffer sb = new StringBuffer();
        recursivelyPrintNode(root, sb, 0);
        return sb.toString();
    }

    /**
     * 
     * @param root
     *            the root node of the tree to be printed
     * @param sb
     *            The StringBuffer that is modified
     * @param sep
     *            The number of blanks needed to properly indent the nodes
     */
    private static void recursivelyPrintNode(Node root, StringBuffer sb,
            int sep) {
        // add separators
        for (int i = 0; i < sep; i++) {
            sb.append(" ");
        }
        sb.append(root.toString());
        sb.append("\n");
        for (Node node : root.getChildren()) {
            recursivelyPrintNode(node, sb, sep + 1);
        }
    }

}
