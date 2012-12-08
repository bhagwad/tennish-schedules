package com.bhagwad.tennis;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.actionbarsherlock.app.SherlockFragment;

public class MenUpcoming extends SherlockFragment {
	
	//public static final String MEN_UPCOMING = "http://www.atpworldtour.com/Tournaments/Event-Calendar.aspx";
	public static final String MEN_UPCOMING = "http://espn.go.com/tennis/schedule";
	
	ProgressDialog pd = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View v = inflater.inflate(R.layout.upcoming_tournaments_men, container, false);
		
		if (!Utilities.isConnected(getActivity())) {
			
			new AlertDialog.Builder(getActivity())
			.setTitle("")
			.setMessage("Check your Internet settings")
			.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int which) {

						}
					}).show();
			
			return v;
		}
		
		WebView mUpcomingTournaments = (WebView) v.findViewById(R.id.webview_men_upcoming);
		
		mUpcomingTournaments.setWebChromeClient(new WebChromeClient() {
			
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				
				if (pd == null || !pd.isShowing())
					return;
				
				pd.setProgress(newProgress);
				
						
			}
			
		});
		
		mUpcomingTournaments.setWebViewClient(new WebViewClient() {
			
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				
				// Set up the progress thing
				
				pd = new ProgressDialog(getActivity());
				pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				pd.setMessage("Loading...");
				pd.show();
			}
			
			@Override
			public void onPageFinished(WebView view, String url) {
				
				if (pd == null || !pd.isShowing())
					return;
				
				pd.dismiss();
				
			}
			
		});
		
		mUpcomingTournaments.loadUrl(MEN_UPCOMING);
		
		
		return v;
		
	}



}
