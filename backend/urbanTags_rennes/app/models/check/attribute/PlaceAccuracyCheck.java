package models.check.attribute;

import play.data.validation.Check;

public class PlaceAccuracyCheck extends Check
{

  @Override
  public boolean isSatisfied(Object validatedObject, Object value)
  {
    try
    {
      String accuracy = (String) value;
      return (accuracy.equals("low") || accuracy.equals("medium") || accuracy.equals("high"));
    }
    catch (Exception e)
    {
    }
    return false;
  }

}
