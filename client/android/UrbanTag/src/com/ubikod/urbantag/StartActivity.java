package com.ubikod.urbantag;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.ubikod.urbantag.model.DatabaseHelper;
import com.ubikod.urbantag.model.Tag;
import com.ubikod.urbantag.model.TagManager;

public class StartActivity extends Activity
{
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    // re initiate wifi message display
    SharedPreferences pref = getApplicationContext().getSharedPreferences("URBAN_TAG_PREF",
      Context.MODE_PRIVATE);
    pref.edit().putBoolean("notifiedWifi", false).commit();
    // Temp !!!!!!
    loadData();
    startActivity(new Intent(this, UrbanTagMainActivity.class));
    finish();
  }

  private void loadData()
  {
    Log.i("Loading data", "Dooooo");
    Tag t0 = new Tag(0, "Musique", 0xff79D438), t1 = new Tag(1, "Architecture", 0xffF86883), t2 = new Tag(
      2, "Insolite", 0xff56B2E1), t3 = new Tag(3, "Bla", 0xffF9D424), t4 = new Tag(4, "Poney",
      0xffEB0066), t5 = new Tag(5, "Horlogerie", 0xffD7E60E), t6 = new Tag(6, "Equitation",
      0xffF119B6);
    List<Tag> tagsList = new ArrayList<Tag>();
    tagsList.add(t1);
    tagsList.add(t2);
    tagsList.add(t3);
    tagsList.add(t6);
    tagsList.add(t0);
    tagsList.add(t4);
    tagsList.add(t5);
    DatabaseHelper dbHelper = new DatabaseHelper(this, null);
    TagManager tagManager = new TagManager(dbHelper);
    tagManager.update(tagsList);
    // PlaceManager placeManager = new PlaceManager(dbHelper);
    // Place p = new Place(1, "Ubikod", t3, new GeoPoint(-1672014, 48107096), tagsList);
    // placeManager.save(p);
    // p = new Place(2, "Ubu", t0, new GeoPoint(-1673387, 48107976), tagsList);
    // placeManager.save(p);
    // Place p2 = p;
    // p = new Place(3, "Thêatre National de Bretagne", t6, new GeoPoint(-1672701, 48107789),
    // tagsList);
    // placeManager.save(p);

    // p = new Place(4, "blaaaaaaaaa", t6, new GeoPoint(48104093, -1672497), tagsList);
    // placeManager.supress(p);

    // ContentManager contentManager = new ContentManager(dbHelper);
    // Content c = new Content(5, "un truc", 1, 1, p2, t3, tagsList);
    // contentManager.save(c);

  }
}
