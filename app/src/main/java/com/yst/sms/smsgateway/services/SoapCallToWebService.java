package com.yst.sms.smsgateway.services;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by user on 07.02.2017.
 */
public class SoapCallToWebService {

    public static final String mUrlService="http://37.1.84.50:8080/YST/ws/ServiceTransfer";
    private static HashMap<String,String> mHeaders = new HashMap<>();

    static {
        mHeaders.put("Accept-Encoding","gzip,deflate");
        mHeaders.put("Content-Type", "application/soap+xml");
        mHeaders.put("Host", "37.1.84.50:8080");
        mHeaders.put("Connection", "Keep-Alive");
        mHeaders.put("User-Agent","AndroidApp");
        mHeaders.put("Authorization","Basic Q2xpZW50NTkzMzppMjR4N2U=");

    }


    /*
    Отправить конкретное сообщение shipmentId в SOAP
     */
    public final InputStream sendMessage( String phoneNumber, String message)
    {
        int status=0;

/*
* <ser:SetOrderstatusDeliveredByDriver>
         <PhoneNumber>89301265544</PhoneNumber>
         <Message>4</Message>
      </ser:SetOrderstatusDeliveredByDriver>

* */
        String xmlstring= "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"http://37.1.84.50:8080/ServiceTransfer\" xmlns:tran=\"http://37.1.84.50:8080/Transfer\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <ser:SetOrderstatusDeliveredByDriver>\n" +
                "         <PhoneNumber>"+phoneNumber+"</PhoneNumber>\n" +
                "         <Message>"+message+"</Message>\n" +
                "      </ser:SetOrderstatusDeliveredByDriver>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>";




        HttpURLConnection connection = null;
        try {
            URL url = new URL(mUrlService);
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestProperty("Content-Length", xmlstring.getBytes().length + "");
            connection.setRequestProperty("SOAPAction", "http://37.1.84.50:8080/ServiceTransfer/SetOrderstatusDeliveredByDriver");

            for(Map.Entry<String, String> entry : mHeaders.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                connection.setRequestProperty(key,value);

            }

            connection.setRequestMethod("POST");
            connection.setDoInput(true);

            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(xmlstring.getBytes("UTF-8"));
            outputStream.close();

            connection.connect();
            status = connection.getResponseCode();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            Log.d("HTTP Client", "HTTP status code : " + status);
        }

        InputStream inputStream = null;
        try {
            inputStream = connection.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return inputStream;
    }
}
