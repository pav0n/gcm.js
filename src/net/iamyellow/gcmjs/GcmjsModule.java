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

import java.io.IOException;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiApplication;

import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.gcm.GoogleCloudMessaging;

@Kroll.module(name = "Gcmjs", id = "net.iamyellow.gcmjs")
public class GcmjsModule extends KrollModule {

	private GoogleCloudMessaging gcm;

	// *************************************************************
	// constants

	private static final String EVENT_PROPERTY_DEVICE_TOKEN = "deviceToken";
	private static final String EVENT_PROPERTY_ERROR = "error";
	private static final String EVENT_PROPERTY_DATA = "data";

	public static String PROPERTY_SENDER_ID = "GCM_sender_id";
	public static final boolean DBG = org.appcelerator.kroll.common.TiConfig.LOGD;

	// *************************************************************
	// logging

	private static final String LCAT = "gcmjs";

	public static void logd(String msg) {
		if (DBG) {
			Log.d(LCAT, msg);
		}
	}

	public static void logw(String msg) {
		Log.e(LCAT, msg);
	}

	// *************************************************************
	// singleton

	private static GcmjsModule instance = null;

	public static GcmjsModule getInstance() {
		return instance;
	}

	// *************************************************************
	// constructor

	public GcmjsModule() {
		super();

		instance = this;
	}

	// *************************************************************
	// registration

	@Kroll.method
	public void registerForPushNotifications() {

		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				try {
					if (gcm == null) {
						Context context = TiApplication.getInstance().getApplicationContext();
						gcm = GoogleCloudMessaging.getInstance(context);
					}
					String registrationId = gcm.register(TiApplication.getInstance().getAppProperties().getString(GcmjsModule.PROPERTY_SENDER_ID, ""));
					msg = "Device registered: registrationId = " + registrationId;
					fireSuccess(registrationId);
				} catch (IOException e) {
					msg = "Error: " + e.getMessage();
					fireError(msg);
				}
				return msg;
			}

			@Override
			protected void onPostExecute(String msg) {
			}
		}.execute(null, null, null);
	}

	// *************************************************************
	// main activity class name helper

	@Kroll.getProperty
	@Kroll.method
	public String getMainActivityClassName() {
		return TiApplication
				.getInstance()
				.getPackageManager()
				.getLaunchIntentForPackage(
						TiApplication.getInstance().getPackageName())
				.getComponent().getClassName();
	}

	// *************************************************************
	// events

	public void fireSuccess(String registrationId) {
		logd("Start firing success.");
		KrollDict event = new KrollDict();
		event.put(EVENT_PROPERTY_DEVICE_TOKEN, registrationId);
		fireEvent("success", event);     
	}

	public void fireError(String error) {
		logd("Start firing error.");
		KrollDict event = new KrollDict();
		event.put(EVENT_PROPERTY_ERROR, error);
		fireEvent("error", event);
	}

	public void fireUnregister(String registrationId) {
		logd("Start firing unregister.");
		KrollDict event = new KrollDict();
		event.put(EVENT_PROPERTY_DEVICE_TOKEN, registrationId);
		fireEvent("unregister", event);
	}

	public void fireMessage(String message) {
		logd("Start firing callback.");
		KrollDict event = new KrollDict();
		event.put(EVENT_PROPERTY_DATA, message);
		fireEvent("callback", event);
	}
}
