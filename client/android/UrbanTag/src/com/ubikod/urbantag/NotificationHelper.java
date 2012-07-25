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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * Class helper for notifications
 * @author cdesneuf
 */
public class NotificationHelper
{
  /**
   * Key for bundle extra specifying new activity is started from a click on a notification Type of
   * notification(NEW_CONTENT_NOTIF or NEW_PLACE_NOTIF) is associated to this key.
   */
  public static final String FROM_NOTIFICATION = "from_notification";

  /**
   * Notification key for a new content(new event).
   */
  public static final int NEW_CONTENT_NOTIF = 1;

  /**
   * Notification key for a new place description
   */
  public static final int NEW_PLACE_NOTIF = 2;

  /**
   * Icon for place notification
   */
  private static final int place_icon = android.R.drawable.ic_dialog_map;

  /**
   * Icon for event notification
   */
  private static final int content_icon = android.R.drawable.ic_lock_idle_alarm;

  /**
   * Context
   */
  private Context mContext;

  /**
   * Notification Manager
   */
  private NotificationManager mNotificationManager;

  /**
   * Cosntructor
   * @param context
   */
  public NotificationHelper(Context context)
  {
    this.mContext = context;
    this.mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

  }

  /**
   * Notify a new content(place or event)
   * @param contentId Id of content
   * @param content Content name
   * @param placeName Place name it will happend
   * @param type type of notification
   */
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
    intent.putExtra(FROM_NOTIFICATION, type);
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

  /**
   * Notify a new content(event)
   * @param contentId content id
   * @param contentTitle Event name
   * @param placeName Place it will happend
   */
  public void notifyNewContent(int contentId, String contentTitle, String placeName)
  {
    Log.i(UrbanTag.TAG, "Notification : Received for contentId " + contentId);

    notify(contentId, contentTitle, placeName, NEW_CONTENT_NOTIF);
  }

  /**
   * Notify a new place
   * @param contentId content id
   * @param placeName Place name
   */
  public void notifyNewPlace(int contentId, String placeName)
  {
    Log.i(UrbanTag.TAG, "Notification : Received for contentId " + contentId);

    notify(contentId, null, placeName, NEW_PLACE_NOTIF);
  }

  /**
   * Close new content(event) notification
   */
  public void closeContentNotif()
  {
    mNotificationManager.cancel(NEW_CONTENT_NOTIF);
  }

  /**
   * Close new place notification
   */
  public void closePlaceNotif()
  {
    mNotificationManager.cancel(NEW_PLACE_NOTIF);
  }

}
