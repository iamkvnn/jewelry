package com.web.jewelry.service.email;

import com.web.jewelry.dto.request.EmailRequest;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class EmailQueueService {
    private final BlockingQueue<EmailRequest> emailQueue = new LinkedBlockingQueue<>();

    public void enqueue(EmailRequest request) {
        System.out.println("Enqueuing email request: " + request.getTo());
        if(!emailQueue.offer(request)) {
            System.out.println("Email queue is full. Unable to enqueue request.");
        }
    }

    public EmailRequest dequeue() throws InterruptedException {
        return emailQueue.take();
    }
}

