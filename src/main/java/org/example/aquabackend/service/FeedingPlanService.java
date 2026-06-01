package org.example.aquabackend.service;

import org.example.aquabackend.entity.FeedingPlan;

import java.util.List;
import java.util.Map;

public interface FeedingPlanService {

    List<FeedingPlan> getPlans(Integer pondId, String status);

    List<FeedingPlan> generatePlans();

    FeedingPlan executePlan(Integer planId, String operator);

    List<FeedingPlan> executeAllPlans(String operator);

    FeedingPlan cancelPlan(Integer planId);

    List<Map<String, Object>> getFeedingLogs(Integer pondId, int page, int size);
}
