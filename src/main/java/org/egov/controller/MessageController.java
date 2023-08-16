package org.egov.controller;

import org.egov.MessageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//This is just for checking kafka functionality will be removed
@RestController
@RequestMapping("mdms/v1/messages")
public class MessageController {

    private KafkaTemplate<String,String> kafkaTemplate;

    public MessageController(KafkaTemplate<String,String> kafkaTemplate){
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping
    public void publish(@RequestBody MessageRequest request){
        kafkaTemplate.send("mdms",request.getMessage());
    }
}