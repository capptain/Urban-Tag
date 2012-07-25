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
import com.commonsware.android.listview.SectionedAdapter;
import com.ubikod.urbantag.layout.FlowLayout;
import com.ubikod.urbantag.model.Content;
import com.ubikod.urbantag.model.ContentManager;
import com.ubikod.urbantag.model.DatabaseHelper;
import com.ubikod.urbantag.model.Place;
import com.ubikod.urbantag.model.PlaceManager;
import com.ubikod.urbantag.model.Tag;

public class ContentsListActivity extends SherlockListActivity
{

  /**
   * Key for bundle extra in order to specify mode
   */
  public static final String MODE = "mode";

  /**
   * Display contents for a given place. Need to specify PLACE_ID int
   */
  public static final int MODE_PLACE = 0;

  /**
   * Display contents for given contents id. Need to specify CONTENTS_ID array
   */
  public static final int MODE_CONTENTS_LIST = 1;

  /**
   * Display contents for given tags. Need to specify TAGS_ID array
   */
  public static final int MODE_TAG_LIST = 2;

  /**
   * Key for bundle extra in order to specify display
   */
  public static final String DISPLAY = "display";

  /**
   * Display events and place descriptions
   */
  public static final int DISPLAY_ALL = 0;

  /**
   * Display only events
   */
  public static final int DISPLAY_ONLY_EVENT = 1;

  /**
   * Key for bundle extra for a place id
   */
  public static final String PLACE_ID = "place_id";

  /**
   * Key for bundle extra for several contents id
   */
  public static final String CONTENTS_IDS = "contents_ids";

  /**
   * Key for bundle extra for several tags id
   */
  public static final String TAGS_IDS = "tags_ids";

  /** List of contents */
  private List<Content> mContents = new ArrayList<Content>();

  /** DatabaseHelper */
  private DatabaseHelper mDbHelper;

  /** PlaceManager */
  private PlaceManager mPlaceManager;

  /** ContentManager */
  private ContentManager mContentManager;

  /** Layout inflater */
  private LayoutInflater mInflater;

  /** ViewHolder */
  private static class ViewHolder
  {
    FlowLayout tagContainer;
    TextView nameView;
  }

