package samuelpalmer.sensorautorotation.components;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import samuelpalmer.sensorautorotation.android.AndroidSystemSettingSource;
import samuelpalmer.sensorautorotation.systemconfiguration.SettingWriteFailedException;
import samuelpalmer.sensorautorotation.systemconfiguration.SystemSettings;

@RunWith(RobolectricTestRunner.class)
public class RotationServiceTest {
	
	@Test
	public void preservesSystemAutoRotationSettingOnClose() throws SettingWriteFailedException {
		SystemSettings settings = new SystemSettings(new AndroidSystemSettingSource(Robolectric.application.getContentResolver()));
		settings.accelerometerRotation.set(true);
		
		Robolectric.buildService(RotationService.class)
			.attach()
			.create()
			.destroy();
		
		assertThat(settings.accelerometerRotation.getOrNull(), equalTo(true));
	}
}
