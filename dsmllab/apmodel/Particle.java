package dsmllab.apmodel;

import sim.engine.*;
import sim.util.Double2D;
import sim.util.Bag;
import sim.portrayal.*;
import java.awt.geom.*;
import java.awt.*;

import java.lang.reflect.*;
import java.lang.Class;
import java.util.*;
import java.io.*;

/**
 *  This base class implements a generic particle/particle.  The particle contains supporting
 *  routines to allow derived particles (e.g., Protector, Ship) to implement
 *  Bill &amp; Diana Spears' Artificial Physicomemetics system.
 *
 *  The system is a little backwards here.  Each particle is responsible for
 *  imparting its force on the surrounding particles which it affects.  This means
 *  that if the method impartForce() is not called in the step() method, this
 *  particle/particle will have no affect on other particles/particles.  The forces
 *  are accumulated, and when the velocity is updated, the cumulative force
 *  is cleared for the new round.  
 *
 *  This allows several conveniences.  First, we don't have to check which
 *  particle it is in order to know which properties to use.  The particle
 *  called upon knows its own properties, etc.  Second, there is no need
 *  for any model-level method to handle this; everything can be handled
 *  between local particles.
 *
 *  @author R. Paul Wiegand
 *  @version Version 0.1 beta
 */

public class Particle extends SimplePortrayal2D implements Steppable, Parsable {
  public String id;
  protected int particleType    = -1;
  protected int particleSubtype = -1;  
  protected Color particleColor = new Color(255,0,0);

  // Physical state information
  public Double2D particleLocation; 
  public Double2D lastLocation;
  public Double2D particleVelocity;
  public Double2D forceOnParticle;

  // Properties of the particle/particle
  public double mass = 1.0;
  public double maxVelocity = 2.0;
  public double diameter = 4.0;	
  public boolean useMomentum = true;
  public boolean useFriction = true;
  protected double friction = 0.15;

  public ForceInteractionModel forceInteractionModel = null;

  /** Constructor for the particle, setting several key properties. */
  public Particle(String id, Double2D location, double mass, int subtype,
                  ForceInteractionModel forceInteractionModel) {
    this.id = id;
    this.particleLocation = location;
    this.particleVelocity = new Double2D(0,0);	
    this.forceOnParticle = new Double2D(0,0);
    this.mass = mass;
    this.particleSubtype = subtype;
    this.forceInteractionModel = forceInteractionModel;
    this.particleType = forceInteractionModel.registerParticle(this,this.particleSubtype);
  }//constructor


  public Particle() {
    this.id = "Particle";
    this.particleLocation = new Double2D(0,0);
    this.particleVelocity = new Double2D(0,0);
    this.forceOnParticle = new Double2D(0,0);
    this.mass = 1.0;
    this.particleSubtype = 0;
    this.forceInteractionModel = null;
    this.particleType = -1;
    this.friction = 0.0;
  }


  protected void loadFromProperties(Properties properties) {
    this.particleSubtype = (int)( Integer.parseInt(properties.getProperty("subtype","0")) );
    this.mass = getParamFromProp(properties,"mass",mass,0.001,100000.0);
    this.friction = getParamFromProp(properties,"friction",friction,0.0,1.0);
    this.diameter = getParamFromProp(properties,"diamter",diameter,0.01,1000.0);
    this.maxVelocity = getParamFromProp(properties,"maxVelocity",maxVelocity,0.0,100.0);

    String defColor = "{" + particleColor.getRed() + "," + particleColor.getGreen() + "," + particleColor.getBlue() + "}";
    double color[] = APParser.getDblArrayProp(properties,"color",defColor);
    particleColor = new Color((int)color[0],(int)color[1],(int)color[2]);
  }


  public Particle newParticleInstance() {
    return new Particle();
  }

  public Vector<Parsable> createCopiesFromParse(String instantiationValues) {
    Properties properties = new Properties();
    try {
      //StringBufferInputStream sbis = new StringBufferInputStream(instantiationValues);
      StringReader sr = new StringReader(instantiationValues);
      //BufferedInputStream bis = new BufferedInputStream(sr);
      properties.load(sr);//bis);
    }
    catch (Exception e) {
      System.err.println("Error while creating from the parse of " + getClass().getName() + ": " + e);
    }

    int number =(int)(Integer.valueOf(properties.getProperty("number","1")) );
    int subtype = -1;
    Vector<Parsable> particles = new Vector<Parsable>(number);
    for (int i=0; i<number; i++) {
      Particle particle = newParticleInstance();
      particle.loadFromProperties(properties);
      particle.id = particle.getClass().getName() + "-" + particle.getParticleSubtype() + "-" + i;
      subtype = particle.getParticleSubtype();
      particles.add(particle);
    }

    if (APParser.VERBOSE_LOAD) 
      System.out.println("Loaded " + number + " copies of particle " + 
                         getClass().getName() + "-" + subtype);

    return(particles);
  }

