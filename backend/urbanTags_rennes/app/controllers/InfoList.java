package controllers;

import models.Info;
import models.User;
import play.mvc.Controller;

import com.google.gson.Gson;

public class InfoList extends Controller
{

  public static void item(String json)
  {
    Gson gson = new Gson();

    Info info = new Info();
    info = gson.fromJson(json, Info.class);
    User user = null;
    if (Security.isConnected())
      user = User.find("byEmail", Security.connected()).first();
    render(user, info);
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
