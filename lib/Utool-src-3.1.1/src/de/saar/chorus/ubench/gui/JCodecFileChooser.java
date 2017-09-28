package de.saar.chorus.ubench.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import de.saar.basic.GenericFileFilter;
import de.saar.chorus.domgraph.codec.CodecManager;

/**
 * A class representing a <code>JFileChooser</code> which can
 * display and control options for Input and Output codecs used
 * in Utool.
 * 
 * This class is responsible for displaying the right kind
 * of dialog and options according to the type of codec (either
 * input or output) and to do display the correct options when
 * the codec selection has changed.
 * 
 * 
 * @author Alexander Koller
 * @author Michaela Regneri
 *
 */
public class JCodecFileChooser extends JFileChooser
			implements PropertyChangeListener, ActionListener {
	private JPanel empty,	// if there is no codec selected
				   button;  // the panel shown when options are hidden
	
	private boolean input, 				// input codec or not?
					optview;  			// options visible or not?
	
	private CodecManager manager; 		// the active codec manager
	
	private JCodecOptionPane options; 	// the current options panel
	
	private JButton showOptions;		// the button to access the option panel
	
	// a file filter accepting all known codec extensions
	private SeveralExtensionsFilter allKnownTypesFileFilter;
	
	// the default selected file filter
	private FileFilter defaultFileFilter;
	
	// The name of the currently selected codec. It starts out as "null" (for no
	// selection) and is updated every time the user chooses a specific codec from
	// the dropdown menu or selects a file whose extension is associated with a codec.
	private String currentCodec;
	
	/**
	 * The enum class representing the
	 * three diffent file chooser types for the
	 * three different tasks.
	 * 
	 * @author Alexander Koller
	 *
	 */
	public static enum Type {
		OPEN                 ("Open USR", true),
		EXPORT               ("Export USR", false),
		EXPORT_SOLVED_FORMS  ("Export solved forms", false);
		
		public String dialogTitle;
		public boolean isInput;

		private Type(String dialogTitle, boolean isInput) {
			this.dialogTitle = dialogTitle;
			this.isInput = isInput;
		}
	}
	

	/**
	 * A new <code>JCodecFileChooser</code> initialised
	 * with the folder to display and its task type.
	 * 
	 * @param path the path for the file view
	 * @param type the task type 
	 */
	public JCodecFileChooser(String path, Type type) {
		super(path);
		
		setDialogTitle(type.dialogTitle);
		input = type.isInput;
		
		allKnownTypesFileFilter = new SeveralExtensionsFilter();
	
		
		if( input ) {
			setAcceptAllFileFilterUsed(true);
			addChoosableFileFilter(allKnownTypesFileFilter);
			defaultFileFilter = allKnownTypesFileFilter;
		} else {
			setAcceptAllFileFilterUsed(false);
			defaultFileFilter = null;
		}

		addPropertyChangeListener(this);
		manager = Ubench.getInstance().getCodecManager();
		empty = new JPanel();

		empty.add(new JLabel("No Codec" +
				System.getProperty("line.separator")
				+ " selected."));
		
		showOptions = new JButton("Options");
		showOptions.setActionCommand("showOptions");
		showOptions.setEnabled(false);
		showOptions.addActionListener(this);
		optview = false;
		button = new JPanel();
		button.add(showOptions, BorderLayout.CENTER);
		setAccessory(button);
		//setAccessory(empty);
		
		currentCodec = null;
	}
	
	/**
	 * Add all the file filters for codecs this 
	 * file chooser shall accept.
	 * 
	 * @param filters the <code>List</code> of file filters
	 */
	public void addCodecFileFilters(List<GenericFileFilter> filters) {
		for( GenericFileFilter ff : filters ) {
			addChoosableFileFilter(ff);
			allKnownTypesFileFilter.addExtension(ff.getExtension());
			
			if( !input && (defaultFileFilter == null)) {
				defaultFileFilter = ff;
				enableOptions(ff.getName());
			}
		}

		if( defaultFileFilter != null ) {
			setFileFilter(defaultFileFilter);
		}
	}


	/**
	 * Returns the selected values of the codec
	 * options in string representation. The map returned 
	 * can be forwarded to the <code>CodecManager</code>
	 * to construct a codec initialised with these options.
	 * 
	 * @return the user selection of options
	 */
	public Map<String,String> getCodecOptions() {
		if( options != null) {
			return options.getOptionMap();
		} else return new HashMap<String,String>();
	}

	/**
	 * This is responsible for showing the options 
	 * belonging to the selected codec and for listening
	 * on codec selection changes.
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		String prop = evt.getPropertyName();
		String codecname = null;

		if( isShowing() ) {  // ignore all events that occur before the dialog is displayed
			if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(prop)) {
				// If the user selected a file which is associated to a codec, switch
				// the option display to that codec.
				File file = (File) evt.getNewValue();
				
				if( file != null ) {
					if( input ) {
						codecname = manager.getInputCodecNameForFilename(file.getName());
					} else {
						codecname = manager.getOutputCodecNameForFilename(file.getName());
					}
				}

			} else if(JFileChooser.FILE_FILTER_CHANGED_PROPERTY.equals(prop)) {
				// If the user selected a single codec in the dropdown menu, switch
				// to that codec.
				FileFilter filter = getFileFilter();

				if(filter instanceof GenericFileFilter) {
					codecname = ((GenericFileFilter) filter).getName(); 
				}
			}
			
			if( codecname != null ) {
				// Switch the option pane to that codec and enable the options button.
				enableOptions(codecname);
			}
		}
	}
	
	/**
	 * This constructs and shows an JCodecOptionPane of the 
	 * a certain codec.
	 * 
	 * @param codecname the name of the codec
	 */
	private void enableOptions(String codecname) {
		showOptions.setEnabled(true);
		currentCodec = codecname;
		options = new JCodecOptionPane(
				input ? manager.getInputCodecOptionTypes(codecname) 
					  : manager.getOutputCodecOptionTypes(codecname));
		if( input ) {
			for( String parameter : manager.getInputCodecOptionTypes(codecname).keySet() ) {
				options.setDefault(parameter, 
						manager.getInputCodecParameterDefaultValue(codecname, parameter));
			}
		} else {
			for( String parameter : manager.getOutputCodecOptionTypes(codecname).keySet() ) {
				options.setDefault(parameter, 
						manager.getOutputCodecParameterDefaultValue(codecname, parameter));
			}
		}
		
		if( optview ) {
			showOptionAccess(options);
		}
		
		validate();
	}

	/**
	 * Switches between option view and hidden option
	 * view (with the button giving access to the options.)
	 * 
	 * @param show if true, options are shown
	 */
	private void setShowAccessory(boolean show) {
		optview = show;
		if(show)  {
			showOptionAccess(options);
			
		} else {
	
			setAccessory(button);
		}
		validate();
	}

 	
	/**
	 * Displays a <code>JComponent</code> as accessory
	 * of this file chooser.
	 * 
	 * @param optionpane
	 */
	private void showOptionAccess(JComponent optionpane) {
		JPanel helperPanel = new JPanel();
		BoxLayout layout = new BoxLayout(helperPanel, BoxLayout.PAGE_AXIS);
		
		helperPanel.setLayout(layout);
		//helperPanel.add(new JLabel("Codec: " + ((GenericFileFilter) getFileFilter()).getName()));
		//helperPanel.add(new JLabel(" "));

		String  codecname = currentCodec;
		String title;
		if(codecname == null) {
			title = "Options";
		} else {
			title = "Options: " + codecname;
		}
		
		optionpane.setBorder(new TitledBorder(
				new LineBorder(Color.GRAY, 1, true), 
				title,
				TitledBorder.CENTER,
				TitledBorder.ABOVE_TOP));
		JScrollPane optionscrollpane = new JScrollPane(optionpane);
		optionscrollpane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		optionscrollpane.setBorder(new EmptyBorder(0,0,0,0));
		helperPanel.add(optionscrollpane);
		
		// doesn't work...
		
	
		helperPanel.add(new JLabel("     "));
		JButton hide = new JButton("Hide");
		hide.addActionListener(this);
		hide.setActionCommand("hide");
		helperPanel.add(hide);
		 
		setAccessory(helperPanel);
	
		validate();
		
		Dimension dim = new Dimension(
				Math.max(getTextLabelWidth(title), 
						optionscrollpane.getPreferredSize().width),
						optionscrollpane.getPreferredSize().height);
		optionpane.setMinimumSize(dim);
		optionpane.setPreferredSize(dim);
	
		helperPanel.setPreferredSize( new Dimension(
				dim.width + (int) optionscrollpane.getVerticalScrollBar().getWidth(),
				dim.height) );
		optionpane.revalidate();
		optionscrollpane.revalidate();
		revalidate();
	}

	/**
	 * This reactc when either the "Options" or the "Hide" button
	 * is pressed.
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("showOptions")) {
			setShowAccessory(true);
		} else if(e.getActionCommand().equals("hide")) {
			setShowAccessory(false);
		}
		
	}
	
	/**
	 * An estimate of the label with of the codec.
	 * 
	 * @param text
	 * @return
	 */
	private int getTextLabelWidth(String text) {
		JLabel ruler = new JLabel(text);
		return ruler.getMaximumSize().width;
	}
	
	/**
	 * A <code>FileFilter</code> designed to 
	 * accept a succesively added collection of
	 * extensions.
	 * 
	 *
	 */
	private static class SeveralExtensionsFilter extends FileFilter {
		
		Set<String> extensions;
		
		/**
		 *  Empty constructor (just for initialising).
		 *
		 */
		SeveralExtensionsFilter() {
			extensions = new HashSet<String>();
		}
		
		/**
		 * Initialise the Filter with a list of 
		 * extensions to accept.
		 * Please make sure to have a Collection of
		 * extension strings starting with "." !
		 * 
		 * @param ext
		 */
		SeveralExtensionsFilter(Collection<String> ext) {
			extensions = new HashSet<String>(ext);
		}
		
		/**
		 * Add a file extension that shall be accepted
		 * by the filter
		 * 
		 * @param extension the new extension
		 */
		public void addExtension(String extension) {
			if( extension.startsWith(".") ) {
				extensions.add(extension);
			} else {
				extensions.add("."+ extension);
			}
		}
		
		/**
		 * 
		 * @return true if the file has an extension
		 *        contained here or is a folder
		 */
		public boolean accept(File f) {
			
			String fileName = f.getName();
			
			if( f.isDirectory() ) {
				return true;
			} 
			
			for(String extension : extensions ) {
				if(fileName.endsWith(extension) ) {
					return true;
				}
				
			}
			return false;
		}
		
		/**
		 * 
		 */
		public String getDescription() {
			return "All known file types";
		}
		
	}
	
	
	private static final long serialVersionUID = 2420583972471990944L;



}


