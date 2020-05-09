package samuelpalmer.sensorautorotation.processes.ui.preferences.incompatibleapps;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.utilities.TaskRunner;

public class CheckedInstalledApp extends LinearLayout {
    
    private PackageManager packageManager;
    
    private ImageView icon;
    private ProgressBar iconProgress;
    private TextView label;
    private TextView packageName;
    private CheckBox checkbox;

    private InstalledApp currentItem;
    private Future<?> currentLoader;
    
    public CheckedInstalledApp(Context context) {
        super(context);
        init(context);
    }
    
    public CheckedInstalledApp(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    public CheckedInstalledApp(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }
    
    @SuppressWarnings("unused")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CheckedInstalledApp(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }
    
    private void init(Context context) {
        packageManager = context.getPackageManager();
        
        LayoutInflater.from(context).inflate(R.layout.incompatible_apps_chooser_item, this);
        setOrientation(HORIZONTAL);
        setPadding(getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin), 0, getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin), 0);
        setMinimumHeight(getResources().getDimensionPixelSize(R.dimen.list_item_min_height));
        
        icon = findViewById(R.id.incompatible_apps_item_icon);
        iconProgress = findViewById(R.id.incompatible_item_icon_progress);
        label = findViewById(R.id.incompatible_apps_item_label);
        packageName = findViewById(R.id.incompatible_apps_item_package);
        checkbox = findViewById(R.id.incompatible_apps_item_checkbox);
    }
    
    public void setItem(InstalledApp item) {
        if (item != currentItem) {
            if (currentLoader != null)
                currentLoader.cancel(true);

            iconProgress.setVisibility(View.VISIBLE);
            icon.setVisibility(View.GONE);

            ImageLoader task = new ImageLoader(item, new WeakReference<>(this));
            currentLoader = ImageLoader.taskRunner.executeAsync(task, imageLoaded);
            
            label.setText(item.label);
            packageName.setText(item.packageName);
            packageName.setVisibility(label.getText().equals(packageName.getText()) ? GONE : VISIBLE);
        }
        
        // Even if we're changing to the same item, it's possible that the "isIncompatible" flag has since been changed.
        checkbox.setChecked(item.isIncompatible);
        currentItem = item;
    }

    private final TaskRunner.Callback<ImageLoader, Drawable> imageLoaded = (task, image) -> {
        if (image == null)
            return; // Task was cancelled

        CheckedInstalledApp listItem = task.listItemReference.get();
        if (listItem != null) {
            // It's possible that this item has since been repurposed, which would make the loaded icon irrelevant
            if (task.installedApp == listItem.currentItem) {
                listItem.iconProgress.setVisibility(View.GONE);
                listItem.icon.setImageDrawable(image);
                listItem.icon.setVisibility(View.VISIBLE);
            }
        }
    };

    /**
     * Loads icons in the background to avoid lag when scrolling through the list
     */
    private static class ImageLoader implements Callable<Drawable> {

        // Not using THREAD_POOL_EXECUTOR since it crashes if we load a lot of tasks in parallel
        private static final TaskRunner taskRunner = new TaskRunner(Executors.newSingleThreadExecutor());

        private final InstalledApp installedApp;
        
        // Android Studio says this class could cause a memory leak by indirectly holding onto the context in a background thread.
        // So using a weak reference to get around that.
        private final WeakReference<CheckedInstalledApp> listItemReference;
        
        private ImageLoader(InstalledApp installedApp, WeakReference<CheckedInstalledApp> listItemReference) {
            this.installedApp = installedApp;
            this.listItemReference = listItemReference;
        }
        
        @Override
        public Drawable call() {
            if (Thread.currentThread().isInterrupted())
                return null;
            
            CheckedInstalledApp listItem = listItemReference.get();
            if (listItem != null)
                return installedApp.loadIcon(listItem.packageManager);
            else
                return null;
        }
        
    }

}
