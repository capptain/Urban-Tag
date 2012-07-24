package com.ubikod.urbantag.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.android.maps.GeoPoint;

public class PlaceManager
{
  /** The Database */
  private SQLiteDatabase mDB;

  /** DatabaseHelper */
  private DatabaseHelper mDbHelper;

  /** Hashmap containing all place already accessed. Key is id, value is place */
  private static HashMap<Integer, Place> mPlaces = new HashMap<Integer, Place>();

  /**
   * Cosntructor
   * @param databaseHelper
   */
  public PlaceManager(DatabaseHelper databaseHelper)
  {
    mDbHelper = databaseHelper;
  }

  /**
   * Get places we have to show on map
   * @return visible places
   */
  public Vector<Place> getVisiblePlaces()
  {
    Vector<Place> res = new Vector<Place>();
    for (Place p : this.getAll())
    {
      if (p.getMainTag().isSelected())
        res.add(p);
    }
    return res;
  }

  /**
   * Get specified
   * @param id
   * @return Place with specified id or null
   */
  public Place get(int id)
  {
    if (mPlaces.containsKey(id))
    {
      return mPlaces.get(id);
    }
    else
    {
      Place p = this.dbGet(id);
      if (p != null)
        mPlaces.put(id, p);
      return p;
    }
  }

  /**
   * Get specified places
   * @param ids Array of places id
   * @return A list of specified places
   */
  public List<Place> get(int[] ids)
  {
    this.getAll();
    List<Place> res = new ArrayList<Place>();
    for (int id : ids)
    {
      if (mPlaces.containsKey(id))
        res.add(mPlaces.get(id));
    }

    return res;
  }

  /**
   * Get all places
   * @return List of all places
   */
  public List<Place> getAll()
  {
    mPlaces = this.dbGetAll();
    return new ArrayList<Place>(mPlaces.values());
  }

  /**
   * Test if a place exists
   * @param p a place to test
   * @return True if place exists, false otherwise
   */
  public boolean exists(Place p)
  {
    return mPlaces.containsKey(p.getId()) || this.get(p.getId()) != null;
  }

  /**
   * Save a place. Add it if new, update if already existing
   * @param p the place to save
   */
  public void save(Place p)
  {
    if (this.exists(p))
    {
      this.alter(p);
    }
    else
    {
      this.insert(p);
    }
  }

  /**
   * Insert a place
   * @param p a place. If place already exists(id already allocated) does nothing
   */
  public void insert(Place p)
  {
    if (!this.exists(p))
    {
      mPlaces.put(p.getId(), p);
      this.dbInsert(p);
    }
  }

  /**
   * Suppres a place
   * @param p the place to suppress
   */
  public void supress(Place p)
  {
    if (this.exists(p))
    {
      mPlaces.remove(p.getId());
      this.dbDelete(p);
    }
  }

  /**
   * Alter a place
   * @param p Place to alter
   */
  public void alter(Place p)
  {
    if (this.exists(p))
    {
      mPlaces.put(p.getId(), p);
      this.dbUpdate(p);
    }
  }

  /**
   * Suppress all places
   */
  public void clear()
  {
    this.dbClear();
  }

  /**
   * Get all the places containing specified tags ids
   * @param ids Ids of tags we want
   * @return list of places
   */
  public List<Place> getAllForTags(int[] ids)
  {
    List<Place> res = this.dbGetAllForTags(ids);
    for (Place p : res)
    {
      mPlaces.put(p.getId(), p);
    }
    return res;
  }

  /**
   * Get a place in db
   * @param id Place id
   * @return Place
   */
  private Place dbGet(int id)
  {
    int a[] = { id };
    List<Place> p = this.dbGet(a);
    if (p.size() > 0)
      return this.dbGet(a).get(0);
    return null;
  }

