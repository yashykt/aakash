package com.aakash.brickbounce;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class BrickBounceActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(new BrickBounce(this));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.brick_bounce, menu);
		return true;
	}

}
