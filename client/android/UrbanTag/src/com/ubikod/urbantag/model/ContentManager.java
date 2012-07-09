package com.ubikod.urbantag.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ContentManager
{

  private SQLiteDatabase DB;

  private DatabaseHelper dbHelper;

  private HashMap<Integer, Content> contents = new HashMap<Integer, Content>();

  public ContentManager(DatabaseHelper databaseHelper)
  {
    dbHelper = databaseHelper;
  }

  public Content get(int id)
  {
    if (contents.containsKey(id))
    {
      return contents.get(id);
    }
    else
    {
      Content c = this.dbGet(id);
      if (c != null)
        contents.put(id, c);
      return c;
    }
  }

  public List<Content> getAll()
  {
    contents = this.dbGetAll();
    return new ArrayList<Content>(contents.values());
  }

  public List<Content> getAllForTags(int[] ids)
  {
    List<Content> res = this.dbGetAllForTags(ids);
    for (Content c : res)
    {
      contents.put(c.getId(), c);
    }
    return res;
  }

  public List<Content> getAllForPlace(int id)
  {
    List<Content> res = this.dbGetAllForPlace(id);
    for (Content c : res)
    {
      contents.put(c.getId(), c);
    }
    return res;
  }

  public boolean exists(Content c)
  {
    return contents.containsKey(c.getId()) || this.get(c.getId()) != null;
  }

  public void save(Content c)
  {
    if (this.exists(c))
    {
      this.alter(c);
    }
    else
    {
      this.insert(c);
    }
  }

  public void insert(Content c)
  {
    if (!this.exists(c))
    {
      contents.put(c.getId(), c);
      this.dbInsert(c);
    }
  }

  public void supress(Content c)
  {
    if (this.exists(c))
    {
      contents.remove(c.getId());
      this.dbDelete(c);
    }
  }

  public void alter(Content c)
  {
    if (this.exists(c))
    {
      contents.put(c.getId(), c);
      this.dbUpdate(c);
    }
  }

  public void clear()
  {
    this.dbClear();
  }

  private Content dbGet(int id)
  {
    this.open();
    Cursor c = DB.query(DatabaseHelper.TABLE_CONTENTS, new String[] {
      DatabaseHelper.CONTENT_COL_ID, DatabaseHelper.CONTENT_COL_NAME,
      DatabaseHelper.CONTENT_COL_PLACE, DatabaseHelper.CONTENT_COL_START_DATE,
      DatabaseHelper.CONTENT_COL_END_DATE, DatabaseHelper.CONTENT_COL_TAG },
      DatabaseHelper.CONTENT_COL_ID + "=? ", new String[] { String.valueOf(id) }, null, null, null);

    if (c.getCount() == 0)
    {
      c.close();
      return null;
    }

    c.moveToFirst();
    Content p = cursorToContent(c);
    c.close();
    this.close();
    return p;
  }

  private void dbDelete(Content c)
  {
    this.open();
    DB.delete(DatabaseHelper.TABLE_CONTENTS, DatabaseHelper.CONTENT_COL_ID + "=?",
      new String[] { String.valueOf(c.getId()) });

    DB.delete(DatabaseHelper.TABLE_CONTENTS, DatabaseHelper.CONTENT_TAG_COL_CONTENT + "=?",
      new String[] { String.valueOf(c.getId()) });
    this.close();
  }

  private void dbUpdate(Content c)
  {
    this.open();
    DB.update(DatabaseHelper.TABLE_CONTENTS, prepare(c, true), DatabaseHelper.CONTENT_COL_ID
      + " =? ", new String[] { String.valueOf(c.getId()) });

    /* Hardcore solution ... */
    // clean all tags
    DB.delete(DatabaseHelper.TABLE_CONTENTS_TAGS, DatabaseHelper.CONTENT_TAG_COL_CONTENT + "=?",
      new String[] { String.valueOf(c.getId()) });

    // reinsert in content-tag table
    String req = "INSERT INTO '" + DatabaseHelper.TABLE_CONTENTS_TAGS + "' ";
    boolean first = true;
    for (Tag t : c.getAllTags())
    {
      if (first)
      {
        first = false;
        req += "SELECT '" + c.getId() + "' AS '" + DatabaseHelper.CONTENT_TAG_COL_CONTENT + "', '"
          + t.getId() + "' AS '" + DatabaseHelper.CONTENT_TAG_NUM_COL_TAG + "'";
      }
      else
      {
        req += " UNION SELECT '" + c.getId() + "', '" + t.getId() + "'";
      }
    }
    DB.execSQL(req);
    this.close();
  }

  private void dbInsert(Content c)
  {
    this.open();
    DB.insert(DatabaseHelper.TABLE_CONTENTS, DatabaseHelper.CONTENT_COL_ID, prepare(c, false));

    // insert in place-tag table
    String req = "INSERT INTO '" + DatabaseHelper.TABLE_CONTENTS_TAGS + "' ";
    boolean first = true;
    for (Tag t : c.getAllTags())
    {
      if (first)
      {
        first = false;
        req += "SELECT " + c.getId() + " AS '" + DatabaseHelper.CONTENT_TAG_COL_CONTENT + "', "
          + t.getId() + " AS '" + DatabaseHelper.CONTENT_TAG_COL_TAG + "'";
      }
      else
      {
        req += " UNION SELECT " + c.getId() + ", " + t.getId() + "";
      }
    }
    req += ";";
    DB.execSQL(req);
    this.close();

  }

  private HashMap<Integer, Content> dbGetAll()
  {
    open();
    Cursor c = DB.query(DatabaseHelper.TABLE_CONTENTS, new String[] {
      DatabaseHelper.CONTENT_COL_ID, DatabaseHelper.CONTENT_COL_NAME,
      DatabaseHelper.CONTENT_COL_PLACE, DatabaseHelper.CONTENT_COL_START_DATE,
      DatabaseHelper.CONTENT_COL_END_DATE, DatabaseHelper.CONTENT_COL_TAG }, null, null, null,
      null, null);
    HashMap<Integer, Content> contents = new HashMap<Integer, Content>();

    if (c.getCount() == 0)
    {
      c.close();
      close();
      return contents;
    }

    c.moveToFirst();
    do
    {
      Content p = this.cursorToContent(c);
      contents.put(p.getId(), p);
    }
    while (c.moveToNext());

    c.close();
    close();
    return contents;
  }

  private List<Content> dbGetAllForPlace(int placeId)
  {
    List<Content> res = new ArrayList<Content>();
    this.open();
    Cursor c = DB.query(DatabaseHelper.TABLE_CONTENTS, new String[] {
      DatabaseHelper.CONTENT_COL_ID, DatabaseHelper.CONTENT_COL_NAME,
      DatabaseHelper.CONTENT_COL_PLACE, DatabaseHelper.CONTENT_COL_START_DATE,
      DatabaseHelper.CONTENT_COL_END_DATE, DatabaseHelper.CONTENT_COL_TAG },
      DatabaseHelper.CONTENT_COL_PLACE + "=? ", new String[] { String.valueOf(placeId) }, null,
      null, null);

    if (c.getCount() == 0)
    {
      c.close();
      close();
      return res;
    }

    c.moveToFirst();
    do
    {
      res.add(cursorToContent(c));
    }
    while (c.moveToNext());

    c.close();
    this.close();
    return res;
  }

  private List<Content> dbGetAllForTags(int[] tagsId)
  {
    List<Content> res = new ArrayList<Content>();
    if (tagsId.length > 0)
    {
      String[] selectionArgs = new String[tagsId.length];
      String sql = "SELECT DISTINCT " + DatabaseHelper.CONTENT_COL_ID + ", "
        + DatabaseHelper.CONTENT_COL_NAME + ", " + DatabaseHelper.CONTENT_COL_PLACE + ", "
        + DatabaseHelper.CONTENT_COL_START_DATE + ", " + DatabaseHelper.CONTENT_COL_END_DATE
        + ", contents." + DatabaseHelper.CONTENT_COL_TAG + " FROM " + DatabaseHelper.TABLE_CONTENTS
        + " contents INNER JOIN " + DatabaseHelper.TABLE_CONTENTS_TAGS + " pivot ON contents."
        + DatabaseHelper.CONTENT_COL_ID + "= pivot." + DatabaseHelper.CONTENT_TAG_COL_CONTENT
        + " WHERE pivot." + DatabaseHelper.CONTENT_TAG_COL_TAG + "=?";

      selectionArgs[0] = String.valueOf(tagsId[0]);
      for (int i = 1; i < tagsId.length; i++)
      {
        selectionArgs[i] = String.valueOf(tagsId[i]);
        sql += " OR pivot." + DatabaseHelper.CONTENT_TAG_COL_TAG + "=?";
      }

      this.open();
      Cursor c = DB.rawQuery(sql, selectionArgs);
      if (c.getCount() > 0)
      {
        c.moveToFirst();
        do
        {
          res.add(cursorToContent(c));
        }
        while (c.moveToNext());
      }
      c.close();
      this.close();
    }
    return res;
  }

  private void dbClear()
  {
    this.open();
    DB.delete(DatabaseHelper.TABLE_CONTENTS, null, null);
    DB.delete(DatabaseHelper.TABLE_CONTENTS_TAGS, null, null);
    this.close();
  }

  private void open()
  {
    DB = dbHelper.getWritableDatabase();
  }

  private void close()
  {
    DB.close();
  }

  private Content cursorToContent(Cursor c)
  {
    TagManager tagManager = new TagManager(dbHelper);
    PlaceManager placeManager = new PlaceManager(dbHelper);
    Content res = new Content(c.getInt(DatabaseHelper.CONTENT_NUM_COL_ID),
      c.getString(DatabaseHelper.CONTENT_NUM_COL_NAME),
      c.getInt(DatabaseHelper.CONTENT_NUM_COL_START_DATE),
      c.getInt(DatabaseHelper.CONTENT_NUM_COL_END_DATE),
      placeManager.get(c.getInt(DatabaseHelper.CONTENT_NUM_COL_PLACE)),
      tagManager.get(c.getInt(DatabaseHelper.CONTENT_NUM_COL_TAG)),
      tagManager.getAllForContent(c.getInt(DatabaseHelper.CONTENT_NUM_COL_ID)));
    return res;
  }

  private ContentValues prepare(Content c, boolean update)
  {
    ContentValues values = new ContentValues();
    if (!update)
    {
      values.put(DatabaseHelper.CONTENT_COL_ID, c.getId());
    }
    values.put(DatabaseHelper.CONTENT_COL_NAME, c.getName());
    values.put(DatabaseHelper.CONTENT_COL_PLACE, c.getPlace().getId());
    values.put(DatabaseHelper.CONTENT_COL_TAG, c.getTag().getId());
    values.put(DatabaseHelper.CONTENT_COL_START_DATE, c.getStartDate());
    values.put(DatabaseHelper.CONTENT_COL_END_DATE, c.getEndDate());
    return values;
  }
}
