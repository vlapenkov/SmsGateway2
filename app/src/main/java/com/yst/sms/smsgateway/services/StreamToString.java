package com.yst.sms.smsgateway.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

/**
 * Created by user on 08.02.2017.
 */
 public  class StreamToString {


     public static String Convert (InputStream stream)
    {

        final int bufferSize = 1024;
        final char[] buffer = new char[bufferSize];
        final StringBuilder out = new StringBuilder();
        Reader in = null;


        if (stream!=null)
        {
            try {
                in = new InputStreamReader(stream, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            for (; ; ) {
                int rsz = 0;
                try {
                    rsz = in.read(buffer, 0, buffer.length);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (rsz < 0)
                    break;
                out.append(buffer, 0, rsz);
            }
            return out.toString();

        }else return "";
}}
