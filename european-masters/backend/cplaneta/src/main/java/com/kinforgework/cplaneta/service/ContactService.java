package com.kinforgework.cplaneta.service;


import com.kinforgework.cplaneta.dto.ContactRequest;
import com.kinforgework.cplaneta.dto.ContactResponse;
import com.kinforgework.cplaneta.entities.ContactEntity;
import com.kinforgework.cplaneta.enums.ContactStatus;
import com.kinforgework.cplaneta.entities.MasterProgramEntity;
import com.kinforgework.cplaneta.exception.ResourceAlreadyExistsException;
import com.kinforgework.cplaneta.exception.ResourceNotFoundException;
import com.kinforgework.cplaneta.mapper.ContactMapper;
import com.kinforgework.cplaneta.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactService {

    private final ContactRepository contactRepository;
    private final ContactMapper contactMapper;
    private final MasterProgramService masterProgramService;

    @Transactional(readOnly = true)
    public List<ContactResponse> findAll() {
        return contactRepository.findAll()
                .stream()
                .map(contactMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ContactResponse findById(Long id) {
        return contactMapper.toResponse(getContactOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<ContactResponse> findByProgram(Long programId) {
        return contactRepository.findByMasterProgramId(programId)
                .stream()
                .map(contactMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public long countPending() {
        return contactRepository.countByStatus(ContactStatus.PENDING);
    }

    @Transactional
    public ContactResponse create(ContactRequest request) {
        MasterProgramEntity program = masterProgramService.getProgramOrThrow(request.masterProgramId());

        if (contactRepository.existsByEmailAndMasterProgramId(
                request.email(),
                request.masterProgramId()
        )
        ) {
            throw new ResourceAlreadyExistsException(
                    "Contact with email '" + request.email() +
                            "' already registered for program id=" + request.masterProgramId());
        }

        ContactEntity contactEntity = contactMapper.toEntity(request);
        contactEntity.setMasterProgram(program);
        ContactEntity saved = contactRepository.save(contactEntity);

        log.info(
                "Contact created: id={} email='{}' program='{}'",
                saved.getId(), saved.getEmail(), program.getName()
        );
        return contactMapper.toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        ContactEntity contactEntity = getContactOrThrow(id);
        contactRepository.delete(contactEntity);
        log.info("Contact deleted: id={}", id);
    }

    // -------------------------------------------------------------------------
    // Package-private helper
    // -------------------------------------------------------------------------

    private ContactEntity getContactOrThrow(Long id) {
        return contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found: " + id));
    }
}
