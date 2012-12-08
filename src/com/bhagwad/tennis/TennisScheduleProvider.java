package com.bhagwad.tennis;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import com.bhagwad.tennis.TennisSchedule.TennisScheduleColumns;

public class TennisScheduleProvider extends ContentProvider {
	
	private static final String DATABASE_NAME = "tennis.db";
	private static final int DATABASE_VERSION = 1;
	
	public static final String MEN_TABLE = "men_schedule";
	public static final String WOMEN_TABLE = "women_schedule";
	public static final String LAST_UPDATED = "last_updated";
	public static final String PLAYERS = "players";
	
	private static final UriMatcher sUriMatcher;
	
	private static final int SCHEDULE_MEN = 0;
	private static final int SCHEDULE_WOMEN = 1;
	private static final int UPDATED = 2;
	private static final int PLAYER_TABLE = 3;
	
	
	private static class DatabaseHelper extends SQLiteOpenHelper {
		
		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			
			db.execSQL("CREATE TABLE " + MEN_TABLE + " ("
					+ TennisScheduleColumns._ID + " INTEGER PRIMARY KEY, "
					+ TennisScheduleColumns.MATCHUP_NAMES + " TEXT, "
					+ TennisScheduleColumns.MATCHUP_DATE + " INTEGER, "
					+ TennisScheduleColumns.TOURNAMENT_NAME + " TEXT, "
					+ "UNIQUE ("+TennisScheduleColumns.MATCHUP_NAMES+","+TennisScheduleColumns.MATCHUP_DATE
					+ ","+TennisScheduleColumns.TOURNAMENT_NAME+") ON CONFLICT IGNORE"
					+ ");");
			
			db.execSQL("CREATE TABLE " + WOMEN_TABLE + " ("
					+ TennisScheduleColumns._ID + " INTEGER PRIMARY KEY, "
					+ TennisScheduleColumns.MATCHUP_NAMES + " TEXT, "
					+ TennisScheduleColumns.MATCHUP_DATE + " INTEGER, "
					+ TennisScheduleColumns.TOURNAMENT_NAME + " TEXT, "
					+ "UNIQUE ("+TennisScheduleColumns.MATCHUP_NAMES+","+TennisScheduleColumns.MATCHUP_DATE
					+ ","+TennisScheduleColumns.TOURNAMENT_NAME+") ON CONFLICT IGNORE"
					+ ");");
			
			db.execSQL("CREATE TABLE " + LAST_UPDATED + " ("
					+ TennisScheduleColumns._ID + " INTEGER PRIMARY KEY, "
					+ TennisScheduleColumns.GENDER + " TEXT, "
					+ TennisScheduleColumns.UPDATE_TIME + " INTEGER"
					+ ");");
			
			db.execSQL("INSERT INTO " + LAST_UPDATED + " ("
					+ TennisScheduleColumns.GENDER + "," + TennisScheduleColumns.UPDATE_TIME + ") VALUES" + 
					"('men', '0')");
			
			db.execSQL("INSERT INTO " + LAST_UPDATED + " ("
					+ TennisScheduleColumns.GENDER + "," + TennisScheduleColumns.UPDATE_TIME + ") VALUES" + 
					"('women', '0')");
			
			db.execSQL("CREATE TABLE " + PLAYERS + " ("
					+ TennisScheduleColumns._ID + " INTEGER PRIMARY KEY, "
					+ TennisScheduleColumns.PLAYER_NAME + " TEXT UNIQUE ON CONFLICT IGNORE"
					+ ");");
			
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			
			
		}
		
	}
	
	private DatabaseHelper mOpenHelper;
	
	@Override
	public int delete(Uri uri, String selection, String[] args) {
		
		String mTableName;
		
		switch(sUriMatcher.match(uri)) {
		
		case SCHEDULE_MEN:
			mTableName = MEN_TABLE;
			break;
		case SCHEDULE_WOMEN:
			mTableName = WOMEN_TABLE;
			break;
			
		default:
            throw new IllegalArgumentException("Unknown URI " + uri);
		
		}
		
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		long rowId = db.delete(mTableName, selection, null);
		
		return (int) rowId;
	}

	@Override
	public String getType(Uri uri) {
		
		switch (sUriMatcher.match(uri)) {
		
		case SCHEDULE_MEN:
			return TennisScheduleColumns.CONTENT_TYPE_DIR_MEN;
			
		case SCHEDULE_WOMEN:
			return TennisScheduleColumns.CONTENT_TYPE_DIR_WOMEN;
			
		default:
            throw new IllegalArgumentException("Unknown URI " + uri);
		
		}
		
		
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		
		ContentValues values;
		
		if (initialValues == null)
			values = new ContentValues();
		else
			values = new ContentValues(initialValues);
		
		String mTableName;
		
		switch(sUriMatcher.match(uri)) {
		
		case SCHEDULE_MEN:
			mTableName = MEN_TABLE;
			break;
		case SCHEDULE_WOMEN:
			mTableName = WOMEN_TABLE;
			break;
			
		case UPDATED:
			mTableName = LAST_UPDATED;
			break;
			
		case PLAYER_TABLE:
			mTableName = PLAYERS;
			
			break;
			
		default:
            throw new IllegalArgumentException("Unknown URI " + uri);
		
		}
		
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		long rowId = db.insertWithOnConflict(mTableName, TennisScheduleColumns._ID, values, SQLiteDatabase.CONFLICT_IGNORE);

		
		
		Uri mAddedUri = ContentUris.withAppendedId(uri, rowId);

		if (rowId > 0) {
			getContext().getContentResolver().notifyChange(mAddedUri, null);
		}

		return mAddedUri;

		//throw new SQLException("Failed to insert row into " + uri + " " + rowId);
		
	}

	@Override
	public boolean onCreate() {
		mOpenHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		
		switch (sUriMatcher.match(uri)) {
		
		case SCHEDULE_MEN:
			qb.setTables(MEN_TABLE);
			break;
		
		case SCHEDULE_WOMEN:
			qb.setTables(WOMEN_TABLE);
			break;
			
		case UPDATED:
			qb.setTables(LAST_UPDATED);
			break;
			
		case PLAYER_TABLE:
			qb.setTables(PLAYERS);
			break;
			

			
		default:
            throw new IllegalArgumentException("Unknown URI " + uri);
			
			
		}
		
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
		
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count;
		
		switch(sUriMatcher.match(uri)) {
		
			case UPDATED:
				
				count = db.update(LAST_UPDATED, contentValues, selection, selectionArgs);
				break;
				

				
			default:
	            throw new IllegalArgumentException("Unknown URI " + uri);
			
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
	
	static {
		
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(TennisSchedule.AUTHORITY, MEN_TABLE, SCHEDULE_MEN);
		sUriMatcher.addURI(TennisSchedule.AUTHORITY, WOMEN_TABLE, SCHEDULE_WOMEN);
		sUriMatcher.addURI(TennisSchedule.AUTHORITY, LAST_UPDATED, UPDATED);
		sUriMatcher.addURI(TennisSchedule.AUTHORITY, PLAYERS, PLAYER_TABLE);

		
		
		
	}

}
