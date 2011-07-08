package com.jvitela.flashcards;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

public class Play extends Activity {
	// Maximum and minimum Ratings for cards
	private static final int MAX_RATING = 10; 
	private static final int MIN_RATING = -10;

	// Saved States
	private static final String SAVED_STATE_OFFSET = "Offset";

    // Dialogs displayed 
    private static final int DIALOG_CONFIRM_EDIT_CARD	= 0;

    // Options menu
    private static final int OMNU_SETTINGS		= Menu.FIRST;
    private static final int OMNU_EDIT_CARD 	= Menu.FIRST + 1;

    // INVALIDATE Options
    private static final int INVALIDATE_ALL_FIELDS = 0;
    private static final int INVALIDATE_FRONT_FIELDS = 1;
    private static final int INVALIDATE_BACK_FIELDS = 2;

    private FlashCardsDbAdapter	mDb;
    private Cursor 				mCursor;
    private Long 				mDeckId;
    private int					mRating;
    private Long				mRowId;

    private int					mSuccessCount;
    private int					mFailCount;
    
    private ViewSwitcher		mSwitcher;
    private TextView 			mFrontTitle;
    private TextView 			mFrontDesc;
    private TextView 			mBackTitle;
    private TextView 			mBackDesc;

    private Rotate3dAnimation	mRotationAnimIn;
    private Rotate3dAnimation	mRotationAnimOut;
    private Animation	mSlideAnimIn;
    private Animation	mSlideAnimOut;

    /** 
     * Called when the activity is starting. This is where most initialization should go
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.play_view);
        setTitle(R.string.lbl_play);
		
    	mFrontTitle = (TextView) findViewById(R.id.txtFrontTitle);
    	mFrontDesc = (TextView) findViewById(R.id.txtFrontDesc);

    	mBackTitle = (TextView) findViewById(R.id.txtBackTitle);
    	mBackDesc = (TextView) findViewById(R.id.txtBackDesc);
    	
        // Attempt to get DeckId
    	mDeckId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(CardsTable.KEY_DECKID);
		if (mDeckId == null) {
			Bundle extras = getIntent().getExtras();
			if( extras != null && extras.containsKey(CardsTable.KEY_DECKID) ) {
				mDeckId =  extras.getLong(CardsTable.KEY_DECKID);
			}
		}

		// Attempt to get offset
		Integer startOffset = (savedInstanceState == null) ? 0 :
            (Integer) savedInstanceState.getSerializable(SAVED_STATE_OFFSET);


        // Database
        mDb = new FlashCardsDbAdapter(this);
        mDb.open();

    	QuerySorter sort = new QuerySorter().asc(CardsTable.KEY_RATING).rand();
		mCursor =  mDb.mCards.fetchAllCards(mDeckId, sort.toString());
        startManagingCursor(mCursor);
        if( mCursor.getCount()<1 ) {
        	mCursor = null;
        	findViewById(R.id.frontBackSwitcher).setVisibility(View.GONE);
        	findViewById(android.R.id.empty).setVisibility(View.VISIBLE);
        	return;
        }

        if( startOffset==null || !mCursor.move(startOffset) || mCursor.isAfterLast() )
			mCursor.moveToFirst();

		invalidateFields(INVALIDATE_ALL_FIELDS);
    	
        mSuccessCount = 0;
        mFailCount = 0;

    	createSlideAnimations();
    }

    /** 
     * Creates cursor
     * This is a good place to begin animations, open exclusive-access devices (such as the camera), etc. 
     * I had to initialize the cursor here in order to retrieve a new one after the activity is back
     * from calling another activity, which caused the cursor to be lost
     *
    @Override
    protected void onResume(){
    	super.onResume();
    }*/

