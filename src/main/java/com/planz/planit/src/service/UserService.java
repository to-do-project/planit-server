package com.planz.planit.src.service;

import com.planz.planit.config.BaseException;
import com.planz.planit.config.BaseResponseStatus;
import com.planz.planit.src.domain.user.User;
import com.planz.planit.src.domain.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.planz.planit.config.BaseResponseStatus.DATABASE_ERROR;
import static com.planz.planit.config.BaseResponseStatus.NOT_EXIST_USER;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findUser(Long userId) throws BaseException {
        try{
            return userRepository.findById(userId).orElseThrow(()->new BaseException(NOT_EXIST_USER));
        }catch (Exception e){
            if(e instanceof BaseException){
                throw new BaseException(((BaseException) e).getStatus());
            }
            throw new BaseException(DATABASE_ERROR);
        }
    }
}
