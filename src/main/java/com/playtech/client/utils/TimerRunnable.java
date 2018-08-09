package com.playtech.client.utils;

/**
 * Prints out count-down timer on console for user.
 */
public class TimerRunnable implements Runnable {

    private volatile boolean done = false;

    public void shutdown() {
        done = true;
    }

    public void run() {
        for (int i = 10; i >=1; i--) {
            if(!done) {
                System.out.print(i+"\t");
                try {
                    // thread to sleep for 1000 milliseconds or 1 second
                    Thread.sleep(1000);
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
            else{
//                Debugger.logMessage("Previous Thread stopped..");
            }

        }
    }
}