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
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class FlashCardsListView extends ListActivity {
	// Activities called for results
    private static final int ACTIVITY_CREATE_CARD	= 0;
    private static final int ACTIVITY_EDIT_CARD		= 1;

    // Dialogs displayed 
    private static final int DIALOG_CARD_OPTIONS		= 0;
    private static final int DIALOG_CONFIRM_REMOVE_CARD	= 1;

    // Options menu
    private static final int OMNU_IMPORT_CARDS	= Menu.FIRST;
    private static final int OMNU_CREATE_CARD 	= Menu.FIRST + 1;
    private static final int OMNU_EDIT_MANY 	= Menu.FIRST + 2;
    private static final int OMNU_SETTINGS 		= Menu.FIRST + 3;

    private FlashCardsDbAdapter mDb;
    private long mSelectedItemId;
    private Long mDeckId;

    // Called when the activity is starting
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_view); // this is not mandatory, but we want a custom view
        mDb = new FlashCardsDbAdapter(this);
        mDb.open();

    	mDeckId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(DecksTable.KEY_DECKID);
		if (mDeckId == null) {
			Bundle extras = getIntent().getExtras();
			mDeckId = extras != null ? extras.getLong(DecksTable.KEY_DECKID)
									: null;
		}
		mSelectedItemId = -1;
        fillData();
	}

    // Initialize the contents of the Activity's standard options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0,	OMNU_IMPORT_CARDS,	Menu.NONE,	R.string.menu_import_cards_for_deck);
        menu.add(0,	OMNU_CREATE_CARD,	Menu.NONE,	R.string.menu_create_card);
        menu.add(1,	OMNU_EDIT_MANY,		Menu.NONE,	R.string.menu_edit_many);
        menu.add(1,	OMNU_SETTINGS,		Menu.NONE,	R.string.menu_settings);
        return true;
    }

    //  This is called right before the menu is shown, every time it is shown.
    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
    	menu.setGroupEnabled(1, getListView().getCount()>0 );
    	return true;
    }

    // Callback for creating dialogs that are managed (saved and restored) for you by the activity.
	@Override
	protected Dialog onCreateDialog(int id) {
	    Dialog dialog = null;
	    switch(id) {
	    case DIALOG_CARD_OPTIONS:
	    	dialog = createCardOptionsDialog();
	        break;
	    case DIALOG_CONFIRM_REMOVE_CARD:
	    	dialog = createRemoveCardConfirm();
	        break;
	    default:
	        dialog = null;
	    }
	    return dialog;
	}
	
    // This hook is called whenever an item in your options menu is selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	int id = item.getItemId(); 
        switch( id ) {
            case OMNU_IMPORT_CARDS:
                importCards();
                return true;
            case OMNU_CREATE_CARD:
            	createCard(null);
                return true;
            case OMNU_EDIT_MANY:
            	editMany();
                return true;
            case OMNU_SETTINGS:
            	alert("TODO: Settings");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // This method will be called when an item in the list is selected. Subclasses should override. 
    // Subclasses can call getListView().getItemAtPosition(position) if they need to access the data associated with the selected item.
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        mSelectedItemId = id;
        showDialog(DIALOG_CARD_OPTIONS);
    }
    
    // This method will be called when an item in the list is selected. Subclasses should override. 
    // Subclasses can call getListView().getItemAtPosition(position) if they need to access the data associated with the selected item.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch(requestCode) {
        case ACTIVITY_CREATE_CARD:
        	if( resultCode==Activity.RESULT_OK )
        		alert("Card created");
        	break;
        case ACTIVITY_EDIT_CARD:
        	if( resultCode==Activity.RESULT_OK )
        		alert("Card updated");
        	break;
        }
        fillData(); // refresh list
    }
	
	// Create options dialog for card 
	private AlertDialog createCardOptionsDialog() {
    	final CharSequence[] items = {
	    		getResources().getText(R.string.menu_edit_card), 
	    		getResources().getText(R.string.menu_remove_card)
	    };
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(R.string.lbl_card_options);
    	builder.setItems(
    		items, 
    		new DialogInterface.OnClickListener() {
	    	    public void onClick(DialogInterface dialog, int item) {
	    	    	switch( item ) {
	    	    	case 0:
	    	    		editCard();
	    	    		dialog.dismiss();
	    	    		break;
	    	    	case 1:
	    	    		showDialog(DIALOG_CONFIRM_REMOVE_CARD);
	    	    		dialog.dismiss();
	    	    		break;
	    	    	}
	    	    }
	    	}
    	);
    	return builder.create();
	}

	private AlertDialog createRemoveCardConfirm() {
		AlertDialog.Builder dlg = new AlertDialog.Builder(this);
        dlg.setTitle(R.string.lbl_remove_card_confirm);
        dlg.setPositiveButton(R.string.lbl_accept, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	removeCard();
            	dialog.dismiss();
            }
        });
        dlg.setNegativeButton(R.string.lbl_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	dialog.dismiss();
            }
        });
        return dlg.create();
	}

    // Populate list view
    private void fillData() {
        Cursor notesCursor = mDb.mCards.fetchAllDeckCards(mDeckId,CardsTable.KEY_FRONT);
        startManagingCursor(notesCursor);

        // Create an array to specify the fields we want to display in the list (only TITLE)
        String[] from = new String[]{CardsTable.KEY_FRONT, CardsTable.KEY_BACK};

        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{R.id.text1, R.id.text2};

        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter notes = new SimpleCursorAdapter(
        		this, 
        		R.layout.list_row, 
        		notesCursor, 
        		from, 
        		to
        );
        setListAdapter(notes);
    }

    public void createCard(View v) {
        Intent intent = new Intent(this, CardEdit.class);
		intent.putExtra(CardsTable.KEY_DECKID, mDeckId);
		startActivityForResult(intent, ACTIVITY_CREATE_CARD);
    }

    private void editCard() {
        Intent intent = new Intent(this, CardEdit.class);
		intent.putExtra(CardsTable.KEY_DECKID, mDeckId);
        intent.putExtra(CardsTable.KEY_ROWID, mSelectedItemId);
        startActivityForResult(intent, ACTIVITY_EDIT_CARD);
    }

    private void editMany() {
        Intent i = new Intent(this, FlashCardsMSelectListView.class);
        startActivity(i);    	
    }

    public void removeCard(){
    	if( mSelectedItemId!=-1 ) {
	        mDb.mCards.deleteCard(mSelectedItemId);
	        mSelectedItemId = -1;
	        fillData();
    		alert("Card removed");
    	}
    	else 
    		alert("Select an item");
    }
    
    private void importCards(){
        Intent i = new Intent(this, Import.class);
		i.putExtra(DecksTable.KEY_DECKID, mDeckId);
        startActivity(i);
    }

	// Displays a toast
    private void alert(String text) {
		Context context = getApplicationContext();
    	int duration = Toast.LENGTH_SHORT;
    	Toast toast = Toast.makeText(context, text, duration);
    	toast.setGravity(Gravity.CENTER, 0, 0);
    	toast.show();    	
    }
}
