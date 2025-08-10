package id.ac.tazkia.minibank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;

import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;

@SpringBootApplication
public class AplikasiMinibankApplication {

	public static void main(String[] args) {
		SpringApplication.run(AplikasiMinibankApplication.class, args);
	}

	@Bean
	public LayoutDialect layoutDialect() {
	return new LayoutDialect();
	}

	@Bean
    public SpringSecurityDialect springSecurityDialect() {
        return new SpringSecurityDialect();
    }

}
