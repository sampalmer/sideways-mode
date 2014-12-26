package samuelpalmer.sensorautorotation;

import java.util.Iterator;
import java.util.LinkedList;

public class RecentWindows implements Iterable<AppPackage> {

	private final LinkedList<AppPackage> data = new LinkedList<>();

	public void add(AppPackage window) {
		if (data.contains(window))
			data.remove(window);

		data.addFirst(window);

		if (data.size() > 5)
			data.removeLast();
	}

	@Override
	public Iterator<AppPackage> iterator() {
		return data.iterator();
	}
}
