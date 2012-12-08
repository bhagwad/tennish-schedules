package com.bhagwad.tennis.syncadapter;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AuthenticatorService extends Service {
	
	private AccountAuthenticator mAuthenticator;
	
	@Override
    public void onCreate() {
		
		if (mAuthenticator == null)		
			mAuthenticator = new AccountAuthenticator(this);
    }

	@Override
	public IBinder onBind(Intent arg0) {
		
		return mAuthenticator.getIBinder();
	}

}
