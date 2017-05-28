package samuelpalmer.sensorautorotation.processes.ui.feedback;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import samuelpalmer.common.IntentExtensions;
import samuelpalmer.sensorautorotation.ComponentType;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.UiActivity;

public class SendFeedbackActivity extends UiActivity {
	
	private NavigationView navigation;
	
	private final NavigationView.OnNavigationItemSelectedListener itemSelectedListener = new NavigationView.OnNavigationItemSelectedListener() {
		@SuppressWarnings("SameReturnValue")
		@Override
		public boolean onNavigationItemSelected(@NonNull MenuItem item) {
			switch (item.getItemId()) {
				case R.id.action_feedback_email: {
					Intent intent = ComponentType.create(EnterFeedbackActivity.class)
							.intent()
							.putExtra(EnterFeedbackActivity.EXTRA_TYPE, EnterFeedbackActivity.TYPE_FEEDBACK)
							.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //Don't want the user to lose what they've typed into the feedback screen when a different activity starts
					startActivity(intent);

					break;
				}
				case R.id.action_review: {
					Intent intent = appMarketIntent();
					
					try {
						startActivity(intent);
					} catch (ActivityNotFoundException ex) {
						Toast.makeText(SendFeedbackActivity.this, R.string.error_no_market_app, Toast.LENGTH_LONG).show();
					}

					break;
				}
				case R.id.action_report: {
					Intent intent = ComponentType.create(EnterFeedbackActivity.class)
							.intent()
							.putExtra(EnterFeedbackActivity.EXTRA_TYPE, EnterFeedbackActivity.TYPE_PROBLEM)
							.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //Don't want the user to lose what they've typed into the problem report when a different activity starts
					startActivity(intent);

					break;
				}
				default:
					throw new RuntimeException("Unknown menu item id: " + item.getItemId());
			}

			return false; //Never want to mark the item as selected since each menu item launches its own activity rather than updating this activity.
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_send_feedback);

		navigation = (NavigationView) findViewById(R.id.feedback_navigation);
		navigation.setNavigationItemSelectedListener(itemSelectedListener);
		
		updateReviewItemVisibility();
	}
	
	private void updateReviewItemVisibility() {
		Menu menu = navigation.getMenu();
		MenuItem reviewMenuItem = menu.findItem(R.id.action_review);
		if (reviewMenuItem == null)
			throw new RuntimeException("Couldn't find Review menu item.");
		
		if (IntentExtensions.activityDoesntExist(appMarketIntent(), getPackageManager()))
			menu.removeItem(reviewMenuItem.getItemId());
	}
	
	private Intent appMarketIntent() {
		return new Intent(Intent.ACTION_VIEW)
			.setData(Uri.parse("market://details?id=" + SendFeedbackActivity.this.getPackageName()));
	}

}
