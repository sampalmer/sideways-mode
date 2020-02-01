package com.example.notificationlayoutdump;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.View;
import android.view.ViewGroup;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

public class ViewLayoutDumper {

    private static String TAG = ViewLayoutDumper.class.getSimpleName();

    private ViewLayoutDumper() {}

    public static String viewToXml(Context context, View view) {
        XmlSerializer serializer = Xml.newSerializer();
        serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
        StringWriter writer = new StringWriter();

        try {
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", true);
            viewToXml(context, view, serializer);
            serializer.endDocument();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        return writer.toString();
    }

    private static void viewToXml(Context context, View view, XmlSerializer serializer) throws IOException, InvocationTargetException, IllegalAccessException {
        String className = view.getClass().getName();
        serializer.startTag(null, className);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams != null) {
            writeAttributes(context, layoutParams, serializer, "layout_");
        }

        writeAttributes(context, view, serializer, "");

        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); ++i) {
                View child = viewGroup.getChildAt(i);
                viewToXml(context, child, serializer);
            }
        }

        serializer.endTag(null, className);
    }

    private static void writeAttributes(Context context, Object object, XmlSerializer serializer, String prefix) throws IllegalAccessException, InvocationTargetException, IOException {
        Object defaultObject = null;
        if (object.getClass().getConstructors().length > 0)
            try {
                if (object instanceof View) {
                    try {
                        Constructor<?> constructor = object.getClass().getConstructor(Context.class);
                        defaultObject = constructor.newInstance(context);
                    } catch (NoSuchMethodException e) {
                        Constructor<?> constructor = object.getClass().getConstructor(Context.class, AttributeSet.class);
                        defaultObject = constructor.newInstance(context, null);
                    }
                } else if (object instanceof ViewGroup.LayoutParams) {
                    Constructor<?> constructor = object.getClass().getConstructor(int.class, int.class);
                    defaultObject = constructor.newInstance(Integer.MIN_VALUE, Integer.MIN_VALUE);
                } else {
                    throw new RuntimeException("Don't know how to make instance of " + object.getClass().getName());
                }
            } catch (NoSuchMethodException e) {
                ArrayList<String> constructorStrings = new ArrayList<>();
                Constructor<?>[] constructors = object.getClass().getConstructors();
                for (Constructor constructor : constructors) {
                    constructorStrings.add(constructor.toString());
                }

                throw new RuntimeException("Can't find suitable constructor for class " + object.getClass() + ". Available constructors: " + TextUtils.join("\n", constructorStrings.toArray()));
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            }

        Field[] fields = object.getClass().getFields();
        for (Field field : fields) {
            if (!Modifier.isStatic(field.getModifiers())) {
                Class<?> type = field.getType();
                if (isSimpleType(type)) {
                    Object fieldValue = field.get(object);

                    if (defaultObject != null && !areEqual(fieldValue, field.get(defaultObject))) {
                        String stringFieldValue = valueToXmlValue(fieldValue);
                        serializer.attribute(null, prefix + field.getName(), stringFieldValue);
                    }
                }
            }
        }

        Method[] methods = object.getClass().getMethods();
        for (Method method : methods) {
            String name = method.getName();
            boolean isGetter = name.startsWith("get");
            if (isGetter) {
                boolean hasNoParams = method.getParameterTypes().length == 0;
                if (hasNoParams) {
                    Class<?> returnType = method.getReturnType();
                    if (isSimpleType(returnType)) {
                        String fieldNamePascalCase = name.substring(3);
                        String fieldNameCamelCase = fieldNamePascalCase.substring(0, 1).toLowerCase() + fieldNamePascalCase.substring(1);
                        Object result = method.invoke(object);

                        if (defaultObject != null && !areEqual(result, method.invoke(defaultObject))) {
                            String stringResult = valueToXmlValue(result);
                            serializer.attribute(null, prefix + fieldNameCamelCase, stringResult);
                        }
                    }
                }
            }
        }
    }

    private static boolean areEqual(Object a, Object b) {
        if (a == null || b == null)
            return a == b;

        return a.equals(b);
    }

    private static boolean isSimpleType(Class<?> type) {
        return type.isPrimitive() || type == Integer.class || type == Long.class || type == Float.class || type == Double.class || type == Boolean.class || type == Byte.class || type == Character.class || type == Short.class || type == String.class || type == CharSequence.class;
    }

    private static String valueToXmlValue(Object result) {
        return result == null ? "@null" : result.toString();
    }

}
