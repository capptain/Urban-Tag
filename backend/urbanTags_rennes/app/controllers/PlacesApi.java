package controllers;

import java.util.List;

import models.Info;
import models.Place;
import models.Tag;
import models.User;
import models.data.PlaceData;
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

          place.name = data.getName();
          place.expiration = data.getExpiration();
          place.accuracy = data.getAccuracy();
          place.longitude = data.getLongitude();
          place.latitude = data.getLatitude();
          place.radius = data.getRadius();

          place.removeAllTags();
        }
        else
        {
          place = new Place(user, data.getName(), data.getLongitude(), data.getLatitude(),
            data.getRadius(), data.getAccuracy(), data.getExpiration());
        }

        for (int i = 0; i < data.getTags().length; i++)
        {
          long tagId = data.getTags()[i];
          boolean isMain = (tagId == data.getMainTag());
          Tag tag = Tag.findById(tagId);
          place.tagItWith(tag, isMain);
        }

        if (place.validateAndSave())
        {
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
            List<Info> infos = Info.find("byPlace", place).fetch();
            for (Info info : infos)
            {
              info.delete();
            }

            place.delete();

            ok();
          }
        }
      }
    }

    badRequest();
  }
}
