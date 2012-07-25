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

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.ubikod.urbantag.model.Content;
import com.ubikod.urbantag.model.ContentManager;
import com.ubikod.urbantag.model.DatabaseHelper;

/**
 * Activity displaying content on webview
 * @author cdesneuf
 */
public class ContentViewerActivity extends SherlockActivity
{
  /**
   * Action we have to perform on API to fetch webview content
   */
  private static final String ACTION_GET_CONTENT = "info/%/content";

  /**
   * mimeType of webview content
   */
  private final String mimeType = "text/html";

  /**
   * Webview content encoding
   */
  private final String encoding = "UTF-8";

  /**
   * Maximal time for webview content loading
   */
  private static final int TIMEOUT = 60000;

  /**
   * Key for bundle extra to sepcify content we want to display
   */
  public static final String CONTENT_ID = "contentid";

  /**
   * Thread handling timeOut
   */
  private Thread timeOutHandler;

  /**
   * Thread fetching webview content
   */
  private Thread contentFetcher;

  /**
   * Content we want to display
   */
  private Content content = null;

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    Bundle extras = getIntent().getExtras();

    /* If we are coming from Notification delete notification */
    if (extras.getInt(NotificationHelper.FROM_NOTIFICATION, -1) == NotificationHelper.NEW_CONTENT_NOTIF)
    {
      NotificationHelper notificationHelper = new NotificationHelper(this);
      notificationHelper.closeContentNotif();
    }
    else if (extras.getInt(NotificationHelper.FROM_NOTIFICATION, -1) == NotificationHelper.NEW_PLACE_NOTIF)
    {
      NotificationHelper notificationHelper = new NotificationHelper(this);
      notificationHelper.closePlaceNotif();
    }

    /* Fetch content info */
    ContentManager contentManager = new ContentManager(new DatabaseHelper(this, null));
    Log.i(UrbanTag.TAG, "View content : " + extras.getInt(CONTENT_ID));
    this.content = contentManager.get(extras.getInt(CONTENT_ID));
    if (this.content == null)
    {
      Toast.makeText(this, R.string.error_occured, Toast.LENGTH_SHORT).show();
      finish();
      return;
    }
    setTitle(content.getName());
    com.actionbarsherlock.app.ActionBar actionBar = this.getSupportActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);
    setContentView(R.layout.content_viewer);

    /* Find webview and create url for content */
    final WebView webView = (WebView) findViewById(R.id.webview);
    final String URL = UrbanTag.API_URL
      + ACTION_GET_CONTENT.replaceAll("%", "" + this.content.getId());

    /* Display progress animation */
    final ProgressDialog progress = ProgressDialog.show(this, "",
      this.getResources().getString(R.string.loading_content), false, true,
      new DialogInterface.OnCancelListener()
      {

        @Override
        public void onCancel(DialogInterface dialog)
        {
          timeOutHandler.interrupt();
          webView.stopLoading();
          ContentViewerActivity.this.finish();
        }

      });

    /* Go fetch content */
    contentFetcher = new Thread(new Runnable()
    {

      DefaultHttpClient httpClient;

      @Override
      public void run()
      {
        Looper.prepare();
        Log.i(UrbanTag.TAG, "Fetching content...");
        httpClient = new DefaultHttpClient();
        try
        {
          String responseBody = httpClient.execute(new HttpGet(URL), new BasicResponseHandler());
          webView.loadDataWithBaseURL("fake://url/for/encoding/hack...", responseBody, mimeType,
            encoding, "");
          timeOutHandler.interrupt();
          if (progress.isShowing())
            progress.dismiss();
        }
        catch (ClientProtocolException cpe)
        {
          new Handler().post(new Runnable()
          {
            @Override
            public void run()
            {
              Toast.makeText(getApplicationContext(), R.string.error_loading_content,
                Toast.LENGTH_SHORT).show();
            }
          });
          timeOutHandler.interrupt();
          progress.cancel();
        }
        catch (IOException ioe)
        {
          new Handler().post(new Runnable()
          {
            @Override
            public void run()
            {
              Toast.makeText(getApplicationContext(), R.string.error_loading_content,
                Toast.LENGTH_SHORT).show();
            }
          });
          timeOutHandler.interrupt();
          progress.cancel();
        }

        Looper.loop();
      }

    });
    contentFetcher.start();

    /* TimeOut Handler */
    timeOutHandler = new Thread(new Runnable()
    {
      private int INCREMENT = 1000;

      @Override
      public void run()
      {
        Looper.prepare();
        try
        {
          for (int time = 0; time < TIMEOUT; time += INCREMENT)
          {
            Thread.sleep(INCREMENT);
          }

          Log.w(UrbanTag.TAG, "TimeOut !");
          new Handler().post(new Runnable()
          {
            @Override
            public void run()
            {
              Toast.makeText(getApplicationContext(), R.string.error_loading_content,
                Toast.LENGTH_SHORT).show();
            }
          });

          contentFetcher.interrupt();
          progress.cancel();
        }
        catch (InterruptedException e)
        {
          e.printStackTrace();
        }

        Looper.loop();

      }

    });
    timeOutHandler.start();

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
  public boolean onOptionsItemSelected(MenuItem item)
  {
    switch (item.getItemId())
    {
      case android.R.id.home:
        finish();
        break;
    }
    return false;
  }

}
