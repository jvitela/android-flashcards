package com.jvitela.flashcards;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class DeckEdit extends Activity {

    private EditText mName;
    private EditText mDescription;
    private Long mDeckId;
    private FlashCardsDbAdapter mDb;

    //Called when the activity is starting. 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Database
        mDb = new FlashCardsDbAdapter(this);
        mDb.open();

        setContentView(R.layout.deck_edit);
        setTitle(R.string.menu_edit_deck);

        mName = (EditText) findViewById(R.id.deck_name);
        mDescription = (EditText) findViewById(R.id.deck_desc);

        // Attempt to get id from previous session
        // This is in case the activity was closed when editing a deck
        mDeckId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(DecksTable.KEY_DECKID);
        // Attempt to get id from call parameters
        // This is in case the activity was called to edit an existing deck
		if (mDeckId == null) {
			Bundle extras = getIntent().getExtras();
			mDeckId = extras != null ? extras.getLong(DecksTable.KEY_DECKID)
									: null;
		}
		populateFields();
    }

    // Save the deck info to the database
    public void onSaveButton(View v) {
    	saveState();
    	Intent intent = new Intent();
    	intent.putExtra(DecksTable.KEY_DECKID, mDeckId);
        setResult(Activity.RESULT_OK,intent);
        finish(); // finish activity
    }

    // Cancel
    public void onCancelButton(View v) {
        setResult(Activity.RESULT_CANCELED);
        finish(); // finish activity
    }

    // Populate form fields
    private void populateFields() {
        if (mDeckId != null) {
            Cursor deck = mDb.mDecks.fetchDeck(mDeckId);
            if( deck.getCount()<1 ) {
            	alert("Deck Not Found");
            	return;
            }
            startManagingCursor(deck);
            mName.setText(deck.getString(
            		deck.getColumnIndexOrThrow(DecksTable.KEY_NAME)));
            mDescription.setText(deck.getString(
            		deck.getColumnIndexOrThrow(DecksTable.KEY_DESC)));            
        }
        else {
            mName.setText("");
            mDescription.setText("");        	
        }
    }

    // This method is called before an activity may be killed so that when it comes back 
    // some time in the future it can restore its state.
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(DecksTable.KEY_DECKID, mDeckId); // Save the deck's id
    }

    // Save the deck info to the database
    private void saveState() {
        String name = mName.getText().toString();
        String desc = mDescription.getText().toString();

        // Create a new deck
        if (mDeckId == null) {
            long id = mDb.mDecks.createDeck(name, desc);
            if (id >= 0) {
                mDeckId = id;
            }
        }
        // Edit an existing deck
        else {
            mDb.mDecks.updateDeck(mDeckId, name, desc);
        }
    }

    private void alert(String text) {
		Context context = getApplicationContext();
    	int duration = Toast.LENGTH_SHORT;
    	Toast toast = Toast.makeText(context, text, duration);
    	toast.setGravity(Gravity.CENTER, 0, 0);
    	toast.show();    	
    }
}
