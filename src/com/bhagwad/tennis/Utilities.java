package com.bhagwad.tennis;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.RemoteException;
import android.text.format.DateUtils;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.bhagwad.tennis.TennisSchedule.TennisScheduleColumns;

public class Utilities {
	
	private final static String ATP_SCHEDULE_URL = "http://www.wettpoint.com/schedules/tennis/atp/";
	private final static String WTA_SCHEDULE_URL = "http://www.wettpoint.com/schedules/tennis/wta/";
	
	public static void refreshSchedules(Context ctxt, int mDisplayedFragment) throws IOException {
		
		if (mDisplayedFragment == EventsHolder.MEN_DISPLAYED)
			refreshFromUrlAndUri(ATP_SCHEDULE_URL, TennisScheduleColumns.CONTENT_URI_MEN_SCHEDULE, ctxt);
		else
			refreshFromUrlAndUri(WTA_SCHEDULE_URL, TennisScheduleColumns.CONTENT_URI_WOMEN_SCHEDULE, ctxt);
			

	}

	private static void refreshFromUrlAndUri(String atpScheduleUrl, Uri contentUriSchedule, Context ctxt) throws IOException {
		
		if (!hasChanged(atpScheduleUrl, contentUriSchedule, ctxt))
			return;
		
		Document doc = Jsoup.connect(atpScheduleUrl).get();
		

		// Find the appropriate rows that are characterized by their background
		Elements mRows = doc.select("tr[bgcolor~=#E1EBF0|#DAE6EB]");

		Iterator<Element> mRowIterator = mRows.iterator();

		SimpleDateFormat mDateFormat = new SimpleDateFormat(
				"dd/MM/yy, kk:mm Z");
		Date mDate = new Date();

		// Start the batch operation
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		ops.add(ContentProviderOperation.newDelete(contentUriSchedule)
				.withSelection("1", null).build());

		while (mRowIterator.hasNext()) {
			Element mCurrentRow = mRowIterator.next();

			// Get the list of <td> elements inside each row

			Elements mColumns = mCurrentRow.getElementsByTag("td");

			// We need to extract the date and time properly and make sure it's
			// GMT +2
			try {
				mDate = mDateFormat.parse(mColumns.eq(0).text() + " +0200");
			} catch (ParseException e) {

				e.printStackTrace();
			}

			ContentValues cv = new ContentValues();
			cv.put(TennisScheduleColumns.MATCHUP_DATE, mDate.getTime());
			cv.put(TennisScheduleColumns.MATCHUP_NAMES, mColumns.eq(1).text());
			cv.put(TennisScheduleColumns.TOURNAMENT_NAME, mColumns.eq(2).text());
			
			// Extract and insert the names into the players database
			Utilities.insertNames(mColumns.eq(1).text(), ops);

			ops.add(ContentProviderOperation.newInsert(contentUriSchedule)
					.withValues(cv).build());

		}
		
		// Add the last updated time into the table
		
		insertRefreshTime(ops, contentUriSchedule);
		

		try {
			ctxt.getContentResolver().applyBatch(TennisSchedule.AUTHORITY, ops);
		} catch (RemoteException e) {

			e.printStackTrace();
		} catch (OperationApplicationException e) {

			e.printStackTrace();
		}

	}

	// Check if the document has been modified since
	
	private static boolean hasChanged(String atpScheduleUrl, Uri contentUriSchedule, Context ctxt) {

		String gender;
		
		if (contentUriSchedule.toString().equals(TennisScheduleColumns.CONTENT_URI_MEN_SCHEDULE))
			
			gender = "men";
		else
			gender = "women";
		
		try {
			
			HttpURLConnection connection;	
			// Get last modified date
			connection = (HttpURLConnection) new URL(atpScheduleUrl).openConnection();
			long mUrlChangeddate = connection.getLastModified();
			connection.disconnect();
			// Get latest date we have on record for the given URI
			
			long mUpdatedDate = getLastChangedDate(ctxt, gender);
			
			if (mUrlChangeddate > mUpdatedDate) 
				return true;
			else 
				return false;
			
			
		} catch (MalformedURLException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}

		
		return false;
	}

	private static String[] insertNames(String text, ArrayList<ContentProviderOperation> ops) {
		
		String mNames[] = text.split("-");
		
		for (int i = 0; i<mNames.length; i++) {
			
			if (mNames[i].contains("Unknown"))
				continue;
			
			mNames[i] = mNames[i].trim();
			
			
			ContentValues cv = new ContentValues();
			cv.put(TennisScheduleColumns.PLAYER_NAME, mNames[i]);
			
			ops.add(ContentProviderOperation.newInsert(TennisScheduleColumns.CONTENT_URI_PLAYERS).withValues(cv).build());
		}
		
		return null;
	}

