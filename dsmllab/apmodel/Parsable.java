package dsmllab.apmodel;

import java.util.Vector;

public interface Parsable {
  public Vector<Parsable> createCopiesFromParse(String instantiationValues);
  public void registerWithModel(ForceInteractionModel forceInteractionModel,
				APModel ap);
}
