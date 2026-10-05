package com.ccnlthd.taskmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;


@SpringBootApplication
public class TaskManagerApiApplication extends SpringBootServletInitializer {

	@Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder b) { return b.sources(TaskManagerApiApplication.class); }

	public static void main(String[] args) {
		SpringApplication.run(TaskManagerApiApplication.class, args);
	}

}

