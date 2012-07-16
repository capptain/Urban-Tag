package com.ubikod.urbantag;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.ubikod.capptain.android.sdk.CapptainApplication;
import com.ubikod.urbantag.model.DatabaseHelper;
import com.ubikod.urbantag.model.Tag;
import com.ubikod.urbantag.model.TagManager;

public class UrbanTagApplication extends CapptainApplication
{
  @Override
  protected void onApplicationProcessCreate()
  {
    NotificationHelper notifHelper = new NotificationHelper(this);
    notifHelper.notifyAppliRunning();

    // re initiate wifi message display
    SharedPreferences pref = getApplicationContext().getSharedPreferences("URBAN_TAG_PREF",
      Context.MODE_PRIVATE);
    pref.edit().putBoolean("notifiedWifi", false).commit();

    new AsyncTask<Void, Void, List<Tag>>()
    {

      @Override
      protected List<Tag> doInBackground(Void... v)
      {
        List<Tag> res = new ArrayList<Tag>();

        // TODO Auto-generated method stub
        return res;
      }

      @Override
      protected void onPostExecute(List<Tag> list)
      {
        TagManager tagManager = new TagManager(new DatabaseHelper(getApplicationContext(), null));
        tagManager.update(list);
      }
    };
  }
}
