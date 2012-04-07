package com.elance;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;

public class GetMacActivity extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		WifiManager wfManager;
		WifiInfo wifiinfo;
		wfManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		wifiinfo = wfManager.getConnectionInfo();
		String MAC = wifiinfo.getMacAddress();
		Log.e("mac", MAC);
	}
}