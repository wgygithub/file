package com.example.demo.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class JsonTest {

    private static final ObjectMapper OM = new ObjectMapper();

    public static JsonNode readTree(String jsonString) {
        JsonNode jsonNode = null;
        try {
            jsonNode = OM.readTree(jsonString);
        } catch (JsonProcessingException e) {
            System.out.printf("Read jsonString to JsonNode error", e);
        }
        return jsonNode;
    }


    public static Person getPerson() {
        Person person = new Person();
        person.setUserName("小明");
        person.setAge(10);
        person.setHeader(Arrays.asList("11111", "22222", "3333"));
        ArrayList<Hobby> list = new ArrayList();
        list.add(new Hobby("游泳", "用力游泳"));
        list.add(new Hobby("打篮球", "投蓝得分"));
        person.setHobbies(list);
        return person;
    }

    public static void main(String[] args) throws JsonProcessingException {

        Person person = getPerson();

        String json = OM.writeValueAsString(person);

        Set<String> keys = new HashSet<>();
        keys.add("header");
        keys.add("userName");
        keys.add("hobbyName");
        keys.add("rule");
        String s = desensitizedJsonString(json, keys);

        System.out.printf(s);

    }

    public static String desensitizedJsonString(String jsonString,
                                                Set<String> keys) {
        if (StringUtils.isBlank(jsonString)) {
            return jsonString;
        }
        JsonNode jsonNode = readTree(jsonString);
        return desensitizedJsonNode(jsonNode, keys);
    }

    public static String desensitizedJsonNode(JsonNode jsonNode,
                                              Set<String> keys) {
        desensitizedKeysValue(jsonNode, keys);
        return jsonNode.toString();
    }


    private static void desensitizedValueNode(ObjectNode on, String key, JsonNode value) {
        if (value == null) {
            return;
        }
        if (value.isTextual()) {
            on.replace(key, new TextNode(desensitizedString(value.asText())));
        } else {
            on.replace(key, NullNode.getInstance());
        }
    }

    /**
     * desensitized jsonNode keys value
     *
     * @param jsonNode jsonNode
     * @param keys     keys
     */
    private static void desensitizedKeysValue(JsonNode jsonNode, Set<String> keys) {
        Iterator<String> stringIterator = jsonNode.fieldNames();
        while (stringIterator.hasNext()) {
            String fieldName = stringIterator.next();
            JsonNode item = jsonNode.get(fieldName);
            if (item.isObject()) {
                desensitizedKeysValue(item, keys);
            } else if (item.isArray()) {
                desensitizedKeysValueArray((ObjectNode) jsonNode, item, fieldName, keys);
            } else {
                if (keys.contains(fieldName)) {
                    desensitizedValueNode((ObjectNode) jsonNode, fieldName, item);
                }
            }
        }
    }

    private static void desensitizedKeysValueArray(ObjectNode parent, JsonNode arrayNode, String key, Set<String> keys) {
        StringBuffer buffer = new StringBuffer();
        boolean flag = false;
        for (int i = 0; i < arrayNode.size(); i++) {
            JsonNode jsonNode = arrayNode.get(i);
            if (jsonNode.isObject()) {
                desensitizedKeysValue(jsonNode, keys);
            } else if (keys.contains(key)) {
                if (i == 0) buffer.append("[");
                String s = desensitizedString(jsonNode.toString());
                buffer.append("'").append(s).append("'");
                flag = true;
                if (i != arrayNode.size() - 1) buffer.append(",");
            }
        }
        if (flag) {
            buffer.append("]");
            parent.replace(key, new TextNode(buffer.toString()));

        }
    }

    /**
     * 字符串脱敏
     *
     * @param text 账号
     * @return 结果
     */
    private static String desensitizedString(String text) {
        return "*****";
    }
}
