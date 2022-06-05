package samuelpalmer.sensorautorotation.processes.service;

import android.content.Context;

import samuelpalmer.sensorautorotation.utilities.State;

public class SideSwapHandler extends State {

    private final OrientationCalculator calculator;

    public SideSwapHandler(Context context, OrientationCalculator calculator) {
        this.calculator = calculator;

        // To get initial user side: calculator.side()
        // To change user side: calculator.setSide()
    }

    @Override
    protected void start() {
        // Register sensor(s) etc here
    }

    @Override
    protected void stop() {
        // Unregister sensor(s) etc here
    }

}
