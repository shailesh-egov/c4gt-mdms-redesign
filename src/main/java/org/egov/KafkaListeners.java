package org.egov;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaListeners {

    //Just to check kafka connection
    //Will be removed
    @KafkaListener(topics = "mdms",groupId = "groupId")
    void listener(String data){
        System.out.println("Listener received: "+data+" done");

    }
}
