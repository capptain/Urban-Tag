package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import models.Place;
import models.Tag;
import play.mvc.Controller;
import play.mvc.With;

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

  public static void validateFirstStep(long id, String name, String accuracy, String expiration,
    String tags, String mainTag)
  {
    HashMap<String, String> errors = new HashMap<String, String>();

    // Name validation
    if (!name.isEmpty())
    {
      long count = Place.count("byName", name);
      if (id == -1)
      {
        if (count > 0)
          errors.put("name", "Un lieu portant le nom '" + name + "' existe déjà.");
      }
      else
      {
        Place place = Place.findById(id);
        if (place == null)
        {
          errors.put("id", "L'identifiant du lieu est incorrect.");
        }
        else
        {
          Place otherPlace = Place.find("byName", name).first();
          if (otherPlace != null)
          {
            if (otherPlace.id != id)
            {
              errors.put("name", "Un autre lieu portant le nom '" + name + "' existe déjà.");
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
    try
    {
      int castedAccuracy = Integer.parseInt(accuracy);
      if (castedAccuracy < 0)
        errors.put("accuracy", "Le seuil de précision doit être positif.");
    }
    catch (NumberFormatException e)
    {
      errors.put("accuracy", "Le seuil de précision doit être un entier positif.");
    }

    // Expiration validation
    try
    {
      int castedExpiration = Integer.parseInt(expiration);
      if (castedExpiration < 0)
        errors.put("expiration", "Le seuil d'expiration doit être positif.");
    }
    catch (NumberFormatException e)
    {
      errors.put("expiration", "Le seuil d'expiration doit être un entier positif.");
    }

    // Tags validation
    List<Long> castedTags = new ArrayList<Long>();
    if (tags.equals("null"))
    {
      errors.put("tags", "Veuillez indiquer au moins un tag.");
    }
    else
    {
      try
      {
        String[] tagArray = tags.split(",");

        for (String tag : tagArray)
        {
          long tagId = Long.parseLong(tag);
          long count = Tag.count("byId", tagId);
          if (count != 1)
          {
            errors.put("tags", "Tag incorrect.");
            break;
          }
          else
            castedTags.add(tagId);
        }
      }
      catch (NumberFormatException e)
      {
        errors.put("tags", "Tag incorrect.");
      }
    }

    // Maintag validation
    try
    {
      long tagId = Long.parseLong(mainTag);
      if (!castedTags.contains(tagId))
      {
        errors.put("mainTag", "Le tag principal doit faire parti des tags sélectionnés.");
      }
    }
    catch (NumberFormatException e)
    {
      errors.put("mainTag", "Tag principal incorrect.");
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
