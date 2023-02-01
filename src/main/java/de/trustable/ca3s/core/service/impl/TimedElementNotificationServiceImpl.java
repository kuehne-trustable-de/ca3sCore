package de.trustable.ca3s.core.service.impl;

import de.trustable.ca3s.core.domain.TimedElementNotification;
import de.trustable.ca3s.core.repository.TimedElementNotificationRepository;
import de.trustable.ca3s.core.service.TimedElementNotificationService;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link TimedElementNotification}.
 */
@Service
@Transactional
public class TimedElementNotificationServiceImpl implements TimedElementNotificationService {

    private final Logger log = LoggerFactory.getLogger(TimedElementNotificationServiceImpl.class);

    private final TimedElementNotificationRepository timedElementNotificationRepository;

    public TimedElementNotificationServiceImpl(TimedElementNotificationRepository timedElementNotificationRepository) {
        this.timedElementNotificationRepository = timedElementNotificationRepository;
    }

    @Override
    public TimedElementNotification save(TimedElementNotification timedElementNotification) {
        log.debug("Request to save TimedElementNotification : {}", timedElementNotification);
        return timedElementNotificationRepository.save(timedElementNotification);
    }

    @Override
    public TimedElementNotification update(TimedElementNotification timedElementNotification) {
        log.debug("Request to update TimedElementNotification : {}", timedElementNotification);
        return timedElementNotificationRepository.save(timedElementNotification);
    }

    @Override
    public Optional<TimedElementNotification> partialUpdate(TimedElementNotification timedElementNotification) {
        log.debug("Request to partially update TimedElementNotification : {}", timedElementNotification);

        return timedElementNotificationRepository
            .findById(timedElementNotification.getId())
            .map(existingTimedElementNotification -> {
                if (timedElementNotification.getType() != null) {
                    existingTimedElementNotification.setType(timedElementNotification.getType());
                }
                if (timedElementNotification.getNotifyOn() != null) {
                    existingTimedElementNotification.setNotifyOn(timedElementNotification.getNotifyOn());
                }
                if (timedElementNotification.getCustomMessage() != null) {
                    existingTimedElementNotification.setCustomMessage(timedElementNotification.getCustomMessage());
                }

                return existingTimedElementNotification;
            })
            .map(timedElementNotificationRepository::save);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimedElementNotification> findAll() {
        log.debug("Request to get all TimedElementNotifications");
        return timedElementNotificationRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TimedElementNotification> findOne(Long id) {
        log.debug("Request to get TimedElementNotification : {}", id);
        return timedElementNotificationRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete TimedElementNotification : {}", id);
        timedElementNotificationRepository.deleteById(id);
    }
}
