package controllers;

import models.User;
import play.mvc.Before;
import play.mvc.Controller;

public class Application extends Controller
{
  @Before
  static void setConnectedUser()
  {
    if (Security.isConnected())
    {
      User user = User.find("byEmail", Security.connected()).first();
      if (user != null)
        renderArgs.put("user", user.username);
    }
  }

  public static void index()
  {
    render();
  }

}