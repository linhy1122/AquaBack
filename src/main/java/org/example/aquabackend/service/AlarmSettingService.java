package org.example.aquabackend.service;

import org.example.aquabackend.dto.AlarmSettingDTO;
import org.example.aquabackend.entity.AlarmSetting;

import java.util.List;

public interface AlarmSettingService {

    AlarmSetting getByPondId(Integer pondId);

    List<AlarmSetting> getAll();

    void save(AlarmSettingDTO dto);
}
