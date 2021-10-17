/* Generic Target class 
        Contains basic info about each target including coordinates.
*/

package dsmllab.apmodel;

import java.awt.*;
import sim.portrayal.simple.*;

public class Target extends OvalPortrayal2D {
    private static final long serialVersionUID = 1;
    public double x;
    public double y;
    
    public Target(double diameter, Paint target_color, double x_coord, double y_coord) {
        super(target_color,diameter);
        this.y = y_coord;
        this.x = x_coord;


    }

}
