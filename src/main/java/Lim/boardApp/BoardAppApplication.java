package Lim.boardApp;

import Lim.boardApp.domain.Text;
import Lim.boardApp.repository.TextRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Calendar;

@SpringBootApplication
public class BoardAppApplication {
	public static void main(String[] args) {
		SpringApplication.run(BoardAppApplication.class, args);
	}
}
