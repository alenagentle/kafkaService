package com.irlix.Server.kafkaconf;

import com.irlix.Server.models.KafkaResponseModel;
import com.irlix.Server.models.MsgModel;
import com.irlix.Server.models.UserLoginModel;
import com.irlix.Server.models.UserRegistrationModel;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfiguration {

    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaServer;

    @Value("${spring.kafka.consumer.group-id}")
    private String kafkaGroupId;

    @Value("${kafka.topic.car.reply}")
    private String replyTopic;

    @Bean
    public Map<String, Object> requestProducerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                kafkaServer);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JsonSerializer.class);
        //props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 1_000);
        return props;
    }

    @Bean
    public ProducerFactory<String, UserRegistrationModel> requestProducerFactory() {
        return new DefaultKafkaProducerFactory<>(requestProducerConfigs());
    }

    public ConsumerFactory<String, KafkaResponseModel> consumerResultFactory() {
        JsonDeserializer<KafkaResponseModel> deserializer = new JsonDeserializer<>(KafkaResponseModel.class);
        deserializer.setRemoveTypeHeaders(false);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeMapperForKey(true);

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializer);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaGroupId);
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(),deserializer);
    }

    @Bean
    public  KafkaMessageListenerContainer<String ,KafkaResponseModel> replyListenerContainer() {
        ContainerProperties containerProperties = new ContainerProperties(replyTopic);
        return  new KafkaMessageListenerContainer<>(consumerResultFactory() , containerProperties);
    }

    @Bean
    public ReplyingKafkaTemplate<String, UserRegistrationModel, KafkaResponseModel> replyingKafkaTemplate( ProducerFactory<String, UserRegistrationModel> producerFactory,
                                                                                           KafkaMessageListenerContainer<String, KafkaResponseModel> messageListenerContainer) {
        return new ReplyingKafkaTemplate<>(producerFactory,messageListenerContainer);

    }
}