package com.jvitela.flashcards;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class DecksList extends ListActivity {
	// Dialogs
    //private static final int DIALOG_DECK_INFO = 0;
    //private static final int DIALOG_DECK_OPTIONS = 1;
    private static final int DIALOG_REMOVE_ALL_CARDS = 2;

	// Activities called for results
    private static final int ACTIVITY_EDIT_DECK		= 0;
    private static final int ACTIVITY_CREATE_DECK 	= 1;
    private static final int ACTIVITY_IMPORT_CARDS_FOR_DECK = 2;

    // Options Menu items
    private static final int OMNU_CREATE_DECK = Menu.FIRST;
    private static final int OMNU_IMPORT_DECK = Menu.FIRST + 1;

    // Context menu items
    private static final int CMNU_EDIT_DECK 	= 0;
    private static final int CMNU_DELETE_DECK 	= 1;
    private static final int CMNU_CARDS_DECK 	= 2;
    private static final int CMNU_IMPORT_DECK 	= 3;

    private FlashCardsDbAdapter mDb;
    private long mSelectedDeckId;	// This variable is required due to we cannot bundle args to dialog calls in api level 7 :-(

    /**
     *	Create the activity and its view 
     */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.decks_list_view);	// this is not mandatory, but we want a custom view
		registerForContextMenu( getListView() );	// tell the activity we use a context menu
		// create a DB adapter and open the database
		mDb = new FlashCardsDbAdapter(this);
		mDb.open();
		// No deck selected
		mSelectedDeckId = -1;
		// populate our list
        fillData();
	}

	/** 
	 *	Create a menu for this activity
	 *	This is the menu displayed when pressing the menu button
	 *	This is only called once, the first time the options menu is displayed. 
	 *	To update the menu every time it is displayed, see onPrepareOptionsMenu(Menu). 
	 */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE, OMNU_CREATE_DECK, Menu.NONE, R.string.menu_create_deck);
        menu.add(Menu.NONE, OMNU_IMPORT_DECK, Menu.NONE, R.string.menu_import_deck);
        return true;
    }

	/** 
	 *	Create a context menu for this activity 
	 *	Context menu's are called after long clicks
	 *	This will be called every time the context menu is about to be shown
	 *	@param menuInfo:	Extra menu information provided
	 *		Fields:
	 *			- long id:			The row id of the item for which the context menu is being displayed.
	 *			- int position:		The position in the adapter for which the context menu is being displayed.
	 *			- View targetView:	The child view for which the context menu is being displayed.
	 */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu,v,menuInfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
    	mSelectedDeckId = info.id; //getSelectedItemId(); ??

        menu.setHeaderTitle( R.string.lbl_manage_deck );
        menu.add(ContextMenu.NONE, CMNU_EDIT_DECK,	ContextMenu.NONE, R.string.menu_edit_deck);
        menu.add(ContextMenu.NONE, CMNU_DELETE_DECK,ContextMenu.NONE, R.string.menu_delete_deck);
        menu.add(ContextMenu.NONE, CMNU_CARDS_DECK, ContextMenu.NONE, R.string.menu_manage_deck_cards);
        menu.add(ContextMenu.NONE, CMNU_IMPORT_DECK,ContextMenu.NONE, R.string.menu_import_cards_for_deck);
    }

    /**
     *	Create a dialog, this is only called once per dialog 
     */
	@Override
	protected Dialog onCreateDialog(int id) {
	    Dialog dialog = null;
	    switch(id) {
	    /*case DIALOG_DECK_INFO:
	    	dialog = createDeckInfoDialog();
	        break;
	    case DIALOG_DECK_OPTIONS:
	    	dialog = createDeckOptionsDialog();
	        break;*/
	    case DIALOG_REMOVE_ALL_CARDS:
	    	dialog = createDeleteAllCardsDialog();
	        break;
	    default:
	        dialog = null;
	    }
	    return dialog;
	}

	/**
	 *	Initialize the dialog before displaying it 
	 *
	@Override
	protected void onPrepareDialog (int id, Dialog dialog) {
		switch(id) {
		case DIALOG_DECK_INFO:
	    	TextView vName = (TextView) dialog.findViewById(R.id.deck_name );
			//AlertDialog alert = (AlertDialog) dialog;
			//Button btn = alert.getButton(AlertDialog.BUTTON_NEUTRAL);
			// do not allow to delete the default deck
			if( mSelectedItemId>0 ) {
				Cursor cur  = mDb.mDecks.fetchDeck(mSelectedItemId);
				int nameIdx = cur.getColumnIndex(DecksTable.KEY_NAME);
				String name = cur.getString(nameIdx);
				vName.setText(name);
				//btn.setVisibility(View.VISIBLE);
			}
			else {
				//btn.setVisibility(View.GONE);
				vName.setText("");
			}
			break;
		}
	}*/

	/**
	 * Creates a dialog to confirm the remove of all cards
	 * @return AlertDialog object
	 */
	private AlertDialog createDeleteAllCardsDialog() {
		AlertDialog.Builder dlg = new AlertDialog.Builder(this);
        dlg.setTitle(R.string.lbl_remove_deck);
        dlg.setMessage(R.string.lbl_delete_allcards_question);
        dlg.setPositiveButton(R.string.lbl_accept, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	deleteDeck(true);
            }
        });
        dlg.setNegativeButton(R.string.lbl_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	//deleteDeck(false);
            }
        });
        return dlg.create();
	}
	
	/**
	 * Creates a dialog to edit a deck's info
	 * @return
	 *
	private AlertDialog createDeckInfoDialog() {
        // This example shows how to add a custom layout to an AlertDialog
        LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(R.layout.deck_edit, null);
    	AlertDialog.Builder dlg =  new AlertDialog.Builder(this);
        //dlg.setIcon(R.drawable.alert_dialog_icon)
    	dlg.setTitle(R.string.menu_edit_deck);
        dlg.setView(textEntryView);
        dlg.setPositiveButton(R.string.lbl_save, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	TextView name = (TextView) textEntryView.findViewById( R.id.deck_name );
        		editDeck( name.getText().toString() );                		
            	dialog.dismiss();
            }
        });
        dlg.setNegativeButton(R.string.lbl_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	dialog.dismiss();
            }
        });
        //dlg.setNeutralButton(R.string.delete, new DialogInterface.OnClickListener() {
        //    public void onClick(DialogInterface dialog, int whichButton) {
        //    	dialog.dismiss();
        //    	showDialog(DIALOG_REMOVE_ALL_CARDS);
        //    }
        //});
        return dlg.create();
	}*/
	
	/**
	 * Creates a dialog with options
	 * @return
	 *
	private AlertDialog createDeckOptionsDialog() {
    	final CharSequence[] items = {
	    		getResources().getText(R.string.menu_edit_deck), 
	    		getResources().getText(R.string.menu_delete_deck),
	    		getResources().getText(R.string.add_cards_to_deck)
	    	};
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(R.string.menu_cards);
    	builder.setItems(
    		items, 
    		new DialogInterface.OnClickListener() {
	    	    public void onClick(DialogInterface dialog, int item) {
	    	    	switch( item ) {
	    	    	case 0:
	    	    		showDialog(DIALOG_DECK_INFO);
	    	    		dialog.dismiss();
	    	    		break;
	    	    	case 1:
	    	    		showDialog(DIALOG_REMOVE_ALL_CARDS);
	    	    		dialog.dismiss();
	    	    		break;
	    	    	case 2:
	    	            addCardsToDeck();
	    	    		break;
	    	    	}
	    	    }
	    	}
    	);
    	return builder.create();
	}*/

    /**
     *  populate list view
     */
    private void fillData() {
        Cursor decksCursor = mDb.mDecks.fetchAllDecks();
        startManagingCursor(decksCursor);

        // Create an array to specify the fields we want to display in the list
        String[] from = new String[]{DecksTable.KEY_NAME,DecksTable.KEY_DESC};

        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{R.id.text1,R.id.text2};

        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter notes = new SimpleCursorAdapter(
        		this, 
        		R.layout.decks_list_row, 
        		decksCursor, 
        		from, 
        		to
        );
        setListAdapter(notes);
    }

    /**
     *  View's Menu, Add Deck
     *  Called when an option of the activity's menu is selected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	mSelectedDeckId = -1;
    	int id = item.getItemId(); 
        switch( id ) {
            case OMNU_CREATE_DECK:
            	createDeck(null);
                return true;
            case OMNU_IMPORT_DECK:
            	importDeck();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when an item of the list is clicked
     * Item click, Edit or delete
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        mSelectedDeckId = id;
        Intent intent = new Intent(this,Play.class);
        intent.putExtra(CardsTable.KEY_DECKID, mSelectedDeckId);
        startActivity(intent);
    }

    /**
     * Return false to allow normal context menu processing to proceed, true to consume it here.
     */
    public boolean onContextItemSelected (MenuItem item) {
    	int id = item.getItemId();
        switch( id ) {
            case CMNU_EDIT_DECK:
            	editDeck();
                return true;
            case CMNU_DELETE_DECK:
            	// Cannot bundle args to dialog in api level 7 :-(
            	showDialog(DIALOG_REMOVE_ALL_CARDS);
            	return true;
            case CMNU_CARDS_DECK:
            	manageCards();
            	return true;
            case CMNU_IMPORT_DECK:
            	importDeck();
            	return true;
        }
        return false; //super.onContextItemSelected(item);
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	switch( requestCode ) {
    	case ACTIVITY_CREATE_DECK:
    		if( resultCode==Activity.RESULT_OK ) {
    			alert("Deck created");
    		}
    		break;
    	case ACTIVITY_EDIT_DECK:
    		if( resultCode==Activity.RESULT_OK ) {
    			alert("Deck edited");
    		}
    		break;
    	case ACTIVITY_IMPORT_CARDS_FOR_DECK:
    		if( resultCode==Activity.RESULT_OK ) {
	    		mSelectedDeckId = data.getLongExtra(DecksTable.KEY_DECKID, -1);
	    		if( mSelectedDeckId!=-1 )
	    			manageCards();
    		}
    		break;
    	}
    }
    
	private void manageCards() {
		if( mSelectedDeckId<0 ) {
			alert("Select an item first!");
			return;
		}
		Intent intent = new Intent(this, CardsList.class);
		intent.putExtra(DecksTable.KEY_DECKID, mSelectedDeckId);
		startActivity(intent);	
	}
	
	public void createDeck(View v) {
		mSelectedDeckId = -1;
		Intent intent = new Intent(this, DeckEdit.class);
		startActivityForResult(intent,ACTIVITY_CREATE_DECK);
	}

    private void editDeck() {
		if( mSelectedDeckId<0 ) {
			alert("Select an item first!");
			return;
		}
		Intent intent = new Intent(this, DeckEdit.class);
		intent.putExtra(DecksTable.KEY_DECKID, mSelectedDeckId);
		startActivityForResult(intent,ACTIVITY_EDIT_DECK);    		
    	fillData();
    }

    private void importDeck() {
		Intent intent = new Intent(this, Import.class);
		if( mSelectedDeckId!=-1 )
			intent.putExtra(DecksTable.KEY_DECKID, mSelectedDeckId);
		startActivityForResult(intent,ACTIVITY_IMPORT_CARDS_FOR_DECK);    		
    	fillData();
    }

    public void deleteDeck(boolean delAllCards){
		if( mSelectedDeckId<0 ) {
			alert("Select an item first!");
			return;
		}
		// delete or move all cards within this deck
    	if( delAllCards==true )
    		mDb.mCards.deleteAllCards(mSelectedDeckId);
    	else
    		mDb.mCards.moveCardsFromDeck(mSelectedDeckId,0);
    	// delete deck
    	mDb.mDecks.deleteDeck(mSelectedDeckId);
        fillData();
		alert("Deck deleted");
    }

    private void alert(String text) {
		Context context = getApplicationContext();
    	int duration = Toast.LENGTH_SHORT;
    	Toast toast = Toast.makeText(context, text, duration);
    	toast.setGravity(Gravity.CENTER, 0, 0);
    	toast.show();    	
    }
}