	private static void insertRefreshTime(ArrayList<ContentProviderOperation> ops, Uri contentUriSchedule) {
		
		String selectedGender;
		if (contentUriSchedule == TennisScheduleColumns.CONTENT_URI_MEN_SCHEDULE)
			selectedGender = TennisScheduleColumns.MEN;
		else
			selectedGender = TennisScheduleColumns.WOMEN;
		
		ContentValues cv = new ContentValues();
		
		cv.put(TennisScheduleColumns.UPDATE_TIME, Long.valueOf(System.currentTimeMillis()));
		ops.add(ContentProviderOperation.newUpdate(TennisScheduleColumns.CONTENT_URI_LAST_UPDATED).withSelection(TennisScheduleColumns.GENDER + "='" + selectedGender+"'", null).withValues(cv).build());
		
		
		
	}

	public static void updateLastChanged(TextView v, SherlockFragmentActivity sherlockActivity, String gender) {


		long mDate = getLastChangedDate(sherlockActivity, gender);

			if (mDate != 0) {
				
				Date d = new Date(mDate);
				
				String elapsedTime = (String) DateUtils
						.getRelativeDateTimeString(sherlockActivity,
								d.getTime(),
								DateUtils.MINUTE_IN_MILLIS,
								DateUtils.WEEK_IN_MILLIS, 0);
				
				v.setText("Last Updated: " + elapsedTime);
			} else
				v.setText("Last Updated: Never");

		

	}

	
	private static long getLastChangedDate(
			Context ctxt, String gender) {
		
		Cursor c = ctxt.getContentResolver().query(
				TennisScheduleColumns.CONTENT_URI_LAST_UPDATED,
				new String[] { TennisScheduleColumns.UPDATE_TIME },
				TennisScheduleColumns.GENDER + "=?", new String[] { gender },
				null);
		
		if (c.moveToFirst())
			return c.getLong(c.getColumnIndex(TennisScheduleColumns.UPDATE_TIME));
		
		else
			return 0;
		
	}

	public static boolean isConnected(Context ctxt) {
		
		ConnectivityManager connManager = (ConnectivityManager) ctxt
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		// No network service
		NetworkInfo networkInfo = connManager.getActiveNetworkInfo();

		if (networkInfo == null
				|| !connManager.getActiveNetworkInfo().isConnected()) {
			return false;
		}
		
		return true;
		
	}

	public static void updateWidget(Context ctxt, int mAppWidgetId, String text) {
		

		// Let's get the first result of whatever the guy wants.
		
		Cursor mTwoCusors[] = new Cursor[2];
		
		mTwoCusors[0] = ctxt.getContentResolver().query(TennisScheduleColumns.CONTENT_URI_MEN_SCHEDULE, null, TennisScheduleColumns.MATCHUP_NAMES + " LIKE ?", new String [] {"%"+text+"%"}, null);
		mTwoCusors[1] = ctxt.getContentResolver().query(TennisScheduleColumns.CONTENT_URI_WOMEN_SCHEDULE, null, TennisScheduleColumns.MATCHUP_NAMES + " LIKE ?", new String [] {"%"+text+"%"}, null);
		
		MergeCursor mMerged = new MergeCursor(mTwoCusors);
		
		String mMatchup = "";
		String mDate = "";
		String mTournament = "";
		
		AppWidgetManager mAppWidgetManager = AppWidgetManager.getInstance(ctxt);
		RemoteViews view = new RemoteViews(ctxt.getPackageName(), R.layout.widget_layout);
		
		Intent i = new Intent(ctxt, EventsHolder.class);
		PendingIntent pi = PendingIntent.getActivity(ctxt, 0, i, 0);
		
		if (mMerged.moveToFirst()) {
			mMatchup = mMerged.getString(mMerged.getColumnIndexOrThrow(TennisScheduleColumns.MATCHUP_NAMES));
			
			Date d = new Date(mMerged.getLong(mMerged.getColumnIndexOrThrow(TennisScheduleColumns.MATCHUP_DATE)));
			mDate = Utilities.formatDate(d);
			
			mTournament = mMerged.getString(mMerged.getColumnIndexOrThrow(TennisScheduleColumns.TOURNAMENT_NAME));
			
		} else {
			
			if (text == null)
				text = "";
			
			mMatchup = "No upcoming matches for '" + text + "'";
			mDate = "";
			mTournament = "";
			
		}
		
		mMerged.close();
		
		// Make sure that the widget id, text is in the shared preferences
		
		saveTheName(ctxt, mAppWidgetId, text);
		

		
		view.setTextViewText(R.id.txt_widget_matchup, mMatchup);
		view.setTextViewText(R.id.txt_widget_date, mDate);
		view.setTextViewText(R.id.txt_widget_tournament, mTournament);
		
		view.setOnClickPendingIntent(R.id.widget_container, pi);
		mAppWidgetManager.updateAppWidget(mAppWidgetId, view);
		
		
		
	}

	private static void saveTheName(Context ctxt, int mAppWidgetId, String text) {
		
		SharedPreferences.Editor prefs = ctxt.getSharedPreferences(WidgetConfiguration.PREFS, 0).edit();
		prefs.putString(WidgetConfiguration.PREFS_PREFIX_KEY+mAppWidgetId, text);
		prefs.commit();
		
	}

	public static String formatDate(Date d) {
		
		SimpleDateFormat mDateFormatter = new SimpleDateFormat("K:mm a E dd MMM ");
		
		return mDateFormatter.format(d);
	}

}
