package com.ck;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@MapperScan(basePackages = "com.ck.db.mapper")
@SpringBootApplication
public class JumperTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(JumperTestApplication.class, args);
    }
}
