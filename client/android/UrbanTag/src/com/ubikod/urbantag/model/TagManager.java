package com.ubikod.urbantag.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class TagManager
{
  private static SQLiteDatabase DB;

  private static DatabaseHelper dbHelper;

  private static HashMap<Integer, Tag> tags = new HashMap<Integer, Tag>();

  public TagManager(DatabaseHelper databaseHelper)
  {
    dbHelper = databaseHelper;
  }

  public void renewList()
  {

  }

  public void update(List<Tag> list)
  {
    // Tags maj ou ajoutes
    for (Tag tag : list)
    {
      this.save(tag);
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
      this.supress(t);
    }
  }

  public void toggleNotification(Tag t)
  {
    t.setSelected(!t.isSelected());
    this.alter(t);
  }

  public Tag get(int id)
  {
    return this.get(id, true);
  }

  private Tag get(int id, boolean first)
  {
    if (tags.containsKey(id))
    {
      return tags.get(id);
    }
    else
    {
      if (!first)
        return null;

      Tag t = this.dbGet(id);
      if (t == null)
      {
        this.renewList();
        t = this.get(id, false);
      }
      else
      {
        tags.put(id, t);
      }
      return t;
    }
  }

  public List<Tag> getAll()
  {
    tags = this.dbGetAll();
    return new ArrayList<Tag>(tags.values());
  }

  public boolean exists(Tag t)
  {
    return tags.containsKey(t.getId()) || this.get(t.getId()) != null;
  }

  public void save(Tag t)
  {
    if (this.exists(t))
    {
      this.alter(t);
    }
    else
    {
      this.insert(t);
    }
  }

  public void insert(Tag t)
  {
    if (!this.exists(t))
    {
      tags.put(t.getId(), t);
      this.dbInsert(t);
    }
  }

  public void supress(Tag t)
  {
    if (this.exists(t))
    {
      tags.remove(t.getId());
      this.dbDelete(t);
    }
  }

  public void alter(Tag t)
  {
    if (this.exists(t))
    {
      Log.i("UrbanTag", "Altering tag");
      tags.put(t.getId(), t);
      this.dbUpdate(t);
    }
  }

  public List<Tag> getAllForContent(int id)
  {
    return this.dbGetAllFor(DatabaseHelper.TABLE_CONTENTS_TAGS,
      DatabaseHelper.CONTENT_TAG_COL_CONTENT, id);
  }

  public List<Tag> getAllForPlace(int id)
  {
    return this.dbGetAllFor(DatabaseHelper.TABLE_PLACES_TAGS, DatabaseHelper.PLACE_TAG_COL_PLACE,
      id);
  }

  private void dbInsert(Tag t)
  {
    this.open();
    ContentValues values = new ContentValues();
    values.put(DatabaseHelper.TAG_COL_ID, t.getId());
    values.put(DatabaseHelper.TAG_COL_NAME, t.getValue());
    values.put(DatabaseHelper.TAG_COL_COLOR, t.getColor());
    values.put(DatabaseHelper.TAG_COL_NOTIFY, t.isSelected() ? 1 : 0);

    DB.insert(DatabaseHelper.TABLE_TAGS, DatabaseHelper.TAG_COL_ID, values);
    this.close();
  }

  private void dbDelete(Tag t)
  {
    this.open();
    DB.delete(DatabaseHelper.TABLE_TAGS, DatabaseHelper.TAG_COL_ID + "=?",
      new String[] { String.valueOf(t.getId()) });
    this.close();
  }

  private Tag dbGet(int id)
  {
    this.open();
    Cursor c = DB.query(DatabaseHelper.TABLE_TAGS, new String[] { DatabaseHelper.TAG_COL_ID,
      DatabaseHelper.TAG_COL_NAME, DatabaseHelper.TAG_COL_COLOR, DatabaseHelper.TAG_COL_NOTIFY },
      DatabaseHelper.TAG_COL_ID + "=? ", new String[] { String.valueOf(id) }, null, null, null);

    if (c.getCount() == 0)
    {
      c.close();
      return null;
    }

    c.moveToFirst();
    Tag t = this.cursorToTag(c);
    c.close();
    this.close();
    return t;
  }

  private void dbUpdate(Tag t)
  {
    this.open();
    ContentValues values = new ContentValues();
    values.put(DatabaseHelper.TAG_COL_NAME, t.getValue());
    values.put(DatabaseHelper.TAG_COL_COLOR, t.getColor());
    values.put(DatabaseHelper.TAG_COL_NOTIFY, t.isSelected() ? 1 : 0);

    DB.update(DatabaseHelper.TABLE_TAGS, values, DatabaseHelper.TAG_COL_ID + " =? ",
      new String[] { String.valueOf(t.getId()) });
    this.close();
  }

  private HashMap<Integer, Tag> dbGetAll()
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
      Tag t = this.cursorToTag(c);
      tags.put(t.getId(), t);
    }
    while (c.moveToNext());

    c.close();
    close();
    return tags;
  }

  private List<Tag> dbGetAllFor(String pivotTable, String pivotColumn, int id)
  {
    open();
    String query = "SELECT " + DatabaseHelper.TAG_COL_ID + ", " + DatabaseHelper.TAG_COL_NAME
      + ", " + DatabaseHelper.TAG_COL_COLOR + ", " + DatabaseHelper.TAG_COL_NOTIFY + " FROM "
      + DatabaseHelper.TABLE_TAGS + " tags INNER JOIN " + pivotTable + " pivot ON tags."
      + DatabaseHelper.TAG_COL_ID + "=pivot." + DatabaseHelper.CONTENT_TAG_COL_TAG
      + " WHERE pivot." + pivotColumn + "=?";

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

  private Tag cursorToTag(Cursor c)
  {
    Tag t = new Tag(c.getInt(DatabaseHelper.TAG_NUM_COL_ID),
      c.getString(DatabaseHelper.TAG_NUM_COL_NAME), c.getInt(DatabaseHelper.TAG_NUM_COL_COLOR));
    t.setSelected(c.getInt(DatabaseHelper.TAG_NUM_COL_NOTIFY) == 1);
    return t;
  }

  private void open()
  {
    DB = dbHelper.getWritableDatabase();
  }

  private void close()
  {
    DB.close();
  }
}