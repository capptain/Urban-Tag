package controllers;

import java.util.List;

import models.Place;
import models.User;
import models.data.PlaceData;
import play.data.validation.Validation;
import play.mvc.Controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class PlacesApi extends Controller
{

  public static void getPlaceList()
  {
    List<Place> places = Place.findAll();
    Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    renderJSON(gson.toJson(places));
  }

  public static void getPlace(long id)
  {
    Place place = Place.findById(id);

    if (place != null)
    {
      Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
      renderJSON(gson.toJson(place));
    }
  }

  public static void savePlace(String json)
  {
    PlaceData data = new Gson().fromJson(json, PlaceData.class);
    if (Security.isConnected())
    {
      User user = User.find("byEmail", Security.connected()).first();
      if (user != null)
      {
        data.setIdOwner(user.id);
        long id = data.getId();

        Place place;

        // If place edition
        if (id != -1)
        {
          place = Place.findById(data.getId());

          // Check owner is correct
          if (place == null || place.owner.id != user.id)
          {
            badRequest();
          }
        }
        else
        {
          place = new Place();
        }

        place.setData(data);

        /* Save if valid */
        if (Validation.current().valid(place).ok)
        {
          place.save();
          Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
          renderJSON(gson.toJson(place));
        }
      }
    }
    badRequest();
  }

  public static void deletePlace(long id)
  {
    if (Security.isConnected())
    {
      User user = User.find("byEmail", Security.connected()).first();
      if (user != null)
      {
        Place place = Place.findById(id);
        if (place != null)
        {
          if (place.owner.id == user.id)
          {
            place.delete();
            ok();
          }
        }
      }
    }

    badRequest();
  }
}
