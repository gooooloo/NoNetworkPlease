/*
 * Copyright 2013 Qidu Lin
 * 
 * This file is part of NoNetworkPlease.
 * 
 * NoNetworkPlease is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * NoNetworkPlease is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * NoNetworkPlease. If not, see <http://www.gnu.org/licenses/>.
 */

package com.qidu.lin.nonetworkplease;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;

public class MainActivity extends Activity
{

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setTitle(R.string.set_up_the_network);
	}

	public void noNetwork(View v)
	{
		doJob(false, false);
	}

	public void onWifiOnly(View v)
	{
		doJob(true, false);
	}

	public void onDataOnly(View v)
	{
		doJob(false, true);
	}

	public void onAllNetwork(View v)
	{
		doJob(true, true);
	}

	private void doJob(final boolean wifiEnabled, final boolean dataEnabled)
	{
		setWifi(wifiEnabled);
		set3gNoThrow(dataEnabled);

		new AsyncTask<Void, Void, Void>()
		{
			private static final int DURATION = 3000;

			private ProgressDialog pd;

			/*
			 * (non-Javadoc)
			 * 
			 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
			 */
			@Override
			protected void onPostExecute(Void result)
			{
				pd.dismiss();
				MainActivity.this.finish();
				super.onPostExecute(result);
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see android.os.AsyncTask#onPreExecute()
			 */
			@Override
			protected void onPreExecute()
			{
				super.onPreExecute();
				pd = new ProgressDialog(MainActivity.this);
				pd.show();
			}

			@Override
			protected Void doInBackground(Void... arg0)
			{
				// skip the background waiting once contition matched.
				// but we can't do this for wifi/data, because even the status
				// is enabled, the Android system status bar's ui won't be
				// updated right away, so our UI also shows progress indicator.
				if (!wifiEnabled && !dataEnabled)
				{
					if (detectWifiDisabled() && detect3gDisabledNoThrow())
					{
						return null;
					}
				}

				try
				{
					Thread.sleep(DURATION);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				return null;
			}
		}.execute();
	}

	private boolean detectWifiDisabled()
	{
		return ((WifiManager) this.getSystemService(Context.WIFI_SERVICE)).getWifiState() == WifiManager.WIFI_STATE_DISABLED;
	}

	private boolean detect3gDisabledNoThrow()
	{
		try
		{
			int currentapiVersion = android.os.Build.VERSION.SDK_INT;
			if (currentapiVersion >= android.os.Build.VERSION_CODES.GINGERBREAD)
			{
				return !detectMobileDataEnabledNew();
			}
			else
			{
				// can't detect for this old sdk version.
				return false;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}

	private boolean set3gNoThrow(boolean isEnabled)
	{

		try
		{
			int currentapiVersion = android.os.Build.VERSION.SDK_INT;
			if (currentapiVersion >= android.os.Build.VERSION_CODES.GINGERBREAD)
			{
				setMobileDataEnabledNew(this, isEnabled);
			}
			else
			{
				setMobileDataEnabledOld(isEnabled);
			}
			return true;
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (NoSuchMethodException e)
		{
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
		catch (InvocationTargetException e)
		{
			e.printStackTrace();
		}
		catch (NoSuchFieldException e)
		{
			e.printStackTrace();
		}
		return false;
	}

	private void setWifi(boolean status)
	{
		WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		wifiManager.setWifiEnabled(status);
	}

	/**
	 * this method is copied from
	 * http://stackoverflow.com/questions/3644144/how-
	 * to-disable-mobile-data-on-android, Vladimir Sorokin's answer.
	 * 
	 * @param context
	 * @param enabled
	 * @throws ClassNotFoundException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 */
	private void setMobileDataEnabledNew(Context context, boolean enabled) throws ClassNotFoundException, NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException
	{
		final Object iConnectivityManager = getConnectivityManagerObject();
		final Class<?> iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
		final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
		setMobileDataEnabledMethod.setAccessible(true);

		setMobileDataEnabledMethod.invoke(iConnectivityManager, enabled);
	}

	private boolean detectMobileDataEnabledNew() throws ClassNotFoundException, NoSuchFieldException, IllegalArgumentException,
			IllegalAccessException, NoSuchMethodException, InvocationTargetException
	{
		final Object iConnectivityManager = getConnectivityManagerObject();
		final Class<?> iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
		final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("getMobileDataEnabled");
		setMobileDataEnabledMethod.setAccessible(true);

		return (Boolean) setMobileDataEnabledMethod.invoke(iConnectivityManager);
	}

	private Object getConnectivityManagerObject() throws ClassNotFoundException, NoSuchFieldException, IllegalArgumentException,
			IllegalAccessException
	{
		final ConnectivityManager conman = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
		final Class<?> conmanClass = Class.forName(conman.getClass().getName());
		final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
		iConnectivityManagerField.setAccessible(true);
		return iConnectivityManagerField.get(conman);
	}

	/**
	 * this method is copied from
	 * http://stackoverflow.com/questions/3644144/how-
	 * to-disable-mobile-data-on-android, phaniKumar's answer.
	 * 
	 * @param isEnabled
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private void setMobileDataEnabledOld(boolean isEnabled) throws ClassNotFoundException, NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException
	{
		Method dataConnSwitchmethod;
		Class<?> telephonyManagerClass;
		Object ITelephonyStub;
		Class<?> ITelephonyClass;

		TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);

		if (telephonyManager.getDataState() == TelephonyManager.DATA_CONNECTED)
		{
			isEnabled = true;
		}
		else
		{
			isEnabled = false;
		}

		telephonyManagerClass = Class.forName(telephonyManager.getClass().getName());
		Method getITelephonyMethod = telephonyManagerClass.getDeclaredMethod("getITelephony");
		getITelephonyMethod.setAccessible(true);
		ITelephonyStub = getITelephonyMethod.invoke(telephonyManager);
		ITelephonyClass = Class.forName(ITelephonyStub.getClass().getName());

		if (isEnabled)
		{
			dataConnSwitchmethod = ITelephonyClass.getDeclaredMethod("disableDataConnectivity");
		}
		else
		{
			dataConnSwitchmethod = ITelephonyClass.getDeclaredMethod("enableDataConnectivity");
		}
		dataConnSwitchmethod.setAccessible(true);
		dataConnSwitchmethod.invoke(ITelephonyStub);
	}
}
