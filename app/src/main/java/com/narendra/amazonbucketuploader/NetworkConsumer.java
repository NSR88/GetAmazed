package com.narendra.amazonbucketuploader;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkConsumer {

	Context _context;

	public NetworkConsumer(Context _ctxt) {
		// TODO Auto-generated constructor stub
		this._context = _ctxt;
	}
	
	public boolean isNetworkAvailable()
	{
		ConnectivityManager connectivityManager = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetworkInfo != null && activeNetworkInfo.isAvailable() && activeNetworkInfo.isConnectedOrConnecting()) 
		{
			return true;
		}
		return false;
	}
}
