package org.example.aquabackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.aquabackend.dto.AlarmSettingDTO;
import org.example.aquabackend.entity.AlarmSetting;
import org.example.aquabackend.mapper.AlarmSettingMapper;
import org.example.aquabackend.service.AlarmSettingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlarmSettingServiceImpl implements AlarmSettingService {

    private static final Logger logger = LoggerFactory.getLogger(AlarmSettingServiceImpl.class);

    @Autowired
    private AlarmSettingMapper alarmSettingMapper;

    @Override
    public AlarmSetting getByPondId(Integer pondId) {
        return alarmSettingMapper.selectOne(
                new QueryWrapper<AlarmSetting>().eq("pond_id", pondId)
        );
    }

    @Override
    public List<AlarmSetting> getAll() {
        return alarmSettingMapper.selectList(
                new QueryWrapper<AlarmSetting>().orderByAsc("pond_id")
        );
    }

    @Override
    public void save(AlarmSettingDTO dto) {
        AlarmSetting existing = alarmSettingMapper.selectOne(
                new QueryWrapper<AlarmSetting>().eq("pond_id", dto.getPondId())
        );

        if (existing != null) {
            BeanUtils.copyProperties(dto, existing);
            alarmSettingMapper.updateById(existing);
        } else {
            AlarmSetting entity = new AlarmSetting();
            BeanUtils.copyProperties(dto, entity);
            alarmSettingMapper.insert(entity);
        }
        logger.info("Saved alarm settings for pondId={}", dto.getPondId());
    }
}
