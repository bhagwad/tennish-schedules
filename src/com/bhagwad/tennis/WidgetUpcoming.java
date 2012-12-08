package com.bhagwad.tennis;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class WidgetUpcoming extends AppWidgetProvider {
	
	private static String WIDGET_ID = "widget_id";
	private static String WIDGET_TEXT = "widget_text";
	
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		if (intent.getAction() == EventsHolder.BROADCAST_UPDATE) {

			AppWidgetManager mgr = AppWidgetManager.getInstance(context);
			ComponentName cmpName = new ComponentName(context.getPackageName(), WidgetUpcoming.class.getName());

			onUpdate(context, mgr, mgr.getAppWidgetIds(cmpName));
		} else
			super.onReceive(context, intent);
	}
	

	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		
		// Start the service for each widget that has to be updated
		
		for (int i = 0; i < appWidgetIds.length; i++) {
			
			int id = appWidgetIds[i];
			
			// Get the text stored for each widget
			SharedPreferences prefs = context.getSharedPreferences(WidgetConfiguration.PREFS, 0);
			String text = prefs.getString(WidgetConfiguration.PREFS_PREFIX_KEY+id, null);
			
			Intent mIntent = new Intent (context, UpcomingGames.class);
			mIntent.putExtra(WIDGET_ID, id);
			mIntent.putExtra(WIDGET_TEXT, text);
			
			context.startService(mIntent);
			
			
		}
		
	}
	
	public static class UpcomingGames extends IntentService {
		
		

		public UpcomingGames() {
			super("WidgetUpcoming$UpcomingGames");
			
		}

		@Override
		protected void onHandleIntent(Intent intent) {
			
			Bundle bundle = intent.getExtras();
			
			
			if (bundle != null) {
				
				int id = bundle.getInt(WIDGET_ID);
				String text = bundle.getString(WIDGET_TEXT);
				
				Utilities.updateWidget(this, id, text);
				
				
				
			}
			
		}
		
	}

}
