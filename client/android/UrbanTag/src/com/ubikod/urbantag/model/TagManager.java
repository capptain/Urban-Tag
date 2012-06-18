package com.ubikod.urbantag.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class TagManager
{
  private static SQLiteDatabase DB;

  private static DatabaseHelper dbHelper;

  private static Context context;

  private static HashMap<Integer, Tag> tags = new HashMap<Integer, Tag>();

  public TagManager(Context context)
  {
    context = context;
    dbHelper = new DatabaseHelper(context, null);
    tags = loadAllFromDB();
  }

  public void update(List<Tag> list)
  {
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
    return new ArrayList<Tag>(tags.values());
  }

  public Tag getByID(int id)
  {
    return tags.get(id);
  }

  public List<Tag> getAllForPlace(int idPlace)
  {
    return dbGetAllForPlace(idPlace);
  }

  private void update(Tag t)
  {
    tags.put(t.getId(), t);
    open();
    dbUpdate(t);
    close();
  }

  private void insert(Tag t)
  {
    tags.put(t.getId(), t);
    open();
    dbInsert(t);
    close();
  }

  private void delete(Tag t)
  {
    tags.remove(t.getId());
    open();
    dbDelete(t);
    close();
  }

  private void dbInsert(Tag t)
  {
    ContentValues values = new ContentValues();
    values.put(DatabaseHelper.TAG_COL_ID, t.getId());
    values.put(DatabaseHelper.TAG_COL_NAME, t.getValue());
    values.put(DatabaseHelper.TAG_COL_COLOR, t.getColor());
    values.put(DatabaseHelper.TAG_COL_NOTIFY, t.isSelected() ? 1 : 0);

    DB.insert(DatabaseHelper.TABLE_TAGS, DatabaseHelper.TAG_COL_ID, values);
  }

  private void dbUpdate(Tag t)
  {
    ContentValues values = new ContentValues();
    values.put(DatabaseHelper.TAG_COL_NAME, t.getValue());
    values.put(DatabaseHelper.TAG_COL_COLOR, t.getColor());
    values.put(DatabaseHelper.TAG_COL_NOTIFY, t.isSelected() ? 1 : 0);

    DB.update(DatabaseHelper.TABLE_TAGS, values, DatabaseHelper.TAG_COL_ID + " =? ",
      new String[] { String.valueOf(t.getId()) });
  }

  private Tag dbGetByID(int id)
  {
    Cursor c = DB.query(DatabaseHelper.TABLE_TAGS, new String[] { DatabaseHelper.TAG_COL_ID,
      DatabaseHelper.TAG_COL_NAME, DatabaseHelper.TAG_COL_COLOR, DatabaseHelper.TAG_COL_NOTIFY },
      DatabaseHelper.TAG_COL_ID + "=? ", new String[] { String.valueOf(id) }, null, null, null);

    if (c.getCount() == 0)
    {
      c.close();
      return null;
    }

    c.moveToFirst();
    Tag t = cursorToTag(c);
    c.close();
    return t;
  }

  private void dbDelete(Tag t)
  {
    DB.delete(DatabaseHelper.TABLE_TAGS, DatabaseHelper.TAG_COL_ID + "=?",
      new String[] { String.valueOf(t.getId()) });
  }

  private HashMap<Integer, Tag> loadAllFromDB()
  {
    open();

    Cursor c = DB.query(DatabaseHelper.TABLE_TAGS, new String[] { DatabaseHelper.TAG_COL_ID,
      DatabaseHelper.TAG_COL_NAME, DatabaseHelper.TAG_COL_COLOR, DatabaseHelper.TAG_COL_NOTIFY },
      null, null, null, null, null);
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

  private List<Tag> dbGetAllForPlace(int id)
  {
    open();
    String query = "SELECT " + DatabaseHelper.TAG_COL_ID + ", " + DatabaseHelper.TAG_COL_NAME
      + ", " + DatabaseHelper.TAG_COL_COLOR + ", " + DatabaseHelper.TAG_COL_NOTIFY + " FROM "
      + DatabaseHelper.TABLE_TAGS + " tags INNER JOIN " + DatabaseHelper.TABLE_PLACES_TAGS
      + " pivot ON tags." + DatabaseHelper.TAG_COL_ID + "=pivot."
      + DatabaseHelper.PLACE_TAG_COL_TAG + " WHERE pivot." + DatabaseHelper.PLACE_TAG_COL_PLACE
      + "=?";

    Cursor c = DB.rawQuery(query, new String[] { String.valueOf(id) });
    List<Tag> tags = new ArrayList<Tag>();

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
      tags.add(t);
    }
    while (c.moveToNext());

    c.close();
    close();
    return tags;
  }

  private void open()
  {
    DB = dbHelper.getWritableDatabase();
  }

  private void close()
  {
    DB.close();
  }

  private Tag cursorToTag(Cursor c)
  {
    Tag t = new Tag(c.getInt(DatabaseHelper.TAG_NUM_COL_ID),
      c.getString(DatabaseHelper.TAG_NUM_COL_NAME), c.getInt(DatabaseHelper.TAG_NUM_COL_COLOR));
    t.setSelected(c.getInt(DatabaseHelper.TAG_NUM_COL_NOTIFY) == 1);
    return t;
  }

}
