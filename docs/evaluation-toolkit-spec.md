# Open AI Evaluation Toolkit for OT Environments

## Specification v0.1 — Draft

### 1. Purpose

This document specifies the design of an open evaluation toolkit for assessing AI models deployed in Operational Technology (OT) and Industrial Control System (ICS) environments. The toolkit provides standardized, reproducible testing methodologies that account for the unique constraints of safety-critical industrial systems — where false positives can halt production lines and false negatives can permit physical damage.

No equivalent open-source AI evaluation framework exists for OT environments. Existing AI benchmarks (MLPerf, GLUE, SuperGLUE, HELM) are designed for IT-domain tasks. OT-specific security evaluation requires fundamentally different testing paradigms because the cost function is asymmetric (a missed threat can cause physical harm; a false alarm can halt a $2M/day production line), the baseline traffic is deterministic and cyclical (unlike stochastic IT traffic), adversarial techniques target physical processes rather than data exfiltration, and model updates must be validated against safety instrumented system (SIS) constraints before deployment.

### 2. Scope

The toolkit evaluates AI/ML models performing the following functions in OT environments:

- **Anomaly detection** on industrial protocol traffic (Modbus, OPC-UA, EtherNet/IP, DNP3, PROFINET)
- **Threat classification** of OT-specific attack patterns (process manipulation, safety system targeting, reconnaissance of control loops)
- **Asset behavior profiling** for programmable logic controllers (PLCs), distributed control systems (DCS), remote terminal units (RTUs), and human-machine interfaces (HMIs)
- **RAG-generated incident briefs** for accuracy, completeness, hallucination rate, and actionability

### 3. Evaluation Dimensions

#### 3.1 Detection Efficacy

| Metric | Definition | OT-Specific Consideration |
|--------|-----------|--------------------------|
| True Positive Rate (TPR) | Correctly identified threats / total threats | Must be evaluated per protocol (Modbus vs. OPC-UA may have different baselines) |
| False Positive Rate (FPR) | False alarms / total benign events | Critical: FPR >5% is operationally unacceptable in production OT environments |
| Mean Time to Detection (MTTD) | Time from threat initiation to alert | Must account for OT scan cycle times (typically 10ms–1s) |
| Detection at Technique Level | MITRE ATT&CK for ICS technique coverage | Mapped to ICS ATT&CK matrix, not Enterprise ATT&CK |

#### 3.2 Operational Safety

| Metric | Definition | OT-Specific Consideration |
|--------|-----------|--------------------------|
| SIS Non-Interference | Model operation does not affect safety instrumented systems | Must be verified under IEC 61511 functional safety constraints |
| Production Impact Score | Quantified risk of model actions on production continuity | Measured as potential downtime-minutes per model decision |
| Fail-Safe Behavior | Model behavior when input data is unavailable or corrupted | Must default to human-operator-alert, never autonomous action |
| Resource Footprint | CPU/memory/network consumption on edge hardware | Must not exceed 15% of edge node capacity to preserve headroom for OT operations |

#### 3.3 Adversarial Robustness

| Test Category | Description |
|--------------|-------------|
| Protocol Fuzzing Resistance | Model stability when presented with malformed or edge-case industrial protocol packets |
| Evasion Attack Resistance | Resistance to adversarial perturbations designed to cause misclassification of OT traffic |
| Concept Drift Detection | Ability to detect when OT baseline traffic patterns shift due to production changes (not threats) |
| Poisoning Resistance | Robustness against training data contamination via manipulated OT telemetry |

#### 3.4 RAG Output Quality (for RAG Decision Engine)

| Metric | Definition |
|--------|-----------|
| Factual Accuracy | Percentage of claims in generated incident briefs that are verifiable against source telemetry |
| Hallucination Rate | Percentage of generated statements not supported by any retrieved context |
| Actionability Score | Operator assessment of whether the brief contains sufficient information to make a containment decision |
| Source Attribution | Percentage of claims with traceable provenance to specific telemetry events, threat intelligence entries, or asset records |
| Latency | Time from event detection to completed brief generation (target: <30 seconds) |

### 4. Test Dataset Requirements

#### 4.1 Benign Baseline Datasets

The toolkit requires representative benign OT traffic datasets for each supported protocol. These datasets must reflect:

