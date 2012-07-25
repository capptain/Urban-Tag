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

import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.ubikod.urbantag.model.DatabaseHelper;
import com.ubikod.urbantag.model.Tag;
import com.ubikod.urbantag.model.TagManager;

/**
 * Activity displaying tags for selection
 * @author cdesneuf
 */
public class TagsListActivity extends SherlockActivity implements OnClickListener
{
  public static final int CODE = 2;

  /**
   * Database helper
   */
  private DatabaseHelper mDbHelper;

  /**
   * Tag manager
   */
  private TagManager mTagManager = null;

  /**
   * Layout inflater
   */
  private LayoutInflater mInflater;

  /**
   * Listview on which we display tags
   */
  private ListView tagsList;

  /**
   * View holder
   * @author cdesneuf
   */
  private static class ViewHolder
  {
    TextView label;
    TextView colorBar;
  }

  /**
   * Tag bundle
   */
  private static class TagBundle
  {
    Tag tag;
    ViewHolder viewHolder;
  }

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.tags_list);
    setTitle(R.string.menu_tags);
    com.actionbarsherlock.app.ActionBar actionBar = this.getSupportActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);

    mDbHelper = new DatabaseHelper(this, null);
    mTagManager = new TagManager(mDbHelper);
    mInflater = LayoutInflater.from(this);

    tagsList = (ListView) this.findViewById(R.id.tagsList);

    tagsList.setAdapter(createAdapter());

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
  }

  @Override
  public void onPause()
  {
    super.onPause();
    Common.onPause(this);
  }

  @Override
  public void onClick(View v)
  {
    /* update in model */
    Tag t = ((TagBundle) v.getTag()).tag;
    mTagManager.toggleNotification(t);

    /* update screen */
    v.findViewById(R.id.color_bar).setBackgroundColor(
      t.isSelected() ? t.getColor() : R.color.bar_unselected);
    ((TextView) v.findViewById(R.id.label)).setTextColor(t.isSelected() ? getResources().getColor(
      R.color.label_selected) : getResources().getColor(R.color.label_unselected));

    /* Notify changes to map view */
    setResult(1);
  }

  /**
   * Create adapter for tag list
   * @return
   */
  private BaseAdapter createAdapter()
  {
    return new BaseAdapter()
    {
      List<Tag> tagsList = mTagManager.getAll();

      @Override
      public View getView(int position, View convertView, ViewGroup parent)
      {
        ViewHolder holder;
        TagBundle tagBundle;

        Tag t = tagsList.get(position);

        if (convertView == null)
        {
          convertView = mInflater.inflate(R.layout.tag_row, null);

          convertView.setOnClickListener(TagsListActivity.this);

          holder = new ViewHolder();
          holder.colorBar = (TextView) convertView.findViewById(R.id.color_bar);
          holder.label = (TextView) convertView.findViewById(R.id.label);

          tagBundle = new TagBundle();
          tagBundle.viewHolder = holder;

          convertView.setTag(tagBundle);
        }
        else
        {
          tagBundle = ((TagBundle) convertView.getTag());
          holder = tagBundle.viewHolder;
        }

        tagBundle.tag = t;
        holder.label.setText(t.getValue());
        holder.colorBar.setBackgroundColor(t.isSelected() ? t.getColor() : R.color.bar_unselected);
        holder.label.setTextColor(t.isSelected() ? getResources().getColor(R.color.label_selected)
          : getResources().getColor(R.color.label_unselected));

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
        return tagsList.get(position);
      }

      @Override
      public int getCount()
      {
        return tagsList.size();
      }
    };
  }
}
