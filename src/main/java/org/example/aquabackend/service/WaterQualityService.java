package org.example.aquabackend.service;

import org.example.aquabackend.vo.WaterQualitySnapshotVO;

import java.util.List;

public interface WaterQualityService {

    void simulateWaterQuality();

    List<WaterQualitySnapshotVO> getLatestSnapshots();

    List<WaterQualitySnapshotVO> getRecentHistory(Integer pondId, Integer limit);
}
