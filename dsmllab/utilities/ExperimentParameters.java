//
// ExperimentParameters.java 
//

package dsmllab.utilities;

import java.lang.reflect.*;
import java.lang.Class;
import java.util.*;
import java.io.*;


/** This class is a generic base class for collecting parameters from a 
 *  parameter file.  To use it, simply inherit from this and declare
 *  public, non-static variables in child class.  The code that uses
 *  this child parameters class will have to create an instance of it
 *  then call the method readParams().  Afterward, all public variables
 *  will be populated from the file specified by <i>-Dexperiment=<b>[filename]</b></i>
 *  given to the JRE at runtime.  The default file is "test.exp".  Default
 *  values for the parameters can be provided by the programmer of the 
 *  parameters class by simply assigning a value to the public member
 *  variable in the declaration. 
 *
 *  Parameters in the file are read with the following syntax:
 *  
 *    <i><b>[classname]</b>.<b>[parametername]</b>=<b>[parametervalue]</b></i>
 *
 *  The advantage is that you can produce decending ExperimentParameters
 *  classes that inhererit the ability to read the parmaeters in their
 *  parent, etc.
 *
 *  @author R. Paul Wiegand
 */

public class ExperimentParameters implements Serializable
{
  // If this is on, it matches key strings in the file as classname.fieldname=value
  // If it is off, it matches the key strings in the file as fieldname=value
  public static boolean USE_CLASSNAME_IN_FIELDNAME = false;
  public static PrintWriter statusOut = new PrintWriter(System.out, true);
  public static String username;

  protected static Properties properties = null;
  protected static boolean mIsLoaded = false;

  // The paramFile member variable will not be overwritten during the read.
  public String paramFile = "test.exp";

  public String notes = "This is a base ExperimentParameters field";


  /** This constructor establishes the name of the parameter file by
   *  being explicitly told from the calling code.  It <i>does not</i>
   *  read the file.
   *
   *  @param paramFile*/
  public ExperimentParameters(String paramFile) {
    this.paramFile = paramFile;
    mIsLoaded = false;
  }


  /** This constructor establishes the name of the parameter file by
   *  assuming it has been provided at runtime by using the
   *  <i>-Dexperiment=[<b>filename</b>]</i> syntax.  It <i>does not</i>
   *  read the file. */
  public ExperimentParameters() {
    this.paramFile = System.getProperty("experiment",this.paramFile);
    mIsLoaded = false;
  }

  // This methods are meant to be overridden by the child class if the
  // user needs to perform some actions prior to or just after field
  // population.  For instance, a child class can change defaults by
  // overridding the prePopulateHook() method or constraint values
  // in the postPopulateHood() method.
  protected void prePopulateHook() {}
  protected void postPopulateHook() {}

  /** This method forces the parameters to be read (it it isn't
   *  already), populates all the fields defined in the instance class
   *  and all parent classes up to ExperimentParameters.  The
   *  parameters cannot be automaticaly loaded by the constructor of
   *  ExperimentParameters because the subordinate classes will not
   *  have populated those variables with their defaults yet. */
  public void loadParameters() {
    if (properties == null) readParameters();
    prePopulateHook();
    populateAllFields();
    postPopulateHook();
    printAllFields();
    mIsLoaded = true;
  }

  public boolean isLoaded() {return mIsLoaded;}

  /** This function causes the class to read the parameters from the <i>paramsFile</i>
   *  and populate the internal <i>properties</i> member variable. */
  public void readParameters() {
    String userDir = System.getProperty("user.dir", "./");
    statusOut.println("Looking for parameters file in " + userDir + " ...");
        
    username = System.getProperty("user.name", "wiegand");
        
    // Load properties from file...
    properties = new Properties();
    try {    
      File configFile = new File(userDir,paramFile);
      statusOut.println("Loading parameters from " + paramFile + " ...");
      properties.load(new FileInputStream(configFile));
    } 
    catch (IOException e) {
      System.err.println("IO Exception reading " + paramFile);
      System.err.println("  " + e.getMessage());
      System.err.println("  Using default paramters from " + this.getClass().getName());
    }
  }
  
    

