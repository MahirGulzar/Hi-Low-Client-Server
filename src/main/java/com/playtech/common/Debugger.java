package com.playtech.common;

import java.sql.Timestamp;

/**
 * Custom Debugger class to display logs on console effectively.
 */
public class Debugger {
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";


    public static void logConnect(Object o){
        System.out.println(ANSI_GREEN+ embedTimeStamp()+"[Connection]: "+o.toString()+ANSI_GREEN);
    }
    public static void logDisconnect(Object o){
        System.out.println(ANSI_GREEN+ embedTimeStamp()+"[Connection]: "+o.toString()+ANSI_GREEN);
    }
    public static void logMessage(Object o){
        System.out.println(ANSI_YELLOW+ embedTimeStamp()+"[Message]: "+o.toString()+ANSI_YELLOW);
    }
    public static void logError(Object o){
        System.out.println(ANSI_RED+ embedTimeStamp()+"[Message]: "+o.toString()+ANSI_RED);
    }
    public static void logNotifcation(Object o){
        System.out.println(ANSI_RED+ embedTimeStamp()+"[Notification]: "+o.toString()+ANSI_RED);
    }

    /**
     * Embed TimeStamp to the log..
     * @return current TimeStamp.
     */
    public static String embedTimeStamp() {
        java.util.Date date= new java.util.Date();
        return (new Timestamp(date.getTime())).toString();
    }
}