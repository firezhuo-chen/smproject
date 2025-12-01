package is.smbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("is.smbackend.mapper")

public class SmBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmBackendApplication.class, args);
    }

}
