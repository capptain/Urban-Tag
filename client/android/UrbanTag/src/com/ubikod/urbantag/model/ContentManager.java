package com.ubikod.urbantag.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ContentManager
{

  private SQLiteDatabase mDB;

  private DatabaseHelper mDbHelper;

  private HashMap<Integer, Content> mContents = new HashMap<Integer, Content>();

  public ContentManager(DatabaseHelper databaseHelper)
  {
    mDbHelper = databaseHelper;
  }

  public Content get(int id)
  {
    if (mContents.containsKey(id))
    {
      return mContents.get(id);
    }
    else
    {
      Content c = this.dbGet(id);
      if (c != null)
        mContents.put(id, c);
      return c;
    }
  }

  public List<Content> get(int[] ids)
  {
    this.getAll();
    List<Content> res = new ArrayList<Content>();
    for (int id : ids)
    {
      if (mContents.containsKey(id))
        res.add(mContents.get(id));
    }

    return res;
  }

  public List<Content> getAll()
  {
    mContents = this.dbGetAll();
    return new ArrayList<Content>(mContents.values());
  }

  public List<Content> getAllForTags(int[] ids)
  {
    List<Content> res = this.dbGetAllForTags(ids);
    for (Content c : res)
    {
      mContents.put(c.getId(), c);
    }
    return res;
  }

  public List<Content> getAllForPlace(int id)
  {
    List<Content> res = this.dbGetAllForPlace(id);
    for (Content c : res)
    {
      mContents.put(c.getId(), c);
    }
    return res;
  }

  public boolean exists(Content c)
  {
    return mContents.containsKey(c.getId()) || this.get(c.getId()) != null;
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
      mContents.put(c.getId(), c);
      this.dbInsert(c);
    }
  }

  public void supress(Content c)
  {
    if (this.exists(c))
    {
      mContents.remove(c.getId());
      this.dbDelete(c);
    }
  }

  public void alter(Content c)
  {
    if (this.exists(c))
    {
      mContents.put(c.getId(), c);
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
    Cursor c = mDB.query(DatabaseHelper.TABLE_CONTENTS, new String[] {
      DatabaseHelper.CONTENT_COL_ID, DatabaseHelper.CONTENT_COL_NAME,
      DatabaseHelper.CONTENT_COL_PLACE, DatabaseHelper.CONTENT_COL_START_DATE,
      DatabaseHelper.CONTENT_COL_END_DATE, DatabaseHelper.CONTENT_COL_TAG },
      DatabaseHelper.CONTENT_COL_ID + "=? ", new String[] { String.valueOf(id) }, null, null, null);

    if (c.getCount() == 0)
    {
      c.close();
      this.close();
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
    mDB.delete(DatabaseHelper.TABLE_CONTENTS, DatabaseHelper.CONTENT_COL_ID + "=?",
      new String[] { String.valueOf(c.getId()) });

    mDB.delete(DatabaseHelper.TABLE_CONTENTS, DatabaseHelper.CONTENT_TAG_COL_CONTENT + "=?",
      new String[] { String.valueOf(c.getId()) });
    this.close();
  }

  private void dbUpdate(Content c)
  {
    this.open();
    mDB.update(DatabaseHelper.TABLE_CONTENTS, prepare(c, true), DatabaseHelper.CONTENT_COL_ID
      + " =? ", new String[] { String.valueOf(c.getId()) });

    /* Hardcore solution ... */
    // clean all tags
    mDB.delete(DatabaseHelper.TABLE_CONTENTS_TAGS, DatabaseHelper.CONTENT_TAG_COL_CONTENT + "=?",
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
    mDB.execSQL(req);
    this.close();
  }

  private void dbInsert(Content c)
  {
    this.open();
    mDB.insert(DatabaseHelper.TABLE_CONTENTS, DatabaseHelper.CONTENT_COL_ID, prepare(c, false));

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
    mDB.execSQL(req);
    this.close();

  }

  private HashMap<Integer, Content> dbGetAll()
  {
    open();
    Cursor c = mDB.query(DatabaseHelper.TABLE_CONTENTS, new String[] {
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
    Cursor c = mDB.query(DatabaseHelper.TABLE_CONTENTS, new String[] {
      DatabaseHelper.CONTENT_COL_ID, DatabaseHelper.CONTENT_COL_NAME,
      DatabaseHelper.CONTENT_COL_TAG, DatabaseHelper.CONTENT_COL_PLACE,
      DatabaseHelper.CONTENT_COL_START_DATE, DatabaseHelper.CONTENT_COL_END_DATE },
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
        + DatabaseHelper.CONTENT_COL_NAME + ", contents." + DatabaseHelper.CONTENT_COL_TAG + ", "
        + DatabaseHelper.CONTENT_COL_PLACE + ", " + DatabaseHelper.CONTENT_COL_START_DATE + ", "
        + DatabaseHelper.CONTENT_COL_END_DATE + ", contents." + DatabaseHelper.CONTENT_COL_TAG
        + " FROM " + DatabaseHelper.TABLE_CONTENTS + " contents INNER JOIN "
        + DatabaseHelper.TABLE_CONTENTS_TAGS + " pivot ON contents."
        + DatabaseHelper.CONTENT_COL_ID + "= pivot." + DatabaseHelper.CONTENT_TAG_COL_CONTENT
        + " WHERE pivot." + DatabaseHelper.CONTENT_TAG_COL_TAG + "=?";

      selectionArgs[0] = String.valueOf(tagsId[0]);
      for (int i = 1; i < tagsId.length; i++)
      {
        selectionArgs[i] = String.valueOf(tagsId[i]);
        sql += " OR pivot." + DatabaseHelper.CONTENT_TAG_COL_TAG + "=?";
      }

      this.open();
      Cursor c = mDB.rawQuery(sql, selectionArgs);
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
    mDB.delete(DatabaseHelper.TABLE_CONTENTS, null, null);
    mDB.delete(DatabaseHelper.TABLE_CONTENTS_TAGS, null, null);
    this.close();
  }

  private void open()
  {
    mDB = mDbHelper.getWritableDatabase();
  }

  private void close()
  {
    mDB.close();
  }

  private Content cursorToContent(Cursor c)
  {
    TagManager tagManager = new TagManager(mDbHelper);
    PlaceManager placeManager = new PlaceManager(mDbHelper);
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
