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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;

import com.ubikod.capptain.android.sdk.CapptainAgent;
import com.ubikod.capptain.android.sdk.CapptainAgentUtils;

/**
 * Class containing common actions made by several activities
 * @author cdesneuf
 */
public class Common
{

  /**
   * Action to perform on resume
   * @param activity
   */
  public static void onResume(final Activity activity)
  {
    String activityNameOnCapptain = CapptainAgentUtils.buildCapptainActivityName(activity.getClass());
    CapptainAgent.getInstance(activity).startActivity(activity, activityNameOnCapptain, null);
    LocationManager locationManager = (LocationManager) activity.getSystemService(Activity.LOCATION_SERVICE);
    WifiManager wifiManager = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);

    boolean wifiChecked = wifiManager.isWifiEnabled()
      || wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING;
    boolean networkChecked = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

    SharedPreferences pref = activity.getSharedPreferences("URBAN_TAG_PREF", Context.MODE_PRIVATE);
    boolean notifiedWifi = pref.getBoolean("notifiedWifi", false);

    if (!networkChecked)
    {

      new AlertDialog.Builder(activity).setTitle(R.string.activate_geoloc)
        .setMessage(activity.getResources().getString(R.string.activate_geoloc_confirm))
        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
        {
          @Override
          public void onClick(DialogInterface dialog, int which)
          {
            activity.startActivity(new Intent(
              android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
          }
        })
        .setOnCancelListener(new DialogInterface.OnCancelListener()
        {

          @Override
          public void onCancel(DialogInterface dialog)
          {
            activity.moveTaskToBack(true);
          }
        })
        .show();
    }

    if (!notifiedWifi && networkChecked && !wifiChecked)
    {
      pref.edit().putBoolean("notifiedWifi", true).commit();
      new AlertDialog.Builder(activity).setTitle(R.string.advice)
        .setMessage(R.string.advice_unable_wifi)
        .setPositiveButton(R.string.i_agree, new DialogInterface.OnClickListener()
        {
          @Override
          public void onClick(DialogInterface dialog, int which)
          {
            // Activate Wi-Fi
            WifiManager wifiManager = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
            wifiManager.setWifiEnabled(true);
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity);
            pref.edit().putBoolean("active_wifi", true).commit();
          }
        })
        .setNegativeButton(R.string.dont_want, null)
        .show();

    }
  }

  /**
   * Action to perform on pause
   * @param activity
   */
  public static void onPause(final Activity activity)
  {
    CapptainAgent.getInstance(activity).endActivity();
  }

}
