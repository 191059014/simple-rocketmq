package com.hb.rocketmq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * RocketMQ工具类
 *
 * @version v0.1, 2020/8/13 17:39, create by huangbiao.
 */
public class RocketMQUtils {

    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RocketMQUtils.class);

    /**
     * 创建MQ消息实体
     *
     * @param topic      主题
     * @param msgContent 消息内容
     * @return MQ消息实体
     */
    public static Message createMessage(String topic, Object msgContent) {
        return createMessage(topic, msgContent, null);
    }

    /**
     * 创建MQ消息实体
     *
     * @param topic      主题
     * @param msgContent 消息内容
     * @param version    版本号
     * @return MQ消息实体
     */
    public static Message createMessage(String topic, Object msgContent, String version) {
        String encode = encode(topic, msgContent, version);
        Objects.requireNonNull(encode, "create MQ Message failed");
        Message message = new Message(topic, encode.getBytes());
        LOGGER.info("create MQ Message result = {}", message);
        return message;
    }

    /**
     * MQ发送消息转码
     *
     * @param topic      主题
     * @param msgContent 消息内容
     * @return json格式的消息
     */
    public static String encode(String topic, Object msgContent) {
        return encode(topic, msgContent, null);
    }

    /**
     * MQ发送消息转码
     *
     * @param topic      主题
     * @param msgContent 消息内容
     * @param version    版本号
     * @return json格式的消息
     */
    public static String encode(String topic, Object msgContent, String version) {
        // 请求头
        Map<String, String> header = new HashMap<>();
        header.put("version", version == null ? "1.0" : version);
        header.put("topicName", topic);
        // 请求体
        Map<String, Object> body = new HashMap<>();
        body.put("key", msgContent);
        // 完整请求参数
        Map<String, Object> map = new HashMap<>();
        map.put("header", header);
        map.put("body", body);
        // 转换为json
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String result = objectMapper.writeValueAsString(map);
            LOGGER.info("MQ send message encode result = {}", result);
            return result;
        } catch (JsonProcessingException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("MQ send message encode error: ", e);
            }
            return null;
        }
    }

    /**
     * MQ发送消息解码
     *
     * @param message 消息
     * @param tClass  类
     * @param <T>     泛型
     * @return T
     */
    public static <T> T decode(String message, Class<T> tClass) {
        Objects.requireNonNull(message, "message is null");
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(message);
            String msgContent = root.get("body").get("key").toString();
            T t = mapper.readValue(msgContent, tClass);
            LOGGER.info("MQ receive message decode result = {}", t);
            return t;
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("MQ receive message decode error: ", e);
            }
            return null;
        }

    }

}

    