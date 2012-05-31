package com.ubikod.urbantag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class TagManager implements OnClickListener
{
  private SQLiteDatabase DB;

  private DatabaseHelper dbHelper;

  private BaseAdapter adapter = null;

  private Context context;

  public TagManager(Context context)
  {
    this.context = context;
    dbHelper = new DatabaseHelper(context, null);
  }

  public void update(List<Tag> list)
  {
    HashMap<Integer, Tag> tags = loadAllFromDB();
    // Tags maj ou ajoutes
    for (Tag tag : list)
    {
      if (tags.containsKey(tag.getId()))
      {
        if (!tag.hasChanged(tags.get(tag.getId())))
        {
          update(tag);
        }
      }
      else
      {
        insert(tag);
      }
    }

    // Tags n'existant plus
    Vector<Tag> toDelete = new Vector<Tag>();
    for (Tag t : tags.values())
    {
      if (!list.contains(t))
      {
        Log.i("Deleting", t.getValue());
        toDelete.add(t);
      }
    }

    for (Tag t : toDelete)
    {
      delete(t);
    }
  }

  public void toggleNotification(Tag t)
  {
    t.setSelected(!t.isSelected());
    update(t);
  }

  public List<Tag> getAll()
  {
    return new ArrayList<Tag>(loadAllFromDB().values());
  }

  @Override
  public void onClick(View v)
  {
    Tag t = (Tag) v.getTag();
    t.setSelected(!t.isSelected());
    v.findViewById(R.id.color_bar).setBackgroundColor(
      t.isSelected() ? t.getColor() : R.color.bar_unselected);
    ((TextView) v.findViewById(R.id.label)).setTextColor(t.isSelected() ? context.getResources()
      .getColor(R.color.label_selected) : context.getResources().getColor(R.color.label_unselected));
    update(t);
  }

  public BaseAdapter getAdapter()
  {
    if (adapter == null)
      adapter = createAdapter(context, this);
    return adapter;
  }

  private BaseAdapter createAdapter(final Context context, final TagManager tagManager)
  {
    return new BaseAdapter()
    {
      List<Tag> tagsList = getAll();

      @Override
      public View getView(int position, View convertView, ViewGroup parent)
      {
        LayoutInflater inflater = LayoutInflater.from(context);

        convertView = inflater.inflate(R.layout.tag_row, null);

        Tag t = tagsList.get(position);
        convertView.setTag(t);
        convertView.setOnClickListener(tagManager);

        TextView label = (TextView) convertView.findViewById(R.id.label);

        TextView colorBar = (TextView) convertView.findViewById(R.id.color_bar);

        label.setText(t.getValue());

        colorBar.setBackgroundColor(t.isSelected() ? t.getColor() : R.color.bar_unselected);
        label.setTextColor(t.isSelected() ? context.getResources().getColor(R.color.label_selected)
          : context.getResources().getColor(R.color.label_unselected));

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

  private void update(Tag t)
  {
    open();
    dbUpdate(t);
    close();
  }

  private void insert(Tag t)
  {
    open();
    dbInsert(t);
    close();
  }

  private void delete(Tag t)
  {
    open();
    dbDelete(t);
    close();
  }

  private void open()
  {
    DB = dbHelper.getWritableDatabase();
  }

  private void close()
  {
    DB.close();
  }

  private void dbInsert(Tag t)
  {
    ContentValues values = new ContentValues();
    values.put(DatabaseHelper.COL_ID, t.getId());
    values.put(DatabaseHelper.COL_TAG_NAME, t.getValue());
    values.put(DatabaseHelper.COL_COLOR, t.getColor());
    values.put(DatabaseHelper.COL_NOTIFY, t.isSelected() ? 1 : 0);

    DB.insert(DatabaseHelper.TABLE_TAGS, DatabaseHelper.COL_ID, values);
  }

  private void dbUpdate(Tag t)
  {
    ContentValues values = new ContentValues();
    values.put(DatabaseHelper.COL_TAG_NAME, t.getValue());
    values.put(DatabaseHelper.COL_COLOR, t.getColor());
    values.put(DatabaseHelper.COL_NOTIFY, t.isSelected() ? 1 : 0);

    DB.update(DatabaseHelper.TABLE_TAGS, values, DatabaseHelper.COL_ID + " =? ",
      new String[] { String.valueOf(t.getId()) });
  }

  private Tag dbGetByID(int id)
  {
    Cursor c = DB.query(DatabaseHelper.TABLE_TAGS, new String[] { DatabaseHelper.COL_ID,
      DatabaseHelper.COL_TAG_NAME, DatabaseHelper.COL_COLOR, DatabaseHelper.COL_NOTIFY },
      DatabaseHelper.COL_ID + "=? ", new String[] { String.valueOf(id) }, null, null, null);
    c.moveToFirst();
    Tag t = cursorToTag(c);
    c.close();
    return t;
  }

  private void dbDelete(Tag t)
  {
    DB.delete(DatabaseHelper.TABLE_TAGS, DatabaseHelper.COL_ID + "=?",
      new String[] { String.valueOf(t.getId()) });
  }

  private HashMap<Integer, Tag> loadAllFromDB()
  {
    open();

    Cursor c = DB.query(DatabaseHelper.TABLE_TAGS, new String[] { DatabaseHelper.COL_ID,
      DatabaseHelper.COL_TAG_NAME, DatabaseHelper.COL_COLOR, DatabaseHelper.COL_NOTIFY }, null,
      null, null, null, null);
    HashMap<Integer, Tag> tags = new HashMap<Integer, Tag>();

    if (c.getCount() == 0)
    {
      c.close();
      close();
      return tags;
    }

    c.moveToFirst();
    do
    {
      Tag t = cursorToTag(c);
      tags.put(t.getId(), t);
    }
    while (c.moveToNext());

    c.close();
    close();
    return tags;
  }

  private Tag cursorToTag(Cursor c)
  {
    Tag t = new Tag(c.getInt(DatabaseHelper.NUM_COL_ID),
      c.getString(DatabaseHelper.NUM_COL_TAG_NAME), c.getInt(DatabaseHelper.NUM_COL_COLOR));
    t.setSelected(c.getInt(DatabaseHelper.NUM_COL_NOTIFY) == 1);
    return t;
  }

}
