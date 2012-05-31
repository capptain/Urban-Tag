package controllers;

import java.util.List;

import models.Place;
import models.User;
import play.mvc.Controller;

public class PlacesApi extends Controller
{

  public static void getPlaceList()
  {
    if (Security.isConnected())
    {
      User user = User.find("byEmail", Security.connected()).first();

      if (user != null)
      {
        List<Place> places = Place.find("byOwner", user).fetch();
        renderJSON(places);
      }
    }

    List<Place> places = Place.findAll();
    renderJSON(places);
  }

  public static void getPlace(long id)
  {
    Place place = Place.findById(id);

    if (place != null)
      renderJSON(place);
  }
}
