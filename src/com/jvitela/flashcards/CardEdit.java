package com.jvitela.flashcards;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ViewSwitcher;

public class CardEdit extends Activity {
	// Dialogs
    private static final int DIALOG_EDIT_FRONT_TITLE 	= 0;
    private static final int DIALOG_EDIT_FRONT_DESC 	= 1;
    private static final int DIALOG_EDIT_BACK_TITLE 	= 2;
    private static final int DIALOG_EDIT_BACK_DESC 		= 3;

    // Options menu
    private static final int OMNU_SETTINGS	= Menu.FIRST;

    private Long 				mRowId;
    private Long 				mDeckId;
    private FlashCardsDbAdapter	mDb;
    private ViewSwitcher		mSwitcher;
    private Button 				mFrontTitle;
    private Button 				mFrontDesc;
    private Button 				mBackTitle;
    private Button 				mBackDesc;

    // Called when the activity is starting. This is where most initialization should go
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Database
        mDb = new FlashCardsDbAdapter(this);
        mDb.open();

        setContentView(R.layout.card_edit);
        setTitle(R.string.edit_card);

    	mFrontTitle = (Button) findViewById(R.id.btnFrontTitle);
    	mFrontDesc = (Button) findViewById(R.id.btnFrontDesc);

    	mBackTitle = (Button) findViewById(R.id.btnBackTitle);
    	mBackDesc = (Button) findViewById(R.id.btnBackDesc);

    	mDeckId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(CardsTable.KEY_DECKID);
		if (mDeckId == null) {
			Bundle extras = getIntent().getExtras();
			if( extras != null && extras.containsKey(CardsTable.KEY_DECKID) ) {
				mDeckId =  extras.getLong(CardsTable.KEY_DECKID);
			}
		}

        mRowId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(CardsTable.KEY_ROWID);
		if (mRowId == null) {
			Bundle extras = getIntent().getExtras();
			if( extras!=null && extras.containsKey(CardsTable.KEY_ROWID) )
				mRowId = extras.getLong(CardsTable.KEY_ROWID);
		}

