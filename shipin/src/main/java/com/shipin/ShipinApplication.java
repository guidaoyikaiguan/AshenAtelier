package com.shipin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.shipin.mappers")
@EnableAspectJAutoProxy
@EnableScheduling
public class ShipinApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShipinApplication.class, args);
    }

}