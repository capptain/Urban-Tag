package com.ubikod.urbantag;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockMapActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.ubikod.urbantag.model.DatabaseHelper;
import com.ubikod.urbantag.model.PlaceManager;

public class UrbanTagMainActivity extends SherlockMapActivity
{
  private static UrbanTagMainActivity instance;
  /* UI Elements */

  /** map view */
  private MapView mapView;

  /* Map related */
  /** Default zoom level */
  private int zoomLevel = 17;

  /** MapController */
  private MapController mapController;

  /** Location manager */
  private LocationManager locationManager;

  /** LocationOverlay */
  private MyLocationOverlay locationOverlay;

  /** PlaceOverlay */
  private PlaceOverlay placeOverlay;

  /** PlaceManager */
  private PlaceManager placeManager;

  private DatabaseHelper dbHelper;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    dbHelper = new DatabaseHelper(this, null);

    WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

    boolean wifiChecked = wifiManager.isWifiEnabled()
      || wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING;
    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
    pref.edit().putBoolean("notifiedWifi", wifiChecked).commit();

    placeOverlay = new PlaceOverlay(this, this.getResources().getDrawable(R.drawable.ic_launcher));
    placeManager = new PlaceManager(dbHelper);

    /* Initiate Map */
    mapView = (MapView) this.findViewById(R.id.mapView);
    mapView.setBuiltInZoomControls(true);
    mapController = mapView.getController();
    mapController.setZoom(zoomLevel);

    /* Set location manager */
    locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
    locationOverlay = new MyLocationOverlay(getApplicationContext(), mapView);
    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, locationOverlay);
    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0,
      locationOverlay);
    mapView.getOverlays().add(locationOverlay);
    mapView.getOverlays().add(placeOverlay);
    locationOverlay.enableMyLocation();
    locationOverlay.runOnFirstFix(new Runnable()
    {
      public void run()
      {
        GeoPoint curPosition = locationOverlay.getMyLocation();
        mapController.animateTo(curPosition);
        mapController.setCenter(curPosition);
      }
    });
  }

  public void onMapTap(ArrayList<Integer> placesId)
  {
    if (placesId.size() > 1)
    {
      Intent intent = new Intent(this, PlaceListActivity.class);
      int[] array = new int[placesId.size()];
      for (int i = 0; i < placesId.size(); i++)
      {
        array[i] = placesId.get(i);
      }
      intent.putExtra("placesId", array);
      startActivity(intent);
    }
    else if (placesId.size() == 1)
    {
      Intent intent = new Intent(this, ContentsListActivity.class);
      intent.putExtra("placeId", placesId.get(0));
      startActivity(intent);
    }

  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data)
  {
    switch (requestCode)
    {
      case PreferencesActivity.CODE:
        switch (resultCode)
        {
          case 1:
            placeOverlay.clear();
            mapView = (MapView) this.findViewById(R.id.mapView);
            mapView.invalidate();
            break;
        }

      case TagsListActivity.CODE:
        switch (resultCode)
        {
          case 1:
            placeOverlay.setPlaces(placeManager.getVisiblePlaces());
            break;
        }
    }
  }

  @Override
  public void onResume()
  {
    super.onResume();
    Common.onResume(this);
    instance = this;

    drawPlaces();
  }

  @Override
  public void onPause()
  {
    super.onPause();
    Common.onPause(this);

    instance = null;
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    MenuInflater menuInflater = new MenuInflater(this);
    menuInflater.inflate(R.menu.menu, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    Intent intent;
    switch (item.getItemId())
    {
      case android.R.id.home:
        Toast.makeText(this, "HOME", Toast.LENGTH_SHORT).show();
        break;

      case R.id.menu_tags:
        intent = new Intent(this, TagsListActivity.class);
        startActivityForResult(intent, TagsListActivity.CODE);
        break;

      case R.id.menu_preferences:
        intent = new Intent(this, PreferencesActivity.class);
        startActivityForResult(intent, PreferencesActivity.CODE);
        break;

      case R.id.menu_search:
        intent = new Intent(this, SearchActivity.class);
        startActivity(intent);
        break;

      case R.id.menu_quit:
        Toast.makeText(this, "Tapped share", Toast.LENGTH_SHORT).show();
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected boolean isRouteDisplayed()
  {
    // TODO Auto-generated method stub
    return false;
  }

  private void drawPlaces()
  {
    Log.i(UrbanTag.TAG, "drawing places");
    /* Show places */
    placeOverlay.clear();
    placeOverlay.setPlaces(placeManager.getVisiblePlaces());
  }

  public static void notifyNewPlace()
  {
    if (instance != null)
      instance.drawPlaces();
  }
}