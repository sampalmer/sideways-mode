package samuelpalmer.sensorautorotation.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import samuelpalmer.sensorautorotation.Orientation;
import samuelpalmer.sensorautorotation.OrientationCalculator;

public class OrientationCoordinatorTests {
	private OrientationCalculator sut;
	
	@Test
	public void rotatingIntoLandscapeWithSensorCoveredShouldCauseScreenRotation() {
		sut = new OrientationCalculator(Orientation.PORTRAIT, Orientation.PORTRAIT, true);
		
		sut.registerNewDeviceOrientation(Orientation.LANDSCAPE);
		
		screenShouldBe(Orientation.LANDSCAPE);
	}

	@Test
    public void rotatingWithoutSensorCoveredShouldntCauseScreenRotation() {
		sut = new OrientationCalculator(Orientation.PORTRAIT, Orientation.PORTRAIT, false);
		
		sut.registerNewDeviceOrientation(Orientation.LANDSCAPE);
		
		screenShouldBe(Orientation.PORTRAIT); 
    }
	
	@Test
	public void rotatingWhileSidewaysShouldWork() {
		sut = new OrientationCalculator(Orientation.PORTRAIT, Orientation.LANDSCAPE, true);
		
		sut.registerNewDeviceOrientation(Orientation.REVERSE_PORTRAIT);
		
		screenShouldBe(Orientation.LANDSCAPE); 
	}
	
	@Test
    public void supportsUserReturningFromSide() {
		sut = new OrientationCalculator(Orientation.PORTRAIT, Orientation.LANDSCAPE, false);
		
		sut.registerNewDeviceOrientation(Orientation.PORTRAIT);
		
		screenShouldBe(Orientation.PORTRAIT); 
    }
	
	@Test
	public void supportsUserGoingOnSideWhileInLandscape() {
		sut = new OrientationCalculator(Orientation.LANDSCAPE, Orientation.LANDSCAPE, true);
		
		sut.registerNewDeviceOrientation(Orientation.REVERSE_PORTRAIT);
		
		screenShouldBe(Orientation.LANDSCAPE);
	}
	
	@Test
	public void supportsUserReturningFromSideWhileInLandscape() {
		sut = new OrientationCalculator(Orientation.LANDSCAPE, Orientation.REVERSE_PORTRAIT, true);
		
		sut.registerNewDeviceOrientation(Orientation.LANDSCAPE);
		
		screenShouldBe(Orientation.LANDSCAPE);
	}
	
	@Test
	public void doesntSupportReversePortrait() {
		sut = new OrientationCalculator(Orientation.LANDSCAPE, Orientation.LANDSCAPE, false);
		
		sut.registerNewDeviceOrientation(Orientation.REVERSE_PORTRAIT);
		
		screenShouldBe(Orientation.LANDSCAPE);
	}
	
	@Test
	public void preservesOrientationWhenUserRecoversFromReversePortraitAttempt() {
		sut = new OrientationCalculator(Orientation.LANDSCAPE, Orientation.LANDSCAPE, false);
		
		sut.registerNewDeviceOrientation(Orientation.REVERSE_PORTRAIT);
		sut.registerNewDeviceOrientation(Orientation.LANDSCAPE);
		
		screenShouldBe(Orientation.LANDSCAPE);
	}
	
	@Test
	public void supportsOrientationChangesAfterRecoveryFromReversePortraitAttempt() {
		sut = new OrientationCalculator(Orientation.LANDSCAPE, Orientation.LANDSCAPE, false);
		sut.registerNewDeviceOrientation(Orientation.REVERSE_PORTRAIT);
		sut.registerNewDeviceOrientation(Orientation.LANDSCAPE);
		
		sut.registerNewDeviceOrientation(Orientation.PORTRAIT);
		
		screenShouldBe(Orientation.PORTRAIT);
	}
	
	private void screenShouldBe(Orientation orientation) {
		assertEquals(orientation, sut.screenOrientation());
	}
}
