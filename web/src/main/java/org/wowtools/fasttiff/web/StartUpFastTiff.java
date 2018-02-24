package org.wowtools.fasttiff.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * @author liuyu
 * @date 2018/2/24
 */
@SpringBootApplication
@EnableEurekaClient
public class StartUpFastTiff {
    public static void main(String[] args) {
        SpringApplication.run(StartUpFastTiff.class, args);
    }
}
