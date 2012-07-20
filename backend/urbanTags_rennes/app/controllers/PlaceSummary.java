package controllers;

import models.User;
import play.mvc.Before;
import play.mvc.Controller;

public class PlaceSummary extends Controller
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

  public static void getTemplate()
  {
    render("Place/summary/placeSummary.html");
  }

  public static void getEmptyMessage()
  {
    render("Place/summary/emptyMessage.html");
  }

}
