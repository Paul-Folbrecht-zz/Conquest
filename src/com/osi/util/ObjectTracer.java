package com.osi.util;


import com.osi.conquest.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;


/**
 * @author Paul Folbrecht
 */
public class ObjectTracer {
    private static List tracers = new ArrayList();

    /**
     *
     */
    static {
        tracers.add(new PrimitiveTracer());
        tracers.add(new ObjectArrayTracer());
        tracers.add(new CollectionTracer());
        tracers.add(new MapTracer());
        tracers.add(new CustomTracer());
    }

    /**
     *
     */
    public static void trace(String name, Object object) {
        StringBuffer buffer = new StringBuffer(1024);

        buffer.append(name);

        if (object == null) {
            buffer.append(": NULL");
        } else {
            buffer.append(" <" + object.getClass().getName() + ">: ");
            getTracer(object.getClass()).trace(object, buffer, 1);
        }

        Logger.info(buffer.toString());
    }

    /**
     *
     */
    private static Tracer getTracer(Class clazz) {
        for (int index = 0; index < tracers.size(); index++) {
            Tracer tracer = (Tracer) tracers.get(index);
            if (tracer.canHandle(clazz)) {
                return tracer;
            }
        }

        return null;
    }

    /**
     *
     */
    private static interface Tracer {
        public boolean canHandle(Class clazz);

        public void trace(Object object, StringBuffer buffer, int indent);
    }

    /**
     *
     */
    private static class PrimitiveTracer implements Tracer {
        public boolean canHandle(Class clazz) {
            return (clazz.isPrimitive() || clazz.getName().startsWith("java.lang."));
        }

        public void trace(Object object, StringBuffer buffer, int indent) {
            buffer.append(object.toString());
        }
    }

    /**
     *
     */
    private abstract static class ComplexObjectTracer implements Tracer {
        protected void indent(StringBuffer buffer, int indent) {
            for (int index = 0; index < indent; index++) {
                buffer.append("  ");
            }
        }

        protected void traceImpl(String name, Object object, StringBuffer buffer,
                                 int indent, boolean isLast) {
            indent(buffer, indent);
            buffer.append(name);

            if (object == null) {
                buffer.append(": NULL");
            } else {
                buffer.append(" <" + object.getClass().getName() + ">: ");
                getTracer(object.getClass()).trace(object, buffer, indent + 1);
            }

            if (!isLast) {
                buffer.append("\n");
            }
        }
    }

    /**
     *
     */
    private static class ObjectArrayTracer extends ComplexObjectTracer {
        public boolean canHandle(Class clazz) {
            return Object[].class.isAssignableFrom(clazz);
        }

        public void trace(Object object, StringBuffer buffer, int indent) {
            Object[] array = (Object[]) object;

            buffer.append("\n");

            for (int index = 0; index < array.length; index++) {
                traceImpl("Element " + index, array[index], buffer, indent, index == array.length - 1);
            }
        }
    }

    /**
     *
     */
    private static class CollectionTracer extends ComplexObjectTracer {
        public boolean canHandle(Class clazz) {
            return Collection.class.isAssignableFrom(clazz);
        }

        public void trace(Object object, StringBuffer buffer, int indent) {
            Iterator data = ((Collection) object).iterator();
            int index = 0;

            buffer.append("\n");

            while (data.hasNext()) {
                traceImpl("Element " + index++, data.next(), buffer, indent, !data.hasNext());
            }
        }
    }

    /**
     *
     */
    private static class MapTracer extends ComplexObjectTracer {
        public boolean canHandle(Class clazz) {
            return Map.class.isAssignableFrom(clazz);
        }

        public void trace(Object object, StringBuffer buffer, int indent) {
            Map map = (Map) object;
            Iterator keys = map.keySet().iterator();

            while (keys.hasNext()) {
                Object key = keys.next();

                traceImpl(key.toString(), map.get(key), buffer, indent, !keys.hasNext());
            }
        }
    }

    /**
     *
     */
    private static class CustomTracer extends ComplexObjectTracer {
        public boolean canHandle(Class clazz) {
            return true;
        }

        public void trace(Object object, StringBuffer buffer, int indent) {
            List fields = extractFields(object);

            buffer.append("\n");

            for (int index = 0; index < fields.size(); index++) {
                Field field = (Field) fields.get(index);

                field.setAccessible(true);
                try {
                    traceImpl(field.getName(), field.get(object), buffer, indent, index == fields.size() - 1);
                } catch (IllegalAccessException e) {
                    buffer.append(field.getName() + ": IllegalAccessException!\n");
                }
            }
        }

        /**
         * Helper method to extract all fields from the passed object and package them in a List of
         * FieldData objects; walks the class heirarchy to obtain superclass fields.
         */
        private List extractFields(Object object) {
            List list = new ArrayList();
            Class clazz = object.getClass();

            while (true) {
                Field[] fields = clazz.getDeclaredFields();

                for (int index = 0; index < fields.length; index++) {
                    Field field = fields[index];
                    int modifiers = field.getModifiers();

                    if (!Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers)) {
                        list.add(field);
                    }
                }

                clazz = clazz.getSuperclass();
                if (clazz == null || clazz.equals(Object.class)) {
                    return list;
                }
            }
        }
    }
}
