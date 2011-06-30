package com.jvitela.flashcards;

import android.app.Activity;
import android.content.Context;
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

public class FlashCardsPlay extends Activity {
	// Activities invoked
	private static final int ACTIVITY_EDIT_CARD = 0;
	// Saved States
	private static final String SAVED_STATE_OFFSET = "Offset";
    // Options menu
    private static final int OMNU_SETTINGS		= Menu.FIRST;
    private static final int OMNU_EDIT_CARD 	= Menu.FIRST + 1;
    
    private static final int OPT_ALL_FIELDS = 0;
    private static final int OPT_FRONT_FIELDS = 1;
    private static final int OPT_BACK_FIELDS = 2;

    private FlashCardsDbAdapter	mDb;
    private Cursor 				mCursor;
    private Long 				mDeckId;
    private int					mRating;
    private long				mRowId;

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

    // Called when the activity is starting. This is where most initialization should go
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.play_view);
        setTitle(R.string.lbl_play);

        // Database
        mDb = new FlashCardsDbAdapter(this);
        mDb.open();
    	
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
		Integer offset = (savedInstanceState == null) ? 0 :
            (Integer) savedInstanceState.getSerializable(SAVED_STATE_OFFSET);

		mCursor =  mDb.mCards.fetchAllDeckCards(mDeckId, CardsTable.KEY_RATING);
        startManagingCursor(mCursor);
        if( mCursor.getCount()<1 ) {
        	mCursor = null;
        	findViewById(R.id.frontBackSwitcher).setVisibility(View.GONE);
        	findViewById(android.R.id.empty).setVisibility(View.VISIBLE);
        	return;
        }

		if( offset==null || !mCursor.move(offset) || mCursor.isAfterLast() )
			mCursor.moveToFirst();

    	mFrontTitle = (TextView) findViewById(R.id.txtFrontTitle);
    	mFrontDesc = (TextView) findViewById(R.id.txtFrontDesc);

    	mBackTitle = (TextView) findViewById(R.id.txtBackTitle);
    	mBackDesc = (TextView) findViewById(R.id.txtBackDesc);
		invalidateFields(OPT_ALL_FIELDS);
    	
        mSuccessCount = 0;
        mFailCount = 0;

    	createSlideAnimations();
    }

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
     * This method is called before an activity may be killed so that 
     * when it comes back some time in the future it can restore its state
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(CardsTable.KEY_DECKID, mDeckId);
        if( mCursor!=null )
        	outState.putSerializable(SAVED_STATE_OFFSET, mCursor.getPosition());
    }

    /** 
     * This method will be called when an item in the list is selected. Subclasses should override. 
     * Subclasses can call getListView().getItemAtPosition(position) 
     * if they need to access the data associated with the selected item.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch(requestCode) {
        case ACTIVITY_EDIT_CARD:
        	if( resultCode==Activity.RESULT_OK ) {
        		alert("Card updated");
        		invalidateFields(OPT_ALL_FIELDS); // refresh all fields
        	}
        	break;
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
        	Intent intent = new Intent(this,CardEdit.class);
        	intent.putExtra(CardsTable.KEY_DECKID, mDeckId);
        	intent.putExtra(CardsTable.KEY_ROWID, mRowId);
        	startActivityForResult(intent,ACTIVITY_EDIT_CARD);
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
				invalidateFields(OPT_FRONT_FIELDS);
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
				invalidateFields(OPT_BACK_FIELDS);
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
     */
    public void onMoveNext() {
    	if( mCursor.moveToNext() ) {
    		mSwitcher.setInAnimation(mSlideAnimIn);
    		mSwitcher.setOutAnimation(mSlideAnimOut);
    		mSwitcher.showNext();
    	}
    	else {
    		// TODO: Launch Play-Results activity  
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
        --mFailCount;
    	saveState(-1);
    	onMoveNext();
    }

    /** 
     * Updates the class internal data with fresh data from the database
     * @param which:	Indicates which fields are to be updated, can be one of:
     * 					- OPT_ALL_FIELDS:	Updates all fields
     * 					- OPT_FRONT_FIELDS:	Updates only the fields of the front face
     * 					- OPT_BACK_FIELDS:	Updates only the fields of the back face
     * 					Also, in all cases updates the data related to current card rating and id
     */
    private void invalidateFields(int which) {
        if (mCursor != null) {
        	if( which==OPT_ALL_FIELDS || which==OPT_FRONT_FIELDS ) {
	            mFrontTitle.setText(mCursor.getString(
	            		mCursor.getColumnIndexOrThrow(CardsTable.KEY_FRONT)));
	            mFrontDesc.setText(mCursor.getString(
	            		mCursor.getColumnIndexOrThrow(CardsTable.KEY_FRONT_DESC)));
        	}

        	if( which==OPT_ALL_FIELDS || which==OPT_BACK_FIELDS ) {
	            mBackTitle.setText(mCursor.getString(
	            		mCursor.getColumnIndexOrThrow(CardsTable.KEY_BACK)));
	            mBackDesc.setText(mCursor.getString(
	            		mCursor.getColumnIndexOrThrow(CardsTable.KEY_BACK_DESC)));
        	}
            mRating = mCursor.getInt(mCursor.getColumnIndexOrThrow(CardsTable.KEY_RATING));
        	mRowId = mCursor.getInt(mCursor.getColumnIndexOrThrow(CardsTable.KEY_ROWID));
        }
    }

    /** 
     * Save to database
     * @param rating:	Current cart rating amount to be updated
     */
    private void saveState(int rating) {
    	rating = mRating+rating;
    	if( rating<0 ) rating=0;
        mDb.mCards.updateCard(mRowId, rating);
    }

    /** 
     * Calls the activity to add cards
     * @param v
     */
    public void onAddCards(View v) {
    	Intent intent = new Intent(this,CardEdit.class);
    	intent.putExtra(CardsTable.KEY_DECKID, mDeckId);
    	startActivity(intent);
    	finish();
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
