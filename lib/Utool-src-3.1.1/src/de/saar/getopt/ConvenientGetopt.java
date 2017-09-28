/*
 * Created on 06.08.2004
 *
 */
package de.saar.getopt;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class encapsulates the GNU Getopt library and makes it more convenient
 * to use from Java programs.
 * 
 * It hides the details of the parameter string syntax completely from the user,
 * and provides the automatic display of usage information.
 *
 */
public class ConvenientGetopt {
    /**
     * Option takes no arguments.
     */
    public final static int NO_ARGUMENT = LongOpt.NO_ARGUMENT;
    /**
     * Option takes an optional argument.
     */
    public final static int OPTIONAL_ARGUMENT = LongOpt.OPTIONAL_ARGUMENT;
    /**
     * Option takes a required argument.
     */
    public final static int REQUIRED_ARGUMENT = LongOpt.REQUIRED_ARGUMENT;
    
    // Encapsulation of the components of a single command-line option.
    private class GetoptEntry {
        String longname;         // long name, e.g. "server" (for --server option)
        char shortname;          // short name, e.g. 's' (for -s option)
        int hasArg;              // does it take arguments? 
        String description;      // description for the usage message
        String defaultValue;     // default value (if option not given by user, or optional argument)
    }
    
    private List<GetoptEntry> entriesInOrder;               // list of all entries (= entries, but ordered)
    private Set<GetoptEntry> entries;                       // set of all entries
    private Map<Character,GetoptEntry> shortnameToEntry;    // map short names to entries
    private Map<String,GetoptEntry> longnameToEntry;        // map long names to entries

    private Map<GetoptEntry,String> values;        // after parsing, this holds values for options that were given with arguments
    private Set<GetoptEntry> foundArgs;            // set of all options that were present on cmd line
    private List<String> remaining;                // list of command-line items that were not options
    
    private String progname;          // name of the program (for error messages)
    private String howToCall;         // description of program call syntax
    private String docBelow;          // optional documentation that goes below options in usage message
    
    /**
     * Create new getopt object.
     * 
     * @param progname name of the program (for error message)
     * @param howToCall description of program call syntax (or null)
     * @param docBelow documentation that goes below the options in the usage message (or null) 
     */
    public ConvenientGetopt(String progname, String howToCall, String docBelow) {
        entries = new HashSet<GetoptEntry>();
        entriesInOrder = new ArrayList<GetoptEntry>();
        values = new HashMap<GetoptEntry,String>();
        foundArgs = new HashSet<GetoptEntry>();
        shortnameToEntry = new HashMap<Character,GetoptEntry>();
        longnameToEntry = new HashMap<String,GetoptEntry>();
        remaining = new ArrayList<String>();
        
        this.progname = progname;
        this.howToCall = howToCall;
        this.docBelow = docBelow;
    }
    
    
    /**
     * Add an option that has no long name.
     * 
     * @param shortname short name of the option ('s' for option -s)
     * @param hasArg does the option have arguments? (no, optional, required)
     * @param defaultValue default value for the argument
     * @param description short description for the usage message.
     */
    public void addOption(char shortname, int hasArg, String defaultValue, String description) {
        addOption(shortname, null, hasArg, description, defaultValue);
    }
    
    /**
     * Add an option that has a long name.
     * 
     * @param shortname short name of the option ('s' for option -s)
     * @param longname long name of the option ("server" for option --server)
     * @param hasArg does the option have arguments? (no, optional, required)
     * @param defaultValue default value for the argument
     * @param description short description for the usage message.
     */
    public void addOption(char shortname, String longname, int hasArg, String description, String defaultValue) {
        GetoptEntry entry = new GetoptEntry();
        entry.shortname = shortname;
        entry.description = description;
        entry.hasArg = hasArg;
        entry.defaultValue = defaultValue;
        entry.longname = longname;
        
        entries.add(entry);
        entriesInOrder.add(entry);
        
        if( longname != null )
            longnameToEntry.put(longname, entry);
        
        shortnameToEntry.put(new Character(shortname), entry);
    }
    
