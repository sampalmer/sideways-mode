package samuelpalmer.sensorautorotation;

import android.util.SparseIntArray;

public class BidirectionalIntMap {
	private final SparseIntArray keysToValues;
	private final SparseIntArray valuesToKeys;
	
	public BidirectionalIntMap() {
		this.keysToValues = new SparseIntArray();
		this.valuesToKeys = new SparseIntArray();
	}
	
	public void add(int key, int value) {
		keysToValues.append(key, value);
		valuesToKeys.append(value, key);
	}
	
	public int getKey(int value) {
		return this.valuesToKeys.get(value);
	}
	
	public int getValue(int key) {
		return this.keysToValues.get(key);
	}
}
