package com.ubikod.urbantag;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.ubikod.capptain.android.sdk.reach.CapptainReachDataPushReceiver;

public class DataPushHandler extends CapptainReachDataPushReceiver
{
  @Override
  protected Boolean onDataPushStringReceived(Context context, String body)
  {
    Log.d("tmp", "String data push message received: " + body);

    try
    {
      JSONObject json = new JSONObject(body);
      if (json.has("type"))
      {
        int type = json.getInt("type");
        switch (type)
        {
          case 0:
            break;
        }
      }
    }
    catch (JSONException e)
    {
      Log.e("JSON", "Exception :" + e.getMessage());
    }
    return true;
  }

  @Override
  protected Boolean onDataPushBase64Received(Context context, byte[] decodedBody, String encodedBody)
  {
    Log.d("tmp", "Base64 data push message received: " + encodedBody);
    // Do something useful with decodedBody like updating an image view
    return true;
  }
}
