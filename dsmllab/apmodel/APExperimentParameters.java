
//
// ExperimentParameters.java 
//

package dsmllab.apmodel;

import java.lang.reflect.*;
import java.lang.Class;
import java.util.*;
import java.io.*;

import dsmllab.utilities.ExperimentParameters;


/**
 *  This class is mainly a record for the experimental parameters.  It
 *  contains one method (its constructor) which reads the experiment
 *  data from a file.
 *
 *  @author R. Paul Wiegand
 */
public class APExperimentParameters extends ExperimentParameters implements Serializable
{
  // Define the simulation params
  public int numParticles = 10;
  public double friction = 0.15;
  public double particleMass = 1.0;
  public double visionRange = 60;
  public double attRepBound = 30;
  public double G = 1200;
  public double distPower = 2.0;
  public double massPower = 1.0;

  // System-level parmaeters
  public String  apParamFileName = "hex.params"; 
  public int numSteps = 1000;
  public int numSimTrials = 1;
  public boolean readFromParamFile = true;
  public String username = "ENV_NAME"; 
  protected  String defaultMeasures[] = {};

  public APExperimentParameters() { super(); }
  public APExperimentParameters(String paramFile) {super(paramFile);}

  // Override to set the default measures ...
  protected String[] getDefaultMeasures() {
    String defaultMsrs[] = {};
    return defaultMsrs;
  }


  protected void postPopulateHook() {
    String msrs[] = getDefaultMeasures();
    if (msrs.length > 0) {
      String defaultMsrString = stringArrayToString(getDefaultMeasures());
      registerAllMeasures(getStringArrayProp("measures",defaultMsrString));
    }
    if (username.compareTo("ENV_NAME") == 0) 
      username = System.getProperty("user.name", "wiegand");
  }


  protected void registerAllMeasures(String measureIDs[]) {
    if (APModel.measures == null) {
      APModel.measures = new MeasureRegistry();
      for (int i=0; i<measureIDs.length; i++) {
	try {
	  Class c = Class.forName("ncarai.apmodel." + measureIDs[i]);
	  AbstractMeasure msr = (AbstractMeasure)c.newInstance();
          APModel.measures.registerMeasure(msr);
	}//try
	catch (Exception ex) {
	  System.err.println("Error:  Could not load measure " + measureIDs[i]);
	}//catch
      }//for
    }//if
  }
    

}
