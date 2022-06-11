package samuelpalmer.sensorautorotation.utilities.testing;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Utilities for working with constant fields
 */
@SuppressWarnings("unused")
public class FieldConstants<TClass> {

    private final Class<TClass> containingClass;
    private final String prefix;

    /**
     * @param containingClass The class containing the constant fields to inspect
     * @param prefix Filters out all constant fields whose names don't begin with this
     */
    public FieldConstants(Class<TClass> containingClass, String prefix) {
        this.containingClass = containingClass;
        this.prefix = prefix;
    }

    /**
     * Gets the fields name of the given constant value
     */
    @SuppressWarnings("unchecked")
    public <TConstant> String getName(TConstant value) {
        for (Field field : containingClass.getDeclaredFields())
            if (field.getName().startsWith(prefix) && Modifier.isStatic(field.getModifiers())) {
                TConstant currentFieldValue;
                try {
                    currentFieldValue = (TConstant) field.get(null);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

                if (currentFieldValue == null)
                    throw new IllegalStateException(String.format("Field %s is null, but all constant fields are expected to have a value", field.getName()));

                if (currentFieldValue.equals(value))
                    return field.getName();
            }

        throw new RuntimeException(String.format("Couldn't find constant in class %s with value %s", containingClass.getName(), value));
    }

}
