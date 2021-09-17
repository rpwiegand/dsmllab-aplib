package dsmllab.apmodel;

import sim.util.*;
import java.util.*;

/**
 *
 * This class implements a model of interactions by using a directed graph.  Nodes in
 * graph correspond to types of particles, edges to interactions between particles.  
 * Each interaction has an independent force law.  This allows for the possibility of
 * developing models with a lot of versitility.  Moreover, it is not entirely unrealistic.
 * Consider an example in which particles of type A are drawn to particle type B mainly
 * based on gravity, while particle type B is drawn to particle type C mainly based on
 * electro-magnitism.  While this model cannot enforce multiple force laws on the same
 * interaction, it is reasonable to assume there is one dominant force law between any
 * two types of particles.
 *
 * The types are assigned and enumerated using a registration scheme.  It is assumed
 * that particles can have "subtypes", so the type of a particle is uniquely determined
 * by the class of particle and the particle's subtype.
 *
 * Using the model works as follows:  
 *   1.) Create a ForceInteractionModel
 *   2.) Register all particles
 *   3.) Initialize the model
 *   4.) Set inteteractions
 *   5.) Use interactions
 *
 *  @author R. Paul Wiegand
 *  @version Version 0.1 beta
 */

public class ForceInteractionModel
{
  protected Map<String, Integer> typeTable;
  protected int numParticleTypes;
  protected ForceLaw interactions[][];  // adjacency matrix for graph
  protected int maxParticleSubtype = 0;

  /** Constructer creates the internal map and resets the particle type counter.*/
  public ForceInteractionModel() {
    typeTable = new HashMap<String, Integer>();
    numParticleTypes = 0;
    maxParticleSubtype = 0;
  }//consutrctor()


  /** Register  particle by passing the object and subtype.  The class of the
   *  object is used, and no data instance information is assumed here.  The
   *  particle is added to the map if isn't already there, and the particle
   *  type is returned either way.  This way, a user can register all particles
   *  in the system without worry of whether or not many are actualy the same
   *  type.  The method will work this out.*/
  public int registerParticle(Object obj, int subtype) {
    String key = obj.getClass().getName() + subtype;
    int particleType = numParticleTypes;
    if (subtype > maxParticleSubtype) maxParticleSubtype = subtype;

    Object val = typeTable.get(key);
    
    if (val != null) // Particle type is not already in the map
      particleType = ((Integer)val).intValue();
    else             // Particle type was already in the map
      typeTable.put(key, Integer.valueOf(numParticleTypes++));
        
    return particleType;
  }//registerParticle


  /** After all interactions have been registered, the internal
   *  adjacency matrix for the graph must be constructed.  This
   *  routine does this ... and it MUST be called before interactions
   *  can actually be defined and/or used.*/
  public void initializeInteractions() {
    interactions = new ForceLaw[numParticleTypes][];
    for (int i=0; i<numParticleTypes; i++) 
      interactions[i] = new ForceLaw[numParticleTypes];
  }//initializeInteractions()


  /** Use the class and subtype of the particle instance to compute
   *  the particle type.  If there is no such particle registered,
   *  the function returns a -1. */
  public int getParticleType(Particle particle) {
    String key = particle.getClass().getName() + particle.getParticleSubtype();
    int particleType = -1;
    Object val = typeTable.get(key);
    
    if (val != null)
      particleType = ((Integer)val).intValue();
    
    return particleType;
  }//registerParticle


  /** Use the class name and subtype of a particle to compute
   *  the particle type.  If there is no such particle registered,
   *  the function returns a -1. */
  public int getParticleType(String className, int subtype) {
    String key = className + subtype;
    int particleType = -1;
    Object val = typeTable.get(key);
    
    if (val != null)
      particleType = ((Integer)val).intValue();
    
    return particleType;
  }//registerParticle


  public int getMaxParticleSubtype() {
    return(maxParticleSubtype);
  }


  /** Given two particles, return a boolean indicating whether or not an
   *  interaction from the first to the second has been setup in the
   *  model.*/
  public boolean isInteractionFromTo(final Particle fromParticle, final Particle toParticle) {
    if (interactions == null) return false;
    
    double distance = Particle.distanceBetween(fromParticle,toParticle);
    ForceLaw fl = interactions[getParticleType(fromParticle)][getParticleType(toParticle)];
    
    return ( (fromParticle != toParticle) && (fl != null) && (distance <= fl.getEffectRange()) );
  }//isInteractionFromTo()


  /** Pass through method:  This method looks up the interaction from the
   *  first particle to the second, and calls the apply() method of the ForceLaw
   *  assigned to the interaction.*/
  public Double2D apply(final Particle particle1, final Particle particle2) {
    ForceLaw interaction = interactions[getParticleType(particle1)][getParticleType(particle2)];
    return interaction.apply(particle1,particle2);    
  }
  

  // Accessor methods
  public ForceLaw getInteractionFromTo(final Particle fromParticle, final Particle toParticle) {
    return getInteractionFromTo(getParticleType(fromParticle),
                                getParticleType(toParticle));
  }
  public ForceLaw getInteractionFromTo(int fromType, int toType) {
    return interactions[fromType][toType];
  }

  public void setInteractionFromTo(final Particle fromParticle, final Particle toParticle, 
				   ForceLaw interaction) {
    setInteractionFromTo(getParticleType(fromParticle),
                         getParticleType(toParticle),
                         interaction);
  }
  public void setInteractionFromTo(int fromType, int toType, 
                                   ForceLaw interaction) {
    interactions[fromType][toType] = interaction;    
  }


  /** Return the maximum range of effect all force laws "implemented" by 
   *  the fromParticle.  In other words, search all valid interactions from 
   *  the given particle and return the maximum range of effect of all
   *  force laws associated with those interactions.*/
  public double getMaxEffectRangeFrom(final Particle fromParticle) {
    double maxEffectRange = -1;
    ForceLaw laws[] = interactions[getParticleType(fromParticle)];

    for (int i=0; i<laws.length; i++)
      if (laws[i] != null)
        if (laws[i].getEffectRange() > maxEffectRange) 
          maxEffectRange = laws[i].getEffectRange();

    return(maxEffectRange);
  }

}