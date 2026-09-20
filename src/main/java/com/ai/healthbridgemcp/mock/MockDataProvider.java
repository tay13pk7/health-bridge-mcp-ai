package com.ai.healthbridgemcp.mock;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Provides realistic mock data for all EHR vendors across two tenants each.
 *
 * Athena    : ACME-DEMO (healthy) | RIVERSIDE-HEALTH (degraded)
 * ModMed    : DALLAS-ORTHO (healthy) | PLANO-MEDICAL (degraded)
 * Healthjump: BAYVIEW-CLINIC (healthy) | LAKESIDE-HEALTH (degraded)
 * Allscripts: METRO-CARE (healthy) | SUMMIT-HEALTH (degraded)
 *
 * No real PHI — all data is fabricated for demo purposes.
 */
public final class MockDataProvider {

    private MockDataProvider() {}

    // ─────────────────────────── HELPERS ───────────────────────────

    /** Returns true when the tenant is a "degraded / problem" tenant for demo purposes. */
    private static boolean isDegraded(String tenantId) {
        if (tenantId == null) return false;
        return switch (tenantId.toUpperCase()) {
            case "RIVERSIDE-HEALTH", "PLANO-MEDICAL", "LAKESIDE-HEALTH", "SUMMIT-HEALTH" -> true;
            default -> false;
        };
    }

    // ═══════════════════════════ ATHENA ═══════════════════════════
    // Tenants: ACME-DEMO (healthy) | RIVERSIDE-HEALTH (degraded)

    public static Map<String, Object> athenaConnectionTest(String tenantId) {
        if (isDegraded(tenantId)) {
            return Map.of(
                "ehr", "ATHENA",
                "tenantId", tenantId,
                "status", "DEGRADED",
                "apiEndpoint", "https://api.athenahealth.com/v1",
                "latencyMs", 4800,
                "tokenValid", false,
                "lastSyncTime", LocalDateTime.now().minusHours(14).toString(),
                "message", "WARNING: OAuth token expired 14 hours ago. Sync is stalled. Immediate credential rotation required."
            );
        }
        return Map.of(
            "ehr", "ATHENA",
            "tenantId", tenantId,
            "status", "CONNECTED",
            "apiEndpoint", "https://api.athenahealth.com/v1",
            "latencyMs", 42,
            "tokenValid", true,
            "lastSyncTime", LocalDateTime.now().minusMinutes(3).toString(),
            "message", "Athena API connection is healthy"
        );
    }

    public static List<Map<String, Object>> athenaProviders(String tenantId) {
        if (isDegraded(tenantId)) {
            // RIVERSIDE-HEALTH: smaller practice, several inactive/on-leave providers
            return List.of(
                Map.of("providerId", "ATH-R001", "firstName", "Gregory", "lastName", "Hawkins",
                        "npi", "1122334455", "specialty", "Family Medicine",
                        "department", "Primary Care", "status", "ACTIVE", "tenantId", tenantId),
                Map.of("providerId", "ATH-R002", "firstName", "Natalie", "lastName", "Brooks",
                        "npi", "2233445566", "specialty", "Internal Medicine",
                        "department", "Primary Care", "status", "ON_LEAVE", "tenantId", tenantId),
                Map.of("providerId", "ATH-R003", "firstName", "Victor", "lastName", "Okafor",
                        "npi", "3344556677", "specialty", "Cardiology",
                        "department", "Cardiology", "status", "INACTIVE", "tenantId", tenantId),
                Map.of("providerId", "ATH-R004", "firstName", "Sandra", "lastName", "Patel",
                        "npi", "4455667788", "specialty", "Pediatrics",
                        "department", "Pediatrics", "status", "ACTIVE", "tenantId", tenantId),
                Map.of("providerId", "ATH-R005", "firstName", "Thomas", "lastName", "Nguyen",
                        "npi", "5566778899", "specialty", "Orthopedics",
                        "department", "Surgery", "status", "INACTIVE", "tenantId", tenantId),
                Map.of("providerId", "ATH-R006", "firstName", "Carla", "lastName", "Mendes",
                        "npi", "6677889900", "specialty", "Dermatology",
                        "department", "Dermatology", "status", "ON_LEAVE", "tenantId", tenantId)
            );
        }
        // ACME-DEMO: large active practice
        return List.of(
            Map.of("providerId", "ATH-P001", "firstName", "Sarah", "lastName", "Mitchell",
                    "npi", "1234567890", "specialty", "Family Medicine",
                    "department", "Primary Care", "status", "ACTIVE", "tenantId", tenantId),
            Map.of("providerId", "ATH-P002", "firstName", "James", "lastName", "Rodriguez",
                    "npi", "2345678901", "specialty", "Cardiology",
                    "department", "Cardiology", "status", "ACTIVE", "tenantId", tenantId),
            Map.of("providerId", "ATH-P003", "firstName", "Priya", "lastName", "Sharma",
                    "npi", "3456789012", "specialty", "Pediatrics",
                    "department", "Pediatrics", "status", "ACTIVE", "tenantId", tenantId),
            Map.of("providerId", "ATH-P004", "firstName", "Michael", "lastName", "Chen",
                    "npi", "4567890123", "specialty", "Internal Medicine",
                    "department", "Primary Care", "status", "ACTIVE", "tenantId", tenantId),
            Map.of("providerId", "ATH-P005", "firstName", "Emily", "lastName", "Johnson",
                    "npi", "5678901234", "specialty", "Dermatology",
                    "department", "Dermatology", "status", "ACTIVE", "tenantId", tenantId),
            Map.of("providerId", "ATH-P006", "firstName", "Daniel", "lastName", "Wright",
                    "npi", "6789012340", "specialty", "Neurology",
                    "department", "Neurology", "status", "ACTIVE", "tenantId", tenantId),
            Map.of("providerId", "ATH-P007", "firstName", "Aisha", "lastName", "Okonkwo",
                    "npi", "7890123401", "specialty", "OB/GYN",
                    "department", "Women's Health", "status", "ACTIVE", "tenantId", tenantId),
            Map.of("providerId", "ATH-P008", "firstName", "Louis", "lastName", "Fernandez",
                    "npi", "8901234012", "specialty", "Psychiatry",
                    "department", "Behavioral Health", "status", "INACTIVE", "tenantId", tenantId)
        );
    }

