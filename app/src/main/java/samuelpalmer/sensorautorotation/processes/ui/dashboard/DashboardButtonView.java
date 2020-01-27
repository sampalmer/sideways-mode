package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.widget.ImageViewCompat;

import samuelpalmer.sensorautorotation.R;

public class DashboardButtonView extends FrameLayout {

    private View container;
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

        container = inflate(getContext(), R.layout.dashboard_button, this);
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
        @ColorRes int colorControlNormal = getThemeColour(R.attr.colorControlNormal);
        @ColorRes int colorPrimary = getThemeColour(R.attr.colorPrimary);

        @ColorInt int backgroundColour = isSelected() ? ColorUtils.setAlphaComponent(ContextCompat.getColor(getContext(), colorPrimary), Math.round(0xFF * 0.12f)) : Color.TRANSPARENT;
        setBackgroundColor(backgroundColour);

        @ColorInt int iconColour = ContextCompat.getColor(getContext(), isSelected() ? colorPrimary : colorControlNormal);
        ImageViewCompat.setImageTintList(icon, ColorStateList.valueOf(iconColour));
    }

    @ColorRes
    private int getThemeColour(@AttrRes int attribute) {
        Resources.Theme theme = getContext().getTheme();

        TypedValue outValue = new TypedValue();
        if (!theme.resolveAttribute(attribute, outValue, true))
            throw new RuntimeException("Can't find theme color for " + attribute);

        @ColorRes int colorControlNormal = outValue.resourceId;
        if (colorControlNormal == 0)
            throw new RuntimeException("Theme color " + attribute + " is not a resource");

        return colorControlNormal;
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        syncState();
    }

}
