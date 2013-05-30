/*
 * Copyright (c) 2012 Socialize Inc.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.socialize.demo;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import com.google.android.gcm.GCMRegistrar;
import com.socialize.ConfigUtils;
import com.socialize.Socialize;
import com.socialize.android.ioc.IOCContainer;
import com.socialize.config.SocializeConfig;
import com.socialize.entity.Entity;
import com.socialize.error.SocializeException;
import com.socialize.listener.SocializeInitListener;
import com.socialize.ui.dialog.DialogRegister;
import com.socialize.ui.dialog.SafeProgressDialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * @author Jason Polites
 *
 */
public abstract class DemoActivity extends Activity implements DialogRegister {
	
	private List<Dialog> dialogs = new ArrayList<Dialog>();
	protected Entity entity;
	
	public static final int PAGE_SIZE = 10;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		StictModeUtils.enableDefaults();

		Socialize.onCreate(this, savedInstanceState);

		final SafeProgressDialog dialog = SafeProgressDialog.show(this);

		// NOTE:
		// THIS call to initAsync is NOT required by normal apps.  We are just
		// doing this in our demo to disable some analytics reporting.
		Socialize.initAsync(this, new SocializeInitListener() {
			@Override
			public void onInit(Context context, IOCContainer container) {
				SocializeConfig config = ConfigUtils.getConfig(DemoActivity.this);

				config.setProperty(SocializeConfig.SOCIALIZE_EVENTS_AUTH_ENABLED, "false");
				config.setProperty(SocializeConfig.SOCIALIZE_EVENTS_SHARE_ENABLED, "false");

				String entityKey = config.getProperty("entity.key");
				String entityName = config.getProperty("entity.name");

				entity = Entity.newInstance(entityKey, entityName);
				entity.setType("article");

				// Standard GCM Registration
				// This is simply to verify that SmartAlerts work where there is already a GCM implementation
				// If you are not already using GCM you can ignore this.
				GCMRegistrar.checkDevice(DemoActivity.this);
				GCMRegistrar.checkManifest(DemoActivity.this);

				final String regId = GCMRegistrar.getRegistrationId(DemoActivity.this);

				if (regId.equals("")) {
					GCMRegistrar.register(DemoActivity.this, GCMIntentService.SENDER_ID);
				}

				onCreate();

				dialog.dismiss();
			}

			@Override
			public void onError(SocializeException error) {
				// Handle error
				error.printStackTrace();

				dialog.dismiss();
			}
		});

	}

	protected abstract void onCreate();

	/* (non-Javadoc)
	 * @see com.socialize.ui.dialog.DialogRegister#register(android.app.Dialog)
	 */
	@Override
	public void register(Dialog dialog) {
		dialogs.add(dialog);
	}

	/* (non-Javadoc)
	 * @see com.socialize.ui.dialog.DialogRegister#getDialogs()
	 */
	@Override
	public Collection<Dialog> getDialogs() {
		return dialogs;
	}
	
	protected void handleError(Activity context, Exception error) {
		error.printStackTrace();
		DemoUtils.showErrorDialog(context, error);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		Socialize.onPause(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Socialize.onResume(this);
	}

	@Override
	protected void onDestroy() {
		if(dialogs != null) {
			for (Dialog dialog : dialogs) {
				dialog.dismiss();
			}
			dialogs.clear();
		}		
		
		Socialize.onDestroy(this);
		
		super.onDestroy();
	}
}
