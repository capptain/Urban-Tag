package com.ubikod.urbantag;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

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
    startActivity(new Intent(this, UrbanTagMainActivity.class));
    finish();
  }
}
