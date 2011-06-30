package com.jvitela.flashcards;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * 
 */
public class Import extends ListActivity {
	// Activities called for results
    private static final int ACTIVITY_CREATE_DECK 	= 0;

    // Dialogs
	private static final int DIALOG_PROGRESS = 0;
    
	// Constants
    private static final String		ROOT = "/sdcard/flashcards/";
    
    private List<String> 		mFilesList;
    private File				mCurDir;
    private SCVFileFilter		mFilter;
    private Long				mDeckId;
    private String				mFile;

    private ProgressDialog mProgressDialog;
    private Handler mProgressHandler;
    private Thread	mThread;

    /**
     * Filter for selecting only the CSV files
     */
    private class SCVFileFilter implements FileFilter {
    	public boolean accept(File f){
    		return (f.isFile() &&
    				f.canRead() &&
    				f.getName().endsWith("csv") );
    	}
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mFilesList = new ArrayList<String>();
        mFilter = new SCVFileFilter();
        mCurDir = new File(ROOT);
        if( !mCurDir.exists() ) { 
        	 mCurDir.mkdir();
        }
        
        setContentView( R.layout.list_view_import );

    	mDeckId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(DecksTable.KEY_DECKID);
		if (mDeckId == null) {
			Bundle extras = getIntent().getExtras();
			if( extras!=null && extras.containsKey(DecksTable.KEY_DECKID) )
				mDeckId = extras.getLong(DecksTable.KEY_DECKID);
		}

		/** 
		 * Handler to manage communication between the Process thread and the gui front-end
		 */
        mProgressHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Bundle data = msg.getData();
                switch( data.getInt(CSVFileImporter.ID_STATUS, 0) ) {
                case CSVFileImporter.STS_LOADING_DATA:
                    mProgressDialog.incrementProgressBy(1);
                	break;
                case CSVFileImporter.STS_DATA_LOADED:
                	int max = data.getInt(CSVFileImporter.ID_VALUE, 100);
                	mProgressDialog.setMax( max );
                	mProgressDialog.setMessage("Saving cards to database...");
                	mProgressDialog.setProgress(0);
                	break;
                case CSVFileImporter.STS_SAVING_DATA:
                    mProgressDialog.incrementProgressBy(1);
                	break;
                case CSVFileImporter.STS_DATA_SAVED:
                	mProgressDialog.setMessage("Done.");
                	mProgressDialog.dismiss();
                	// Return result
                	Intent extra = new Intent();
                	extra.putExtra(DecksTable.KEY_DECKID, mDeckId);
                	setResult(RESULT_OK,extra);
                	// finish activity
                	finish();
                	break;
                case CSVFileImporter.STS_ERROR:
                	alert( data.getString(CSVFileImporter.ID_MESSAGE) );
                	break;
                }
            }
        };
		
		fillData();
    }
    
    /** 
     * Call-back for creating dialogs that are managed (saved and restored) for you by the activity.
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DIALOG_PROGRESS:
        	if( mProgressDialog==null ) {
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setTitle(R.string.lbl_import);
            	mProgressDialog.setMessage("Loading cards from file...");
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setMax(100);
                mProgressDialog.setButton(getText(R.string.lbl_cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        /* User clicked Cancel so do some stuff */
                    	dismissDialog(DIALOG_PROGRESS);
                    	mThread.interrupt();
                    }
                });
        	}
        	return mProgressDialog;
        }
        return super.onCreateDialog(id);
    }

    /** 
     * Provides an opportunity to prepare a managed dialog before it is being shown
     */
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch(id) {
        case DIALOG_PROGRESS:
        	if( mThread!=null ) return;
        	mProgressDialog.setProgress(0);
        	mThread = new CSVFileImporter(this, mFile, mDeckId, mProgressHandler);
        	mThread.start();
        	return;
        }
    }

    /**
     *  This method will be called when an item in the list is selected. Subclasses should override. 
     *	Subclasses can call getListView().getItemAtPosition(position) if they need to access the data associated with the selected item.
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
    	mFile = ROOT+mFilesList.get(position);
        if( mDeckId==null ) {
    		Intent intent = new Intent(this, DeckEdit.class);
    		startActivityForResult(intent,ACTIVITY_CREATE_DECK);
        }
        else if( mThread==null ) {
        	showDialog(DIALOG_PROGRESS);
        }
    }

    /** 
     * Gets result of an invoked activity
     */
    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	switch( requestCode ) {
    	case ACTIVITY_CREATE_DECK:
    		if( resultCode==Activity.RESULT_OK ) {
     			mDeckId = data.getLongExtra(DecksTable.KEY_DECKID, 0);
    			showDialog(DIALOG_PROGRESS);
    		}
    		break;
    	}
    }

    /** 
     * Populates list rows
     */
    private void fillData()
    {
    	mFilesList.clear();
		File[] files = mCurDir.listFiles(mFilter);

		for(int i=0; i<files.length; i++) {
			File file = files[i];
			mFilesList.add(file.getName());
		}

		ArrayAdapter<String> fileList = new ArrayAdapter<String>(
				this, 
				android.R.layout.simple_list_item_1, 
				mFilesList
		);
		setListAdapter(fileList);
    }

	/** 
	 * Displays a toast
	 * @param text
	 */
    private void alert(String text) {
		Context context = getApplicationContext();
    	int duration = Toast.LENGTH_SHORT;
    	Toast toast = Toast.makeText(context, text, duration);
    	toast.setGravity(Gravity.CENTER, 0, 0);
    	toast.show();    	
    }
}
