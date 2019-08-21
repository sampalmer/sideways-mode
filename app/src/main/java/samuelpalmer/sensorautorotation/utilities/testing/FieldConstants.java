package samuelpalmer.sensorautorotation.utilities.testing;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@SuppressWarnings("unused")
public class FieldConstants<TClass> {

    private final Class<TClass> containingClass;
    private final String prefix;

    public FieldConstants(Class<TClass> containingClass, String prefix) {
        this.containingClass = containingClass;
        this.prefix = prefix;
    }

    public <TConstant> String getName(TConstant value) {
        for (Field field : containingClass.getDeclaredFields())
            if (field.getName().startsWith(prefix) && Modifier.isStatic(field.getModifiers())) {
                TConstant currentFieldValue;
                try {
                    //noinspection unchecked
                    currentFieldValue = (TConstant) field.get(null);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

                if (currentFieldValue == null)
                    throw new IllegalStateException(String.format("Field %s is null, but all constant fields are expected to have a value", field.getName()));

                if (currentFieldValue.equals(value))
                    return field.getName();
            }

        throw new RuntimeException("Couldn't find value");
    }

}
