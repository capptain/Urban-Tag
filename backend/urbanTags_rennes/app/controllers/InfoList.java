package controllers;

import models.User;
import play.mvc.Before;
import play.mvc.Controller;

public class InfoList extends Controller
{
  @Before
  static void setConnectedUser()
  {
    if (Security.isConnected())
    {
      User user = User.find("byEmail", Security.connected()).first();
      if (user != null)
      {
        renderArgs.put("user", user.username);
        renderArgs.put("userId", user.id);
      }
    }
  }

  public static void item()
  {
    render();
  }

  public static void getTemplate()
  {
    render();
  }

  public static void empty()
  {
    render();
  }
}
