package com.ubikod.urbantag.model;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ContentManager
{

  private SQLiteDatabase DB;

  private DatabaseHelper dbHelper;

  public ContentManager(DatabaseHelper databaseHelper)
  {
    dbHelper = databaseHelper;
  }

  public List<Content> getAllForPlace(int placeId)
  {
    return this.dbGetAllForPlace(placeId);
  }

  public Content getById(int id)
  {
    Content c = null;
    open();
    c = this.dbGetById(id);
    close();
    return c;
  }

  public void insert(Content c)
  {
    open();
    dbInsert(c);
    close();
  }

  public List<Content> getAllForTags(int[] tagsId)
  {
    return this.dbGetAllForTags(tagsId);
  }

  private void delete(Content c)
  {
    open();
    dbDelete(c);
    close();
  }

  private void update(Content c)
  {
    open();
    dbUpdate(c);
    close();
  }

  private void dbInsert(Content c)
  {
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

  }

  private void dbUpdate(Content c)
  {
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
  }

  private void dbDelete(Content c)
  {
    DB.delete(DatabaseHelper.TABLE_CONTENTS, DatabaseHelper.CONTENT_COL_ID + "=?",
      new String[] { String.valueOf(c.getId()) });

    DB.delete(DatabaseHelper.TABLE_CONTENTS, DatabaseHelper.CONTENT_TAG_COL_CONTENT + "=?",
      new String[] { String.valueOf(c.getId()) });
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

  private List<Content> dbGetAllForPlace(int placeId)
  {
    List<Content> res = new ArrayList<Content>();
    open();
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
    close();
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

      open();
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
      close();
    }
    return res;
  }

  private Content dbGetById(int id)
  {
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
    return p;
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
      placeManager.getById(c.getInt(DatabaseHelper.CONTENT_NUM_COL_PLACE)),
      tagManager.getByID(c.getInt(DatabaseHelper.CONTENT_NUM_COL_TAG)),
      tagManager.getAllForContent(c.getInt(DatabaseHelper.CONTENT_NUM_COL_ID)));
    return res;
  }
}
