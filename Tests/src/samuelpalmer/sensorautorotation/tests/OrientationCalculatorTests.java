package samuelpalmer.sensorautorotation.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import samuelpalmer.sensorautorotation.Orientation;
import samuelpalmer.sensorautorotation.OrientationCalculator;
import samuelpalmer.sensorautorotation.Screen;
import samuelpalmer.sensorautorotation.systemconfiguration.SystemSettings;
import samuelpalmer.sensorautorotation.systemconfiguration.tests.InMemorySystemSettingSource;

public class OrientationCalculatorTests {
	private OrientationCalculator sut;
	private Screen screen;
	
	@Test
	public void rotatingIntoLandscapeWithSensorCoveredShouldCauseScreenRotation() {
		setState(Orientation.PORTRAIT, Orientation.PORTRAIT, true);
		
		sut.setDevice(Orientation.LANDSCAPE);
		
		screenShouldBe(Orientation.LANDSCAPE);
	}

	@Test
    public void rotatingWithoutSensorCoveredShouldntCauseScreenRotation() {
		setState(Orientation.PORTRAIT, Orientation.PORTRAIT, false);
		
		sut.setDevice(Orientation.LANDSCAPE);
		
		screenShouldBe(Orientation.PORTRAIT); 
    }
	
	@Test
	public void rotatingWhileSidewaysShouldWork() {
		setState(Orientation.PORTRAIT, Orientation.LANDSCAPE, true);
		
		sut.setDevice(Orientation.REVERSE_PORTRAIT);
		
		screenShouldBe(Orientation.LANDSCAPE); 
	}
	
	@Test
    public void supportsUserReturningFromSide() {
		setState(Orientation.PORTRAIT, Orientation.LANDSCAPE, false);
		
		sut.setDevice(Orientation.PORTRAIT);
		
		screenShouldBe(Orientation.PORTRAIT); 
    }
	
	@Test
	public void supportsUserGoingOnSideWhileInLandscape() {
		setState(Orientation.LANDSCAPE, Orientation.LANDSCAPE, true);
		
		sut.setDevice(Orientation.REVERSE_PORTRAIT);
		
		screenShouldBe(Orientation.LANDSCAPE);
	}
	
	@Test
	public void supportsUserReturningFromSideWhileInLandscape() {
		setState(Orientation.LANDSCAPE, Orientation.REVERSE_PORTRAIT, true);
		
		sut.setDevice(Orientation.LANDSCAPE);
		
		screenShouldBe(Orientation.LANDSCAPE);
	}
	
	@Test
	public void doesntSupportReversePortrait() {
		setState(Orientation.LANDSCAPE, Orientation.LANDSCAPE, false);
		
		sut.setDevice(Orientation.REVERSE_PORTRAIT);
		
		screenShouldBe(Orientation.LANDSCAPE);
	}
	
	@Test
	public void preservesOrientationWhenUserRecoversFromReversePortraitAttempt() {
		setState(Orientation.LANDSCAPE, Orientation.LANDSCAPE, false);
		
		sut.setDevice(Orientation.REVERSE_PORTRAIT);
		sut.setDevice(Orientation.LANDSCAPE);
		
		screenShouldBe(Orientation.LANDSCAPE);
	}
	
	@Test
	public void supportsOrientationChangesAfterRecoveryFromReversePortraitAttempt() {
		setState(Orientation.LANDSCAPE, Orientation.LANDSCAPE, false);
		sut.setDevice(Orientation.REVERSE_PORTRAIT);
		sut.setDevice(Orientation.LANDSCAPE);
		
		sut.setDevice(Orientation.PORTRAIT);
		
		screenShouldBe(Orientation.PORTRAIT);
	}
	
	private void setState(Orientation screen, Orientation device, boolean covered) {
		this.screen = new Screen(new SystemSettings(new InMemorySystemSettingSource()));
		this.screen.set(screen);
		
		sut = new OrientationCalculator(this.screen);
		sut.setDevice(device);
		sut.setCovered(covered);
	}
	
	private void screenShouldBe(Orientation orientation) {
		assertEquals(orientation, screen.get());
	}
}
