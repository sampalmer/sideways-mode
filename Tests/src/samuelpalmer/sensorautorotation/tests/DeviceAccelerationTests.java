package samuelpalmer.sensorautorotation.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import samuelpalmer.sensorautorotation.DeviceAcceleration;
import samuelpalmer.sensorautorotation.Orientation;

public class DeviceAccelerationTests {

	private static final DeviceAcceleration stationary = new DeviceAcceleration(0, 0);
	private static final int SUFFICIENT_MAGNITUDE = 100;

	@Test
	public void hidesResultForSmallAcceleration() {
		assertFalse(stationary.hasOrientation());
	}
	
	@Test
	public void hidesResultForAmbiguousDirection() {
		DeviceAcceleration ambiguous = new DeviceAcceleration(SUFFICIENT_MAGNITUDE, -SUFFICIENT_MAGNITUDE);
		assertFalse(ambiguous.hasOrientation());
	}
	
	@Test
	public void treatsPositiveYAsPortrait() {
		DeviceAcceleration upwardMotion = new DeviceAcceleration(0, SUFFICIENT_MAGNITUDE);
		assertEquals(Orientation.PORTRAIT, upwardMotion.orientation());
	}
	
	@Test
	public void treatsNegativeYAsDown() {
		DeviceAcceleration downwardMotion = new DeviceAcceleration(0, -SUFFICIENT_MAGNITUDE);
		assertEquals(Orientation.REVERSE_PORTRAIT, downwardMotion.orientation());
	}
	
	@Test
	public void treatsPositiveXAs() {
		DeviceAcceleration rightwardMotion = new DeviceAcceleration(SUFFICIENT_MAGNITUDE, 0);
		assertEquals(Orientation.REVERSE_LANDSCAPE, rightwardMotion.orientation());
	}
	
	@Test
	public void treatsNegativeXAs() {
		DeviceAcceleration leftwardMotion = new DeviceAcceleration(-SUFFICIENT_MAGNITUDE, 0);
		assertEquals(Orientation.LANDSCAPE, leftwardMotion.orientation());
	}
	
	@Test(expected=Exception.class)
	public void preventsAccessToUnknownOrientation() {
		stationary.orientation();
	}
}
