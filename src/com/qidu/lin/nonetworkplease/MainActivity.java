package com.qidu.lin.nonetworkplease;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
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

	}

	public void noNetwork(View v)
	{
		setWifi(v, false);
		set3gNoThrow(false);
		afterWork();
	}

	public void onAllNetwork(View v)
	{
		setWifi(v, true);
		set3gNoThrow(true);
		afterWork();
	}

	private void afterWork()
	{
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
					// TODO Auto-generated catch block
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
			set3g(isEnabled);
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
		return false;
	}

	private void set3g(boolean isEnabled) throws ClassNotFoundException, NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException
	{
		Method dataConnSwitchmethod;
		Class telephonyManagerClass;
		Object ITelephonyStub;
		Class ITelephonyClass;

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

	private void setWifi(View v, boolean status)
	{
		WifiManager wifiManager = (WifiManager) v.getContext().getSystemService(Context.WIFI_SERVICE);
		wifiManager.setWifiEnabled(status);
	}
}
