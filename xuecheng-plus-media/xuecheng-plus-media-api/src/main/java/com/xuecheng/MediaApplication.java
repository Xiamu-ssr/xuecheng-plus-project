package com.xuecheng;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class MediaApplication {
	public static void main(String[] args) {
		SpringApplication.run(MediaApplication.class, args);
	}
}