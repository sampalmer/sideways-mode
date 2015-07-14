package samuelpalmer.sensorautorotation;

public class Reflection {
	public static boolean classExists(String fullyQualifiedName) {
		try {
			Class.forName(fullyQualifiedName);
		} catch (ClassNotFoundException e) {
			return false;
		}

		return true;
	}
}