  /** TagBundle : Store data in view tag */
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
    com.actionbarsherlock.app.ActionBar actionBar = this.getSupportActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);

    mDbHelper = new DatabaseHelper(this, null);
    mPlaceManager = new PlaceManager(mDbHelper);
    mContentManager = new ContentManager(mDbHelper);
    mInflater = LayoutInflater.from(getApplicationContext());

    /* Handle MODE_PLACE => display all contents for this place */
    if (extras != null && extras.getInt(MODE, -1) == MODE_PLACE)
    {
      Place place = mPlaceManager.get(extras.getInt(PLACE_ID));

      if (place != null)
      {
        setTitle(place.getName());
        this.mContents = mContentManager.getAllForPlace(place.getId());

        if (this.mContents.size() == 0)
        {
          Toast.makeText(this, R.string.no_contents_for_place, Toast.LENGTH_SHORT).show();
          finish();
          return;
        }
      }
      else
      {
        Toast.makeText(this, R.string.error_occured, Toast.LENGTH_SHORT).show();
        finish();
        return;
      }
    }

    /* Handle MODE_CONTENTS_LIST => display selected contents */
    else if (extras != null && extras.getInt(MODE, -1) == MODE_CONTENTS_LIST)
    {
      setTitle(R.string.events);
      int[] ids;
      if ((ids = extras.getIntArray(CONTENTS_IDS)) != null)
      {
        this.mContents = mContentManager.get(ids);
        if (this.mContents.size() == 0)
        {
          Toast.makeText(this, R.string.error_occured, Toast.LENGTH_SHORT).show();
          finish();
          return;
        }
      }
      else
      {
        Toast.makeText(this, R.string.error_occured, Toast.LENGTH_SHORT).show();
        finish();
        return;
      }
    }

    /* Handle MODE_TAG_LIST => display contents with corresponding tags */
    else if (extras != null && extras.getInt(MODE, -1) == MODE_TAG_LIST)
    {
      setTitle(R.string.events);
      int[] ids;
      if ((ids = extras.getIntArray(TAGS_IDS)) != null)
      {
        this.mContents = mContentManager.getAllForTags(ids);
      }
      else
      {
        Toast.makeText(this, R.string.error_occured, Toast.LENGTH_SHORT).show();
        finish();
        return;
      }
    }
    else
    {
      Toast.makeText(this, R.string.error_occured, Toast.LENGTH_SHORT).show();
      finish();
      return;
    }

    /* Get place descriptions from all contents */
    List<Content> placesDescriptions = new ArrayList<Content>();
    List<Content> events = mContents;
    for (Content content : events)
    {
      if (content.getStartDate() == -1 || content.getEndDate() == -1)
      {
        placesDescriptions.add(content);
      }
    }

    /* Remove place descriptions from event list */
    for (Content content : placesDescriptions)
    {
      events.remove(content);
    }

    /* If we don't have description or event toast and finish activity */
    if (events.size() == 0 && placesDescriptions.size() == 0)
    {
      if (extras != null && extras.getInt(DISPLAY, -1) != DISPLAY_ONLY_EVENT)
        Toast.makeText(this, R.string.no_matching_content, Toast.LENGTH_SHORT).show();
      else
        Toast.makeText(this, R.string.no_matching_event, Toast.LENGTH_SHORT).show();
      finish();
      return;
    }

    /* Create sectioned adapter */
    SectionedAdapter adapter = new SectionedAdapter()
    {
      @Override
      protected View getHeaderView(String caption, int index, View convertView, ViewGroup parent)
      {
        /* If we don't have caption just return an empty view */
        if ((convertView == null || !(convertView instanceof TextView)) && caption != null)
        {
          convertView = new TextView(getApplicationContext());
          ((TextView) convertView).setTextColor(0xff000000);
          ((TextView) convertView).setPadding(
            (int) (15 * getResources().getDisplayMetrics().density + 0.5f), 0, 0, 0);
          ((TextView) convertView).setTextAppearance(ContentsListActivity.this,
            android.R.style.TextAppearance_Medium);
          ((TextView) convertView).setBackgroundColor(android.R.style.Widget_ActionBar);
        }
        else
        {
          convertView = new View(getApplicationContext());
        }

        if (caption != null)
          ((TextView) convertView).setText(caption);

        return convertView;
      }
    };

    /* Load place descriptions data in adapter */
    if (extras != null && extras.getInt(DISPLAY, -1) != DISPLAY_ONLY_EVENT
      && placesDescriptions.size() > 0)
    {
      if (placesDescriptions.size() == 1)
      {
        adapter.addSection(null, createAdapter(placesDescriptions, true));
      }
      else
      {
        adapter.addSection(getResources().getString(R.string.description),
          createAdapter(placesDescriptions, true));
      }
    }

    /* Load events in adapter */
    if (events.size() > 0)
    {
      adapter.addSection(getResources().getString(R.string.events), createAdapter(events));
    }

    setListAdapter(adapter);

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

  /**
   * Create a list adapter
   * @param contents Contents to display on list
   * @return
   */
  private BaseAdapter createAdapter(final List<Content> contents)
  {
    return createAdapter(contents, false);
  }

  /**
   * Create a list adapter
   * @param contents Contents to display on list
   * @param isDescription true if contents are description, false otherwise. If true and contents
   *          size is one replace content name by description
   * @return
   */
  private BaseAdapter createAdapter(final List<Content> contents, final boolean isDescription)
  {
    return new BaseAdapter()
    {

      @Override
      public View getView(int position, View convertView, ViewGroup parent)
      {
        ViewHolder holder;
        TagBundle tagBundle;

        Content c = contents.get(position);

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

        if (isDescription && contents.size() == 1)
        {
          holder.nameView.setText(R.string.description);
        }
        else
          holder.nameView.setText(c.getName());

        holder.tagContainer.removeAllViews();
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
        return contents.get(position);
      }

      @Override
      public int getCount()
      {
        return contents.size();
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
  /**
   * Handle click on a list item
   * Start contentViewerActivity for selected content
   */
  public void onListItemClick(ListView l, View v, int position, long id)
  {
    Intent intent = new Intent(this, ContentViewerActivity.class);
    intent.putExtra(ContentViewerActivity.CONTENT_ID, ((TagBundle) v.getTag()).content.getId());
    startActivity(intent);
  }

}
