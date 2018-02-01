/*
 *  File XMLTreeDisplay.java
 *
 *  Authors:
 *     Johannes Dellert  <johannes.dellert@sfs.uni-tuebingen.de>
 *     David Arps <david.arps@hhu.de>
 *          
 *  Copyright:
 *     Johannes Dellert, 2007
 *     David Arps, 2017
 *
 *  Last modified:
 *     2017
 *
 *  This file is part of the TuLiPA-frames system
 *     https://github.com/spetitjean/TuLiPA-frames
 *
 *  TuLiPA is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  TuLiPA is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package de.tuebingen.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.w3c.dom.Document;

import de.tuebingen.expander.DOMderivationBuilder;
import de.tuebingen.gui.tree.view.TreeViewPanel;
import de.tuebingen.util.XMLUtilities;

public class XMLTreeDisplay extends JFrame implements ListSelectionListener,
        ActionListener, FocusListener, MouseListener {
    // serialVersionUID to avoid warning
    private static final long serialVersionUID = 1L;
    // added sentence parsed
    String sentence;

    ArrayList<ParseTreeCollection> trees;

    ArrayList<Integer> selectedDisplays;

    JTabbedPane displayTab;

    ArrayList<JScrollPane> displayPanes;

    JPanel listsPane;

    JScrollPane parseListPane;
    JScrollPane eTreeListPane;
    JScrollPane stepListPane;
    JScrollPane semTextPane;
    JScrollPane framePane;

    JList parseList;
    JList eTreeList;
    JList stepList;
    JTextPane semText;

    int displayTreeID;
    int eTreeID;
    int stepID;

    // for displaying dynamically
    int availableHeight;
    int availableWidth;

    JMenuBar menuBar;
    JMenu fileMenu;
    // JMenuItem loadMenuItem;
    JMenuItem dumpMenuItem;
    JMenuItem closeMenuItem;

    JMenu treeMenu;

    JMenu helpMenu;
    JMenuItem aboutMenuItem;

    // view extensions for different tree displays
    FancyAttributeDisplayExtension attributeExtension;

    boolean displayElementaryTrees = true;

    public XMLTreeDisplay(ArrayList<ParseTreeCollection> trees) {
        this.getContentPane().setLayout(new GridLayout(1, 0));
        this.trees = trees;
        initializeDisplay();
        this.addFocusListener(this);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    public XMLTreeDisplay(String s, ArrayList<ParseTreeCollection> trees) {
        this(trees);
        sentence = s;
    }

    public void initializeDisplay() {
        // ---- to get the dimensions of the screen ---- //
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension dimension = toolkit.getScreenSize();
        availableHeight = dimension.height;
        availableWidth = dimension.width;
        // --------------------------------------------- //

        attributeExtension = new FancyAttributeDisplayExtension();

        this.selectedDisplays = new ArrayList<Integer>();
        for (int i = 0; i < trees.size(); i++) {
            selectedDisplays.add(0);
        }
        this.displayTab = new JTabbedPane();

        // create the selection menu for the parses
        this.parseList = new JList(trees.toArray());
        parseList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        parseList.addListSelectionListener(this);
        parseList.addMouseListener(this);
        this.displayTreeID = 0;
        this.parseListPane = new JScrollPane(parseList);
        this.parseListPane
                .setBorder(BorderFactory.createTitledBorder("parses"));

        // create the selection menu for elementary trees
        if (trees.get(displayTreeID).elementaryTrees != null) {
            this.eTreeList = new JList(
                    trees.get(displayTreeID).elementaryTrees.toArray());
            eTreeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            eTreeList.addListSelectionListener(this);
            this.eTreeID = -1;
            this.eTreeListPane = new JScrollPane(eTreeList);
            this.eTreeListPane.setBorder(BorderFactory.createTitledBorder(
                    "elementary trees (no variable renaming!)"));
        }

        // create the selection menu for the derivation steps
        if (trees.get(displayTreeID).derivationSteps != null) {
            this.stepList = new JList(
                    trees.get(displayTreeID).derivationSteps.toArray());
            stepList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            stepList.addListSelectionListener(this);
            this.stepID = -1;
            this.stepListPane = new JScrollPane(stepList);
            this.stepListPane.setBorder(
                    BorderFactory.createTitledBorder("derivation steps"));
        }

        // create the Text Panel at the bottom (for semantic information)
        semText = new JTextPane();
        semText.setContentType("text/html");
        semText.setText("<b>" + trees.get(displayTreeID).semantics + "</b>");
        this.semTextPane = new JScrollPane(semText);

        buildStandardDisplay();

        // build the menu with all its components...

        this.menuBar = new JMenuBar();

        this.fileMenu = new JMenu("File");
        // this.loadMenuItem = new JMenuItem("Load...");
        this.dumpMenuItem = new JMenuItem("Dump XML");
        this.closeMenuItem = new JMenuItem("Close");
        // loadMenuItem.addActionListener(this);
        dumpMenuItem.addActionListener(this);
        closeMenuItem.addActionListener(this);
        // fileMenu.add(loadMenuItem);
        fileMenu.add(dumpMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(closeMenuItem);

        this.treeMenu = new JMenu("Tree");
        JMenuItem expandAllAttributesMenuItem = new JMenuItem(
                "Expand all attributes");
        expandAllAttributesMenuItem.addActionListener(this);
        JMenuItem collapseAllAttributesMenuItem = new JMenuItem(
                "Collapse all attributes");
        collapseAllAttributesMenuItem.addActionListener(this);
        JMenuItem expandAllNodesMenuItem = new JMenuItem("Expand all nodes");
        expandAllNodesMenuItem.addActionListener(this);
        JMenuItem collapseAllNodesMenuItem = new JMenuItem(
                "Collapse all nodes");
        collapseAllNodesMenuItem.addActionListener(this);
        JMenuItem increaseVerticalNodeDistanceMenuItem = new JMenuItem(
                "Increase vertical node distance");
        increaseVerticalNodeDistanceMenuItem.addActionListener(this);
        JMenuItem decreaseVerticalNodeDistanceMenuItem = new JMenuItem(
                "Decrease vertical node distance");
        decreaseVerticalNodeDistanceMenuItem.addActionListener(this);
        JMenuItem increaseHorizontalNodeDistanceMenuItem = new JMenuItem(
                "Increase horizontal node distance");
        increaseHorizontalNodeDistanceMenuItem.addActionListener(this);
        JMenuItem decreaseHorizontalNodeDistanceMenuItem = new JMenuItem(
                "Decrease horizontal node distance");
        decreaseHorizontalNodeDistanceMenuItem.addActionListener(this);
        JMenuItem toggleEdgyLinesMenuItem = new JMenuItem("Toggle edgy lines");
        toggleEdgyLinesMenuItem.addActionListener(this);
        JMenuItem savePaneToFileMenuItem = new JMenuItem(
                "Save tree display ...");
        savePaneToFileMenuItem.addActionListener(this);
        treeMenu.add(expandAllAttributesMenuItem);
        treeMenu.add(collapseAllAttributesMenuItem);
        treeMenu.addSeparator();
        treeMenu.add(expandAllNodesMenuItem);
        treeMenu.add(collapseAllNodesMenuItem);
        treeMenu.addSeparator();
        treeMenu.add(increaseVerticalNodeDistanceMenuItem);
        treeMenu.add(decreaseVerticalNodeDistanceMenuItem);
        treeMenu.addSeparator();
        treeMenu.add(increaseHorizontalNodeDistanceMenuItem);
        treeMenu.add(decreaseHorizontalNodeDistanceMenuItem);
        treeMenu.addSeparator();
        treeMenu.add(toggleEdgyLinesMenuItem);
        treeMenu.addSeparator();
        treeMenu.add(savePaneToFileMenuItem);

        this.helpMenu = new JMenu("Help");
        this.aboutMenuItem = new JMenuItem("About");
        aboutMenuItem.addActionListener(this);
        helpMenu.add(aboutMenuItem);

        menuBar.add(fileMenu);
        menuBar.add(treeMenu);
        menuBar.add(helpMenu);
        this.setJMenuBar(menuBar);

        listsPane = new JPanel();
        listsPane.setLayout(new GridLayout(0, 1));
        listsPane.add(parseListPane);
        if (eTreeListPane != null) {
            listsPane.add(eTreeListPane);
        }
        if (stepListPane != null) {
            listsPane.add(stepListPane);
        }

        // TODO GUI for the frames
        // define the frame display
        framePane = new JScrollPane();
        framePane.setMinimumSize(
                new Dimension(availableWidth / 10, availableHeight / 10));
        framePane.setBackground(getBackground());

        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                displayTab, semTextPane);
        rightSplit.setResizeWeight(0.8);
        rightSplit.setDividerLocation(0.8);

        // for the frame display
        JSplitPane frameSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                rightSplit, framePane);
        frameSplit.setDividerLocation(0.6);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                listsPane, rightSplit);
        mainSplit.setResizeWeight(0.2);
        rightSplit.setDividerLocation(0.2);
        this.getContentPane().add(mainSplit);

        repaint();
    }

    public void updateDisplay() {
        this.displayTab.removeAll();
        parseList.setListData(trees.toArray());
        buildStandardDisplay();
        if (trees.size() < 2) {
            parseListPane.setVisible(false);
        } else {
            parseListPane.setVisible(true);
        }
        repaint();
    }

    public void valueChanged(ListSelectionEvent e) {

        if (e.getSource() == parseList) {
            int displayID = parseList.getSelectedIndex();
            int oldTabSelection = this.displayTab.getSelectedIndex();
            this.displayTab.removeAll();
            buildStandardDisplay();
            selectedDisplays.set(displayTreeID, oldTabSelection);
            displayTreeID = displayID;
            displayTab.setSelectedIndex(selectedDisplays.get(displayID));
            if (eTreeList != null) {
                eTreeList.setListData(
                        trees.get(displayTreeID).elementaryTrees.toArray());
            }
            if (stepList != null) {
                stepList.setListData(
                        trees.get(displayTreeID).derivationSteps.toArray());
            }
            String semPaneText = "<b>" + trees.get(displayTreeID).semantics
                    + "</b>";
            semText.setText(semPaneText);
        }

        if (e.getSource() == eTreeList) {
            eTreeID = eTreeList.getSelectedIndex();
            if (eTreeID >= 0) {

                int oldTabSelection = this.displayTab.getSelectedIndex();
                if (this.displayTab.getComponentCount() > 1) {
                    selectedDisplays.set(displayTreeID, oldTabSelection);
                }

                this.displayTab.removeAll();

                TreeViewPanel eTreeDisplay = new TreeViewPanel();
                eTreeDisplay
                        .setMouseListener(new XMLTreeListener(eTreeDisplay));
                eTreeDisplay.viewExtensionsAfterMainRendering
                        .add(attributeExtension);
                eTreeDisplay.t = trees.get(displayTreeID).elementaryTrees
                        .get(eTreeID);
                JScrollPane eTreeDisplayPane = new JScrollPane(eTreeDisplay);
                displayTab.add(((XMLViewTree) eTreeDisplay.t).description,
                        eTreeDisplayPane);

                semText.setText("<b>" + trees.get(displayTreeID).elementaryTrees
                        .get(eTreeID).prettySem + "</b>");
            }
        }
        if (e.getSource() == stepList) {
            stepID = stepList.getSelectedIndex();
            if (stepID >= 0) {

                int oldTabSelection = this.displayTab.getSelectedIndex();
                if (this.displayTab.getComponentCount() > 1) {
                    selectedDisplays.set(displayTreeID, oldTabSelection);
                }

                this.displayTab.removeAll();

                TreeViewPanel stepDisplay = new TreeViewPanel();
                stepDisplay.setMouseListener(new XMLTreeListener(stepDisplay));
                stepDisplay.viewExtensionsAfterMainRendering
                        .add(attributeExtension);
                stepDisplay.t = trees.get(displayTreeID).derivationSteps
                        .get(stepID);
                JScrollPane stepDisplayPane = new JScrollPane(stepDisplay);
                displayTab.add(((XMLViewTree) stepDisplay.t).description,
                        stepDisplayPane);
                semText.setText("<b>" + trees.get(displayTreeID).derivationSteps
                        .get(stepID).prettySem + "</b>");
            }
        }
        repaint();
    }

    // code doubling to allow returning to parse tree view by clicking without
    // selection change
    // to be optimized if a better code structure becomes available in the next
    // major revision
    public void mouseClicked(MouseEvent e) {
        if (e.getSource() == parseList) {
            int displayID = parseList.getSelectedIndex();
            int oldTabSelection = this.displayTab.getSelectedIndex();
            this.displayTab.removeAll();
            buildStandardDisplay();
            selectedDisplays.set(displayTreeID, oldTabSelection);
            displayTreeID = displayID;
            displayTab.setSelectedIndex(selectedDisplays.get(displayID));
            if (eTreeList != null) {
                eTreeList.setListData(
                        trees.get(displayTreeID).elementaryTrees.toArray());
            }
            if (stepList != null) {
                stepList.setListData(
                        trees.get(displayTreeID).derivationSteps.toArray());
            }
            semText.setText(
                    "<b>" + trees.get(displayTreeID).semantics + "</b>");
        }
    }

    // have to be implemented because of MouseListener interface
    public void mousePressed(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    /**
     * Method for building the fields for derivation tree and derived tree
     */
    public void buildStandardDisplay() {
        if (trees.size() > 0) {
            // show derivation tree
            TreeViewPanel derivationDisplay = new TreeViewPanel();
            derivationDisplay
                    .setMouseListener(new XMLTreeListener(derivationDisplay));
            derivationDisplay.t = trees.get(displayTreeID).derivationTree;
            JScrollPane derivationDisplayPane = new JScrollPane(
                    derivationDisplay);
            displayTab.add("Derivation tree", derivationDisplayPane);

            // show derived tree
            TreeViewPanel derivedDisplay = new TreeViewPanel();
            derivedDisplay
                    .setMouseListener(new XMLTreeListener(derivedDisplay));
            derivedDisplay.viewExtensionsAfterMainRendering
                    .add(attributeExtension);
            derivedDisplay.t = trees.get(displayTreeID).derivedTree;
            JScrollPane derivedDisplayPane = new JScrollPane(derivedDisplay);
            displayTab.add("Derived tree", derivedDisplayPane);
        }
    }

    public void actionPerformed(ActionEvent e) {
        String s = e.getActionCommand();
        if (s.equals("Load...")) {
            /*
             * JFileChooser chooser = new JFileChooser(new File("."));
             * chooser.showOpenDialog(this);
             * File xmlFile = chooser.getSelectedFile();
             * trees =
             * DerivationTreeViewer.getViewkFromFile(xmlFile.getAbsolutePath
             * ());
             * updateDisplay();
             */
        } else if (s.equals("Dump XML")) {
            JFileChooser chooser = new JFileChooser(new File("."));
            chooser.showSaveDialog(this);
            File xmlFile = chooser.getSelectedFile();

            Document dparses = new DOMderivationBuilder(sentence)
                    .buildDOMderivation(trees);
            XMLUtilities.writeXML(dparses, xmlFile.getAbsolutePath(),
                    "tulipa-parses.dtd,xml", true);
        } else if (s.equals("Close")) {
            this.dispose();
        } else if (s.equals("About")) {
            String title = "TuLiPa tree viewer";
            String message = "A graphical user interface to explore parses in the TAG and TT-MCTAG formalisms\n";
            message += "developed at Tuebingen University in connection with the TuLiPa project.\n";
            message += "                                  (c) Johannes Dellert 2007-09";
            JOptionPane.showMessageDialog(this, message, title,
                    JOptionPane.INFORMATION_MESSAGE);
        } else if (s.equals("Expand all attributes")) {
            ((XMLViewTree) currentPanel().t).expandAllAttributes();
            currentPanel().repaint();
        } else if (s.equals("Collapse all attributes")) {
            ((XMLViewTree) currentPanel().t).collapseAllAttributes();
            currentPanel().repaint();
        } else if (s.equals("Expand all nodes")) {
            ((XMLViewTree) currentPanel().t).expandAllNodes();
            currentPanel().repaint();
        } else if (s.equals("Collapse all nodes")) {
            ((XMLViewTree) currentPanel().t).collapseAllNodes();
            currentPanel().repaint();
        } else if (s.equals("Increase vertical node distance")) {
            currentPanel().t.increaseVerticalNodeDistance();
            currentPanel().repaint();
        } else if (s.equals("Decrease vertical node distance")) {
            currentPanel().t.decreaseVerticalNodeDistance();
            currentPanel().repaint();
        } else if (s.equals("Increase horizontal node distance")) {
            currentPanel().t.increaseHorizontalNodeDistance();
            currentPanel().repaint();
        } else if (s.equals("Decrease horizontal node distance")) {
            currentPanel().t.decreaseHorizontalNodeDistance();
            currentPanel().repaint();
        } else if (s.equals("Toggle edgy lines")) {
            currentPanel().toggleEdgyLines();
            currentPanel().repaint();
        } else if (s.equals("Save tree display ...")) {
            JFileChooser chooser = new JFileChooser(new File("."));
            // FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG
            // Images", "png");
            // chooser.setFileFilter(filter);
            chooser.showSaveDialog(this);
            File outputFile = chooser.getSelectedFile();

            BufferedImage outputImage = new BufferedImage(
                    currentPanel().getWidth(), currentPanel().getHeight(),
                    BufferedImage.TYPE_INT_ARGB);
            Graphics outputCanvas = outputImage.getGraphics();
            currentPanel().paint(outputCanvas);
            try {
                ImageIO.write(outputImage, "png", outputFile);
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(this, ioe.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void focusLost(FocusEvent e) {
        repaint();
    }

    public void focusGained(FocusEvent e) {
        repaint();
    }

    public TreeViewPanel currentPanel() {
        // dirty: there should be a better way to access tree panels
        return (TreeViewPanel) ((JScrollPane) displayTab
                .getComponent(displayTab.getSelectedIndex())).getComponent(0)
                        .getComponentAt(0, 0);
    }
}