package dsmllab.apmodel;

import sim.engine.*;
import sim.util.Double2D;
import sim.portrayal.*;
import java.awt.geom.*;
import java.awt.*;
import java.util.*;
import java.io.*;


/**
 *  This base class implements a base class for defining a force law
 *  to be used by an particle.  It assumes only three basic properties.
 *  First, it is assumed that there is a maximum range of effect for 
 *  each force law.  Second, that there is a boundary inside this 
 *  effect range wherein attractive forces switch to repelling forces
 *  ... that is, inside the ARBoundary forces repel, outside they
 *  attract.  Finally, it is assumed there is a maximum scalar force
 *  limiting the force law in some way.
 *
 *  @author R. Paul Wiegand
 *  @version Version 0.1 beta
 */

public abstract class ForceLaw implements Parsable {
  public static APParser apparser; // Ugly hack ... it is set by APPaser's constructor

  protected double effectRange = 100.0;
  protected double arBoundary  = 50;
  protected double maxForce    = 100000.0;

  // Used by APParser to automatically register laws defined in
  // the params file.
  protected String fromParticleName;
  protected int fromParticleSubtype;
  protected String toParticleName;
  protected int toParticleSubtype;

  //Accessor methods
  public double getEffectRange() {return effectRange;}
  public double getARBoundary()  {return arBoundary;}
  public double getMaxForce()    {return maxForce;}

  public ForceLaw() {
    // take the defaults
  }

  public ForceLaw(double maxForce, double effectRange, double arBoundary) {
    this.maxForce = maxForce;
    this.effectRange = effectRange;
    this.arBoundary = arBoundary;
  }//constructor

  public abstract ForceLaw newForceLawInstance();

  public abstract Double2D apply(final Particle particle1, final Particle particle2);


  // --- Everything from here down has to do with loading from the parser ----
  protected void loadFromProperties(Properties properties) {
    effectRange   = getParamFromProp(properties,"effectRange",effectRange,0,100000.0);
    maxForce      = getParamFromProp(properties,"maxForce",maxForce,0,100000.0);
    arBoundary    = getParamFromProp(properties,"arBoundary",arBoundary,0,100000.0);
  }

  public Vector createCopiesFromParse(String instantiationValues) {
    Vector laws = new Vector();

    Properties properties = new Properties();
    try {
      StringBufferInputStream sbis = new StringBufferInputStream(instantiationValues);
      //       StringReader sr = new StringReader(instantiationValues);
      //       BufferedInputStream bis = new BufferedInputStream(sr);
      properties.load(sbis);
    }
    catch (Exception e) {
      System.out.println("Error while creating from the parse of " + getClass().getName() + ": " + e);
    }

    fromParticleName = properties.getProperty("fromParticleName","ncarai.apmodel.Particle");
    String strFromSubtype = properties.getProperty("fromParticleSubtype","0");
    toParticleName = properties.getProperty("toParticleName","ncarai.apmodel.Particle");
    String strToSubtype = properties.getProperty("toParticleSubtype","0");

    // The subtypes are specified numberically
    if ((strFromSubtype.compareTo("*")!=0)  && (strToSubtype.compareTo("*")!=0) ) {
      fromParticleSubtype = (int)(new Double(strFromSubtype)).doubleValue();
      toParticleSubtype = (int)(new Double(strToSubtype)).doubleValue();
      loadFromProperties(properties);
      laws.add(this);
    }

    // A wildcard is used for 'from' subtype, the 'to' subtype is specified
    else if ((strFromSubtype.compareTo("*")==0)  && (strToSubtype.compareTo("*")!=0) ) {
      int subtypes[] = apparser.getAllRegisteredParticleSubtypes(fromParticleName);
      for (int i=0; i<subtypes.length; i++) {
	ForceLaw fl = newForceLawInstance();
        fromParticleSubtype = subtypes[i];
        toParticleSubtype = (int)(new Double(strToSubtype)).doubleValue();

	fl.fromParticleName = fromParticleName;
	fl.fromParticleSubtype = fromParticleSubtype;
	fl.toParticleName = toParticleName;
	fl.toParticleSubtype = toParticleSubtype;
	fl.loadFromProperties(properties);
	laws.add(fl);
      }
    }

    // A wildcard is used for 'to' subtype, the 'from' subtype is specified
    else if ((strFromSubtype.compareTo("*")!=0)  && (strToSubtype.compareTo("*")==0) ) {
      int subtypes[] = apparser.getAllRegisteredParticleSubtypes(toParticleName);
      for (int i=0; i<subtypes.length; i++) {
	ForceLaw fl = newForceLawInstance();
        fromParticleSubtype = (int)(new Double(strFromSubtype)).doubleValue();
        toParticleSubtype = subtypes[i];

	fl.fromParticleName = fromParticleName;
	fl.fromParticleSubtype = toParticleSubtype;
	fl.toParticleName = toParticleName;
	fl.toParticleSubtype = toParticleSubtype;
	fl.loadFromProperties(properties);
	laws.add(fl);
      }
    }

    else
      System.err.println("ERROR:  Both " + fromParticleName + " and " + toParticleName + 
                         " are configured using a wildcard for the subtype information.  " +
                         "The parser can deal with one or the other, but not both.");


    return(laws);
  }


  public void registerWithModel(ForceInteractionModel forceInteractionModel,
				APModel ap) {
    int fromType = forceInteractionModel.getParticleType(fromParticleName,fromParticleSubtype);
    int toType = forceInteractionModel.getParticleType(toParticleName,toParticleSubtype);
    if (fromType < 0) {
      System.err.println("Error:  Could not find " + fromParticleName + "-" + fromParticleSubtype +
			 " in forceInteractionModel registry.");
      return;
    }
    if (toType < 0) {
      System.err.println("Error:  Could not find " + toParticleName + "-" + toParticleSubtype +
			 " in forceInteractionModel registry.");
      return;
    }

    forceInteractionModel.setInteractionFromTo(fromType,toType,this);

    if (APParser.VERBOSE_LOAD) printInteractionInfo();
  }

  protected void printInteractionInfo() {
    System.out.println("Setting Force Law Interaction:");
    System.out.println("  fromParticle:  " + fromParticleName + ":" + fromParticleSubtype);
    System.out.println("  toParticle:    " + toParticleName + ":" + toParticleSubtype);
    System.out.println("  effectRange:   " + effectRange);
  }

  protected double getParamFromProp(Properties properties, String key, 
                                    double defaultVal, double lb, double ub) {
    double rawValue = (new Double(properties.getProperty(key,"" + defaultVal))).doubleValue();

    if (rawValue < lb) rawValue = lb;
    if (rawValue > ub) rawValue = ub;

    return (rawValue);
  }

}

