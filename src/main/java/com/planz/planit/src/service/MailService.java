package com.planz.planit.src.service;

import com.planz.planit.config.BaseException;
import com.planz.planit.src.domain.mail.MailDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import static com.planz.planit.config.BaseResponseStatus.MAIL_SEND_ERROR;

@Service
public class MailService {

    private final JavaMailSender javaMailSender;
    private static final String FROM_ADDRESS = "planz0520@gmail.com";

    @Autowired
    public MailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

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
            throw new BaseException(MAIL_SEND_ERROR);
        }
    }
}
