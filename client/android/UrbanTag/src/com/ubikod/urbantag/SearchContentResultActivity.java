package com.ubikod.urbantag;

import java.util.List;

import android.app.ActionBar.LayoutParams;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;
import com.ubikod.urbantag.model.Content;
import com.ubikod.urbantag.model.ContentManager;
import com.ubikod.urbantag.model.DatabaseHelper;
import com.ubikod.urbantag.model.Tag;

public class SearchContentResultActivity extends SherlockListActivity
{
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setTitle(R.string.content);
    com.actionbarsherlock.app.ActionBar actionBar = this.getSupportActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);

    Bundle extras = getIntent().getExtras();
    final int[] tagsId = extras.getIntArray("tagsId");
    ContentManager contentManager = new ContentManager(new DatabaseHelper(this, null));
    final List<Content> contents = contentManager.getAllForTags(tagsId);
    BaseAdapter adapter = new BaseAdapter()
    {

      @Override
      public View getView(int position, View convertView, ViewGroup parent)
      {
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        Content c = contents.get(position);

        convertView = inflater.inflate(R.layout.search_content_row, null);
        TextView nameView = (TextView) convertView.findViewById(R.id.name);
        TextView placeNameView = (TextView) convertView.findViewById(R.id.place_name);
        nameView.setText(c.getName());
        placeNameView.setText(c.getPlace().getName());
        convertView.setTag(c);

        FlowLayout tagContainer = (FlowLayout) convertView.findViewById(R.id.tag_container);
        for (Tag t : c.getAllTags())
        {
          TextView tag = new TextView(getApplicationContext());
          tag.setTextColor(Color.WHITE);
          tag.setBackgroundColor(t.getColor());
          tag.setText(t.getValue());
          tag.setPadding(5, 5, 5, 5);
          LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT);
          llp.setMargins(0, 0, 10, 0);
          tag.setLayoutParams(llp);
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
    Intent intent = new Intent(this, ContentViewerActivity.class);
    intent.putExtra("contentId", ((Content) v.getTag()).getId());
    startActivity(intent);
  }
}
