package samuelpalmer.sensorautorotation.settings.application.multiprocess;

import java.util.HashMap;

import samuelpalmer.sensorautorotation.settings.application.multiprocess.contracts.ApplyContract;
import samuelpalmer.sensorautorotation.settings.application.multiprocess.contracts.CommitContract;
import samuelpalmer.sensorautorotation.settings.application.multiprocess.contracts.ContainsContract;
import samuelpalmer.sensorautorotation.settings.application.multiprocess.contracts.GetAllContract;
import samuelpalmer.sensorautorotation.settings.application.multiprocess.contracts.GetBooleanContract;
import samuelpalmer.sensorautorotation.settings.application.multiprocess.contracts.GetFloatContract;
import samuelpalmer.sensorautorotation.settings.application.multiprocess.contracts.GetIntContract;
import samuelpalmer.sensorautorotation.settings.application.multiprocess.contracts.GetLongContract;
import samuelpalmer.sensorautorotation.settings.application.multiprocess.contracts.GetStringContract;
import samuelpalmer.sensorautorotation.settings.application.multiprocess.contracts.GetStringSetContract;
import samuelpalmer.sensorautorotation.settings.application.multiprocess.contracts.RegisterOnSharedPreferenceChangeListenerContract;
import samuelpalmer.sensorautorotation.settings.application.multiprocess.contracts.UnregisterOnSharedPreferenceChangeListenerContract;

class ContractMap {

	public static Contract lookup(String method) {
		Contract result = lookup.get(method);
		if (result == null)
			throw new RuntimeException("Unknown method: " + method);

		return result;
	}

	private static final HashMap<String, Contract> lookup;
	static {
		Contract[] contracts = new Contract[] {
			new ApplyContract(),
			new CommitContract(),
			new ContainsContract(),
			new GetAllContract(),
			new GetBooleanContract(),
			new GetFloatContract(),
			new GetIntContract(),
			new GetLongContract(),
			new GetStringContract(),
			new GetStringSetContract(),
			new RegisterOnSharedPreferenceChangeListenerContract(),
			new UnregisterOnSharedPreferenceChangeListenerContract()
		};

		lookup = new HashMap<>(contracts.length);

		for (Contract contract : contracts)
			lookup.put(contract.methodName(), contract);
	}
}
