package com.ubikod.urbantag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.ubikod.urbantag.layout.FlowLayout;
import com.ubikod.urbantag.layout.MultipleSelection;
import com.ubikod.urbantag.layout.MultipleSelection.MultiSpinnerListener;
import com.ubikod.urbantag.model.DatabaseHelper;
import com.ubikod.urbantag.model.Tag;
import com.ubikod.urbantag.model.TagManager;

/**
 * Search activity
 * @author cdesneuf
 */
public class SearchActivity extends SherlockActivity implements MultiSpinnerListener<Tag>
{
  /**
   * All tags known
   */
  private List<Tag> mAllTags = null;

  /**
   * Selected tags for search
   */
  private List<Tag> mSelectedTags;

  /**
   * Tag selection spinner
   */
  private MultipleSelection<Tag> mSpinner;

  /**
   * Submit button
   */
  private Button mBtnSubmit;

  /**
   * Tag container. We display selected tags in this view.
   */
  private FlowLayout mTagContainer;

  /**
   * Shared preferences
   */
  private SharedPreferences mPrefs;

  /**
   * Shared preferences editor
   */
  private SharedPreferences.Editor mEditor;

  @SuppressWarnings("unchecked")
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    /* Initiate activity */
    super.onCreate(savedInstanceState);
    setContentView(R.layout.search_form);
    setTitle(R.string.menu_search);
    com.actionbarsherlock.app.ActionBar actionBar = this.getSupportActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);
    TagManager tagManager = new TagManager(new DatabaseHelper(this, null));
    mAllTags = tagManager.getAll();
    mSelectedTags = new ArrayList<Tag>();
    mPrefs = getSharedPreferences("UrbanTag", Context.MODE_PRIVATE);
    mEditor = mPrefs.edit();

    /* Get selectedTags from previous search */
    String previousSearch = mPrefs.getString("PreviousSearch", "");
    StringTokenizer st = new StringTokenizer(previousSearch, ",");
    HashMap<Integer, Tag> allTags = tagManager.getAllAsHashMap();
    while (st.hasMoreTokens())
    {
      int value = Integer.parseInt(st.nextToken());
      Tag t = allTags.get(value);
      if (t != null)
        mSelectedTags.add(t);
    }

    /* Find views */
    mSpinner = (MultipleSelection<Tag>) findViewById(R.id.spinner);
    mTagContainer = (FlowLayout) findViewById(R.id.tag_container);
    mBtnSubmit = (Button) findViewById(R.id.submit);

    /* Logic when submit button clicked */
    mBtnSubmit.setOnClickListener(new OnClickListener()
    {

      @Override
      public void onClick(View v)
      {
        RadioButton placeButton = (RadioButton) findViewById(R.id.place);
        if (mSelectedTags.size() == 0)
        {
          Toast.makeText(getApplicationContext(), R.string.category_needed, Toast.LENGTH_LONG)
            .show();
        }
        else
        {
          /* Create an int array with selected tag ids */
          int[] array = new int[mSelectedTags.size()];
          for (int i = 0; i < mSelectedTags.size(); i++)
          {
            array[i] = mSelectedTags.get(i).getId();
          }

          /* Create intent accordingly to search type(event or place) */
          Intent intent = new Intent();
          if (placeButton.isChecked())
          {
            intent = new Intent(SearchActivity.this, PlaceListActivity.class);
            intent.putExtra(PlaceListActivity.MODE, PlaceListActivity.MODE_TAGS_IDS);
            intent.putExtra(PlaceListActivity.TAGS_IDS, array);
          }
          else
          {
            intent = new Intent(SearchActivity.this, ContentsListActivity.class);
            intent.putExtra(ContentsListActivity.MODE, ContentsListActivity.MODE_TAG_LIST);
            intent.putExtra(ContentsListActivity.TAGS_IDS, array);
            /* Specify we only want event and no description */
            intent.putExtra(ContentsListActivity.DISPLAY, ContentsListActivity.DISPLAY_ONLY_EVENT);
          }
          startActivity(intent);
        }
      }
    });

    updateTagList();

  }

  /**
   * Update tag list. Communicate selected tags to spinner and tagcontainer View
   */
  private void updateTagList()
  {
    boolean[] sel = new boolean[mAllTags.size()];
    for (int i = 0; i < sel.length; i++)
    {
      sel[i] = mSelectedTags.contains(mAllTags.get(i));
    }

    mSpinner.setItems(mAllTags, sel, getResources().getString(R.string.select_tags), this);

    mTagContainer.removeAllViews();
    for (Tag t : mSelectedTags)
    {
      TextView tv = createViewTag(t);
      tv.setTag(t);

      mTagContainer.addView(tv, new FlowLayout.LayoutParams(10, 10));
    }
  }

  /**
   * Create a text view for tag
   * @param t
   * @return
   */
  private TextView createViewTag(Tag t)
  {
    TextView tag = new TextView(getApplicationContext());
    tag.setTextColor(Color.WHITE);
    tag.setBackgroundColor(t.getColor());
    tag.setText(t.getValue());
    tag.setTypeface(null, Typeface.BOLD);
    tag.setPadding(5, 5, 5, 5);
    tag.setGravity(Gravity.CENTER);
    LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
      LayoutParams.MATCH_PARENT);
    llp.setMargins(0, 0, 0, 10);
    tag.setLayoutParams(llp);
    return tag;
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
  public void onStop()
  {
    super.onStop();
    /* Store selected tag for next search */
    String selectedTags = "";
    for (Tag t : mSelectedTags)
    {
      selectedTags += t.getId() + ",";
    }
    mEditor.putString("PreviousSearch", selectedTags);
    mEditor.commit();

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
  public void onSaveInstanceState(Bundle outState)
  {
    /* Remember selected tags when rotating screen */
    int[] array = new int[mSelectedTags.size()];
    int i = 0;

    for (Tag t : mSelectedTags)
    {
      array[i] = t.getId();
      i++;
    }
    outState.putIntArray("selectedTags", array);
    super.onSaveInstanceState(outState);
  }

  @Override
  public void onRestoreInstanceState(Bundle savedInstanceState)
  {
    /* Get selected tags after a rotation */
    super.onRestoreInstanceState(savedInstanceState);
    int[] selectedTagsArray = savedInstanceState.getIntArray("selectedTags");
    TagManager tagManager = new TagManager(new DatabaseHelper(this, null));
    for (int i = 0; i < selectedTagsArray.length; i++)
    {
      mSelectedTags.add(tagManager.get(selectedTagsArray[i]));
    }
    updateTagList();
  }

  @Override
  public void onItemsSelected(List<Tag> selectedItems)
  {
    mSelectedTags = selectedItems;
    updateTagList();
  }
}
