package com.ccnlthd.taskmanager;

import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class TaskManagerApiApplication extends SpringBootServletInitializer {

	@Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder b) {
        return b.sources(TaskManagerApiApplication.class)
                .properties("spring.profiles.default=dev");
    }

	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(TaskManagerApiApplication.class);
		application.setDefaultProperties(Map.of("spring.profiles.default", "dev"));
		application.run(args);
	}

}
