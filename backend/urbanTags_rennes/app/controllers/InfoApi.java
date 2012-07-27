package controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.Info;
import models.Place;
import models.User;
import models.data.InfoData;
import play.Play;
import play.data.validation.Validation;
import play.data.validation.Validation.ValidationResult;
import play.mvc.Controller;
import ru.perm.kefir.bbcode.BBProcessorFactory;
import ru.perm.kefir.bbcode.TextProcessor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class InfoApi extends Controller
{

  public static void getPlaceDescription(long placeId)
  {
    GsonBuilder gb = new GsonBuilder();
    Gson gson = gb.excludeFieldsWithoutExposeAnnotation().create();
    Place place = Place.findById(placeId);
    if (place != null)
    {
      Info description = Info.find("byPlaceAndStartDateIsNullAndEndDateIsNull", place).first();
      if (description != null)
      {
        String json = gson.toJson(description);
        renderJSON(json);
      }
      else
      {
        renderText("");
      }
    }

    badRequest();
  }

  public static void getPlaceEvents(long placeId, String filter, long startDate, long endDate)
  {
    GsonBuilder gb = new GsonBuilder();
    gb.setDateFormat("dd/MM/yyyy HH:mm");
    Gson gson = gb.excludeFieldsWithoutExposeAnnotation().create();
    Place place = Place.findById(placeId);

    Date objStartDate = null;
    Date objEndDate = null;

    String query = "place=? and startDate is not null and endDate is not null";
    if (startDate != -1 && endDate != -1)
    {
      objStartDate = new Date();
      objStartDate.setTime(startDate);
      objEndDate = new Date();
      objEndDate.setTime(endDate);

      if (filter.equals("createdIn"))
      {
        query += " and addedAt between ? and ?";
      }
      else if (filter.equals("activeIn"))
      {
        query += " and endDate >= ?";
        query += " and startDate <= ?";
      }
      else if (filter.equals("startIn"))
      {
        query += " and startDate between ? and ?";
      }
      else if (filter.equals("finishIn"))
      {
        query += " and endDate between ? and ?";
      }
    }

    query += " order by startDate desc";

    if (place != null)
    {
      List<Info> events = null;

      if (startDate != -1 && endDate != -1)
        events = Info.find(query, place, objStartDate, objEndDate).fetch();
      else
        events = Info.find(query, place).fetch();

      String json = gson.toJson(events);
      renderJSON(json);
    }

    badRequest();
  }

  public static void getPlaceInfos(long id, int from, int to)
  {
    Place place = Place.findById(id);
    if (place != null)
    {
      List<Info> infos;

      if (to > 0)
        infos = Info.find("byPlace", place).from(from).fetch(to);
      else
        infos = Info.find("byPlace", place).from(from).fetch();

      GsonBuilder gb = new GsonBuilder();
      gb.setDateFormat("dd/MM/yyyy HH:mm");
      Gson gson = gb.excludeFieldsWithoutExposeAnnotation().create();

      renderJSON(gson.toJson(infos));
    }
    else
      flash.error("L'identifiant du lieu semble incorrect.", id);
  }

  public static void deleteInfo(long id)
  {
    if (Security.isConnected())
    {
      User user = User.find("byEmail", Security.connected()).first();
      Info info = Info.findById(id);

      if (info.place.owner.id == user.id)
      {
        try
        {
          if (info.delete() != null)
            flash.success("L'info a été supprimée correctement.", id);
        }
        catch (Exception e)
        {
          flash.error("Une erreur est survenue lors de la suppression de l'info.");
        }
      }
      else
      {
        flash.error("Vous n'avez pas les droits nécessaires pour réaliser cette action.");
      }
    }
    else
    {
      flash.error("Vous devez être connecté pour accéder à cette fonctionnalité.");
    }
  }

  public static void webView(long id)
  {
    Info info = Info.findById(id);
    if (info != null)
    {
      String bbCode = Play.applicationPath.getPath() + "/conf/bbCodeConfig.xml";
      TextProcessor bbcodeParser = BBProcessorFactory.getInstance().create(new File(bbCode));
      String parsedContent = bbcodeParser.process(info.content);
      render(info, parsedContent);
    }
    else
    {
      notFound();
    }
  }

  public static void webContent(long id)
  {
    Info info = Info.findById(id);
    if (info != null)
    {
      String bbCode = Play.applicationPath.getPath() + "/conf/bbCodeConfig.xml";
      TextProcessor bbcodeParser = BBProcessorFactory.getInstance().create(new File(bbCode));
      String parsedContent = bbcodeParser.process(info.content);
      render(info, parsedContent);
    }
    else
    {
      notFound();
    }
  }

  public static void getParsedContent(String content) throws Exception
  {
    String bbCode = Play.applicationPath.getPath() + "/conf/bbCodeConfig.xml";
    TextProcessor bbcodeParser = BBProcessorFactory.getInstance().create(new File(bbCode));
    String parsedContent = bbcodeParser.process(content);
    renderText(parsedContent);
  }

  public static void add(String json) throws Exception
  {
    List<String> errors = new ArrayList<String>();

    /* Check user is connected */
    InfoData data = new Gson().fromJson(json, InfoData.class);
    if (Security.isConnected())
    {
      User user = User.find("byEmail", Security.connected()).first();
      if (user != null)
      {
        long id = data.getId();
        Info info = null;

        /* If info edition */
        info = Info.findById(data.getId());
        if (id != -1 && info != null)
        {
          data.setAddedAt(info.addedAt.getTime());
        }
        /* Info creation */
        else
        {
          data.setAddedAt(new Date().getTime());
          info = new Info();
        }

        /* Set new data */
        try
        {
          info.setData(data);
        }
        catch (Exception e)
        {
          e.printStackTrace();
          badRequest();
        }

        /* Check place owner is correct */
        if (info.place == null || info.place.owner.id != user.id)
        {
          badRequest();
        }

        /* Check unicity if the info is a description */
        if (info.startDate == null && info.endDate == null)
        {
          Place place = Place.findById(data.getPlaceId());
          if (place != null)
          {
            Info description = Info.find("byPlaceAndStartDateIsNullAndEndDateIsNull", place)
              .first();

            if (description != null && description.id != info.id)
            {
              errors.add("Une description est déjà présente pour ce lieu.");
            }
          }
        }

        if (errors.size() == 0)
        {
          /* Save if valid */
          ValidationResult validationResult = Validation.current().valid(info);
          if (validationResult.ok)
          {
            if (info.save() != null)
            {
              GsonBuilder gb = new GsonBuilder();
              gb.setDateFormat("dd/MM/yyyy HH:mm");
              Gson gson = gb.excludeFieldsWithoutExposeAnnotation().create();
              renderJSON(gson.toJson(info));
            }
          }
        }
      }
    }

    errors.add("La validation de l'info a échoué, certaines valeurs sont invalides.");
    response.status = 400;
    renderJSON(errors);
  }
}
