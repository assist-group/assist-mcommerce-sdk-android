package ru.assisttech.assistsdk;

import android.app.Activity;
import android.os.Bundle;
import androidx.core.app.NavUtils;
import android.view.MenuItem;

/**
 * Provides up button functionality.
 * <p>Parent activities stated in AndroidManifest.xml
 */
public class UpButtonActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onUpPressed();
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void onUpPressed() {
    }
}