		populateFields();
    }

    // Initialize the contents of the Activity's standard options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE,	OMNU_SETTINGS,	Menu.NONE,	R.string.menu_settings);
        return true;
    }

    /**
     *	Create a dialog, this is only called once per dialog 
     */
	@Override
	protected Dialog onCreateDialog(int id) {
	    Dialog dialog = null;
	    String title = null;
	    Button target = null;
	    Resources res = getResources();
	    switch(id) {
	    case DIALOG_EDIT_FRONT_TITLE:
	    	title = res.getString(R.string.lbl_front_title);
	    	target = (Button) findViewById(R.id.btnFrontTitle);
	    	dialog = new EditCardTextDialog( this, title, target);
	        break;
	    case DIALOG_EDIT_FRONT_DESC:
	    	title = res.getString(R.string.lbl_front_desc);
	    	target = (Button) findViewById(R.id.btnFrontDesc);
	    	dialog = new EditCardTextDialog( this, title, target);
	        break;
	    case DIALOG_EDIT_BACK_TITLE:
	    	title = res.getString(R.string.lbl_back_title);
	    	target = (Button) findViewById(R.id.btnBackTitle);
	    	dialog = new EditCardTextDialog( this, title, target);
	        break;
	    case DIALOG_EDIT_BACK_DESC:
	    	title = res.getString(R.string.lbl_back_desc);
	    	target = (Button) findViewById(R.id.btnBackDesc);
	    	dialog = new EditCardTextDialog( this, title, target);
	        break;
	    default:
	        dialog = null;
	    }
	    return dialog;
	}

    // This method is called before an activity may be killed so that 
    // when it comes back some time in the future it can restore its state
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(CardsTable.KEY_DECKID, mDeckId);
        outState.putSerializable(CardsTable.KEY_ROWID, mRowId);
    }
	
    // This hook is called whenever an item in your options menu is selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	int id = item.getItemId(); 
        switch( id ) {
            case OMNU_SETTINGS:
            	alert("TODO: Settings");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 
     */
    private class 		EditCardTextDialog 
    		extends		AlertDialog 
    		implements	DialogInterface.OnClickListener {

    	private Button		mTargetBtn;
    	private EditText	mText;
    	private String		mTitle;

    	// Constructor
    	public EditCardTextDialog(Context context, String title, Button target) {
    		super(context);
    		mTargetBtn = target;
    		mTitle = title;
    	}

    	// 
    	public void onClick(DialogInterface dialog,int which) {
    		switch( which ) {
    		case Dialog.BUTTON_POSITIVE:
    			if( mTargetBtn!=null )
    				mTargetBtn.setText(mText.getText());
    			break;
    		}
    	}

    	@Override
    	public void show(){
            mText.setText( mTargetBtn.getText() ); // refresh

    		super.show(); // show the dialog

    		// Replace original listener in order to avoid auto dismiss
    		final Button btn = getButton(DialogInterface.BUTTON_NEUTRAL); 
    		btn.setOnClickListener(new View.OnClickListener() { 
    			public void onClick(View v) { 
    				mText.setText(""); 
    			}
    		});
    	}

    	/**
    	 * Creates a dialog to confirm the remove of all cards
    	 * @return AlertDialog object
    	 */
    	protected void onCreate(Bundle savedInstanceState) {
    		setTitle(mTitle);
    		//setCancelable(false);
    		
            mText = new EditText( getContext() );
            mText.setText( mTargetBtn.getText() );
            setView( mText );
            
            String txt = getResources().getString(R.string.lbl_accept);
            setButton(Dialog.BUTTON_POSITIVE,txt, this);
            
            txt = getResources().getString(R.string.lbl_clear);
            setButton(Dialog.BUTTON_NEUTRAL,txt, this);
            
            txt = getResources().getString(R.string.lbl_cancel);
            setButton(Dialog.BUTTON_NEGATIVE,txt, this);
            
    		super.onCreate(savedInstanceState);
    	}
    };
	
    // Switches from front to back face and vice-versa
    // We have to instantiate the animations here because on the Create event we 
    // don't have the width and height of the container, required for the animation
    public void onSwitchView(View v) {
    	if( mSwitcher==null ) {
            mSwitcher = (ViewSwitcher) findViewById(R.id.frontBackSwitcher);

            float centerX = mSwitcher.getWidth() / 2.0f;
            float centerY = mSwitcher.getHeight() / 2.0f;

            Rotate3dAnimation rotationIn = new Rotate3dAnimation(180, 360, centerX, centerY, 310.0f, true);
            rotationIn.setDuration(500);
            rotationIn.setInterpolator(new AccelerateInterpolator());

            Rotate3dAnimation rotationOut = new Rotate3dAnimation(0, 180, centerX, centerY, 310.0f, false);
            rotationOut.setDuration(500);
            rotationOut.setInterpolator(new AccelerateInterpolator());

            mSwitcher.setInAnimation( rotationIn );
            mSwitcher.setOutAnimation( rotationOut );    		
    	}
    	mSwitcher.showNext();
    }

    // Save button event
    public void onSaveButton(View v) {
    	saveState();
        setResult(RESULT_OK);
        finish();
    }

    // Save and Add More Button event
    public void onAddMoreButton(View v) {
    	saveState();
    	mRowId = null;
    	clearFields();
    	alert("Card saved");
    }
    
    // Cancel button event
    public void onCancelButton(View v) {
        setResult(RESULT_CANCELED);
    	finish();
    }

    //
    public void onEditCardText(View view) {
    	switch( view.getId() ) {
    	case R.id.btnFrontTitle:
    		showDialog(DIALOG_EDIT_FRONT_TITLE);
    		break;
    	case R.id.btnFrontDesc:
    		showDialog(DIALOG_EDIT_FRONT_DESC);
    		break;
    	case R.id.btnBackTitle:
    		showDialog(DIALOG_EDIT_BACK_TITLE);
    		break;
    	case R.id.btnBackDesc:
    		showDialog(DIALOG_EDIT_BACK_DESC);
    		break;
    	}
    }

    // fill in the form with database values
    private void populateFields() {
        if (mRowId != null) {
            Cursor card = mDb.mCards.fetchCard(mRowId);
            //startManagingCursor(card);

            mFrontTitle.setText(card.getString(
            		card.getColumnIndexOrThrow(CardsTable.KEY_FRONT)));
            mFrontDesc.setText(card.getString(
            		card.getColumnIndexOrThrow(CardsTable.KEY_FRONT_DESC)));

            mBackTitle.setText(card.getString(
            		card.getColumnIndexOrThrow(CardsTable.KEY_BACK)));
            mBackDesc.setText(card.getString(
            		card.getColumnIndexOrThrow(CardsTable.KEY_BACK_DESC)));
        }
    }

    // reset the form
    private void clearFields() {
    	mFrontTitle.setText( "" );
    	mFrontDesc.setText( "" );

    	mBackTitle.setText( "" );
    	mBackDesc.setText( "" );
    }

    // Save to database
    private void saveState() {
    	// Add a new card
        if (mRowId == null) {
        	mRowId = mDb.mCards.createCard(
        			mFrontTitle.getText().toString(), 
        			mFrontDesc.getText().toString(),
        			mBackTitle.getText().toString(),
        			mBackDesc.getText().toString(),
        			mDeckId
        	);
            if( mRowId<=0 ) {
                mRowId = null;
            }
        } 
        // Update existent card
        else {
            mDb.mCards.updateCard(
            		mRowId.longValue(),
        			mFrontTitle.getText().toString(), 
        			mFrontDesc.getText().toString(),
        			mBackTitle.getText().toString(),
        			mBackDesc.getText().toString(),
        			mDeckId.longValue()
            );
        }
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
