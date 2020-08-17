# simple-rocketmq
针对rocketmq的薄封装

## 如何使用
直接下载本项目，然后pom引入即可使用

## 核心类
1. RocketMQUtils
```
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
 * 工具类
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
```
2. RocketMQWapper
```
package com.hb.rocketmq;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * rocket包装类
 *
 * @version v0.1, 2020/8/14 16:39, create by huangbiao.
 */
public class RocketMQWapper {

    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RocketMQWapper.class);

    /**
     * 创建一个普通消息的生产者
     *
     * @param producerGroup 生产者组
     * @param nameSrvAddr   状态机集群地址，多个ip:port，中间用逗号隔开
     * @return DefaultMQProducer
     */
    public static DefaultMQProducer createSimpleProducer(String producerGroup, String nameSrvAddr) {
        Objects.requireNonNull(producerGroup, "producerGroup is null");
        Objects.requireNonNull(nameSrvAddr, "nameSrvAddr is null");
        DefaultMQProducer defaultMQProducer = new DefaultMQProducer(producerGroup);
        defaultMQProducer.setNamesrvAddr(nameSrvAddr);
        LOGGER.info("DefaultMQProducer create success [producerGroup={}, nameSrvAddr={}]", producerGroup, nameSrvAddr);
        return defaultMQProducer;
    }

    /**
     * 创建一个事务消息的生产者
     *
     * @param producerGroup       生产者组
     * @param nameSrvAddr         状态机集群地址，多个ip:port，中间用逗号隔开
     * @param executorService     线程池
     * @param transactionListener 监听
     * @return TransactionProducerAgent
     */
    public static TransactionMQProducer createTransactionProducer(String producerGroup, String nameSrvAddr, ExecutorService executorService, TransactionListener transactionListener) {
        Objects.requireNonNull(producerGroup, "producerGroup is null");
        Objects.requireNonNull(nameSrvAddr, "nameSrvAddr is null");
        Objects.requireNonNull(executorService, "executorService is null");
        Objects.requireNonNull(transactionListener, "transactionListener is null");
        TransactionMQProducer transactionMQProducer = new TransactionMQProducer(producerGroup);
        transactionMQProducer.setNamesrvAddr(nameSrvAddr);
        transactionMQProducer.setExecutorService(executorService);
        transactionMQProducer.setTransactionListener(transactionListener);
        LOGGER.info("TransactionMQProducer create success [producerGroup={}, nameSrvAddr={}, executorService={}, transactionListener={}]", producerGroup, nameSrvAddr, executorService, transactionListener);
        return transactionMQProducer;
    }

    /**
     * 创建（推）消息消费者（tags为*，集群消费模式）
     *
     * @param consumerGroup   消费者组
     * @param nameSrvAddr     状态机集群地址
     * @param topic           消息主题
     * @param messageListener 监听
     * @return PushConsumerAgent
     */
    public static DefaultMQPushConsumer createPushConsumer(String consumerGroup, String nameSrvAddr, String topic, MessageListenerConcurrently messageListener) throws MQClientException {
        return createPushConsumer(consumerGroup, nameSrvAddr, topic, null, null, null, null, messageListener);
    }

    /**
     * 创建（推）消息消费者（tags为*）
     *
     * @param consumerGroup   消费者组
     * @param nameSrvAddr     状态机集群地址
     * @param topic           消息主题
     * @param messageModel    消费模式
     * @param messageListener 监听
     * @return PushConsumerAgent
     */
    public static DefaultMQPushConsumer createPushConsumer(String consumerGroup, String nameSrvAddr, String topic, MessageModel messageModel, MessageListenerConcurrently messageListener) throws MQClientException {
        return createPushConsumer(consumerGroup, nameSrvAddr, topic, null, messageModel, null, null, messageListener);
    }

    /**
     * 创建（推）消息消费者（集群消费模式）
     *
     * @param consumerGroup   消费者组
     * @param nameSrvAddr     状态机集群地址
     * @param topic           消息主题
     * @param tags            标签
     * @param messageListener 监听
     * @return PushConsumerAgent
     */
    public static DefaultMQPushConsumer createPushConsumer(String consumerGroup, String nameSrvAddr, String topic, String tags, MessageListenerConcurrently messageListener) throws MQClientException {
        return createPushConsumer(consumerGroup, nameSrvAddr, topic, tags, null, null, null, messageListener);
    }

    /**
     * 创建（推）消息消费者
     *
     * @param consumerGroup   消费者组
     * @param nameSrvAddr     状态机集群地址
     * @param topic           消息主题
     * @param tags            标签
     * @param messageModel    消费模式
     * @param messageListener 监听
     * @return PushConsumerAgent
     */
    public static DefaultMQPushConsumer createPushConsumer(String consumerGroup, String nameSrvAddr, String topic, String tags, MessageModel messageModel, MessageListenerConcurrently messageListener) throws MQClientException {
        return createPushConsumer(consumerGroup, nameSrvAddr, topic, tags, messageModel, null, null, messageListener);
    }

    /**
     * 创建（推）消息消费者
     *
     * @param consumerGroup   消费者组
     * @param nameSrvAddr     状态机集群地址
     * @param topic           消息主题
     * @param tags            标签
     * @param messageModel    消费模式
     * @param threadMax       最大线程数
     * @param threadMin       最小线程数
     * @param messageListener 监听
     * @return PushConsumerAgent
     */
    public static DefaultMQPushConsumer createPushConsumer(String consumerGroup, String nameSrvAddr, String topic, String tags, MessageModel messageModel, Integer threadMax, Integer threadMin, MessageListenerConcurrently messageListener) throws MQClientException {
        Objects.requireNonNull(consumerGroup, "consumerGroup is null");
        Objects.requireNonNull(nameSrvAddr, "nameSrvAddr is null");
        Objects.requireNonNull(topic, "topic is null");
        Objects.requireNonNull(messageListener, "messageListener is null");
        DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer(consumerGroup);
        defaultMQPushConsumer.setNamesrvAddr(nameSrvAddr);
        // 程序第一次启动从消息队列头取数据
        // 如果非第一次启动，那么按照上次消费的位置继续消费
        defaultMQPushConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        if (tags != null) {
            defaultMQPushConsumer.subscribe(topic, tags);
        } else {
            defaultMQPushConsumer.subscribe(topic, "*");
        }
        if (messageModel != null) {
            defaultMQPushConsumer.setMessageModel(messageModel);
        }
        if (threadMax != null) {
            defaultMQPushConsumer.setConsumeThreadMax(threadMax);
        }
        if (threadMin != null) {
            defaultMQPushConsumer.setConsumeThreadMin(threadMin);
        }
        defaultMQPushConsumer.registerMessageListener(messageListener);
        LOGGER.info("DefaultMQPushConsumer create success [consumerGroup={}, nameSrvAddr={}, topic={}, tags={}, messageModel={}, threadMax={}, threadMin={}, messageListener={}]", consumerGroup, nameSrvAddr, topic, tags, messageModel, threadMax, threadMin, messageListener);
        return defaultMQPushConsumer;
    }

}

    
```