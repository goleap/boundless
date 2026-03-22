# Reference Architecture: Discrete Manufacturing Environment

## Deployment Blueprint v0.1 — Draft

### 1. Environment Description

Discrete manufacturing facilities produce distinct, countable items — automotive parts, electronics, machined components, consumer goods. These environments are characterized by:

- Multiple CNC machines, robotic cells, and assembly stations controlled by PLCs
- Typically Rockwell Allen-Bradley (ControlLogix/CompactLogix) or Siemens (S7-1500) PLCs
- EtherNet/IP and/or PROFINET as primary industrial protocols
- MES (Manufacturing Execution System) bridging IT and OT layers
- Shift-based production cycles with scheduled maintenance windows
- Purdue model Levels 0–3 present; Level 3.5 DMZ varies by facility maturity

### 2. Typical Network Topology

```
PURDUE LEVEL 4–5 (Enterprise)
┌─────────────────────────────────────────────────┐
│  Corporate IT Network                            │
│  ERP, Email, Business Intelligence               │
└────────────────────┬────────────────────────────┘
                     │ Firewall
PURDUE LEVEL 3.5 (DMZ)
┌────────────────────┴────────────────────────────┐
│  ┌──────────────────────┐  ┌──────────────────┐ │
│  │ Centralized Fusion   │  │ Historian Mirror │ │
│  │ Layer                │  │                  │ │
│  └──────────┬───────────┘  └──────────────────┘ │
└─────────────┼───────────────────────────────────┘
              │ Firewall (IT/OT boundary)
PURDUE LEVEL 3 (Site Operations)
┌─────────────┴───────────────────────────────────┐
│  ┌────────────┐  ┌────────────┐                  │
│  │    MES     │  │  Historian │                  │
│  └─────┬──────┘  └─────┬──────┘                  │
│        │               │                          │
│  ──────┴───────────────┴────── Site Operations    │
│                    Network                        │
└────────────────────┬────────────────────────────┘
                     │
PURDUE LEVEL 2 (Supervisory)
┌────────────────────┴────────────────────────────┐
│  ┌──────┐  ┌──────┐  ┌──────┐                   │
│  │ HMI  │  │ HMI  │  │ Eng  │                   │
│  │ Stn 1│  │ Stn 2│  │ Wkstn│                   │
│  └──┬───┘  └──┬───┘  └──┬───┘                   │
│     │         │         │                        │
│  ───┴─────────┴─────────┴──── Supervisory Net    │
│     │                                            │
│  ┌──┴───────────────┐                            │
│  │ EDGE INFERENCE   │ ← Deployed here            │
│  │ ENGINE           │                            │
│  └──┬───────────────┘                            │
│     │ (receives TAP/SPAN copy)                   │
└─────┼────────────────────────────────────────────┘
      │
PURDUE LEVEL 1 (Basic Control)
┌─────┴────────────────────────────────────────────┐
│  ┌─────┐  ┌─────┐  ┌─────┐  ┌─────┐            │
│  │ PLC │  │ PLC │  │ PLC │  │ VFD │            │
│  │ L1  │  │ L2  │  │ L3  │  │     │            │
│  └──┬──┘  └──┬──┘  └──┬──┘  └──┬──┘            │
│     │         │         │        │               │
│  ═══╪═════════╪═════════╪════════╪═══            │
│     │    NETWORK TAP (passive)                   │
│     │         │                                  │
│     │    ┌────┴──────────────┐                   │
│     │    │ VNOIP COLLECTOR   │                   │
│     │    │ (passive, receive │                   │
│     │    │  only)            │                   │
│     │    └───────────────────┘                   │
│     │                                            │
└─────┼────────────────────────────────────────────┘
      │
PURDUE LEVEL 0 (Physical Process)
┌─────┴────────────────────────────────────────────┐
│  Sensors, Actuators, Motors, Valves               │
└──────────────────────────────────────────────────┘
```

### 3. Component Placement

| Component | Location | Rationale |
|-----------|----------|-----------|
| **VNOIP Collector** | Purdue Level 1, connected to network TAP on control network | Passive observation of PLC-to-PLC and PLC-to-HMI traffic at the control layer |
| **Edge Inference Engine** | Purdue Level 2 or Level 3, on dedicated hardware | Close enough to OT network for low-latency telemetry ingestion; separated from Level 1 to avoid any risk of interference |
| **Centralized Fusion Layer** | Purdue Level 3.5 (DMZ) | Cross-facility correlation requires DMZ placement; receives pre-processed telemetry only (no raw OT traffic) |
| **RAG Decision Engine** | Co-located with Fusion Layer in DMZ | Requires access to threat intel feeds (internet-sourced) and asset inventory; must not be on OT network |

### 4. Protocol Monitoring Focus

For discrete manufacturing, prioritize monitoring of:

| Protocol | Why | What to Watch |
|----------|-----|--------------|
| EtherNet/IP (CIP) | Primary protocol for Allen-Bradley PLCs | Unauthorized program downloads, configuration changes, unexpected connected/unconnected messaging |
| PROFINET | Primary protocol for Siemens PLCs | Alarm patterns, diagnostic messages, unexpected IO controller changes |
| Modbus TCP | Legacy devices, VFDs, power monitoring | Write commands to holding registers from unexpected sources |
| OPC-UA | MES-to-PLC communication | Authentication failures, subscription changes, method calls to safety-relevant nodes |

### 5. Baseline Considerations for Discrete Manufacturing

Discrete manufacturing environments exhibit highly cyclical traffic patterns that simplify baseline construction:

- **Shift patterns**: Traffic volume and function code distribution correlate with shift schedules (day shift, night shift, weekend)
- **Production cycles**: Batch changeovers produce predictable traffic spikes (recipe downloads, parameter changes)
- **Maintenance windows**: Scheduled downtime produces distinct traffic signatures (firmware updates, diagnostic scans) that must be tagged to avoid false positives
- **Seasonal variation**: Some facilities have seasonal production changes (e.g., automotive model-year changeovers) that require baseline retraining

**Recommendation**: Configure baseline learning with shift-aware time windowing and explicitly tag maintenance windows to prevent baseline contamination.

### 6. Deployment Checklist

- [ ] Identify all OT network segments and Purdue level boundaries
- [ ] Install passive network TAP on Level 1 control network (coordinate with OT operations to schedule during maintenance window)
- [ ] Deploy VNOIP Collector on dedicated hardware with receive-only network interface
- [ ] Verify zero packet loss on TAP (compare TAP output byte count to switch port counters)
- [ ] Deploy Edge Inference Engine on industrial-grade hardware at Level 2/3
- [ ] Configure VNOIP Collector → Edge Engine telemetry stream
- [ ] Run 72-hour baseline learning period during normal production
- [ ] Tag scheduled maintenance windows in baseline configuration
- [ ] Deploy Centralized Fusion Layer in DMZ (if multi-facility)
- [ ] Configure Edge Engine → Fusion Layer telemetry forwarding
- [ ] Load threat intelligence feeds into RAG knowledge stores
- [ ] Import or discover asset inventory
- [ ] Run Evaluation Toolkit baseline assessment
- [ ] Review initial alert output with OT operations team; tune FPR thresholds
- [ ] Transition from shadow mode to active monitoring

### 7. Status

This reference architecture is in **draft** status. Additional architectures for process manufacturing (continuous flow) and hybrid IT/OT converged environments are planned.
