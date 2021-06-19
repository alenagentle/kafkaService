package com.irlix.Server.services;

import com.irlix.Server.models.KafkaResponseModel;
import com.irlix.Server.models.UserRegistrationModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import java.io.IOException;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@EnableKafka
public class KafkaService {

    @Value("${kafka.topic.car.reply}")
    private String replyTopic;

    @KafkaListener(topics = "${kafka.topic.car.request}" , containerFactory = "requestListenerContainerFactory")
    @SendTo
    public KafkaResponseModel receive(UserRegistrationModel userRegistration) {
//        System.out.println("KafkaListener!");
//        for (int i = 0; i<659999990; i++) {
//            Random random = new Random();
//            random.nextInt(10);
//        }
        return validatePassword(userRegistration.getPassword());
    }

    public KafkaResponseModel validatePassword(String password){
        System.out.println("validatePassword: password = "  + password);
        KafkaResponseModel kafkaResponseModel = new KafkaResponseModel();
        Pattern pattern = Pattern.compile("#|!|@|%|&|\\*");
        Matcher matcher = pattern.matcher(password);
        if(matcher.find()){
            String successDescription = "Проверка выполнена.";
            System.out.println(successDescription);
            kafkaResponseModel.setValidationState(true);
            kafkaResponseModel.setDescription(successDescription);
        }
        else {
            String failureDescription = "Пароль не надежен, отсутствуют специальные символы";
            System.out.println(failureDescription);
            kafkaResponseModel.setValidationState(false);
            kafkaResponseModel.setDescription(failureDescription);
        }
        return  kafkaResponseModel;
    }
}