    public static List<Map<String, Object>> athenaAppointmentTypes(String tenantId) {
        List<Map<String, Object>> base = new ArrayList<>(List.of(
            Map.of("typeId", "AT-01", "name", "New Patient Visit", "duration", 30, "category", "OFFICE_VISIT"),
            Map.of("typeId", "AT-02", "name", "Follow-Up Visit", "duration", 15, "category", "OFFICE_VISIT"),
            Map.of("typeId", "AT-03", "name", "Annual Physical", "duration", 45, "category", "PREVENTIVE"),
            Map.of("typeId", "AT-04", "name", "Telehealth Consult", "duration", 20, "category", "TELEHEALTH"),
            Map.of("typeId", "AT-05", "name", "Urgent Visit", "duration", 20, "category", "URGENT"),
            Map.of("typeId", "AT-06", "name", "Lab Work", "duration", 10, "category", "LAB"),
            Map.of("typeId", "AT-07", "name", "Vaccination", "duration", 15, "category", "IMMUNIZATION"),
            Map.of("typeId", "AT-08", "name", "Procedure", "duration", 60, "category", "PROCEDURE")
        ));
        if (!isDegraded(tenantId)) {
            base.add(Map.of("typeId", "AT-09", "name", "Wellness Check", "duration", 30, "category", "PREVENTIVE"));
            base.add(Map.of("typeId", "AT-10", "name", "Chronic Disease Management", "duration", 25, "category", "OFFICE_VISIT"));
        }
        return base;
    }

    public static List<Map<String, Object>> athenaDepartments(String tenantId) {
        if (isDegraded(tenantId)) {
            return List.of(
                Map.of("departmentId", "RH-DEP-01", "name", "Primary Care",
                        "address", "55 Riverside Blvd, Suite 100",
                        "city", "Sacramento", "state", "CA", "phone", "916-555-0201", "status", "ACTIVE"),
                Map.of("departmentId", "RH-DEP-02", "name", "Cardiology",
                        "address", "55 Riverside Blvd, Suite 200",
                        "city", "Sacramento", "state", "CA", "phone", "916-555-0202", "status", "INACTIVE"),
                Map.of("departmentId", "RH-DEP-03", "name", "Pediatrics",
                        "address", "80 Valley Oak Dr",
                        "city", "Folsom", "state", "CA", "phone", "916-555-0203", "status", "ACTIVE"),
                Map.of("departmentId", "RH-DEP-04", "name", "Surgery",
                        "address", "200 Medical Center Way",
                        "city", "Sacramento", "state", "CA", "phone", "916-555-0204", "status", "INACTIVE")
            );
        }
        return List.of(
            Map.of("departmentId", "DEP-101", "name", "Primary Care",
                    "address", "100 Medical Plaza, Suite 200",
                    "city", "Austin", "state", "TX", "phone", "512-555-0101", "status", "ACTIVE"),
            Map.of("departmentId", "DEP-102", "name", "Cardiology",
                    "address", "100 Medical Plaza, Suite 300",
                    "city", "Austin", "state", "TX", "phone", "512-555-0102", "status", "ACTIVE"),
            Map.of("departmentId", "DEP-103", "name", "Pediatrics",
                    "address", "200 Health Blvd",
                    "city", "Round Rock", "state", "TX", "phone", "512-555-0103", "status", "ACTIVE"),
            Map.of("departmentId", "DEP-104", "name", "Dermatology",
                    "address", "300 Wellness Way",
                    "city", "Austin", "state", "TX", "phone", "512-555-0104", "status", "ACTIVE"),
            Map.of("departmentId", "DEP-105", "name", "Neurology",
                    "address", "400 Brain & Spine Center",
                    "city", "Austin", "state", "TX", "phone", "512-555-0105", "status", "ACTIVE"),
            Map.of("departmentId", "DEP-106", "name", "Women's Health",
                    "address", "500 Wellness Way",
                    "city", "Austin", "state", "TX", "phone", "512-555-0106", "status", "ACTIVE")
        );
    }

