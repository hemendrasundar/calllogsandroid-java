package com.hemendra.algorithmclass;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    ListView lv;
    private ProgressDialog progress;
    String phonenumber;
    int mYear, mMonth, mDay, mHour, mMinute;
    ArrayList<String> phonenumberlist = new ArrayList<>();
    static String date;
    TextView emptytextview;
    SimpleDateFormat dateFormat;
    TextView sim_tv;
    final Calendar myCalendar = Calendar.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
         date = dateFormat.format(myCalendar.getTime());
        sim_tv = findViewById(R.id.sim_tv);
        getSupportActionBar().setTitle(date);
        lv = findViewById(R.id.lv_calllogs);
         emptytextview = findViewById(R.id.empty_tv);
        if(isPermissionGranted()) {
           // getCallLog();
            getCallDetails(date);
            TelephonyManager mTelephonyMgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
            String imsi = mTelephonyMgr.getSubscriberId();
            String imei = mTelephonyMgr.getDeviceId();
            String simno = mTelephonyMgr.getSimSerialNumber();
            Log.v("", ""+imsi);
            Log.v("", ""+imei);
            Log.v("", ""+simno);
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.upload_btn:

                addItemToSheet(getCallDetails(date));
                return true;

            case R.id.date:
                showDatePicker();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public  boolean isPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_CALL_LOG)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("TAG","Permission is granted");
                return true;
            } else {
                Log.v("TAG","Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALL_LOG}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v("TAG","Permission is granted");
            return true;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {

            case 1: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Permission granted", Toast.LENGTH_SHORT).show();
                    //getCallLog();
                    getCallDetails(date);
                } else {
                    Toast.makeText(getApplicationContext(), "Permission denied", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void getCallLog()
    {
        ArrayList<String> phonenumbers = new ArrayList<String>();
        String[] projection = new String[]{
                CallLog.Calls._ID, CallLog.Calls.NUMBER, CallLog.Calls.DATE,
        };
        Cursor query = this.managedQuery(
                CallLog.Calls.CONTENT_URI, projection, null, null, null);

        ListAdapter adapter = new SimpleCursorAdapter(
                this, android.R.layout.simple_list_item_1, query,
                new String[]{CallLog.Calls.NUMBER},
                new int[]{android.R.id.text1});

        lv.setAdapter(adapter);
//        query.moveToFirst();
//
//        while(!query.isAfterLast()) {
//
//            phonenumbers.add(query.getString(query.getColumnIndex("NUMBER")));
//            query.moveToNext();
//        }
//        query.close();
//        return phonenumbers.toArray(new String[phonenumbers.size()]);

    }

    private void   addItemToSheet(final ArrayList<String> phone_numbers) {

        final ProgressDialog loading = ProgressDialog.show(this,"Adding Item","Please wait");
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://script.google.com/macros/s/AKfycbyMnGhwFW2KzhAsoPkyq3ULztlOhiPRlKdwSLCha1dsb_I_G9sD/exec",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        loading.dismiss();
                        Toast.makeText(MainActivity.this,response,Toast.LENGTH_LONG).show();


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parmas = new HashMap<>();

              //  String[] arr = new String[7];
                JSONArray care_type = new JSONArray();
                for(int i=0; i < phone_numbers.size(); i++) {
                    care_type.put(phone_numbers.get(i));   // create array and add items into that
                }

                //here we pass params
                parmas.put("action","addItem");
                parmas.put("jarr",care_type.toString());

                return parmas;
            }
        };

        int socketTimeOut = 50000;// u can change this .. here it is 50 seconds
        RetryPolicy retryPolicy = new DefaultRetryPolicy(socketTimeOut, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(retryPolicy);
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }

    private ArrayList<String> getCallDetails(String date) {
        ArrayList<String> numbers = new ArrayList<>();
        ArrayList<String> listWithoutDuplicates= new ArrayList<>();
        ContentResolver cr = getApplication().getContentResolver();
        StringBuffer sb = new StringBuffer();
        String strOrder = android.provider.CallLog.Calls.DATE + " DESC";
        Uri callUri = Uri.parse("content://call_log/calls");

        Calendar calendar = Calendar.getInstance();
        calendar.set(2020,Calendar.JANUARY, 1);
        String fromDate = String.valueOf(calendar.getTimeInMillis());
        calendar.set(2020, Calendar.APRIL, 23);
        String toDate = String.valueOf(calendar.getTimeInMillis());
        String[] whereValue = {fromDate, toDate};

        Cursor cur = cr.query(callUri, null, null,null,null);
         // Cursor cur = cr.query(callUri, null, android.provider.CallLog.Calls.DATE + " BETWEEN ? AND ?", whereValue, strOrder);
        //Cursor cur = cr.query(callUri, null, android.provider.CallLog.Calls.DATE+" >= ?", whereValue, strOrder);
        // loop through cursor
        while (cur.moveToNext()) {
            String callNumber = cur.getString(cur
                    .getColumnIndex(android.provider.CallLog.Calls.NUMBER));
            String id = cur.getString(cur.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID));
            String simmid = cur
                    .getString(cur
                            .getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID));
            sim_tv.setText(simmid);
            String callName = cur
                    .getString(cur
                            .getColumnIndex(android.provider.CallLog.Calls.CACHED_NAME));

            String callDate = cur.getString(cur
                    .getColumnIndex(android.provider.CallLog.Calls.DATE));
            SimpleDateFormat formatter = new SimpleDateFormat(
                    "dd-MMM-yyyy");
            String dateString = formatter.format(new Date(Long
                    .parseLong(callDate)));
            Log.e("date",date);
            Log.e("dateString",dateString);
            String callType = cur.getString(cur
                    .getColumnIndex(android.provider.CallLog.Calls.TYPE));
            String dir = null;
            int dircode = Integer.parseInt(callType);
            switch (dircode) {
                case CallLog.Calls.OUTGOING_TYPE:
                    dir = "OUTGOING";
                    break;
                case CallLog.Calls.INCOMING_TYPE:
                    dir = "INCOMING";
                    break;
                case CallLog.Calls.MISSED_TYPE:
                    dir = "MISSED";
                    break;
            }

            String isCallNew = cur.getString(cur
                    .getColumnIndex(android.provider.CallLog.Calls.NEW));

            String duration = cur.getString(cur
                    .getColumnIndex(android.provider.CallLog.Calls.DURATION));

            if(dateString.equals(date)) {
                   numbers.add(callNumber);

                    }
            LinkedHashSet<String> hashSet = new LinkedHashSet<>(numbers);

            listWithoutDuplicates = new ArrayList<>(hashSet);
            this.phonenumberlist= (ArrayList<String>) listWithoutDuplicates.clone();
            ListAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listWithoutDuplicates);
            if(adapter.getCount()==0)
            {
                emptytextview.setVisibility(View.VISIBLE);
                emptytextview.setText("No Call logs found for this Date.\nTry Changing Date...");
            }
            else{
                emptytextview.setVisibility(View.GONE);
                lv.setAdapter(adapter);

            }
        }
        return listWithoutDuplicates;
    }

    public void showDatePicker()
    {
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, monthOfYear);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        String dtStart = dateFormat.format(myCalendar.getTime());
                        MainActivity.date = dtStart;
                        Log.e("dialog",dtStart);
                        getSupportActionBar().setTitle(dtStart);
                        getCallDetails(dtStart);
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();

    }
}
