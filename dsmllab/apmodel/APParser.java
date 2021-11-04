package dsmllab.apmodel;

import java.lang.reflect.*;
import java.lang.Class;
import java.util.*;
import java.io.*;

public class APParser {
  public static final boolean VERBOSE_LOAD = false;//true;

  public static final int APS_PRE_BLOCK  = 0;
  public static final int APS_IN_BLOCK   = 1;
  public static final int APS_POST_BLOCK = 2;

  protected APModel ap;
  protected ForceInteractionModel fim;
  protected Vector loadedItemsList;

  public APParser(APModel ap, ForceInteractionModel fim) {
    this.ap = ap;
    this.fim = fim;
    ForceLaw.apparser = this;
  }


  public String getBlockValues(StreamTokenizer tokenizer, String blockName) throws Exception {
    int state = APS_PRE_BLOCK;
    String blockValue = "";
    int level = 0;
    tokenizer.wordChars(33,126);
    tokenizer.eolIsSignificant(true);

    while (state != APS_POST_BLOCK) {
      int token = tokenizer.nextToken();
      String value = "";
      if (tokenizer.ttype == StreamTokenizer.TT_WORD) value = tokenizer.sval;
      else if (tokenizer.ttype == StreamTokenizer.TT_NUMBER) value = "" + tokenizer.nval;
      else if (token == StreamTokenizer.TT_EOL) value = "\n";

      if (state == APS_PRE_BLOCK) {
	if (token == StreamTokenizer.TT_EOL) value = "";

	else if (token == StreamTokenizer.TT_EOF)
          throw new Exception("Error parsing AP params:  '{' expected but never found after block declaration " + blockName + ".");

	else if (value.compareTo("}") == 0)
          throw new Exception("Error parsing AP params:  unmatched '}' found in block " + blockName + ".");

	else if (value.compareTo("{") == 0) state = APS_IN_BLOCK;

	else 
          throw new Exception("Error parsing AP params:  '{' expected but never found after block declaration " + blockName + ".");
      }

      else if (state == APS_IN_BLOCK) {
	if (token == StreamTokenizer.TT_EOF) 
          throw new Exception("Error parsing AP params:  '}' expected but never found in block " + blockName + ".");

        else if (value.compareTo("{") == 0) {level++; blockValue += value;}

        else if ( (value.compareTo("}") == 0) && (level == 0) ) state = APS_POST_BLOCK;

        else if ( (value.compareTo("}") == 0) && (level > 0) ) {level--; blockValue += value;}

        else if ( (value.compareTo("}") == 0) && (level < 0) ) 
          throw new Exception("Error parsing AP params:  unmatched '}' found in block " + blockName + ".");

	else blockValue += value;
  
      }
    }
    //System.out.println("Block value = " + blockValue);
    return blockValue;
  }


  public void instantiateBlock(String blockName, String blockValue) {
    try{
      Class c = Class.forName(blockName);
      Object o = c.newInstance();
      Vector items = ((Parsable)o).createCopiesFromParse(blockValue);
      loadedItemsList.addAll(items);
    }
    catch (Exception e) {
      System.err.println("Could not instantiate class appropriate Parsable class: " + blockName );
      System.err.println("ERROR: " + e);
      System.err.println(blockValue);
    }
  }


  protected int loadFromTokenizer(StreamTokenizer tokenizer) throws Exception {
    int numItems = 0;
    loadedItemsList = new Vector();

    while (true) {
      int token = tokenizer.nextToken();
      if (token == StreamTokenizer.TT_EOF) break;
      else {
	String blockName = tokenizer.sval;
	String blockValues = getBlockValues(tokenizer,blockName);
	instantiateBlock(blockName,blockValues);
	tokenizer.eolIsSignificant(false);
	numItems++;
      }	
    }

    return numItems;
  }


  public void loadFromString(String apParamsString) {
    int numItems = 0;

    try {
      StringReader sr = new StringReader(apParamsString);
      StreamTokenizer tokenizer = new StreamTokenizer(sr);
      numItems = loadFromTokenizer(tokenizer);
    } catch (Exception e) {
      System.err.println("Error reading AP model parameters: " + e);
      System.exit(1);
    }

    if (VERBOSE_LOAD)
      System.out.println("Read " + numItems + " item types from apparams string");
  }


