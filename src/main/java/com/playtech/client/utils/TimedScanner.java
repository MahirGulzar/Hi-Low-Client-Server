package com.playtech.client.utils;

import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.*;

/**
 *  A wrapper on Scanner.
 *  Responsible for handling user interrupts while game waits for user input.
 */
public class TimedScanner
{
    public TimedScanner(InputStream input)
    {
        this.in = new Scanner(input);
    }

    private Scanner in;
    private ExecutorService ex = Executors.newSingleThreadExecutor(new ThreadFactory()
    {
        @Override
        public Thread newThread(Runnable r)
        {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        }
    });


    public String nextLine(int timeout) throws InterruptedException, ExecutionException
    {
        Future<String> result = ex.submit(new Worker());
        try
        {
            return result.get(timeout, TimeUnit.MILLISECONDS);
        }
        catch (TimeoutException e)
        {
            return null;
        }
    }

    private class Worker implements Callable<String>
    {
        @Override
        public String call() throws Exception
        {
            return in.nextLine();
        }
    }
}