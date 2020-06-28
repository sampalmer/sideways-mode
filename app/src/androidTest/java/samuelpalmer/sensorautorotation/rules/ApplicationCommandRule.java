package samuelpalmer.sensorautorotation.rules;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.rules.ExternalResource;

import samuelpalmer.sensorautorotation.values.ApplicationCommand;

/**
 * Provides direct control of the app using {@link ApplicationCommand}
 */
public class ApplicationCommandRule extends ExternalResource {

    private Context context;

    @Override
    protected void before() throws Throwable {
        super.before();

        // Must use target context rather than our own context so that the commands are received by the app
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    @Override
    protected void after() {
        super.after();

        run(ApplicationCommand.ACTION_TURN_OFF);
        context = null;
    }

    /**
     * Runs the given {@link ApplicationCommand}
     * @param action One of the actions from {@link ApplicationCommand}
     */
    public void run(@ApplicationCommand.ACTION String action) {
        ApplicationCommand command = new ApplicationCommand(action);
        command.send(context);

        // TODO: Wait for command to take effect (using ApplicationStatusService)
    }

}
