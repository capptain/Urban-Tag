package com.ubikod.urbantag;

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
import com.ubikod.urbantag.model.Content;
import com.ubikod.urbantag.model.ContentManager;
import com.ubikod.urbantag.model.DatabaseHelper;
import com.ubikod.urbantag.model.Place;
import com.ubikod.urbantag.model.PlaceManager;
import com.ubikod.urbantag.model.Tag;

public class ContentsListActivity extends SherlockListActivity
{

  private Place place = null;

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    Bundle extras = getIntent().getExtras();
    PlaceManager placeManager = new PlaceManager(new DatabaseHelper(this, null));
    this.place = placeManager.getById(extras.getInt("placeId"));
    setTitle(place.getName());
    com.actionbarsherlock.app.ActionBar actionBar = this.getSupportActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);
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
  public void onResume()
  {
    super.onResume();
    Common.onResume(this);

    if (place != null)
    {
      final int placeId = place.getId();
      final ContentManager contentManager = new ContentManager(new DatabaseHelper(this, null));
      final List<Content> contents = contentManager.getAllForPlace(placeId);

      if (contents.size() == 0)
      {
        Toast.makeText(this, R.string.no_contents_for_place, Toast.LENGTH_SHORT).show();
        finish();
      }
      BaseAdapter adapter = new BaseAdapter()
      {
        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
          LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
          Content c = contents.get(position);

          convertView = inflater.inflate(R.layout.place_row, null);
          TextView nameView = (TextView) convertView.findViewById(R.id.name);
          nameView.setText(c.getName());
          convertView.setTag(c);

          FlowLayout tagContainer = (FlowLayout) convertView.findViewById(R.id.tag_container);
          for (Tag t : c.getAllTags())
          {
            TextView tag = new TextView(getApplicationContext());
            tag.setTextColor(Color.WHITE);
            tag.setBackgroundColor(t.getColor());
            tag.setText(t.getValue());
            tag.setPadding(5, 5, 5, 5);
            tagContainer.addView(tag, new FlowLayout.LayoutParams(10, 10));
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
          return contents.get(position);
        }

        @Override
        public int getCount()
        {
          return contents.size();
        }
      };

      setListAdapter(adapter);
    }
    else
    {
      finish();
    }

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
    intent.putExtra("contentId", ((Content) v.getTag()).getId());
    startActivity(intent);
  }

}
