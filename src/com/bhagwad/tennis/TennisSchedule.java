package com.bhagwad.tennis;

import android.net.Uri;
import android.provider.BaseColumns;

public class TennisSchedule {
	
	public static final String AUTHORITY = "com.bhagwad.tennis.provider";
	
	private TennisSchedule() {}

	public static final class TennisScheduleColumns implements BaseColumns {
		
		private TennisScheduleColumns() {}
		
		public static final String MATCHUP_NAMES = "matchup_names";
		public static final String MATCHUP_DATE = "matchup_date";
		public static final String TOURNAMENT_NAME = "tournament_name";
		public static final String SORT_ORDER = "matchup_date ASC";
		
		public static final String GENDER = "gender";
		public static final String MEN= "men";
		public static final String WOMEN = "women";
		public static final String UPDATE_TIME = "update_time";
		public static final String PLAYER_NAME = "player_name";
		
		
		
		public static final Uri CONTENT_URI_MEN_SCHEDULE = Uri.parse("content://" + AUTHORITY + "/" + TennisScheduleProvider.MEN_TABLE);
		public static final Uri CONTENT_URI_WOMEN_SCHEDULE = Uri.parse("content://" + AUTHORITY + "/" + TennisScheduleProvider.WOMEN_TABLE);
		public static final Uri CONTENT_URI_LAST_UPDATED = Uri.parse("content://" + AUTHORITY + "/" + TennisScheduleProvider.LAST_UPDATED);
		public static final Uri CONTENT_URI_PLAYERS = Uri.parse("content://" + AUTHORITY + "/" + TennisScheduleProvider.PLAYERS);

		
		public static final String CONTENT_TYPE_ROW_MEN = "vnd.android.cursor.item/vnd.com.bhagwad.provider." + TennisScheduleProvider.MEN_TABLE;
		public static final String CONTENT_TYPE_DIR_MEN = "vnd.android.cursor.dir/vnd.com.bhagwad.provider." + TennisScheduleProvider.MEN_TABLE;
		
		public static final String CONTENT_TYPE_ROW_WOMEN = "vnd.android.cursor.item/vnd.com.bhagwad.provider." + TennisScheduleProvider.WOMEN_TABLE;
		public static final String CONTENT_TYPE_DIR_WOMEN = "vnd.android.cursor.dir/vnd.com.bhagwad.provider." + TennisScheduleProvider.WOMEN_TABLE;
		
		
	}
}