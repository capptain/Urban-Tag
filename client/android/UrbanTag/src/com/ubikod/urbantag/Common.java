package com.ubikod.urbantag;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;

public class Common
{

  public static void onResume(final Context context)
  {
    LocationManager locationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
    if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
    {
      new AlertDialog.Builder(context).setTitle(R.string.activate_geoloc)
        .setMessage(context.getResources().getString(R.string.activate_geoloc_confirm))
        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
        {

          @Override
          public void onClick(DialogInterface dialog, int which)
          {
            context.startActivity(new Intent(
              android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
          }
        })
        .show();
    }
  }
}
