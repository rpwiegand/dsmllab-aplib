//
// ExpParamTest.java
//

package dsmllab.utilities;

public class ExpParamTest extends ExperimentParameters {
  public int intExample = 1;
  public float floatExample =(float) 1.0;
  public double doubleExample = 1.0;
  public boolean boolExample = false;
  public String strExample = "foo";
  public double[] daExample = {1.0, 2.0};
  public String[] saExample = {"One", "Two", "Three"};

  public static void main(String args[]) {
    ExpParamTest expParams = new ExpParamTest();
    expParams.loadParameters();
  }

}
