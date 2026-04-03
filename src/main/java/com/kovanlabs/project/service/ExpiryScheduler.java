package com.kovanlabs.project.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@EnableScheduling
public class ExpiryScheduler {

    @Autowired
    private ExpiryService expiryService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void runDaily() {
        expiryService.markExpired();
    }
}