package com.jvitela.flashcards;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class FlashCards extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    public void onPlayButton(View v) {
        startActivity(new Intent(this, FlashCardsPlay.class));
    	/*Context context = getApplicationContext();
    	CharSequence text = "Play";
    	int duration = Toast.LENGTH_SHORT;
    	Toast toast = Toast.makeText(context, text, duration);
    	toast.setGravity(Gravity.CENTER, 0, 0);
    	toast.show();*/
    }

    public void onCardsButton(View v) {
        startActivity(new Intent(this, FlashCardsListView.class));
    }

    public void onDecksButton(View v) {
        startActivity(new Intent(this, FlashCardsDeckListView.class));
    	/*Context context = getApplicationContext();
    	CharSequence text = "Records";
    	int duration = Toast.LENGTH_SHORT;
    	Toast toast = Toast.makeText(context, text, duration);
    	toast.setGravity(Gravity.CENTER, 0, 0);
    	toast.show();*/
    }
    public void onImportButton(View v) {
    	
    }
}