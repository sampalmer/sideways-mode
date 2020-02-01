package com.example.notificationlayoutdump;

import android.util.Xml;
import android.view.View;
import android.view.ViewGroup;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ViewLayoutDumper {

    private ViewLayoutDumper() {}

    public static String viewToXml(View view) {
        XmlSerializer serializer = Xml.newSerializer();
        serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
        StringWriter writer = new StringWriter();

        try {
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", true);
            viewToXml(view, serializer);
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

    private static void viewToXml(View view, XmlSerializer serializer) throws IOException, InvocationTargetException, IllegalAccessException {
        String className = view.getClass().getName();
        serializer.startTag(null, className);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams != null) {
            writeAttributes(layoutParams, serializer, "layout_");
        }

        writeAttributes(view, serializer, "");

        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); ++i) {
                View child = viewGroup.getChildAt(i);
                viewToXml(child, serializer);
            }
        }

        serializer.endTag(null, className);
    }

    private static void writeAttributes(Object object, XmlSerializer serializer, String prefix) throws IllegalAccessException, InvocationTargetException, IOException {
        Method[] methods = object.getClass().getMethods();
        for (Method method : methods) {
            String name = method.getName();
            boolean isGetter = name.startsWith("get");
            if (isGetter) {
                boolean hasNoParams = method.getParameterTypes().length == 0;
                if (hasNoParams) {
                    Class<?> returnType = method.getReturnType();
                    if (returnType.isPrimitive() || returnType == Integer.class || returnType == Long.class || returnType == Float.class || returnType == Double.class || returnType == Boolean.class || returnType == Byte.class || returnType == Character.class || returnType == Short.class || returnType == String.class || returnType == CharSequence.class) {
                        String fieldNamePascalCase = name.substring(3);
                        String fieldNameCamelCase = fieldNamePascalCase.substring(0, 1).toLowerCase() + fieldNamePascalCase.substring(1);
                        Object result = method.invoke(object);
                        String stringResult = result == null ? "@null" : result.toString();
                        serializer.attribute(null, prefix + fieldNameCamelCase, stringResult);
                    }
                }
            }
        }
    }

}
