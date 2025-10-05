package org.project.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.project.dto.request.CreatePaymentRequest;
import org.project.dto.response.PaymentResponse;
import org.project.model.Payment;

import java.util.UUID;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PaymentMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "appointmentId", source = "appointmentId")
    @Mapping(target = "paymentStatus", ignore = true)
    @Mapping(target = "transactionId", ignore = true)
    @Mapping(target = "gatewayTransactionId", ignore = true)
    @Mapping(target = "gatewayResponse", ignore = true)
    @Mapping(target = "paymentDate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Payment toEntity(CreatePaymentRequest request);

    @Mapping(target = "appointmentId", source = "appointmentId")
    PaymentResponse toResponse(Payment payment);
//
//    default Appointment mapAppointmentId(UUID appointmentId) {
//        if (appointmentId == null) {
//            return null;
//        }
//        Appointment appointment = new Appointment();
//        appointment.setId(appointmentId);
//        return appointment;
//    }

    void updatePaymentFromRequest(CreatePaymentRequest request, @MappingTarget Payment payment);
}
