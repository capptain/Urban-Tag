package com.ubikod.urbantag;

import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;
import com.ubikod.urbantag.model.DatabaseHelper;
import com.ubikod.urbantag.model.Tag;
import com.ubikod.urbantag.model.TagManager;

public class TagsListActivity extends SherlockListActivity implements OnClickListener
{
  public static final int CODE = 2;

  private DatabaseHelper dbHelper;
  private TagManager tagManager = null;

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setTitle(R.string.menu_tags);
    com.actionbarsherlock.app.ActionBar actionBar = this.getSupportActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);

    dbHelper = new DatabaseHelper(this, null);
    tagManager = new TagManager(dbHelper);

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
    Tag t = (Tag) v.getTag();
    // update in model
    tagManager.toggleNotification(t);
    // update screen
    v.findViewById(R.id.color_bar).setBackgroundColor(
      t.isSelected() ? t.getColor() : R.color.bar_unselected);
    ((TextView) v.findViewById(R.id.label)).setTextColor(t.isSelected() ? getResources().getColor(
      R.color.label_selected) : getResources().getColor(R.color.label_unselected));

    setResult(1);
  }

  private BaseAdapter createAdapter()
  {
    return new BaseAdapter()
    {
      List<Tag> tagsList = tagManager.getAll();

      @Override
      public View getView(int position, View convertView, ViewGroup parent)
      {
        LayoutInflater inflater = LayoutInflater.from(TagsListActivity.this);

        convertView = inflater.inflate(R.layout.tag_row, null);

        Tag t = tagsList.get(position);
        convertView.setTag(t);
        convertView.setOnClickListener(TagsListActivity.this);

        TextView label = (TextView) convertView.findViewById(R.id.label);

        TextView colorBar = (TextView) convertView.findViewById(R.id.color_bar);

        label.setText(t.getValue());

        colorBar.setBackgroundColor(t.isSelected() ? t.getColor() : R.color.bar_unselected);
        label.setTextColor(t.isSelected() ? getResources().getColor(R.color.label_selected)
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
