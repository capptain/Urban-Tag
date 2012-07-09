package com.ubikod.urbantag;

import android.os.Bundle;
import android.webkit.WebView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.ubikod.urbantag.model.Content;
import com.ubikod.urbantag.model.ContentManager;
import com.ubikod.urbantag.model.DatabaseHelper;

public class ContentViewerActivity extends SherlockActivity
{
  private Content content = null;

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    Bundle extras = getIntent().getExtras();
    ContentManager contentManager = new ContentManager(new DatabaseHelper(this, null));
    int bla = extras.getInt("contentId");
    this.content = contentManager.get(extras.getInt("contentId"));
    setTitle(content.getName());
    com.actionbarsherlock.app.ActionBar actionBar = this.getSupportActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);

    setContentView(R.layout.content_viewer);
    WebView wvSite = (WebView) findViewById(R.id.webview);
    wvSite.loadUrl("http://www.test.fr");
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
