package controllers;

import java.util.Date;

import models.Info;
import models.Place;
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

  public static void getMinDate(long idPlace)
  {
    Place place = Place.findById(idPlace);
    Info info = Info.find("byPlace order by startDate", place).first();
    Date minDate = info.startDate;
    renderText(minDate.getTime());
  }

  public static void getMaxDate(long idPlace)
  {
    Place place = Place.findById(idPlace);
    Info info = Info.find("byPlace order by startDate desc", place).first();
    Date maxDate = info.startDate;
    renderText(maxDate.getTime());
  }
}