  /**
   * Get places on db
   * @param placeId Array of places id we want to retrieve from db
   * @return List of places
   */
  private List<Place> dbGet(int[] placeId)
  {
    open();

    String ids = "(";
    boolean first = true;
    for (int i = 0; i < placeId.length; i++)
    {
      if (!first)
      {
        ids += ", ";
      }
      ids += placeId[i];
      first = false;
    }

    ids += ")";

    Cursor c = mDB.query(DatabaseHelper.TABLE_PLACES, new String[] { DatabaseHelper.PLACE_COL_ID,
      DatabaseHelper.PLACE_COL_NAME, DatabaseHelper.PLACE_COL_TAG, DatabaseHelper.PLACE_COL_LAT,
      DatabaseHelper.PLACE_COL_LON }, DatabaseHelper.PLACE_COL_ID + " IN " + ids, null, null, null,
      null);
    List<Place> places = new ArrayList<Place>();

    if (c.getCount() == 0)
    {
      c.close();
      close();
      return places;
    }

    c.moveToFirst();
    do
    {
      places.add(this.cursorToPlace(c));
    }
    while (c.moveToNext());

    c.close();
    close();
    return places;
  }

  /**
   * Insert a place on db
   * @param p
   */
  private void dbInsert(Place p)
  {
    this.open();
    mDB.insert(DatabaseHelper.TABLE_PLACES, DatabaseHelper.PLACE_COL_ID, this.prepare(p, false));

    // insert in place-tag table
    String req = "INSERT INTO '" + DatabaseHelper.TABLE_PLACES_TAGS + "' ";
    boolean first = true;
    for (Tag t : p.getAllTags())
    {
      if (first)
      {
        first = false;
        req += "SELECT " + p.getId() + " AS '" + DatabaseHelper.PLACE_TAG_COL_PLACE + "', "
          + t.getId() + " AS '" + DatabaseHelper.PLACE_TAG_COL_TAG + "'";
      }
      else
      {
        req += " UNION SELECT " + p.getId() + ", " + t.getId() + "";
      }
    }
    req += ";";
    mDB.execSQL(req);
    this.close();
  }

  /**
   * Update a place on db
   * @param p
   */
  private void dbUpdate(Place p)
  {
    this.open();
    mDB.update(DatabaseHelper.TABLE_PLACES, prepare(p, true), DatabaseHelper.PLACE_COL_ID + " =? ",
      new String[] { String.valueOf(p.getId()) });

    /* Hardcore solution ... */
    // clean all tags
    mDB.delete(DatabaseHelper.TABLE_PLACES_TAGS, DatabaseHelper.PLACE_TAG_COL_PLACE + "=?",
      new String[] { String.valueOf(p.getId()) });

    // reinsert in place-tag table
    String req = "INSERT INTO '" + DatabaseHelper.TABLE_PLACES_TAGS + "' ";
    boolean first = true;
    for (Tag t : p.getAllTags())
    {
      if (first)
      {
        first = false;
        req += "SELECT '" + p.getId() + "' AS '" + DatabaseHelper.PLACE_TAG_COL_PLACE + "', '"
          + t.getId() + "' AS '" + DatabaseHelper.PLACE_TAG_COL_TAG + "'";
      }
      else
      {
        req += " UNION SELECT '" + p.getId() + "', '" + t.getId() + "'";
      }
    }
    mDB.execSQL(req);
    this.close();
  }

  /**
   * delete a place on db
   * @param p
   */
  private void dbDelete(Place p)
  {
    this.open();
    mDB.delete(DatabaseHelper.TABLE_PLACES, DatabaseHelper.PLACE_COL_ID + "=?",
      new String[] { String.valueOf(p.getId()) });

    mDB.delete(DatabaseHelper.TABLE_PLACES_TAGS, DatabaseHelper.PLACE_TAG_COL_PLACE + "=?",
      new String[] { String.valueOf(p.getId()) });
    this.close();
  }

  /**
   * Get all places stocked on db as a hashmap
   * @return hashmap of places. Key is id, value is place.
   */
  private HashMap<Integer, Place> dbGetAll()
  {
    open();

    Cursor c = mDB.query(DatabaseHelper.TABLE_PLACES, new String[] { DatabaseHelper.PLACE_COL_ID,
      DatabaseHelper.PLACE_COL_NAME, DatabaseHelper.PLACE_COL_TAG, DatabaseHelper.PLACE_COL_LAT,
      DatabaseHelper.PLACE_COL_LON }, null, null, null, null, null);
    HashMap<Integer, Place> places = new HashMap<Integer, Place>();

    if (c.getCount() == 0)
    {
      c.close();
      close();
      return places;
    }

    c.moveToFirst();
    do
    {
      Place p = cursorToPlace(c);
      places.put(p.getId(), p);
    }
    while (c.moveToNext());

    c.close();
    close();
    return places;
  }

