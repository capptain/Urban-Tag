package com.ubikod.urbantag;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper
{
  public static final int VERSION_BDD = 3;
  public static final String DB_NAME = "urbantag.db";

  public static final String TABLE_TAGS = "tags";
  public static int NUM_COL_ID = 0;
  public static final String COL_ID = "id";
  public static int NUM_COL_TAG_NAME = 1;
  public static final String COL_TAG_NAME = "tag_name";
  public static int NUM_COL_COLOR = 2;
  public static final String COL_COLOR = "color";
  public static int NUM_COL_NOTIFY = 3;
  public static final String COL_NOTIFY = "notify";

  private static final String CREATE_BDD = "CREATE TABLE " + TABLE_TAGS + " (" + COL_ID
    + " INTEGER PRIMARY KEY , " + COL_TAG_NAME + " INTEGER, " + COL_COLOR + " TEXT, " + COL_NOTIFY
    + " INTEGER)";

  public DatabaseHelper(Context context, CursorFactory factory)
  {
    super(context, DB_NAME, factory, VERSION_BDD);
  }

  @Override
  public void onCreate(SQLiteDatabase db)
  {
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_TAGS);
    db.execSQL(CREATE_BDD);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
  {
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_TAGS);
    onCreate(db);
  }

}
