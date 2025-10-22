-- Database indexes để tối ưu hóa queries cho DoctorAvailabilityService

-- Index cho query theo doctor_id và slot_date (query chính)
CREATE INDEX IF NOT EXISTS idx_doctor_available_slots_doctor_date 
ON doctor_available_slots (doctor_user_id, slot_date);

-- Index cho query theo doctor_id, slot_date và availability
CREATE INDEX IF NOT EXISTS idx_doctor_available_slots_doctor_date_available 
ON doctor_available_slots (doctor_user_id, slot_date, is_available);

-- Index cho query theo slot_date range
CREATE INDEX IF NOT EXISTS idx_doctor_available_slots_date_range 
ON doctor_available_slots (slot_date);

-- Index cho query theo doctor_id và date range (batch query)
CREATE INDEX IF NOT EXISTS idx_doctor_available_slots_doctor_date_range 
ON doctor_available_slots (doctor_user_id, slot_date, start_time, end_time);

-- Index cho query theo availability status
CREATE INDEX IF NOT EXISTS idx_doctor_available_slots_available 
ON doctor_available_slots (is_available);

-- Composite index cho time range queries
CREATE INDEX IF NOT EXISTS idx_doctor_available_slots_time_range 
ON doctor_available_slots (doctor_user_id, slot_date, start_time, end_time, is_available);

-- Index cho sorting
CREATE INDEX IF NOT EXISTS idx_doctor_available_slots_created_at 
ON doctor_available_slots (created_at);

-- Index cho updated_at (để cache invalidation)
CREATE INDEX IF NOT EXISTS idx_doctor_available_slots_updated_at 
ON doctor_available_slots (updated_at);

-- Index cho version (optimistic locking)
CREATE INDEX IF NOT EXISTS idx_doctor_available_slots_version 
ON doctor_available_slots (version);

-- Partial index cho available slots only (giảm kích thước index)
CREATE INDEX IF NOT EXISTS idx_doctor_available_slots_available_only 
ON doctor_available_slots (doctor_user_id, slot_date, start_time, end_time) 
WHERE is_available = true;

-- Index cho full-text search trên doctor name (nếu cần)
-- CREATE INDEX IF NOT EXISTS idx_doctor_available_slots_doctor_name_fts 
-- ON doctor_available_slots USING gin(to_tsvector('english', doctor_name));

-- Statistics update để query planner có thông tin chính xác
ANALYZE doctor_available_slots;
