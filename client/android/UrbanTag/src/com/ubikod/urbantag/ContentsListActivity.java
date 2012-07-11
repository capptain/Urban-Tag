package com.ubikod.urbantag;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;
import com.ubikod.urbantag.layout.FlowLayout;
import com.ubikod.urbantag.model.Content;
import com.ubikod.urbantag.model.ContentManager;
import com.ubikod.urbantag.model.DatabaseHelper;
import com.ubikod.urbantag.model.Place;
import com.ubikod.urbantag.model.PlaceManager;
import com.ubikod.urbantag.model.Tag;

public class ContentsListActivity extends SherlockListActivity
{

  /* The place currently viewed */
  private Place mPlace = null;

  /* Contents list for place */
  private List<Content> mContents = new ArrayList<Content>();

  private DatabaseHelper mDbHelper;

  private PlaceManager mPlaceManager;

  private ContentManager mContentManager;

  private LayoutInflater mInflater;

  private static class ViewHolder
  {
    FlowLayout tagContainer;
    TextView nameView;
  }

  private static class TagBundle
  {
    ViewHolder viewHolder;
    Content content;
  }

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    Bundle extras = getIntent().getExtras();

    mDbHelper = new DatabaseHelper(this, null);
    mPlaceManager = new PlaceManager(mDbHelper);
    mContentManager = new ContentManager(mDbHelper);
    mInflater = LayoutInflater.from(getApplicationContext());

    this.mPlace = mPlaceManager.get(extras.getInt("placeId"));

    if (this.mPlace != null)
    {
      setTitle(mPlace.getName());
      this.mContents = mContentManager.getAllForPlace(mPlace.getId());

      if (this.mContents.size() == 0)
      {
        Toast.makeText(this, R.string.no_contents_for_place, Toast.LENGTH_SHORT).show();
        finish();
      }
    }
    else
    {
      finish();
    }

    com.actionbarsherlock.app.ActionBar actionBar = this.getSupportActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);

    setListAdapter(createAdapter());
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

  private BaseAdapter createAdapter()
  {
    return new BaseAdapter()
    {

      @Override
      public View getView(int position, View convertView, ViewGroup parent)
      {
        ViewHolder holder;
        TagBundle tagBundle;

        Content c = mContents.get(position);

        if (convertView == null)
        {
          convertView = mInflater.inflate(R.layout.place_row, null);

          holder = new ViewHolder();
          holder.nameView = (TextView) convertView.findViewById(R.id.name);
          holder.tagContainer = (FlowLayout) convertView.findViewById(R.id.tag_container);

          tagBundle = new TagBundle();
          tagBundle.viewHolder = holder;

          convertView.setTag(tagBundle);
        }
        else
        {
          tagBundle = (TagBundle) convertView.getTag();
          holder = tagBundle.viewHolder;
        }

        tagBundle.content = c;
        holder.nameView.setText(c.getName());

        for (Tag t : c.getAllTags())
        {
          TextView tag = new TextView(getApplicationContext());
          tag.setTextColor(Color.WHITE);
          tag.setBackgroundColor(t.getColor());
          tag.setText(t.getValue());
          tag.setPadding(5, 5, 5, 5);
          holder.tagContainer.addView(tag, new FlowLayout.LayoutParams(10, 10));
        }

        return convertView;
      }

      @Override
      public long getItemId(int position)
      {
        return position;
      }

      @Override
      public Object getItem(int position)
      {
        return mContents.get(position);
      }

      @Override
      public int getCount()
      {
        return mContents.size();
      }
    };

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
  public void onListItemClick(ListView l, View v, int position, long id)
  {
    Intent intent = new Intent(this, ContentViewerActivity.class);
    intent.putExtra("contentId", ((TagBundle) v.getTag()).content.getId());
    startActivity(intent);
  }

}
