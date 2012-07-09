package com.ubikod.urbantag;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Vibrator;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.ubikod.capptain.android.sdk.reach.CapptainReachDataPushReceiver;
import com.ubikod.urbantag.model.Content;
import com.ubikod.urbantag.model.ContentManager;
import com.ubikod.urbantag.model.DatabaseHelper;
import com.ubikod.urbantag.model.Place;
import com.ubikod.urbantag.model.PlaceManager;
import com.ubikod.urbantag.model.Tag;
import com.ubikod.urbantag.model.TagManager;

public class DataPushHandler extends CapptainReachDataPushReceiver
{
  @Override
  protected Boolean onDataPushStringReceived(Context context, String body)
  {
    Log.d("tmp", "String data push message received: " + body);

    try
    {
      JSONObject json = new JSONObject(body);
      DatabaseHelper dbHelper = new DatabaseHelper(context, null);
      ContentManager contentManager = new ContentManager(dbHelper);
      PlaceManager placeManager = new PlaceManager(dbHelper);
      TagManager tagManager = new TagManager(dbHelper);

      boolean error = false;

      if (json.has("place"))
      {
        JSONObject placeJSON = json.getJSONObject("place");

        if (json.has("idInfo") && json.has("title") && json.has("mainTag") && json.has("place")
          && json.has("tags") && placeJSON.has("id") && placeJSON.has("name")
          && placeJSON.has("lon") && placeJSON.has("lat") && placeJSON.has("tags")
          && placeJSON.has("mainTag"))
        {
          List<Tag> contentTags = new ArrayList<Tag>(), placeTags = new ArrayList<Tag>();

          JSONArray contentTagsId = json.getJSONArray("tags");
          for (int i = 0; i < contentTagsId.length(); i++)
          {
            Tag t = tagManager.get(contentTagsId.getInt(i));
            contentTags.add(t);
          }

          JSONArray placeTagsId = placeJSON.getJSONArray("tags");
          for (int i = 0; i < contentTagsId.length(); i++)
          {
            Tag t = tagManager.get(contentTagsId.getInt(i));
            placeTags.add(t);
          }

          Tag placeTag = tagManager.get(placeJSON.getInt("mainTag"));
          Tag contentTag = tagManager.get(placeJSON.getInt("mainTag"));

          Place p = new Place(placeJSON.getInt("id"), placeJSON.getString("name"), placeTag,
            new GeoPoint((int) (placeJSON.getDouble("lat") * 1E6),
              (int) (placeJSON.getDouble("lon") * 1E6)), placeTags);

          if (!placeManager.exists(p))
          {
            placeManager.save(p);
            UrbanTagMainActivity.notifyNewPlace();
          }
          else
          {
            if (p.hasChanged(placeManager.get(p.getId())))
            {
              placeManager.save(p);
              UrbanTagMainActivity.notifyNewPlace();
            }
          }

          int start = 0, end = 0;
          if (json.has("startDate") && json.has("endDate"))
          {
            start = json.getInt("startDate");
            end = json.getInt("endDate");
          }
          Content c = new Content(json.getInt("idInfo"), json.getString("title"), start, end, p,
            contentTag, contentTags);

          contentManager.save(c);

          Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
          vibrator.vibrate(1000);
        }
        else
          Log.e("DataPushHandler", "Missing info in JSON");
      }
      else
        Log.e("DataPushHandler", "Missing place info in JSON");

    }
    catch (JSONException e)
    {
      Log.e("JSON", "Exception :" + e.getMessage());
    }
    return true;
  }

  @Override
  protected Boolean onDataPushBase64Received(Context context, byte[] decodedBody, String encodedBody)
  {
    Log.d("tmp", "Base64 data push message received: " + encodedBody);
    // Do something useful with decodedBody like updating an image view
    return true;
  }

}
