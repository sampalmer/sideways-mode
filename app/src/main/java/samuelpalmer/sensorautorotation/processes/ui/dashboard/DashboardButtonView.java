package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.widget.ImageViewCompat;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.utilities.ThemeUtilities;

/**
 * {@link View} for each state icon button on the main dashboard
 */
public class DashboardButtonView extends FrameLayout {

    private AppCompatImageView icon;

    public DashboardButtonView(Context context) {
        super(context);
        init(null);
    }

    public DashboardButtonView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public DashboardButtonView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public DashboardButtonView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(@Nullable AttributeSet attrs) {
        int src = getSource(attrs);

        View container = inflate(getContext(), R.layout.dashboard_button, this);
        icon = container.findViewById(R.id.dashboard_button_icon);
        icon.setImageResource(src);

        syncState();
    }

    @DrawableRes
    private int getSource(@Nullable AttributeSet attrs) {
        if (attrs == null)
            throw new RuntimeException("No attributes supplied");

        TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.DashboardButtonView, 0, 0);
        @DrawableRes int src;
        try {
            src = a.getResourceId(R.styleable.DashboardButtonView_src, -1);
        } finally {
            a.recycle();
            a = null;
        }

        if (src == -1)
            throw new RuntimeException("Source image not specified");

        return src;
    }

    private void syncState() {
        // Get colours
        @ColorInt int colorControlNormal = ThemeUtilities.getThemeColour(getContext(), R.attr.colorControlNormal);
        @ColorInt int colorPrimary = ThemeUtilities.getThemeColour(getContext(), R.attr.colorPrimary);
        @ColorInt int colorOnPrimary = ThemeUtilities.getThemeColour(getContext(), R.attr.colorOnPrimary);

        // Using the primary colour to make the selection stand out
        @ColorInt int backgroundColour = isSelected() ? colorPrimary : Color.TRANSPARENT;
        setBackgroundColor(backgroundColour);

        @ColorInt int iconColour = isSelected() ? colorOnPrimary : colorControlNormal;
        ImageViewCompat.setImageTintList(icon, ColorStateList.valueOf(iconColour));
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        syncState();
    }

}
