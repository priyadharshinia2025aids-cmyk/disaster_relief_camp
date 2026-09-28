package com.reliefcamp.tracker.service;
import org.slf4j.*;
import org.springframework.stereotype.Service;
@Service
public class NotificationService {
    private static final Logger log=LoggerFactory.getLogger(NotificationService.class);
    public void statusChanged(String message){log.info("[RELIEF CAMP NOTIFICATION] {}",message);}
}