    public static List<Map<String, Object>> athenaPatients(String tenantId, String searchName) {
        List<Map<String, Object>> all = isDegraded(tenantId)
            ? List.of(
                Map.of("patientId", "RH-PAT-001", "firstName", "Linda", "lastName", "Hoffman",
                        "dateOfBirth", "[REDACTED]", "gender", "Female", "ssn", "[REDACTED]",
                        "insurancePlan", "Medi-Cal", "primaryProvider", "Dr. Gregory Hawkins",
                        "lastVisit", LocalDate.now().minusDays(60).toString(), "status", "ACTIVE"),
                Map.of("patientId", "RH-PAT-002", "firstName", "Carlos", "lastName", "Williams",
                        "dateOfBirth", "[REDACTED]", "gender", "Male", "ssn", "[REDACTED]",
                        "insurancePlan", "Medicare", "primaryProvider", "Dr. Sandra Patel",
                        "lastVisit", LocalDate.now().minusDays(120).toString(), "status", "INACTIVE"),
                Map.of("patientId", "RH-PAT-003", "firstName", "Judy", "lastName", "Thompson",
                        "dateOfBirth", "[REDACTED]", "gender", "Female", "ssn", "[REDACTED]",
                        "insurancePlan", "Covered CA", "primaryProvider", "Dr. Gregory Hawkins",
                        "lastVisit", LocalDate.now().minusDays(8).toString(), "status", "ACTIVE")
              )
            : List.of(
                Map.of("patientId", "PAT-1001", "firstName", "John", "lastName", "Williams",
                        "dateOfBirth", "[REDACTED]", "gender", "Male", "ssn", "[REDACTED]",
                        "insurancePlan", "Blue Cross PPO", "primaryProvider", "Dr. Sarah Mitchell",
                        "lastVisit", LocalDate.now().minusDays(14).toString(), "status", "ACTIVE"),
                Map.of("patientId", "PAT-1002", "firstName", "Maria", "lastName", "Garcia",
                        "dateOfBirth", "[REDACTED]", "gender", "Female", "ssn", "[REDACTED]",
                        "insurancePlan", "Aetna HMO", "primaryProvider", "Dr. James Rodriguez",
                        "lastVisit", LocalDate.now().minusDays(7).toString(), "status", "ACTIVE"),
                Map.of("patientId", "PAT-1003", "firstName", "Robert", "lastName", "Thompson",
                        "dateOfBirth", "[REDACTED]", "gender", "Male", "ssn", "[REDACTED]",
                        "insurancePlan", "UnitedHealth", "primaryProvider", "Dr. Michael Chen",
                        "lastVisit", LocalDate.now().minusDays(30).toString(), "status", "ACTIVE"),
                Map.of("patientId", "PAT-1004", "firstName", "Susan", "lastName", "Williams",
                        "dateOfBirth", "[REDACTED]", "gender", "Female", "ssn", "[REDACTED]",
                        "insurancePlan", "Cigna PPO", "primaryProvider", "Dr. Priya Sharma",
                        "lastVisit", LocalDate.now().minusDays(3).toString(), "status", "ACTIVE"),
                Map.of("patientId", "PAT-1005", "firstName", "David", "lastName", "Martinez",
                        "dateOfBirth", "[REDACTED]", "gender", "Male", "ssn", "[REDACTED]",
                        "insurancePlan", "Blue Shield HMO", "primaryProvider", "Dr. Daniel Wright",
                        "lastVisit", LocalDate.now().minusDays(21).toString(), "status", "ACTIVE")
              );

        if (searchName == null || searchName.isBlank()) return all;
        String search = searchName.toLowerCase();
        return all.stream()
                .filter(p -> p.get("firstName").toString().toLowerCase().contains(search)
                        || p.get("lastName").toString().toLowerCase().contains(search))
                .toList();
    }

    // ═══════════════════════════ MODMED ═══════════════════════════
    // Tenants: DALLAS-ORTHO (healthy) | PLANO-MEDICAL (degraded)

    public static Map<String, Object> modmedConnectionTest(String tenantId) {
        if (isDegraded(tenantId)) {
            return Map.of(
                "ehr", "MODMED",
                "tenantId", tenantId,
                "status", "UNSTABLE",
                "apiEndpoint", "https://api.modmed.com/fhir/r4",
                "latencyMs", 7200,
                "tokenValid", true,
                "lastSyncTime", LocalDateTime.now().minusHours(9).toString(),
                "message", "WARNING: API response latency is critically high (7.2s). Possible ModMed service degradation or network issue."
            );
        }
        return Map.of(
            "ehr", "MODMED",
            "tenantId", tenantId,
            "status", "CONNECTED",
            "apiEndpoint", "https://api.modmed.com/fhir/r4",
            "latencyMs", 67,
            "tokenValid", true,
            "lastSyncTime", LocalDateTime.now().minusMinutes(8).toString(),
            "message", "ModMed API connection is healthy"
        );
    }

