package com.mnssoftware.validator.openapi.service.openapi.parameter;

import com.mnssoftware.oas.model.Parameter;
import com.mnssoftware.openapi.OpenApiHandler;
import com.mnssoftware.utility.StringUtils;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.AttachmentKey;
import io.undertow.util.HttpString;

import java.util.*;

public class HeaderParameterDeserializer implements ParameterDeserializer {
    private static final String SIMPLE = "simple";

    @Override
    public AttachmentKey<Map<String, Object>> getAttachmentKey() {
        return OpenApiHandler.DESERIALIZED_HEADER_PARAMETERS;
    }

    @Override
    public StyleParameterDeserializer getStyleDeserializer(String style) {
        if (StringUtils.isNotBlank(style) && !SIMPLE.equalsIgnoreCase(style)) {
            return null;
        }

        return new StyleParameterDeserializer() {

            @Override
            public Object deserialize(HttpServerExchange exchange, Parameter parameter,
                                      ValueType valueType,
                                      boolean exploade) {
                Collection<String> values = exchange.getRequestHeaders().get(new HttpString(parameter.getName()));

                if (ValueType.ARRAY == valueType) {
                    List<String> valueList = new ArrayList<>();

                    values.forEach(v -> valueList.addAll(asList(v, Delimiters.COMMA)));

                    return valueList;
                } else if (ValueType.OBJECT == valueType) {
                    Map<String, String> valueMap = new HashMap<>();
                    values.forEach(v -> valueMap.putAll(exploade ? asExploadeMap(v, Delimiters.COMMA) : asMap(v, Delimiters.COMMA)));

                    return valueMap;
                }

                return null;
            }

        };
    }
}

