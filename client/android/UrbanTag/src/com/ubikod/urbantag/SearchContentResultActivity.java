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
import com.ubikod.urbantag.model.Content;
import com.ubikod.urbantag.model.ContentManager;
import com.ubikod.urbantag.model.DatabaseHelper;
import com.ubikod.urbantag.model.Tag;

public class SearchContentResultActivity extends SherlockListActivity
{

  private DatabaseHelper mDbHelper;
  private ContentManager mContentManager;
  private LayoutInflater mInflater;

  private List<Content> mContents = new ArrayList<Content>();

  private static class ViewHolder
  {
    TextView nameView;
    TextView placeNameView;
    FlowLayout tagContainer;
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
    setTitle(R.string.content);
    com.actionbarsherlock.app.ActionBar actionBar = this.getSupportActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);

    Bundle extras = getIntent().getExtras();
    final int[] tagsId = extras.getIntArray("tagsId");

    mDbHelper = new DatabaseHelper(this, null);
    mContentManager = new ContentManager(mDbHelper);
    mInflater = LayoutInflater.from(getApplicationContext());

    mContents = mContentManager.getAllForTags(tagsId);

    setListAdapter(createAdapter());
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
          convertView = mInflater.inflate(R.layout.search_content_row, null);

          holder = new ViewHolder();
          holder.nameView = (TextView) convertView.findViewById(R.id.name);
          holder.placeNameView = (TextView) convertView.findViewById(R.id.place_name);
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
        holder.placeNameView.setText(c.getPlace().getName());

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
    intent.putExtra("contentId", ((TagBundle) v.getTag()).content.getId());
    startActivity(intent);
  }
}
