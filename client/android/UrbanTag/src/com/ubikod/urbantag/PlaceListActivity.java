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

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;
import com.ubikod.urbantag.model.DatabaseHelper;
import com.ubikod.urbantag.model.Place;
import com.ubikod.urbantag.model.PlaceManager;
import com.ubikod.urbantag.model.Tag;

public class PlaceListActivity extends SherlockListActivity
{

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setTitle(R.string.menu_select_place);
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
    Bundle extras = getIntent().getExtras();
    final int[] placesId = extras.getIntArray("placesId");
    final PlaceManager placeManager = new PlaceManager(new DatabaseHelper(getApplicationContext(),
      null));
    BaseAdapter adapter = new BaseAdapter()
    {
      List<Place> places = placeManager.get(placesId);

      @Override
      public View getView(int position, View convertView, ViewGroup parent)
      {
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        Place p = places.get(position);

        convertView = inflater.inflate(R.layout.place_row, null);
        TextView nameView = (TextView) convertView.findViewById(R.id.name);
        nameView.setText(p.getName());
        convertView.setTag(p);

        FlowLayout tagContainer = (FlowLayout) convertView.findViewById(R.id.tag_container);
        for (Tag t : p.getAllTags())
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
        return places.get(position);
      }

      @Override
      public int getCount()
      {
        return places.size();
      }
    };

    setListAdapter(adapter);

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

    Intent intent = new Intent(this, ContentsListActivity.class);
    intent.putExtra("placeId", ((Place) v.getTag()).getId());
    startActivity(intent);
  }
}
