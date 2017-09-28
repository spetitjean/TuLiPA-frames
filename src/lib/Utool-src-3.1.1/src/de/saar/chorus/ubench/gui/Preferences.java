package de.saar.chorus.ubench.gui;

/**
 * This contains several preferences for layouting and solving 
 * graphs and a master object containing the general used
 * preferences.
 * Every <code>JDomGraph</code> has a <code>Preferences</code>
 * object of its own and has to be repaintet, if its preferences
 * are not aligned to the global master preferences.
 * 
 * The global preferences concerning layout and solving can be
 * changed by using the menu checkboxes of the <code>JDomGraphMenu</code>.
 * The default values are <code>true</code> for showing labels and
 * automatical solving and <code>false</code> for automatical 
 * window fitting.
 * 
 * @author Alexander Koller
 *
 */
public class Preferences implements Cloneable {
    
    // static fields: per-application preferences 
    private static boolean autoCount = true;
    private static boolean fitToWindow = false;
    
    // non-static fields: specific to each graph
    private boolean showLabels;
    
    
    
    // the global preferences
    private static Preferences master;

    /**
     * Creating a new Preferences object
     * with the default values.
     */
	public Preferences() {
		showLabels = true;
	}
	
    
    /******** accessor methods **************/
	
	/**
	 * @return true if node labels are shown (default)
	 */
    public boolean isShowLabels() {
		return showLabels;
	}
	
    /**
     * Setting the parameter indicating whether node labels
     * or node names are shown. If set to false, the node names
     * are shown instead of the node labels.
     * (Default: <code>true</code>, node labels are shown)
     * 
     * @param showLabels set this to false for showing node names
     */
	public void setShowLabels(boolean showLabels) {
		this.showLabels = showLabels;
	}
    
	
  
    
    /**
     * Set to true, fitWindow will cause the visible and
     * all further opened graphs to be zoomed out if they 
     * oversize their tab. 
     * Default: <code>false</code>.
     * 
     * @param fit
     */
    public static void setFitToWindow(boolean fit) {
        fitToWindow = fit;
    }
    
    /**
     * 
     * @return true if all graphs are automatically solved
     */
    public static boolean isAutoCount() {
        return autoCount;
    }

    /**
     * This can enable or disable the automatical solving /
     * counting of solved forms. 
     * If disabled, a menu item for "manual" solving / 
     * solved form counting should get enabled!     
     * Default: <code>true</code> (automatical counting)
     * 
     * @param xautoCount 
     */
    public static void setAutoCount(boolean xautoCount) {
        autoCount = xautoCount;
    }

    /**
     * 
     * @return true if the graphs shall be fitted into their tab
     */
    public static boolean isFitToWindow() {
        return fitToWindow;
    }
    

 




    /*********** instance management methods **************/
    
    /**
     * Returns the master <code>Preferences</code> object
     * which is a new one if there is no master yet.
     * 
     * @return the master Preferences object
     */
    public static Preferences getInstance() {
        if( master == null ) {
            master = new Preferences();
        }
        
        return master;
    }
    
    /**
     * Clones this.
     * 
     * @return a clone of this Preferences
     */
    public Preferences clone() {
        try {
            return (Preferences) super.clone();
        } catch (CloneNotSupportedException e) {
            // This shouldn't happen because we are Cloneable.
            throw new InternalError();
        }
    }
    
    /**
     * Compares the given Preferences object with this one.
     * 
     * @param previousLayoutPreferences the preferences to compare to
     * @return true if the previous preferences are out of date
     */
    public static boolean mustUpdateLayout(Preferences previousLayoutPreferences) {
        return (previousLayoutPreferences.showLabels != getInstance().showLabels);
    }

    
    /**
     * Copies these preferences to a second given
     * <code>Preferences</code> object.
     * 
     * @param second the Preferences to copy the values to
     */
	public void copyTo(Preferences second) {
        second.showLabels = showLabels;
	}


}
