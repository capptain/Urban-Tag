/*
 * Copyright 2012 Ubikod
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.ubikod.urbantag;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
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

/**
 * Class handling datapush received from Capptain plateform
 * @author cdesneuf
 */
public class DataPushHandler extends CapptainReachDataPushReceiver
{

  @Override
  protected Boolean onDataPushStringReceived(Context context, String body)
  {
    Log.d("tmp", "String data push message received: " + body);

    try
    {
      /* Create a JSONObject from received string */
      JSONObject json = new JSONObject(body);

      /* Create Managers */
      DatabaseHelper dbHelper = new DatabaseHelper(context, null);
      ContentManager contentManager = new ContentManager(dbHelper);
      PlaceManager placeManager = new PlaceManager(dbHelper);
      TagManager tagManager = new TagManager(dbHelper);

      /* Ensure with info about the place */
      if (json.has("place"))
      {
        JSONObject placeJSON = json.getJSONObject("place");

        /* Ensure we have all needed infos */
        if (json.has("idInfo") && json.has("title") && json.has("mainTag") && json.has("place")
          && json.has("tags") && placeJSON.has("id") && placeJSON.has("name")
          && placeJSON.has("lon") && placeJSON.has("lat") && placeJSON.has("tags")
          && placeJSON.has("mainTag"))
        {
          boolean error = false;

          /* Get mainTag for content */
          Tag contentTag = TagManager.createTag(placeJSON.getJSONObject("mainTag"));
          if (contentTag != null)
          {
            if (!tagManager.exists(contentTag)
              || contentTag.hasChanged(tagManager.get(contentTag.getId())))
            {
              tagManager.save(contentTag);
            }
          }
          else
            error = true;

          /* If the contentTag is not selected we must skip the notification */
          if (!contentTag.isSelected())
            return true;

          List<Tag> contentTagsList = new ArrayList<Tag>(), placeTagsList = new ArrayList<Tag>();

          /* Get all tags for content */
          JSONArray contentTags = json.getJSONArray("tags");
          for (int i = 0; i < contentTags.length(); i++)
          {
            Tag t = TagManager.createTag(contentTags.getJSONObject(i));
            if (t != null)
            {
              if (!tagManager.exists(t) || t.hasChanged(tagManager.get(t.getId())))
              {
                tagManager.save(t);
              }
              contentTagsList.add(t);
            }
            else
              error = true;
          }

          /* idem for place */
          JSONArray placeTags = placeJSON.getJSONArray("tags");
          for (int i = 0; i < placeTags.length(); i++)
          {
            Tag t = TagManager.createTag(placeTags.getJSONObject(i));
            if (t != null)
            {
              if (!tagManager.exists(t) || t.hasChanged(tagManager.get(t.getId())))
              {
                tagManager.save(t);
              }
              placeTagsList.add(t);
            }
            else
              error = true;
          }

          /* Get mainTag for place */
          Tag placeTag = TagManager.createTag(placeJSON.getJSONObject("mainTag"));
          if (placeTag != null)
          {
            if (!tagManager.exists(placeTag)
              || placeTag.hasChanged(tagManager.get(placeTag.getId())))
            {
              tagManager.save(placeTag);
            }
          }
          else
            error = true;

          /* In case of error just quit method */
          if (error)
            return true;

          /* Create place and store it if new or update if changed */
          Place p = new Place(placeJSON.getInt("id"), placeJSON.getString("name"), placeTag,
            new GeoPoint((int) (placeJSON.getDouble("lat") * 1E6),
              (int) (placeJSON.getDouble("lon") * 1E6)), placeTagsList);

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

          /* Create content and store it */
          Content c = new Content(json.getInt("idInfo"), json.getString("title"),
            json.getInt("startDate"), json.getInt("endDate"), p, contentTag, contentTagsList);
          contentManager.save(c);

          /* Notify */
          NotificationHelper notificationHelper = new NotificationHelper(context);
          /* if endDate or startDate equals to -1 it's a place description */
          if (c.getEndDate() == -1 || c.getStartDate() == -1)
          {
            notificationHelper.notifyNewPlace(c.getId(), c.getName());
          }
          else
            notificationHelper.notifyNewContent(c.getId(), c.getName(), p.getName());

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
