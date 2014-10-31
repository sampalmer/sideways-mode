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
		setState(Orientation.NATURAL, Orientation.NATURAL, true);
		
		sut.setDevice(Orientation.LEFT_SIDE);
		
		screenShouldBe(Orientation.LEFT_SIDE);
	}

	@Test
    public void rotatingWithoutSensorCoveredShouldntCauseScreenRotation() {
		setState(Orientation.NATURAL, Orientation.NATURAL, false);
		
		sut.setDevice(Orientation.LEFT_SIDE);
		
		screenShouldBe(Orientation.NATURAL); 
    }
	
	@Test
	public void rotatingWhileSidewaysShouldWork() {
		setState(Orientation.NATURAL, Orientation.LEFT_SIDE, true);
		
		sut.setDevice(Orientation.UPSIDE_DOWN);
		
		screenShouldBe(Orientation.LEFT_SIDE); 
	}
	
	@Test
    public void supportsUserReturningFromSide() {
		setState(Orientation.NATURAL, Orientation.LEFT_SIDE, false);
		
		sut.setDevice(Orientation.NATURAL);
		
		screenShouldBe(Orientation.NATURAL); 
    }
	
	@Test
	public void supportsUserGoingOnSideWhileInLandscape() {
		setState(Orientation.LEFT_SIDE, Orientation.LEFT_SIDE, true);
		
		sut.setDevice(Orientation.UPSIDE_DOWN);
		
		screenShouldBe(Orientation.LEFT_SIDE);
	}
	
	@Test
	public void supportsUserReturningFromSideWhileInLandscape() {
		setState(Orientation.LEFT_SIDE, Orientation.UPSIDE_DOWN, true);
		
		sut.setDevice(Orientation.LEFT_SIDE);
		
		screenShouldBe(Orientation.LEFT_SIDE);
	}
	
	@Test
	public void doesntSupportReversePortrait() {
		setState(Orientation.LEFT_SIDE, Orientation.LEFT_SIDE, false);
		
		sut.setDevice(Orientation.UPSIDE_DOWN);
		
		screenShouldBe(Orientation.LEFT_SIDE);
	}
	
	@Test
	public void preservesOrientationWhenUserRecoversFromReversePortraitAttempt() {
		setState(Orientation.LEFT_SIDE, Orientation.LEFT_SIDE, false);
		
		sut.setDevice(Orientation.UPSIDE_DOWN);
		sut.setDevice(Orientation.LEFT_SIDE);
		
		screenShouldBe(Orientation.LEFT_SIDE);
	}
	
	@Test
	public void supportsOrientationChangesAfterRecoveryFromReversePortraitAttempt() {
		setState(Orientation.LEFT_SIDE, Orientation.LEFT_SIDE, false);
		sut.setDevice(Orientation.UPSIDE_DOWN);
		sut.setDevice(Orientation.LEFT_SIDE);
		
		sut.setDevice(Orientation.NATURAL);
		
		screenShouldBe(Orientation.NATURAL);
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
