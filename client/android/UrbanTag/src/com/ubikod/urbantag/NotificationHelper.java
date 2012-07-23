package com.ubikod.urbantag;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class NotificationHelper
{
  public static final String FROM_NOTIFICATION = "from_notification";
  private static final int NEW_CONTENT_NOTIF = 1;
  private static final int NEW_PLACE_NOTIF = 2;

  private static final int place_icon = android.R.drawable.ic_dialog_map;
  private static final int content_icon = android.R.drawable.ic_lock_idle_alarm;

  private Context mContext;
  private NotificationManager mNotificationManager;

  public NotificationHelper(Context context)
  {
    this.mContext = context;
    this.mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

  }

  private void notify(int contentId, String content, String placeName, int type)
  {

    String title = content;
    String text = placeName;
    int icon = content_icon;
    if (type == NEW_PLACE_NOTIF)
    {
      title = placeName;
      text = mContext.getResources().getString(R.string.new_place);
      icon = place_icon;
    }
    /* Modify intent to transmit id */
    Intent intent;
    intent = new Intent(mContext, ContentViewerActivity.class);
    intent.putExtra(ContentViewerActivity.CONTENT_ID, contentId);

    /* Add the from notification extra info and create the pending intent */
    intent.putExtra(FROM_NOTIFICATION, true);
    PendingIntent activity = PendingIntent.getActivity(mContext, 0, intent,
      PendingIntent.FLAG_UPDATE_CURRENT);

    /* Create notification */
    Notification newNotification = new NotificationCompat.Builder(mContext).setSmallIcon(icon)
      .setContentText(text)
      .setContentTitle(title)
      .setContentIntent(activity)
      .setTicker(title)
      .getNotification();

    // newNotification.contentView = contentView;
    newNotification.flags |= Notification.FLAG_AUTO_CANCEL;
    newNotification.defaults |= Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND;

    /* notify */
    mNotificationManager.notify(type, newNotification);
  }

  public void notifyNewContent(int contentId, String contentTitle, String placeName)
  {
    Log.i(UrbanTag.TAG, "Notification : Received for contentId " + contentId);

    notify(contentId, contentTitle, placeName, NEW_CONTENT_NOTIF);
  }

  public void notifyNewPlace(int contentId, String placeName)
  {
    Log.i(UrbanTag.TAG, "Notification : Received for contentId " + contentId);

    notify(contentId, null, placeName, NEW_PLACE_NOTIF);
  }

  public void closeContentNotif()
  {
    mNotificationManager.cancel(NEW_CONTENT_NOTIF);
  }

  public void closePlaceNotif()
  {
    mNotificationManager.cancel(NEW_PLACE_NOTIF);
  }

}
