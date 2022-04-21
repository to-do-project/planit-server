package com.planz.planit.src.apicontroller;

import com.planz.planit.src.domain.user.User;
import com.planz.planit.src.domain.user.UserRole;
import com.planz.planit.src.domain.user.UserRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Log4j2
@RestController
public class UserController {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Autowired
    public UserController(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    // 회원가입
    @PostMapping("/join")
    public String join(@RequestBody Map<String, String> user) {
        log.info("UserController.join() 호출");

        userRepository.save(User.builder()
                .email(user.get("email"))
                .password(passwordEncoder.encode(user.get("password")))
                .role(UserRole.ROLE_USER) // 최초 가입시 USER 로 설정
                .nickname(user.get("email"))
                .build());
        return user.get("email") + " - 회원가입 성공";
    }

}
