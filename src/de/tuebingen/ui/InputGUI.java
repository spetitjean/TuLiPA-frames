/*
 *  File InputGUI.java
 *
 *  Authors:
 *     Wolfgang Maier  <wo.maier@uni-tuebingen.de>
 *     Yannick Parmentier <parmenti@sfs.uni-tuebingen.de>
 *     David Arps <david.arps@hhu.de>
 *     
 *  Copyright:
 *     Wolfgang Maier, 2007
 *     Yannick Parmentier, 2007
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
package de.tuebingen.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.net.URL;
import java.util.Enumeration;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import de.duesseldorf.frames.Situation;
import de.duesseldorf.ui.CommandLineProcesses;
import de.duesseldorf.ui.ParsingInterface;
import de.duesseldorf.ui.WorkbenchLoader;
import de.tuebingen.tag.TTMCTAG;
import de.tuebingen.tree.Grammar;

public class InputGUI implements ActionListener {

    /**
     * InputGUI is a window where to enter a sentence to parse in interactive
     * mode
     */
    @SuppressWarnings("unused")
    private static final long serialVersionUID = 1L;

    private static final int LABSIZE = 150;
    private static final int FSIZE = 400;
    private static final int STHEIGHT = 30;
    private static final int SIDEBUTW = 80;
    private static final int PANELH = 150;

    public static final String VERSION = "3.0.0";

    // action commands
    private static final String QUIT = "Quit";
    private static final String ABOUT = "About";
    private static final String OUTPUT = "Output file";
    private static final String GRAMMAR = "Grammar";
    private static final String FRAMEGRAMMAR = "Frame grammar";
    private static final String TYPEHIERARCHY = "Type Hierarchy";
    private static final String LEMMA = "Lemma";
    private static final String MORPH = "Morph";
    private static final String AXIOM = "Axiom";
    private static final String PARSE = "Parse";
    private static final String GRAMSEL = "gramsel";
    private static final String SENT_SELECTDOWN = "sent_selectdown";
    private static final String SENT_SELECTUP = "sent_selectup";
    private static final String SENT_CLEAR = "sent_clear";
    private static final String SHELL_CLEAR = "clear";
    private static final String SHELL_TOGGLE = "pop out";
    private static final String SHELL_COPY = "to clipboard";
    private static final String SHELL_FILE = "to file";

    private static final String RCG = "rcg";
    private static final String TAG = "tag";
    private static final String TTMCTAG = "ttmctag";
    private static final String CFG = "cfg";
    private static final String LCFRS = "lcfrs";

    private static final String splashLocation = "images/splash.gif";
    private static final String logoLocation = "images/tulipa-logo.gif";

    private CommandLineOptions ops = null;
    private CommandLineOptions localops = null;

    private JFrame guiFrame = null;

    private String grammar = null;
    private String frameGrammar = null;
    private String typeHierarchy = null;
    private String lemma = null;
    private String morph = null;
    private String sentence = null;
    private String axiom = null;
    private String outfile = null;

    private JLabel gText = null;
    private JLabel fgText = null;
    private JLabel tyHiText = null;
    private JLabel lText = null;
    private JLabel mText = null;
    private JLabel aText = null;
    private JLabel outText = null;

    private JTextField gramF = null;
    private JTextField fgramF = null;
    private JTextField tyHiF = null;
    private JTextField lemmaF = null;
    private JTextField morphF = null;
    private JTextField aF = null;
    private JTextField outF = null;
    private JTextField addOptsF = null;

    private JButton fileSel = null;
    private JRadioButton rcgb = null;
    private JRadioButton tagb = null;
    private JRadioButton ttmctagb = null;
    private JRadioButton cfgb = null;
    private JRadioButton lcfrsb = null;

    private JLabel sText = null;
    private JComboBox<Object> toParse = null;
    private Object[] sentences = new Object[0];
    private JButton parse = null;

    private Border eb = null;

    private JCheckBox verboseBox = null;
    private JCheckBox xmlBox = null;
    private JCheckBox derivBox = null;
    private JCheckBox dependencyBox = null;
    private String depOutputLocation = ".";

    private JPanel shellPanel = null;
    private JTextArea shell = null;
    private JTextArea auxshell = null;
    private JScrollPane shellPane = null;
    private JFrame shellFrame = null;
    private JButton shellToggleButton = null;

    private PrintStream errs = new PrintStream(
            new StderrStream(new ByteArrayOutputStream()));

    /*
     * a thread for doing parsing + variable for managing interrupts
     */
    private Thread pt = null;

    public InputGUI(CommandLineOptions ops) {
        Thread splash = new Thread(new SplashScreen());
        splash.start();

        System.setErr(errs);
        pt = new Thread();

        guiFrame = new JFrame();
        guiFrame.setTitle("TuLiPA " + InputGUI.VERSION);
        guiFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        guiFrame.setJMenuBar(getMenuBar());
        guiFrame.setContentPane(getContainer());
        guiFrame.pack();
        guiFrame.setResizable(false);
        guiFrame.setLocationRelativeTo(null);

        shellFrame = new JFrame();
        shellFrame.setTitle("TuLiPA shell");
        shellFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        shellFrame.setContentPane(auxshell);
        shellFrame.setSize(new Dimension(400, 300));
        shellFrame.setResizable(true);
        shellFrame.setLocationRelativeTo(guiFrame);

        this.setOps(ops);
        localops = null;

        guiFrame.setVisible(true);

    }

    public JFrame getGuiFrame() {
        return guiFrame;
    }

    private JMenuBar getMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu1 = new JMenu("Input");
        JMenu menu2 = new JMenu("Output");
        JMenu menu3 = new JMenu("Help");
        JMenu menu4 = new JMenu("Quit");
        menu1.setMnemonic('i');
        menu2.setMnemonic('o');
        menu3.setMnemonic('h');
        menu4.setMnemonic('q');

        JMenuItem gr = new JMenuItem(GRAMMAR);
        gr.setActionCommand(GRAMMAR);
        gr.setMnemonic(KeyEvent.VK_G);
        menu1.add(gr);
        gr.addActionListener(this);
        JMenuItem le = new JMenuItem(LEMMA);
        le.setActionCommand(LEMMA);
        le.setMnemonic(KeyEvent.VK_L);
        menu1.add(le);
        le.addActionListener(this);
        JMenuItem mo = new JMenuItem(MORPH);
        mo.setActionCommand(MORPH);
        mo.setMnemonic(KeyEvent.VK_M);
        menu1.add(mo);
        mo.addActionListener(this);
        JMenuItem ax = new JMenuItem(AXIOM);
        ax.setMnemonic(KeyEvent.VK_A);
        menu1.add(ax);
        ax.addActionListener(this);
        JMenuItem ou = new JMenuItem(OUTPUT);
        ou.setActionCommand(OUTPUT);
        ou.setMnemonic(KeyEvent.VK_O);
        menu2.add(ou);
        ou.addActionListener(this);
        JMenuItem about = new JMenuItem(ABOUT);
        about.setActionCommand(ABOUT);
        about.setMnemonic(KeyEvent.VK_A);
        menu3.add(about);
        about.addActionListener(this);
        JMenuItem quit = new JMenuItem(QUIT);
        quit.setActionCommand(QUIT);
        quit.setMnemonic(KeyEvent.VK_Q);
        menu4.add(quit);
        quit.addActionListener(this);

        menuBar.add(menu1);
        menuBar.add(menu2);
        menuBar.add(menu3);
        menuBar.add(menu4);

        return menuBar;
    }

    private JPanel getOptionsPanel() {

        JPanel mainOptionsContainer = new JPanel();
        mainOptionsContainer.setLayout(
                new BoxLayout(mainOptionsContainer, BoxLayout.LINE_AXIS));
        eb = new EmptyBorder(4, 4, 4, 4);
        mainOptionsContainer.setBorder(eb);

        /*
         * -r (rcg parser, otherwise default is TAG/TT-MCTAG)
         * -k N (limits the size of the list of pending arguments to N)
         * -v (verbose mode, for debugging purposes)
         * -w (when used with the graphical interface, displays the derivation
         * steps and some debugging info)
         * -x (output the XML derivation forest in either stdout or using the -o
         * option, otherwise default is graphical output)
         */
        // add all the options

        ButtonGroup grammarButtonGroup = new ButtonGroup();
        tagb = new JRadioButton();
        tagb.setText("TAG");
        tagb.setSelected(true);
        rcgb = new JRadioButton();
        rcgb.setText("RCG");
        rcgb.setSelected(false);
        ttmctagb = new JRadioButton();
        ttmctagb.setText("TT-MCTAG");
        ttmctagb.setSelected(false);
        cfgb = new JRadioButton();
        cfgb.setText("CFG");
        cfgb.setSelected(false);
        lcfrsb = new JRadioButton();
        lcfrsb.setText("LCFRS");
        lcfrsb.setSelected(false);

        grammarButtonGroup.add(rcgb);
        rcgb.addKeyListener(new GrammarKeyListener(this));
        grammarButtonGroup.add(tagb);
        tagb.addKeyListener(new GrammarKeyListener(this));
        grammarButtonGroup.add(ttmctagb);
        ttmctagb.addKeyListener(new GrammarKeyListener(this));
        grammarButtonGroup.add(cfgb);
        cfgb.addKeyListener(new GrammarKeyListener(this));
        grammarButtonGroup.add(lcfrsb);
        lcfrsb.addKeyListener(new GrammarKeyListener(this));

        JPanel grammarOpts = new JPanel();
        grammarOpts.setLayout(new GridLayout(2, 3));
        grammarOpts.add(cfgb);
        grammarOpts.add(lcfrsb);
        grammarOpts.add(rcgb);
        grammarOpts.add(tagb);
        grammarOpts.add(ttmctagb);
        grammarOpts.setBorder(new TitledBorder("Mode"));

        JPanel tagOpts = new JPanel();
        tagOpts.setLayout(new BoxLayout(tagOpts, BoxLayout.PAGE_AXIS));
        derivBox = new JCheckBox("Show derivation steps in GUI");
        tagOpts.add(derivBox);
        dependencyBox = new JCheckBox("Dependency output");
        dependencyBox.setToolTipText("Output directory: " + depOutputLocation);
        tagOpts.add(dependencyBox);
        tagOpts.setBorder(new TitledBorder("TT-MCTAG/TAG"));

        JPanel miscOpts = new JPanel();
        miscOpts.setLayout(new BoxLayout(miscOpts, BoxLayout.PAGE_AXIS));
        miscOpts.setBorder(new TitledBorder("Misc"));
        verboseBox = new JCheckBox("Verbose mode");
        miscOpts.add(verboseBox);
        xmlBox = new JCheckBox("XML output, no GUI");
        miscOpts.add(xmlBox);

        JPanel addOptionsContainer = new JPanel();
        addOptionsContainer.setLayout(
                new BoxLayout(addOptionsContainer, BoxLayout.LINE_AXIS));
        addOptionsContainer.setBorder(new EmptyBorder(4, 4, 4, 4));
        JPanel fieldPanel = new JPanel();
        fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.LINE_AXIS));
        fieldPanel.setBorder(new TitledBorder("Additional Options"));
        addOptsF = new JTextField();
        fieldPanel.add(addOptsF);
        addOptionsContainer.add(fieldPanel);

        mainOptionsContainer.add(grammarOpts);
        mainOptionsContainer.add(tagOpts);
        mainOptionsContainer.add(miscOpts);

        JPanel etchedContainer = new JPanel();
        etchedContainer
                .setLayout(new BoxLayout(etchedContainer, BoxLayout.PAGE_AXIS));
        etchedContainer.setBorder(new EtchedBorder());
        etchedContainer.add(mainOptionsContainer);
        etchedContainer.add(addOptionsContainer);

        JPanel spacedEtchedContainer = new JPanel();
        spacedEtchedContainer.setBorder(new EmptyBorder(3, 3, 3, 3));
        spacedEtchedContainer.add(etchedContainer);

        return spacedEtchedContainer;
    }

    private JPanel getInputPanel() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));

        JPanel tPanel = new JPanel();
        eb = new EmptyBorder(-4, -4, -4, -4);
        tPanel.setBorder(eb);
        gText = new JLabel();
        gText.setPreferredSize(new Dimension(LABSIZE, STHEIGHT));
        gText.setText("Grammar: ");
        gramF = new JTextField();
        gramF.setText("");
        gramF.setPreferredSize(new Dimension(FSIZE, STHEIGHT));
        fileSel = new JButton();
        fileSel.setActionCommand(GRAMMAR);
        fileSel.addActionListener(this);
        fileSel.setText("Browse");
        fileSel.setPreferredSize(new Dimension(SIDEBUTW, STHEIGHT));
        tPanel.add(gText);
        tPanel.add(gramF);
        tPanel.add(fileSel);
        container.add(tPanel);

        tPanel = new JPanel();
        eb = new EmptyBorder(-4, -4, -4, -4);
        tPanel.setBorder(eb);
        fgText = new JLabel();
        fgText.setPreferredSize(new Dimension(LABSIZE, STHEIGHT));
        fgText.setText("Frame Grammar: ");
        fgramF = new JTextField();
        fgramF.setText("");
        fgramF.setPreferredSize(new Dimension(FSIZE, STHEIGHT));
        fileSel = new JButton();
        fileSel.setActionCommand(FRAMEGRAMMAR);
        fileSel.addActionListener(this);
        fileSel.setText("Browse");
        fileSel.setPreferredSize(new Dimension(SIDEBUTW, STHEIGHT));
        tPanel.add(fgText);
        tPanel.add(fgramF);
        tPanel.add(fileSel);
        container.add(tPanel);

        tPanel = new JPanel();
        eb = new EmptyBorder(-4, -4, -4, -4);
        tPanel.setBorder(eb);
        tyHiText = new JLabel();
        tyHiText.setPreferredSize(new Dimension(LABSIZE, STHEIGHT));
        tyHiText.setText("Type hierarchy: ");
        tyHiF = new JTextField();
        tyHiF.setText("");
        tyHiF.setPreferredSize(new Dimension(FSIZE, STHEIGHT));
        fileSel = new JButton();
        fileSel.setActionCommand(TYPEHIERARCHY);
        fileSel.addActionListener(this);
        fileSel.setText("Browse");
        fileSel.setPreferredSize(new Dimension(SIDEBUTW, STHEIGHT));
        tPanel.add(tyHiText);
        tPanel.add(tyHiF);
        tPanel.add(fileSel);
        container.add(tPanel);

        tPanel = new JPanel();
        eb = new EmptyBorder(-4, -4, -4, -4);
        tPanel.setBorder(eb);
        lText = new JLabel();
        lText.setPreferredSize(new Dimension(LABSIZE, STHEIGHT));
        lText.setText("Lemmas: ");
        lemmaF = new JTextField();
        lemmaF.setText("");
        lemmaF.setPreferredSize(new Dimension(FSIZE, STHEIGHT));
        fileSel = new JButton();
        fileSel.setActionCommand(LEMMA);
        fileSel.addActionListener(this);
        fileSel.setText("Browse");
        fileSel.setPreferredSize(new Dimension(SIDEBUTW, STHEIGHT));
        tPanel.add(lText);
        tPanel.add(lemmaF);
        tPanel.add(fileSel);
        container.add(tPanel);

        tPanel = new JPanel();
        eb = new EmptyBorder(-4, -4, -4, -4);
        tPanel.setBorder(eb);
        mText = new JLabel();
        mText.setText("Morphological entries: ");
        mText.setPreferredSize(new Dimension(LABSIZE, STHEIGHT));
        morphF = new JTextField();
        morphF.setText("");
        morphF.setPreferredSize(new Dimension(FSIZE, STHEIGHT));
        fileSel = new JButton();
        fileSel.setActionCommand(MORPH);
        fileSel.addActionListener(this);
        fileSel.setText("Browse");
        fileSel.setPreferredSize(new Dimension(SIDEBUTW, STHEIGHT));
        tPanel.add(mText);
        tPanel.add(morphF);
        tPanel.add(fileSel);
        container.add(tPanel);

        tPanel = new JPanel();
        eb = new EmptyBorder(-4, -4, -4, -4);
        tPanel.setBorder(eb);
        outText = new JLabel();
        outText.setPreferredSize(new Dimension(LABSIZE, STHEIGHT));
        outText.setText("Output file: ");
        outF = new JTextField();
        outF.setPreferredSize(new Dimension(FSIZE, STHEIGHT));
        outF.setText("");
        fileSel = new JButton();
        fileSel.setActionCommand(OUTPUT);
        fileSel.addActionListener(this);
        fileSel.setText("Browse");
        fileSel.setPreferredSize(new Dimension(SIDEBUTW, STHEIGHT));
        tPanel.add(outText);
        tPanel.add(outF);
        tPanel.add(fileSel);
        container.add(tPanel);

        tPanel = new JPanel();
        eb = new EmptyBorder(-4, -4, -4, -4);
        tPanel.setBorder(eb);
        aText = new JLabel();
        aText.setPreferredSize(new Dimension(LABSIZE, STHEIGHT));
        aText.setText("Axiom: ");
        aF = new JTextField();
        aF.setPreferredSize(new Dimension(FSIZE, STHEIGHT));
        aF.setText("");
        JPanel ph = new JPanel();
        ph.setPreferredSize(new Dimension(SIDEBUTW, STHEIGHT));
        tPanel.add(aText);
        tPanel.add(aF);
        tPanel.add(ph);
        container.add(tPanel);

        tPanel = new JPanel();
        eb = new EmptyBorder(-4, -4, -4, -4);
        tPanel.setBorder(eb);
        sText = new JLabel();
        sText.setPreferredSize(new Dimension(LABSIZE, STHEIGHT));
        sText.setText("Sentence: ");
        toParse = new JComboBox<Object>(sentences);
        toParse.setPreferredSize(new Dimension(FSIZE, STHEIGHT));
        toParse.setEditable(true);
        // toParse.addActionListener(this);
        Component ec = toParse.getEditor().getEditorComponent();
        ec.addKeyListener(new SboxKeyListener(this));
        JButton clearBut = new JButton();
        clearBut.setActionCommand(SENT_CLEAR);
        clearBut.addActionListener(this);
        clearBut.setText("Clear");
        clearBut.setPreferredSize(new Dimension(SIDEBUTW, STHEIGHT));
        tPanel.add(sText);
        tPanel.add(toParse);
        tPanel.add(clearBut);
        container.add(tPanel);

        tPanel = new JPanel();
        eb = new EmptyBorder(3, 3, 3, 3);
        tPanel.setBorder(eb);

        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setPreferredSize(new Dimension(LABSIZE, STHEIGHT + 22));
        JLabel logolabel = new JLabel();
        URL imgURL = InputGUI.class.getResource(logoLocation);
        ImageIcon icon = null;
        if (imgURL != null) {
            icon = new ImageIcon(imgURL);
        }
        if (icon != null) {
            logolabel.setIcon(icon);
        }
        logoPanel.add(logolabel, BorderLayout.SOUTH);
        parse = new JButton();
        parse.setMnemonic('p');
        parse.setActionCommand(PARSE);
        parse.setText(PARSE);
        parse.setPreferredSize(new Dimension(FSIZE, STHEIGHT));
        parse.addActionListener(this);
        JButton quitButton = new JButton(QUIT);
        quitButton.setActionCommand(QUIT);
        quitButton.addActionListener(this);
        quitButton.setPreferredSize(new Dimension(SIDEBUTW, STHEIGHT));
        tPanel.add(logoPanel);
        tPanel.add(parse);
        tPanel.add(quitButton);
        container.add(tPanel);

        shellPanel = new JPanel();
        shell = new JTextArea();
        shell.setEditable(false);
        auxshell = new JTextArea();
        auxshell.setEditable(false);

        JPanel shellControlPanel = new JPanel(new BorderLayout());
        eb = new EmptyBorder(0, 0, 0, 0);
        shellControlPanel.setBorder(eb);

        JPanel shellControlSubPanel = new JPanel();
        // eb = new EmptyBorder(0,0,0,0);
        JButton shellButton = new JButton(SHELL_COPY);
        shellButton.setActionCommand(SHELL_COPY);
        shellButton.addActionListener(this);
        shellButton
                .setPreferredSize(new Dimension(SIDEBUTW + 50, STHEIGHT - 8));
        shellControlSubPanel.add(shellButton);
        shellButton = new JButton(SHELL_FILE);
        shellButton.setActionCommand(SHELL_FILE);
        shellButton.addActionListener(this);
        shellButton
                .setPreferredSize(new Dimension(SIDEBUTW + 50, STHEIGHT - 8));
        shellControlSubPanel.add(shellButton);
        shellButton = new JButton(SHELL_CLEAR);
        shellButton.setActionCommand(SHELL_CLEAR);
        shellButton.addActionListener(this);
        shellButton
                .setPreferredSize(new Dimension(SIDEBUTW + 50, STHEIGHT - 8));
        shellControlSubPanel.add(shellButton);
        shellControlPanel.add(shellControlSubPanel, BorderLayout.WEST);

        shellControlSubPanel = new JPanel();
        shellToggleButton = new JButton(SHELL_TOGGLE);
        shellToggleButton.setActionCommand(SHELL_TOGGLE);
        shellToggleButton.addActionListener(this);
        shellToggleButton
                .setPreferredSize(new Dimension(SIDEBUTW + 50, STHEIGHT - 8));
        shellControlSubPanel.add(shellToggleButton);
        shellControlPanel.add(shellControlSubPanel, BorderLayout.EAST);

        shellPane = new JScrollPane(shell);
        shellPanel.setLayout(new BorderLayout());
        shellPane.setPreferredSize(
                new Dimension(FSIZE + SIDEBUTW + LABSIZE, PANELH));
        shellPanel.add(shellPane, BorderLayout.CENTER);
        shellPanel.add(shellControlPanel, BorderLayout.SOUTH);
        container.add(shellPanel);

        return container;
    }

    public JPanel getContainer() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));
        container.add(getOptionsPanel());
        container.add(getInputPanel());
        return container;
    }

    private File getPathFromJTextField(JTextField tf) {
        File ret = new File(".");
        String text = tf.getText();
        if (text.length() > 0) {
            File f = new File(text);
            if (f != null) {
                if (f.isDirectory()) {
                    ret = f;
                } else if (f.isFile()) {
                    ret = f.getParentFile();
                }
            }
        }
        return ret;
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        // System.err.println("[GUI] " + command);
        if (GRAMMAR.equals(command)) {
            JFileChooser jf = new JFileChooser();
            File cd = getPathFromJTextField(gramF);
            if (cd != null) {
                jf.setCurrentDirectory(cd);
            }
            jf.showOpenDialog(guiFrame);
            jf.setDialogTitle("Please select a grammar");
            File tf = jf.getSelectedFile();
            if (tf != null) {
                grammar = jf.getSelectedFile().getAbsolutePath();
                gramF.setText(grammar);
            }
        } else if (FRAMEGRAMMAR.equals(command)) {
            // Choose the frame grammar file
            JFileChooser jf = new JFileChooser();
            File cd = getPathFromJTextField(gramF);
            if (cd != null) {
                jf.setCurrentDirectory(cd);
            }
            jf.showOpenDialog(guiFrame);
            jf.setDialogTitle("Please select a frame grammar");
            File tf = jf.getSelectedFile();
            if (tf != null) {
                frameGrammar = jf.getSelectedFile().getAbsolutePath();
                fgramF.setText(frameGrammar);
            }
        } else if (TYPEHIERARCHY.equals(command)) {
            // Choose the type hierarchy file
            JFileChooser jf = new JFileChooser();
            File cd = getPathFromJTextField(gramF);
            if (cd != null) {
                jf.setCurrentDirectory(cd);
            }
            jf.showOpenDialog(guiFrame);
            jf.setDialogTitle("Please select a type hierarchy");
            File tf = jf.getSelectedFile();
            if (tf != null) {
                typeHierarchy = jf.getSelectedFile().getAbsolutePath();
                tyHiF.setText(typeHierarchy);
            }
        } else if (LEMMA.equals(command)) {
            JFileChooser jf = new JFileChooser();
            File cd = getPathFromJTextField(gramF);
            if (cd != null) {
                jf.setCurrentDirectory(cd);
            }
            jf.showOpenDialog(guiFrame);
            jf.setDialogTitle("Please select a lemma");
            File tf = jf.getSelectedFile();
            if (tf != null) {
                lemma = jf.getSelectedFile().getAbsolutePath();
                lemmaF.setText(lemma);
            }
        } else if (MORPH.equals(command)) {
            JFileChooser jf = new JFileChooser();
            File cd = getPathFromJTextField(gramF);
            if (cd != null) {
                jf.setCurrentDirectory(cd);
            }
            jf.showOpenDialog(guiFrame);
            jf.setDialogTitle("Please select a morphological lexicon");
            File tf = jf.getSelectedFile();
            if (tf != null) {
                morph = jf.getSelectedFile().getAbsolutePath();
                morphF.setText(morph);
            }
        } else if (OUTPUT.equals(command)) {
            JFileChooser jf = new JFileChooser();
            File cd = getPathFromJTextField(gramF);
            if (cd != null) {
                jf.setCurrentDirectory(cd);
            }
            jf.showSaveDialog(guiFrame);
            jf.setDialogTitle("Please select an output file");
            File tf = jf.getSelectedFile();
            if (tf != null) {
                outfile = jf.getSelectedFile().getAbsolutePath();
                outF.setText(outfile);
            }
        } else if (AXIOM.equals(command)) {
            axiom = (String) JOptionPane
                    .showInputDialog("Axiom of the grammar:");
            aF.setText(axiom);
        } else if (ABOUT.equals(command)) {
            String msg = "TuLiPA - " + InputGUI.VERSION
                    + "\nTuLiPA is a parsing architecture based on Range Concatenation Grammars \n";
            msg += "developed at University Tuebingen, more information at : \n";
            msg += "http://www.sfs.uni-tuebingen.de/emmy/tulipa/";
            JOptionPane.showMessageDialog(guiFrame, msg);
        } else if (QUIT.equals(command)) {
            System.exit(0);
        } else if (PARSE.equals(command)) {
            pt = new ParseLauncher();
            pt.start();
        } else if (GRAMSEL.equals(command)) {
            toggleGramSelection();
        } else if (SENT_SELECTDOWN.equals(command)) {
            int selind = toParse.getSelectedIndex();
            if (toParse.isPopupVisible() && selind == -1) {
                selind = 0;
                toParse.setPopupVisible(false);
            }
        } else if (SENT_SELECTUP.equals(command)) {
            int selind = toParse.getSelectedIndex();
            if (toParse.isPopupVisible() && selind < 1) {
                toParse.setPopupVisible(false);
            }
        } else if (SENT_CLEAR.equals(command)) {
            toParse.removeAllItems();
        } else if (SHELL_CLEAR.equals(command)) {
            shell.setText("");
            auxshell.setText("");
        } else if (SHELL_TOGGLE.equals(command)) {
            if (shellFrame.isVisible()) {
                shellFrame.setVisible(false);
            } else {
                shellFrame.setVisible(true);
            }
        } else if (SHELL_COPY.equals(command)) {
            Clipboard sysclip = Toolkit.getDefaultToolkit()
                    .getSystemClipboard();
            Transferable shellcont = new StringSelection(shell.getText());
            sysclip.setContents(shellcont, null);
        } else if (SHELL_FILE.equals(command)) {
            String shellText = shell.getText();
            if (!shellText.equals("")) {
                JFileChooser jf = new JFileChooser();
                File cd = new File(".");
                if (cd != null) {
                    jf.setCurrentDirectory(cd);
                }
                jf.showSaveDialog(guiFrame);
                jf.setDialogTitle("Please select an output file");
                File tf = jf.getSelectedFile();
                if (tf != null) {
                    File shellof = jf.getSelectedFile();
                    try {
                        Writer shellow = new BufferedWriter(
                                new FileWriter(shellof));
                        try {
                            shellow.write(shell.getText());
                        } finally {
                            shellow.close();
                        }
                    } catch (Exception fe) {
                        JOptionPane.showMessageDialog(guiFrame,
                                "Could not write shell output to file. Reason: "
                                        + fe.getMessage(),
                                "File Error", JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        }
    }

    /**
     * Call the interface with the current parameters.
     * This method is synchronized to prevent multiple calls.
     */
    public synchronized void doParse() {
        shell.setText("");
        auxshell.setText("");
        updateOps();
        String sentence = (String) toParse.getEditor().getItem();
        addSentenceToHistory(sentence);
        ops.setVal("s", " " + sentence + " ");
        // precheck if grammar is present for nicer message
        if (gramF.getText().length() == 0) {
            JOptionPane.showMessageDialog(guiFrame,
                    "A grammar must be specified!", "Grammar missing",
                    JOptionPane.WARNING_MESSAGE);
        } else {
            Grammar g = null;
            Grammar frameG = null;
            Situation sit = null;
            try {
                sit = WorkbenchLoader.loadSituation(ops, gramF.getText(),
                        fgramF.getText(), lemmaF.getText(), morphF.getText(),
                        tyHiF.getText());
                g = sit.getGrammar();
                frameG = sit.getFrameGrammar();
            } catch (Exception e) {
                e.printStackTrace();
                String msg = e.getMessage();
                JOptionPane.showMessageDialog(guiFrame, msg, "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            boolean parseres = false;
            try {
                if (g instanceof TTMCTAG) {
                    // bruteforce new method. TODO: create Butten for tag2rcg
                    parseres = ParsingInterface.parseTAG(ops, sit, sentence);
                    // parseres = ParsingInterface.parseSentence(ops, sit,
                    // sentence);
                    // parseres = Interface.parseSentence(ops, g, sentence);
                } else {
                    parseres = ParsingInterface.parseNonTAG(ops, g, sentence);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(guiFrame, e + "\n",
                        "Error during parsing", JOptionPane.ERROR_MESSAGE);
                // System.err.println(e);
                // e.printStackTrace();
                return;
            }
            if (!parseres) {
                JOptionPane.showMessageDialog(guiFrame, "No parse found.");
            }
        }
    }

    public class ParseLauncher extends Thread {
        public void run() {
            doParse();
        }
    }

    private void setGram(String gram) {
        if (gram.equals(TAG)) {
            tagb.setSelected(true);
            tagb.requestFocus();
        } else if (gram.equals(RCG)) {
            rcgb.setSelected(true);
            xmlBox.setSelected(true);
            rcgb.requestFocus();
        } else if (gram.equals(TTMCTAG)) {
            ttmctagb.setSelected(true);
            ttmctagb.requestFocus();
        } else if (gram.equals(CFG)) {
            cfgb.setSelected(true);
            cfgb.requestFocus();
        } else if (gram.equals(LCFRS)) {
            lcfrsb.setSelected(true);
            lcfrsb.requestFocus();
        }
    }

    private String getGram() {
        String ret = "";
        if (tagb.isSelected()) {
            ret = TAG;
        } else if (rcgb.isSelected()) {
            xmlBox.setSelected(true);
            ret = RCG;
        } else if (ttmctagb.isSelected()) {
            ret = TTMCTAG;
        } else if (cfgb.isSelected()) {
            ret = CFG;
        } else if (lcfrsb.isSelected()) {
            ret = LCFRS;
        }
        return ret;
    }

    public String getGrammar() {
        return grammar;
    }

    public void setGrammar(String grammar) {
        this.grammar = grammar;
        gramF.setText(grammar);
    }

    public String getLemma() {
        return lemma;
    }

    public void setLemma(String lemma) {
        this.lemma = lemma;
        lemmaF.setText(lemma);
    }

    public String getMorph() {
        return morph;
    }

    public void setMorph(String morph) {
        this.morph = morph;
        morphF.setText(morph);
    }

    private void toggleGramSelection() {
        if (rcgb.isSelected()) {
            xmlBox.setSelected(true);
            setGram(CFG);
        } else if (tagb.isSelected()) {
            xmlBox.setSelected(true);
            setGram(LCFRS);
        } else if (cfgb.isSelected()) {
            xmlBox.setSelected(false);
            setGram(TAG);
        } else if (lcfrsb.isSelected()) {
            xmlBox.setSelected(false);
            setGram(TTMCTAG);
        } else if (ttmctagb.isSelected()) {
            xmlBox.setSelected(true);
            setGram(RCG);
        } else {
            xmlBox.setSelected(true);
            setGram(RCG);
        }
    }

    public void addSentenceToHistory(String current) {
        boolean hasItem = false;
        for (int i = 0; i < toParse.getItemCount() && !hasItem; ++i) {
            if (toParse.getItemAt(i) != null
                    && toParse.getItemAt(i).equals(current)) {
                hasItem = true;
            }
        }
        if (!hasItem && current.trim().length() > 0) {
            toParse.addItem(current);
            toParse.setSelectedIndex(toParse.getItemCount() - 1);
        }
    }

    public String getSentence() {
        return sentence;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
        addSentenceToHistory(sentence);
    }

    public String getAxiom() {
        return axiom;
    }

    public void setAxiom(String axiom) {
        this.axiom = axiom;
        aF.setText(axiom);
    }

    public String getOutfile() {
        return outfile;
    }

    public void setOutfile(String outfile) {
        this.outfile = outfile;
        outF.setText(outfile);
    }

    public CommandLineOptions getOps() {
        return ops;
    }

    public void setOps(CommandLineOptions ops) {
        /*
         * -r (rcg parser, otherwise default is TAG/TT-MCTAG)
         * -k N (limits the size of the list of pending arguments to N)
         * -v (verbose mode, for debugging purposes)
         * -w (when used with the graphical interface, displays the derivation
         * steps and some debugging info)
         * -x (output the XML derivation forest in either stdout or using the -o
         * option, ot
         */
        this.ops = ops;
        if (ops.check("r")) {
            setGram("rcg");
        } else if (ops.check("c")) {
            setGram("cfg");
        } else if (ops.check("lcfrs")) {
            setGram("lcfrs");
        } else {
            setGram("tag");
        }
        if (ops.check("a")) {
            aF.setText(ops.getVal("a"));
        }
        if (ops.check("d")) {
            dependencyBox.setSelected(true);
        }
        if (ops.check("g")) {
            gramF.setText(ops.getVal("g"));
        }
        if (ops.check("f")) {
            fgramF.setText(ops.getVal("f"));
        }
        if (ops.check("th")) {
            tyHiF.setText(ops.getVal("th"));
        }
        if (ops.check("l")) {
            lemmaF.setText(ops.getVal("l"));
        }
        if (ops.check("m")) {
            morphF.setText(ops.getVal("m"));
        }
        verboseBox.setSelected(ops.check("v"));
        derivBox.setSelected(ops.check("w"));
        xmlBox.setSelected(ops.check("x"));
    }

    public void updateOps() {
        ops.removeVal("r");
        ops.removeVal("lcfrs");
        ops.removeVal("c");
        if (RCG.equals(getGram())) {
            ops.setOurVal("r", "");
        } else if (CFG.equals(getGram())) {
            ops.setOurVal("c", "");
        } else if (LCFRS.equals(getGram())) {
            ops.setOurVal("lcfrs", "");
        } else {
        }
        if (verboseBox.isSelected()) {
            ops.setOurVal("v", "");
        } else {
            ops.removeVal("v");
        }
        if (!dependencyBox.isSelected()) {
            ops.removeVal("d");
        } else {
            ops.setOurVal("d", depOutputLocation);
        }
        if (derivBox.isSelected()) {
            ops.setOurVal("w", "");
        } else {
            ops.removeVal("w");
        }
        if (xmlBox.isSelected()) {
            ops.setOurVal("x", "");
        } else {
            ops.removeVal("x");
        }
        String outftext = outF.getText().trim();
        if (outftext.length() > 0) {
            ops.setOurVal("o", outftext);
        }
        String axiomtext = aF.getText().trim();
        if (axiomtext.length() > 0) {
            ops.setOurVal("a", axiomtext);
        }
        String additionalOpts = addOptsF.getText().trim();
        String[] addOptsSplit = additionalOpts.split("\\s+");
        // delete old local options
        if (localops != null) {
            Enumeration<String> e = localops.getKeys();
            while (e.hasMoreElements()) {
                ops.removeVal(e.nextElement());
            }
            localops.removeAll();
        }
        if (additionalOpts.length() > 0) {
            localops = CommandLineProcesses.processCommandLine(addOptsSplit);
            // System.err.println(" *** " + localops.toString());
            ops.merge(localops);
            // System.err.println("Set options:\n" + ops.toString());
        }
        // update grammar, frame, lexicon, morph and type hierarchy
        if (!(gramF.getText().equals("")))
            ops.setOurVal("g", gramF.getText());
        if (!(fgramF.getText().equals("")))
            ops.setOurVal("f", fgramF.getText());
        if (!(tyHiF.getText().equals("")))
            ops.setOurVal("th", tyHiF.getText());
        if (!(lemmaF.getText().equals("")))
            ops.setOurVal("l", lemmaF.getText());
        if (!(morphF.getText().equals("")))
            ops.setOurVal("m", morphF.getText());
    }

    /*
     * KeyListener for the grammar selection radio buttons.
     * Checks for arrow keys and selects the other one.
     */
    private class GrammarKeyListener implements KeyListener {

        private InputGUI g;

        GrammarKeyListener(InputGUI g) {
            super();
            this.g = g;
        }

        public void keyPressed(KeyEvent e) {
            int keyc = e.getKeyCode();
            if (keyc == KeyEvent.VK_UP || keyc == KeyEvent.VK_DOWN
                    || keyc == KeyEvent.VK_LEFT || keyc == KeyEvent.VK_RIGHT) {
                // System.err.println("arrow key released");
                ActionEvent action = new ActionEvent(e.getSource(),
                        ActionEvent.ACTION_PERFORMED, InputGUI.GRAMSEL);
                g.actionPerformed(action);
            }
        }

        public void keyReleased(KeyEvent e) {
        }

        public void keyTyped(KeyEvent e) {
        }
    }

    /*
     * Key Listener for the parse button
     */
    private class SboxKeyListener implements KeyListener {

        private InputGUI g;

        SboxKeyListener(InputGUI g) {
            super();
            this.g = g;
        }

        public void keyPressed(KeyEvent e) {
            int keyc = e.getKeyCode();

            ActionEvent action = null;
            Object src = e.getSource();
            int ap = ActionEvent.ACTION_PERFORMED;
            String command = "";

            if (keyc == KeyEvent.VK_ENTER && !g.toParse.isPopupVisible()) {
                command = InputGUI.PARSE;
            } else if (keyc == KeyEvent.VK_DOWN) {
                command = InputGUI.SENT_SELECTDOWN;
            } else if (keyc == KeyEvent.VK_UP) {
                command = InputGUI.SENT_SELECTUP;
            } else {
                return;
            }
            action = new ActionEvent(src, ap, command);
            g.actionPerformed(action);
        }

        public void keyReleased(KeyEvent e) {
        }

        public void keyTyped(KeyEvent e) {
        }
    }

    class StderrStream extends FilterOutputStream {

        public StderrStream(OutputStream os) {
            super(os);
        }

        public void write(byte b[]) throws IOException {
            String s = new String(b);
            shell.append(s);
            auxshell.append(s);
            // autoscroll
            String st = shell.getText();
            // simple: scrolls to the right, too.
            // if (st.length() > 1) {
            // shell.setCaretPosition(shell.getCaretPosition() + s.length());
            // }
            // doesn't scroll to the right, only downwards
            if (st.length() > 2) {
                int nli = st.lastIndexOf('\n');
                if (nli > 0 && st.substring(0, nli).lastIndexOf('\n') > -1) {
                    // caret position is one char after penultimate \n
                    shell.setCaretPosition(nli + 1);
                    auxshell.setCaretPosition(nli + 1);
                }
            }
        }

        public void write(byte b[], int off, int len) throws IOException {
            String s = new String(b, off, len);
            shell.append(s);
            auxshell.append(s);
            // autoscroll
            String st = shell.getText();
            // simple: scrolls to the right, too.
            // if (st.length() > 1) {
            // shell.setCaretPosition(shell.getCaretPosition() + s.length());
            // }
            // doesn't scroll to the right, only downwards
            if (st.length() > 2) {
                int nli = st.lastIndexOf('\n');
                if (nli > 0 && st.substring(0, nli).lastIndexOf('\n') > -1) {
                    // caret position is one char after penultimate \n
                    shell.setCaretPosition(nli + 1);
                    auxshell.setCaretPosition(nli + 1);
                }
            }
        }
    }

    public class SplashScreen extends JFrame implements Runnable {

        private static final long serialVersionUID = 1L;
        private static final int SPLASHW = 392;
        private static final int SPLASHH = 279;
        private static final int SPLASHWAIT = 500;

        private ImageIcon splash = null;
        private JLabel imgPanel = new JLabel();

        public SplashScreen() {
            this.setSize(SPLASHW, SPLASHH);
            URL splashurl = InputGUI.class.getResource(splashLocation);
            this.setUndecorated(true);
            this.addMouseListener(new CloseListener(this));
            this.setLocationRelativeTo(null);
            this.setAlwaysOnTop(true);
            splash = new ImageIcon(getToolkit().createImage(splashurl));
            this.imgPanel.setIcon(splash);
            this.add(imgPanel);
            this.pack();
        }

        public void run() {
            this.setVisible(true);
            try {
                Thread.sleep(SPLASHWAIT);
            } catch (InterruptedException e) {
                dispose();
            }
            dispose();
        }

        public void dispose() {
            super.dispose();
        }

        /*
         * public void paint(Graphics g) {
         * g.drawImage(splash, 0,0, this);
         * }
         */

        private class CloseListener implements MouseListener {
            private JFrame hook;

            public CloseListener(JFrame splashWindow) {
                this.hook = splashWindow;
            }

            public void mouseClicked(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }

            public void mousePressed(MouseEvent e) {
                hook.dispose();
            }

            public void mouseReleased(MouseEvent e) {
            }
        }

    }

}
