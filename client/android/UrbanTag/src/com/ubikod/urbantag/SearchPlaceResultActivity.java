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

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;
import com.ubikod.urbantag.layout.FlowLayout;
import com.ubikod.urbantag.model.DatabaseHelper;
import com.ubikod.urbantag.model.Place;
import com.ubikod.urbantag.model.PlaceManager;
import com.ubikod.urbantag.model.Tag;

public class SearchPlaceResultActivity extends SherlockListActivity
{

  private LayoutInflater mInflater;
  private PlaceManager mPlaceManager;
  private DatabaseHelper mDbHelper;
  private List<Place> mPlaces = new ArrayList<Place>();

  private static class TagBundle
  {
    ViewHolder viewHolder;
    Place place;
  }

  private static class ViewHolder
  {
    TextView nameView;
    FlowLayout tagContainer;
  }

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setTitle(R.string.place);
    com.actionbarsherlock.app.ActionBar actionBar = this.getSupportActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);

    mInflater = LayoutInflater.from(this);
    mDbHelper = new DatabaseHelper(this, null);
    mPlaceManager = new PlaceManager(mDbHelper);

    Bundle extras = getIntent().getExtras();
    final int[] tagsId = extras.getIntArray("tagsId");

    mPlaces = mPlaceManager.getAllForTags(tagsId);

    BaseAdapter adapter = new BaseAdapter()
    {

      @Override
      public View getView(int position, View convertView, ViewGroup parent)
      {
        ViewHolder holder;
        TagBundle tagBundle;

        if (convertView == null)
        {
          convertView = mInflater.inflate(R.layout.place_row, null);

          holder = new ViewHolder();
          holder.nameView = (TextView) convertView.findViewById(R.id.name);
          holder.tagContainer = (FlowLayout) convertView.findViewById(R.id.tag_container);

          tagBundle = new TagBundle();
          tagBundle.viewHolder = holder;
          tagBundle.place = mPlaces.get(position);
          convertView.setTag(tagBundle);
        }
        else
        {
          tagBundle = (TagBundle) convertView.getTag();
        }

        tagBundle.viewHolder.nameView.setText(tagBundle.place.getName());

        for (Tag t : tagBundle.place.getAllTags())
        {
          TextView tag = new TextView(getApplicationContext());
          tag.setTextColor(Color.WHITE);
          tag.setBackgroundColor(t.getColor());
          tag.setText(t.getValue());
          tag.setPadding(5, 5, 5, 5);

          tagBundle.viewHolder.tagContainer.addView(tag, new FlowLayout.LayoutParams(10, 10));
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
        return mPlaces.get(position);
      }

      @Override
      public int getCount()
      {
        return mPlaces.size();
      }
    };

    setListAdapter(adapter);
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

  @Override
  public void onListItemClick(ListView l, View v, int position, long id)
  {
    Intent intent = new Intent(this, ContentsListActivity.class);
    intent.putExtra("placeId", ((TagBundle) v.getTag()).place.getId());
    startActivity(intent);
  }
}
