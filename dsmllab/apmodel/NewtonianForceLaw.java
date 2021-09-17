package dsmllab.apmodel;

import sim.engine.*;
import sim.util.Double2D;
import sim.portrayal.*;
import java.awt.geom.*;
import java.awt.*;
import java.util.*;
import java.io.*;

/**
 *  This class implements a Newtonian Force Law
 *  to be used by an particle. 
 *
 *  @author R. Paul Wiegand
 *  @version Version 0.1 beta
 */

public class NewtonianForceLaw extends ForceLaw {
  public double G = 1200.0;
  public double distancePower = 2.0;
  public double massPower = 1.0;
  
  public NewtonianForceLaw() {
    //stick with defaults...
  }
  
  public NewtonianForceLaw(double maxForce, double effectRange, double arBoundary) {
    super(maxForce,effectRange,arBoundary);
  }//simple constructor

  public NewtonianForceLaw(double maxForce, double effectRange, double arBoundary,
			   double G, double distancePower, double massPower) {
    super(maxForce,effectRange,arBoundary);
    this.G = G;
    this.distancePower = distancePower;
    this.massPower = massPower;
  }//more sophisticated constructor


  public ForceLaw newForceLawInstance() {return new NewtonianForceLaw();}


  /** Apply the force law from particle 1 to particle 1, computing the
   *  appropriate force vector based on the parameterized force
   *  model. */
  public Double2D apply(final Particle particle1, final Particle particle2) {
    double distance = Particle.distanceBetween(particle1,particle2);
    
    // I generalized the model in a fairly unrealistic way here ... the
    // mass factors are raised to a power (called massPower here).  While
    // true Newtonian physics doesn't do this, it allows me to easily weight
    // the importance of mass for a given instantiation of the force law.    
    double massFactor = Math.pow(particle1.mass*particle2.mass,massPower);

    //NOTE:  Here we depart from the AP model as published by
    //       providing a linear gradient to transition between
    //       the repulsor and attractor fields.  We do this
    //       using the dampingFactor variable. */
    double delta = arBoundary * 1.0;
    double dampingFactor = Math.abs(arBoundary - distance)/delta;
    double forceScalar = (G * massFactor) / Math.pow(distance,distancePower);

    // Don't let the magnitude of the force be bigger than the
    // model-level maxForce property.
    if (forceScalar > maxForce) forceScalar = maxForce;

    // Attract particles that are close-by, attract particles that are 
    // farther away than the attracting-repulsing boundary
    if(distance > arBoundary) forceScalar = -forceScalar;

    // Use this damping factor to smooth over the discontinuity
    // that occurs along the attractive-repulsing boundary.
    if (Math.abs(arBoundary - distance) < delta) forceScalar *= dampingFactor;

    Double2D forceVect = Particle.unitVectorTowards(particle2.particleLocation,particle1.particleLocation);

    return new Double2D(forceVect.x*forceScalar,forceVect.y*forceScalar);
  }//apply()


  protected void loadFromProperties(Properties properties) {
    super.loadFromProperties(properties);
    G             = getParamFromProp(properties,"G",G,0,100000.0);
    distancePower = getParamFromProp(properties,"distancePower",distancePower,-5.0,5.0);
    massPower     = getParamFromProp(properties,"massPower",massPower,0.0,10000.0);

    if (APParser.VERBOSE_LOAD)
      System.out.println("Loaded Newtonian force law from " + fromParticleName + "-" + fromParticleSubtype + 
                         " to " + toParticleName + "-" + toParticleSubtype);
  }


  protected void printInteractionInfo() {
    System.out.println("Setting Newtonian Interaction:");
    System.out.println("  fromParticle:  " + fromParticleName + ":" + fromParticleSubtype);
    System.out.println("  toParticle:    " + toParticleName + ":" + toParticleSubtype);
    System.out.println("  effectRange:   " + effectRange);
    System.out.println("  maxForce:      " + maxForce);
    System.out.println("  arBoundary:    " + arBoundary);
    System.out.println("  G:             " + G);
    System.out.println("  distancePower: " + distancePower);
    System.out.println("  massPower:     " + massPower);
  }  

}
