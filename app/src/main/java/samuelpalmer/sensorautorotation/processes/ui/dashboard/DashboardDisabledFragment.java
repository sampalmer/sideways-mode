package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.UserSide;

public class DashboardDisabledFragment extends Fragment {
	
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_dashboard_disabled, container, false);
		configureUserSideButton(view, R.id.choose_side_left_caption, UserSide.LEFT, R.drawable.app_left_side);
		configureUserSideButton(view, R.id.choose_side_right_caption, UserSide.RIGHT, R.drawable.app_right_side);
		
		return view;
	}
	
	private void configureUserSideButton(View layout, int buttonId, final UserSide side, int iconId) {
		TextView button = layout.findViewById(buttonId);
		
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				startApp(side);
			}
		});
		
		/*
			All image drawables must be set in app code to avoid crashes since:
			1. We're using the vector compat stuff
			2. When this view is inflated by the quick setting service, any images defined in the XML aren't loaded
		 */
		Drawable icon = ResourcesCompat.getDrawable(getResources(), iconId, null);
		
		//noinspection ConstantConditions
		icon.setColorFilter(ResourcesCompat.getColor(getResources(), R.color.primary_500, null), PorterDuff.Mode.MULTIPLY);
		
		button.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
	}
	
	private void startApp(UserSide userSide) {
		userSide.command.send(getContext());
		getActivity().finish();
	}
	
}
