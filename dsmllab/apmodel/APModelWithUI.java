package dsmllab.apmodel;

import sim.portrayal.continuous.*;
import sim.engine.*;
import sim.display.*;
import javax.swing.*;
import java.awt.Color;


public class APModelWithUI extends GUIState {

  public Display2D display;
  public JFrame displayFrame;

  public int trials = 0;
    
  ContinuousPortrayal2D vidPortrayal = new ContinuousPortrayal2D();

  public static void main(String[] args) {
    if (args.length > 0) APModel.expParams = new APExperimentParameters(args[0]);
    else APModel.expParams = new APExperimentParameters();

    APModelWithUI vid = new APModelWithUI(args);
    Console c = new Console(vid);
    c.setVisible(true);
  }//main()


  public APModelWithUI(String[] args) {super(new APModel( System.currentTimeMillis()));}
  public APModelWithUI(SimState state) { super(state); }

  public static String getName() { return "Artificial Physicomimetics"; }
    
  public static String getInfo() {
    //    APModel vids = (APModel) state;

    return
      "<H2>Bill &amp; Diana Spears'  Physicomimetic Demonstration</H2>by R. Paul Wiegand.<p>Parameters:" +
      "</table>";
  }//getInfo()

  public Object getSimulationInspectedObject() {return state;}
    
  public void start() {
    super.start();
    setupPortrayals();
  }//start()

  public void finish() {
    super.finish();

    trials++;

    if ( ((APModel)state).measures != null) {
      ((APModel)state).measures.reportAllCurrentTrial();
      ((APModel)state).measures.reportAllAggregates();
    }

    System.out.println();
  }


  public void load(SimState state) {
    super.load(state);
    setupPortrayals();
  }//load()

        
  public void setupPortrayals() {
    // tell the portrayals what to portray and how to portray them
    vidPortrayal.setField(((APModel)state).environment);
            
    // reschedule the displayer
    display.reset();
    display.setBackdrop(Color.white);
                
    // redraw the display
    display.repaint();
  }//setupPortrayals


  public void init(Controller c) {
    super.init(c);

    // make the displayer
    display = new Display2D(800,600,this);

    displayFrame = display.createFrame();
    displayFrame.setTitle("Artificial Physicomimetics Display");
    c.registerFrame(displayFrame);   // register the frame so it appears in the "Display" list
    displayFrame.setVisible(true);
    display.attach( vidPortrayal, "Particles" );
  }//init()
        

  public void quit() {
    super.quit();
        
    if (displayFrame!=null) displayFrame.dispose();
    displayFrame = null;
    display = null;
  }//quit()

}

