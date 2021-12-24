package com.web.jwtauth;

import com.web.jwtauth.jms.Message;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.jms.JMSException;
import javax.jms.Queue;

@Configuration
public class Config {

    @Bean
    public Message message(){
        return new Message();
    }

    @Bean
    public Queue queue(){
        return () -> "queue";
    }
}
