package samuelpalmer.sensorautorotation.orientationenforcement;

public class AppPackage {

	private final CharSequence qualifiedName;

	public AppPackage(CharSequence qualifiedName) {
		this.qualifiedName = qualifiedName;
	}

	public CharSequence getQualifiedName() {
		return qualifiedName;
	}

	@Override
	public boolean equals(Object o) {
		return o != null
				&& o.getClass() == getClass()
				&& ((AppPackage)o).toString().equals(toString());
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public String toString() {
		return qualifiedName.toString();
	}
}
