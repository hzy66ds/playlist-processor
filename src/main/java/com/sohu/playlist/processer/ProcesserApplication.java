package com.sohu.playlist.processer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
@ImportResource("classpath:spring/*.xml")
@PropertySource("classpath:config.properties")
public class ProcesserApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProcesserApplication.class, args);
    }

}
