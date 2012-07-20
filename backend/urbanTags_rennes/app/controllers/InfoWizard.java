package controllers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import models.Info;
import models.Place;
import models.Tag;
import models.data.InfoData;
import play.mvc.Controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class InfoWizard extends Controller
{

  public static void getTemplate()
  {
    render("InfoWizard/infoWizard.html");
  }

  public static void firstStep()
  {
    Calendar date = Calendar.getInstance();
    List<Tag> tags = Tag.findAll();
    String day = (date.get(Calendar.DAY_OF_MONTH) < 10) ? ("0" + date.get(Calendar.DAY_OF_MONTH))
      : "" + date.get(Calendar.DAY_OF_MONTH);
    String month = (date.get(Calendar.MONTH) + 1) < 10 ? ("0" + (date.get(Calendar.MONTH) + 1))
      : "" + (date.get(Calendar.MONTH) + 1);
    int year = date.get(Calendar.YEAR);
    render(tags, day, month, year);
  }

  public static void secondStep()
  {
    render();
  }

  public static void thirdStep()
  {
    render();
  }

  public static void validateFirstStep(String json)
  {
    HashMap<String, String> errors = new HashMap<String, String>();
    Gson gson = new GsonBuilder().create();
    InfoData data = gson.fromJson(json, InfoData.class);

    if (!(data.getPlaceId() < 0))
    {
      Place place = Place.findById(data.getPlaceId());
      if (place == null)
      {
        errors.put("general", "Le lieu associé à l'info semble incorrect.");
      }

      // Check title
      if (!data.getTitle().isEmpty())
      {
        if (place != null)
        {
          Info info = Info.find("byTitleAndPlace", data.getTitle(), place).first();
          if (data.getId() == -1 && info != null)
          {
            errors.put("title", "Une autre info associée à ce lieu possède déjà ce titre.");
          }
          else if (data.getId() != -1 && info != null && data.getId() != info.id)
          {
            errors.put("title", "Une autre info associée à ce lieu possède déjà ce titre.");
          }
        }
      }
      else
      {
        errors.put("title", "Le titre ne peut pas être vide.");
      }

      if (!data.getStartDate().isEmpty() && !data.getEndDate().isEmpty()
        && !data.getStartTime().isEmpty() && !data.getEndTime().isEmpty())
      {
        Date startDate = null;
        Date endDate = null;
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        try
        {
          startDate = dateFormat.parse(data.getStartDate() + " " + data.getStartTime());
        }
        catch (java.text.ParseException e)
        {
          errors.put("startDate", "La date de début semble incorrecte.");
        }

        try
        {
          endDate = dateFormat.parse(data.getEndDate() + " " + data.getEndTime());
        }
        catch (java.text.ParseException e)
        {
          errors.put("endDate", "La date de fin semble incorrecte.");
        }

        if (startDate != null && endDate != null && !endDate.after(startDate))
        {
          errors.put("endDate", "La date de fin doit être postérieure à la date de début.");
        }

        Date now = Calendar.getInstance().getTime();

        if (endDate != null && !errors.containsKey("endDate") && !endDate.after(now))
        {
          errors.put("endDate", "La date de fin ne peut pas être déjà passée.");
        }
      }
      else
      {
        errors.put("startDate", "Veuillez indiquer une date de début pour l'événement.");
        errors.put("endDate", "Veuillez indiquer une date de fin pour l'événement.");
      }

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
        errors.put("mainTag", "Veuillez indiquer le tag principal de l'info.");
      }
    }
    else
    {
      badRequest();
    }

    // Return 'bad request' response if errors
    if (errors.size() > 0)
    {
      response.status = 400;
      renderJSON(errors);
    }

    ok();
  }

  public static void validateSecondStep(String json)
  {
    HashMap<String, String> errors = new HashMap<String, String>();
    Gson gson = new GsonBuilder().create();
    InfoData data = gson.fromJson(json, InfoData.class);

    if (data.getContent() == null || data.getContent().isEmpty())
    {
      errors.put("content", "Le contenu d'une info ne peut pas être vide.");
    }

    // Return 'bad request' response if errors
    if (errors.size() > 0)
    {
      response.status = 400;
      renderJSON(errors);
    }

    ok();
  }
}
