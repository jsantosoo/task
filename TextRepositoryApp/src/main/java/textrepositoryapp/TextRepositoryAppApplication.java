package textrepositoryapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableKafka
public class TextRepositoryAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(TextRepositoryAppApplication.class, args);
    }
}
