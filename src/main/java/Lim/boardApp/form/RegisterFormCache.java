package Lim.boardApp.form;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Data
public class RegisterFormCache {

    private Map<String ,CustomerRegisterForm> registerFormMap;

    public RegisterFormCache(){
        registerFormMap = ExpiringMap.builder()
                .expirationPolicy(ExpirationPolicy.CREATED)
                .expiration(10, TimeUnit.MINUTES)
                .build();
    }
}
