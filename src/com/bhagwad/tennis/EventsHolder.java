package com.bhagwad.tennis;

import java.io.IOException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.webkit.WebView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class EventsHolder extends SherlockFragmentActivity {
	
	public static final int MEN_DISPLAYED = 0;
	public static final int WOMEN_DISPLAYED = 1;
	
	public static final String ACCOUNT_NAME_MATCHUPS = "Tennis Matches";
	public static final String ACCOUNT_NAME_TOURNAMENTS = "Tennis Tournaments";
	public static final String ACCOUNT_TYPE = "com.bhagwad.tennis.account";
	public static final String PROVIDER = "com.bhagwad.tennis.provider";
	
	public static final String BROADCAST_UPDATE = "com.bhagwad.tennis.updatewidgets";
	
	
	ProgressDialog pd = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setAccounts();
        
        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setTitle("Upcoming Matches");
        mActionBar.setDisplayShowTitleEnabled(true);
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
        Tab tab;
        
        tab = mActionBar.newTab();
        tab.setText("Men");
        tab.setTabListener(new TabListener<MenFragment>(this, "men", MenFragment.class, savedInstanceState));
        mActionBar.addTab(tab, true);
        
        tab = mActionBar.newTab();
        tab.setText("Women");
        tab.setTabListener(new TabListener<WomenFragment>(this, "women", WomenFragment.class, savedInstanceState));
        mActionBar.addTab(tab);
        
        tab = mActionBar.newTab();
        tab.setText("Events");
        tab.setTabListener(new TabListener<MenUpcoming>(this, "men_upcoming", MenUpcoming.class, savedInstanceState));
        mActionBar.addTab(tab);
        
    }
    
    @Override
    protected void onResume() {
    	
        
        // If a sync is already in progress, show a dialog till it ends, and then exit
		
		Account account = new Account(ACCOUNT_NAME_MATCHUPS, ACCOUNT_TYPE);

		if (ContentResolver.isSyncActive(account, TennisSchedule.AUTHORITY)) {
			showDialogUntilSyncEnds();
		
		}
    	
		super.onResume();
    }

    private void setAccounts() {
    	
    	// Set up two accounts. One for the matchups and one for the tournaments
		
    	AccountManager accountManager = AccountManager.get(getApplicationContext());
    	
    	Account appAccount = new Account(ACCOUNT_NAME_MATCHUPS, ACCOUNT_TYPE);
    	
    	if (accountManager.addAccountExplicitly(appAccount, null, null)) {
    	   
			Bundle extras = new Bundle();
			extras.putBoolean("dummy stuff", true);

			ContentResolver.addPeriodicSync(appAccount, PROVIDER, extras, 43200);
			ContentResolver.setIsSyncable(appAccount, PROVIDER, 1);
			ContentResolver.setSyncAutomatically(appAccount, PROVIDER, true);
    	}

    	
	}

	@Override
    public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
    	
    	MenuInflater mi = getSupportMenuInflater();
		mi.inflate(R.menu.action_bar, menu);
		return true;
    	
    }
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case R.id.menu_refresh:
			
			// Check the Internet connection
			
			if (!Utilities.isConnected(this)) {
				
				new AlertDialog.Builder(this)
				.setTitle("")
				.setMessage("Check your Internet settings")
				.setPositiveButton("Ok",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {

							}
						}).show();
				
				return true;
			}
				

			// If a sync is already in progress, show a dialog till it ends, and then exit
			
			Account account = new Account(ACCOUNT_NAME_MATCHUPS, ACCOUNT_TYPE);

			if (ContentResolver.isSyncActive(account, TennisSchedule.AUTHORITY)) {
				showDialogUntilSyncEnds();
				return true;
			}
			
			pd = ProgressDialog.show(this, "Getting Fresh Results", "");

			new SyncDatabase().execute();
			
		}

		return true;

	}
    
	private void showDialogUntilSyncEnds() {
		
		pd = ProgressDialog.show(this, "Getting Fresh Results", "");

		final Handler mHandler = new Handler();

		Runnable r = new Runnable() {

			@Override
			public void run() {

				Account account = new Account(ACCOUNT_NAME_MATCHUPS, ACCOUNT_TYPE);

				if (ContentResolver.isSyncActive(account,
						TennisSchedule.AUTHORITY))
					mHandler.postDelayed(this, 2000);
				else {

					if (pd.isShowing())
						pd.dismiss();

				}

			}
		};
		
		mHandler.post(r);

	}

	private class TabListener <T extends SherlockFragment> implements ActionBar.TabListener {
    	
        private final SherlockFragmentActivity mActivity;
        private final String mTag;
        private final Class<T> mClass;
        private Bundle mArgs;
        private SherlockFragment mFragment;
    	
        public TabListener(SherlockFragmentActivity activity, String tag, Class<T> clz, Bundle args) {
            mActivity = activity;
            mTag = tag;
            mClass = clz;
            mArgs = args;

            mFragment = (SherlockFragment) mActivity.getSupportFragmentManager().findFragmentByTag(mTag);

        }

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			
			if (mFragment == null) {
				
				mFragment = (SherlockFragment) SherlockFragment.instantiate(mActivity, mClass.getName(), mArgs);
				ft.add(android.R.id.content, mFragment, mTag);
			} else
				ft.attach(mFragment);
			
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			
			if (mFragment != null) {
                ft.detach(mFragment);
            }
			
		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
			
			
		}
    	
    }
    
	private class SyncDatabase  extends AsyncTask <Void, Void, String> {

		@Override
		protected String doInBackground(Void... args) {
			
			SherlockFragment mFragment;
			String mDisplayed = null;

			try {

				// Find out what fragment is displaying and execute the refresh only for that
				
				mFragment = (SherlockFragment) EventsHolder.this.getSupportFragmentManager().findFragmentByTag("men");

				if (mFragment != null && !mFragment.isDetached()) {
					mDisplayed = "men";
					Utilities.refreshSchedules(EventsHolder.this, MEN_DISPLAYED);
					return mDisplayed;
				}
				
				mFragment = (SherlockFragment) EventsHolder.this.getSupportFragmentManager().findFragmentByTag("women");

				if (mFragment != null && !mFragment.isDetached()) {
					mDisplayed = "women";
					Utilities.refreshSchedules(EventsHolder.this, WOMEN_DISPLAYED);
					return mDisplayed;
				}
				
					
			} catch (IOException e) {

				e.printStackTrace();
			}
			
			return null;
			
		}
		
		@Override
		protected void onPostExecute(String result) {
			
			if (pd.isShowing())
				pd.dismiss();
			
			if (result != null) {
				
				TextView t = (TextView) getSupportFragmentManager().findFragmentByTag(result).getView().findViewById(R.id.txt_lastupdated);
				Utilities.updateLastChanged(t, EventsHolder.this, result);
				
			}
			
			// Update the app widgets
			
			Intent i = new Intent();
			i.setAction(BROADCAST_UPDATE);
			sendBroadcast(i);
			 
		}
		
	}

	@Override
	public void onBackPressed() {
		
		 SherlockFragment mPage = (SherlockFragment) getSupportFragmentManager().findFragmentByTag("men_upcoming");
		 
	       if (mPage!= null && !mPage.isDetached()) {
	              
	    	   WebView mWebView = (WebView) mPage.getView().findViewById(R.id.webview_men_upcoming);
	    	   mWebView.goBack();
	    	   
	       } else
	       
	       super.onBackPressed();
	}
    
}
