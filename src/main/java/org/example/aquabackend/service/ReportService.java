package org.example.aquabackend.service;

import java.util.Map;

public interface ReportService {

    Map<String, Object> getDailyReport(String date, Integer pondId);

    Map<String, Object> getMonthlyReport(String month, Integer pondId);

    Map<String, Object> getAnalysisReport(String range, Integer pondId);
}
