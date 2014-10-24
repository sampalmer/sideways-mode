package samuelpalmer.sensorautorotation.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import samuelpalmer.sensorautorotation.Orientation;
import samuelpalmer.sensorautorotation.OrientationCalculator;

public class OrientationCoordinatorTests {
	private OrientationCalculator sut;
	
	@Test
	public void rotatingIntoLandscapeWithSensorCoveredShouldCauseScreenRotation() {
		holdProximitySensor();
		sut.registerNewDeviceOrientation(Orientation.LANDSCAPE);
		
		screenShouldBe(Orientation.LANDSCAPE);
	}

	private void holdProximitySensor() {
		sut.registerNewCoveredState(true);
	}
	
	@Test
    public void rotatingWithoutSensorCoveredShouldntCauseScreenRotation() {
		sut.registerNewDeviceOrientation(Orientation.LANDSCAPE);
		
		screenShouldBe(Orientation.PORTRAIT); 
    }
	
	@Test
	public void rotatingWhileSidewaysShouldWork() {
		sut.registerNewDeviceOrientation(Orientation.LANDSCAPE);
		
		holdProximitySensor();
		sut.registerNewDeviceOrientation(new Orientation(2));
		
		screenShouldBe(Orientation.LANDSCAPE); 
	}
	
	@Test
    public void supportsUserReturningFromSide() {
		sut.registerNewDeviceOrientation(Orientation.LANDSCAPE);
		sut.registerNewDeviceOrientation(Orientation.PORTRAIT);
		
		screenShouldBe(Orientation.PORTRAIT); 
    }
	
	@Test
	public void supportsUserGoingOnSideWhileInLandscape() {
		holdProximitySensor();
		sut.registerNewDeviceOrientation(Orientation.LANDSCAPE);
		
		sut.registerNewDeviceOrientation(new Orientation(2));
		
		screenShouldBe(Orientation.LANDSCAPE);
	}
	
	@Test
	public void supportsUserReturningFromSideWhileInLandscape() {
		holdProximitySensor();
		sut.registerNewDeviceOrientation(Orientation.LANDSCAPE);
		sut.registerNewDeviceOrientation(Orientation.REVERSE_PORTRAIT);
		
		sut.registerNewDeviceOrientation(Orientation.LANDSCAPE);
		
		screenShouldBe(Orientation.LANDSCAPE);
	}
	
	private void screenShouldBe(Orientation orientation) {
		assertEquals(orientation, sut.screenOrientation());
	}

	@Before
	public void createSut() {
		sut = new OrientationCalculator(Orientation.PORTRAIT, Orientation.PORTRAIT, false);
	}
}
