/*
 * Copyright 2012 Ubikod
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.ubikod.urbantag.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class TagManager
{
  /** Database */
  private SQLiteDatabase mDB;

  /** DatabaseHelper */
  private DatabaseHelper mDbHelper;

  /** Store if we made an request to retrieve all tags from db */
  private boolean madeAllRequest = false;

  /**
   * Hashmap containing all place already accessed. Key is id, value is tag
   */
  private static HashMap<Integer, Tag> sTags = new HashMap<Integer, Tag>();

  /**
   * Constructor
   * @param databaseHelper
   */
  public TagManager(DatabaseHelper databaseHelper)
  {
    mDbHelper = databaseHelper;
  }

  /**
   * Create a tag from json
   * @param json
   * @return Tag
   */
  public static Tag createTag(JSONObject json)
  {
    Tag t = null;
    if (json.has("id") && json.has("name") && json.has("color"))
    {
      try
      {
        t = new Tag(json.getInt("id"), json.getString("name"), Integer.parseInt(
          json.getString("color"), 16) + 0xff000000);
      }
      catch (JSONException je)
      {
        je.printStackTrace();
      }
    }
    return t;
  }

  /**
   * Do update on stocked tags. If tag is new insert it, if already existing update it if needed.
   * Caution : If a already existing tag is missing it will be deleted.
   * @param list
   */
  public void update(List<Tag> list)
  {
    // Tags maj ou ajoutes
    for (Tag tag : list)
    {
      this.save(tag);
    }

    // Tags n'existant plus
    Vector<Tag> toDelete = new Vector<Tag>();
    for (Tag t : sTags.values())
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

  /**
   * Toggle notification for a specified tag
   * @param t
   */
  public void toggleNotification(Tag t)
  {
    t.setSelected(!t.isSelected());
    this.alter(t);
  }

  /**
   * Retrieve tag from its id
   * @param id
   * @return Tag if existing, null otherwise
   */
  public Tag get(int id)
  {
    if (sTags.containsKey(id))
    {
      return sTags.get(id);
    }
    else
    {
      Tag t = this.dbGet(id);
      if (t != null)
      {
        sTags.put(id, t);
      }
      return t;
    }
  }

  /**
   * Get all tags as a hashmap
   * @return
   */
  public HashMap<Integer, Tag> getAllAsHashMap()
  {
    if (!madeAllRequest)
    {
      sTags = this.dbGetAll();
      madeAllRequest = true;
    }
    return sTags;
  }

  /**
   * Get all tags as a list
   * @return
   */
  public List<Tag> getAll()
  {
    if (!madeAllRequest)
    {
      sTags = this.dbGetAll();
      madeAllRequest = true;
    }
    return new ArrayList<Tag>(sTags.values());
  }

  /**
   * Test if tags exists
   * @param t
   * @return True if the tag exists, false otherwise
   */
  public boolean exists(Tag t)
  {
    return sTags.containsKey(t.getId()) || this.get(t.getId()) != null;
  }

  /**
   * Save a tag. If tag already exists update it, if new add it
   * @param t
   */
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

  /**
   * Insert a tag. If tag already exists(id is allocated) does nothing
   * @param t
   */
  public void insert(Tag t)
  {
    if (!this.exists(t))
    {
      sTags.put(t.getId(), t);
      this.dbInsert(t);
    }
  }

  /**
   * Supress a tag
   * @param t
   */
  public void supress(Tag t)
  {
    if (this.exists(t))
    {
      sTags.remove(t.getId());
      this.dbDelete(t);
    }
  }

  /**
   * Alter a tag
   * @param t
   */
  public void alter(Tag t)
  {
    if (this.exists(t))
    {
      Log.i("UrbanTag", "Altering tag");
      sTags.put(t.getId(), t);
      this.dbUpdate(t);
    }
  }

  /**
   * Get all tags for content id
   * @param id content id
   * @return List of tags
   */
  public List<Tag> getAllForContent(int id)
  {
    return this.dbGetAllFor(DatabaseHelper.TABLE_CONTENTS_TAGS,
      DatabaseHelper.CONTENT_TAG_COL_CONTENT, id);
  }

  /**
   * Gets all tags for place id
   * @param id place id
   * @return List of tags
   */
  public List<Tag> getAllForPlace(int id)
  {
    return this.dbGetAllFor(DatabaseHelper.TABLE_PLACES_TAGS, DatabaseHelper.PLACE_TAG_COL_PLACE,
      id);
  }

  /**
   * Insert tag on db
   * @param t
   */
  private void dbInsert(Tag t)
  {
    this.open();
    ContentValues values = new ContentValues();
    values.put(DatabaseHelper.TAG_COL_ID, t.getId());
    values.put(DatabaseHelper.TAG_COL_NAME, t.getValue());
    values.put(DatabaseHelper.TAG_COL_COLOR, t.getColor());
    values.put(DatabaseHelper.TAG_COL_NOTIFY, t.isSelected() ? 1 : 0);

    mDB.insert(DatabaseHelper.TABLE_TAGS, DatabaseHelper.TAG_COL_ID, values);
    this.close();
  }

  /**
   * Delete tag on db
   * @param t
   */
  private void dbDelete(Tag t)
  {
    this.open();
    mDB.delete(DatabaseHelper.TABLE_TAGS, DatabaseHelper.TAG_COL_ID + "=?",
      new String[] { String.valueOf(t.getId()) });
    this.close();
  }

  /**
   * Fetch tag from db
   * @param id tag id
   * @return
   */
  private Tag dbGet(int id)
  {
    this.open();
    Cursor c = mDB.query(DatabaseHelper.TABLE_TAGS, new String[] { DatabaseHelper.TAG_COL_ID,
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

  /**
   * Update a tag on db
   * @param t
   */
  private void dbUpdate(Tag t)
  {
    this.open();
    ContentValues values = new ContentValues();
    values.put(DatabaseHelper.TAG_COL_NAME, t.getValue());
    values.put(DatabaseHelper.TAG_COL_COLOR, t.getColor());
    values.put(DatabaseHelper.TAG_COL_NOTIFY, t.isSelected() ? 1 : 0);

    mDB.update(DatabaseHelper.TABLE_TAGS, values, DatabaseHelper.TAG_COL_ID + " =? ",
      new String[] { String.valueOf(t.getId()) });
    this.close();
  }

  /**
   * Get all tags from db
   * @return
   */
  private HashMap<Integer, Tag> dbGetAll()
  {
    open();

    Cursor c = mDB.query(DatabaseHelper.TABLE_TAGS, new String[] { DatabaseHelper.TAG_COL_ID,
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

  /**
   * Get all tags from a pivot table
   * @param pivotTable pivot table name
   * @param pivotColumn pivot table column
   * @param id
   * @return
   */
  private List<Tag> dbGetAllFor(String pivotTable, String pivotColumn, int id)
  {
    open();
    String query = "SELECT " + DatabaseHelper.TAG_COL_ID + ", " + DatabaseHelper.TAG_COL_NAME
      + ", " + DatabaseHelper.TAG_COL_COLOR + ", " + DatabaseHelper.TAG_COL_NOTIFY + " FROM "
      + DatabaseHelper.TABLE_TAGS + " tags INNER JOIN " + pivotTable + " pivot ON tags."
      + DatabaseHelper.TAG_COL_ID + "=pivot." + DatabaseHelper.CONTENT_TAG_COL_TAG
      + " WHERE pivot." + pivotColumn + "=?";

    Cursor c = mDB.rawQuery(query, new String[] { String.valueOf(id) });
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

  /**
   * Convert a cursor to a tag
   * @param c
   * @return
   */
  private Tag cursorToTag(Cursor c)
  {
    Tag t = new Tag(c.getInt(DatabaseHelper.TAG_NUM_COL_ID),
      c.getString(DatabaseHelper.TAG_NUM_COL_NAME), c.getInt(DatabaseHelper.TAG_NUM_COL_COLOR));
    t.setSelected(c.getInt(DatabaseHelper.TAG_NUM_COL_NOTIFY) == 1);
    return t;
  }

  /**
   * Open database connection
   */
  private void open()
  {
    mDB = mDbHelper.getWritableDatabase();
  }

  /**
   * Close database connection
   */
  private void close()
  {
    mDB.close();
  }
}
