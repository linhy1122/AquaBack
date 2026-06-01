package org.example.aquabackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.aquabackend.entity.*;
import org.example.aquabackend.mapper.*;
import org.example.aquabackend.service.FeedingPlanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FeedingPlanServiceImpl implements FeedingPlanService {

    private static final Logger logger = LoggerFactory.getLogger(FeedingPlanServiceImpl.class);

    @Autowired
    private FeedingPlanMapper feedingPlanMapper;

    @Autowired
    private FeedingLogMapper feedingLogMapper;

    @Autowired
    private FarmingBatchMapper farmingBatchMapper;

    @Autowired
    private PondMapper pondMapper;

    @Autowired
    private MaterialMapper materialMapper;

    @Override
    public List<FeedingPlan> getPlans(Integer pondId, String status) {
        QueryWrapper<FeedingPlan> qw = new QueryWrapper<>();
        if (pondId != null) {
            qw.eq("pond_id", pondId);
        }
        if (status != null) {
            qw.eq("status", status);
        }
        qw.orderByDesc("plan_id");
        return feedingPlanMapper.selectList(qw);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<FeedingPlan> generatePlans() {
        List<Pond> activePonds = pondMapper.selectList(
                new QueryWrapper<Pond>().eq("deleted", 0).in("status", "active", "idle"));
        if (activePonds.isEmpty()) {
            logger.info("No ponds found for plan generation");
            return Collections.emptyList();
        }

        List<FarmingBatch> allBatches = farmingBatchMapper.selectList(
                new QueryWrapper<FarmingBatch>().eq("status", "active"));

        Map<Integer, FarmingBatch> latestBatchByPond = allBatches.stream()
                .collect(Collectors.groupingBy(
                    FarmingBatch::getPondId,
                    Collectors.collectingAndThen(
                        Collectors.maxBy(Comparator.comparing(FarmingBatch::getBatchId)),
                        opt -> opt.orElse(null)
                    )
                ));

        List<Material> feeds = materialMapper.selectList(
                new QueryWrapper<Material>().eq("category", "饲料"));

        Map<Integer, FeedingPlan> existingPlans = feedingPlanMapper.selectList(null).stream()
                .filter(p -> p.getPondId() != null)
                .collect(Collectors.toMap(FeedingPlan::getPondId, p -> p, (a, b) -> a));

        List<FeedingPlan> plans = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Pond pond : activePonds) {
            FarmingBatch batch = latestBatchByPond.get(pond.getPondId());
            FeedingPlan existing = existingPlans.get(pond.getPondId());

            FeedingPlan plan = existing != null ? existing : new FeedingPlan();
            plan.setPondId(pond.getPondId());
            plan.setPondName(pond.getName());
            plan.setPondCode(pond.getCode());
            plan.setBatchId(batch != null ? batch.getBatchId() : null);
            plan.setGeneratedAt(now);
            plan.setActualAmount(null);
            plan.setExecutedAt(null);
            plan.setOperator(null);

            if (batch == null) {
                plan.setStatus("no_batch");
                plan.setSuggestedAmount(0.0);
                plan.setCalcReason("该塘口无活跃养殖批次");
                plan.setFactorsJson(null);
            } else if (batch.getCurrentNum() == null || batch.getAvgSpec() == null) {
                plan.setStatus("data_missing");
                plan.setSuggestedAmount(0.0);
                plan.setCalcReason("批次数据不完整（缺当前数量或平均规格）");
                Map<String, Object> factors = new LinkedHashMap<>();
                factors.put("species", batch.getSpecies());
                factors.put("currentNum", batch.getCurrentNum());
                factors.put("avgSpec", batch.getAvgSpec());
                plan.setFactorsJson(factors.toString());
            } else {
                double biomass = batch.getCurrentNum() * batch.getAvgSpec() / 1000.0;
                if (biomass <= 0) {
                    plan.setStatus("data_missing");
                    plan.setSuggestedAmount(0.0);
                    plan.setCalcReason("存塘重量为0，无法计算投喂量");
                    Map<String, Object> factors = new LinkedHashMap<>();
                    factors.put("species", batch.getSpecies());
                    factors.put("biomassKg", 0);
                    plan.setFactorsJson(factors.toString());
                } else {
                    double baseRate = getBaseFeedRate(batch.getSpecies());
                    double tempFactor = 1.0;
                    double stageFactor = getStageFactor(batch.getStockDate());
                    double feedRate = baseRate * tempFactor * stageFactor;
                    double suggestedAmount = biomass * feedRate / 100.0;
                    suggestedAmount = Math.round(suggestedAmount * 100.0) / 100.0;

                    plan.setStockWeight(Math.round(biomass * 100.0) / 100.0);
                    plan.setFeedRate(Math.round(feedRate * 1000.0) / 1000.0);
                    plan.setSuggestedAmount(suggestedAmount);

                    Integer materialId = null;
                    String materialName = null;
                    if (!feeds.isEmpty()) {
                        int idx = new Random().nextInt(feeds.size());
                        Material picked = feeds.get(idx);
                        materialId = picked.getMaterialId();
                        materialName = picked.getName();
                    }
                    plan.setMaterialId(materialId);
                    plan.setMaterialName(materialName);

                    List<String> calcParts = new ArrayList<>();
                    calcParts.add("species=" + (batch.getSpecies() != null ? batch.getSpecies() : "?"));
                    calcParts.add("stockWeight=" + String.format("%.2f", biomass));
                    calcParts.add("feedRate=" + String.format("%.3f", feedRate));
                    calcParts.add("baseRate=" + baseRate);
                    calcParts.add("stageFactor=" + stageFactor);

                    Map<String, Object> factors = new LinkedHashMap<>();
                    factors.put("species", batch.getSpecies());
                    factors.put("currentNum", batch.getCurrentNum());
                    factors.put("avgSpec", batch.getAvgSpec());
                    factors.put("biomassKg", Math.round(biomass * 100.0) / 100.0);
                    factors.put("baseRate", baseRate);
                    factors.put("stageFactor", stageFactor);
                    factors.put("tempFactor", tempFactor);
                    factors.put("materialPool", feeds.size());

                    if (materialId == null) {
                        plan.setStatus("no_feed");
                        plan.setCalcReason("未匹配到合适饲料");
                        factors.put("reason", "无可用饲料");
                    } else {
                        plan.setStatus("pending");
                        plan.setCalcReason(String.join("; ", calcParts));
                    }
                    plan.setFactorsJson(factors.toString());
                }
            }

            if (existing != null) {
                feedingPlanMapper.updateById(plan);
            } else {
                feedingPlanMapper.insert(plan);
            }
            plans.add(plan);
        }

        logger.info("Generated/updated {} feeding plans for {} ponds", plans.size(), activePonds.size());
        return plans;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FeedingPlan executePlan(Integer planId, String operator) {
        FeedingPlan plan = feedingPlanMapper.selectById(planId);
        if (plan == null) {
            throw new RuntimeException("投喂计划不存在");
        }
        if (!"pending".equals(plan.getStatus())) {
            throw new RuntimeException("计划状态不是待执行，无法执行");
        }

        String materialName = plan.getMaterialName();
        if (plan.getMaterialId() != null) {
            Material material = materialMapper.selectById(plan.getMaterialId());
            if (material != null) {
                if (materialName == null) materialName = material.getName();
                Double stock = material.getStockQty() != null ? material.getStockQty() : 0;
                if (stock < plan.getSuggestedAmount()) {
                    throw new RuntimeException(String.format(
                            "库存不足！饲料[%s]当前库存: %.2f kg, 计划投喂: %.2f kg",
                            material.getName(), stock, plan.getSuggestedAmount()));
                }
                material.setStockQty(stock - plan.getSuggestedAmount());
                materialMapper.updateById(material);
            }
        }

        String pondName = plan.getPondName();
        String pondCode = plan.getPondCode();
        if ((pondName == null || pondCode == null) && plan.getPondId() != null) {
            Pond pond = pondMapper.selectById(plan.getPondId());
            if (pond != null) {
                if (pondName == null) pondName = pond.getName();
                if (pondCode == null) pondCode = pond.getCode();
            }
        }

        FeedingLog log = new FeedingLog();
        log.setPondId(plan.getPondId());
        log.setPondCode(pondCode);
        log.setPondName(pondName);
        log.setBatchId(plan.getBatchId());
        log.setPlanId(plan.getPlanId());
        log.setMaterialId(plan.getMaterialId());
        log.setMaterialName(materialName);
        log.setPlannedAmount(plan.getSuggestedAmount());
        log.setActualAmount(plan.getSuggestedAmount());
        log.setFeedRate(plan.getFeedRate());
        log.setStockWeight(plan.getStockWeight());
        log.setOperator(operator);
        log.setExecuteStatus("success");
        log.setFeedTime(LocalDateTime.now());
        feedingLogMapper.insert(log);

        plan.setActualAmount(plan.getSuggestedAmount());
        plan.setStatus("executed");
        plan.setExecutedAt(LocalDateTime.now());
        plan.setOperator(operator);
        if (materialName != null) plan.setMaterialName(materialName);
        if (pondName != null) plan.setPondName(pondName);
        if (pondCode != null) plan.setPondCode(pondCode);
        feedingPlanMapper.updateById(plan);

        logger.info("Executed feeding plan: planId={}, pondId={}, pond={}, amount={}",
                planId, plan.getPondId(), pondName, plan.getSuggestedAmount());
        return plan;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<FeedingPlan> executeAllPlans(String operator) {
        List<FeedingPlan> pending = feedingPlanMapper.selectList(
                new QueryWrapper<FeedingPlan>().eq("status", "pending"));

        List<FeedingPlan> executed = new ArrayList<>();
        for (FeedingPlan plan : pending) {
            try {
                executePlan(plan.getPlanId(), operator);
                executed.add(plan);
            } catch (Exception e) {
                logger.warn("Failed to execute plan {}: {}", plan.getPlanId(), e.getMessage());
            }
        }
        return executed;
    }

    @Override
    public FeedingPlan cancelPlan(Integer planId) {
        FeedingPlan plan = feedingPlanMapper.selectById(planId);
        if (plan == null) {
            throw new RuntimeException("投喂计划不存在");
        }
        plan.setStatus("cancelled");
        feedingPlanMapper.updateById(plan);
        return plan;
    }

    @Override
    public List<Map<String, Object>> getFeedingLogs(Integer pondId, int page, int size) {
        QueryWrapper<FeedingLog> qw = new QueryWrapper<>();
        if (pondId != null) {
            qw.eq("pond_id", pondId);
        }
        qw.orderByDesc("feed_time");

        List<FeedingLog> logs = feedingLogMapper.selectList(qw);
        Map<Integer, Pond> pondCache = new HashMap<>();
        Map<Integer, Material> materialCache = new HashMap<>();
        Map<Integer, FarmingBatch> batchCache = new HashMap<>();
        List<Map<String, Object>> result = new ArrayList<>();

        for (FeedingLog log : logs) {
            Integer resolvedPondId = log.getPondId();
            String pondName = log.getPondName();
            String pondCode = log.getPondCode();
            String materialName = log.getMaterialName();

            if (resolvedPondId == null && log.getBatchId() != null) {
                FarmingBatch batch = batchCache.computeIfAbsent(log.getBatchId(),
                        id -> farmingBatchMapper.selectById(id));
                if (batch != null) {
                    resolvedPondId = batch.getPondId();
                }
            }
            if ((pondName == null || pondCode == null) && resolvedPondId != null) {
                Pond p = pondCache.computeIfAbsent(resolvedPondId, id -> pondMapper.selectById(id));
                if (p != null) {
                    if (pondName == null) pondName = p.getName();
                    if (pondCode == null) pondCode = p.getCode();
                }
            }
            if (materialName == null && log.getMaterialId() != null) {
                Material m = materialCache.computeIfAbsent(log.getMaterialId(), id -> materialMapper.selectById(id));
                if (m != null) materialName = m.getName();
            }

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("logId", log.getLogId());
            item.put("pondId", resolvedPondId);
            item.put("pondCode", pondCode);
            item.put("pondName", pondName);
            item.put("batchId", log.getBatchId());
            item.put("planId", log.getPlanId());
            item.put("materialId", log.getMaterialId());
            item.put("materialName", materialName);
            item.put("plannedAmount", log.getPlannedAmount());
            item.put("actualAmount", log.getActualAmount());
            item.put("feedRate", log.getFeedRate());
            item.put("stockWeight", log.getStockWeight());
            item.put("operator", log.getOperator());
            item.put("executeStatus", log.getExecuteStatus());
            item.put("feedTime", log.getFeedTime());
            item.put("remark", log.getRemark());
            result.add(item);
        }

        return result;
    }

    private double getBaseFeedRate(String species) {
        if (species == null) return 1.0;
        if (species.contains("对虾") || species.contains("虾")) return 4.0;
        if (species.contains("草鱼")) return 3.0;
        if (species.contains("罗非")) return 2.5;
        if (species.contains("鲫鱼") || species.contains("鲤")) return 2.0;
        return 2.0;
    }

    private double getStageFactor(java.time.LocalDate stockDate) {
        if (stockDate == null) return 1.0;
        long days = java.time.temporal.ChronoUnit.DAYS.between(stockDate, java.time.LocalDate.now());
        if (days < 30) return 1.2;
        if (days < 90) return 1.0;
        return 0.8;
    }
}
