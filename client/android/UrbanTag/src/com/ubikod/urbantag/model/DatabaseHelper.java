package com.ubikod.urbantag.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper
{
  public static final int VERSION_BDD = 3;
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
  public static final int CONTENT_NUM_COL_PLACE = 3;
  public static final String CONTENT_COL_PLACE = "place_id";
  public static final int CONTENT_NUM_COL_START_DATE = 4;
  public static final String CONTENT_COL_START_DATE = "start_date";
  public static final int CONTENT_NUM_COL_END_DATE = 5;
  public static final String CONTENT_COL_END_DATE = "end_date";

  // Place - Tag pivot
  public static final String TABLE_PLACES_TAGS = "places_tags";
  public static final int PLACE_TAG_NUM_COL_PLACE = 0;
  public static final String PLACE_TAG_COL_PLACE = "place_id";
  public static final int PLACE_TAG_NUM_COL_TAG = 1;
  public static final String PLACE_TAG_COL_TAG = "tag_id";

  // Content - Tag pivot
  public static String TABLE_CONTENTS_TAGS = "contents_tags";
  public static int CONTENT_TAG_NUM_COL_CONTENT = 0;
  public static String CONTENT_TAG_COL_CONTENT = "content_id";
  public static int CONTENT_TAG_NUM_COL_TAG = 1;
  public static String CONTENT_TAG_COL_TAG = "tag_id";

  // Tables
  private static final String CREATE_TABLE_TAGS = "CREATE TABLE " + TABLE_TAGS + " ( " + TAG_COL_ID
    + " INTEGER PRIMARY KEY , " + TAG_COL_NAME + " TEXT, " + TAG_COL_COLOR + " TEXT, "
    + TAG_COL_NOTIFY + " INTEGER )";

  private static final String CREATE_TABLE_PLACES = "CREATE TABLE " + TABLE_PLACES + " ( "
    + PLACE_COL_ID + " INTEGER PRIMARY KEY , " + PLACE_COL_NAME + " TEXT , " + PLACE_COL_TAG
    + " INTEGER , " + PLACE_COL_LAT + " INTEGER , " + PLACE_COL_LON + " INTEGER, FOREIGN KEY ("
    + PLACE_COL_TAG + ") REFERENCES " + TABLE_TAGS + " (" + TAG_COL_ID + "));";

  private static final String CREATE_TABLE_CONTENTS = "CREATE TABLE " + TABLE_CONTENTS + " ( "
    + CONTENT_COL_ID + " INTEGER PRIMARY KEY , " + CONTENT_COL_NAME + " TEXT , " + CONTENT_COL_TAG
    + " INTEGER , " + CONTENT_COL_PLACE + " INTEGER , " + CONTENT_COL_START_DATE + " INTEGER , "
    + CONTENT_COL_END_DATE + " INTEGER , FOREIGN KEY (" + CONTENT_COL_TAG + ") REFERENCES "
    + TABLE_TAGS + " (" + TAG_COL_ID + "), FOREIGN KEY (" + CONTENT_COL_PLACE + ") REFERENCES "
    + TABLE_PLACES + " (" + PLACE_COL_ID + "));";

  private static final String CREATE_TABLE_PLACE_TAG = "CREATE TABLE " + TABLE_PLACES_TAGS + " ( "
    + PLACE_TAG_COL_PLACE + " INTEGER , " + PLACE_TAG_COL_TAG + " INTEGER , FOREIGN KEY ("
    + PLACE_TAG_COL_TAG + ") REFERENCES " + TABLE_TAGS + " (" + TAG_COL_ID + "), FOREIGN KEY ("
    + PLACE_TAG_COL_PLACE + ") REFERENCES " + TABLE_PLACES + "(" + PLACE_COL_ID + "), PRIMARY KEY("
    + PLACE_TAG_COL_PLACE + "," + PLACE_TAG_COL_TAG + "));";

  private static final String CREATE_TABLE_CONTENT_TAG = "CREATE TABLE " + TABLE_CONTENTS_TAGS
    + " ( " + CONTENT_TAG_COL_CONTENT + " INTEGER , " + CONTENT_TAG_COL_TAG
    + " INTEGER , FOREIGN KEY (" + CONTENT_TAG_COL_TAG + ") REFERENCES " + TABLE_TAGS + " ("
    + TAG_COL_ID + "), FOREIGN KEY (" + CONTENT_TAG_COL_CONTENT + ") REFERENCES " + TABLE_CONTENTS
    + "(" + CONTENT_COL_ID + "), PRIMARY KEY (" + CONTENT_TAG_COL_CONTENT + ","
    + CONTENT_TAG_COL_TAG + "));";

  // Triggers for constraints
  private static final String CONSTRAINT_PLACE_TAG = "fk_placetag_tagid";
  private static final String CONSTRAINT_PLACE_IN_PLACE_TAG = "fk_placeid_placeid1";
  private static final String CONSTRAINT_TAG_IN_PLACE_TAG = "fk_tagid_tagid1";
  private static final String CONSTRAINT_CONTENT_TAG = "fk_contenttag_tagid";
  private static final String CONSTRAINT_CONTENT_IN_CONTENT_TAG = "fk_contentid_contentid";
  private static final String CONSTRAINT_TAG_IN_CONTENT_TAG = "fk_tagid_tagid2";
  private static final String CONSTRAINT_CONTENT_PLACE = "fk_placeid_placeid2";

  private static final String CREATE_TRIGGER_TAG_IN_PLACE = "CREATE TRIGGER "
    + CONSTRAINT_PLACE_TAG + " BEFORE INSERT ON " + TABLE_PLACES + " FOR EACH ROW BEGIN"
    + " SELECT CASE WHEN " + "((SELECT " + TAG_COL_ID + " FROM " + TABLE_TAGS + " WHERE "
    + TAG_COL_ID + "=new." + PLACE_COL_TAG + " ) IS NULL)"
    + "THEN RAISE (ABORT, 'Foreign Key Violation') END;" + "END;";

  private static final String CREATE_TRIGGER_TAG_IN_PLACE_TAG = "CREATE TRIGGER "
    + CONSTRAINT_TAG_IN_PLACE_TAG + " BEFORE INSERT ON " + TABLE_PLACES_TAGS
    + " FOR EACH ROW BEGIN" + " SELECT CASE WHEN " + "((SELECT " + TAG_COL_ID + " FROM "
    + TABLE_TAGS + " WHERE " + TAG_COL_ID + "=new." + PLACE_TAG_COL_TAG + " ) IS NULL)"
    + "THEN RAISE (ABORT, 'Foreign Key Violation') END; END;";

  private static final String CREATE_TRIGGER_PLACE_PLACE_TAG = "CREATE TRIGGER "
    + CONSTRAINT_PLACE_IN_PLACE_TAG + " BEFORE INSERT ON " + TABLE_PLACES_TAGS
    + " FOR EACH ROW BEGIN" + " SELECT CASE WHEN " + "((SELECT " + PLACE_COL_ID + " FROM "
    + TABLE_PLACES + " WHERE " + PLACE_COL_ID + "=new." + PLACE_TAG_COL_PLACE + " ) IS NULL)"
    + "THEN RAISE (ABORT, 'Foreign Key Violation') END; END;";

  private static final String CREATE_TRIGGER_TAG_IN_CONTENT = "CREATE TRIGGER "
    + CONSTRAINT_CONTENT_TAG + " BEFORE INSERT ON " + TABLE_CONTENTS + " FOR EACH ROW BEGIN"
    + " SELECT CASE WHEN " + "((SELECT " + TAG_COL_ID + " FROM " + TABLE_TAGS + " WHERE "
    + TAG_COL_ID + "=new." + CONTENT_COL_TAG + " ) IS NULL)"
    + "THEN RAISE (ABORT, 'Foreign Key Violation') END;" + "END;";

  private static final String CREATE_TRIGGER_CONTENT_IN_CONTENT_TAG = "CREATE TRIGGER "
    + CONSTRAINT_CONTENT_IN_CONTENT_TAG + " BEFORE INSERT ON " + TABLE_CONTENTS_TAGS
    + " FOR EACH ROW BEGIN" + " SELECT CASE WHEN " + "((SELECT " + CONTENT_COL_ID + " FROM "
    + TABLE_CONTENTS + " WHERE " + CONTENT_COL_ID + "=new." + CONTENT_TAG_COL_CONTENT
    + " ) IS NULL)" + "THEN RAISE (ABORT, 'Foreign Key Violation') END; END;";

  private static final String CREATE_TRIGGER_TAG_IN_CONTENT_TAG = "CREATE TRIGGER "
    + CONSTRAINT_TAG_IN_CONTENT_TAG + " BEFORE INSERT ON " + TABLE_CONTENTS_TAGS
    + " FOR EACH ROW BEGIN" + " SELECT CASE WHEN " + "((SELECT " + TAG_COL_ID + " FROM "
    + TABLE_TAGS + " WHERE " + TAG_COL_ID + "=new." + CONTENT_TAG_COL_TAG + " ) IS NULL)"
    + "THEN RAISE (ABORT, 'Foreign Key Violation') END; END;";

  private static final String CREATE_TRIGGER_PLACE_IN_CONTENT = "CREATE TRIGGER "
    + CONSTRAINT_CONTENT_PLACE + " BEFORE INSERT ON " + TABLE_CONTENTS_TAGS + " FOR EACH ROW BEGIN"
    + " SELECT CASE WHEN " + "((SELECT " + PLACE_COL_ID + " FROM " + TABLE_PLACES + " WHERE "
    + PLACE_COL_ID + "=new." + CONTENT_COL_PLACE + " ) IS NULL)"
    + "THEN RAISE (ABORT, 'Foreign Key Violation') END; END;";

  public DatabaseHelper(Context context, CursorFactory factory)
  {
    super(context, DB_NAME, factory, VERSION_BDD);
  }

  @Override
  public void onCreate(SQLiteDatabase db)
  {
    db.execSQL(CREATE_TABLE_TAGS);
    db.execSQL(CREATE_TABLE_PLACES);
    db.execSQL(CREATE_TABLE_CONTENTS);
    db.execSQL(CREATE_TABLE_PLACE_TAG);
    db.execSQL(CREATE_TABLE_CONTENT_TAG);
    db.execSQL(CREATE_TRIGGER_TAG_IN_PLACE);
    db.execSQL(CREATE_TRIGGER_TAG_IN_CONTENT);
    db.execSQL(CREATE_TRIGGER_PLACE_IN_CONTENT);
    db.execSQL(CREATE_TRIGGER_PLACE_PLACE_TAG);
    db.execSQL(CREATE_TRIGGER_TAG_IN_PLACE_TAG);
    db.execSQL(CREATE_TRIGGER_TAG_IN_CONTENT_TAG);
    db.execSQL(CREATE_TRIGGER_CONTENT_IN_CONTENT_TAG);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
  {
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_TAGS);
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLACES);
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTENTS);
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLACES_TAGS);
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTENTS_TAGS);
    db.execSQL("DROP TRIGGER IF EXISTS " + CONSTRAINT_PLACE_TAG);
    db.execSQL("DROP TRIGGER IF EXISTS " + CONSTRAINT_CONTENT_TAG);
    db.execSQL("DROP TRIGGER IF EXISTS " + CONSTRAINT_CONTENT_PLACE);
    db.execSQL("DROP TRIGGER IF EXISTS " + CONSTRAINT_TAG_IN_CONTENT_TAG);
    db.execSQL("DROP TRIGGER IF EXISTS " + CONSTRAINT_CONTENT_IN_CONTENT_TAG);
    db.execSQL("DROP TRIGGER IF EXISTS " + CONSTRAINT_TAG_IN_PLACE_TAG);
    db.execSQL("DROP TRIGGER IF EXISTS " + CONSTRAINT_PLACE_IN_PLACE_TAG);

    onCreate(db);
  }
}
