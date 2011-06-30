package com.jvitela.flashcards;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class CardsTable {
	private SQLiteDatabase	mDb;
    public static final String NAME				= "cards";
    public static final String KEY_FRONT 		= "front";
    public static final String KEY_FRONT_DESC	= "front_description";
    public static final String KEY_BACK 		= "back";
    public static final String KEY_BACK_DESC	= "back_description";
    public static final String KEY_ROWID 		= "_id";
    public static final String KEY_DECKID 		= "deckId";
    public static final String KEY_RATING 		= "rating";

    public CardsTable(SQLiteDatabase db) {
    	mDb = db;
    }
    /**
     * Table creation sql statement
     */
    public static void create(SQLiteDatabase db) {
	    db.execSQL(
	    	"create table "+CardsTable.NAME +" ("
			+ CardsTable.KEY_ROWID	 	+" integer primary key autoincrement, "
			+ CardsTable.KEY_FRONT	 	+" text not null, "
			+ CardsTable.KEY_FRONT_DESC	+" text, "
			+ CardsTable.KEY_BACK	 	+" text not null, "
			+ CardsTable.KEY_BACK_DESC	+" text, "
			+ CardsTable.KEY_DECKID 	+" integer, "
			+ CardsTable.KEY_RATING 	+" integer);"
	    );
    }
    
    public static void upgrade(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS "+NAME);
        CardsTable.create(db);   	
    }

    /**
     * Create a new note using the title and body provided. If the note is
     * successfully created return the new rowId for that note, otherwise return
     * a -1 to indicate failure.
     * 
     * @param title the title of the note
     * @param body the body of the note
     * @return rowId or -1 if failed
     */
    public long createCard(String front, String frontDesc, String back, String backDesc, long deckId) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(CardsTable.KEY_FRONT, front);
        initialValues.put(CardsTable.KEY_FRONT_DESC, frontDesc);
        initialValues.put(CardsTable.KEY_BACK, back);
        initialValues.put(CardsTable.KEY_BACK_DESC, backDesc);
        initialValues.put(CardsTable.KEY_DECKID, deckId);
        return mDb.insert(CardsTable.NAME, null, initialValues);
    }

    /**
     * Delete the card with the given rowId
     * 
     * @param rowId id of note to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteCard(long rowId) {
        return mDb.delete(CardsTable.NAME, CardsTable.KEY_ROWID + "=" + rowId, null) > 0;
    }


    /**
     * Delete all the cards with the given deck id
     * 
     * @param deckId id of card's deck to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteAllCards(long deckId) {
        return mDb.delete(CardsTable.NAME, CardsTable.KEY_DECKID + "=" + deckId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all cards in the database
     * 
     * @return Cursor over all cards
     */
    public Cursor fetchAllCards(String sort) {
    	String[] fields = new String[] {
    			CardsTable.KEY_ROWID,
    			CardsTable.KEY_FRONT, 
    			CardsTable.KEY_FRONT_DESC, 
    			CardsTable.KEY_BACK,
    			CardsTable.KEY_BACK_DESC,
    			CardsTable.KEY_DECKID,
    			CardsTable.KEY_RATING
        };
        return mDb.query(
        	CardsTable.NAME,	// table 
    		fields, 			// columns
    		null,				// selection
    		null, 				// selection Args
    		null, 				// group by
    		null, 				// having
    		sort+" asc"			// order by
        );
    }

    /**
     * Return a Cursor over the list of all cards belonging to one deck in the database
     * 
     * @return Cursor over all cards
     */
    public Cursor fetchAllDeckCards(long deckId, String sort) {
    	String[] fields = new String[] {
    			CardsTable.KEY_ROWID,
    			CardsTable.KEY_FRONT, 
    			CardsTable.KEY_FRONT_DESC, 
    			CardsTable.KEY_BACK,
    			CardsTable.KEY_BACK_DESC,
    			CardsTable.KEY_DECKID,
    			CardsTable.KEY_RATING
        };
        return mDb.query(
        	CardsTable.NAME,		// table 
    		fields, 				// columns
    		// selection
    		CardsTable.KEY_DECKID+"="+deckId,
    		null, 					// selection Args
    		null, 					// group by
    		null, 					// having
    		sort+" asc"		// order by
        );
    }

    /**
     * Return a Cursor positioned at the note that matches the given rowId
     * 
     * @param rowId id of note to retrieve
     * @return Cursor positioned to matching note, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchCard(long rowId) throws SQLException {

    	String[] fields = new String[] {
    			CardsTable.KEY_ROWID,
    			CardsTable.KEY_FRONT, 
    			CardsTable.KEY_FRONT_DESC, 
    			CardsTable.KEY_BACK,
    			CardsTable.KEY_BACK_DESC,
    			CardsTable.KEY_DECKID,
    			CardsTable.KEY_RATING
        };
        Cursor mCursor = mDb.query(
    		true,					// distinct
    		CardsTable.NAME, 		// table
    		fields, 				// columns
    		CardsTable.KEY_ROWID + "=" + rowId,//  selection
    		null,					// selection args
            null, 					// group by
            null, 					// having
            null, 					// order by
            null					// limit
        );
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    /**
     * Update the card using the details provided. The card to be updated is
     * specified using the rowId, and it is altered to use the front and back text
     * values passed in
     * 
     * @param rowId id of card to update
     * @param front value to set card front text to
     * @param back value to set card back text to
     * @return true if the card was successfully updated, false otherwise
     */
    public boolean updateCard(long rowId, String front, String frontDesc, String back, String backDesc, long deckId) {
        ContentValues args = new ContentValues();
        args.put(CardsTable.KEY_FRONT, front);
        args.put(CardsTable.KEY_FRONT_DESC, frontDesc);
        args.put(CardsTable.KEY_BACK, back);
        args.put(CardsTable.KEY_BACK_DESC, backDesc);
        args.put(CardsTable.KEY_DECKID, deckId);
        return mDb.update(CardsTable.NAME, args, CardsTable.KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Update the card using the details provided. The card to be updated is
     * specified using the rowId
     * 
     * @param rowId id of card to update
     * @param rating of success to set card to
     * @return true if the card was successfully updated, false otherwise
     */
    public boolean updateCard(long rowId, int rating) {
        ContentValues args = new ContentValues();
        args.put(CardsTable.KEY_RATING, rating);

        return mDb.update(CardsTable.NAME, args, CardsTable.KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Update the card using the details provided. The card to be updated is
     * specified using the rowId
     * 
     * @param rowId id of card to update
     * @param deckId if of the deck the card is in
     * @return true if the card was successfully updated, false otherwise
     */
    public boolean updateCard(long rowId, long deckId) {
        ContentValues args = new ContentValues();
        args.put(CardsTable.KEY_DECKID, deckId);

        return mDb.update(CardsTable.NAME, args, CardsTable.KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    /**
     * Replaces the deckId in all cards with 'oldDeckId' with 'newDeckId'
     *  
     * @param oldDeckId	original deckId to be replaced
     * @param newDeckId	new deckId to be replaced with
     * @return true if the cards were successfully updated, false otherwise
     */
    public boolean moveCardsFromDeck(long oldDeckId, long newDeckId) {
        ContentValues args = new ContentValues();
        args.put(CardsTable.KEY_DECKID, newDeckId);

        return mDb.update(CardsTable.NAME, args, CardsTable.KEY_DECKID + "=" + oldDeckId, null) > 0;    	
    }
}
