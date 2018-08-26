package com.ljheee.api.core;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lijianhua04 on 2018/8/23.
 */
public class JsonUtil {

    private static ObjectMapper mapper = new ObjectMapper();


    public static Map<String,Object> toMap(String params) throws IllegalArgumentException {
        Map<String, Object> map = new HashMap<String, Object>();
        ObjectMapper mapper = new ObjectMapper();

        try{
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.configure(DeserializationFeature.USE_LONG_FOR_INTS, true);

//            map = mapper.readValue(params, Map.class);
            map = mapper.readValue(params, new TypeReference<HashMap<String,Object>>(){});
        }catch(Exception e){
            throw new IllegalArgumentException(e);
        }

        return map;
    }

    public static <T> T convertValue(Object value, Class<T> clazz) throws IllegalArgumentException {

        if(StringUtils.isEmpty(value)){
            return null;
        }

        try {
            if(value instanceof String)
                value = mapper.readTree((String) value);
            return mapper.convertValue(value, clazz);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }

    }


}
