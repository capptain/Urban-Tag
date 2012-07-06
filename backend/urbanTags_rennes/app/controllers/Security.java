package controllers;

import models.Roles;
import models.User;

public class Security extends Secure.Security
{
  static boolean authenticate(String username, String password)
  {
    return User.connect(username, password) != null;
  }

  static void onDisconnected()
  {
    Application.index();
  }

  static void onAuthenticated()
  {
    Application.index();
  }

  static boolean check(String profile)
  {
    User user = User.find("byEmail", connected()).<User> first();
    if (user == null)
      return false;

    if ("admin".equals(profile))
    {
      return user.role != null && user.role.equals(Roles.ADMIN);
    }

    return false;
  }

  public static boolean getIsConnected()
  {
    return isConnected();
  }

  public static String getConnected()
  {
    return connected();
  }
}
