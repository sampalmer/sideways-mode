package samuelpalmer.sensorautorotation.utilities;

public class NumberUtilities {
    
    public static int wrap(int value, int length) {
        if (length <= 0)
            throw new IllegalArgumentException();
        
        while (value < 0)
            value += length;
        
        return value % length;
    }
    
}
