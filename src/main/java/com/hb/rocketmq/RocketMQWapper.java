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
 * RocketMQ包装类
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

    