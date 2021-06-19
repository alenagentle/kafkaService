package com.irlix.Server.services;

import com.irlix.Server.entities.User;
import com.irlix.Server.models.KafkaResponseModel;
import com.irlix.Server.models.UserRegistrationModel;
import com.irlix.Server.repositories.UserRepository;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class UserService implements UserDetailsService {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private ReplyingKafkaTemplate< String, UserRegistrationModel, KafkaResponseModel > requestReplyKafkaTemplate;

    @Autowired
    public void setRequestReplyKafkaTemplate(ReplyingKafkaTemplate<String, UserRegistrationModel, KafkaResponseModel> requestReplyKafkaTemplate) {
        this.requestReplyKafkaTemplate = requestReplyKafkaTemplate;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    @Value("${kafka.topic.car.request}")
    private String requestTopic;

    @Value("${kafka.topic.car.reply}")
    private String replyTopic;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

//        System.out.println("User name= " + username);
        User user = userRepository
                .findByLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь c именем " + username + " не был найден"));

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getLogin())
                .password(user.getPassword())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("USER")))
                .build();
        return userDetails;
    }

    public void register(UserRegistrationModel userRegistration) {
        User user = new User(
                userRegistration.getLogin(),
                passwordEncoder.encode(userRegistration.getPassword()),
                userRegistration.getEmail(),
                userRegistration.getFio());
        this.userRepository.save(user);
    }

    public List<User> getUsersByStringFilter(String str) {
        List<User> listUsers = this.userRepository.findAllByFioContains(str);
//        for (User user: listUsers
//        ) {
//            System.out.println("List: user.getLogin() = " + user.getLogin());
//        }
        System.out.println("listUsers = " + listUsers);
        return listUsers;
    }

    public KafkaResponseModel sendValidatePassword(UserRegistrationModel userRegistration) throws InterruptedException, ExecutionException, TimeoutException {
        System.out.println("sendValidatePassword...");
//        Message<UserRegistrationModel> message = MessageBuilder
//                .withPayload(userRegistration)
//                .setHeader(KafkaHeaders.TOPIC, TOPIC)
//                .build();
//        ListenableFuture<SendResult<String, UserRegistrationModel>> future = this.kafkaTemplate.send(message);

        ProducerRecord <String, UserRegistrationModel> record = new ProducerRecord <String, UserRegistrationModel>(requestTopic, userRegistration);
        record.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, replyTopic.getBytes()));

        RequestReplyFuture< String, UserRegistrationModel, KafkaResponseModel > requestReplyFuture =
                requestReplyKafkaTemplate.sendAndReceive(record);
        try {
            return requestReplyFuture.get(2L, TimeUnit.SECONDS).value();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            KafkaResponseModel kafkaResponse = new KafkaResponseModel();
            kafkaResponse.setValidationState(false);
            kafkaResponse.setDescription("processing interrupted");
            return kafkaResponse;
        } catch (ExecutionException e) {
            e.printStackTrace();
            KafkaResponseModel kafkaResponse = new KafkaResponseModel();
            kafkaResponse.setValidationState(false);
            kafkaResponse.setDescription("something wrong");
            return kafkaResponse;
        } catch (TimeoutException e) {
            e.printStackTrace();
            KafkaResponseModel kafkaResponse = new KafkaResponseModel();
            kafkaResponse.setValidationState(false);
            kafkaResponse.setDescription("processing time out");
            return kafkaResponse;
        }
    }
}
