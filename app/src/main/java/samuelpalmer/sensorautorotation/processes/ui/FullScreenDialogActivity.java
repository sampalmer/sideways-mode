package samuelpalmer.sensorautorotation.processes.ui;

import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;

import samuelpalmer.sensorautorotation.R;

public abstract class FullScreenDialogActivity extends UiActivity {
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		ActionBar supportActionBar = getSupportActionBar();
		assert supportActionBar != null;
		supportActionBar.setDisplayHomeAsUpEnabled(true);
		supportActionBar.setHomeAsUpIndicator(R.drawable.close);
		
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onFullScreenDialogCloseButtonPressed();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Called when the "X" button in the top-left corner is pressed.
	 */
	protected void onFullScreenDialogCloseButtonPressed() {
		finish();
	}
	
}
