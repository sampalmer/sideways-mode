package samuelpalmer.sensorautorotation;

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
				&& ((AppPackage)o).qualifiedName.equals(qualifiedName);
	}

	@Override
	public int hashCode() {
		return qualifiedName.hashCode();
	}

}
