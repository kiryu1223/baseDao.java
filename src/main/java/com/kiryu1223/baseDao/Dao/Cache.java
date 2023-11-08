package com.kiryu1223.baseDao.Dao;

import javax.persistence.*;
import javax.persistence.Entity;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Cache
{
    private static final Map<Class<?>, Map<String, String>> JavaFieldNameToDbFieldNameMappingMap = new ConcurrentHashMap<>();

    private static void addClassFieldMapping(Class<?> c)
    {
        Map<String, String> map = new HashMap<>();
        for (Field field : c.getDeclaredFields())
        {
            field.setAccessible(true);
            map.put(field.getName(), field.isAnnotationPresent(Column.class) ? field.getAnnotation(Column.class).name() : field.getName());
        }
        JavaFieldNameToDbFieldNameMappingMap.put(c, map);
    }

    public static Map<String, String> getJavaFieldNameToDbFieldNameMappingMap(Class<?> c)
    {
        if (!JavaFieldNameToDbFieldNameMappingMap.containsKey(c))
        {
            addClassFieldMapping(c);
        }
        return JavaFieldNameToDbFieldNameMappingMap.get(c);
    }

    private static final Map<Class<?>, String> ClassNameToTableNameMappingMap = new ConcurrentHashMap<>();

    private static void addClassNameToTableNameMapping(Class<?> c)
    {
        String table = c.isAnnotationPresent(Table.class) ?
                "`" + c.getAnnotation(Table.class).schema() + "`.`" + c.getAnnotation(Table.class).name() + "`"
                : "`" + c.getSimpleName() + "`";
        ClassNameToTableNameMappingMap.put(c, table);
    }

    public static String getTableName(Class<?> c)
    {
        if (!ClassNameToTableNameMappingMap.containsKey(c))
        {
            addClassNameToTableNameMapping(c);
        }
        return ClassNameToTableNameMappingMap.get(c);
    }

    private static final Map<Class<?>, Map<String, Field>> DbNameToFieldMappingMap = new ConcurrentHashMap<>();

    private static void addDbNameToFieldMapping(Class<?> c)
    {
        if (c.isAnnotationPresent(Entity.class))
        {
            Map<String, Field> map = new HashMap<>();
            for (Field field : c.getDeclaredFields())
            {
                field.setAccessible(true);
                map.put(field.isAnnotationPresent(Column.class) ? field.getAnnotation(Column.class).name() : field.getName(), field);
            }
            DbNameToFieldMappingMap.put(c, map);
        }
        else
        {
            throw new RuntimeException("is not Entity!!");
        }
    }

    public static Map<String, Field> getDbNameToFieldMapping(Class<?> c)
    {
        if (!DbNameToFieldMappingMap.containsKey(c))
        {
            addDbNameToFieldMapping(c);
        }
        return DbNameToFieldMappingMap.get(c);
    }

    private static final Map<Class<?>, Map<String, Field>> FieldNameToFieldMappingMap = new ConcurrentHashMap<>();

    private static void addFieldNameToFieldMapping(Class<?> c)
    {
        Map<String, Field> map = new HashMap<>();
        for (Field field : c.getDeclaredFields())
        {
            field.setAccessible(true);
            map.put(field.getName(), field);
        }
        FieldNameToFieldMappingMap.put(c, map);
    }

    public static Map<String, Field> getFieldNameToFieldMapping(Class<?> c)
    {
        if (!FieldNameToFieldMappingMap.containsKey(c))
        {
            addFieldNameToFieldMapping(c);
        }
        return FieldNameToFieldMappingMap.get(c);
    }

    private static final Map<Class<?>, Map<String, Method>> MethodNameToMethodMappingMap = new ConcurrentHashMap<>();

    private static void addMethodNameToMethodMapping(Class<?> c)
    {
        Map<String, Method> methodMap = new HashMap<>();
        for (Method method : c.getDeclaredMethods())
        {
            method.setAccessible(true);
            if (method.getParameterCount() == 1)
            {
                methodMap.put(method.getName(), method);
            }
        }
        MethodNameToMethodMappingMap.put(c, methodMap);
    }

    public static Map<String, Method> getMethodNameToMethodMapping(Class<?> c)
    {
        if (!MethodNameToMethodMappingMap.containsKey(c))
        {
            addMethodNameToMethodMapping(c);
        }
        return MethodNameToMethodMappingMap.get(c);
    }

    private static final Map<Class<?>, List<Field>> TypeFieldMap = new ConcurrentHashMap<>();

    public static List<Field> getTypeFields(Class<?> c)
    {
        if (!TypeFieldMap.containsKey(c))
        {
            ArrayList<Field> fields = new ArrayList<Field>();
            for (Field a : c.getDeclaredFields())
            {
                a.setAccessible(true);
                fields.add(a);
            }
            TypeFieldMap.put(c, fields);
        }
        return TypeFieldMap.get(c);
    }

    public static <T> void setCLassDefVal(T t)
    {
        getTypeFields(t.getClass()).forEach(a ->
        {
            try
            {
                if (a.getType() == Integer.class)
                {
                    a.set(t, 0);
                }
                else if (a.getType() == Byte.class)
                {
                    a.set(t, (byte) 0);
                }
                else if (a.getType() == Short.class)
                {
                    a.set(t, (short) 0);
                }
                else if (a.getType() == Long.class)
                {
                    a.set(t, 0L);
                }
                else if (a.getType() == String.class)
                {
                    a.set(t, " ");
                }
                else if (a.getType() == Character.class)
                {
                    a.set(t, ' ');
                }
                else if (a.getType() == Float.class)
                {
                    a.set(t, (float) 0);
                }
                else if (a.getType() == Double.class)
                {
                    a.set(t, (double) 0);
                }
                else if (a.getType() == BigInteger.class)
                {
                    a.set(t, BigDecimal.valueOf(0));
                }
                else
                {
                    a.set(t, a.getType().getConstructor().newInstance());
                }
            }
            catch (IllegalAccessException | InvocationTargetException | InstantiationException |
                   NoSuchMethodException e)
            {
                throw new RuntimeException(e);
            }
        });
    }
}
