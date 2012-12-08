package com.bhagwad.tennis;

import java.util.Date;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.bhagwad.tennis.TennisSchedule.TennisScheduleColumns;

public class WomenFragment extends SherlockFragment implements LoaderCallbacks<Cursor>  {
	
	SimpleCursorAdapter mAdapter;
	ListView mListViewWomen;
	String mSelection = "";
	EditText mSearchText;
	TextView mLastUpdated;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View v = inflater.inflate(R.layout.men, container, false);
		
		mListViewWomen = (ListView) v.findViewById(R.id.listview_men);
		mSearchText = (EditText)v.findViewById(R.id.search_men);
		mLastUpdated = (TextView) v.findViewById(R.id.txt_lastupdated);
		
		Utilities.updateLastChanged(mLastUpdated, getSherlockActivity(), "women");
		
	
			
		
		mSearchText.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				  
				if (!s.equals(""))
					mSelection = s.toString();
				else
					mSelection = "";
				
				getLoaderManager().restartLoader(0, null, WomenFragment.this);
				
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				
			}
		});
		
		String columns[] = {TennisScheduleColumns.MATCHUP_DATE, TennisScheduleColumns.MATCHUP_NAMES, TennisScheduleColumns.TOURNAMENT_NAME};
		mAdapter = new SimpleCursorAdapter(getSherlockActivity(), R.layout.matchup_list, null, columns, new int[] {R.id.txt_matchuptime, R.id.txt_matchup_name, R.id.txt_tournament}, 0);
		
		mAdapter.setViewBinder(new ViewBinder() {
			
			@Override
			public boolean setViewValue(View v, Cursor c, int columnIndex) {
				
				if (columnIndex == c.getColumnIndex(TennisScheduleColumns.MATCHUP_DATE)) {
					
					TextView mTxtDate = (TextView) v;
					
					// Convert the date from milliseconds into a date and display it
					
					Date d = new Date(c.getLong(columnIndex));
					
					String mDateText = Utilities.formatDate(d);
					
					mTxtDate.setText(mDateText);
					
					return true;
					
				}
				
				return false;
				
			}
		});
		
		mListViewWomen.setAdapter(mAdapter);
		
		getLoaderManager().initLoader(0, null, this);
		
	
		
		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		
		return new CursorLoader(getSherlockActivity(), TennisScheduleColumns.CONTENT_URI_WOMEN_SCHEDULE, null, TennisScheduleColumns.MATCHUP_NAMES + " LIKE ? OR " + TennisScheduleColumns.TOURNAMENT_NAME + " LIKE ?", new String [] {"%"+mSelection+"%", "%"+mSelection+"%"}, null);

	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mAdapter.swapCursor(data);
		
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		mAdapter.swapCursor(null);
		
	}
	
}
