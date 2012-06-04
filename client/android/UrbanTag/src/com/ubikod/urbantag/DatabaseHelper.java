package com.ubikod.urbantag;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper
{
  public static final int VERSION_BDD = 1;
  public static final String DB_NAME = "urbantag.db";

  // Common

  // Tags
  public static int TAG_NUM_COL_ID = 0;
  public static final String TAG_COL_ID = "id";
  public static final String TABLE_TAGS = "tags";
  public static final int TAG_NUM_COL_NAME = 1;
  public static final String TAG_COL_NAME = "name";
  public static final int TAG_NUM_COL_COLOR = 2;
  public static final String TAG_COL_COLOR = "color";
  public static final int TAG_NUM_COL_NOTIFY = 3;
  public static final String TAG_COL_NOTIFY = "notify";

  // Place
  public static final String TABLE_PLACES = "places";
  public static final int PLACE_NUM_COL_ID = 0;
  public static final String PLACE_COL_ID = "id";
  public static final int PLACE_NUM_COL_NAME = 1;
  public static final String PLACE_COL_NAME = "name";
  public static final int PLACE_NUM_COL_TAG = 2;
  public static final String PLACE_COL_TAG = "tag_id";
  public static final int PLACE_NUM_COL_LAT = 3;
  public static final String PLACE_COL_LAT = "lat";
  public static final int PLACE_NUM_COL_LON = 4;
  public static final String PLACE_COL_LON = "lon";

  // Content
  public static final String TABLE_CONTENTS = "contents";
  public static final int CONTENT_NUM_COL_ID = 0;
  public static final String CONTENT_COL_ID = "id";
  public static final int CONTENT_NUM_COL_NAME = 1;
  public static final String CONTENT_COL_NAME = "name";
  public static final int CONTENT_NUM_COL_TAG = 2;
  public static final String CONTENT_COL_TAG = "tag_id";
  public static final int CONTENT_NUM_COL_START_DATE = 3;
  public static final String CONTENT_COL_START_DATE = "start_date";
  public static final int CONTENT_NUM_COL_END_DATE = 4;
  public static final String CONTENT_COL_END_DATE = "end_date";

  // Place - Tag pivot
  public static final String TABLE_PLACES_TAGS = "places_tags";
  public static final int PLACE_TAG_NUM_COL_PLACE = 0;
  public static final String PLACE_TAG_COL_PLACE = "place_id";
  public static final int PLACE_TAG_NUM_COL_TAG = 1;
  public static final String PLACE_TAG_COL_TAGS = "tag_id";

  // Content - Tag pivot
  public static String TABLE_CONTENTS_TAGS = "contents_tags";
  public static int CONTENT_TAG_NUM_COL_CONTENT = 0;
  public static String CONTENT_TAG_COL_CONTENT = "content_id";
  public static int CONTENT_TAG_NUM_COL_TAG = 1;
  public static String CONTENT_TAG_COL_TAG = "tag_id";

  // Tables
  private static final String CREATE_TABLE_TAGS = "CREATE TABLE " + TABLE_TAGS + " ( " + TAG_COL_ID
    + " INTEGER PRIMARY KEY , " + TAG_COL_NAME + " INTEGER, " + TAG_COL_COLOR + " TEXT, "
    + TAG_COL_NOTIFY + " INTEGER )";

  private static final String CREATE_TABLE_PLACES = "CREATE TABLE " + TABLE_PLACES + " ( "
    + PLACE_COL_ID + " INTEGER PRIMARY KEY , " + PLACE_COL_NAME + " TEXT , " + PLACE_COL_TAG
    + " INTEGER , " + PLACE_COL_LAT + " INTEGER , " + PLACE_COL_LON + " INTEGER )";

  private static final String CREATE_TABLE_CONTENTS = "CREATE TABLE " + TABLE_CONTENTS + " ( "
    + CONTENT_COL_ID + " INTEGER PRIMARY KEY , " + CONTENT_COL_NAME + " TEXT , " + CONTENT_COL_TAG
    + " INTEGER , " + CONTENT_COL_START_DATE + " INTEGER , " + CONTENT_COL_END_DATE + " INTEGER )";

  private static final String CREATE_TABLE_PLACE_TAG = "CREATE TABLE " + TABLE_PLACES_TAGS + " ( "
    + PLACE_TAG_COL_PLACE + " INTEGER , " + PLACE_TAG_COL_TAGS + " INTEGER )";

  private static final String CREATE_TABLE_CONTENT_TAG = "CREATE TABLE " + TABLE_CONTENTS_TAGS
    + " ( " + CONTENT_TAG_COL_CONTENT + " INTEGER , " + PLACE_TAG_COL_TAGS + " INTEGER )";

  public DatabaseHelper(Context context, CursorFactory factory)
  {
    super(context, DB_NAME, factory, VERSION_BDD);
  }

  @Override
  public void onCreate(SQLiteDatabase db)
  {
    // db.execSQL("DROP TABLE IF EXISTS " + TABLE_TAGS);
    // db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLACES);
    // db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTENTS);
    // db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTENTS_TAGS);
    // db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLACES_TAGS);
    db.execSQL(CREATE_TABLE_TAGS);
    // db.execSQL(CREATE_TABLE_PLACES);
    // db.execSQL(CREATE_TABLE_CONTENTS);
    // db.execSQL(CREATE_TABLE_PLACE_TAG);
    // db.execSQL(CREATE_TABLE_CONTENT_TAG);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
  {
    db.execSQL("DROP TABLE " + TABLE_TAGS);
    onCreate(db);
  }
}
