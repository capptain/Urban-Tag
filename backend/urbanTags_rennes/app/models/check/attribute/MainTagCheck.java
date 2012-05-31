package models.check.attribute;

import java.util.Set;

import models.Place;
import models.Tag;
import play.data.validation.Check;

/**
 * Check that the main tag is included in the tag set of the Info
 * @author Guillaume PANNETIER
 */
public class MainTagCheck extends Check
{

  @Override
  public boolean isSatisfied(Object place, Object mainTag)
  {
    // Check there is a main tag to check
    if (mainTag != null)
    {
      // Cast the place to the correct class
      Place castedPlace = (Place) place;

      // Get the tagSet of the place
      Set<Tag> tagSet = castedPlace.tags;

      // Test the tag set contains the main tag of the place
      return tagSet != null && tagSet.contains(mainTag);
    }

    return true;
  }
}
