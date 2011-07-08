package com.jvitela.flashcards;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class Results extends Activity {
	public final static String	SUCCESSES = "Successes";
	public final static String	FAILS = "Fails";
	
	private Long 		mDeckId;
	private Integer		mSuccesses;
	private Integer		mFails;

    /** 
     * Called when the activity is starting. 
     * This is where most initialization should go
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.results);

		Bundle extras = getIntent().getExtras();
        // Attempt to get DeckId
		if( savedInstanceState!=null ) {
			mDeckId = (Long) savedInstanceState.getSerializable(CardsTable.KEY_DECKID);
		}
		if( mDeckId == null ) {
			if( extras != null && extras.containsKey(DecksTable.KEY_DECKID) ) {
				mDeckId =  extras.getLong(DecksTable.KEY_DECKID);
			}
		}
		// Attempt to get Successes
		if( savedInstanceState!=null ){
			mSuccesses = (Integer) savedInstanceState.getSerializable(SUCCESSES);
		}
		if( mSuccesses == null ) {
			if( extras != null && extras.containsKey(SUCCESSES) ) {
				mSuccesses =  extras.getInt(SUCCESSES);
			}
		}
		// Attempt to get Fails
		if( savedInstanceState!=null ){
			mFails = (Integer) savedInstanceState.getSerializable(FAILS);
		}
		if( mFails == null ) {
			if( extras != null && extras.containsKey(FAILS) ) {
				mFails =  extras.getInt(FAILS);
			}
		}
		
		// Initialize Graphic
		PieChartView pie = (PieChartView) findViewById(R.id.pieChart);
		pie.setValues(mSuccesses, mFails);

		// Labels
		//float total =  mSuccesses+mFails;
		TextView txt = (TextView)findViewById(R.id.txtSuccesses);
		//float percent = (mSuccesses/total)*100;
		txt.setText( String.format( getString(R.string.lbl_successes), mSuccesses) );
		txt = (TextView)findViewById(R.id.txtFails);
		//percent = (mFails/total)*100;
		txt.setText( String.format( getString(R.string.lbl_fails), mFails) );
    }

    /** 
     * This method is called before an activity may be killed so that 
     * when it comes back some time in the future it can restore its state
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(CardsTable.KEY_DECKID, mDeckId);
    	outState.putSerializable(SUCCESSES, mSuccesses);
    	outState.putSerializable(FAILS, mFails);
    }
}
