package com.planz.planit.src.service;

import com.planz.planit.config.BaseException;
import com.planz.planit.src.domain.mail.MailDTO;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import static com.planz.planit.config.BaseResponseStatus.MAIL_SEND_ERROR;

@Service
@Log4j2
public class MailService {

    private final JavaMailSender javaMailSender;
    private static final String FROM_ADDRESS = "planz0520@gmail.com";

    @Autowired
    public MailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    // 메일 전송하기
    public void mailSend(MailDTO mailDTO) throws BaseException{
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM_ADDRESS);
            message.setTo(mailDTO.getAddress());
            message.setSubject(mailDTO.getTitle());
            message.setText(mailDTO.getContent());
            javaMailSender.send(message);
        }
        catch (Exception e){
            log.error("mailSend() : javaMailSender.send(message) 실행 중 에러 발생");
            throw new BaseException(MAIL_SEND_ERROR);
        }
    }
}
