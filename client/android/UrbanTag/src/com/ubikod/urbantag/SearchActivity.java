package com.ubikod.urbantag;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
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

public class SearchActivity extends SherlockActivity implements MultiSpinnerListener<Tag>
{
  private List<Tag> mAllTags = null;
  private List<Tag> mSelectedTags;
  private MultipleSelection<Tag> mSpinner;
  private Button mBtnSubmit;
  private FlowLayout mTagContainer;

  @SuppressWarnings("unchecked")
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.search_form);
    setTitle(R.string.menu_search);
    com.actionbarsherlock.app.ActionBar actionBar = this.getSupportActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);
    TagManager tagManager = new TagManager(new DatabaseHelper(this, null));
    mAllTags = tagManager.getAll();
    mSelectedTags = new ArrayList<Tag>();

    mSpinner = (MultipleSelection<Tag>) findViewById(R.id.spinner);
    mTagContainer = (FlowLayout) findViewById(R.id.tag_container);
    mBtnSubmit = (Button) findViewById(R.id.submit);

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
          int[] array = new int[mSelectedTags.size()];
          for (int i = 0; i < mSelectedTags.size(); i++)
          {
            array[i] = mSelectedTags.get(i).getId();
          }

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
          }

          startActivity(intent);
        }
      }
    });

    updateTagList();

  }

  private void updateTagList()
  {
    boolean[] sel = new boolean[mAllTags.size()];
    for (int i = 0; i < sel.length; i++)
    {
      sel[i] = mSelectedTags.contains(mAllTags.get(i));
      Log.i("isSelected", sel[i] + " " + i + " " + mAllTags.get(i).getValue());
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
