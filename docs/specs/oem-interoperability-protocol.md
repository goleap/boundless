# Vendor-Neutral OEM Interoperability Protocol (VNOIP)

## Specification v0.1 — Draft

### 1. Problem Statement

Manufacturing OT environments are characterized by extreme vendor heterogeneity. A single factory floor may contain PLCs from Siemens, Allen-Bradley (Rockwell), Mitsubishi, and Schneider Electric, each with proprietary communication protocols, diagnostic interfaces, and firmware management systems. OEM licensing agreements frequently prohibit installation of third-party software on production controllers, and warranty terms may be voided by unauthorized network monitoring.

This fragmentation creates a fundamental barrier to unified security monitoring. No standardized mechanism exists for extracting security-relevant telemetry from heterogeneous OT vendor environments without requiring vendor-specific agents, proprietary APIs, or OEM authorization for each device class.

VNOIP addresses this gap by defining a vendor-neutral protocol for passive security telemetry exchange that operates entirely at the network layer — requiring no software installation on OT assets, no modification to OEM firmware, and no vendor-specific API integration.

### 2. Design Constraints

The protocol must satisfy the following constraints, derived from real-world OT operational requirements:

| Constraint | Rationale |
|-----------|-----------|
| **Zero software footprint on OT assets** | OEM restrictions and safety certification (IEC 61508/61511) prohibit third-party software on safety-critical controllers |
| **Passive-only network observation** | Active scanning or probing can disrupt deterministic OT communication cycles and trigger safety interlocks |
| **No dependency on vendor APIs** | Vendor API availability is inconsistent, often requires paid licensing, and creates single-vendor dependency |
| **Air-gap compatible** | Many OT networks operate in air-gapped or semi-air-gapped configurations; protocol must function without internet connectivity |
| **Bandwidth-minimal** | OT networks frequently operate at 10/100 Mbps with strict bandwidth budgets; security telemetry must not compete with production traffic |
| **Determinism-preserving** | Must not introduce jitter, latency, or packet loss on production network segments |

### 3. Architecture

```
┌──────────────────────────────────────────────────────────┐
│                    OT PRODUCTION NETWORK                  │
│                                                          │
│  ┌─────┐  ┌─────┐  ┌─────┐  ┌─────┐  ┌─────┐          │
│  │ PLC │  │ PLC │  │ DCS │  │ RTU │  │ HMI │          │
│  │(S7) │  │(CIP)│  │     │  │     │  │     │          │
│  └──┬──┘  └──┬──┘  └──┬──┘  └──┬──┘  └──┬──┘          │
│     │        │        │        │        │               │
│  ═══╪════════╪════════╪════════╪════════╪═══            │
│     │        │        │        │        │               │
│     │    NETWORK TAP / SPAN PORT                        │
│     │        │ (passive copy)                           │
│     │        ▼                                          │
│  ┌──────────────────┐                                   │
│  │  VNOIP Collector  │  ← Runs on dedicated hardware    │
│  │  (Passive Only)   │     outside OT network segment   │
│  └────────┬─────────┘                                   │
│           │                                             │
└───────────┼─────────────────────────────────────────────┘
            │
            │ VNOIP Telemetry Stream
            │ (standardized format)
            ▼
┌───────────────────────┐
│   Edge AI Inference   │
│   Engine              │
└───────────────────────┘
```

### 4. Telemetry Data Model

VNOIP defines a standardized telemetry envelope that normalizes security-relevant observations from heterogeneous OT protocols into a common schema.

#### 4.1 VNOIP Telemetry Envelope

```json
{
  "vnoip_version": "0.1",
  "timestamp_utc": "2026-03-15T14:32:07.123456Z",
  "collector_id": "factory-a-collector-01",
  "source": {
    "mac_address": "00:1A:2B:3C:4D:5E",
    "ip_address": "192.168.1.10",
    "asset_type": "PLC",
    "vendor_class": "siemens_s7",
    "protocol_observed": "s7comm"
  },
  "destination": {
    "mac_address": "00:1A:2B:3C:4D:5F",
    "ip_address": "192.168.1.20",
    "asset_type": "HMI",
    "vendor_class": "generic_hmi",
    "protocol_observed": "s7comm"
  },
  "observation": {
    "category": "control_command",
    "subcategory": "write_variable",
    "severity_hint": "informational",
    "details": {
      "function_code": "0x05",
      "memory_area": "DB100",
      "data_length_bytes": 4,
      "is_response": false
    }
  },
  "network_context": {
    "packet_size_bytes": 128,
    "inter_arrival_time_ms": 100,
    "session_id": "a1b2c3d4",
    "vlan_id": 10
  },
  "enrichment": {
    "asset_criticality": "high",
    "zone": "purdue_level_1",
    "known_baseline_match": true,
    "baseline_deviation_score": 0.02
  }
}
```

