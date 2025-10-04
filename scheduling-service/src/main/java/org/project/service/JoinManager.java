//package org.project.service;
//
//import jakarta.persistence.criteria.Join;
//import jakarta.persistence.criteria.JoinType;
//import jakarta.persistence.criteria.Root;
//import org.project.appointment_project.schedule.model.DoctorSchedule;
//import org.project.appointment_project.user.model.MedicalProfile;
//import org.project.appointment_project.user.model.Specialty;
//import org.project.appointment_project.user.model.User;
//import org.project.appointment_project.user.model.UserProfile;
//
//public class JoinManager {
//    private final Root<User> root;
//    private Join<User, MedicalProfile> medicalJoin;
//    private Join<User, UserProfile> userProfileJoin;
//    private Join<MedicalProfile, Specialty> specialtyJoin;
//    private Join<User, DoctorSchedule> scheduleJoin;
//
//    public JoinManager(Root<User> root) {
//        this.root = root;
//    }
//
//    public Join<User, MedicalProfile> getMedicalJoin() {
//        if (medicalJoin == null) {
//            medicalJoin = root.join("medicalProfile", JoinType.LEFT);
//        }
//        return medicalJoin;
//    }
//
//    public Join<User, UserProfile> getUserProfileJoin() {
//        if (userProfileJoin == null) {
//            userProfileJoin = root.join("userProfile", JoinType.LEFT);
//        }
//        return userProfileJoin;
//    }
//
//    public Join<MedicalProfile, Specialty> getSpecialtyJoin() {
//        if (specialtyJoin == null) {
//            specialtyJoin = getMedicalJoin().join("specialty", JoinType.LEFT);
//        }
//        return specialtyJoin;
//    }
//
//    public Join<User, DoctorSchedule> getScheduleJoin() {
//        if (scheduleJoin == null) {
//            scheduleJoin = root.join("doctorSchedules", JoinType.INNER);
//        }
//        return scheduleJoin;
//    }
//}
