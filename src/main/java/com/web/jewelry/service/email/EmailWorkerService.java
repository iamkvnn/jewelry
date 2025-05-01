package com.web.jewelry.service.email;

import com.web.jewelry.dto.request.EmailRequest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class EmailWorkerService {
    private final EmailQueueService queueService;
    private final JavaMailSender mailSender;

    public EmailWorkerService(EmailQueueService queueService, JavaMailSender mailSender) {
        this.queueService = queueService;
        this.mailSender = mailSender;
        startWorker();
    }

    private void startWorker() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            while (true) {
                try {
                    EmailRequest request = queueService.dequeue();
                    sendEmail(request);
                } catch (Exception e) {
                    System.out.println("Error processing email request: " + e.getMessage());
                }
            }
        });
    }

    private void sendEmail(EmailRequest request) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(request.getTo());
        message.setSubject(request.getSubject());
        message.setText(request.getBody());
        mailSender.send(message);
    }
}

