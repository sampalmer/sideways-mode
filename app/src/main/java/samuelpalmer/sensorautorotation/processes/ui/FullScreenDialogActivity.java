package samuelpalmer.sensorautorotation.processes.ui;

import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.ActionBar;
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
            onFullScreenDialogClosing();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * Default implementation calls {@link #onFullScreenDialogClosing()}.
     */
    @Override
    public void onBackPressed() {
        onFullScreenDialogClosing();
        // Not calling superclass implementation since that would finish the activity.
    }
    
    /**
     * Called when the user tries to close the dialog by either pressing the "X" button in the
     * top-left corner or pressing the device's 'back' button.
     * The default implementation finishes the activity.
     */
    protected void onFullScreenDialogClosing() {
        finish();
    }
    
}
