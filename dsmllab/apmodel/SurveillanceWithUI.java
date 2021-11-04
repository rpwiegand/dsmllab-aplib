/* 
Creates the actual display
*/

package dsmllab.apmodel;


import sim.portrayal.continuous.*;
import sim.engine.*;
import sim.display.*;
import javax.swing.*;
import java.awt.Color;

public class SurveillanceWithUI extends GUIState {

    public Display2D display;
    public JFrame displayFrame;

    public int trials = 0;

    ContinuousPortrayal2D targets_portrayal = new ContinuousPortrayal2D();

    public static void main(String[] args) {
        new SurveillanceWithUI().createController();
    }

    public SurveillanceWithUI() { super(new Surveillance( System.currentTimeMillis())); }
    public SurveillanceWithUI(SimState state) { super(state); }

    public static String getName() { return "Targets"; }

    public Object getSimulationInspectedObject() {return state;}

    public void start() {
        super.start();
        setupPortrayals();
        System.out.println("Finish ports");
    }

    public void finish() {
        super.finish();
    
        trials++;
    
        if ( ((Surveillance)state).measures != null) {
          ((Surveillance)state).measures.reportAllCurrentTrial();
          ((Surveillance)state).measures.reportAllAggregates();
        }

        System.out.println();
    }
    

    public void load(SimState state) {
        //System.out.println("1 Loading state here");
        super.load(state);
        //Surveillance s = (Surveillance)state;
        //double num = state.targets_covered();
        //System.out.println(num);
        System.out.println("2 Loading state here");
        setupPortrayals();
    }
        
    public void setupPortrayals() {
        // tell the portrayals what to portray and how to portray them

        targets_portrayal.setField(((Surveillance)state).environment);
     
        // reschedule the displayer
        display.reset();

        display.setBackdrop(Color.white);

                
        // redraw the display
        display.repaint();
    }

    public void init(Controller c) {
        super.init(c);

        // make the displayer
        display = new Display2D(600,600,this);

        displayFrame = display.createFrame();
        displayFrame.setTitle("Grid with Targets");
        c.registerFrame(displayFrame);   
        displayFrame.setVisible(true);
        display.attach( targets_portrayal, "Obstacles" );
    }
        
    public void quit() {
        super.quit();
        
        if (displayFrame!=null) displayFrame.dispose();
        displayFrame = null;
        display = null;
    }

    }
