package com.jvitela.flashcards;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

public class FlashCardsDbAdapter {
    private static final String DATABASE_NAME = "flashcards";
    private static final int DATABASE_VERSION = 1;

    private DatabaseHelper 	mDbHelper;
    private SQLiteDatabase 	mDb;
    private final Context 	mCtx;
    public CardsTable		mCards;
    public DecksTable		mDecks;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        	CardsTable.create(db);
        	DecksTable.create(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        	if( oldVersion<newVersion ) {
            	CardsTable.upgrade(db);
            	DecksTable.upgrade(db);
        	}
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public FlashCardsDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public FlashCardsDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        mCards = new CardsTable(mDb);
        mDecks = new DecksTable(mDb);
        return this;
    }

    public void close() {
        mDbHelper.close();
    }
    
    // taken from: http://stackoverflow.com/questions/1995320/how-to-backup-database-file-to-sdcard-on-android
    public void backup() {
		try {
			File sd = Environment.getExternalStorageDirectory();
			File data = Environment.getDataDirectory();
			if (sd.canWrite()) {
				String currentDBPath = "\\data\\{package name}\\databases\\{database name}";
				String backupDBPath = "{database name}";
				File currentDB = new File(data, currentDBPath);
				File backupDB = new File(sd, backupDBPath);
				if (currentDB.exists()) {
					FileChannel src = new FileInputStream(currentDB).getChannel();
					FileChannel dst = new FileOutputStream(backupDB).getChannel();
					dst.transferFrom(src, 0, src.size());
					src.close();
					dst.close();
				}
			}
	    }
		catch (Exception e) {}
    }
}
