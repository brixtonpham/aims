package com.aims.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * Notification configuration for email and other notification channels
 * Configures email settings and notification properties
 */
@Configuration
public class NotificationConfig {

    /**
     * Email configuration properties
     */
    @Bean
    @ConfigurationProperties(prefix = "notification.email")
    public EmailProperties emailProperties() {
        return new EmailProperties();
    }

    /**
     * Email configuration properties class
     */
    public static class EmailProperties {
        private String host;
        private int port;
        private String username;
        private String password;
        private String fromAddress;
        private String fromName;
        private boolean enableSmtpAuth = true;
        private boolean enableStartTls = true;
        private int connectionTimeout = 5000;
        private int timeout = 5000;

        // Getters and Setters
        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getFromAddress() {
            return fromAddress;
        }

        public void setFromAddress(String fromAddress) {
            this.fromAddress = fromAddress;
        }

        public String getFromName() {
            return fromName;
        }

        public void setFromName(String fromName) {
            this.fromName = fromName;
        }

        public boolean isEnableSmtpAuth() {
            return enableSmtpAuth;
        }

        public void setEnableSmtpAuth(boolean enableSmtpAuth) {
            this.enableSmtpAuth = enableSmtpAuth;
        }

        public boolean isEnableStartTls() {
            return enableStartTls;
        }

        public void setEnableStartTls(boolean enableStartTls) {
            this.enableStartTls = enableStartTls;
        }

        public int getConnectionTimeout() {
            return connectionTimeout;
        }

        public void setConnectionTimeout(int connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
        }

        public int getTimeout() {
            return timeout;
        }

        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }
    }

    /**
     * Java Mail Sender configuration
     */
    @Bean
    @Profile("!test")
    public JavaMailSender javaMailSender(EmailProperties emailProperties) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        
        mailSender.setHost(emailProperties.getHost());
        mailSender.setPort(emailProperties.getPort());
        mailSender.setUsername(emailProperties.getUsername());
        mailSender.setPassword(emailProperties.getPassword());
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", emailProperties.isEnableSmtpAuth());
        props.put("mail.smtp.starttls.enable", emailProperties.isEnableStartTls());
        props.put("mail.smtp.connectiontimeout", emailProperties.getConnectionTimeout());
        props.put("mail.smtp.timeout", emailProperties.getTimeout());
        props.put("mail.debug", "false");
        
        return mailSender;
    }

    /**
     * Mock Mail Sender for testing
     */
    @Bean
    @Profile("test")
    public JavaMailSender mockMailSender() {
        return new JavaMailSenderImpl(); // Simple mock implementation
    }

    /**
     * Development profile email configuration
     */
    @Bean
    @Profile("dev")
    public EmailProperties devEmailProperties() {
        EmailProperties props = new EmailProperties();
        props.setHost("smtp.gmail.com");
        props.setPort(587);
        props.setFromAddress("noreply@aims.com");
        props.setFromName("AIMS Development");
        return props;
    }

    /**
     * Production profile email configuration
     */
    @Bean
    @Profile("prod")
    public EmailProperties prodEmailProperties() {
        EmailProperties props = new EmailProperties();
        props.setFromAddress("noreply@aims.com");
        props.setFromName("AIMS System");
        return props;
    }
}
