# OT-AI Evaluation Report Template

## Report Metadata

| Field | Value |
|-------|-------|
| Report ID | |
| Date | |
| Model Under Test | |
| Model Version | |
| Evaluation Toolkit Version | |
| Evaluator | |
| Edge Hardware Spec | |
| Baseline Dataset Version | |
| Attack Dataset Version | |

## 1. Detection Efficacy

| Metric | Result | Target | Pass/Fail |
|--------|--------|--------|-----------|
| True Positive Rate (overall) | | ≥90% | |
| True Positive Rate (per protocol) | | ≥85% per protocol | |
| False Positive Rate | | ≤5% | |
| Mean Time to Detection | | ≤OT scan cycle × 10 | |
| MITRE ATT&CK for ICS Technique Coverage | | ≥70% of applicable techniques | |

### Detection by Protocol

| Protocol | TPR | FPR | MTTD | Notes |
|----------|-----|-----|------|-------|
| Modbus TCP | | | | |
| OPC-UA | | | | |
| EtherNet/IP | | | | |
| S7comm | | | | |
| DNP3 | | | | |
| PROFINET | | | | |

## 2. Operational Safety

| Metric | Result | Target | Pass/Fail |
|--------|--------|--------|-----------|
| SIS Non-Interference | | Verified | |
| Production Impact Score | | 0 downtime-minutes | |
| Fail-Safe Behavior | | Alert-only on failure | |
| Resource Footprint (CPU) | | ≤15% | |
| Resource Footprint (Memory) | | ≤15% | |
| Resource Footprint (Network) | | ≤5% of link capacity | |

## 3. Adversarial Robustness

| Test | Result | Notes |
|------|--------|-------|
| Protocol Fuzzing Resistance | | |
| Evasion Attack Resistance | | |
| Concept Drift Detection | | |
| Poisoning Resistance | | |

## 4. RAG Output Quality (if applicable)

| Metric | Result | Target | Pass/Fail |
|--------|--------|--------|-----------|
| Factual Accuracy | | ≥95% | |
| Hallucination Rate | | ≤2% | |
| Actionability Score | | ≥4.0/5.0 | |
| Source Attribution Coverage | | 100% | |
| Brief Generation Latency | | ≤30s | |

## 5. Composite OT-AI Readiness Score

| Dimension | Weight | Score | Weighted |
|-----------|--------|-------|----------|
| Detection Efficacy | 35% | | |
| Operational Safety | 30% | | |
| Adversarial Robustness | 20% | | |
| RAG Output Quality | 15% | | |
| **COMPOSITE** | **100%** | | **__/100** |

**Deployment Readiness**: [ ] READY (≥70 composite, ≥80 safety) / [ ] NOT READY

## 6. Test Conditions

### Environment
- Facility type:
- OT network segments monitored:
- Number of OT assets:
- Production state during testing:

### Datasets
- Benign baseline dataset:
- Attack dataset:
- Duration of baseline establishment:
- Number of attack scenarios injected:

### Limitations and Caveats


## 7. Recommendations


## 8. Approvals

| Role | Name | Date | Signature |
|------|------|------|-----------|
| Evaluator | | | |
| OT Operations Review | | | |
| Security Review | | | |
