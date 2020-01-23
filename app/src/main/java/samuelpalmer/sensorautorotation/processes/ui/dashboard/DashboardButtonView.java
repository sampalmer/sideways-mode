package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;

import samuelpalmer.sensorautorotation.R;

public class DashboardButtonView extends LinearLayout {

    private AppCompatImageView imageView;
    private int preferredWidth;
    private int preferredHeight;

    public DashboardButtonView(Context context) {
        super(context);
        //noinspection ConstantConditions
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

        initSelf();
        initImage(src);
        syncState();
    }

    private void initSelf() {
        // Set size
        preferredWidth = dpToPx(64);

        TypedValue listPreferredItemHeight = new TypedValue();
        Resources.Theme theme = getContext().getTheme();
        boolean foundAttribute = theme.resolveAttribute(android.R.attr.listPreferredItemHeight, listPreferredItemHeight, true);
        if (!foundAttribute)
            throw new RuntimeException("Didn't find listPreferredItemHeight theme attribute");
        preferredHeight = (int) listPreferredItemHeight.getDimension(getResources().getDisplayMetrics());

        // Other
        setFocusable(true);
        setGravity(Gravity.CENTER_HORIZONTAL);
    }

    private void initImage(int src) {
        imageView = new AppCompatImageView(getContext());

        // Set size
        int width = dpToPx(48);
        int height = ViewGroup.LayoutParams.MATCH_PARENT;
        LayoutParams params = new LayoutParams(width, height);
        params.gravity = Gravity.CENTER;
        params.setMargins(dpToPx(8), dpToPx(6), dpToPx(8), dpToPx(6));
        imageView.setLayoutParams(params);

        imageView.setScaleType(ImageView.ScaleType.CENTER);
        imageView.setDuplicateParentStateEnabled(true);

        imageView.setImageResource(src);

        addView(imageView);
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private void syncState() {
        setBackgroundColor(ContextCompat.getColor(getContext(), isSelected() ? R.color.primary_500_background_active : android.R.color.transparent));
        ImageViewCompat.setImageTintList(imageView, ColorStateList.valueOf(ContextCompat.getColor(getContext(), isSelected() ? R.color.primary_500 : R.color.not_active)));
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        syncState();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int measuredWidth = getMeasuredDimension(widthMeasureSpec, preferredWidth);
        int measuredHeight = getMeasuredDimension(heightMeasureSpec, preferredHeight);

        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    private int getMeasuredDimension(int measureSpec, int preferredSize) {
        int size;
        int mode = MeasureSpec.getMode(measureSpec);
        switch (mode) {
            case MeasureSpec.UNSPECIFIED:
                size = preferredSize;
                break;
            case MeasureSpec.EXACTLY:
                size = MeasureSpec.getSize(measureSpec);
                break;
            case MeasureSpec.AT_MOST:
                size = Math.min(preferredSize, MeasureSpec.getSize(measureSpec));
                break;
            default:
                throw new RuntimeException("Got unknown measure spec mode: " + mode);
        }

        boolean isTooSmall = size < preferredSize;
        return (size & MEASURED_SIZE_MASK) | (MEASURED_STATE_MASK & (isTooSmall ? MEASURED_STATE_TOO_SMALL : 0));
    }

}
