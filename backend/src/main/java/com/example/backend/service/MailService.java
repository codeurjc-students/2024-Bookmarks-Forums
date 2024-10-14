package com.example.backend.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.example.backend.repository.UserRepository;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class MailService {

    private final UserRepository userRepository;

    private final JavaMailSender javaMailSender;

    private final ResourceLoader resourceLoader;

    private final Mustache.Compiler mustache;

    MailService(JavaMailSender javaMailSender, Mustache.Compiler mustache, UserRepository userRepository,
            ResourceLoader resourceLoader) {
        this.javaMailSender = javaMailSender;
        this.mustache = mustache;
        this.userRepository = userRepository;
        this.resourceLoader = resourceLoader;
    }

        private String loadHtmlContent(String to, String username, String subject, String text) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:/templates/mailPasswordTemplate.html");
        String htmlContent = Files.readString(Paths.get(resource.getURI()));

        Map<String, String> values = new HashMap<>();
        values.put("username", username);
        values.put("subject", subject);
        values.put("text", text);

        Template template = mustache.compile(htmlContent);
        return template.execute(values);

    }

    public void sendEmail(String to, String username, String subject, String text) throws MessagingException, IOException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setTo(to);
        helper.setSubject(subject);

        if (username != null) { 
            String htmlContent = loadHtmlContent(to, username, subject, text);
            helper.setText(htmlContent, true);
        } else { // if no username is given, get it from the database
            String htmlContent = loadHtmlContent(to, userRepository.findByEmail(to).getUsername(), subject, text);
            helper.setText(htmlContent, true);
        }

        javaMailSender.send(message);
    }

        public boolean isCorrectEmail(String email){
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }

}
