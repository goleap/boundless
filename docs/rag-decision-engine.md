# RAG Decision Engine for OT Threat Analysis

## Architectural Specification v0.1 — Draft

### 1. Overview

The RAG Decision Engine generates operator-ready incident briefs by synthesizing OT telemetry, threat intelligence feeds, asset inventories, and historical incident data through Retrieval-Augmented Generation. The engine is designed for OT security operations centers (SOCs) where operators must make containment decisions under time pressure, often with limited cybersecurity expertise.

The primary output is a structured incident brief that a plant operator or OT security analyst can read and act upon within 60 seconds — answering: what happened, what asset is affected, how critical is it, what is the recommended response, and what is the confidence level.

### 2. Why RAG for OT (Not Pure LLM)

Pure LLM inference is insufficient for OT incident analysis because:

- **Hallucination risk is unacceptable**: A fabricated asset name, incorrect protocol detail, or hallucinated remediation step in a safety-critical environment could lead to physical harm
- **OT knowledge is sparse in training data**: LLMs have limited exposure to ICS/SCADA-specific threat patterns, industrial protocol semantics, and OT asset behavior
- **Context must be grounded in live telemetry**: The incident brief must reference actual observed events, not statistical generalizations
- **Auditability is required**: Every claim in the brief must be traceable to a specific source (telemetry event, threat intel entry, asset record)

RAG addresses these limitations by grounding LLM generation in retrieved, verifiable context — ensuring that the model generates based on evidence, not parametric memory alone.

