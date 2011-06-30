package com.jvitela.flashcards;

import android.database.Cursor;

public class FlashCardInfo {
	public int		mId;
	public String	mFront;
	public String	mBack;
	public int		mDeckId;
	public int		mRating;

	public FlashCardInfo(Cursor card,boolean reverse) {
        int iid		= card.getColumnIndexOrThrow(CardsTable.KEY_ROWID);
        int ifront	= card.getColumnIndexOrThrow(CardsTable.KEY_FRONT);
        int iback	= card.getColumnIndexOrThrow(CardsTable.KEY_BACK);
        int iiddeck	= card.getColumnIndexOrThrow(CardsTable.KEY_DECKID);
        int irating	= card.getColumnIndexOrThrow(CardsTable.KEY_RATING);
        mId		= card.getInt(iid);
        if( reverse ) {
			mFront	= card.getString(iback);
			mBack	= card.getString(ifront);        	
        }
        else {
			mFront	= card.getString(ifront);
			mBack	= card.getString(iback);
        }
		mDeckId	= card.getInt(iiddeck);
		mRating	= card.getInt(irating);
	}

	public FlashCardInfo(String front, String back) {
		mFront = front;
		mBack = back;
	}
};