  public void registerWithModel(ForceInteractionModel forceInteractionModel,
				APModel ap) {
    this.forceInteractionModel = forceInteractionModel;
    this.particleType = forceInteractionModel.registerParticle(this,this.particleSubtype);
    this.particleLocation = this.getInitialPosition(ap);
    ap.environment.setObjectLocation(this,this.particleLocation);
    ap.schedule.scheduleRepeating(this);
  }  


  public Double2D getInitialPosition(APModel ap) {
    Double2D loc = new Double2D(-500,-500);
    int times = 0;

    do {
      loc = ap.getRandomInitialPos(0.35);

      // Keep trying to place the particle until it is legal.  If you can't,
      // give up with complaint.
      times++;
      if(times == 1000) {
	System.err.println( "Cannot place particles. Exiting...." );
	System.exit(1);
      }
    } while( !ap.acceptablePosition( this, loc ) );

    return loc;
  }


  public int getParticleType() {return particleType;}
  public int getParticleSubtype() {return particleSubtype;}



  protected double getParamFromProp(Properties properties, String key, 
                                    double defaultVal, double lb, double ub) {
    double rawValue = ( Double.parseDouble(properties.getProperty(key,"" + defaultVal)) );

    if (rawValue < lb) rawValue = lb;
    if (rawValue > ub) rawValue = ub;

    return (rawValue);
  }


  public static double krn(final Double2D A, final Double2D B) {
      // double dX = A.x - B.x;
      // double dY = A.y - B.y;
      // return Math.sqrt(dX+dY);
      
//      double rq = dX*dX + dY*dY;
//      double scale = Math.pow(65.0,-2.0);
//      return 65.0*Math.exp(-scale*rq);
//    double innerProduct = A.x*B.x + A.y*B.y;
//    return 65.0*Math.tanh(innerProduct/7200.0 + 1);
//     return Math.pow(Math.pow(innerProduct+1,4.0),(1.0/8.0));
    return 0.0;
    
  }
  

  /** Compute the distance between two particles. */
  public static double distanceBetween(Particle a1, Particle a2) {
//    return distanceBetween(a1.particleLocation,a2.particleLocation);
    
//     return (krn(a1.particleLocation,a1.particleLocation) +
// 	    krn(a2.particleLocation,a2.particleLocation) -
// 	    2*krn(a1.particleLocation,a1.particleLocation) );
//     //    if (Double.isNaN(a1.particleLocation.x)
     double dX = a1.particleLocation.x - a2.particleLocation.x;
     double dY = a1.particleLocation.y - a2.particleLocation.y;
     double radicalQuantity = dX*dX + dY*dY;

     return(radicalQuantity <= 0 ? 0 : Math.sqrt(radicalQuantity));	
  }//distanceBetween()

  /** Compute the distance between two locations. */
  public static double distanceBetween(Double2D A, Double2D B) {
    // Small pre-condition error check
    if ( (A == null) || (B == null) ) return(APModel.MAX_DIM_SIZE);

//     return (krn(A,A) + krn(B,B) -2*krn(A,B));

    double dX = A.x - B.x;
    double dY = A.y - B.y;
    //  Double2D vct = new Double2D(dX,dY);
    // return (krn(vct,vct));
    
     double radicalQuantity = dX*dX + dY*dY;
     return(radicalQuantity <= 0 ? 0 : Math.sqrt(radicalQuantity));	
  }//distanceBetween()

  /** Return a unit vector pointing from the first location to the second. */
  public static Double2D unitVectorTowards(final Double2D loc1, final Double2D loc2) {
    double dX = loc1.x - loc2.x;
    double dY = loc1.y - loc2.y;
    double norm = Math.sqrt(dX*dX + dY*dY);
    return(new Double2D(dX/norm,dY/norm));
//     double angle = Math.atan2(dY,dX);
//     return( new Double2D(Math.cos(angle),Math.sin(angle)) );
  }//unitVectorTowards()


  /** Find all particles that are nearby and force each of those particles
   *  to compute the force effects this particle has on them. */
  protected void impartForce(final APModel ap) {
    double forceX = 0;
    double forceY = 0;
    double maxVision = forceInteractionModel.getMaxEffectRangeFrom(this);
    Bag nearbyParticles = ap.environment.getNeighborsWithinDistance(particleLocation,maxVision);

    if (nearbyParticles != null)
      for (int i=0; i<nearbyParticles.numObjs; i++)
	if (forceInteractionModel.isInteractionFromTo(this,(Particle)nearbyParticles.objs[i]))
	  ((Particle)nearbyParticles.objs[i]).applyForce(ap,this);
  }//impartForce()