     /**
      * Parse a command line.
      * 
      * If the command line was syntactically valid, this method will store
      * the information about the command line internally; it can then be retrieved 
      * with the hasArgument, getValue, and getRemaining methods.
      * 
      * If the command line was syntactically invalid, the method will print
      * the usage message and terminate the programme.
      * 
     * @param args the command-line array that the main method got.
     */
    public void parse(String[] args) {
         LongOpt[] longopts = new LongOpt[longnameToEntry.size()];
         StringBuilder optstring = new StringBuilder();
         int longoptIdx = 0;
         
         values.clear();
         foundArgs.clear();
         
         // build long and short argument descriptions
         for( GetoptEntry entry : entries ) {
             optstring.append(entry.shortname);
             if(entry.hasArg == REQUIRED_ARGUMENT ) {
                 optstring.append(":");
             } else if( entry.hasArg == OPTIONAL_ARGUMENT ) {
                 optstring.append("::");
             }
             
             if( entry.longname != null ) {
                 LongOpt longopt = new LongOpt(entry.longname, entry.hasArg, null, entry.shortname);
                 longopts[longoptIdx++] = longopt;
             }
             
             // put default entries into the value table
             values.put(entry, entry.defaultValue);
         }
         
         // parse arguments
         Getopt g = new Getopt(progname, args, optstring.toString(), longopts);
         
         // process results
         int c;
         boolean error = false;
         while ((c = g.getopt()) != -1) {
             if( c == '?' || c == ':' ) {
                 error = true;
             } else {
                 GetoptEntry entry = shortnameToEntry.get(new Character((char) c));
                 if( entry != null ) {
                     foundArgs.add(entry);
                     
                     String arg = g.getOptarg();
                     if( arg != null )
                         values.put(entry, arg);
                 }
             }
         }
         
         if( error ) {
             usage();
             System.exit(1);
         }
         
         // process leftover command-line arguments
         remaining.clear();
         for( int i = g.getOptind(); i < args.length; i++ )
             remaining.add(args[i]);
     }
     
     
     
     /**
      * Check whether an option was present on the command line.
      * 
     * @param shortname the short name of the option
     * @return true if the option was there.
     */
    public boolean hasOption(char shortname) {
        GetoptEntry entry = shortnameToEntry.get(new Character(shortname));
        return foundArgs.contains(entry);
     }
     
     /**
      * Check whether an option was present on the command line.
      * 
     * @param longname the long name of the option
     * @return true if the option was there.
     */
    public boolean hasOption(String longname) {
         GetoptEntry entry = longnameToEntry.get(longname);
         return foundArgs.contains(entry);
     }
     

     /**
      * Retrieve the value of an option. If the option was not given on
      * the command line, or the option takes an optional argument and the
      * argument was not given, this method will return the default value
      * for this option.
      * 
     * @param longname the long name of the option
     * @return the value of the option; possibly the default value.
     */
    public String getValue(String longname) {
         GetoptEntry entry = longnameToEntry.get(longname);

         if( entry != null )
             return values.get(entry);
         else
             return null;
     }
     
    /**
     * Retrieve the value of an option. If the option was not given on
     * the command line, or the option takes an optional argument and the
     * argument was not given, this method will return the default value
     * for this option.
     * 
    * @param shortname the short name of the option
    * @return the value of the option; possibly the default value.
    */
     public String getValue(char shortname) {
         GetoptEntry entry = shortnameToEntry.get(new Character(shortname));
         
         if( entry != null ) 
             return values.get(entry);
         else
             return null;
     }
     
     /**
      * Retrieve those command-line arguments that don't belong to any option.
      * 
     * @return the list of remaining command-line arguments, in order.
     */
    public List<String> getRemaining() {
         return remaining;
     }
     
     /**
     * Print a usage string for the programme, based on the option entries.
     */
    private void usage() {
         char[] whitespace = 
            "                                                                                           ".toCharArray();
         final int paddingLength = 32;
         
         if( howToCall == null ) {
             System.err.println("Usage: " + progname + " [options]");
         } else {
             System.err.println("Usage: " + howToCall);
         }
         
         System.err.println("\nOptions:");
         for( GetoptEntry entry : entriesInOrder ) {
             StringBuffer line = new StringBuffer("   ");
             boolean hasShortname = false;
             
             if( Character.isLetterOrDigit(entry.shortname) ) {
            	 line.append("-" + entry.shortname);
            	 hasShortname = true;
             }	 
             
             if( entry.longname != null ) {
            	 if( hasShortname ) {
            		 line.append(", ");
            	 }
            	 
                 line.append("--" + entry.longname);
             }
             
             if( entry.hasArg == REQUIRED_ARGUMENT ) {
                 line.append(" <arg>");
             } else if( entry.hasArg == OPTIONAL_ARGUMENT ) {
                 line.append(" [<arg>]");
             }
             
             if( line.length() >= paddingLength) {
            	 line.append(" ");
             } else {
            	 // pad with whitespace
            	 line.append(whitespace, 0, paddingLength-line.length()); 
             }
             
             if( entry.description != null ) {
                 line.append(entry.description);
             }
   
             if( entry.defaultValue != null ) {
                 line.append (" (default: " + entry.defaultValue + ")");
             }
           
             System.err.println(line);
             
         }
         
         if( docBelow != null ) {
             System.err.println("\n" + docBelow);
         }
     }
}