  /**
   * Suppress all places on db
   */
  private void dbClear()
  {
    this.open();
    mDB.delete(DatabaseHelper.TABLE_PLACES, null, null);
    mDB.delete(DatabaseHelper.TABLE_PLACES_TAGS, null, null);
    this.close();
  }

  /**
   * Fetch places for specified tags id on db
   * @param tagsId
   * @return List of places
   */
  private List<Place> dbGetAllForTags(int[] tagsId)
  {
    List<Place> res = new ArrayList<Place>();
    if (tagsId.length > 0)
    {
      String[] selectionArgs = new String[tagsId.length];
      String sql = "SELECT DISTINCT " + DatabaseHelper.PLACE_COL_ID + ", "
        + DatabaseHelper.PLACE_COL_NAME + ", places." + DatabaseHelper.PLACE_COL_TAG + ", "
        + DatabaseHelper.PLACE_COL_LAT + ", " + DatabaseHelper.PLACE_COL_LON + " FROM "
        + DatabaseHelper.TABLE_PLACES + " places INNER JOIN " + DatabaseHelper.TABLE_PLACES_TAGS
        + " pivot ON places." + DatabaseHelper.PLACE_COL_ID + "= pivot."
        + DatabaseHelper.PLACE_TAG_COL_PLACE + " WHERE pivot." + DatabaseHelper.PLACE_TAG_COL_TAG
        + "=?";

      selectionArgs[0] = String.valueOf(tagsId[0]);
      for (int i = 1; i < tagsId.length; i++)
      {
        selectionArgs[i] = String.valueOf(tagsId[i]);
        sql += " OR pivot." + DatabaseHelper.PLACE_TAG_COL_TAG + "=?";
      }

      this.open();
      Cursor c = mDB.rawQuery(sql, selectionArgs);
      if (c.getCount() > 0)
      {
        c.moveToFirst();
        do
        {
          res.add(cursorToPlace(c));
        }
        while (c.moveToNext());
      }
      c.close();
      this.close();
    }
    return res;
  }

  /**
   * Open connection with db
   */
  private void open()
  {
    mDB = mDbHelper.getWritableDatabase();
  }

  /**
   * Close connection with db
   */
  private void close()
  {
    mDB.close();
  }

  /**
   * Prepare value for db update or insert
   * @param p Place
   * @param update True if doing an update, false otheirwise
   * @return ContentValues
   */
  private ContentValues prepare(Place p, boolean update)
  {
    ContentValues values = new ContentValues();
    if (!update)
    {
      values.put(DatabaseHelper.PLACE_COL_ID, p.getId());
    }
    values.put(DatabaseHelper.PLACE_COL_NAME, p.getName());
    values.put(DatabaseHelper.PLACE_COL_TAG, p.getMainTag().getId());
    values.put(DatabaseHelper.PLACE_COL_LAT, p.getPosition().getLatitudeE6());
    values.put(DatabaseHelper.PLACE_COL_LON, p.getPosition().getLongitudeE6());
    return values;
  }

  /**
   * Convert a cursor to a place
   * @param c
   * @return place
   */
  private Place cursorToPlace(Cursor c)
  {
    TagManager tagManager = new TagManager(mDbHelper);
    Place p = new Place(c.getInt(DatabaseHelper.PLACE_NUM_COL_ID),
      c.getString(DatabaseHelper.PLACE_NUM_COL_NAME),
      tagManager.get(c.getInt(DatabaseHelper.PLACE_NUM_COL_TAG)), new GeoPoint(
        c.getInt(DatabaseHelper.PLACE_NUM_COL_LAT), c.getInt(DatabaseHelper.PLACE_NUM_COL_LON)),
      tagManager.getAllForPlace(c.getInt(DatabaseHelper.PLACE_NUM_COL_ID)));
    return p;
  }
}
