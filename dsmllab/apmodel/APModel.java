package dsmllab.apmodel;

import sim.field.continuous.*;
import sim.engine.*;
import sim.util.*;
import ec.util.*;
import java.io.*;
import java.text.DecimalFormat;

/**
 *  This class implements a SimState class in the MASON simulation toolkit (by Sean Luke).

 *  It is the main class for driving Bill &amp; Diana Spears' Artificial Physicomemetics model.
 *  Particle behaviors are governed by an artificial physics of attractive and repulsive
 *  forces on one another such that emergent formations are possible.  We've heavily 
 *  parameterized the system, so the physics can be evolved to adapt to new situations.
 *
 *  This is the abstract base class for many implementations (e.g., IPSP).
 *
 *  @author R. Paul Wiegand
 *  @version Version 0.1 beta
 */

public class APModel extends SimState {
  public static final int DEBUG_LEVEL = 1;

  // Global constants defining the field of the simulation
  public static final double ASPECT_RATIO=8.0/6.0;
  public static final double MAX_DIM_SIZE=350;
  public static double XMIN = 0;
  public static double XMAX = XMIN + ASPECT_RATIO*MAX_DIM_SIZE;
  public static double YMIN = 0;
  public static double YMAX = YMIN + MAX_DIM_SIZE;

  public double fitness = 0.0;         // long-term, across trials
  public double penalty = 0.0;
  public static APExperimentParameters expParams;// = new APExperimentParameters();
  public String apParamsString;    // populated by the domain & read instead of a file

  // Components of the simulation
  public Continuous2D environment = null;
  public ForceInteractionModel forceInteractionModel = null;
  public static MeasureRegistry measures = null;
  public APParser apParamsParser = null;

  public String getParamFileName() {return expParams.apParamFileName;}
  public void setParamFileName(String name) {expParams.apParamFileName = name;}

  public double getFitness(){return(fitness);}




  /** Creates a APModel simulation with the given random number seed. */
  public APModel(long seed) {
    super(new MersenneTwisterFast(seed), new Schedule());
  }//constructor


  /** Verifies that the particle is in the field.*/
  public boolean acceptablePosition( final Particle particle, final Double2D location ) {
    if( location.x < particle.diameter/2 || location.x > (XMAX-XMIN)-particle.diameter/2 ||
	location.y < particle.diameter/2 || location.y > (YMAX-YMIN)-particle.diameter/2 )
      return false;

    return true;
  }//acceptablePosition()


  /** Create a random location somewhere in a bounding box in
   *  the middle of the field. */
  public Double2D getRandomInitialPos(double boxFactor) {
    if      (boxFactor < 0.0) boxFactor = 0.0;
    else if (boxFactor > 1.0) boxFactor = 0.0;
    else if (boxFactor > 0.5) boxFactor = 1.0 - boxFactor;

    double lbX = boxFactor*(XMAX-XMIN)+XMIN;
    double lbY = boxFactor*(YMAX-YMIN)+YMIN;
    double ubX = (1-boxFactor)*(XMAX-XMIN)+XMIN;
    double ubY = (1-boxFactor)*(YMAX-YMIN)+YMIN;
    double rX = ubX - lbX;
    double rY = ubY - lbY;

    return (new Double2D(random.nextDouble()*rX + lbX,
			 random.nextDouble()*rY  + lbY));
  }


  /** Initialize the simulation.  This is called each time the simulation
   *  is restarted or repeated. */
  public void start() {
    super.start();  // clear out the schedule

    loadParams();
    initializeEnvironment();

    // Once particles are created, all particle types are registered and we
    // can setup all the individual interactions in the model
    setupInteractions();    

    if (DEBUG_LEVEL > 1)
      System.out.println("DEBUG:  finished placing ALL particles");
  }//start()


  /** Load the external parameters governing simulation physics */
  public void loadParams() {
    if (expParams == null) expParams = new APExperimentParameters();
    if (!expParams.isLoaded()) expParams.loadParameters();

    forceInteractionModel = new ForceInteractionModel();
    apParamsParser = new APParser(this,forceInteractionModel);

    apParamsParser.loadFromFile(expParams.apParamFileName);



  }

  /** Place particles, setup simulation, etc. */
  public void initializeEnvironment() {
    double cX = (XMAX-XMIN)/2.0;
    double cY = (YMAX-YMIN)/2.0;
    environment = new Continuous2D(25.0, (XMAX-XMIN), (YMAX-YMIN) );

    apParamsParser.registerAllParticles();

    if (measures != null) measures.scheduleAllMeasures(this);
  }//initializeEnvironment()


  /** Setup force interactions */
  public void setupInteractions() {
    forceInteractionModel.initializeInteractions();
    apParamsParser.registerAllForceLaws();
  }

  
  /** Resolving fitness issues */
  public double resolveFinalFitness()  {return 0.0;}
  protected double resolveTrialFitness() {return 0.0;}


  /** As an alternative to the built-in doLoop() method, this method
   *  is reponsible for creating the simulation, setting the external
   *  parameters properly, and returning a performance score of some
   *  sort.  */
  public static double evaluate(APModel ap, APExperimentParameters expParams, String apParamsString) {
    long trials = 0;

    ap.expParams = expParams;
    ap.expParams.readFromParamFile = false;
    ap.apParamsString = apParamsString;
    ap.penalty = 0.0;
    ap.fitness = 0.0;
        
    for (trials=0; trials < expParams.numSimTrials; trials++) {
      ap.start();
      for (long steps=0; steps < expParams.numSteps; steps++) {
        System.out.println(steps);
	if (DEBUG_LEVEL > 1) System.out.println("DEBUG: step=" + steps);
	if (!ap.schedule.step(ap)) break;
      }

      ap.finish();
    }

    return ( ap.resolveFinalFitness() / (double)trials);
  }//evaluate()


  /** Called when the simulation terminates. */
  public void finish() {
    super.finish();
    fitness += resolveTrialFitness();
  }


  /** Main method that allows the program to be run without the display.
   *  The command-line parameters are standard for MASON simulations.
   *  To get them, try: java sim.app.apmodel.APModel -help **/
  public static void test(Surveillance ap, APExperimentParameters apExpParams) {
    long trials = 0;

    ap.expParams = apExpParams;
    ap.loadParams();
    ap.penalty = 0.0;
    ap.fitness = 0.0;
        
    for (trials=0; trials < expParams.numSimTrials; trials++) {
      ap.start();
      for (long steps=0; steps < expParams.numSteps; steps++) {
	if (DEBUG_LEVEL > 1) System.out.println("DEBUG: step=" + steps);
	if (!ap.schedule.step(ap)) break;
      }

      ap.finish();
      if (ap.measures != null)
	ap.measures.reportAllCurrentTrial();
    }

    double eval = ap.targets_covered();
    System.out.println(eval);
    //System.out.println("covered " + eval);
    //dsmllab.apmodel.Particle

  }


  public static void main(String args[]) {
    if (args.length < 1) {
      System.out.println("Specify experiment parameter name");
      System.exit(1);
    }

    Surveillance ap = new Surveillance(System.currentTimeMillis());
    test(ap,new APExperimentParameters(args[0]));
  }
}