    public static List<Map<String, Object>> modmedProviders(String tenantId) {
        if (isDegraded(tenantId)) {
            // PLANO-MEDICAL: high turnover, several gaps
            return List.of(
                Map.of("providerId", "PM-P001", "firstName", "Alan", "lastName", "Russo",
                        "npi", "1029384756", "specialty", "Rheumatology",
                        "location", "Plano Main", "status", "ACTIVE"),
                Map.of("providerId", "PM-P002", "firstName", "Bridget", "lastName", "Yuen",
                        "npi", "2039485761", "specialty", "Endocrinology",
                        "location", "Plano Main", "status", "INACTIVE"),
                Map.of("providerId", "PM-P003", "firstName", "Charles", "lastName", "Nwosu",
                        "npi", "3049586772", "specialty", "Neurology",
                        "location", "Frisco Branch", "status", "ON_LEAVE"),
                Map.of("providerId", "PM-P004", "firstName", "Diana", "lastName", "Kowalski",
                        "npi", "4059687783", "specialty", "Cardiology",
                        "location", "Frisco Branch", "status", "ACTIVE")
            );
        }
        // DALLAS-ORTHO: thriving specialty clinic
        return List.of(
            Map.of("providerId", "DO-P001", "firstName", "David", "lastName", "Park",
                    "npi", "6789012345", "specialty", "Orthopedics",
                    "location", "Dallas Main Clinic", "status", "ACTIVE"),
            Map.of("providerId", "DO-P002", "firstName", "Angela", "lastName", "Foster",
                    "npi", "7890123456", "specialty", "Sports Medicine",
                    "location", "Dallas Main Clinic", "status", "ACTIVE"),
            Map.of("providerId", "DO-P003", "firstName", "Robert", "lastName", "Kim",
                    "npi", "8901234567", "specialty", "Pain Management",
                    "location", "Uptown Branch", "status", "ACTIVE"),
            Map.of("providerId", "DO-P004", "firstName", "Lisa", "lastName", "Nguyen",
                    "npi", "9012345678", "specialty", "Physical Therapy",
                    "location", "Uptown Branch", "status", "ACTIVE"),
            Map.of("providerId", "DO-P005", "firstName", "Marcus", "lastName", "Webb",
                    "npi", "9123456789", "specialty", "Spine Surgery",
                    "location", "Dallas Main Clinic", "status", "ACTIVE"),
            Map.of("providerId", "DO-P006", "firstName", "Helen", "lastName", "Torres",
                    "npi", "9234567890", "specialty", "Hand & Wrist",
                    "location", "Uptown Branch", "status", "ACTIVE")
        );
    }

    public static List<Map<String, Object>> modmedAppointments(String tenantId, String date) {
        String apptDate = (date != null && !date.isBlank()) ? date : LocalDate.now().toString();

        if (isDegraded(tenantId)) {
            // PLANO-MEDICAL: heavy no-shows, cancellations
            return List.of(
                Map.of("appointmentId", "PM-A001", "patientName", "Bruce Larson",
                        "provider", "Dr. Alan Russo", "date", apptDate,
                        "time", "09:00 AM", "type", "Follow-Up", "status", "NO_SHOW",
                        "reason", "Rheumatoid arthritis check"),
                Map.of("appointmentId", "PM-A002", "patientName", "Cynthia Reed",
                        "provider", "Dr. Diana Kowalski", "date", apptDate,
                        "time", "09:30 AM", "type", "New Patient", "status", "CANCELLED",
                        "reason", "Palpitations evaluation"),
                Map.of("appointmentId", "PM-A003", "patientName", "Edward Nash",
                        "provider", "Dr. Alan Russo", "date", apptDate,
                        "time", "10:00 AM", "type", "Consultation", "status", "NO_SHOW",
                        "reason", "Lupus management"),
                Map.of("appointmentId", "PM-A004", "patientName", "OPEN SLOT",
                        "provider", "Dr. Diana Kowalski", "date", apptDate,
                        "time", "11:00 AM", "type", "-", "status", "AVAILABLE",
                        "reason", ""),
                Map.of("appointmentId", "PM-A005", "patientName", "Frances Bloom",
                        "provider", "Dr. Alan Russo", "date", apptDate,
                        "time", "01:00 PM", "type", "Follow-Up", "status", "CONFIRMED",
                        "reason", "Gout treatment review"),
                Map.of("appointmentId", "PM-A006", "patientName", "OPEN SLOT",
                        "provider", "Dr. Diana Kowalski", "date", apptDate,
                        "time", "02:00 PM", "type", "-", "status", "AVAILABLE",
                        "reason", "")
            );
        }
        // DALLAS-ORTHO: busy, mostly confirmed
        return List.of(
            Map.of("appointmentId", "DO-A001", "patientName", "Alice Cooper",
                    "provider", "Dr. David Park", "date", apptDate,
                    "time", "08:00 AM", "type", "Follow-Up", "status", "CHECKED_IN",
                    "reason", "Post-surgery knee check"),
            Map.of("appointmentId", "DO-A002", "patientName", "Tom Bradley",
                    "provider", "Dr. Angela Foster", "date", apptDate,
                    "time", "08:30 AM", "type", "New Patient", "status", "CONFIRMED",
                    "reason", "ACL tear evaluation"),
            Map.of("appointmentId", "DO-A003", "patientName", "Susan Lee",
                    "provider", "Dr. Robert Kim", "date", apptDate,
                    "time", "09:00 AM", "type", "Consultation", "status", "CONFIRMED",
                    "reason", "Chronic lower back pain"),
            Map.of("appointmentId", "DO-A004", "patientName", "James Wilson",
                    "provider", "Dr. Marcus Webb", "date", apptDate,
                    "time", "10:00 AM", "type", "Procedure", "status", "CONFIRMED",
                    "reason", "L4-L5 disc herniation"),
            Map.of("appointmentId", "DO-A005", "patientName", "Karen Martinez",
                    "provider", "Dr. Lisa Nguyen", "date", apptDate,
                    "time", "11:00 AM", "type", "PT Session", "status", "CHECKED_IN",
                    "reason", "Post-op rehabilitation"),
            Map.of("appointmentId", "DO-A006", "patientName", "Daniel Brown",
                    "provider", "Dr. Helen Torres", "date", apptDate,
                    "time", "01:00 PM", "type", "New Patient", "status", "CONFIRMED",
                    "reason", "Carpal tunnel syndrome"),
            Map.of("appointmentId", "DO-A007", "patientName", "Rachel Adams",
                    "provider", "Dr. David Park", "date", apptDate,
                    "time", "02:00 PM", "type", "Follow-Up", "status", "SCHEDULED",
                    "reason", "Hip replacement 6-week check"),
            Map.of("appointmentId", "DO-A008", "patientName", "OPEN SLOT",
                    "provider", "Dr. Angela Foster", "date", apptDate,
                    "time", "03:30 PM", "type", "-", "status", "AVAILABLE",
                    "reason", "")
        );
    }

