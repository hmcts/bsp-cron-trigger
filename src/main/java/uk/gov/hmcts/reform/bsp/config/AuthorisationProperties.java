package uk.gov.hmcts.reform.bsp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "authorisation")
@Getter
@Setter
public class AuthorisationProperties {
    private String bearerToken;
}
