package dsmllab.apmodel;

import sim.engine.*;
import sim.util.*;

/**
 *  This base class implements an abstract measurement object as an
 *  unembodied particle in the simultation.  It assumes that APModel
 *  is responsible for scheduling all desired measures.
 *
 *  @author R. Paul Wiegand
 *  @version Version 0.1 beta
 */

public abstract class AbstractMeasure implements Steppable {
  protected String id = "NoMeasure";
  protected double currentStepStatistic = 0.0;
  protected double cumulativeStepStatistic = 0.0;
  protected double cumulativeTrialStatistic = 0.0;
  protected long stepCount = 0;
  protected long trialCount = 0;
  protected MeasureRegistry parentRegistry = null;

  public AbstractMeasure() {
    clear();
    this.id = getClass().getName();
  }

  public AbstractMeasure(String id) {
    clear();
    this.id = id;
  }

  public void clear() {
    currentStepStatistic = 0.0;
    cumulativeStepStatistic = 0.0;
    cumulativeTrialStatistic = 0.0;
    stepCount = 0;
    trialCount = 0;
  }


  public void tallyTrial() {
    if (trialCount <= 0) {
      trialCount = 1;
      cumulativeTrialStatistic = 0.0;
    }
    else {
      cumulativeTrialStatistic += getStepsAggregateForCurrentTrial();
      trialCount++;
    }
    stepCount = 0;
    cumulativeStepStatistic = 0.0;
    currentStepStatistic = 0.0;
    trialInitializations();
  }

  protected void trialInitializations() {}
  protected void incStepCount(int incVal) { stepCount += incVal; }

  public void step(SimState state) {
    currentStepStatistic = computeStepMeasure((APModel)state);
    cumulativeStepStatistic += currentStepStatistic;
  }


  public double computeStepMeasure(APModel ap) {
    incStepCount(1);
    return 0.0;
  }


  public void reportAggregate() {
    System.out.println("Aggregate : " + id + " : " + getAggregate() + " : " + trialCount);
  }

  public void reportCurrentTrial() {
    System.out.println("CurrentTrial : " + id + " : " + getStepsAggregateForCurrentTrial() + " : " + stepCount);
  }

  // Accessor methods
  public String getID() { return id; }

  public double getCurrentStepStatistic() { return currentStepStatistic; }

  public double getStepsAggregateForCurrentTrial() {
    return (stepCount == 0 ? 0.0 : cumulativeStepStatistic / (double) stepCount);
  }

  public double getAggregate() { 
    double total = cumulativeTrialStatistic + getStepsAggregateForCurrentTrial();
    return (trialCount <= 0 ? 0.0 : total / (double) trialCount);
  }

  public void setParentRegistry(MeasureRegistry registry) {parentRegistry = registry;}

  public Object getInternalDataStructure() { return null; }

}