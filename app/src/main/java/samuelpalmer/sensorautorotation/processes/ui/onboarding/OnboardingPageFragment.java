package samuelpalmer.sensorautorotation.processes.ui.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import samuelpalmer.sensorautorotation.R;

public class OnboardingPageFragment extends Fragment {

    private static final String ARG_IMAGE = "image";
    private static final String ARG_HEADLINE = "headline";
    private static final String ARG_SUBHEADING = "subheading";

    public static OnboardingPageFragment newInstance(@DrawableRes int image, @Nullable CharSequence headline, CharSequence subheading) {
        OnboardingPageFragment fragment = new OnboardingPageFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_IMAGE, image);
        args.putCharSequence(ARG_HEADLINE, headline);
        args.putCharSequence(ARG_SUBHEADING, subheading);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_onboarding_page, container, false);

        Bundle args = getArguments();
        assert args != null;

        ImageView image = view.findViewById(R.id.onboard_image);
        TextView headline = view.findViewById(R.id.onboard_headline);
        TextView subheading = view.findViewById(R.id.onboard_subheading);

        image.setImageResource(args.getInt(ARG_IMAGE));

        CharSequence headlineText = args.getCharSequence(ARG_HEADLINE);
        if (headlineText != null)
            headline.setText(headlineText);
        else
            headline.setVisibility(View.GONE);

        subheading.setText(args.getCharSequence(ARG_SUBHEADING));
        subheading.setMaxLines(headlineText == null ? 3 : 2);

        return view;
    }
}