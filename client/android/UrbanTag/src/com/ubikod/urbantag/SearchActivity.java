package com.ubikod.urbantag;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.ubikod.urbantag.layout.FlowLayout;
import com.ubikod.urbantag.model.DatabaseHelper;
import com.ubikod.urbantag.model.Tag;
import com.ubikod.urbantag.model.TagManager;

public class SearchActivity extends SherlockActivity
{
  private List<Tag> mAllTags = null;
  private List<Tag> mSelectedTags;
  private List<Tag> mSelectableTags;
  private Spinner mSpinner;
  private Button mBtnAddCategory, mBtnSubmit;
  private FlowLayout mTagContainer;
  private LinearLayout mBottomLayout;

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
    mSelectableTags = mAllTags;

    mSpinner = (Spinner) findViewById(R.id.spinner);
    mBtnAddCategory = (Button) findViewById(R.id.addCategory);
    mTagContainer = (FlowLayout) findViewById(R.id.tag_container);
    mBottomLayout = (LinearLayout) mBtnAddCategory.getParent();
    mBtnSubmit = (Button) findViewById(R.id.submit);

    mBtnAddCategory.setOnClickListener(new OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        Tag t = (Tag) mSpinner.getSelectedItem();
        mSelectedTags.add(t);
        mSelectableTags.remove(t);

        updateTagList();
      }
    });

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
          Intent intent = new Intent();

          if (placeButton.isChecked())
          {
            intent = new Intent(SearchActivity.this, SearchPlaceResultActivity.class);
          }
          else
          {
            intent = new Intent(SearchActivity.this, SearchContentResultActivity.class);
          }
          int[] array = new int[mSelectedTags.size()];
          for (int i = 0; i < mSelectedTags.size(); i++)
          {
            array[i] = mSelectedTags.get(i).getId();
          }

          intent.putExtra("tagsId", array);
          startActivity(intent);
        }
      }
    });

    updateTagList();

  }

  private void updateTagList()
  {
    mBottomLayout.removeView(mBtnAddCategory);
    mBottomLayout.removeView(mSpinner);

    if (mSelectableTags.size() > 0)
    {
      mBottomLayout.addView(mSpinner, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
        LayoutParams.WRAP_CONTENT));
      mBottomLayout.addView(mBtnAddCategory, new LinearLayout.LayoutParams(
        LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    }
    ArrayAdapter<Tag> dataAdapter = new ArrayAdapter<Tag>(this,
      android.R.layout.simple_spinner_item, mSelectableTags);
    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    mSpinner.setAdapter(dataAdapter);

    mTagContainer.removeAllViews();
    for (Tag t : mSelectedTags)
    {
      TextView tv = createViewTag(t);
      tv.setTag(t);

      tv.setOnClickListener(new OnClickListener()
      {
        @Override
        public void onClick(View v)
        {
          Tag t = (Tag) v.getTag();
          mSelectedTags.remove(t);
          mSelectableTags.add(t);
          updateTagList();

        }
      });

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
      mSelectableTags.remove(tagManager.get(selectedTagsArray[i]));
    }
    updateTagList();
  }
}
