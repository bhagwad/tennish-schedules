/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.bhagwad.tennis.syncadapter;

import java.io.IOException;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;

import com.bhagwad.tennis.EventsHolder;
import com.bhagwad.tennis.Utilities;

/**
 * SyncAdapter implementation for syncing sample SyncAdapter contacts to the
 * platform ContactOperations provider.  This sample shows a basic 2-way
 * sync between the client and a sample server.  It also contains an
 * example of how to update the contacts' status messages, which
 * would be useful for a messaging or social networking client.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

	private final Context mContext;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
    
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
        ContentProviderClient provider, SyncResult syncResult) {
    	
    	if (account.name.equals(EventsHolder.ACCOUNT_NAME_MATCHUPS)) {
    		
    		try {
    			
        		Utilities.refreshSchedules(mContext, com.bhagwad.tennis.EventsHolder.MEN_DISPLAYED);
    			Utilities.refreshSchedules(mContext, com.bhagwad.tennis.EventsHolder.WOMEN_DISPLAYED);
    			
    			Intent i = new Intent();
    			i.setAction(EventsHolder.BROADCAST_UPDATE);
    			getContext().sendBroadcast(i);
    			
    		} catch (IOException e) {
    			
    			syncResult.databaseError = true;
    			e.printStackTrace();
    		}
    		
    	}
    	
    	
    	
    }

    /**
     * This helper function fetches the last known high-water-mark
     * we received from the server - or 0 if we've never synced.
     * @param account the account we're syncing
     * @return the change high-water-mark
     */
}