- Normal production cycle patterns (start-up, steady-state, shutdown, maintenance windows)
- Legitimate configuration changes and firmware updates
- Normal operator interactions via HMI
- Scheduled polling and scan cycle traffic

**Source options**: Mississippi State University SCADA datasets, ICS-CERT provided datasets, synthetically generated traffic using OpenPLC and GRFICSv2 testbed environments, or operator-contributed anonymized traffic.

#### 4.2 Attack Datasets

Attack datasets must cover the MITRE ATT&CK for ICS matrix, with priority on:

- **Initial Access**: Exploitation of remote services, internet-accessible devices, engineering workstation compromise
- **Execution**: Command-line interface abuse, scripting on PLCs, unauthorized program uploads
- **Persistence**: Module firmware modification, project file infection
- **Evasion**: Rootkit deployment on embedded devices, indicator removal
- **Discovery**: Control system network scanning, I/O module enumeration
- **Lateral Movement**: Default credential exploitation, remote service abuse
- **Collection**: Point and tag identification, program upload harvesting
- **Inhibit Response Function**: Safety system disabling, alarm suppression, device restart/shutdown
- **Impair Process Control**: Unauthorized parameter modification, braking system manipulation
- **Impact**: Damage to property, denial of control, loss of safety, manipulation of control

#### 4.3 Dataset Governance

All test datasets must be accompanied by:

- Provenance documentation (source, collection method, anonymization applied)
- Protocol version and vendor equipment specifications
- Environmental context (discrete manufacturing, process manufacturing, hybrid)
- Known limitations and gaps

### 5. Evaluation Procedure

#### Step 1: Baseline Establishment
Deploy the model under test against the benign baseline dataset for a minimum of 72 hours of simulated operational time. Record all alerts generated. Calculate the baseline FPR.

#### Step 2: Detection Testing
Inject attack scenarios from the attack dataset into the baseline traffic stream. Record TPR, MTTD, and technique-level coverage. Attacks must be injected at varying intensities (single-event, low-and-slow, burst) and during different operational phases (steady-state, transition, maintenance).

#### Step 3: Adversarial Robustness Testing
Execute the adversarial test suite (protocol fuzzing, evasion attacks, concept drift simulation, poisoning simulation). Record model stability, degradation curves, and recovery behavior.

#### Step 4: Safety Constraint Verification
Verify that model operation does not interfere with SIS functions, does not exceed resource footprint thresholds, and defaults to safe behavior under all failure modes.

#### Step 5: RAG Output Evaluation (if applicable)
Generate incident briefs for a representative sample of detected events. Evaluate using the RAG quality metrics defined in Section 3.4. Minimum sample size: 100 events spanning at least 10 distinct attack techniques.

#### Step 6: Report Generation
Produce a standardized evaluation report including all metrics, test conditions, dataset versions, model version, and edge hardware specifications. Report format is specified in `evaluations/report-template.md`.

### 6. Scoring Framework

The toolkit produces a composite OT-AI Readiness Score (0–100) weighted as follows:

| Dimension | Weight |
|-----------|--------|
| Detection Efficacy | 35% |
| Operational Safety | 30% |
| Adversarial Robustness | 20% |
| RAG Output Quality | 15% |

A model must achieve a minimum composite score of 70 and a minimum Safety score of 80 to be considered deployment-ready for production OT environments.

### 7. Alignment with Standards

- **NIST SP 800-82r3**: Evaluation categories map to SP 800-82r3 security control families (see `docs/nist-800-82r3-alignment.md`)
- **IEC 62443**: Safety and availability metrics align with IEC 62443 security levels (SL 1–4)
- **MITRE ATT&CK for ICS**: Attack dataset coverage maps to the ICS ATT&CK technique matrix
- **NIST AI RMF**: Evaluation dimensions align with the NIST AI Risk Management Framework trustworthiness characteristics

### 8. Status

This specification is in **draft** status. Community review and feedback are welcome via GitHub Issues.

### 9. Planned Additions

- Benchmark dataset packaging and distribution tooling
- Automated evaluation pipeline (CI/CD integration)
- Vendor-submitted model evaluation portal
- Cross-model comparison dashboards
- Integration with CISA CSET for combined manual + AI evaluation workflows
