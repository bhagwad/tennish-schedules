package com.bhagwad.tennis;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.CursorToStringConverter;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FilterQueryProvider;

import com.bhagwad.tennis.TennisSchedule.TennisScheduleColumns;


public class WidgetConfiguration extends FragmentActivity implements OnClickListener {
	
	Button mSaveWidget;
	AutoCompleteTextView mPlayerName;
	int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	String mSelection ="";
	SimpleCursorAdapter mAdapter;
	Cursor c;
	
	
	public static String PREFS = "com.bhagwad.tennis.appwidget";
	public static final String PREFS_PREFIX_KEY = "prefix_";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.widget_configuration);
		
		// If something goes wrong, this screen should be cancelled
		setResult(RESULT_CANCELED);
		
		
		
		mSaveWidget = (Button) findViewById(R.id.button_save_widget);
		mSaveWidget.setOnClickListener(this);
		mPlayerName = (AutoCompleteTextView) findViewById(R.id.edit_filter);
		
		setWidgetId();

		mPlayerName.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

				if (!s.equals(""))
					mSelection = s.toString();
				else
					mSelection = "";
				
				mAdapter.getFilter().filter(mSelection);
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				
				
			}
		});
		
		// Set up the adapter
		
		mAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, null, new String[] {TennisScheduleColumns.PLAYER_NAME}, new int[] {android.R.id.text1}, 0);
		mAdapter.setCursorToStringConverter(new CursorToStringConverter() {
			
			@Override
			public CharSequence convertToString(Cursor c) {
				
				return c.getString(c.getColumnIndexOrThrow(TennisScheduleColumns.PLAYER_NAME)); 
				
			}
		});
		
		// Create the filter to be applied when we called adapter.getfilter like above		
		mAdapter.setFilterQueryProvider(new FilterQueryProvider() {
			
			@Override
			public Cursor runQuery(CharSequence constraint) {
				return getContentResolver().query(TennisScheduleColumns.CONTENT_URI_PLAYERS, new String[] {TennisScheduleColumns._ID, TennisScheduleColumns.PLAYER_NAME}, 
						TennisScheduleColumns.PLAYER_NAME + " LIKE ?", new String[] {"%"+constraint+"%"}, null);
				
			}
		});
		
		mPlayerName.setAdapter(mAdapter);
		
		
		
	}

	private void setWidgetId() {
		
		
		Bundle extras = getIntent().getExtras();
		
		// Extract the widget Id we'll need
		
		if (extras != null) {
			mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		}
		
        // If they gave us an intent without the widget id, just bail.
		
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }
		
	}

	@Override
	public void onClick(View v) {
		
		Utilities.updateWidget(this, mAppWidgetId, mPlayerName.getText().toString());
		
		Intent resultValue = new Intent();
		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
		setResult(RESULT_OK, resultValue);
		finish();
	}

}
