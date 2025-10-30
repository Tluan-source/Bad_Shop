package vn.iotstar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "vn.iotstar")
public class BadmintonMarketplaceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BadmintonMarketplaceApplication.class, args);
	}
}
