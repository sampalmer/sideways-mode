package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
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

        container = inflate(getContext(), R.layout.dashboard_button, null);
        icon = container.findViewById(R.id.dashboard_button_icon);
        icon.setImageResource(src);

        syncState();
        addView(container);
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
        setBackgroundColor(ContextCompat.getColor(getContext(), isSelected() ? R.color.primary_500_background_active : android.R.color.transparent));
        ImageViewCompat.setImageTintList(icon, ColorStateList.valueOf(ContextCompat.getColor(getContext(), isSelected() ? R.color.primary_500 : R.color.not_active)));
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        syncState();
    }

}