    public static List<Map<String, Object>> modmedLocations(String tenantId) {
        if (isDegraded(tenantId)) {
            return List.of(
                Map.of("locationId", "PM-LOC-01", "name", "Plano Main",
                        "address", "900 Independence Pkwy", "city", "Plano", "state", "TX",
                        "phone", "469-555-0101", "providers", 2, "status", "ACTIVE"),
                Map.of("locationId", "PM-LOC-02", "name", "Frisco Branch",
                        "address", "3000 Preston Rd", "city", "Frisco", "state", "TX",
                        "phone", "469-555-0102", "providers", 1, "status", "ACTIVE"),
                Map.of("locationId", "PM-LOC-03", "name", "McKinney Satellite",
                        "address", "500 N. Tennessee St", "city", "McKinney", "state", "TX",
                        "phone", "469-555-0103", "providers", 0, "status", "INACTIVE")
            );
        }
        return List.of(
            Map.of("locationId", "DO-LOC-01", "name", "Dallas Main Clinic",
                    "address", "500 Healthcare Dr", "city", "Dallas", "state", "TX",
                    "phone", "214-555-0200", "providers", 3, "status", "ACTIVE"),
            Map.of("locationId", "DO-LOC-02", "name", "Uptown Branch",
                    "address", "2500 McKinney Ave", "city", "Dallas", "state", "TX",
                    "phone", "214-555-0300", "providers", 3, "status", "ACTIVE"),
            Map.of("locationId", "DO-LOC-03", "name", "Frisco Expansion",
                    "address", "8000 Warren Pkwy", "city", "Frisco", "state", "TX",
                    "phone", "972-555-0400", "providers", 0, "status", "COMING_SOON")
        );
    }

    // ═══════════════════════════ NEXTGEN ═══════════════════════════
    // Tenants: BAYVIEW-CLINIC (healthy) | LAKESIDE-HEALTH (degraded)

    public static Map<String, Object> healthjumpConnectionTest(String tenantId) {
        if (isDegraded(tenantId)) {
            return Map.of(
                "ehr", "NEXTGEN",
                "tenantId", tenantId,
                "status", "DISCONNECTED",
                "apiEndpoint", "https://api.nextgen.com/v2",
                "latencyMs", 0,
                "tokenValid", false,
                "lastSyncTime", LocalDateTime.now().minusDays(2).toString(),
                "message", "CRITICAL: Cannot reach Nextgen endpoint. Connection refused. Last successful sync was 2 days ago — data may be significantly stale."
            );
        }
        return Map.of(
            "ehr", "NEXTGEN",
            "tenantId", tenantId,
            "status", "CONNECTED",
            "apiEndpoint", "https://api.nextgen.com/v2",
            "latencyMs", 88,
            "tokenValid", true,
            "lastSyncTime", LocalDateTime.now().minusMinutes(15).toString(),
            "message", "Healthjump connection is healthy"
        );
    }

