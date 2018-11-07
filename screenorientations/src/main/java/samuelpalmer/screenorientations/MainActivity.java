package samuelpalmer.screenorientations;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

public class MainActivity extends Activity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    
        final ScreenOrientation[] orientations = lookupOrientations();

        Spinner list = findViewById(R.id.list);
        list.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, orientations));
        list.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                setRequestedOrientation(orientations[i].value);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private static ScreenOrientation[] lookupOrientations() {
        ArrayList<ScreenOrientation> results = new ArrayList<>();

        for (Field field : ActivityInfo.class.getDeclaredFields())
            if (field.getName().startsWith("SCREEN_ORIENTATION_") && Modifier.isStatic(field.getModifiers())) {
                int value;
                try {
                    //noinspection unchecked
                    value = (int) field.get(null);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

                ScreenOrientation result = new ScreenOrientation();
                result.name = field.getName();
                result.value = value;
                results.add(result);
            }

        return results.toArray(new ScreenOrientation[0]);
    }

    static class ScreenOrientation {

        int value;
        String name;

        @SuppressWarnings("NullableProblems")
        @Override
        public String toString() {
            return name;
        }
        
    }

}