    /** This method uses reflection to attempt to populate all public, non-static
     *  member variables from the <i>properties</i>.  Currently, it only supports
     *  int, long, float, double, boolean, String, double[], and String[]. */
    protected void populateAllFields() {
      Class loadedClass = this.getClass();
      String className = loadedClass.getName();
      String fieldName = "";

        try {
	  Field fields[] = loadedClass.getFields();
            for (int i=0; i<fields.length; i++) {
                int modifiers = fields[i].getModifiers();
      
                // Consider only public level member variables
                if (  Modifier.isPublic(modifiers) && 
                     !Modifier.isStatic(modifiers) &&
		      (fields[i].getName().compareTo("paramFile") !=0) ) { 
                    fieldName = fields[i].getName();
		    if (USE_CLASSNAME_IN_FIELDNAME) fieldName = className + "." + fieldName;
                    Class  type = fields[i].getType();
                    String typeName = type.getName();
                    Object value = fields[i].get(this); 

		    if (typeName.compareToIgnoreCase("int") == 0) {
		      int x = getIntProp(fieldName,value.toString());
                      fields[i].setInt(this,x);
		    }

		    else if (typeName.compareToIgnoreCase("long") == 0) {
		      long x = getLongProp(fieldName,value.toString());
                      fields[i].setLong(this,x);
		    }

		    else if (typeName.compareToIgnoreCase("float") == 0) {
		      float x = (float) getDblProp(fieldName,value.toString());
                      fields[i].setFloat(this,x);
		    }

		    else if (typeName.compareToIgnoreCase("double") == 0) {
		      double x = getDblProp(fieldName,value.toString());
                      fields[i].setDouble(this,x);
		    }

		    else if (typeName.compareToIgnoreCase("boolean") == 0) {
		      boolean x = getBoolProp(fieldName,value.toString());
                      fields[i].setBoolean(this,x);
		    }

		    else if (typeName.compareToIgnoreCase("java.lang.String") == 0) {
		      String x = properties.getProperty(fieldName,value.toString());
                      fields[i].set(this,x);
		    }

		    else if (typeName.compareToIgnoreCase("[D") == 0) {
		      double x[] = getDblArrayProp(fieldName,dblArrayToString((double[])value));
		      fields[i].set(this,x);
		    }

		    else if (typeName.compareToIgnoreCase("[I") == 0) {
		      int x[] = getIntArrayProp(fieldName,intArrayToString((int[])value));
		      fields[i].set(this,x);
		    }

		    else if (typeName.compareToIgnoreCase("[Ljava.lang.String;") == 0) {
		      String x[] = getStringArrayProp(fieldName,stringArrayToString((String[])value));
		      fields[i].set(this,x);
		    }

		    else
		      statusOut.println("  *Failed to load parameter " + fieldName + ", type not understood");

                }
            }
        }
        catch (Exception e) {
            System.err.println("ERROR:  could not process field " + fieldName +", " + 
			       e.toString());
            System.exit(1);
        }
    }

    
    protected static boolean sysPropsPrinted = false;

    /** This method uses reflection to attempt to print all public, non-static
     *  member variables from the <i>properties</i>.  Currently, it supports
     *  all native types, as well as anything with a toString() method.  Aslo,
     *  it explicitly assumes a display format for double[], and String[]. */
    protected void printAllFields() {
        Class loadedClass = this.getClass();
	String className = loadedClass.getName();

	// Make sure this routine only gets called once.
	if (sysPropsPrinted) {
          statusOut.println("Experiment Parameters:");
	  statusOut.println("  java.class.path=" + System.getProperty("java.class.path"));
	  statusOut.println("  user.name=" + username);
	  sysPropsPrinted = false;
	}

        try {
	  Field fields[] = loadedClass.getFields(); //loadedClass.getDeclaredFields(); 
            for (int i=0; i<fields.length; i++) {
                int modifiers = fields[i].getModifiers();
      
                // print only public level member variables
                if (  Modifier.isPublic(modifiers) && 
                     !Modifier.isStatic(modifiers) ) { 
  		    String decClassName = fields[i].getDeclaringClass().getName();
		    String fieldName =  fields[i].getName();
		    if (USE_CLASSNAME_IN_FIELDNAME) fieldName = decClassName + "." + fieldName;
                    Class  type = fields[i].getType();
                    String typeName = type.getName();
                    Object value = fields[i].get(this);         
		    if (typeName.compareToIgnoreCase("[D") == 0)
		      statusOut.println("  double[] " + fieldName + "=" + 
                                           dblArrayToString((double[])value));
		    else if (typeName.compareToIgnoreCase("[I") == 0)
		      statusOut.println("  int[] " + fieldName + "=" + 
                                           intArrayToString((int[])value));
		    else if (typeName.compareToIgnoreCase("[Ljava.lang.String;") == 0)
		      statusOut.println("  String[] " + fieldName + "=" + 
                                           stringArrayToString((String[])value));
		    else
                      statusOut.println("  " + typeName + " "+ fieldName + "=" + value);
                }
            }
        }
        catch (Exception e) {
            System.err.println("ERROR:  could not process field" + 
			       e.toString());
            System.exit(1);
        }
    }


    // These are some somewhat useless "helper" methods for getting values
    // from the properties.  They are really here simply to make the above
    // routine more readible.  Since obtaining the parameters is only 
    // done once, up front...I am not worried about overhead.


    /**
     *  Returns property as a double.
     *
     *  @param keyName the property key
     *  @param defaultVal the the default value
     */
    public double getDblProp(String keyName,
                             String defaultVal) {
        String dblValStr = properties.getProperty(keyName, defaultVal);
        return (Double.parseDouble(dblValStr));
    }
  

    /**
     *  Returns property as an int.
     *
     *  @param keyName the property key
     *  @param defaultVal the the default value
     */
    public int getIntProp(String keyName,
                  			  String defaultVal) {
        String intValStr = properties.getProperty(keyName, defaultVal);
        return (Integer.parseInt(intValStr));
    }
    
