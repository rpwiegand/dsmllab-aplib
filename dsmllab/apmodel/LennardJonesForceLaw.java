package dsmllab.apmodel;

import sim.engine.*;
import sim.util.Double2D;
import sim.portrayal.*;
import java.awt.geom.*;
import java.awt.*;
import java.util.*;
import java.io.StringBufferInputStream;

/**
 *  This class implements a Newtonian Force Law
 *  to be used by an particle. 
 *
 *  @author R. Paul Wiegand
 *  @version Version 0.1 beta
 */

public class LennardJonesForceLaw extends ForceLaw implements Parsable {
  public static final boolean VERBOSE_LOAD = true;

  public static double d = 0.5;
  public static double c = 0.5;
  public static double epsilon = 1.0;
  
  public LennardJonesForceLaw() {
    // Take the defaults
  }

  public LennardJonesForceLaw(double maxForce, double effectRange, double arBoundary) {
    super(maxForce,effectRange,arBoundary);
  }

  public LennardJonesForceLaw(double maxForce, double effectRange, double arBoundary,
                              double d, double c, double epsilon, int numParticleTypes) {
    super(maxForce,effectRange,arBoundary);
    this.d = d;
    this.c = c;
    this.epsilon = epsilon;    
  }//constructor

    
  public ForceLaw newForceLawInstance() {return new LennardJonesForceLaw();}


  public Double2D apply(final Particle particle1, final Particle particle2) {
    double distance = Particle.distanceBetween(particle1,particle2);

    double repelTerm = (2.0 * d * Math.pow(arBoundary,12.0)) /
      (Math.pow(distance,13.0));

    double attractTerm = (c * Math.pow(arBoundary,6.0)) /
      (Math.pow(distance,7.0));

    double forceScalar = 24 * epsilon * (repelTerm - attractTerm);
    
    // Don't let the magnitude of the force be bigger than the
    // model-level maxForce property.
    if (forceScalar > maxForce) forceScalar = maxForce;

    Double2D forceVect = Particle.unitVectorTowards(particle2.particleLocation,particle1.particleLocation);

    return new Double2D(forceVect.x*forceScalar,forceVect.y*forceScalar);
  }//apply()


  protected void loadFromProperties(Properties properties) {
    super.loadFromProperties(properties);
    d             = getParamFromProp(properties,"d",d,0,1);
    c             = getParamFromProp(properties,"c",c,0,1);
    epsilon       = getParamFromProp(properties,"epsilon",epsilon,0.0,100.0);

    if (APParser.VERBOSE_LOAD) 
      System.out.println("Loaded Lennard-Jones force law from " + fromParticleName + "-" + fromParticleSubtype + 
                         " to " + toParticleName + "-" + toParticleSubtype);
  }

  protected void printInteractionInfo() {
    System.out.println("Setting Lennard-Jones Interaction:");
    System.out.println("  fromParticle:  " + fromParticleName + ":" + fromParticleSubtype);
    System.out.println("  toParticle:    " + toParticleName + ":" + toParticleSubtype);
    System.out.println("  effectRange:   " + effectRange);
    System.out.println("  maxForce:      " + maxForce);
    System.out.println("  arBoundary:    " + arBoundary);
    System.out.println("  c:             " + c);
    System.out.println("  d:             " + d);
    System.out.println("  epsilon:       " + epsilon);
  }

}

