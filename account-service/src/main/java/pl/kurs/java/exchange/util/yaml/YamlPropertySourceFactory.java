package pl.kurs.java.exchange.util.yaml;

import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.support.PropertySourceFactory;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class YamlPropertySourceFactory implements PropertySourceFactory {
    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
        Properties propertiesFromYaml = loadYamlIntoProperties(resource);
        String sourceName = name != null ? name : resource.getResource().getFilename();
        return new PropertiesPropertySource(sourceName, propertiesFromYaml);
    }

    private Properties loadYamlIntoProperties(EncodedResource resource) throws IOException {
        Properties properties = new Properties();
        Yaml yaml = new Yaml();
        InputStream inputStream = resource.getInputStream();
        try {
            Map<String, Object> yamlMap = yaml.load(inputStream);
            properties.putAll(flattenMap(yamlMap));
        } finally {
            inputStream.close();
        }
        return properties;
    }

    private Map<String, Object> flattenMap(Map<String, Object> source) {
        Map<String, Object> result = new HashMap<>();
        buildFlattenedMap(result, source, null);
        return result;
    }

    private void buildFlattenedMap(Map<String, Object> result, Map<String, Object> source, String path) {
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (StringUtils.hasText(path)) {
                key = path + '.' + key;
            }
            if (value instanceof Map) {
                // Need to flatten nested maps
                @SuppressWarnings("unchecked")
                Map<String, Object> subMap = (Map<String, Object>) value;
                buildFlattenedMap(result, subMap, key);
            } else {
                result.put(key, value);
            }
        }
    }
}
