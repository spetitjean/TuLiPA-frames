package de.saar.basic;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * A <code>FileFilter</code> which can be customised with an
 * arbitrary file type and which generates a standard
 * description do appear in a </code>JFileChooser</code>. 
 * 
 * @author Alexander Koller
 * @see javax.swing.JFileChooser
 * @see javax.swing.filechooser.FileFilter
 *
 */
public class GenericFileFilter extends FileFilter implements Comparable {
	private String extension;
	private String desc;
	
	/**
	 * Create a <code>GenericFileFilter</code> accepting
	 * the file type described.
	 * 
	 * @param extension the extension of the file type
	 * @param desc a description of the file type
	 */
	public GenericFileFilter(String extension, String desc) {
		if( extension.startsWith(".") )
			this.extension = extension;
		else
			this.extension = "." + extension;
		
		this.desc = desc;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean accept(File f) {
		String fileName = f.getName();
		
		if( f.isDirectory() ) {
			return true;
		} 
		
		if(fileName.endsWith(extension) ) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Returns the description of this file filter, as displayed in the
	 * dropdown list.
	 * 
	 * @return
	 */
	public  String getDescription() {
		return desc + " files (*" + extension + ")";
	}
    
	/**
	 * Returns the name of the file type (e.g., the codec name, not the
	 * extension) of this file filter.
	 * 
	 * @return
	 */
	public String getName() {
		return desc;
	}
	
	/**
	 * Returns the filename extension associated with this file filter.
	 * 
	 * @return
	 */
	public String getExtension() {
		return extension;
	}
	
	public int compareTo(Object o) {
		return desc.compareTo( 
				((GenericFileFilter)o).desc);
	}
	
}