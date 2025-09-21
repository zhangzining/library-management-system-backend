package com.zzn.librarysystem.common.config;

import com.zzn.librarysystem.common.util.DataUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class SpringSecurityAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        String userName = DataUtil.getCurrentUserName();
        log.info("[GetCurrentAuditor] userName:{}", userName);
        return Optional.ofNullable(userName);
    }
}
