package org.example.aquabackend.service;

import org.example.aquabackend.entity.WaterQualityData;

public interface AlarmEvaluator {

    void evaluate(WaterQualityData wqData);
}
