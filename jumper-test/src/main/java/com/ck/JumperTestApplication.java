package com.ck;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @ClassName EurekaApplication
 * @Description Gateway启动类
 * @Author Cyk
 * @Version 1.0
 * @since 2022/5/5 17:03
 **/
@MapperScan(basePackages = "com.ck.db.mapper")
@SpringBootApplication
public class JumperTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(JumperTestApplication.class, args);
    }
}
