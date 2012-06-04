package com.ubikod.urbantag;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;

import com.ubikod.capptain.android.sdk.CapptainAgent;
import com.ubikod.capptain.android.sdk.CapptainAgentUtils;

public class Common
{

  public static void onResume(final Activity activity)
  {
    String activityNameOnCapptain = CapptainAgentUtils.buildCapptainActivityName(activity.getClass());
    CapptainAgent.getInstance(activity).startActivity(activity, activityNameOnCapptain, null);
    LocationManager locationManager = (LocationManager) activity.getSystemService(activity.LOCATION_SERVICE);
    if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
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
        .show();
    }
  }

  public static void onPause(final Activity activity)
  {
    CapptainAgent.getInstance(activity).endActivity();
  }
}
