package org.project.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    INTERNAL_SERVER_ERROR(9999, "Lỗi hệ thống", HttpStatus.INTERNAL_SERVER_ERROR),
    EXTERNAL_SERVICE_ERROR(9998, "Dịch vụ bên ngoài đang gặp sự cố", HttpStatus.SERVICE_UNAVAILABLE),
    SERVICE_UNAVAILABLE(9997, "Dịch vụ tạm thời không khả dụng", HttpStatus.SERVICE_UNAVAILABLE),
    COMMUNICATION_ERROR(9996, "Lỗi khi giao tiếp với dịch vụ khác", HttpStatus.SERVICE_UNAVAILABLE),
    INVALID_REQUEST(1000, "Yêu cầu không hợp lệ", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1001, "Chưa xác thực", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1002, "Không có quyền truy cập", HttpStatus.UNAUTHORIZED),
    FORBIDDEN(1003, "Truy cập bị từ chối", HttpStatus.FORBIDDEN),
    RESOURCE_NOT_FOUND(1004, "Không tìm thấy tài nguyên", HttpStatus.NOT_FOUND),
    VALIDATION_ERROR(1005, "Lỗi xác thực dữ liệu", HttpStatus.BAD_REQUEST),
    INVALID_CREDENTIALS(1006, "Tên đăng nhập hoặc mật khẩu không hợp lệ", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(1007, "Mã token đã hết hạn", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID(1008, "Mã token không hợp lệ", HttpStatus.UNAUTHORIZED),
    ACCOUNT_DISABLED(1009, "Tài khoản đã bị vô hiệu hóa", HttpStatus.UNAUTHORIZED),
    ACCOUNT_NOT_VERIFIED(1010, "Tài khoản chưa được xác minh", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED(1011, "Truy cập bị từ chối", HttpStatus.FORBIDDEN),
    TOKEN_GENERATION_FAILED(1012, "Tạo mã token thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    TOKEN_PARSE_ERROR(1013, "Lỗi xử lý mã token", HttpStatus.BAD_REQUEST),
    INVALID_ROLE(1014, "Vai trò người dùng không hợp lệ", HttpStatus.BAD_REQUEST),

    MISSING_REQUIRED_FIELD(1015, "Trường thông tin bắt buộc còn thiếu", HttpStatus.BAD_REQUEST),
    INVALID_INPUT(1016, "Dữ liệu đầu vào không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_ROLE_OPERATION(1017, "Thao tác không được phép cho vai trò này", HttpStatus.BAD_REQUEST),
    INVALID_UUID_FORMAT(1018, "Định dạng UUID không hợp lệ", HttpStatus.BAD_REQUEST),
    ROLE_ALREADY_ASSIGNED(1019, "Vai trò đã được gán cho người dùng", HttpStatus.CONFLICT),
    ROLE_NOT_FOUND(1020, "Không tìm thấy vai trò của người dùng", HttpStatus.NOT_FOUND),
    INVALID_EXPIRATION_DATE(1023, "Ngày hết hạn phải ở trong tương lai", HttpStatus.BAD_REQUEST),
    ROLE_ASSIGNMENT_FAILED(1022, "Gán vai trò cho người dùng thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    ROLE_REVOCATION_FAILED(1023, "Thu hồi vai trò từ người dùng thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    ROLE_UPDATE_FAILED(1024, "Cập nhật vai trò thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    INSUFFICIENT_PRIVILEGES(1025, "Không đủ đặc quyền để thực hiện hành động này", HttpStatus.FORBIDDEN),

    // Lỗi người dùng
    USERNAME_ALREADY_EXISTS(2001, "Tên đăng nhập đã tồn tại", HttpStatus.CONFLICT),
    EMAIL_ALREADY_EXISTS(2002, "Email đã tồn tại", HttpStatus.CONFLICT),
    USER_NOT_FOUND(2003, "Không tìm thấy người dùng", HttpStatus.NOT_FOUND),
    REGISTRATION_FAILED(2004, "Đăng ký người dùng thất bại", HttpStatus.BAD_REQUEST),
    LICENSE_NUMBER_ALREADY_EXISTS(2005, "Số giấy phép hành nghề đã tồn tại", HttpStatus.CONFLICT),
    EMAIL_NOT_VERIFIED(2007, "Email chưa được xác minh", HttpStatus.UNAUTHORIZED),
    USER_CANNOT_BE_DELETED(2008, "Người dùng này không thể bị xóa", HttpStatus.BAD_REQUEST),
    USER_NOT_DELETED(2009, "Người dùng không ở trạng thái đã xóa", HttpStatus.BAD_REQUEST),
    USER_ALREADY_DELETED(2010, "Người dùng đã bị xóa", HttpStatus.BAD_REQUEST),

    // Lỗi chuyên khoa
    SPECIALTY_NOT_FOUND(3001, "Không tìm thấy chuyên khoa", HttpStatus.NOT_FOUND),

    // Lỗi tập tin
    INVALID_FILE_FORMAT(4001, "Định dạng tệp không hợp lệ. Chỉ cho phép tệp JPG, PNG, GIF", HttpStatus.BAD_REQUEST),
    FILE_SIZE_EXCEEDED(4002, "Vượt quá kích thước tệp. Kích thước tối đa cho phép là 5MB", HttpStatus.BAD_REQUEST),
    FILE_UPLOAD_FAILED(4003, "Tải tệp lên thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_DELETE_FAILED(4004, "Xóa tệp cũ thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_FILE_CONTENT_TYPE(4005, "Loại nội dung tệp không hợp lệ", HttpStatus.BAD_REQUEST),
    FILE_NOT_PROVIDED(4006, "Tệp không được cung cấp hoặc trống", HttpStatus.BAD_REQUEST),
    PHOTO_UPLOAD_ERROR(4007, "Đã xảy ra lỗi khi tải ảnh lên", HttpStatus.INTERNAL_SERVER_ERROR),
    DIRECTORY_CREATION_FAILED(4008, "Tạo thư mục tải lên thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    UPDATE_FAILED(4009, "Cập nhật tệp thất bại", HttpStatus.INTERNAL_SERVER_ERROR),

    // Lỗi chuyên khoa & lịch làm việc
    SPECIALTY_NAME_EXISTS(5001, "Tên chuyên khoa đã tồn tại", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_PERMISSION(5002, "Người dùng không có đủ quyền", HttpStatus.BAD_REQUEST),
    SCHEDULE_ALREADY_EXISTS(5003, "Bác sĩ đã có lịch làm việc được cấu hình", HttpStatus.CONFLICT),
    SCHEDULE_NOT_FOUND(5004, "Không tìm thấy lịch làm việc của bác sĩ", HttpStatus.NOT_FOUND),
    DUPLICATE_SCHEDULE_DAY(5004, "Ngày trong tuần bị lặp lại trong lịch làm việc", HttpStatus.CONFLICT),
    INVALID_TIME_RANGE(5005, "Khoảng thời gian không hợp lệ: thời gian bắt đầu phải trước thời gian kết thúc", HttpStatus.BAD_REQUEST),
    INVALID_WORKING_HOURS(5006, "Giờ làm việc phải trong khoảng từ 6:00 sáng đến 11:00 tối", HttpStatus.BAD_REQUEST),
    INVALID_SLOT_DURATION(5007, "Thời lượng một ca khám vượt quá tổng thời gian làm việc", HttpStatus.CONFLICT),

    // Lỗi tìm kiếm
    INVALID_SEARCH_CRITERIA(6001, "Tiêu chí tìm kiếm không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_DATE_RANGE(6002, "Khoảng ngày tìm kiếm không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_EXPERIENCE_RANGE(6003, "Khoảng kinh nghiệm không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_FEE_RANGE(6004, "Khoảng phí tư vấn không hợp lệ", HttpStatus.BAD_REQUEST),
    DATABASE_ERROR(6005, "Đã xảy ra lỗi cơ sở dữ liệu trong quá trình tìm kiếm", HttpStatus.BAD_REQUEST),

    // Lỗi khung giờ khám (Slot)
    SLOT_NOT_FOUND(7001, "Không tìm thấy khung giờ khám", HttpStatus.NOT_FOUND),
    SLOT_NOT_AVAILABLE(7002, "Khung giờ khám không có sẵn", HttpStatus.NOT_FOUND),
    SLOT_ACCESS_DENIED(7003, "Từ chối truy cập vào khung giờ khám", HttpStatus.UNAUTHORIZED),
    SLOT_ALREADY_RESERVED(7004, "Khung giờ khám đã được đặt trước", HttpStatus.CONFLICT),
    SLOT_ALREADY_AVAILABLE(7005, "Khung giờ khám đã ở trạng thái sẵn có", HttpStatus.CONFLICT),
    SLOT_IN_PAST(7006, "Không thể sửa đổi khung giờ khám trong quá khứ", HttpStatus.BAD_REQUEST),
    SLOT_UPDATE_FAILED(7007, "Cập nhật trạng thái khung giờ khám thất bại", HttpStatus.BAD_REQUEST),
    INVALID_SLOT_OPERATION(7008, "Thao tác trên khung giờ khám không hợp lệ", HttpStatus.BAD_REQUEST),
    BULK_OPERATION_LIMIT_EXCEEDED(7009, "Vượt quá giới hạn thao tác hàng loạt", HttpStatus.BAD_REQUEST),
    DUPLICATE_SLOT_IDS(7010, "Tìm thấy các ID khung giờ khám trùng lặp", HttpStatus.NOT_FOUND),

    // Lỗi đặt lịch hẹn
    SLOT_ALREADY_BOOKED(8001, "Khung giờ chọn đã được bệnh nhân khác đặt", HttpStatus.CONFLICT),
    INVALID_SLOT_DOCTOR(8002, "Khung giờ đã chọn không thuộc về bác sĩ đã chọn", HttpStatus.BAD_REQUEST),
    PATIENT_NOT_FOUND(8003, "Bệnh nhân không tồn tại", HttpStatus.NOT_FOUND),
    PATIENT_INACTIVE(8004, "Tài khoản bệnh nhân không hoạt động", HttpStatus.BAD_REQUEST),
    PATIENT_NO_ROLE(8005, "Người dùng không có vai trò bệnh nhân", HttpStatus.UNAUTHORIZED),
    DOCTOR_NOT_FOUND(8006, "Bác sĩ không tồn tại", HttpStatus.NOT_FOUND),
    DOCTOR_INACTIVE(8007, "Tài khoản bác sĩ không hoạt động", HttpStatus.BAD_REQUEST),
    DOCTOR_NOT_APPROVED(8008, "Bác sĩ chưa được phê duyệt", HttpStatus.BAD_REQUEST),
    PATIENT_OVERLAPPING_APPOINTMENT(8009, "Bệnh nhân đã có một cuộc hẹn trùng vào thời gian này", HttpStatus.CONFLICT),
    PATIENT_TOO_MANY_PENDING(8010, "Bệnh nhân có quá nhiều lịch hẹn đang chờ xử lý (tối đa 3)", HttpStatus.BAD_REQUEST),
    APPOINTMENT_CREATION_FAILED(8011, "Tạo lịch hẹn thất bại", HttpStatus.BAD_REQUEST),
    CONCURRENT_BOOKING_CONFLICT(8012, "Xung đột xảy ra trong quá trình đặt lịch đồng thời", HttpStatus.CONFLICT),
    APPOINTMENT_FETCH_FAILED(8013, "Lấy danh sách lịch hẹn thất bại", HttpStatus.BAD_REQUEST),
    APPOINTMENT_NOT_PAYABLE(8014, "Lịch hẹn không ở trạng thái có thể thanh toán", HttpStatus.BAD_REQUEST),
    APPOINTMENT_NOT_FOUND(8015, "Không tìm thấy lịch hẹn", HttpStatus.NOT_FOUND),
    DOCTOR_UNAUTHORIZED(8016, "Không có quyền quản lý lịch nghỉ của bác sĩ này", HttpStatus.UNAUTHORIZED),
    APPOINTMENT_STATUS_UPDATE_FAILED(8017, "Cập nhật trạng thái lịch hẹn thất bại", HttpStatus.BAD_REQUEST),
    APPOINTMENT_COMPLETION_FAILED(8018, "Hoàn tất lịch hẹn thất bại", HttpStatus.BAD_REQUEST),
    APPOINTMENT_CANCELLATION_FAILED(8019, "Hủy lịch hẹn thất bại", HttpStatus.BAD_REQUEST),
    APPOINTMENT_CONFIRMATION_FAILED(8020, "Xác nhận lịch hẹn thất bại", HttpStatus.BAD_REQUEST),
    CONSULTATION_FEE_NOT_FOUND(8027, "Bác sĩ chưa cấu hình phí tư vấn", HttpStatus.BAD_REQUEST),

    // Lỗi kiểm tra trạng thái
    INVALID_STATUS_TRANSITION(8021, "Chuyển đổi trạng thái không hợp lệ", HttpStatus.BAD_REQUEST),
    APPOINTMENT_STATUS_ALREADY_SET(8022, "Trạng thái lịch hẹn đã được đặt thành giá trị này", HttpStatus.CONFLICT),
    APPOINTMENT_NOT_CONFIRMED(8023, "Lịch hẹn phải được xác nhận trước khi hoàn tất", HttpStatus.BAD_REQUEST),
    APPOINTMENT_ALREADY_COMPLETED(8024, "Lịch hẹn đã được hoàn tất", HttpStatus.CONFLICT),
    APPOINTMENT_ALREADY_CANCELLED(8025, "Lịch hẹn đã bị hủy", HttpStatus.CONFLICT),
    DOCTOR_NOTES_REQUIRED(8026, "Cần có ghi chú của bác sĩ để hoàn tất lịch hẹn", HttpStatus.BAD_REQUEST),


    // Lỗi thanh toán
    PAYMENT_NOT_FOUND(9001, "Không tìm thấy thông tin thanh toán", HttpStatus.NOT_FOUND),
    PAYMENT_INVALID_AMOUNT(9002, "Số tiền thanh toán phải lớn hơn 0", HttpStatus.BAD_REQUEST),
    PAYMENT_INVALID_STATUS(9003, "Chuyển đổi trạng thái thanh toán không hợp lệ", HttpStatus.BAD_REQUEST),
    PAYMENT_PROCESSING_FAILED(9004, "Xử lý thanh toán thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    PAYMENT_GATEWAY_ERROR(9005, "Lỗi cổng thanh toán", HttpStatus.BAD_GATEWAY),
    PAYMENT_INVALID_SIGNATURE(9006, "Chữ ký thanh toán không hợp lệ", HttpStatus.BAD_REQUEST),
    PAYMENT_EXPIRED(9007, "Thanh toán đã hết hạn", HttpStatus.BAD_REQUEST),
    PAYMENT_ALREADY_PROCESSED(9008, "Thanh toán đã được xử lý", HttpStatus.CONFLICT),
    PAYMENT_CANCELLED(9009, "Thanh toán đã bị hủy", HttpStatus.BAD_REQUEST),
    PAYMENT_ALREADY_EXISTS(9010, "Thanh toán cho lịch hẹn này đã tồn tại", HttpStatus.CONFLICT),
    INVALID_PAYMENT_TYPE(9011, "Loại thanh toán không hợp lệ", HttpStatus.BAD_REQUEST),
    PAYMENT_QUERY_FAILED(9012, "Truy vấn thanh toán thất bại", HttpStatus.BAD_REQUEST),
    REFUND_PROCESSING_ERROR(9013, "Lỗi xử lý hoàn tiền", HttpStatus.INTERNAL_SERVER_ERROR),
    REFUND_GATEWAY_ERROR(9014, "Lỗi cổng hoàn tiền", HttpStatus.INTERNAL_SERVER_ERROR),
    REFUND_TRANSACTION_FAILED(9015, "Giao dịch hoàn tiền thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    REFUND_NOT_FOUND(9016, "Không tìm thấy thông tin hoàn tiền", HttpStatus.NOT_FOUND),
    REFUND_ALREADY_PROCESSED(9017, "Yêu cầu hoàn tiền đã được xử lý", HttpStatus.BAD_REQUEST),
    PAYMENT_NOT_REFUNDABLE(9018, "Thanh toán này không thể hoàn lại", HttpStatus.BAD_REQUEST),
    PAYMENT_ALREADY_REFUNDED(9019, "Thanh toán đã được hoàn lại toàn bộ", HttpStatus.BAD_REQUEST),
    REFUND_PERIOD_EXPIRED(9020, "Thời gian yêu cầu hoàn tiền đã hết hạn", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_REFUND_AMOUNT(9021, "Số tiền hoàn lại vượt quá số tiền có sẵn", HttpStatus.BAD_REQUEST),
    INVALID_REFUND_AMOUNT(9022, "Số tiền hoàn lại không hợp lệ", HttpStatus.BAD_REQUEST),
    REFUND_AMOUNT_TOO_SMALL(9023, "Số tiền hoàn lại quá nhỏ", HttpStatus.BAD_REQUEST),
    REFUND_AMOUNT_TOO_LARGE(9024, "Số tiền hoàn lại quá lớn", HttpStatus.BAD_REQUEST),
    PAYMENT_ALREADY_FULLY_REFUNDED(9025, "Thanh toán đã được hoàn lại toàn bộ", HttpStatus.BAD_REQUEST),
    APPOINTMENT_DATE_PASSED(9026, "Không thể hoàn tiền cho các cuộc hẹn đã qua", HttpStatus.BAD_REQUEST),

    // Lỗi VNPay
    VNPAY_INVALID_RESPONSE(1101, "Phản hồi từ VNPay không hợp lệ", HttpStatus.BAD_REQUEST),
    VNPAY_SIGNATURE_VERIFICATION_FAILED(1102, "Xác minh chữ ký VNPay thất bại", HttpStatus.BAD_REQUEST),
    VNPAY_TRANSACTION_FAILED(1103, "Giao dịch VNPay thất bại", HttpStatus.BAD_REQUEST),

    // Lỗi lịch nghỉ
    ABSENCE_NOT_FOUND(1201, "Không tìm thấy lịch nghỉ của bác sĩ", HttpStatus.NOT_FOUND),
    ABSENCE_CONFLICT(1202, "Lịch nghỉ của bác sĩ trùng với lịch nghỉ đã có", HttpStatus.CONFLICT),
    ABSENCE_INVALID_TIME_RANGE(1203, "Khoảng thời gian nghỉ không hợp lệ", HttpStatus.BAD_REQUEST),
    ABSENCE_PAST_DATE(1204, "Không thể tạo lịch nghỉ cho ngày trong quá khứ", HttpStatus.BAD_REQUEST),
    ABSENCE_TIME_MISMATCH(1205, "Thời gian bắt đầu và kết thúc phải được cung cấp cùng nhau hoặc đều trống", HttpStatus.BAD_REQUEST),
    ABSENCE_INVALID_DURATION(1206, "Thời gian nghỉ không hợp lệ", HttpStatus.BAD_REQUEST),
    ABSENCE_APPOINTMENT_PROCESSING_ERROR(1207, "Xử lý lịch hẹn khi có lịch nghỉ thất bại", HttpStatus.BAD_REQUEST),

    VALIDATION_FAILED(1301, "Xác thực dữ liệu đầu vào thất bại", HttpStatus.BAD_REQUEST),
    REQUIRED_FIELD_MISSING(1302, "Trường thông tin bắt buộc còn thiếu", HttpStatus.BAD_REQUEST),
    INVALID_DATE_FORMAT(1303, "Định dạng ngày không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_TIME_FORMAT(1304, "Định dạng thời gian không hợp lệ", HttpStatus.BAD_REQUEST),
    FIELD_TOO_LONG(1305, "Trường thông tin vượt quá độ dài tối đa", HttpStatus.BAD_REQUEST),

    MEDICAL_RECORD_NOT_FOUND(1401, "Không tìm thấy hồ sơ y tế", HttpStatus.NOT_FOUND),
    MEDICAL_RECORD_ALREADY_EXISTS(1402, "Hồ sơ y tế đã tồn tại cho cuộc hẹn này", HttpStatus.CONFLICT),
    MEDICAL_RECORD_CREATION_FAILED(1403, "Tạo hồ sơ y tế thất bại", HttpStatus.BAD_REQUEST),
    MEDICAL_RECORD_UPDATE_FAILED(1404, "Cập nhật hồ sơ y tế thất bại", HttpStatus.BAD_REQUEST),
    MEDICAL_RECORD_FETCH_FAILED(1405, "Lấy dữ liệu hồ sơ y tế thất bại", HttpStatus.BAD_REQUEST),
    MEDICAL_RECORD_SEARCH_FAILED(1406, "Tìm kiếm hồ sơ y tế thất bại", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED_MEDICAL_RECORD_ACCESS(1407, "Truy cập hồ sơ y tế không được phép", HttpStatus.FORBIDDEN),
    UNAUTHORIZED_MEDICAL_RECORD_UPDATE(1408, "Không có quyền cập nhật hồ sơ y tế", HttpStatus.FORBIDDEN),
    UNAUTHORIZED_PATIENT_RECORD_ACCESS(1409, "Không có quyền truy cập hồ sơ bệnh nhân", HttpStatus.FORBIDDEN),
    UNAUTHORIZED_DOCTOR_RECORD_ACCESS(1410, "Không có quyền truy cập hồ sơ bác sĩ", HttpStatus.FORBIDDEN),
    INVALID_APPOINTMENT_STATUS_FOR_MEDICAL_RECORD(1411, "Trạng thái cuộc hẹn không hợp lệ để tạo hồ sơ y tế", HttpStatus.BAD_REQUEST),
    DOCTOR_PERMISSION_REQUIRED(1412, "Yêu cầu quyền bác sĩ", HttpStatus.FORBIDDEN),
    ADMIN_PERMISSION_REQUIRED(1413, "Yêu cầu quyền quản trị viên", HttpStatus.FORBIDDEN),
    APPOINTMENT_MUST_BE_IN_PROGRESS_OR_COMPLETED(1414, "Cuộc hẹn phải đang diễn ra hoặc đã hoàn thành mới được tạo hồ sơ y tế", HttpStatus.BAD_REQUEST),
    MEDICAL_RECORD_CANNOT_BE_DELETED(1415, "Hồ sơ y tế không thể bị xóa", HttpStatus.BAD_REQUEST),
    INVALID_MEDICAL_RECORD_DATA(1416, "Dữ liệu hồ sơ y tế không hợp lệ", HttpStatus.BAD_REQUEST),
    EXAMINATION_START_FAILED(1417, "Không thể bắt đầu khám bệnh", HttpStatus.BAD_REQUEST),
    APPOINTMENT_NOT_IN_PROGRESS(1418, "Cuộc hẹn không ở trạng thái đang khám", HttpStatus.BAD_REQUEST),
    APPOINTMENT_COMPLETION_WITH_MEDICAL_RECORD_FAILED(1419, "Không thể hoàn thành cuộc hẹn với hồ sơ bệnh án", HttpStatus.BAD_REQUEST),
    APPOINTMENT_NOT_COMPLETED(1420, "Cuộc hẹn chưa được hoàn thành", HttpStatus.BAD_REQUEST),

    // Lỗi Saga
    SAGA_NOT_FOUND(1501, "Không tìm thấy trạng thái saga", HttpStatus.NOT_FOUND),
    SAGA_STATE_UPDATE_FAILED(1502, "Cập nhật trạng thái saga thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    SAGA_COMPENSATION_FAILED(1503, "Bù trừ saga thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    SAGA_ALREADY_COMPLETED(1504, "Saga đã được hoàn thành", HttpStatus.CONFLICT),
    SAGA_ALREADY_COMPENSATED(1505, "Saga đã được bù trừ", HttpStatus.CONFLICT),

    ;


    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}