### 3. Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                      RAG DECISION ENGINE                         │
│                                                                  │
│  ┌──────────┐   ┌─────────────┐   ┌──────────────────────────┐  │
│  │  Query   │──▶│  Retriever  │──▶│  Generator (LLM)         │  │
│  │  Builder │   │  (Hybrid)   │   │  + Policy Constraints    │  │
│  └────┬─────┘   └──────┬──────┘   └────────────┬─────────────┘  │
│       │                │                        │                │
│       │         ┌──────┴──────┐         ┌───────┴──────┐        │
│       │         │  Retrieved  │         │  Incident    │        │
│       │         │  Context    │         │  Brief       │        │
│       │         │  (ranked)   │         │  (structured)│        │
│       │         └─────────────┘         └──────────────┘        │
│       │                                                          │
│  ┌────┴──────────────────────────────────────────────────────┐   │
│  │                    KNOWLEDGE STORES                         │   │
│  │                                                            │   │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────┐ │   │
│  │  │ Telemetry│ │ Threat   │ │ Asset    │ │ Incident     │ │   │
│  │  │ Events   │ │ Intel    │ │ Inventory│ │ History      │ │   │
│  │  │ (Vector) │ │ (Vector) │ │ (Struct) │ │ (Vector+SQL) │ │   │
│  │  └──────────┘ └──────────┘ └──────────┘ └──────────────┘ │   │
│  └────────────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────┘
```

### 4. Knowledge Stores

#### 4.1 Telemetry Events Store
- **Source**: VNOIP telemetry from edge inference engines (anomalies and classified threats only; benign traffic is not stored at this layer)
- **Indexing**: Vector embeddings (for semantic similarity search) + structured metadata (timestamp, asset, protocol, severity)
- **Retention**: Rolling 90-day window (configurable per facility)

#### 4.2 Threat Intelligence Store
- **Sources**: CISA ICS-CERT advisories, Dragos WorldView threat intel, MITRE ATT&CK for ICS technique descriptions, vendor security bulletins, CVE/NVD entries for ICS products
- **Indexing**: Vector embeddings of advisory text + structured fields (CVE ID, affected product, CVSS score, ATT&CK technique)
- **Update frequency**: Continuous ingestion via automated feeds

#### 4.3 Asset Inventory
- **Source**: VNOIP passive asset discovery + operator-maintained asset register
- **Format**: Structured database (asset ID, MAC, IP, vendor, model, firmware version, Purdue level, criticality rating, connected processes)
- **Enrichment**: Automatically cross-referenced with NVD/CVE data for known vulnerabilities per asset

#### 4.4 Incident History
- **Source**: Previous RAG-generated briefs + operator feedback/disposition records
- **Purpose**: Enables "similar incident" retrieval — surfacing how previous similar events were handled
- **Indexing**: Vector embeddings + structured outcome fields (disposition, response time, false positive flag)

### 5. Retrieval Strategy

The retriever uses a hybrid approach combining:

1. **Dense retrieval** (vector similarity): Finds semantically relevant context across all knowledge stores using the detected event as the query
2. **Sparse retrieval** (keyword/metadata matching): Filters by structured fields — matching asset vendor, protocol, ATT&CK technique ID, and time window
3. **Re-ranking**: Retrieved candidates are re-ranked using a cross-encoder model that scores relevance of each candidate to the specific incident context

The retrieval pipeline returns a ranked context set with full provenance metadata (source store, document ID, retrieval score) attached to each retrieved item.

### 6. Generation with Policy Constraints

The LLM generator operates under a strict policy constraint layer that enforces:

| Constraint | Implementation |
|-----------|---------------|
| **No hallucination of asset identifiers** | All asset names, IPs, MACs, and vendor details must be drawn from the retrieved context or asset inventory; any generated identifier not found in the retrieval set triggers a constraint violation |
| **No autonomous containment commands** | The generator may recommend containment actions but must never generate executable commands; all output is advisory |
| **Source attribution required** | Every factual claim in the brief must include a bracketed source reference (e.g., [VNOIP-event-12345], [CISA-ICS-CERT-2026-001]) |
| **Confidence scoring** | Each section of the brief includes a confidence level (High/Medium/Low) based on the quantity and quality of supporting retrieved context |
| **Escalation triggering** | If retrieved context is insufficient to generate a high-confidence brief (confidence below configurable threshold), the engine escalates to human analyst review instead of generating a low-confidence output |

### 7. Incident Brief Output Format

```
╔══════════════════════════════════════════════════════════════════╗
║  INCIDENT BRIEF — OT-2026-03-15-0047                           ║
║  Generated: 2026-03-15T14:32:42Z | Confidence: HIGH             ║
╠══════════════════════════════════════════════════════════════════╣
║                                                                  ║
║  WHAT HAPPENED                                                   ║
║  Anomalous write command detected on Modbus TCP to PLC-A-017    ║
║  (Siemens S7-1500, firmware v2.9.4) targeting holding register  ║
║  range 40100–40108, which controls coolant flow valve position   ║
║  on CNC machining line 3. [VNOIP-event-88421]                   ║
║                                                                  ║
║  The write command originated from 10.0.2.55 (Engineering       ║
║  Workstation EW-04), outside the normal polling schedule and     ║
║  with a function code (0x10, Write Multiple Registers) not      ║
║  previously observed from this source in the 90-day baseline.   ║
║  Baseline deviation score: 0.91. [VNOIP-baseline-PLC-A-017]     ║
║                                                                  ║
║  THREAT ASSESSMENT                                               ║
║  Technique: T0855 — Unauthorized Command Message (MITRE ICS)    ║
║  Severity: HIGH — asset criticality rating "critical"; coolant  ║
║  valve manipulation could cause thermal damage to machining      ║
║  equipment. [Asset-inventory-PLC-A-017]                          ║
║                                                                  ║
║  Similar technique observed in CISA Advisory ICSA-25-091-01     ║
║  (March 2025) targeting Siemens S7-1500 series in manufacturing ║
║  environments. [Threat-intel-ICSA-25-091-01]                     ║
║                                                                  ║
║  SIMILAR INCIDENTS                                               ║
║  OT-2025-11-22-0031: Similar baseline deviation on PLC-A-012   ║
║  (same line); disposition: FALSE POSITIVE — authorized firmware  ║
║  update during maintenance window. Confidence that current event ║
║  is maintenance-related: LOW (no maintenance window active).     ║
║  [Incident-history-OT-2025-11-22-0031]                           ║
║                                                                  ║
║  RECOMMENDED RESPONSE                                            ║
║  1. Verify with line operator whether maintenance or authorized  ║
║     configuration change is in progress on CNC line 3.           ║
║  2. If unauthorized: isolate Engineering Workstation EW-04 from  ║
║     OT network segment via switch port disable.                  ║
║  3. Preserve VNOIP telemetry logs for forensic analysis.         ║
║  4. Notify plant security and OT security lead.                  ║
║                                                                  ║
║  ⚠ THIS BRIEF IS ADVISORY ONLY. NO AUTOMATED ACTION HAS BEEN   ║
║  TAKEN. OPERATOR AUTHORIZATION REQUIRED FOR ALL CONTAINMENT.     ║
╚══════════════════════════════════════════════════════════════════╝
```

### 8. Performance Targets

| Metric | Target |
|--------|--------|
| Brief generation latency | <30 seconds from event detection |
| Factual accuracy | >95% of claims verifiable against source data |
| Hallucination rate | <2% of generated statements |
| Source attribution coverage | 100% of factual claims include source reference |
| Operator actionability rating | >4.0/5.0 average (measured via operator feedback) |

### 9. Deployment Options

- **On-premises** (centralized fusion layer): For organizations requiring 100% data residency
- **Hybrid**: Telemetry processing on-premises; LLM inference via private API endpoint with no raw OT data transmission (only pre-processed, anonymized query context)
- **Air-gapped**: Fully on-premises with locally hosted LLM (e.g., quantized open-weight model)

### 10. Status

This design is in **draft** status. Community review and feedback are welcome via GitHub Issues.
