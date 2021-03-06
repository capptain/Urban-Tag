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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.ubikod.urbantag.model.ContentManager;
import com.ubikod.urbantag.model.DatabaseHelper;
import com.ubikod.urbantag.model.PlaceManager;

/**
 * Preferences Activity
 * @author cdesneuf
 */
public class PreferencesActivity extends SherlockPreferenceActivity
{

  public static final int CODE = 1;

  @SuppressWarnings("deprecation")
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preferences);
    setTitle(R.string.menu_preferences);
    com.actionbarsherlock.app.ActionBar actionBar = this.getSupportActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);

    OnPreferenceChangeListener goToSettings = new OnPreferenceChangeListener()
    {
      @Override
      public boolean onPreferenceChange(Preference preference, Object newValue)
      {
        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        return false;
      }
    };

    Preference active_gps = (Preference) findPreference("active_gps");
    active_gps.setOnPreferenceChangeListener(goToSettings);

    /* Enable or disable WIFI on click */
    Preference active_wifi = (Preference) findPreference("active_wifi");
    active_wifi.setOnPreferenceClickListener(new OnPreferenceClickListener()
    {

      @Override
      public boolean onPreferenceClick(Preference preference)
      {
        CheckBoxPreference cb = (CheckBoxPreference) preference;
        WifiManager wifiManager = (WifiManager) PreferencesActivity.this.getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(cb.isChecked());
        SharedPreferences pref = getApplicationContext().getSharedPreferences("URBAN_TAG_PREF",
          Context.MODE_PRIVATE);
        pref.edit().putBoolean("notifiedWifi", cb.isChecked()).commit();
        return true;
      }
    });

    /* Delete history */
    Preference delete_history = (Preference) findPreference("delete_history");
    delete_history.setOnPreferenceClickListener(new OnPreferenceClickListener()
    {
      public boolean onPreferenceClick(Preference preference)
      {
        new AlertDialog.Builder(PreferencesActivity.this).setTitle(R.string.delete_history)
          .setMessage(
            PreferencesActivity.this.getResources().getString(R.string.delete_history_confirm))
          .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
          {

            @Override
            public void onClick(DialogInterface dialog, int which)
            {
              DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext(), null);
              PlaceManager placeManager = new PlaceManager(dbHelper);
              ContentManager contentManager = new ContentManager(dbHelper);
              placeManager.clear();
              contentManager.clear();
              // Notify main activity it needs to clean the map
              setResult(1);
            }

          })
          .setNegativeButton(R.string.no, null)
          .show();
        return true;
      }
    });

  }

  @Override
  @SuppressWarnings("deprecation")
  public void onResume()
  {
    super.onResume();
    Common.onResume(this);

    /* Display preference accordingly to current system state */
    final CheckBoxPreference active_wifi = (CheckBoxPreference) findPreference("active_wifi");
    final CheckBoxPreference active_gps = (CheckBoxPreference) findPreference("active_gps");
    WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
    LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    boolean wifiChecked = wifiManager.isWifiEnabled()
      || wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING;
    boolean gpsChecked = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    active_wifi.setChecked(wifiChecked);
    active_gps.setChecked(gpsChecked);
  }

  @Override
  public void onPause()
  {
    super.onPause();
    Common.onPause(this);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    switch (item.getItemId())
    {
      case android.R.id.home:
        finish();
        break;
    }
    return false;
  }

}
