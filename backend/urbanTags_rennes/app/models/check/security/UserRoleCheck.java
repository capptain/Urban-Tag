package models.check.security;

import play.data.validation.Check;

public class UserRoleCheck extends Check
{

  @Override
  public boolean isSatisfied(Object validatedObject, Object value)
  {
    String role = (String) value;
    return (role.equals("") || role.equals("Basic") || role.equals("Admin"));
  }
}
