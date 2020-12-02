package com.ai.gss.util;

import com.ai.gss.instance.YamlConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author pangms
 * @date 2020/9/24
 */
public class YamlToObject {
    private final static ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());

    public static <T>  T parseByClassPath(String name, Class<T> type) throws IOException {
        InputStream is = null;
        if(name.contains("classpath:")) {
            name = name.replace("classpath:", "");
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
        } else {
            is = new FileInputStream(name);
        }

        try {
            return YAML_MAPPER.readValue(is, type);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }
}
