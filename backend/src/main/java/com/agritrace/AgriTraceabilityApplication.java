package com.agritrace;

import com.agritrace.blockchain.Blockchain;
import com.agritrace.crypto.DigitalSignature;
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
            System.out.println("  农产品防伪溯源系统启动成功");
            System.out.println("  基于区块链与数字签名技术");
            System.out.println("======================================");
            System.out.println("区块链状态");
            blockchain.printChain();

            System.out.println("\n运行数字签名测试...");
            DigitalSignature.main(args);
        };
    }
}
