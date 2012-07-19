package com.ubikod.urbantag;

import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.ubikod.urbantag.model.Content;
import com.ubikod.urbantag.model.ContentManager;
import com.ubikod.urbantag.model.DatabaseHelper;

public class ContentViewerActivity extends SherlockActivity
{
  public static final String CONTENT_ID = "contentid";
  private Content content = null;
  private static final String ACTION_GET_CONTENT = "info/%/content";

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    Bundle extras = getIntent().getExtras();
    if (extras.getBoolean(NotificationHelper.FROM_NOTIFICATION, false))
    {
      NotificationHelper notificationHelper = new NotificationHelper(this);
      notificationHelper.closeContentNotif();
    }
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
    WebView wvSite = (WebView) findViewById(R.id.webview);
    wvSite.loadUrl(UrbanTag.API_URL + ACTION_GET_CONTENT.replaceAll("%", "" + this.content.getId()));
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
