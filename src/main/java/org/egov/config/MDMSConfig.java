package org.egov.config;

import lombok.*;
import org.egov.tracer.config.TracerConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

@Component
@Data
@Import({TracerConfiguration.class})
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class MDMSConfig {
    @Value("${kafka.topic.save.mdms.data}")
    private String saveMDMDSDataTopic;

    @Value("${kafka.topic.update.mdms.data}")
    private String updateMDMDSDataTopic;

    @Value("${kafka.topic.save.mdms.schemas}")
    private String saveMDMDSSchemaTopic;

    @Value("${kafka.topic.save.mdms.config}")
    private String saveMDMDSConfigTopic;

}
