package samuelpalmer.sensorautorotation.processes.ui;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.view.MenuItem;
import android.widget.Toast;

import samuelpalmer.sensorautorotation.ComponentType;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.VersionInfo;

public class SendFeedbackActivity extends UiActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_send_feedback);

		NavigationView navigation = (NavigationView) findViewById(R.id.feedback_navigation);
		navigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
			@Override
			public boolean onNavigationItemSelected(@NonNull MenuItem item) {
				switch (item.getItemId()) {
					case R.id.action_feedback_email:
						new Email(
								SendFeedbackActivity.this,
								ReportProblemActivity.emailRecipients(),
								StringResource.format(R.string.email_feedback_subject, R.string.app_name_full, VersionInfo.packageInfo().versionName).toString(),
								""
						).send();

						break;
					case R.id.action_feedback_google_play: {
						Intent intent = new Intent(Intent.ACTION_VIEW)
								.setData(Uri.parse("market://details?id=" + SendFeedbackActivity.this.getPackageName()));
						try {
							startActivity(intent);
						} catch (ActivityNotFoundException ex) {
							Toast.makeText(SendFeedbackActivity.this, R.string.error_no_market_app, Toast.LENGTH_LONG).show();
						}

						break;
					}
					case R.id.action_report: {
						Intent intent = ComponentType.create(ReportProblemActivity.class)
								.intent()
								.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //Don't want the user to lose what they've typed into the problem report when a different activity starts
						startActivity(intent);

						break;
					}
					default:
						throw new RuntimeException("Unknown menu item id: " + item.getItemId());
				}

				return false;
			}
		});
	}

}
