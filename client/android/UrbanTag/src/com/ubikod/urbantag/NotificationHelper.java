package com.ubikod.urbantag;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class NotificationHelper
{
  public static final String FROM_NOTIFICATION = "from_notification";
  private static final int NEW_CONTENT_NOTIF = 2;

  private static final int icon = android.R.drawable.ic_lock_idle_alarm;

  private Context mContext;
  private NotificationManager mNotificationManager;

  private static Notification sNewContentNotification = null;

  public NotificationHelper(Context context)
  {
    this.mContext = context;
    this.mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

  }

  public void notifyNewContent(int contentId)
  {
    Log.i(UrbanTag.TAG, "Notification : Received for contentId " + contentId);
    /* Get previous notifications stored */
    SharedPreferences prefs = mContext.getSharedPreferences("UrbanTag", Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = prefs.edit();
    String ContentsForNotification = prefs.getString("ContentsForNotification", "");
    StringTokenizer st = new StringTokenizer(ContentsForNotification, ",");
    List<Integer> contentIds = new ArrayList<Integer>();
    while (st.hasMoreTokens())
    {
      int value = Integer.parseInt(st.nextToken());
      contentIds.add(value);
    }

    /*
     * If not notified yet add it to list and store the list else quit method(we don't need to
     * update notification and to send a new one)
     */
    if (!contentIds.contains(contentId))
    {
      contentIds.add(contentId);
      ContentsForNotification += contentId + ",";
      editor.putString("ContentsForNotification", ContentsForNotification);
      editor.commit();
      Log.i(UrbanTag.TAG, "Notification : contentsIdList -> " + ContentsForNotification);
    }
    else
      return;

    /* Modify intent to transmit all data(all content ids we stocked) */
    Intent intent;
    if (contentIds.size() > 1)
    {
      Log.i(UrbanTag.TAG, "Notification : " + contentIds.size() + " contents to notify");
      int[] ids = new int[contentIds.size()];
      for (int i = 0; i < ids.length; i++)
      {
        ids[i] = contentIds.get(i);
      }
      intent = new Intent(mContext, ContentsListActivity.class);
      intent.putExtra(ContentsListActivity.MODE, ContentsListActivity.MODE_CONTENTS_LIST);
      intent.putExtra(ContentsListActivity.CONTENTS_IDS, ids);
    }
    else
    {
      Log.i(UrbanTag.TAG, "Notification : one content to notify " + contentIds.get(0));
      intent = new Intent(mContext, ContentViewerActivity.class);
      intent.putExtra(ContentViewerActivity.CONTENT_ID, contentIds.get(0));
    }
    /* Add the from notification extra info and create the pending intent */
    intent.putExtra(FROM_NOTIFICATION, true);
    PendingIntent activity = PendingIntent.getActivity(mContext, 0, intent,
      PendingIntent.FLAG_UPDATE_CURRENT);

    /* Create or update notification */
    if (sNewContentNotification == null)
    {
      sNewContentNotification = new NotificationCompat.Builder(mContext).setSmallIcon(icon)
        .setContentText(mContext.getResources().getString(R.string.new_content))
        .setContentTitle(mContext.getResources().getString(R.string.app_name))
        .setContentIntent(activity)
        .setTicker(
          mContext.getResources().getString(R.string.app_name) + " : "
            + mContext.getResources().getString(R.string.new_content))
        .getNotification();

      sNewContentNotification.flags |= Notification.FLAG_AUTO_CANCEL;
      sNewContentNotification.defaults |= Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND;
    }
    else
    {
      sNewContentNotification.contentIntent = activity;
    }

    /* Increment counter and notify */
    sNewContentNotification.number += 1;
    mNotificationManager.notify(NEW_CONTENT_NOTIF, sNewContentNotification);
  }

  public void closeContentNotif()
  {
    mNotificationManager.cancel(NEW_CONTENT_NOTIF);
    sNewContentNotification.number = 0;
    SharedPreferences prefs = mContext.getSharedPreferences("UrbanTag", Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = prefs.edit();
    editor.putString("ContentsForNotification", "");
    editor.commit();
  }

}
