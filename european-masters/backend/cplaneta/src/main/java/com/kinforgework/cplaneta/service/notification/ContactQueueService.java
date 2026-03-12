package com.kinforgework.cplaneta.service.notification;

import com.kinforgework.cplaneta.entities.ContactEntity;
import com.kinforgework.cplaneta.enums.ContactStatus;
import com.kinforgework.cplaneta.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ContactQueueService {

    private final ContactRepository contactRepository;

    public Optional<ContactEntity> nextPending() {

        List<ContactEntity> results = contactRepository.findFirstByStatus(
                ContactStatus.PENDING.name()
        );

        return results.isEmpty()
                ? Optional.empty()
                : Optional.of(results.getFirst());
    }
}