package is.smbackend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SpringBoot3+Vue3脚手架")
                        .version("1.0")
                        .description( "SpringBoot3+Vue3脚手架接口文档")
                        .contact(new Contact().name("smbackend").url("www.smbackend.com")));
    }
}