  /** The affecting particle will call this method, which must compute
   *  the force of the calling particle on this particle and update the
   *  forceOnParticle physical state variable.  This method selects
   *  a force law based on the forceLaw indicator variable. */
  protected void applyForce(final APModel ap, final Particle other) {
    Double2D force = forceInteractionModel.apply(other,this);
    forceOnParticle = new Double2D(forceOnParticle.x + force.x,
                                   forceOnParticle.y + force.y);
  }//applyForce()


  /** Change the velocity physical state variable with the collected force
   *  data, then reset the force physical state variable.  The velocity 
   *  magnitude is capped at maxVelocity.*/
  public void updateVelocity(final APModel ap) {
    double momentumFactor = (useMomentum ? 1.0/mass : 1.0);
    // This was the old way I was implementing friction...
    double frictionFactor = (useFriction ? 1.0-friction : 1.0);

    double newVx = frictionFactor*(forceOnParticle.x * momentumFactor + particleVelocity.x);
    double newVy = frictionFactor*(forceOnParticle.y * momentumFactor + particleVelocity.y);

    // This is the way Bill implements friction
    //     double frictX = (useFriction ? friction * Math.abs(particleVelocity.x) : 0.0);
    //     double frictY = (useFriction ? friction * Math.abs(particleVelocity.y) : 0.0);

    //     double dVx = (forceOnParticle.x - frictX) * momentumFactor;
    //     double dVy = (forceOnParticle.y - frictY) * momentumFactor;
    //     double newVx = dVx + particleVelocity.x;
    //     double newVy = dVy + particleVelocity.y;

    if (Math.sqrt(newVx*newVx + newVy*newVy) <= maxVelocity)
      particleVelocity = new Double2D(newVx,newVy);
    else {
      double velocityScalar = Math.sqrt(newVx*newVx + newVy*newVy)/maxVelocity;
      particleVelocity = new Double2D(newVx/velocityScalar,newVy/velocityScalar);
    }

    if (Double.isNaN(newVx)) {
      System.out.println("DEBUG:  Updating " + id + " with an invalid vecolity");
      System.out.println("     newV = (" + newVx + "," + newVy + ")");
      System.out.println("     force = (" + forceOnParticle.x + "," + forceOnParticle.y + ")");
      System.out.println("     mass = " + mass);
    }

    forceOnParticle = new Double2D(0,0);
  }//updateVelocity()


  /** Change the position physical state variable using the current velocity.
   *  Use the model-level method acceptablePosition() to validate that the
   *  position is on the field.*/
  public void updatePosition(final APModel ap) {
    Double2D oldLocation = particleLocation;

    if( ap.acceptablePosition(this, new Double2D(particleLocation.x + particleVelocity.x, 
						 particleLocation.y + particleVelocity.y)) ) {
      particleLocation = new Double2D(particleLocation.x + particleVelocity.x, 
                                      particleLocation.y + particleVelocity.y);

      if (Double.isNaN(particleLocation.x)) {
        System.out.println("DEBUG:  Updating " + id + " with an invalid position");
	System.out.println("     oldLocation = (" + oldLocation.x + "," + oldLocation.y + ")");
	System.out.println("     velocity= (" + particleVelocity.x + "," + particleVelocity.y + ")");
      }

      lastLocation = oldLocation;
      ap.environment.setObjectLocation(this,particleLocation);
    }
  }//updatePosition


  /** This method can be used to reset the position of an particle.  By default,
   *  it places the particle somewhere on the circumferance of the circle 
   *  circuscribed by the field rectangle.*/
  public void resetPosition(APModel ap) {
    double angle = ap.random.nextDouble()*Math.PI*2.0;		
    double cX = (ap.XMAX + ap.XMIN)/2.0;
    double cY = (ap.YMAX + ap.YMIN)/2.0;
    double radius = Math.min(cX,cY);
    particleLocation = new Double2D(cX + radius*Math.cos(angle),
                                    cY + radius*Math.sin(angle));		
    ap.environment.setObjectLocation(this,particleLocation);
  }//resetPosition()



  public void step(final SimState state) {
    APModel ap = (APModel)state;

    impartForce(ap);  
    updateVelocity(ap);
    updatePosition(ap);	
  }



  /** This method is called by the UI to render the image of the
   *  particle.  Here it defaults to a circle.*/
  public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
    double diamx = info.draw.width * diameter;	    
    double diamy = info.draw.height * diameter;

    graphics.setColor( particleColor );
    graphics.fillOval((int)(info.draw.x-diamx/2),(int)(info.draw.y-diamy/2),(int)(diamx),(int)(diamy));
  }//draw()
	
}
