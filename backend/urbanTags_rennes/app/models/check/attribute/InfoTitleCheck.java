package models.check.attribute;

import models.Info;
import play.data.validation.Check;

/**
 * Check that the title is uniq for the current Place
 * @author Guillaume PANNETIER
 */
public class InfoTitleCheck extends Check
{

  @Override
  public boolean isSatisfied(Object _info, Object _title)
  {
    try
    {
      Info info = (Info) _info;
      String title = (String) _title;

      long count = Info.count("byPlaceAndIdNotEqualAndTitle", info.place, info.id, title);
      return count == 0;
    }
    catch (Throwable t)
    {
      return false;
    }
  }
}
