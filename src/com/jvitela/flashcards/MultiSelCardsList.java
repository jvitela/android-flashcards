package com.jvitela.flashcards;

import java.util.HashSet;
import java.util.Iterator;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class MultiSelCardsList extends ListActivity {
    private static final int TODECK_ID = Menu.FIRST;
    private static final int DELETE_ID = Menu.FIRST + 1;

    private static final int DIALOG_MOVECARDSTODECK_ID=0;

    private FlashCardsDbAdapter mDb;
    private HashSet<Long>		mCheckedIds;
    private Long				mDeckId;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDb = new FlashCardsDbAdapter(this);
        mDb.open();

        mCheckedIds = new HashSet<Long>();
        fillData();
    }
    
    private void fillData() {
        //setContentView(R.layout.list_view); // use default android view
    	QuerySorter sort = new QuerySorter().asc(CardsTable.KEY_FRONT);
        Cursor notesCursor = mDb.mCards.fetchAllCards(mDeckId,sort.toString());
        startManagingCursor(notesCursor);
        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter notes = new SimpleCursorAdapter(
        		this, 
        		R.layout.list_row_multiselect,
        		notesCursor, 
        		new String[]{CardsTable.KEY_FRONT, CardsTable.KEY_BACK}, 
        		new int[]{ R.id.text1, R.id.text2 }
        );
        setListAdapter(notes);

        /* This creates trouble DO NOT USE!!!
        ListView listView = getListView();
        listView.setItemsCanFocus(false);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        */    	
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        ViewGroup row = (ViewGroup)v;
    	CheckedTextView check = (CheckedTextView) row.findViewById(R.id.ck1);
    	check.toggle();
    	if( check.isChecked() ) mCheckedIds.add(id);
    	else mCheckedIds.remove(id);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(1, TODECK_ID,   0, R.string.menu_todeck);
        menu.add(1, DELETE_ID, 0, R.string.menu_remove_card);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	int id = item.getItemId(); 
        switch( id ) {
            case TODECK_ID:
            	showDialog(DIALOG_MOVECARDSTODECK_ID);
                return true;
            case DELETE_ID:
            	long[] ids = getCheckedItemsIds();
            	for(int i=0; i<ids.length; ++i) {
            		mDb.mCards.deleteCard( ids[i] );
            	}
            	fillData();
            	mCheckedIds.clear();
            	Toast.makeText(getApplicationContext(), "Done", Toast.LENGTH_SHORT).show();
            	return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    public long[] getCheckedItemsIds() {
    	Iterator<Long> itr = mCheckedIds.iterator();
    	long[] lids = new long[mCheckedIds.size()];
    	for(int i=0; i<lids.length; ++i) {
    		lids[i] = itr.next();
    	}
    	return lids;
    }

	@Override
	protected Dialog onCreateDialog(int id) {
	    Dialog dialog = null;
	    switch(id) {
	    case DIALOG_MOVECARDSTODECK_ID:
	    	dialog = createDeckListDialog();
	        break;
	    default:
	        dialog = null;
	    }
	    return dialog;
	}
	
	private AlertDialog createDeckListDialog() {
    	Cursor curDecks = mDb.mDecks.fetchAllDecks();
    	CharSequence[] items = new CharSequence[ curDecks.getCount() ];
    	curDecks.moveToFirst();
    	for(int i=0; i<items.length; ++i) {
    		items[i] = curDecks.getString(curDecks.getColumnIndexOrThrow(DecksTable.KEY_NAME));
    		curDecks.moveToNext();
    	}

    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(R.string.menu_decks);
    	builder.setItems(
    		items,
    		new DialogInterface.OnClickListener() {
	    	    public void onClick(DialogInterface dialog, int item) {
	    	    	Cursor curDecks = mDb.mDecks.fetchAllDecks();
	    	    	curDecks.moveToPosition(item);
	    	    	long deckId = curDecks.getInt(curDecks.getColumnIndexOrThrow(DecksTable.KEY_DECKID));

	            	long[] ids = getCheckedItemsIds();
	            	for(int i=0; i<ids.length; ++i) {
	            		mDb.mCards.updateCard(
	            			ids[i], 
	            			deckId
	            		);
	            	}
	            	mCheckedIds.clear();
	            	fillData();
	            	Toast.makeText(getApplicationContext(), "Done", Toast.LENGTH_SHORT).show();
	            }
	    	}
    	);
    	return builder.create();
	}    
}
