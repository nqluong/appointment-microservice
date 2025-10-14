package org.project.client;

import lombok.extern.slf4j.Slf4j;
import org.project.dto.request.ConfirmReservationRequest;
import org.project.dto.request.SlotReleaseRequest;
import org.project.dto.request.SlotReservationRequest;
import org.project.dto.response.SlotDetailsResponse;
import org.project.dto.response.SlotReservationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Slf4j
@Component
public class SchedulingServiceClient extends BaseServiceClient{
    @Value("${services.scheduling.url}")
    private String schedulingServiceUrl;

    public SchedulingServiceClient(RestTemplate restTemplate) {
        super(restTemplate);
    }

    @Override
    protected String getServiceName() {
        return "Scheduling Service";
    }

    /**
     * Reserve slot với idempotency
     */
    public SlotReservationResponse reserveSlot(SlotReservationRequest request) {
        String url = schedulingServiceUrl + "/api/internal/slots/reserve";
        log.info("Reserving slot {} with idempotency key {}",
                request.getSlotId(), request.getIdempotencyKey());

        return post(url, request, SlotReservationResponse.class);
    }

    /**
     * Release slot (compensation)
     */
    public void releaseSlot(SlotReleaseRequest request) {
        String url = schedulingServiceUrl + "/api/internal/slots/release";
        log.info("Releasing slot {} with idempotency key {}",
                request.getSlotId(), request.getIdempotencyKey());

        try {
            post(url, request, Void.class);
        } catch (Exception e) {
            // Log error nhưng không throw để không block compensation
            log.error("Failed to release slot during compensation: {}", e.getMessage());
        }
    }

    /**
     * Get slot details
     */
    public SlotDetailsResponse getSlotDetails(UUID slotId) {
        String url = schedulingServiceUrl + "/api/internal/slots/" + slotId;
        log.debug("Getting slot details for slot {}", slotId);

        return get(url, SlotDetailsResponse.class);
    }

    /**
     * Confirm reservation (sau khi appointment created)
     */
    public void confirmReservation(UUID slotId, String idempotencyKey) {
        String url = schedulingServiceUrl + "/api/internal/slots/confirm";

        ConfirmReservationRequest request = ConfirmReservationRequest.builder()
                .slotId(slotId)
                .idempotencyKey(idempotencyKey)
                .build();

        try {
            post(url, request, Void.class);
            log.info("Confirmed reservation for slot {}", slotId);
        } catch (Exception e) {
            log.error("Failed to confirm reservation: {}", e.getMessage());
        }
    }
}
