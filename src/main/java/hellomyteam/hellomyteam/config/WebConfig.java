package hellomyteam.hellomyteam.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
//                .allowedOrigins("https://hellomyteam.com",
//                        "https://www.hellomyteam.com",
//                        "https://hellomyteam.store",
//                        "https://www.hellomyteam.store",
//                        "http://localhost:3000/",
//                        "https://hellomyteam-front-o8od.vercel.app/")
                .allowedMethods("*");
    }
}

