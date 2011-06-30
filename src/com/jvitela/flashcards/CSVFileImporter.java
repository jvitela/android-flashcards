package com.jvitela.flashcards;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class CSVFileImporter extends Thread {
	/** ID of bundle info send within a message to the Handler
	 * 
	 */
	public static final String	ID_STATUS = "Status";
	public static final String	ID_MESSAGE = "Message";
	public static final String	ID_VALUE = "Value";

	// Possible process Statuses, this are sent as ID_STATUS values
	public static final int		STS_LOADING_DATA 	= 1;
	public static final int		STS_DATA_LOADED  	= 2;
	public static final int		STS_SAVING_DATA  	= 3;
	public static final int		STS_DATA_SAVED 		= 4;
	public static final int		STS_ERROR 			= 5;

	/** The pattern used to match CSV's consists of three alternations:
	 *  the first matches a quoted field, the second unquoted,
	 *  the third a null field.
	 */
	private static final String CSV_PATTERN = "\"([^\"]+?)\"[,;\\t]?|([^,;\\t]+)[,;\\t]?|[,;\\t]";

	private Pattern 			mPattern;
    private String				mFile;
    private FlashCardsDbAdapter mDb;
    private	Handler				mHandler;
    private long				mDeckId;

    // Constructor
	public CSVFileImporter(Context context, String filename, long deckId, Handler handler) {        
        mPattern = Pattern.compile(CSV_PATTERN);
        mDb = new FlashCardsDbAdapter(context);
        mDb.open();
		mFile = filename;
		mHandler = handler;
		mDeckId = deckId;
	}

    /** 
     * Implementation of Runnable
     * 
     */
    public void run() {
        try {
			sendMessage(STS_SAVING_DATA);
			List<List<String>> cards = loadFile( mFile );
        	sendMessage(STS_DATA_LOADED,cards.size());
        	createCards(cards); // save to database
        }
        catch( Exception e ){
        	sendMessage(STS_ERROR,e.getMessage());
        }
        finally {
        	sendMessage(STS_DATA_SAVED);
        }
    }
    
    /**
     *  Read records from CSV file and saves them to a list of lists
     * @param filename
     * @return  List of 'List of Strings'
     * @throws Exception
     */
	private List<List<String>> loadFile(String filename) throws Exception {
        BufferedReader bf = null;
        String line = null;
        List<List<String>> cards = new ArrayList<List<String>>(); 
        try {
            bf = new BufferedReader(new FileReader(filename));
            while ((line = bf.readLine()) != null) {
            	if (Thread.interrupted()) {
            		return cards;
            	}
            	cards.add( parse(line) );
            	sendMessage(STS_LOADING_DATA);
            }
        } 
        catch (FileNotFoundException ex) {
            throw new Exception("File not found:" + filename);
        } 
        catch (IOException ex) {
            throw new Exception("Error reading file " + filename);
        } 
        /* Finally enters also when the return sentence is executed */
        finally {
            if (bf != null) {
                bf.close();
            }
        }		
        return cards;
	}

	/** 
	 * Parse one line.
	 * @param  line:	Line to read
	 * @return List of Strings
	 */
	private List<String> parse(String line) {
		List<String> list = new ArrayList<String>();
		Matcher m = mPattern.matcher(line);
		// For each field
		while (m.find()) {
			String match = m.group();
			if (match == null)
				break;
			if (match.length() == 0)
				match = "";
			else {
				match = match.replaceAll("^[;,\\t\\n\"]{1,2}", ""); // remove starting markers
				match = match.replaceAll("[;,\\t\\n\"]{1,2}$", ""); // remove trailing markers
			}
			list.add(match);
		}
		return list;
	}    

	/** Saves to database
	 * 
	 * @param lst:	List<List<String>>
	 */
	private void createCards(List<List<String>> lst) {
		for( int i=0; i<lst.size(); ++i ){
        	if (Thread.interrupted()) {
        		return;
        	}
			List<String> card = lst.get(i);
			if( card.size()==4 ) {
				mDb.mCards.createCard(card.get(0), card.get(1), card.get(2), card.get(3), mDeckId);
			}
			else if( card.size()==2 ) {
				mDb.mCards.createCard(card.get(0), "", card.get(1), "", mDeckId);			
			}
        	sendMessage(STS_SAVING_DATA);
		}
	}
	
	/** Sends message to handler
	 * 
	 * @param status
	 */
	private void sendMessage(int status){
    	Bundle data = new Bundle();
    	data.putInt(ID_STATUS, status);
    	Message msg = Message.obtain();
    	msg.setData(data);
    	mHandler.sendMessage(msg);		
        Thread.yield();
	}
	
	/** Sends message to handler
	 * 
	 * @param status
	 * @param value:	int
	 */
	private void sendMessage(int status, int value){
    	Bundle data = new Bundle();
    	data.putInt(ID_STATUS, status);
    	data.putInt(ID_VALUE, value);
    	Message msg = Message.obtain();
    	msg.setData(data);
    	mHandler.sendMessage(msg);		
        Thread.yield();
	}
	
	/** Sends message to handler
	 * 
	 * @param status
	 * @param message:	String
	 */
	private void sendMessage(int status, String message){
    	Bundle data = new Bundle();
    	data.putInt(ID_STATUS, status);
    	data.putString(ID_MESSAGE, message);
    	Message msg = Message.obtain();
    	msg.setData(data);
    	mHandler.sendMessage(msg);		
        Thread.yield();
	}
}
