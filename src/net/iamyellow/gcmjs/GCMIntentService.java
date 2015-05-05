//
//   Copyright 2013 jordi domenech <http://iamyellow.net, jordi@iamyellow.net>
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//

package net.iamyellow.gcmjs;

import java.util.HashMap;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiC;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import android.support.v4.app.NotificationCompat;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.Service;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.json.JSONObject;

public class GCMIntentService extends IntentService 
{
	private static final String TAG = "GCMIntentService";

	public static final int NOTIFICATION_ID = 1;
	NotificationCompat.Builder builder;

	public GCMIntentService() {
		super(GCMIntentService.class.getSimpleName());
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		String messageType = gcm.getMessageType(intent);
 
		if (!extras.isEmpty()) {
			if (messageType == null) {
				GcmjsModule.logd(TAG+": messageType is null");
				GcmjsModule.logd(TAG+": ----------------:"+ extras.toString());
			}
			else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
				
				GcmjsModule.logd(TAG+": deleted");
				
			}
			else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
				//
				// Push Notification Received
				//

				// Generate Data 
				GcmjsModule.logd(TAG+": extras.toString():"+ extras.toString());
				HashMap<String, Object> jsonData = new HashMap<String, Object>();
				for (String key : extras.keySet()) {
					if (extras.get(key) != null && !"".equals(extras.get(key))) {
						jsonData.put(key, extras.get(key));
					}
				}

				// Convert JSON format
				JSONObject json = new JSONObject(jsonData);

				// Whether the App is launched or not
				if (!isInForeground()) {
					// Background
					TiApplication tiapp = TiApplication.getInstance();
					Intent launcherIntent = new Intent(tiapp, GcmjsService.class);
					launcherIntent.putExtra("data", json.toString());

					// set Service Mode "START_NOT_STICKY".
					// @see http://developer.android.com/reference/android/app/Service.html#START_NOT_STICKY
					// default is "START_REDELIVER_INTENT" defined in TiJSService
					launcherIntent.putExtra(TiC.INTENT_PROPERTY_START_MODE, Service.START_NOT_STICKY);

					// Start service
					tiapp.startService(launcherIntent);
				}
				else {
					// Forground. Send message to App
					GcmjsModule module = GcmjsModule.getInstance();
					if (module != null) {
						module.fireMessage(json.toString());
					}
					else {
						GcmjsModule.logd(TAG+": fireMessage module instance not found.");
					}
				}
			}
		}
		GCMBroadcastReceiver.completeWakefulIntent(intent);
	}

	public static boolean isInForeground() {
		Context context = TiApplication.getInstance().getApplicationContext();
		ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		String packageName = context.getPackageName();
		if (am.getRunningTasks(1).get(0).topActivity.getPackageName().equals(packageName)) {
			return true;
		}
	    return false;
	}
}
