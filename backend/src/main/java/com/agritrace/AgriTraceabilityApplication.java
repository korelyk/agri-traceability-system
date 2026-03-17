package com.agritrace;

import com.agritrace.blockchain.Blockchain;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication(exclude = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class })
public class AgriTraceabilityApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgriTraceabilityApplication.class, args);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CommandLineRunner init(Blockchain blockchain) {
        return args -> {
            System.out.println("======================================");
            System.out.println("  Agri Traceability System started");
            System.out.println("======================================");
            blockchain.printChain();
        };
    }
}
