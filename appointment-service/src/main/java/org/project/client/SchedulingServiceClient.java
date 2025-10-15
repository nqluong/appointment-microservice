package org.project.client;

import java.util.UUID;

import org.project.dto.request.SlotReservationRequest;
import org.project.dto.response.SlotDetailsResponse;
import org.project.dto.response.SlotReservationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

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

    public SlotReservationResponse reserveSlot(SlotReservationRequest request) {
        String url = schedulingServiceUrl + "/api/internal/slots/reserve";
        log.info("Đặt slot {} cho patient {}", request.getSlotId(), request.getPatientId());

        return post(url, request, SlotReservationResponse.class);
    }

    public void releaseSlot(UUID slotId) {
        String url = schedulingServiceUrl + "/api/internal/slots/release/" + slotId;
        log.info("Giải phóng slot {}", slotId);
        try {
            post(url, null, Void.class);
        } catch (Exception e) {
            log.error("Lỗi khi giải phóng slot: {}", e.getMessage());
        }
    }

    public SlotDetailsResponse getSlotDetails(UUID slotId) {
        String url = schedulingServiceUrl + "/api/internal/slots/" + slotId;
        log.debug("Lấy thông tin slot {}", slotId);
        return get(url, SlotDetailsResponse.class);
    }
}
