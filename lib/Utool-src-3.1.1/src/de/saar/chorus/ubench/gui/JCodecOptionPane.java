package de.saar.chorus.ubench.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;


/**
 * This represents a UI for selecting or entering values of different
 * types (according to possible codec options).
 * Enums are displayed as drop-down menus, boolean values
 * as checkboxes and everything else as text field.
 * Default Values can be set. 
 * This also provides a method to return the values selected is provided.
 * 
 * @author Michaela Regneri
 * @see de.saar.chorus.domgraph.codec.CodecConstructor
 *
 */
public class JCodecOptionPane extends JComponent {

	
	private static final long serialVersionUID = 5201435318585726488L;

	private static Map<String, JTextField> texttypes =
		new HashMap<String, JTextField>();
	
	private static Map<String, JCheckBox> booleans = 
		new HashMap<String, JCheckBox>();
	
	private static Map<String, JComboBox> enums =
		new HashMap<String,JComboBox>();
	
	private GridBagLayout layout;
	private GridBagConstraints left, right;
	
	private Map<String, Class> optionTypes;
	int gridy;
	
	/**
	 * A new <code>JCodecOptionPane</code> initalised
	 * with some parameters given in a Map with the
	 * parameter names and their types as classes.
	 * 
	 * @param options
	 */
	public JCodecOptionPane(Map<String,Class> options) {
		optionTypes = options;

		layout = new GridBagLayout();
		setLayout(layout);
		
		left = new GridBagConstraints();
		left.anchor = GridBagConstraints.LAST_LINE_START;
		left.weightx = 0;
		left.weighty = 0;
		left.gridx = 0;
		left.fill = GridBagConstraints.HORIZONTAL;
		left.insets = new Insets(0,0,0,5);
		
		right = new GridBagConstraints();
		right.anchor = GridBagConstraints.LINE_END;
		right.weightx = 1;
		right.weighty = 0;
		right.gridx = 1;
		right.fill = GridBagConstraints.HORIZONTAL;
		right.insets = new Insets(0,5,0,0);
		gridy = 0;
		constructOptionPanel();
	}
	
	
	/**
	 * Sets the default value of a certain parameter.
	 * 
	 * @param parameter the parameter name
	 * @param value the default value as string
	 */
	public void setDefault(String parameter, String value) {
		if(value != null) {
			if( booleans.containsKey(parameter) ) {
				booleans.get(parameter).setSelected(
						Boolean.parseBoolean(value));
			} else if( enums.containsKey(parameter)) {
				enums.get(parameter).setSelectedItem(value);
			} else if ( texttypes.containsKey(parameter) ) {
				texttypes.get(parameter).setText(value);
			}
		}
	}


	/**
	 * This constructs the panel by iterating over
	 * the parameters and their classes and assinging each
	 * parameter an appropriate SWING component.
	 * The left side in the grid always consists of a 
	 * label containing the parameter name, the right side
	 * is filled with the according SWING component.
	 *
	 */
	private void constructOptionPanel() {
		
		
		// to make sure that the panel always looks
		// the same way
		List<String> optionnames = new ArrayList<String>(optionTypes.keySet());
		Collections.sort(optionnames);
		
		// for each parameter...
		for( String opt : optionnames ) {
	
			// retrieve its type
			Class optclass = optionTypes.get(opt);
			left.gridy = gridy;
			right.gridy = gridy;
			if( optclass == Boolean.TYPE ) {
				// represent a boolean as checkbox 
				JCheckBox box = new JCheckBox();
				JLabel label = new JLabel(opt);
				
				layout.setConstraints(label, left);
				add(label);
				layout.setConstraints(box, right);
				add(box);
				
				
				booleans.put(opt, box);
			} else if( optclass.isEnum() ) {
				
				JLabel label = new JLabel(opt + ":");
				layout.setConstraints(label, left);
				add(label);
				
				// fill all the enum constans of a enum
				// type in a drop down menu
				Object[] constants = optclass.getEnumConstants();
				Vector<String> stringvals = new Vector<String>(constants.length);
				for( Object cos : constants )  {
					stringvals.add(cos.toString());
				}
				JComboBox box = new JComboBox(stringvals);
				layout.setConstraints(box, right);
				add(box);
				enums.put(opt,box);
			} else {
				
				// assing everything else a text field
				JLabel label = new JLabel(opt + ":");
				layout.setConstraints(label, left);
				add(label);
				JTextField line = new JTextField();
				layout.setConstraints(line,right);
				add(line);
				texttypes.put(opt,line);
			}
			gridy++;
		}
		if(optionnames.isEmpty()) {
			
			// construct a field indicating that there
			// are no options
			GridBagConstraints empty = new GridBagConstraints();
			empty.anchor = GridBagConstraints.CENTER;
			empty.fill = GridBagConstraints.VERTICAL;
			empty.gridy = 0;
			
			JLabel firstline = new JLabel("This Codec has no");
			layout.setConstraints(firstline, empty);
			add(firstline);
			
			empty.gridy = 1;
			JLabel secondline = new JLabel("options to set.");
			layout.setConstraints(secondline, empty);
			
			add(firstline);
			add(secondline);
			
		} 
		
		
		doLayout();
		validate();
	}
	
	/**
	 * Returns the user selected options in 
	 * String representation.
	 * 
	 * @return a Map containg the parameter names and their values as string
	 */
	public Map<String,String> getOptionMap() {
		Map<String,String> ret = new HashMap<String,String>();
		
		for(String opt : booleans.keySet() ) {
			ret.put(opt, Boolean.toString(booleans.get(opt).isSelected()));
		}
		
		for(String opt : enums.keySet() ) {
			ret.put(opt, enums.get(opt).getSelectedItem().toString());
		}
		
		for(String opt : texttypes.keySet()) {
			ret.put(opt,texttypes.get(opt).getText());
		}
		
		return ret;
	}
	
	/**
	 * Constructs a command line string out of 
	 * the parameters and their values.
	 * 
	 * @return
	 */
	public String getOptionString() {
		StringBuffer ret = new StringBuffer();
		boolean first = true;
		
		for(String opt : booleans.keySet() ) {
			if(first) {
				first = false;
			} else {
				ret.append(", ");
			}
			ret.append(opt + ":" + booleans.get(opt).isSelected());
		}
		
		for(String opt : enums.keySet() ) {
			if(first) {
				first = false;
			} else {
				ret.append(", ");
			}
			ret.append(opt + ":" + enums.get(opt).getSelectedItem());
		}
		
		for(String opt : texttypes.keySet() ) {
			if(first) {
				first = false;
			} else {
				ret.append(", ");
			}
			ret.append(opt + ":" + texttypes.get(opt).getText());
		}
		
		return ret.toString();
	}
	
}
