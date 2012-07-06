package models.check.attribute;

import java.util.Set;

import models.Info;
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
  public boolean isSatisfied(Object object, Object mainTag)
  {
    try
    {
      // Check there is a main tag to check
      if (mainTag != null)
      {
        // Get the tagSet of the object
        Set<Tag> tagSet = null;

        if (object instanceof Place)
        {
          tagSet = ((Place) object).tags;
        }
        else if (object instanceof Info)
        {
          tagSet = ((Info) object).tags;
        }

        // Test the tag set contains the main tag of the place
        return tagSet != null && tagSet.contains(mainTag);
      }

      return true;
    }
    catch (Throwable t)
    {
      return false;
    }
  }
}
