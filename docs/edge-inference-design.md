# Edge Inference Engine Design

## Architectural Specification v0.1 — Draft

### 1. Overview

The Edge Inference Engine performs on-premises AI anomaly detection and threat classification on OT telemetry so that raw industrial network traffic never leaves the facility. This addresses a fundamental constraint of manufacturing OT environments: production data, process parameters, and control system communications are often classified as proprietary trade secrets or subject to export control restrictions (ITAR/EAR), making cloud-based analysis infeasible for many manufacturing operators.

### 2. Design Requirements

| Requirement | Target | Rationale |
|------------|--------|-----------|
| Classification latency | <100ms per decision | Must keep pace with OT scan cycles (typically 10ms–1s) |
| Throughput | ≥10,000 events/sec per edge node | Typical medium-sized factory generates 5,000–15,000 OT network events/sec |
| Resource ceiling | ≤15% of edge node compute capacity | Remaining capacity reserved for OT operations and headroom |
| Data residency | 100% on-premises processing | No raw OT telemetry transmitted off-site |
| Availability | 99.9% uptime | Monitoring gaps create blind spots; must match OT availability targets |
| Model update mechanism | Signed model packages, verified before deployment | Prevents supply-chain attacks via model poisoning |
| Fail-safe behavior | Default to alert-only mode on any failure | Never block or modify OT traffic under any circumstance |

### 3. Architecture

```
┌────────────────────────────────────────────────────────────┐
│                     EDGE INFERENCE ENGINE                    │
│                                                             │
│  ┌─────────────────┐  ┌─────────────────┐  ┌────────────┐  │
│  │  VNOIP Telemetry │  │  Model Runtime  │  │  Alert     │  │
│  │  Ingestion       │──│  Engine         │──│  Publisher │  │
│  │  (Protocol Buf)  │  │  (ONNX Runtime) │  │            │  │
│  └────────┬─────────┘  └────────┬────────┘  └─────┬──────┘  │
│           │                     │                  │         │
│  ┌────────┴─────────┐  ┌───────┴────────┐  ┌──────┴──────┐  │
│  │  Baseline        │  │  Model         │  │  Telemetry  │  │
│  │  State Store     │  │  Registry      │  │  Forwarder  │  │
│  │  (RocksDB)       │  │  (Signed Pkgs) │  │  (to Fusion)│  │
│  └──────────────────┘  └────────────────┘  └─────────────┘  │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Health Monitor & Watchdog                            │   │
│  │  (restarts engine on failure; alerts on degradation)  │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### 4. Model Runtime

The engine uses ONNX Runtime as the inference backend, providing:

- **Vendor neutrality**: Models can be trained in PyTorch, TensorFlow, or scikit-learn and exported to ONNX format
- **Edge optimization**: ONNX Runtime supports quantization (INT8), graph optimization, and hardware acceleration on edge-class hardware
- **Deterministic behavior**: Inference results are reproducible given identical inputs and model versions

#### 4.1 Model Types

| Model | Function | Input | Output |
|-------|----------|-------|--------|
| Anomaly Detector | Identifies deviations from learned OT baseline behavior | VNOIP telemetry envelope + baseline state | Anomaly score (0.0–1.0) + anomaly category |
| Threat Classifier | Classifies detected anomalies into threat categories mapped to MITRE ATT&CK for ICS | Anomaly event + context window (prior 60s of telemetry) | Threat category + confidence score + ATT&CK technique ID |
| Asset Profiler | Maintains behavioral fingerprints per OT asset | Rolling window of VNOIP telemetry per source MAC/IP | Updated asset behavior profile |

#### 4.2 Model Update Protocol

1. New model packages are signed by the model publisher using Ed25519 digital signatures
2. Edge engine verifies signature against the trusted publisher's public key before loading
3. New model is deployed in shadow mode alongside the active model for a configurable validation period (default: 24 hours)
4. Shadow model outputs are compared against the active model; if divergence exceeds configurable thresholds, the update is flagged for human review
5. After validation, the operator approves promotion to active status via the management console
6. Rollback to previous model version is available at any time

### 5. Baseline State Management

The Baseline State Store (RocksDB) maintains per-asset, per-protocol behavioral baselines:

- Communication frequency histograms (binned by inter-arrival time)
- Function code usage distributions
- Memory area / register access patterns
- Packet size distributions
- Time-of-day activity profiles (aligned to production shift schedules)

Baselines are continuously updated using exponential decay weighting (recent observations weighted more heavily) with a configurable learning rate. Baseline updates can be frozen during known-good production periods and unfrozen during maintenance windows to prevent maintenance activity from contaminating the baseline.

### 6. Hardware Requirements

| Component | Minimum Specification | Recommended |
|-----------|----------------------|-------------|
| CPU | x86-64, 4 cores, 2.0 GHz | 8 cores, 2.5+ GHz |
| RAM | 8 GB | 16 GB |
| Storage | 128 GB SSD (for baseline state and model storage) | 256 GB NVMe |
| Network | 2x 1GbE (1 TAP ingestion, 1 management/telemetry forward) | 2x 10GbE |
| Form factor | Industrial-grade (DIN rail, fanless, extended temp range) | Industrial-grade with redundant power |
| OS | Linux (hardened, minimal installation) | Linux with mandatory access control (SELinux/AppArmor) |

### 7. Deployment Topology

Each edge node is deployed at the facility level, positioned between the OT network TAP/SPAN output and the facility's demilitarized zone (DMZ). The edge node has:

- **Ingestion interface**: Receives passive traffic copy from TAP/SPAN (receive-only; no transmit capability to OT network)
- **Management interface**: Connected to the management network for model updates, health monitoring, and telemetry forwarding to the centralized fusion layer

For facilities with multiple OT network segments (e.g., separate networks for PLCs, DCS, and building automation), multiple edge nodes can be deployed with a local aggregation layer.

### 8. Status

This design is in **draft** status. Community review and feedback are welcome via GitHub Issues.