    /** 
     * Initialize the contents of the Activity's standard options menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE,	OMNU_EDIT_CARD,	Menu.NONE,	R.string.menu_edit_card);
        menu.add(Menu.NONE,	OMNU_SETTINGS,	Menu.NONE,	R.string.menu_settings);
        return ( mCursor!=null ); // whether show or hide the menu
    }

    /** 
     * Call-back for creating dialogs that are managed (saved and restored) for you by the activity.
     */
	@Override
	protected Dialog onCreateDialog(int id) {
	    Dialog dialog = null;
	    switch(id) {
	    case DIALOG_CONFIRM_EDIT_CARD:
	    	dialog = createEditCardConfirm();
	        break;
	    default:
	        dialog = null;
	    }
	    return dialog;
	}

    /** 
     * This method is called before an activity may be killed so that 
     * when it comes back some time in the future it can restore its state
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(CardsTable.KEY_DECKID, mDeckId);
        if( mCursor!=null ) {
        	outState.putSerializable(SAVED_STATE_OFFSET, mCursor.getPosition());
        }
    }

    /** 
     * This hook is called whenever an item in your options menu is selected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	int id = item.getItemId(); 
        switch( id ) {
        case OMNU_SETTINGS:
        	alert("TODO: Settings");
            return true;
        case OMNU_EDIT_CARD:
        	showDialog(DIALOG_CONFIRM_EDIT_CARD);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** 
     * Initializes the animations used to change from one card to another
     */
    private void createSlideAnimations(){
        mSlideAnimIn = AnimationUtils.loadAnimation(this, R.anim.push_left_in);
        mSlideAnimIn.setAnimationListener(new AnimationListener(){
    		public void onAnimationStart(Animation animation) {
				invalidateFields(INVALIDATE_FRONT_FIELDS);
    		}
    		public void onAnimationRepeat(Animation animation) {
    		}
    		public void onAnimationEnd(Animation animation) {
    			mSwitcher.setInAnimation(mRotationAnimIn);
    		}
    	});
        mSlideAnimOut = AnimationUtils.loadAnimation(this, R.anim.push_left_out);
        mSlideAnimOut.setAnimationListener(new AnimationListener(){
    		public void onAnimationStart(Animation animation) {
    		}
    		public void onAnimationRepeat(Animation animation) {
    		}
    		public void onAnimationEnd(Animation animation) {
				invalidateFields(INVALIDATE_BACK_FIELDS);
    			mSwitcher.setOutAnimation(mRotationAnimOut);
    		}
    	});    	
    }

    /**
     * Initializes the animations used to flip the card using a 3d effect
     */
    private void createRotate3dAnimations(){
        mSwitcher = (ViewSwitcher) findViewById(R.id.frontBackSwitcher);

        float centerX = mSwitcher.getWidth() / 2.0f;
        float centerY = mSwitcher.getHeight() / 2.0f;

        mRotationAnimIn = new Rotate3dAnimation(180, 360, centerX, centerY, 310.0f, true);
        mRotationAnimIn.setDuration(500);
        mRotationAnimIn.setInterpolator(new AccelerateInterpolator());

        mRotationAnimOut = new Rotate3dAnimation(0, 180, centerX, centerY, 310.0f, false);
        mRotationAnimOut.setDuration(500);
        mRotationAnimOut.setInterpolator(new AccelerateInterpolator());    	
    }

