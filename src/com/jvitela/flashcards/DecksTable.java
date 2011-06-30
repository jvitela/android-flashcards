package com.jvitela.flashcards;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DecksTable {
	private SQLiteDatabase	mDb;
	public static final String NAME = "decks";
    public static final String KEY_NAME = "name";	
    public static final String KEY_DESC = "description";	
    public static final String KEY_DECKID = "_id";

    public DecksTable(SQLiteDatabase db) {
    	mDb = db;
    }

    public static void create(SQLiteDatabase db) {
        db.execSQL(
        	"create table "	+ NAME +" ("
            + KEY_DECKID	+" integer primary key autoincrement, "
            + KEY_NAME		+" text not null,"
            + KEY_DESC		+" text);"
        );
    }

    public static void upgrade(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS "+NAME);
        DecksTable.create(db);
    }

    /**
     * Create a new deck using the title and desc provided. If the deck is
     * successfully created return the new id for that deck, otherwise return
     * a -1 to indicate failure.
     * 
     * @param name the title of the deck
     * @param des the description of the deck
     * @return deckId or -1 if failed
     */
    public long createDeck(String name, String desc) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_DESC, desc);
        return mDb.insert(NAME, null, initialValues);
    }

    /**
     * Delete the deck with the given rowId
     * 
     * @param rowId id of note to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteDeck(long deckId) {
        return mDb.delete(NAME, KEY_DECKID + "=" + deckId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all notes in the database
     * 
     * @return Cursor over all notes
     */
    public Cursor fetchAllDecks() {
    	String[] fields = new String[] {
    			KEY_DECKID,
                KEY_NAME,
                KEY_DESC
        };
        return mDb.query(
    		NAME,			// table 
    		fields, 				// columns
    		null, 					// selection
    		null, 					// selection Args
    		null, 					// group by
    		null, 					// having
    		KEY_NAME+" asc"			// order by
        );
    }

    /**
     * Return a Cursor positioned at the note that matches the given rowId
     * 
     * @param rowId id of note to retrieve
     * @return Cursor positioned to matching note, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchDeck(long deckId) throws SQLException {

    	String[] fields = new String[] {
    			KEY_DECKID,
                KEY_NAME,
                KEY_DESC
        };
        Cursor mCursor = mDb.query(
    		true,					// distinct
    		NAME, 		// table
    		fields, 				// columns
    		KEY_DECKID + "=" + deckId,//  selection
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
     * Update the deck using the details provided. The note to be updated is
     * specified using the deckId, and it is altered to use the title and body
     * values passed in
     * 
     * @param deckId id of deck to update
     * @param title value to set deck title to
     * @param body value to set deck body to
     * @return true if the deck was successfully updated, false otherwise
     */
    public boolean updateDeck(long deckId, String name, String desc) {
        ContentValues args = new ContentValues();
        args.put(KEY_NAME, name);
        args.put(KEY_DESC, desc);
        return mDb.update(NAME, args, KEY_DECKID + "=" + deckId, null) > 0;
    }
}
