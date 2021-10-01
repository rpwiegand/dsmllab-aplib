package dsmllab.apmodel;

import java.util.Vector;

public interface Parsable {
  public Vector createCopiesFromParse(String instantiationValues);
  public void registerWithModel(ForceInteractionModel forceInteractionModel,
				APModel ap);
}