    /**
     * Builds a dialog to confirm card edition and game exit
     * @return AlertDialog built
     */
	private AlertDialog createEditCardConfirm() {
		AlertDialog.Builder dlg = new AlertDialog.Builder(this);
		dlg.setTitle(R.string.menu_edit_card);
        dlg.setMessage(R.string.lbl_edit_card_confirm);
        dlg.setPositiveButton(R.string.lbl_accept, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	onAddCards(null);
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

    /**
     *  Switches from front to back face and vice-versa
     *  We have to instantiate the animations here because on the Create event we 
     *  don't have the width and height of the container, required for the animation.
     * @param v:	The view that called this event
     */
    public void onSwitchView(View v) {
    	if( mSwitcher==null ) {
    		createRotate3dAnimations();
    	}
        mSwitcher.setInAnimation( mRotationAnimIn );
        mSwitcher.setOutAnimation( mRotationAnimOut );    		
    	mSwitcher.showNext();
    }

    /** 
     * Move to next card
     * This changes the current animation to Slide In and Out, after this animations are done, 
     * the 3d animations are set back again, this is done by the custom animation itself, @see #createSlideAnimations()
     */
    public void onMoveNext() {
    	if( mCursor.moveToNext() ) {
    		mSwitcher.setInAnimation(mSlideAnimIn);
    		mSwitcher.setOutAnimation(mSlideAnimOut);
    		mSwitcher.showNext();
    	}
    	else {
    		// Launch Play-Results activity
    		Intent intent = new Intent(this,Results.class);
    		intent.putExtra(Results.SUCCESSES, mSuccessCount);
    		intent.putExtra(Results.FAILS, mFailCount);
    		intent.putExtra(DecksTable.KEY_DECKID, mDeckId);
    		startActivity(intent);
    		// finish current activity
    		finish();
    	}
    }

    /** 
     * Success button event
     * @param v:	The view that called this event
     */
    public void onSuccess(View v) {
        ++mSuccessCount;
    	saveState(1);
    	onMoveNext();
    }
    /** 
     * Fail button event
     * @param v:	The view that called this event
     */
    public void onFail(View v) {
        ++mFailCount;
    	saveState(-1);
    	onMoveNext();
    }

    /**
     * Go to add cards activity and finish current activity
     * @param v
     */
    public void onAddCards(View v){
    	Intent intent = new Intent( getApplicationContext(),CardEdit.class);
    	intent.putExtra(CardsTable.KEY_DECKID, mDeckId);
    	if( mRowId!=null )
    		intent.putExtra(CardsTable.KEY_ROWID, mRowId);
    	startActivity(intent);    	
    	finish();
    }

    /** 
     * Updates the class internal data with fresh data from the database
     * @param which:	Indicates which fields are to be updated, can be one of:
     * 					- INVALIDATE_ALL_FIELDS:	Updates all fields
     * 					- INVALIDATE_FRONT_FIELDS:	Updates only the fields of the front face
     * 					- INVALIDATE_BACK_FIELDS:	Updates only the fields of the back face
     * 					Also, in all cases updates the data related to current card rating and id
     */
    private void invalidateFields(int which) {
        if (mCursor != null) {
        	if( which==INVALIDATE_ALL_FIELDS || which==INVALIDATE_FRONT_FIELDS ) {
	            mFrontTitle.setText(mCursor.getString(
	            		mCursor.getColumnIndexOrThrow(CardsTable.KEY_FRONT)));
	            mFrontDesc.setText(mCursor.getString(
	            		mCursor.getColumnIndexOrThrow(CardsTable.KEY_FRONT_DESC)));
        	}

        	if( which==INVALIDATE_ALL_FIELDS || which==INVALIDATE_BACK_FIELDS ) {
	            mBackTitle.setText(mCursor.getString(
	            		mCursor.getColumnIndexOrThrow(CardsTable.KEY_BACK)));
	            mBackDesc.setText(mCursor.getString(
	            		mCursor.getColumnIndexOrThrow(CardsTable.KEY_BACK_DESC)));
        	}
            mRating = mCursor.getInt(mCursor.getColumnIndexOrThrow(CardsTable.KEY_RATING));
        	mRowId = mCursor.getLong(mCursor.getColumnIndexOrThrow(CardsTable.KEY_ROWID));
        }
    }

    /** 
     * Save to database
     * @param rating:	Current cart rating amount to be updated, this will only be updated if
     * 					resulting value is within the established range [MIN_RATING,MAX_RATING]
     */
    private void saveState(int rating) {
    	rating = mRating+rating;
    	// update only if rating is within established range
    	if( rating>=MIN_RATING && rating<=MAX_RATING ) {
    		mDb.mCards.updateCard(mRowId, rating);
    	}
    }

	/** 
	 * Displays a toast
	 * @param text:	Text of the toast
	 */
    private void alert(String text) {
		Context context = getApplicationContext();
    	int duration = Toast.LENGTH_SHORT;
    	Toast toast = Toast.makeText(context, text, duration);
    	toast.setGravity(Gravity.CENTER, 0, 0);
    	toast.show();    	
    }
}
