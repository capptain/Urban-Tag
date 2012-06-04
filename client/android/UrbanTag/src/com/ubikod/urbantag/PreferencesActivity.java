package com.ubikod.urbantag;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

public class PreferencesActivity extends SherlockPreferenceActivity
{

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
    Preference active_network = (Preference) findPreference("active_network");
    active_gps.setOnPreferenceChangeListener(goToSettings);
    active_network.setOnPreferenceChangeListener(goToSettings);

    // Enable/Disable Wifi
    Preference active_wifi = (Preference) findPreference("active_wifi");
    active_wifi.setOnPreferenceClickListener(new OnPreferenceClickListener()
    {

      @Override
      public boolean onPreferenceClick(Preference preference)
      {
        CheckBoxPreference cb = (CheckBoxPreference) preference;
        WifiManager wifiManager = (WifiManager) PreferencesActivity.this.getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(cb.isChecked());
        return true;
      }
    });

    // Delete history
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
              Toast.makeText(PreferencesActivity.this, "A implementer !", Toast.LENGTH_SHORT)
                .show();
              return;
            }
          })
          .setNegativeButton(R.string.no, null)
          .show();
        return true;
      }
    });

    // Always enable telephony location
    // final CheckBoxPreference buyPref = (CheckBoxPreference) findPreference("active_telephony");
    // buyPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
    // {
    // @Override
    // public boolean onPreferenceChange(final Preference preference, final Object newValue)
    // {
    // Toast.makeText(PreferencesActivity.this, R.string.static_preference, Toast.LENGTH_SHORT)
    // .show();
    // // set condition true or false here according to your needs.
    // Editor edit = preference.getEditor();
    // edit.putBoolean("active_telephony", true);
    // edit.commit();
    // return false;
    // }
    // });
  }

  @Override
  public void onResume()
  {
    super.onResume();
    Common.onResume(this);

    // Display preference accordingly to current system state
    CheckBoxPreference active_wifi = (CheckBoxPreference) findPreference("active_wifi");
    CheckBoxPreference active_gps = (CheckBoxPreference) findPreference("active_gps");
    CheckBoxPreference active_network = (CheckBoxPreference) findPreference("active_network");
    WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
    LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

    boolean wifiChecked = wifiManager.isWifiEnabled()
      || wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING;
    boolean gpsChecked = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    boolean network_checked = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    Editor edit = active_wifi.getEditor();
    edit.putBoolean("active_gps", gpsChecked);
    edit.putBoolean("active_wifi", wifiChecked);
    edit.putBoolean("active_network", network_checked);
    edit.commit();

    active_wifi.setChecked(wifiChecked);
    active_gps.setChecked(gpsChecked);
    active_network.setChecked(network_checked);
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
    Intent intent;
    switch (item.getItemId())
    {
      case android.R.id.home:
        intent = new Intent(this, UrbanTagMainActivity.class);
        startActivity(intent);
        break;
    }
    return false;
  }

}