    public static List<Map<String, Object>> healthjumpPatients(String tenantId) {
        if (isDegraded(tenantId)) {
            // LAKESIDE-HEALTH: stale data, many inactive
            return List.of(
                Map.of("patientId", "LH-3001", "firstName", "Owen", "lastName", "Fletcher",
                        "dateOfBirth", "[REDACTED]", "gender", "Male",
                        "insurancePlan", "Medicaid", "primaryProvider", "Dr. Anna Vasquez",
                        "lastVisit", LocalDate.now().minusDays(180).toString(), "status", "INACTIVE"),
                Map.of("patientId", "LH-3002", "firstName", "Paula", "lastName", "Grant",
                        "dateOfBirth", "[REDACTED]", "gender", "Female",
                        "insurancePlan", "Medicare", "primaryProvider", "Dr. Anna Vasquez",
                        "lastVisit", LocalDate.now().minusDays(200).toString(), "status", "INACTIVE"),
                Map.of("patientId", "LH-3003", "firstName", "Quentin", "lastName", "Shaw",
                        "dateOfBirth", "[REDACTED]", "gender", "Male",
                        "insurancePlan", "Uninsured", "primaryProvider", "Dr. Ben Osei",
                        "lastVisit", LocalDate.now().minusDays(90).toString(), "status", "ACTIVE"),
                Map.of("patientId", "LH-3004", "firstName", "Rachel", "lastName", "Simmons",
                        "dateOfBirth", "[REDACTED]", "gender", "Female",
                        "insurancePlan", "Medicaid", "primaryProvider", "Dr. Ben Osei",
                        "lastVisit", LocalDate.now().minusDays(365).toString(), "status", "INACTIVE")
            );
        }
        // BAYVIEW-CLINIC: active, good coverage
        return List.of(
            Map.of("patientId", "BV-3001", "firstName", "William", "lastName", "Davis",
                    "dateOfBirth", "[REDACTED]", "gender", "Male",
                    "insurancePlan", "Cigna", "primaryProvider", "Dr. Rachel Adams",
                    "lastVisit", LocalDate.now().minusDays(5).toString(), "status", "ACTIVE"),
            Map.of("patientId", "BV-3002", "firstName", "Jennifer", "lastName", "Clark",
                    "dateOfBirth", "[REDACTED]", "gender", "Female",
                    "insurancePlan", "Medicare", "primaryProvider", "Dr. Rachel Adams",
                    "lastVisit", LocalDate.now().minusDays(12).toString(), "status", "ACTIVE"),
            Map.of("patientId", "BV-3003", "firstName", "Christopher", "lastName", "White",
                    "dateOfBirth", "[REDACTED]", "gender", "Male",
                    "insurancePlan", "Blue Shield", "primaryProvider", "Dr. Kevin Patel",
                    "lastVisit", LocalDate.now().minusDays(8).toString(), "status", "ACTIVE"),
            Map.of("patientId", "BV-3004", "firstName", "Patricia", "lastName", "Moore",
                    "dateOfBirth", "[REDACTED]", "gender", "Female",
                    "insurancePlan", "Humana", "primaryProvider", "Dr. Kevin Patel",
                    "lastVisit", LocalDate.now().minusDays(20).toString(), "status", "ACTIVE"),
            Map.of("patientId", "BV-3005", "firstName", "Andrew", "lastName", "Taylor",
                    "dateOfBirth", "[REDACTED]", "gender", "Male",
                    "insurancePlan", "Aetna", "primaryProvider", "Dr. Rachel Adams",
                    "lastVisit", LocalDate.now().minusDays(2).toString(), "status", "ACTIVE"),
            Map.of("patientId", "BV-3006", "firstName", "Diana", "lastName", "Lewis",
                    "dateOfBirth", "[REDACTED]", "gender", "Female",
                    "insurancePlan", "United Health", "primaryProvider", "Dr. Kevin Patel",
                    "lastVisit", LocalDate.now().minusDays(30).toString(), "status", "ACTIVE")
        );
    }

    public static Map<String, Object> healthjumpSyncStatus(String tenantId) {
        if (isDegraded(tenantId)) {
            return Map.of(
                "tenantId", tenantId,
                "ehr", "NEXTGEN",
                "lastFullSync", LocalDateTime.now().minusDays(2).toString(),
                "lastIncrementalSync", LocalDateTime.now().minusDays(2).toString(),
                "syncStatus", "FAILED",
                "recordsSynced", Map.of(
                    "patients", 312,
                    "appointments", 884,
                    "providers", 4,
                    "insurancePlans", 8
                ),
                "errors", 47,
                "nextScheduledSync", "BLOCKED — connection must be restored"
            );
        }
        return Map.of(
            "tenantId", tenantId,
            "ehr", "NEXTGEN",
            "lastFullSync", LocalDateTime.now().minusHours(6).toString(),
            "lastIncrementalSync", LocalDateTime.now().minusMinutes(15).toString(),
            "syncStatus", "COMPLETED",
            "recordsSynced", Map.of(
                "patients", 2847,
                "appointments", 9312,
                "providers", 18,
                "insurancePlans", 62
            ),
            "errors", 0,
            "nextScheduledSync", LocalDateTime.now().plusMinutes(45).toString()
        );
    }

    // ═══════════════════════════ ALLSCRIPTS ═══════════════════════════
    // Tenants: METRO-CARE (healthy) | SUMMIT-HEALTH (degraded)

    public static Map<String, Object> allscriptsConnectionTest(String tenantId) {
        if (isDegraded(tenantId)) {
            return Map.of(
                "ehr", "ALLSCRIPTS",
                "tenantId", tenantId,
                "status", "ERROR",
                "apiEndpoint", "https://api.allscripts.com/unity",
                "latencyMs", 0,
                "tokenValid", false,
                "lastSyncTime", LocalDateTime.now().minusHours(36).toString(),
                "message", "CRITICAL: Allscripts Unity API returning 503 Service Unavailable. Possible Allscripts outage or misconfigured endpoint URL."
            );
        }
        return Map.of(
            "ehr", "ALLSCRIPTS",
            "tenantId", tenantId,
            "status", "CONNECTED",
            "apiEndpoint", "https://api.allscripts.com/unity",
            "latencyMs", 55,
            "tokenValid", true,
            "lastSyncTime", LocalDateTime.now().minusMinutes(20).toString(),
            "message", "Allscripts Unity API connection is healthy"
        );
    }

