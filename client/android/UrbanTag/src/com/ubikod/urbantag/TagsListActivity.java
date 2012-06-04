package com.ubikod.urbantag;

import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;

public class TagsListActivity extends SherlockListActivity
{
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setTitle(R.string.menu_tags);
    com.actionbarsherlock.app.ActionBar actionBar = this.getSupportActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);

    TagManager tagManager = new TagManager(this);

    setListAdapter(tagManager.getAdapter());

  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    Intent intent;
    switch (item.getItemId())
    {
      case android.R.id.home:
        intent = new Intent(this, UrbanTagMainActivity.class);
        startActivity(intent);
        break;
    }
    return false;
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
}
