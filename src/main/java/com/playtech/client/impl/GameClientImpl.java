package com.playtech.client.impl;

import com.playtech.client.api.GameClient;
import com.playtech.client.utils.TimedScanner;
import com.playtech.client.utils.TimerRunnable;
import com.playtech.common.Debugger;
import com.playtech.common.ProtocolConfiguration;
import com.playtech.game.protocol.FinishRoundRequest;
import com.playtech.game.protocol.PlayerActionRequest;
import com.playtech.game.protocol.PlayerActionResponse;
import com.playtech.game.protocol.StartRoundRequest;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutionException;


/**
 * GameClientImpl handles servers responses and requests.
 * Maintains Core-loop and call validators like GameManager to validate user inputs.
 * Responsible for client side lifecycle.
 */
public class GameClientImpl implements GameClient {

    private Socket clientSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    /**
     * Starts a connection on given particular ip and port.
     * Calls gameLoop() immediately when connection is established.
     * @param ip
     * @param port
     */
    public void startConnection(String ip, int port) {
        try {
            clientSocket = new Socket(ip, port);
            Debugger.logConnect(String.format("Client bind to IP:%s PORT:%d",ip,port));
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());
            this.gameLoop();
        }
        catch (IOException e)
        {
            Debugger.logError(e.getMessage());
        } catch (InterruptedException e) {
            Debugger.logError(e.getMessage());
        } catch (ClassNotFoundException e) {
            Debugger.logError(e.getMessage());
        }
        finally {

        }

    }


    /**
     * Core game-loop of client side.
     * Responsbile for handling: StartRoundRequest,FinishRoundRequest
     * and other server responses.
     * @throws ClassNotFoundException
     * @throws InterruptedException
     */
    public void gameLoop() throws ClassNotFoundException, InterruptedException {

        GameManager gameManager = GameManager.getInstance();
        String input="";
        while(!input.equals("4"))
        {
            System.out.println(Debugger.ANSI_GREEN+"\t\t\t----------< Hi-Low (Ver:0.9) >------------"+Debugger.ANSI_GREEN);
            gameManager.showInstructions();

            StartRoundRequest startRoundRequest=null;
            try{
                startRoundRequest = (StartRoundRequest)this.in.readObject();
                System.out.print(Debugger.ANSI_GREEN+"\nPress [ENTER] key to get Base card:"+Debugger.ANSI_GREEN);
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                br.readLine();
            }
            catch (IOException e)
            {
                //
            }
            this.startRound(startRoundRequest);

            // 1. Create a TimeScanner using the InputStream available.
            TimedScanner in = new TimedScanner(System.in);
            System.out.println(Debugger.ANSI_YELLOW+"Guess the next card..:"+Debugger.ANSI_YELLOW);

            // Run 10 seconds timer thread and show timer on console
            TimerRunnable runnable = new TimerRunnable();
            Thread thread = new Thread(runnable);
            thread.start();
            try
            {
                if ((input = in.nextLine(startRoundRequest.getActionRoundDuration()*1000))== null)
                {
                    System.out.println("Too slow!");
                    input="";
                }
            }
            catch (InterruptedException e)
            {
                Debugger.logError(e);
            }
            catch (ExecutionException e)
            {
                Debugger.logError(e);
            }
            finally {
                runnable.shutdown();
            }


            int intInput;
            PlayerActionRequest playerActionRequest;
            if(gameManager.validateInput(input))
            {
//                Debugger.logMessage("Valid input");
                intInput=Integer.parseInt(input);
                if(input.trim().equals("4")){
                    System.out.println("\n");
                    Debugger.logDisconnect("Quiting Game Now...");
                    stopClient();
                    break;
                }
                playerActionRequest = new PlayerActionRequest(gameManager.getActionByInput(intInput));
            }
            else
            {
                System.out.println();
                Debugger.logError("Invalid Input ....");
                playerActionRequest = new PlayerActionRequest(null);
            }

            System.out.println(Debugger.ANSI_YELLOW+"\nYour Action: "+playerActionRequest.getPlayerAction()+"\n"+Debugger.ANSI_YELLOW);

            try {
                this.out.writeObject(playerActionRequest);
            } catch (IOException e) {
                e.printStackTrace();
            }


            //-----------------------------------------------

            PlayerActionResponse playerActionResponse = null;
            try {
                playerActionResponse = (PlayerActionResponse)this.in.readObject();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(playerActionResponse.getErrorText().length()>0)
            {
                System.out.println();
                Debugger.logError(playerActionResponse.getErrorText());
            }
            else
            {
                Debugger.logMessage("Successful response from Server..!");
            }


            //-----------------------------------------------


            // Get server Response
            FinishRoundRequest finishRoundRequest = null;
            try {
                finishRoundRequest = (FinishRoundRequest)this.in.readObject();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.finishRound(finishRoundRequest);

            // Deliberate delay for next round...
            Thread.sleep(2000);

        }
    }

    @Override
    public void startRound(StartRoundRequest startRoundRequest) {
        System.out.println("Base Card: "+startRoundRequest.getBaseCard());
    }

    @Override
    public void finishRound(FinishRoundRequest finishRoundRequest) {
        if(finishRoundRequest.isWin())
        {
            System.out.println(Debugger.ANSI_GREEN+"\nYou Won this Round!! Congratulations!!..\n\n"+Debugger.ANSI_GREEN);
        }
        else
        {
            System.out.println(Debugger.ANSI_RED+"\nYou Lost this Round!!..\n\n"+Debugger.ANSI_RED);
        }
    }

    public void stopClient(){
        try {
            clientSocket.close();
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Client Application entry point
     */
    public static void main(String[] args){

        GameClientImpl client = new GameClientImpl();
        client.startConnection("127.0.0.1", ProtocolConfiguration.PORT);

    }
}
