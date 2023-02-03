package Lim.boardApp;

import Lim.boardApp.domain.Text;
import Lim.boardApp.repository.TextRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;

import java.util.Calendar;

@EnableJpaAuditing
@SpringBootApplication
public class BoardAppApplication {
	public static void main(String[] args) {
		SpringApplication.run(BoardAppApplication.class, args);
	}

}
