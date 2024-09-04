package ru.Tim.Proj.moneyAnalyzer;

import jakarta.servlet.Filter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.filter.HiddenHttpMethodFilter;

@SpringBootApplication
@EnableAsync
public class MoneyAnalyzerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoneyAnalyzerApplication.class, args);
	}

	@Bean
	public Filter hiddenHttpMethodFilter() {
		return new HiddenHttpMethodFilter();
	}

}
