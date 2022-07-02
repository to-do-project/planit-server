package com.planz.planit.config.security.auth;

import com.planz.planit.src.domain.user.User;
import com.planz.planit.src.domain.user.UserRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class PrincipalDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public PrincipalDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("loadUserByUsername() => 로그인 정보와 DB 정보가 일치하는지 비교 중");
        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null){
            return new PrincipalDetails(user);
        }
        else{
            log.info("사용자를 찾을 수 없습니다.");
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다.");
        }
    }
}
