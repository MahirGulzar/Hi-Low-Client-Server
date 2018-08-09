package com.playtech.server;

import com.playtech.common.*;
import com.playtech.game.protocol.StartRoundRequest;
import com.playtech.server.impl.GameLogic;
import com.playtech.server.impl.GameServiceImpl;
import org.junit.Test;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Tests GameLogic with assertions on GameLogic Decisions.
 * Tests GameService connection with dummy Client.
 */
public class GameServiceTest {

    @Test
    public void givenGameLogic_checkNumberOfCards()
    {
        GameLogic gameLogic = new GameLogic();
        assert(gameLogic.getDeck().size()==52);
    }

    @Test
    public void givenGameLogic_checkGetNextCard()
    {
        GameLogic gameLogic = new GameLogic();
        assert (gameLogic.getNextCard()==gameLogic.getDeck().get(0));
    }

    @Test
    public void givenANextMove_And_PlayerAction_checkWin()
    {
        GameLogic gameLogic = new GameLogic();
        gameLogic.getNextCard();
        assert(gameLogic.checkWin(PlayerAction.HIGHER) ||
                gameLogic.checkWin(PlayerAction.LOWER)||
                gameLogic.checkWin(PlayerAction.EQUALS) );
    }

    @Test
    public void givenAKnowBaseCard_AndKnownResultCard_CheckWin()
    {
        GameLogic gameLogic = new GameLogic();
        Card baseCard =gameLogic.getNextCard();
        Card resultCard= gameLogic.getDeck().get(gameLogic.getCurrentIndex());

        int prevIndex= CardRank.valueOf(baseCard.getValue().toString()).ordinal();
        int currentIndex=CardRank.valueOf(resultCard.getValue().toString()).ordinal();

        if(currentIndex>prevIndex) {
            assert(gameLogic.checkWin(PlayerAction.HIGHER));
        }
        else if(currentIndex<prevIndex) {
            assert(gameLogic.checkWin(PlayerAction.LOWER));
        }
        else {
            assert(gameLogic.checkWin(PlayerAction.EQUALS));
        }
    }

    @Test
    public void givenADummyClient_checkGameServiceRoundStartRequest()
    {
        GameServiceImpl gameService = new GameServiceImpl();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    gameService.start(ProtocolConfiguration.PORT);
                }
                catch (IOException e)
                {
                    Debugger.logError(e.getMessage());
                }
            }
        });

        thread.start();
        Socket clientSocket;
        ObjectOutputStream out;
        ObjectInputStream in;
        try {
            clientSocket = new Socket("127.0.0.1", ProtocolConfiguration.PORT);
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());
            assert(in.readObject() instanceof StartRoundRequest);

            clientSocket.close();
            out.close();
            in.close();
        }
        catch (IOException e)
        {
            Debugger.logError(e.getMessage());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        //Calling deprecated method just to shutdown dummy threads
        thread.stop();
    }
}
