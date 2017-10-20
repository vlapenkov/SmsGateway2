package com.yst.sms.smsgateway.data;

/**
 * Created by user on 08.02.2017.
 */
public class SmsMessagePackage {


    public int Id;
    public String PhoneNumber;
    public String Message;


    public SmsMessagePackage(int id, String phoneNumber, String message) {
        Id = id;
        PhoneNumber = phoneNumber;
        Message = message;
    }


}
