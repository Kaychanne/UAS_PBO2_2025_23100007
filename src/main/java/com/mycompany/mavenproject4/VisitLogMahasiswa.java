package com.mycompany.mavenproject4;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VisitLogMahasiswa {
    private static List<VisitLog> visitLogList = new ArrayList<>();
    private static long idCoun = 1;

    public static VisitLog add(String studentId, String studentName, String studyProgram, String purpose, LocalDateTime visitTime) {
        VisitLog visitLog = new VisitLog(idCoun++, studentId, studentName, studyProgram, purpose, visitTime);
        visitLogList.add(visitLog);
        return visitLog;
    }

    public static VisitLog update(Long id, String studentId, String studentName, String studyProgram, String purpose, LocalDateTime visitTime) {
        VisitLog visitLog = findById(id);
        if (visitLog != null) {
            if (studentId != null) visitLog.studentId = studentId;
            if (studentName != null) visitLog.studentName = studentName;
            if (studyProgram != null) visitLog.studyProgram = studyProgram;
            if (purpose != null) visitLog.purpose = purpose;
            if (visitTime != null) visitLog.visitTime = visitTime;
        } else {
            System.out.println("Update failed: Mahasiswa not fond for id=" + id);
        }
        return visitLog;
    }

    public static boolean delete(Long id) {
        boolean result = visitLogList.removeIf(p -> p.id.equals(id));
        System.out.println("Delete data with id=" + id + ", result=" + result);
        return result;
    }

    public static List<VisitLog> findAll() {
        return visitLogList;
    }

    public static VisitLog findById(Long id) {
        return visitLogList.stream().filter(p -> p.id.equals(id)).findFirst().orElse(null);
    }
}
