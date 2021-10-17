/*
  Surveillance.java extends APModel.java, adding/overriding the following methods
    - initializeEnvironment: registers the particles/agents. Also reads targets from a file and adds them to the environment
    - targets_covered: determines percentage of targets that are covered by at least one agent 
*/

package dsmllab.apmodel;

import sim.engine.*;
import sim.util.*;
import sim.field.continuous.*;
import java.awt.*;
import sim.portrayal.simple.*;
import java.util.Scanner;
import java.io.File;

public class Surveillance extends APModel {

  public static Target[] targets;
  public static int n;

  public Surveillance (long seed) {
    super(seed);
  }

  @Override public void initializeEnvironment() {
    double cX = (super.XMAX-super.XMIN)/2.0;
    double cY = (super.YMAX-super.YMIN)/2.0;
    environment = new Continuous2D(25.0, (super.XMAX-super.XMIN), (super.YMAX-super.YMIN) );

    super.apParamsParser.registerAllParticles();

    if (super.measures != null) super.measures.scheduleAllMeasures(this);

    // try to open the file that has the list of points in it
    try {
      Scanner scanner = new Scanner(new File("points2.txt"));

      //the first number in the file is how many points we have
      n = scanner.nextInt();
      targets = new Target[n];

      // points in the file are of the form (diameter, x coordinate, y coord, red color, green color, blue color)
      for (int i = 0; i < n; i++) {
          int diam = scanner.nextInt();
          double x = scanner.nextInt();
          double y = scanner.nextInt();
          int r = scanner.nextInt();
          int g = scanner.nextInt();
          int b = scanner.nextInt();

          Paint this_color = new Color(r, g, b);

          Target targ = new Target( diam,this_color,x,y);
          targets[i] = targ;

          environment.setObjectLocation( targ, new Double2D( targ.x, targ.y ));
          
      }
      } catch (Exception e) {
          System.out.println(e.getClass());
        }

        

  }//initializeEnvironment()


  /* 
    This function will return the percentage of targets that are within range of at least one agent
  */
  public double targets_covered() {
    int num_covered = 0;

    //grab all the objects from the environment
    Bag particles = environment.allObjects;

    for (int i = 0; i < n; i++) {
      boolean thisTargCovered = false;
      double Tx = targets[i].x;
      double Ty = targets[i].y;

      for (int j=0; j < particles.numObjs; j++){

        //check that we have a particle, not a target
        if (particles.objs[j] instanceof Particle) {
          Particle thisPart = (Particle)particles.objs[j];
          double Px = thisPart.particleLocation.x;
          double Py = thisPart.particleLocation.y;

          //if the dist bt the particle and the targ is less than the radius, that target is within range
          double distance = Math.sqrt( (Py -Ty)*(Py -Ty) + (Px -Tx)*(Px -Tx));

          if (distance < thisPart.radius) {
            thisTargCovered = true;
            
          }

        }
      }
      // after checking each particle, if that target is covered, increment our total
      if (thisTargCovered) num_covered++;
    }

    //System.out.println("total covered");
    //System.out.println(num_covered);
    //System.out.println("n");
    //System.out.println(n);

    return (double)num_covered/n;
  }//targets_covered()

   
}