    public static List<Map<String, Object>> allscriptsSchedule(String tenantId, String date) {
        String schedDate = (date != null && !date.isBlank()) ? date : LocalDate.now().toString();

        if (isDegraded(tenantId)) {
            // SUMMIT-HEALTH: many cancellations, gaps in schedule
            return List.of(
                Map.of("slotId", "SH-S001", "provider", "Dr. Kevin Lang",
                        "date", schedDate, "startTime", "08:00 AM", "endTime", "08:30 AM",
                        "patient", "OPEN SLOT", "visitType", "-", "status", "AVAILABLE"),
                Map.of("slotId", "SH-S002", "provider", "Dr. Kevin Lang",
                        "date", schedDate, "startTime", "08:30 AM", "endTime", "09:00 AM",
                        "patient", "Tony Barker", "visitType", "Follow-Up", "status", "CANCELLED"),
                Map.of("slotId", "SH-S003", "provider", "Dr. Irene Walsh",
                        "date", schedDate, "startTime", "09:00 AM", "endTime", "09:30 AM",
                        "patient", "OPEN SLOT", "visitType", "-", "status", "AVAILABLE"),
                Map.of("slotId", "SH-S004", "provider", "Dr. Kevin Lang",
                        "date", schedDate, "startTime", "09:30 AM", "endTime", "10:00 AM",
                        "patient", "Ursula Mann", "visitType", "New Patient", "status", "NO_SHOW"),
                Map.of("slotId", "SH-S005", "provider", "Dr. Irene Walsh",
                        "date", schedDate, "startTime", "10:00 AM", "endTime", "10:30 AM",
                        "patient", "OPEN SLOT", "visitType", "-", "status", "AVAILABLE"),
                Map.of("slotId", "SH-S006", "provider", "Dr. Kevin Lang",
                        "date", schedDate, "startTime", "11:00 AM", "endTime", "11:30 AM",
                        "patient", "Victor Holt", "visitType", "Annual Physical", "status", "CONFIRMED"),
                Map.of("slotId", "SH-S007", "provider", "Dr. Irene Walsh",
                        "date", schedDate, "startTime", "01:00 PM", "endTime", "01:30 PM",
                        "patient", "OPEN SLOT", "visitType", "-", "status", "AVAILABLE"),
                Map.of("slotId", "SH-S008", "provider", "Dr. Kevin Lang",
                        "date", schedDate, "startTime", "02:00 PM", "endTime", "02:30 PM",
                        "patient", "Wendy Cole", "visitType", "Follow-Up", "status", "CANCELLED")
            );
        }
        // METRO-CARE: full, busy schedule
        return List.of(
            Map.of("slotId", "MC-S001", "provider", "Dr. Mark Stevens",
                    "date", schedDate, "startTime", "08:00 AM", "endTime", "08:30 AM",
                    "patient", "Nancy Hall", "visitType", "Sick Visit", "status", "COMPLETED"),
            Map.of("slotId", "MC-S002", "provider", "Dr. Mark Stevens",
                    "date", schedDate, "startTime", "08:30 AM", "endTime", "09:00 AM",
                    "patient", "George Wright", "visitType", "Follow-Up", "status", "CHECKED_IN"),
            Map.of("slotId", "MC-S003", "provider", "Dr. Sandra Lopez",
                    "date", schedDate, "startTime", "09:00 AM", "endTime", "09:45 AM",
                    "patient", "Betty Harris", "visitType", "New Patient", "status", "CONFIRMED"),
            Map.of("slotId", "MC-S004", "provider", "Dr. Mark Stevens",
                    "date", schedDate, "startTime", "09:30 AM", "endTime", "10:00 AM",
                    "patient", "Arnold Price", "visitType", "Telehealth", "status", "CONFIRMED"),
            Map.of("slotId", "MC-S005", "provider", "Dr. Sandra Lopez",
                    "date", schedDate, "startTime", "10:00 AM", "endTime", "10:30 AM",
                    "patient", "Frank Miller", "visitType", "Annual Physical", "status", "CONFIRMED"),
            Map.of("slotId", "MC-S006", "provider", "Dr. Mark Stevens",
                    "date", schedDate, "startTime", "10:30 AM", "endTime", "11:00 AM",
                    "patient", "Dorothy Green", "visitType", "Procedure", "status", "CONFIRMED"),
            Map.of("slotId", "MC-S007", "provider", "Dr. Sandra Lopez",
                    "date", schedDate, "startTime", "01:00 PM", "endTime", "01:30 PM",
                    "patient", "Henry King", "visitType", "Urgent", "status", "SCHEDULED"),
            Map.of("slotId", "MC-S008", "provider", "Dr. Mark Stevens",
                    "date", schedDate, "startTime", "02:00 PM", "endTime", "02:30 PM",
                    "patient", "Irene Scott", "visitType", "Follow-Up", "status", "SCHEDULED"),
            Map.of("slotId", "MC-S009", "provider", "Dr. Sandra Lopez",
                    "date", schedDate, "startTime", "03:00 PM", "endTime", "03:30 PM",
                    "patient", "OPEN SLOT", "visitType", "-", "status", "AVAILABLE")
        );
    }

    // ═══════════════════════════ JOB DASHBOARD ═══════════════════════════

