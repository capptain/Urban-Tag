package controllers;

import java.util.HashMap;
import java.util.List;

import models.Place;
import models.Tag;
import models.check.attribute.PlaceAccuracyCheck;
import models.data.PlaceData;
import play.mvc.Controller;
import play.mvc.With;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

@With(Secure.class)
public class PlaceWizard extends Controller
{
  public static void firstStep()
  {
    List<Tag> tags = Tag.findAll();
    render(tags);
  }

  public static void secondStep()
  {
    render();
  }

  public static void thirdStep()
  {
    render();
  }

  public static void getTemplate()
  {
    renderTemplate("PlaceWizard/placeWizard.html");
  }

  public static void validateFirstStep(String json)
  {
    Gson gson = new GsonBuilder().create();
    PlaceData data = null;

    try
    {
      data = gson.fromJson(json, PlaceData.class);
    }
    catch (JsonSyntaxException e)
    {
      badRequest();
    }

    HashMap<String, String> errors = new HashMap<String, String>();

    // Name validation
    if (!data.getName().isEmpty())
    {
      long count = Place.count("byName", data.getName());
      if (data.getId() == -1)
      {
        if (count > 0)
          errors.put("name", "Un lieu portant le nom '" + data.getName() + "' existe déjà.");
      }
      else
      {
        Place place = Place.findById(data.getId());
        if (place == null)
        {
          errors.put("id", "L'identifiant du lieu est incorrect.");
        }
        else
        {
          Place otherPlace = Place.find("byName", data.getName()).first();
          if (otherPlace != null)
          {
            if (otherPlace.id != data.getId())
            {
              errors.put("name", "Un autre lieu portant le nom '" + data.getName()
                + "' existe déjà.");
            }
          }
        }
      }
    }
    else
    {
      errors.put("name", "Le nom ne peut pas être vide.");
    }

    // Accuracy validation
    if (!new PlaceAccuracyCheck().isSatisfied(null, data.getAccuracy()))
      errors.put("accuracy", "La valeur de la précision est incorrecte.");

    // Tags validation
    for (long tagId : data.getTags())
    {
      long count = Tag.count("byId", tagId);
      if (count != 1)
      {
        errors.put("tags", "Tag incorrect.");
        break;
      }
    }

    // Maintag validation
    if (data.getMainTag() == -1)
    {
      errors.put("mainTag", "Veuillez indiquer le tag principal du lieu.");
    }

    // If errors, send a "bad request" answer
    if (errors.size() > 0)
    {
      response.status = 400;
      renderJSON(errors);
    }

    ok();
  }

  public static void validateSecondStep(String _longitude, String _latitude, String _radius)
    throws Exception
  {
    HashMap<String, String> errors = new HashMap<String, String>();
    double longitude, latitude;
    int radius;

    try
    {
      longitude = Double.parseDouble(_longitude);

      if (longitude < -180 || longitude > 180)
        throw new Exception("La longitude doit être comprise entre -180 et 180.");
    }
    catch (NumberFormatException e)
    {
      errors.put("longitude", "La longitude doit être un nombre décimal compris entre -180 et 180.");
    }
    catch (Exception e)
    {
      errors.put("longitude", e.getMessage());
    }

    try
    {
      latitude = Double.parseDouble(_latitude);

      if (latitude < -90 || latitude > 90)
        throw new Exception("La latitude doit être comprise entre -90 et 90.");
    }
    catch (NumberFormatException e)
    {
      errors.put("latitude", "La latitude doit être un nombre décimal compris entre -90 et 90.");
    }
    catch (Exception e)
    {
      errors.put("latitude", e.getMessage());
    }

    try
    {
      radius = Integer.parseInt(_radius);

      if (radius < 10 || radius > 300)
        throw new Exception("Le rayon doit être compris entre 10 mètres et 300 mètres.");
    }
    catch (NumberFormatException e)
    {
      errors.put("radius", "Le rayon doit être un entier compris entre 10 et 300.");
    }
    catch (Exception e)
    {
      errors.put("radius", e.getMessage());
    }

    // If errors, send a "bad request" answer
    if (errors.size() > 0)
    {
      response.status = 400;
      renderJSON(errors);
    }

    ok();
  }
}
