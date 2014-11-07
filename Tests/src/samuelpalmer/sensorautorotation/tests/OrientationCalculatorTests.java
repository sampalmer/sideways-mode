package samuelpalmer.sensorautorotation.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import samuelpalmer.sensorautorotation.EnvironmentException;
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
		
		set(Orientation.LEFT_SIDE);
		
		screenShouldBe(Orientation.LEFT_SIDE);
	}

	private void set(Orientation orientation) {
		try {
			sut.setDevice(orientation);
		} catch (EnvironmentException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
    public void rotatingWithoutSensorCoveredShouldntCauseScreenRotation() {
		setState(Orientation.NATURAL, Orientation.NATURAL, false);
		
		set(Orientation.LEFT_SIDE);
		
		screenShouldBe(Orientation.NATURAL); 
    }
	
	@Test
	public void rotatingWhileSidewaysShouldWork() {
		setState(Orientation.NATURAL, Orientation.LEFT_SIDE, true);
		
		set(Orientation.UPSIDE_DOWN);
		
		screenShouldBe(Orientation.LEFT_SIDE); 
	}
	
	@Test
    public void supportsUserReturningFromSide() {
		setState(Orientation.NATURAL, Orientation.LEFT_SIDE, false);
		
		set(Orientation.NATURAL);
		
		screenShouldBe(Orientation.NATURAL); 
    }
	
	@Test
	public void supportsUserGoingOnSideWhileInLandscape() {
		setState(Orientation.LEFT_SIDE, Orientation.LEFT_SIDE, true);
		
		set(Orientation.UPSIDE_DOWN);
		
		screenShouldBe(Orientation.LEFT_SIDE);
	}
	
	@Test
	public void supportsUserReturningFromSideWhileInLandscape() {
		setState(Orientation.LEFT_SIDE, Orientation.UPSIDE_DOWN, true);
		
		set(Orientation.LEFT_SIDE);
		
		screenShouldBe(Orientation.LEFT_SIDE);
	}
	
	private void setState(Orientation screen, Orientation device, boolean covered) {
		try {
		this.screen = new Screen(new SystemSettings(new InMemorySystemSettingSource()));
		this.screen.set(screen);
		} catch (EnvironmentException e) {
			throw new RuntimeException(e);
		}
		
		sut = new OrientationCalculator(this.screen);
		set(device);
		sut.setCovered(covered);
	}
	
	private void screenShouldBe(Orientation orientation) {
		assertEquals(orientation, screen.get());
	}
}