  public void loadFromFile(String fileName) {
    int numItems = 0;

    try {
      FileReader fr = new FileReader(fileName);
      StreamTokenizer tokenizer = new StreamTokenizer(fr);
      numItems = loadFromTokenizer(tokenizer);
    } catch (Exception e) {
      System.err.println("Error reading AP model parameters: " + e);
      System.exit(1);
    }

    if (VERBOSE_LOAD)
      System.out.println("Read " + numItems + " item types from apparams file '" + fileName + "'");
  }


  public void registerAllParticles() {
    for (int i=0; i<loadedItemsList.size(); i++) {
      Object item = loadedItemsList.get(i);
      if (item instanceof Particle) {
	((Parsable)item).registerWithModel(fim,ap);
	((Particle)item).getParticleSubtype();
      }
    }
  }


  public void registerAllForceLaws() {
    for (int i=0; i<loadedItemsList.size(); i++) {
      Object item = loadedItemsList.get(i);
      if (item instanceof ForceLaw) 
	((Parsable)item).registerWithModel(fim,ap);
    }
  }

  // This is a a very UGLY routine and should be made more efficient at
  // some point.  I am not too worried about it because it only gets called
  // when the simulation starts (not every step).  Moreover, the three loops
  // are linear with respect to the the number of particles ... so even though
  // there are three loops, the whole routine is still linear.  Still, there
  // is a strong assumption here that there aren't a huge number of particles.
  public int[] getAllRegisteredParticleSubtypes(String particleTypeName) {
    // maxParticleSubtype is computed in the registration process ...
    // It gives us an upper bound on subtypes.  We create boolean
    // array where each element corresponds to a potential
    // subtype in use.  I shouldn't have to do this ... for some
    // reason fimmaxParticleSubtype isn't populated yet ... RPW 3/6/06
    int maxParticleSubtype = 0;
    for (int i=0; i<loadedItemsList.size(); i++) {
      Object item = loadedItemsList.get(i);
      if (particleTypeName.compareToIgnoreCase(item.getClass().getName()) == 0) {
	int currSubtype = ((Particle)item).getParticleSubtype();
	if (currSubtype > maxParticleSubtype) maxParticleSubtype = currSubtype;
      }
    }

    // This loop spins through all registered items, singles out
    // all particles of the inputted name, and flag used subtypes
    boolean isSubtype[] = new boolean[maxParticleSubtype+1];
    int subtypeCount = 0;
    for (int i=0; i<loadedItemsList.size(); i++) {
      Object item = loadedItemsList.get(i);
      if (particleTypeName.compareToIgnoreCase(item.getClass().getName()) == 0) {
	int currSubtype = ((Particle)item).getParticleSubtype();
	if (!isSubtype[currSubtype]) subtypeCount++;
	isSubtype[currSubtype] = true;
      }
    }

    // We use our boolean array to condense to a list of all
    // subtype values used by the inputted particle type.
    int subtypes[] = new int[subtypeCount+1];
    int j = 0;
    for (int i=0; i<subtypeCount; i++) {
      if (isSubtype[i]) subtypes[j++] = i;
    }

    return subtypes;
  }


  //--------------- Helper functions --------------
  public static String dblArrayToString(double[] x) {
    String dblArrayStr = "{";

    for (int i=0; i<(x.length-1); i++)
      dblArrayStr = dblArrayStr + x[i] + ",";

    dblArrayStr = dblArrayStr + x[x.length-1]+"}";

    return (dblArrayStr);
  }


  public static double[] getDblArrayProp(Properties properties, String keyName, String defaultVal) {
    String value = properties.getProperty(keyName, defaultVal);
    String delims = "{}[],;:() ";

    // Count the numbers ...
    StringTokenizer st = new StringTokenizer(value,delims);
    int nNumbers = 0;
    while (st.hasMoreTokens()) {
      try {
	double x = (new Double(st.nextToken())).doubleValue();
	nNumbers++;
      }
      catch (java.lang.NumberFormatException e) {}
    }

    // Load the array ...
    double vect[] = new double[nNumbers];
    st = new StringTokenizer(value,delims);
    int idx = 0;
    while (st.hasMoreTokens()) 
      try {
	vect[idx] = (new Double(st.nextToken())).doubleValue();
	idx++;
      }
      catch (java.lang.NumberFormatException e) {}

    return(vect);
  }


  public static void main(String args[]) {
    String fileName = "apparser.test";
    if (args.length >= 1) fileName = args[0];

    System.out.println("Testing APParser on file " + fileName + " ...");
    APParser parser = new APParser(null,null);
    parser.loadFromFile(fileName);
  }
}