package com.alamo.asistencia.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.base-dir:uploads}")
    private String baseDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        Path root = Paths.get(baseDir).toAbsolutePath().normalize();

        String location = root.toUri().toString();
        if (!location.endsWith("/")) location += "/";

        // ✅ Esto sirve: /uploads/entregables/...  ->  C:/root/Asistencia/uploads/entregables/...
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location)
                .setCachePeriod(0);
    }
}
