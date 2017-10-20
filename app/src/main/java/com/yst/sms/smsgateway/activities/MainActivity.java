package com.yst.sms.smsgateway.activities;

import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.yst.sms.smsgateway.R;
import com.yst.sms.smsgateway.data.DbHelper;
import com.yst.sms.smsgateway.data.SmsMessagePackage;
import com.yst.sms.smsgateway.services.SoapCallToWebService;
import com.yst.sms.smsgateway.services.StreamToString;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, SearchView.OnQueryTextListener{


   private static int timeoutInMs = 60000;
    private Timer mTimer;
    private DispatcherTimerTask mMyTimerTask;

    SimpleCursorAdapter mAdapter=null;
    ListView lvData =null;
    DbHelper mDbHelper;
    String mCurFilter=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDbHelper = new DbHelper(this);

        lvData = (ListView) findViewById(R.id.lvData);

        mAdapter = new SimpleCursorAdapter(this,
                R.layout.smsmessage_item, mDbHelper.getAllMessages(mCurFilter),
                new String[] { DbHelper.COLUMN_PHONENUMBER,DbHelper.COLUMN_MESSAGE,DbHelper.COLUMN_DATEOFRECEPTION,DbHelper.COLUMN_DATEOFDISPATCH },
                new int[] { R.id.text1, R.id.text2,R.id.text3,R.id.text4  }, 0);

        lvData.setAdapter(mAdapter);

        getSupportLoaderManager().initLoader(0, null, this);

        mTimer = new Timer();
        mMyTimerTask = new DispatcherTimerTask();

        //  mTimer.schedule(mMyTimerTask, 1000);
        // delay 1000ms, repeat in 10000ms


        mTimer.schedule(mMyTimerTask, timeoutInMs, timeoutInMs);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection=null,selectionArgs=null;
        String selection=null , sortOrder=null;
        return new CursorLoader( getApplicationContext(), null, projection, selection,selectionArgs,sortOrder )
        {
            @Override
            public Cursor loadInBackground()
            {
                return mDbHelper.getAllMessages(mCurFilter);

            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    protected void onNewIntent(Intent intent) {

        super.onNewIntent(intent);


        String phonenumber=intent.getStringExtra(DbHelper.COLUMN_PHONENUMBER);
        String message=intent.getStringExtra(DbHelper.COLUMN_MESSAGE);
        mDbHelper.addSmsMessage(phonenumber,message);
        getSupportLoaderManager().restartLoader(0, null, this);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {


            case R.id.removeAllSent: {
                mDbHelper.deleteWhenSent();
                getSupportLoaderManager().restartLoader(0, null, this);
                break;
            }
            case R.id.removeAll:{

                mDbHelper.clearTable(DbHelper.TABLE_NAME);
                getSupportLoaderManager().restartLoader(0, null, this);
                break;
            }
            case R.id.exit: {

                finish();

                return true;
            }
        }
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
      //  return false;
        String newFilter = !TextUtils.isEmpty(query) ? query : null;
        // Don't do anything if the filter hasn't actually changed.
        // Prevents restarting the loader when restoring state.
        if (mCurFilter == null && newFilter == null) {
            return true;
        }
        if (mCurFilter != null && mCurFilter.equals(newFilter)) {
            return true;
        }
        mCurFilter = newFilter;

        getSupportLoaderManager().restartLoader(0, null, this);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }


    class DispatcherTimerTask extends TimerTask
    {
        @Override
        public void run() {

            Log.d("DispatcherTimerTask","Timer worked ");
// 1.Найти сообщения
      List<SmsMessagePackage> list= mDbHelper.getTop10Messages();

            Log.d("DispatcherTimerTask","List size = "+Integer.toString(list.size()));
            InputStream stream;

            boolean flagUpdated=false;
            for(int i=0;i<=list.size()-1;i++)
            {
            stream = new SoapCallToWebService().sendMessage(list.get(i).PhoneNumber,list.get(i).Message);

            String dest=StreamToString.Convert(stream);
                if (!dest.isEmpty()) {
             mDbHelper.updateMessage(list.get(i).Id);
                    flagUpdated =true;
            }


            }
/*
* Лоадер надо обязательно обновлять в UI-потоке
* http://www.twicecircled.com/2c/loadermanager-initloader-needs-to-be-called-from-ui-thread/
 */
            if (flagUpdated)

                runOnUiThread(new Runnable() {
                    public void run() {
                        getSupportLoaderManager().restartLoader(0, null, MainActivity.this);
                    }
                });

      }



     /*
        @Override
        public void run() {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                     DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+03:00"));
        Date curDate = new Date();

        Toast.makeText(MainActivity.this, dateFormat.format(curDate), Toast.LENGTH_SHORT).show();
                }
            });

        } */
    }
}


