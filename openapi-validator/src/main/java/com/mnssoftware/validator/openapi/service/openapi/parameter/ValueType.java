package com.mnssoftware.validator.openapi.service.openapi.parameter;

import com.mnssoftware.utility.StringUtils;

import java.util.HashMap;
import java.util.Map;

public enum ValueType {
    PRIMITIVE,
    ARRAY,
    OBJECT;

    private static Map<String, ValueType> lookup = new HashMap<>();

    static {
        for (ValueType type : ValueType.values()) {
            lookup.put(type.name(), type);
        }
    }

    public static ValueType of(String typeStr) {
        ValueType type = lookup.get(StringUtils.trimToEmpty(typeStr).toUpperCase());

        return null == type ? PRIMITIVE : type;
    }

    public static boolean is(String typeStr, ValueType type) {
        return type == of(typeStr);
    }
}
