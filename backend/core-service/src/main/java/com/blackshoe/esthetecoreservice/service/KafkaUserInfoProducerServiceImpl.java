package com.blackshoe.esthetecoreservice.service;

import com.blackshoe.esthetecoreservice.dto.UserDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor @Slf4j @Service
public class KafkaUserInfoProducerServiceImpl implements KafkaUserInfoProducerService{
    private final ObjectMapper objectMapper;
    private final KafkaProducer kafkaProducer;

    @Override
    public void deleteUser(UserDto.UserInfoDto userInfoDto) {
        String topic = "user-delete";

        String userJsonString;
        try {
            userJsonString = objectMapper.writeValueAsString(userInfoDto);
        } catch (Exception e) {
            log.error("Mapping dto to json string error", e);
            throw new RuntimeException("Error publishing created user");
        }

        kafkaProducer.send(topic, userJsonString);
    }
}
