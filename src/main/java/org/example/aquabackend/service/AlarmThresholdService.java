package org.example.aquabackend.service;

import org.example.aquabackend.dto.AlarmThresholdDTO;
import org.example.aquabackend.vo.AlarmOverviewVO;
import org.example.aquabackend.vo.AlarmThresholdVO;

import java.util.List;

public interface AlarmThresholdService {

    List<AlarmThresholdVO> getThresholds(Integer pondId);

    void batchSave(List<AlarmThresholdDTO> list);

    AlarmOverviewVO getOverview();

    List<AlarmThresholdVO> getThresholdsWithCurrent(Integer pondId);
}
