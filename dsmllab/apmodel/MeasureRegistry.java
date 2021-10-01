package dsmllab.apmodel;

import sim.engine.*;
import sim.util.*;

import java.util.HashMap;
import java.util.Vector;


/**
 *
 *  @author R. Paul Wiegand
 *  @version Version 0.1 beta
 */

public class MeasureRegistry {
  protected HashMap registry = new HashMap();
  public Particle particles[];

  public void registerMeasure(AbstractMeasure measure) {
    registry.put(measure.id,measure);
    measure.setParentRegistry(this);
  }


  public void scheduleAllMeasures(final APModel ap) {
    Vector measures = new Vector(registry.values());
    for (int i=0; i<measures.size(); i++) {
      AbstractMeasure msr = (AbstractMeasure)measures.get(i);
      ap.schedule.scheduleRepeating(msr);
    }

    populateParticleLists(ap);

    for (int i=0; i<measures.size(); i++) {
      AbstractMeasure msr = (AbstractMeasure)measures.get(i);
      msr.tallyTrial();
    }

  }


  public void tallyAllMeasures() {
    Vector measures = new Vector(registry.values());
    for (int i=0; i<measures.size(); i++) {
      AbstractMeasure msr = (AbstractMeasure)measures.get(i);
      msr.tallyTrial();
    }
  }


  public void reportAllAggregates() {
    Vector measures = new Vector(registry.values());
    for (int i=0; i<measures.size(); i++) {
      AbstractMeasure msr = (AbstractMeasure)measures.get(i);
      msr.reportAggregate();
    }
  }

  public void reportAllCurrentTrial() {
    Vector measures = new Vector(registry.values());
    for (int i=0; i<measures.size(); i++) {
      AbstractMeasure msr = (AbstractMeasure)measures.get(i);
      msr.reportCurrentTrial();
    }
  }


  public AbstractMeasure findMeasure(String id) {
    return (AbstractMeasure)registry.get(id);
  }


  public double getAggregateForMsr(String id, double defaultVal) {
    double retVal = defaultVal;
    AbstractMeasure msr = (AbstractMeasure)registry.get(id);

    if (msr != null) retVal = msr.getAggregate();

    return retVal;
  }


  public double getCurrentStepStatisticForMsr(String id, double defaultVal) {
    double retVal = defaultVal;
    AbstractMeasure msr = (AbstractMeasure) registry.get(id);

    if (msr != null) retVal = msr.getCurrentStepStatistic();

    return retVal;
  }

  public Object getInternalDataStructureForMsr(String id) {
    Object retStructure = null;
    AbstractMeasure msr = (AbstractMeasure) registry.get(id);

    if (msr != null) retStructure = msr.getInternalDataStructure();

    return retStructure;
  }

  protected void populateParticleLists(final APModel ap) {
    double distance = 5.0*Math.max(Math.abs(ap.XMAX - ap.XMIN),Math.abs(ap.YMAX-ap.YMIN));
    Double2D center = new Double2D(Math.abs(ap.XMAX - ap.XMIN)/2.0,Math.abs(ap.YMAX - ap.YMIN)/2.0);
    Bag allParticles = ap.environment.getObjectsWithinDistance(center,distance);

    // Count the types of particles
    particles = new Particle[allParticles.numObjs];

    // Populate arrays
    int particleCount = 0;
    for (int i=0; i<allParticles.numObjs; i++)
      particles[i] = (Particle)allParticles.objs[i];
  }


}

