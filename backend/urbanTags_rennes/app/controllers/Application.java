package controllers;

import models.Info;
import models.Place;
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
      {
        renderArgs.put("user", user.username);
        renderArgs.put("userId", user.id);
      }
    }
  }

  public static void index()
  {
    render();
  }

  public static void showInfo(long id)
  {
    Info info = Info.findById(id);
    Place place = info.place;
    if (info != null)
    {
      render(place, info);
    }
  }

}