#### 4.2 Protocol Normalization Layer

Each supported industrial protocol is normalized to the VNOIP observation model through protocol-specific parsers. The normalization layer extracts security-relevant fields while discarding production payload data (process values, setpoints) to minimize data volume and avoid exposing proprietary production information.

| Industrial Protocol | Extracted Fields | Not Extracted |
|--------------------|-----------------|---------------|
| Modbus TCP/RTU | Function codes, register addresses, exception codes, unit IDs | Actual register values (process data) |
| OPC-UA | Service types, node IDs, security policy, authentication events | Node values (process data) |
| EtherNet/IP (CIP) | Service codes, class/instance/attribute paths, connection types | Attribute values (process data) |
| S7comm (Siemens) | Function codes, memory areas, block types, access patterns | Data block contents |
| DNP3 | Function codes, object groups, qualifier codes, unsolicited responses | Point values |
| PROFINET | Alarm types, diagnostic data, connection states | Cyclic IO data |

#### 4.3 Baseline Deviation Scoring

The VNOIP collector maintains a rolling behavioral baseline per asset pair and protocol. The `baseline_deviation_score` (0.0–1.0) quantifies how far the current observation deviates from the learned normal pattern based on:

- Communication frequency (inter-arrival time distribution)
- Function code distribution
- Memory area / register access patterns
- Packet size distribution
- Time-of-day patterns (production shifts, maintenance windows)

A score of 0.0 indicates perfect baseline match; 1.0 indicates a completely novel observation.

### 5. Transport

VNOIP telemetry streams are transported from collectors to the edge inference engine via:

- **Primary**: Unidirectional data diode (hardware-enforced one-way transmission) for air-gapped environments
- **Alternative**: TLS 1.3 encrypted channel over a dedicated monitoring VLAN for semi-air-gapped environments
- **Format**: Protocol Buffers (protobuf) for wire efficiency, with JSON as a human-readable alternative
- **Delivery**: At-least-once delivery semantics with collector-side buffering for network interruptions

### 6. Collector Deployment Models

| Model | Description | Use Case |
|-------|-------------|----------|
| **Hardware TAP** | Dedicated network TAP appliance with VNOIP collector software | Highest fidelity; recommended for critical process networks |
| **SPAN Port** | Collector receives mirrored traffic from managed switch SPAN port | Suitable when TAP installation is infeasible; subject to SPAN port limitations |
| **Diode-Fed** | Collector behind a hardware data diode receiving one-way traffic | Required for air-gapped safety-critical networks (SIL 3/4 environments) |

### 7. Compatibility with Existing OT Security Tooling

VNOIP is designed to complement, not replace, existing OT security tools:

| Tool | Integration Point |
|------|------------------|
| CISA ICSNPP (Zeek) | VNOIP collectors can ingest ICSNPP-parsed protocol logs as an alternative input source |
| Dragos Platform | VNOIP telemetry can be exported in Dragos-compatible formats for organizations using both systems |
| Claroty / Nozomi | VNOIP's normalized schema can serve as a translation layer between proprietary platforms |
| SIEM/SOAR | VNOIP telemetry envelopes are structured for direct ingestion by Splunk, Sentinel, or Chronicle |

### 8. Security Considerations

- Collectors must be hardened and treated as security-sensitive infrastructure
- Collector firmware must be signed and verified at boot
- Telemetry streams must be encrypted in transit (except through hardware data diodes, which provide physical-layer security)
- No write-back capability: collectors MUST NOT have any ability to transmit data back to the OT production network
- Collector access must be restricted to authorized security personnel via role-based access control

### 9. Status

This specification is in **draft** status. Community review and feedback are welcome via GitHub Issues.
