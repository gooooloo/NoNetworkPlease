/**
 * Author : gooooloo (https://github.com/gooooloo)
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

	private void doJob(boolean wifiEnabled, boolean dataEnabled)
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
		final ConnectivityManager conman = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		final Class<?> conmanClass = Class.forName(conman.getClass().getName());
		final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
		iConnectivityManagerField.setAccessible(true);
		final Object iConnectivityManager = iConnectivityManagerField.get(conman);
		final Class<?> iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
		final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
		setMobileDataEnabledMethod.setAccessible(true);

		setMobileDataEnabledMethod.invoke(iConnectivityManager, enabled);
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