    /**
     *  Returns property as a long.
     *
     *  @param keyName the property key
     *  @param defaultVal the the default value
     */
    public long getLongProp(String keyName,
			                      String defaultVal) {
        String longValStr = properties.getProperty(keyName, defaultVal);
        return (Long.parseLong(longValStr));
    }
  
    /**
     *  Returns property as a boolean.
     *
     *  @param keyName the property key
     *  @param defaultVal the the default value
     */
    public boolean getBoolProp(String keyName,
			                         String defaultVal) {
	    boolean returnValue = false;
      
	    String value = properties.getProperty(keyName, defaultVal);    
	    returnValue = ( (value.charAt(0) == 'T') || (value.charAt(0) == 't') || 
			                (value == "1") );
  
	return(returnValue);
    }


    /**
     *  Returns property as an array of doubles.  The format can include spaces, commas, semicolons
     *  braces, brackets, and parentheses as deliminters.
     *
     *  @param keyName the property key
     *  @param defaultVal the the default value
     */
     public double[] getDblArrayProp(String keyName, String defaultVal) {
       String value = properties.getProperty(keyName, defaultVal);
       String delims = "{}[],;:() ";

       // Count the numbers ...
       StringTokenizer st = new StringTokenizer(value,delims);
       int nNumbers = 0;
       while (st.hasMoreTokens()) {
         try {
           double x = ( Double.parseDouble(st.nextToken()) );
	         nNumbers++;
         }
         catch (java.lang.NumberFormatException e) {}
       }

       // Load the array ...
       double vect[] = new double[nNumbers];
       st = new StringTokenizer(value,delims);
       int idx = 0;
       while (st.hasMoreTokens()) 
         try {
           vect[idx] = ( Double.parseDouble(st.nextToken()) );
  	   idx++;
         }
         catch (java.lang.NumberFormatException e) {}

       return(vect);
     }


    /**
     *  Returns property as an array of integers.  The format can include spaces, commas, semicolons
     *  braces, brackets, and parentheses as deliminters.
     *
     *  @param keyName the property key
     *  @param defaultVal the the default value
     */
     public int[] getIntArrayProp(String keyName, String defaultVal) {
       String value = properties.getProperty(keyName, defaultVal);
       String delims = "{}[],;:() ";

       // Count the numbers ...
       StringTokenizer st = new StringTokenizer(value,delims);
       int nNumbers = 0;
       while (st.hasMoreTokens()) {
         try {
           int x = ( Integer.parseInt(st.nextToken()) );
	         nNumbers++;
         }
         catch (java.lang.NumberFormatException e) {}
       }

       // Load the array ...
       int vect[] = new int[nNumbers];
       st = new StringTokenizer(value,delims);
       int idx = 0;
       while (st.hasMoreTokens()) 
         try {
           vect[idx] = ( Integer.parseInt(st.nextToken()) );
  	       idx++;
         }
         catch (java.lang.NumberFormatException e) {}

       return(vect);
     }


  public String dblArrayToString(double[] x) {
    String dblArrayStr = "{";

    for (int i=0; i<(x.length-1); i++)
      dblArrayStr = dblArrayStr + x[i] + ",";

    dblArrayStr = dblArrayStr + x[x.length-1]+"}";

    return (dblArrayStr);
  }


  public String intArrayToString(int[] x) {
    String intArrayStr = "{";

    for (int i=0; i<(x.length-1); i++)
      intArrayStr = intArrayStr + x[i] + ",";

    intArrayStr = intArrayStr + x[x.length-1]+"}";

    return (intArrayStr);
  }


  public String stringArrayToString(String[] x) {
    String dblArrayStr = "{";

    for (int i=0; i<(x.length-1); i++)
      dblArrayStr = dblArrayStr + x[i] + ",";

    dblArrayStr = dblArrayStr + x[x.length-1]+"}";

    return (dblArrayStr);
  }

    /**
     *  Returns property as an array of doubles.  The format can include spaces, commas, semicolons
     *  braces, brackets, and parentheses as deliminters.
     *
     *  @param properties the property list
     *  @param keyName the property key
     *  @param defaultVal the the default value
     */
     public String[] getStringArrayProp(String keyName, String defaultVal) {
       String value = properties.getProperty(keyName, defaultVal);
       String delims = "{}[],;:() ";

       // Count the numbers ...
       StringTokenizer st = new StringTokenizer(value,delims);
       int nStrings = 0;
       while (st.hasMoreTokens()) {
         try {
	   String x = st.nextToken();
	   nStrings++;
         }
         catch (Exception ex) {}
       }

       // Load the array ...
       String vect[] = new String[nStrings];
       st = new StringTokenizer(value,delims);
       int idx = 0;
       while (st.hasMoreTokens()) 
         try {
           vect[idx] = st.nextToken();
  	   idx++;
         }
         catch (Exception e) {}

       return(vect);
     }


  public static void main(String args[]) {
    ExperimentParameters expParams = new ExperimentParameters();
    expParams.loadParameters();
  }


}
