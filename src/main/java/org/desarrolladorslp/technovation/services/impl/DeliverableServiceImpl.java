package org.desarrolladorslp.technovation.services.impl;

import org.desarrolladorslp.technovation.Enum.RelationType;
import org.desarrolladorslp.technovation.dto.DeliverableDTO;
import org.desarrolladorslp.technovation.exception.SessionDoesNotBelongToBatch;
import org.desarrolladorslp.technovation.models.Batch;
import org.desarrolladorslp.technovation.models.Deliverable;
import org.desarrolladorslp.technovation.repository.DeliverableRepository;
import org.desarrolladorslp.technovation.services.DeliverableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DeliverableServiceImpl implements DeliverableService {

    private DeliverableRepository deliverableRepository;

    private static final Logger logger = LoggerFactory.getLogger(DeliverableServiceImpl.class);

    @Override
    @Transactional
    public DeliverableDTO save(DeliverableDTO deliverableDTO) {
        deliverableDTO.setId(UUID.randomUUID());
        Deliverable deliverable = convertToEntity(deliverableDTO);
        return convertToDTO(deliverableRepository.save(deliverable));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliverableDTO> findByBatch(UUID batchId) {
        Batch batch = new Batch();

        batch.setId(batchId);
        List<Deliverable> deliverables = deliverableRepository.findByBatch(batch);

        return deliverables.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DeliverableDTO update(DeliverableDTO deliverableDTO, UUID deliverableId) {
        deliverableDTO.setId(deliverableId);
        Deliverable deliverable = convertToEntity(deliverableDTO);
        return convertToDTO(deliverableRepository.save(deliverable));
    }

    @Override
    @Transactional
    public void delete(UUID deliverableId) {

        try {
            Optional<Deliverable> deliverableOptional = deliverableRepository.findById(deliverableId);
            deliverableOptional.ifPresent(
                    deliverable -> deliverableRepository.delete(deliverable)
            );
        } catch (Exception exception) {

            logger.warn("Error trying to delete a deliverable with id {}", deliverableId, exception);
        }
    }

    @Override
    @Transactional
    public void assignDeliverableToSession(UUID deliverableId, UUID sessionId, RelationType type) {

        if (deliverableRepository.assignDeliverableToSession(deliverableId, sessionId, type.name()) == 0)
            throw new SessionDoesNotBelongToBatch(sessionId + "does not belong to the same batch as deliverable");
    }

    @Autowired
    public void setDeliverableRepository(DeliverableRepository deliverableRepository) {
        this.deliverableRepository = deliverableRepository;
    }

    private Deliverable convertToEntity(DeliverableDTO deliverableDTO) {
        return Deliverable.builder()
                .id(deliverableDTO.getId())
                .dueDate(ZonedDateTime.parse(deliverableDTO.getDueDate(), DateTimeFormatter.ISO_DATE_TIME))
                .title(deliverableDTO.getTitle())
                .description(deliverableDTO.getDescription())
                .batch(Batch.builder().id(deliverableDTO.getBatchId()).build())
                .build();
    }

    private DeliverableDTO convertToDTO(Deliverable deliverable) {
        return DeliverableDTO.builder()
                .id(deliverable.getId())
                .dueDate(deliverable.getDueDate().format(DateTimeFormatter.ISO_DATE_TIME))
                .title(deliverable.getTitle())
                .description(deliverable.getDescription())
                .batchId(deliverable.getBatch().getId())
                .build();
    }


}
