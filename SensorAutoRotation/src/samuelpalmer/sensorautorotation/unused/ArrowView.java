package samuelpalmer.sensorautorotation.unused;

import samuelpalmer.sensorautorotation.IntegerUtilities;
import samuelpalmer.sensorautorotation.Orientation;
import samuelpalmer.sensorautorotation.Rotation;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.view.WindowManager;

public class ArrowView extends View {
	private Paint background;
	private Paint paint;
	private Rotation rotation;

    public ArrowView(Context context, Orientation direction) {
        super(context);
        
		WindowManager windows = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
    	Orientation screenOrientation = Orientation.fromAndroidSurfaceRotation(windows.getDefaultDisplay().getRotation());
		rotation = direction.minus(screenOrientation);
    	
        background = new Paint();
        background.setColor(Color.argb(128, 255, 255, 255));
        
        paint = new Paint();
        paint.setColor(Color.BLUE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int centreX = getWidth() / 2;
        int centreY = getHeight() / 2;
        
        canvas.drawRect(0, 0, getWidth(), getHeight(), background);
        
        //Right-facing arrow
        float[] points = new float[]
		{
        	0, centreY, getWidth() - 1, centreY,
    		getWidth() - 1, centreY, centreX, 0,
    		getWidth() - 1, centreY, centreX, getHeight() - 1
		};
        
        boolean isLeft = IntegerUtilities.wrap(rotation.clockwiseCount(), 4) != 1;
		if (isLeft)
	        for (int i = 0; i < points.length; ++i) {
				boolean isX = i % 2 == 0;
				if (isX)
	        		points[i] = getWidth() - 1 - points[i];
			}
        
        canvas.drawLines(points, paint);
    }
}
