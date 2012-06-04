package com.ubikod.urbantag;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
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
import com.google.android.maps.OverlayItem;

public class UrbanTagMainActivity extends SherlockMapActivity
{
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

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    TagManager tagManager = new TagManager(this);

    List<Tag> tagsList = new ArrayList<Tag>();
    tagsList.add(new Tag(0, "Musique", 0xff79D438));
    tagsList.add(new Tag(1, "Architecture", 0xffF86883));
    tagsList.add(new Tag(2, "Insolite", 0xff56B2E1));
    tagsList.add(new Tag(3, "Bla", 0xffF9D424));

    tagManager.update(tagsList);

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
    locationOverlay.enableMyLocation();
    locationOverlay.runOnFirstFix(new Runnable()
    {
      public void run()
      {
        GeoPoint curPosition = locationOverlay.getMyLocation();
        Log.i("Position", "" + curPosition);
        mapController.animateTo(curPosition);
        mapController.setCenter(curPosition);
      }
    });

    placeOverlay = new PlaceOverlay(this, this.getResources().getDrawable(R.drawable.ic_launcher));
    mapView.getOverlays().add(placeOverlay);
    OverlayItem ubu = new OverlayItem(new GeoPoint(48107976, -1673387), "Ubu", "Concert"), tnb = new OverlayItem(
      new GeoPoint(48107789, -1672701), "TNB", "Thêatre");
    ubu.setMarker(new TextDrawable("Ubu"));
    tnb.setMarker(new TextDrawable("TNB"));
    placeOverlay.addOverlay(ubu);
    placeOverlay.addOverlay(tnb);

  }

  @Override
  public void onResume()
  {
    super.onResume();
    Common.onResume(this);
  }

  @Override
  public void onPause()
  {
    super.onPause();
    Common.onPause(this);
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
        startActivity(intent);
        break;

      case R.id.menu_preferences:
        intent = new Intent(this, PreferencesActivity.class);
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

}