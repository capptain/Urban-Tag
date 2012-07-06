package controllers;

import java.util.Date;
import java.util.List;

import models.Info;
import models.Place;
import models.User;
import models.data.InfoData;
import play.data.validation.Validation;
import play.mvc.Controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class InfoApi extends Controller
{

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

      Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

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

  public static void add(String json) throws Exception
  {
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
        if (id != -1)
        {
          info = Info.findById(data.getId());
          data.setAddedAt(info.addedAt.getTime());

          /* Check place owner is correct */
          if (info.place == null || info.place.owner.id != user.id)
          {
            badRequest();
          }
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

        /* Save if valid */
        if (Validation.current().valid(info).ok)
        {
          info.save();
          Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
          renderJSON(gson.toJson(info));
        }
      }
    }

    badRequest();
  }
}
