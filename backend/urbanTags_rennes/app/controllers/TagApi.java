package controllers;

import models.Tag;
import play.mvc.Controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class TagApi extends Controller
{

  public static void getTag(long id)
  {
    Tag tag = Tag.findById(id);
    if (tag != null)
    {
      renderJSON(tag);
    }
  }

  public static void getTags(String json)
  {
    Gson gson = new GsonBuilder().create();
    long[] ids = gson.fromJson(json, long[].class);

    Tag[] tags = new Tag[ids.length];
    for (int i = 0; i < ids.length; i++)
    {
      long id = ids[i];
      Tag tag = Tag.findById(id);
      tags[i] = tag;
    }

    renderJSON(tags);
  }
}