    public static List<Map<String, Object>> failedJobs(String tenantId, int hours) {
        if (isDegraded(tenantId)) {
            // Degraded tenants have significantly more failures
            return List.of(
                Map.of("jobId", "JOB-D001",
                        "jobName", "Full Patient Sync",
                        "ehr", deriveEhr(tenantId),
                        "tenantId", tenantId,
                        "startTime", LocalDateTime.now().minusHours(2).toString(),
                        "endTime", LocalDateTime.now().minusHours(2).plusMinutes(1).toString(),
                        "status", "FAILED",
                        "errorMessage", "HTTP 503 Service Unavailable — EHR endpoint unreachable",
                        "recordsProcessed", 0,
                        "retryCount", 3),
                Map.of("jobId", "JOB-D002",
                        "jobName", "Appointment Sync",
                        "ehr", deriveEhr(tenantId),
                        "tenantId", tenantId,
                        "startTime", LocalDateTime.now().minusHours(5).toString(),
                        "endTime", LocalDateTime.now().minusHours(5).plusMinutes(2).toString(),
                        "status", "FAILED",
                        "errorMessage", "OAuth token expired — credential rotation required",
                        "recordsProcessed", 0,
                        "retryCount", 3),
                Map.of("jobId", "JOB-D003",
                        "jobName", "Provider Directory Sync",
                        "ehr", deriveEhr(tenantId),
                        "tenantId", tenantId,
                        "startTime", LocalDateTime.now().minusHours(11).toString(),
                        "endTime", LocalDateTime.now().minusHours(11).plusMinutes(1).toString(),
                        "status", "FAILED",
                        "errorMessage", "Response payload malformed — API schema mismatch",
                        "recordsProcessed", 12,
                        "retryCount", 1),
                Map.of("jobId", "JOB-D004",
                        "jobName", "Insurance Plan Sync",
                        "ehr", deriveEhr(tenantId),
                        "tenantId", tenantId,
                        "startTime", LocalDateTime.now().minusHours(22).toString(),
                        "endTime", LocalDateTime.now().minusHours(22).plusMinutes(3).toString(),
                        "status", "FAILED",
                        "errorMessage", "Connection timeout after 30s — possible EHR maintenance window",
                        "recordsProcessed", 0,
                        "retryCount", 3)
            );
        }
        // Healthy tenants: only minor intermittent issues
        return List.of(
            Map.of("jobId", "JOB-9001",
                    "jobName", "Incremental Patient Sync",
                    "ehr", deriveEhr(tenantId),
                    "tenantId", tenantId,
                    "startTime", LocalDateTime.now().minusHours(5).withMinute(30).toString(),
                    "endTime", LocalDateTime.now().minusHours(5).withMinute(32).toString(),
                    "status", "FAILED",
                    "errorMessage", "Connection timeout after 30s — transient network blip, auto-retry succeeded",
                    "recordsProcessed", 0,
                    "retryCount", 1)
        );
    }

    public static Map<String, Object> jobStatusSummary(String tenantId, String ehrType) {
        if (isDegraded(tenantId)) {
            return Map.of(
                "tenantId", tenantId,
                "ehr", ehrType != null && !ehrType.isBlank() ? ehrType : deriveEhr(tenantId),
                "reportTime", LocalDateTime.now().toString(),
                "last24Hours", Map.of(
                    "totalJobs", 18,
                    "successful", 8,
                    "failed", 9,
                    "running", 1
                ),
                "last7Days", Map.of(
                    "totalJobs", 126,
                    "successful", 74,
                    "failed", 52,
                    "running", 0
                ),
                "lastSuccessfulSync", Map.of(
                    deriveEhr(tenantId), LocalDateTime.now().minusHours(14).toString()
                ),
                "alertLevel", "CRITICAL",
                "recommendation", "Immediate intervention required. Rotate credentials and verify EHR endpoint availability."
            );
        }
        return Map.of(
            "tenantId", tenantId,
            "ehr", ehrType != null && !ehrType.isBlank() ? ehrType : deriveEhr(tenantId),
            "reportTime", LocalDateTime.now().toString(),
            "last24Hours", Map.of(
                "totalJobs", 24,
                "successful", 23,
                "failed", 1,
                "running", 0
            ),
            "last7Days", Map.of(
                "totalJobs", 168,
                "successful", 165,
                "failed", 3,
                "running", 0
            ),
            "lastSuccessfulSync", Map.of(
                deriveEhr(tenantId), LocalDateTime.now().minusMinutes(20).toString()
            ),
            "alertLevel", "OK"
        );
    }

    /** Infers the EHR system from a known tenant ID. */
    public static String deriveEhr(String tenantId) {
        if (tenantId == null) return "UNKNOWN";
        return switch (tenantId.toUpperCase()) {
            case "ACME-DEMO", "RIVERSIDE-HEALTH" -> "ATHENA";
            case "DALLAS-ORTHO", "PLANO-MEDICAL"  -> "MODMED";
            case "BAYVIEW-CLINIC", "LAKESIDE-HEALTH" -> "NEXTGEN";
            case "METRO-CARE", "SUMMIT-HEALTH"    -> "ALLSCRIPTS";
            default -> "UNKNOWN";
        };
    }

    /** Returns all known tenants grouped by EHR — useful for monitor/report endpoints. */
    public static Map<String, List<String>> allTenants() {
        return Map.of(
            "ATHENA",      List.of("ACME-DEMO", "RIVERSIDE-HEALTH"),
            "MODMED",      List.of("DALLAS-ORTHO", "PLANO-MEDICAL"),
            "NEXTGEN",     List.of("BAYVIEW-CLINIC", "LAKESIDE-HEALTH"),
            "ALLSCRIPTS",  List.of("METRO-CARE", "SUMMIT-HEALTH")
        );
    }
}

