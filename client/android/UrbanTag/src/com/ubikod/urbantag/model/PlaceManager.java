package com.ubikod.urbantag.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.android.maps.GeoPoint;

public class PlaceManager
{
  private SQLiteDatabase DB;

  private DatabaseHelper dbHelper;

  public PlaceManager(DatabaseHelper databaseHelper)
  {
    dbHelper = databaseHelper;
  }

  // public void loadOverlay()
  // {
  // if (this.overlay != null)
  // {
  // for (Place p : getAll())
  // {
  // if (p.getMainTag().isSelected())
  // overlay.addPlace(p);
  // }
  // }
  // }

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

  public Place getById(int id)
  {
    Place res = null;
    open();
    res = dbGetById(id);
    close();
    return res;
  }

  public void save(Place p)
  {
    if (getById(p.getId()) == null)
    {
      insert(p);
    }
    else
    {
      update(p);
    }
  }

  public void clear()
  {
    open();
    dbClear();
    close();
  }

  public List<Place> get(int[] ids)
  {
    return this.dbGet(ids);
  }

  public List<Place> getAllForTags(int[] tagsId)
  {
    return this.dbGetAllForTags(tagsId);
  }

  private void insert(Place p)
  {
    open();
    dbInsert(p);
    close();
  }

  private void delete(Place p)
  {
    open();
    dbDelete(p);
    close();
  }

  private void update(Place p)
  {
    open();
    dbUpdate(p);
    close();
  }

  private List<Place> getAll()
  {
    return dbGetAll();
  }

  private List<Place> dbGetAll()
  {
    open();

    Cursor c = DB.query(DatabaseHelper.TABLE_PLACES, new String[] { DatabaseHelper.PLACE_COL_ID,
      DatabaseHelper.PLACE_COL_NAME, DatabaseHelper.PLACE_COL_TAG, DatabaseHelper.PLACE_COL_LAT,
      DatabaseHelper.PLACE_COL_LON }, null, null, null, null, null);
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
      Place p = cursorToPlace(c);
      places.add(p);
    }
    while (c.moveToNext());

    c.close();
    close();
    return places;
  }

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

      open();
      Cursor c = DB.rawQuery(sql, selectionArgs);
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
      close();
    }
    return res;
  }

  private Place dbGetById(int id)
  {
    int a[] = { id };
    List<Place> p = this.dbGet(a);
    if (p.size() > 0)
      return this.dbGet(a).get(0);
    return null;
  }

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

    Cursor c = DB.query(DatabaseHelper.TABLE_PLACES, new String[] { DatabaseHelper.PLACE_COL_ID,
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
      places.add(cursorToPlace(c));
    }
    while (c.moveToNext());

    c.close();
    close();
    return places;
  }

  private void dbInsert(Place p)
  {
    DB.insert(DatabaseHelper.TABLE_PLACES, DatabaseHelper.PLACE_COL_ID, prepare(p, false));

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
    DB.execSQL(req);
  }

  private void dbDelete(Place p)
  {
    DB.delete(DatabaseHelper.TABLE_PLACES, DatabaseHelper.PLACE_COL_ID + "=?",
      new String[] { String.valueOf(p.getId()) });

    DB.delete(DatabaseHelper.TABLE_PLACES_TAGS, DatabaseHelper.PLACE_TAG_COL_PLACE + "=?",
      new String[] { String.valueOf(p.getId()) });
  }

  private void dbUpdate(Place p)
  {
    DB.update(DatabaseHelper.TABLE_PLACES, prepare(p, true), DatabaseHelper.PLACE_COL_ID + " =? ",
      new String[] { String.valueOf(p.getId()) });

    /* Hardcore solution ... */
    // clean all tags
    DB.delete(DatabaseHelper.TABLE_PLACES_TAGS, DatabaseHelper.PLACE_TAG_COL_PLACE + "=?",
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
    DB.execSQL(req);
  }

  private void dbClear()
  {
    DB.delete(DatabaseHelper.TABLE_PLACES, null, null);
    DB.delete(DatabaseHelper.TABLE_PLACES_TAGS, null, null);
  }

  private void open()
  {
    DB = dbHelper.getWritableDatabase();
  }

  private void close()
  {
    DB.close();
  }

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

  private Place cursorToPlace(Cursor c)
  {
    TagManager tagManager = new TagManager(dbHelper);
    Place p = new Place(c.getInt(DatabaseHelper.PLACE_NUM_COL_ID),
      c.getString(DatabaseHelper.PLACE_NUM_COL_NAME),
      tagManager.getByID(c.getInt(DatabaseHelper.PLACE_NUM_COL_TAG)), new GeoPoint(
        c.getInt(DatabaseHelper.PLACE_NUM_COL_LAT), c.getInt(DatabaseHelper.PLACE_NUM_COL_LON)),
      tagManager.getAllForPlace(c.getInt(DatabaseHelper.PLACE_NUM_COL_ID)));
    return p;
  }
}
