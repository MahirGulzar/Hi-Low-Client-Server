package com.playtech.server.impl;

import com.playtech.common.Card;
import com.playtech.common.CardRank;
import com.playtech.common.PlayerAction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * GameLogic holds the core logic of game i.e processes a valid input
 * and output the outcomes.
 * Initializes a deck and handles the complete lifecycle of a client game.
 * Is bound to one particular client.
 */
public class GameLogic {

    private List<Card> cardList;    // Shuffled List of Deck
    private int currentIndex;       // current list traverse index
    private int roundID;            // current roundID
    private boolean roundFinished;
    private Card currentBaseCard;    // currentBaseCard

    public GameLogic()
    {
        cardList = new ArrayList<>(52);
        currentIndex=0;
        roundID=1;
        roundFinished=false;
        initDeck();
    }

    public boolean isRoundFinished() {
        return roundFinished;
    }

    public void setRoundFinished(boolean roundFinished) {
        this.roundFinished = roundFinished;
    }

    public Card getCurrentBaseCard() {
        return currentBaseCard;
    }

    public void setCurrentBaseCard(Card currentBaseCard) {
        this.currentBaseCard = currentBaseCard;
    }

    public int getCurrentIndex()
    {
        return this.currentIndex;
    }


    /**
     * Checks if an increment to current index is possible.
     * if yes: then increments the index.
     * @return true of increment possible else false;
     */
    public boolean increamentCurrentIndex()
    {
        if(currentIndex<=cardList.size()-2)
        {
            currentIndex++;
            return true;
        }

        return false;
    }

    public int getRoundID() {
        return roundID;
    }


    /**
     *  Changes the result card and returns next round card
     * @return next Card;
     */
    public Card getNextCard()
    {
        Card baseCard = cardList.get(currentIndex);
        roundID++;
        if (!increamentCurrentIndex())
        {
            resetDeck();    // TODO check validity here..
        }
        currentBaseCard =baseCard;
        return baseCard;
    }

    /**
     * Initialize Deck
     */
    private void initDeck()
    {
        for(Card card:Card.values())
        {
            cardList.add(card);
        }
        Collections.shuffle(cardList);
    }

    /**
     * Checks if player won the round or not
     * @param action PlayerAction {HIGHER,LOWER,EQUAL}
     * @return true if won, false if lost
     */
    public boolean checkWin(PlayerAction action)
    {
        int prevIndex=CardRank.valueOf(currentBaseCard.getValue().toString()).ordinal();
        int currentIndex=CardRank.valueOf(cardList.get(getCurrentIndex()).getValue().toString()).ordinal();

        if(action==null)
            return false;

        switch (action)
        {
            case HIGHER:
                return currentIndex>prevIndex;
            case LOWER:
                return currentIndex<prevIndex;
            case EQUALS:
                return currentIndex==prevIndex;
            default:
                return false;
        }

    }

    /**
     * Reset Deck
     */
    private void resetDeck()
    {
        this.currentIndex=0;
        Collections.shuffle(cardList);
    }

    /**
     * Get the list of deck cards
     * @return List of integers of deck cards
     */
    public List<Card> getDeck() {
        return cardList;
    }


}
