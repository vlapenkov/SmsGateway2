package com.yst.sms.smsgateway.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.yst.sms.smsgateway.activities.MainActivity;
import com.yst.sms.smsgateway.data.DbHelper;

/**
 * Created by user on 07.02.2017.
 */
public class SmsReceiver extends BroadcastReceiver
{


    @Override
    public void onReceive(Context context, Intent intent)
    {
   //     DbHelper dbHelper = new DbHelper(context);
        // Get Bundle object contained in the SMS intent passed in
        Bundle bundle = intent.getExtras();
        SmsMessage[] smsm = null;
        String sms_str ="";
        String phonenumber="",message ="";


        if (bundle != null)
        {
            // Get the SMS message
            Object[] pdus = (Object[]) bundle.get("pdus");
            smsm = new SmsMessage[pdus.length];
            for (int i=0; i<smsm.length; i++){
                smsm[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                sms_str += "Sent From: " + smsm[i].getOriginatingAddress();
                sms_str += "\r\nMessage: ";
                sms_str += smsm[i].getMessageBody().toString();
                sms_str+= "\r\n";
                phonenumber= smsm[i].getOriginatingAddress();
                message=smsm[i].getMessageBody().toString();

            //    dbHelper.addSmsMessage(smsm[i].getOriginatingAddress(),smsm[i].getMessageBody().toString()) ;
            }

            // Start Application's  MainActivty activity

            if (!phonenumber.isEmpty()&&!message.isEmpty())
            {
            Intent smsIntent=new Intent(context,MainActivity.class);
            smsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_SINGLE_TOP);
            //smsIntent.putExtra("sms_str", sms_str);
            smsIntent.putExtra(DbHelper.COLUMN_PHONENUMBER, phonenumber);
            smsIntent.putExtra(DbHelper.COLUMN_MESSAGE, message);
            context.startActivity(smsIntent); }
        }
    }
}
