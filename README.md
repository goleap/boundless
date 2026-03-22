# Boundless

**An open-source Agentic RAG and Data Platform for securing American manufacturing and smart factory environments** — integrating real-time data pipelines, Retrieval-Augmented Generation (RAG), and policy-constrained AI agents to protect Industrial Control Systems (ICS), Operational Technology (OT), and converged IT/OT networks.

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

---

## The Problem

U.S. manufacturing is the most persistently targeted sector in the global cyber threat landscape, accounting for approximately one-quarter of all cyberattacks worldwide. Ransomware attacks against industrial organizations increased 87% year-over-year in 2024, with the average cost of a cyberattack in the industrial sector reaching $5.56 million.

Yet fewer than 10% of OT networks globally have meaningful monitoring in place. Existing cybersecurity tools were designed for IT environments and cannot be deployed on legacy OT assets, air-gapped networks, or production-critical systems subject to strict OEM restrictions. CISA maintains foundational open-source OT tooling (ICSNPP, CSET, LME), but no AI-native detection capability, LLM-based threat analysis, AI safety evaluation framework, or vendor-neutral OEM interoperability protocol exists in the current federal open-source ecosystem.

Boundless addresses that gap.

## Core Design Principles

- **Agentless by default**: No software installed on safety-critical OT assets. Telemetry collection via passive network TAPs and SPAN ports only.
- **Edge-first inference**: AI anomaly detection and threat classification run on-premises. Raw OT traffic never leaves the facility.
- **Human-in-the-loop containment**: Automated detection, human-authorized response. No autonomous action on production systems without operator approval.
- **Vendor-neutral**: No dependency on any single OT vendor, security product, or cloud provider. Designed for adoption across heterogeneous manufacturing environments.
- **NIST-aligned**: All components mapped to NIST SP 800-82r3 (Guide to OT Security) and the NIST Cybersecurity Framework.
- **Open and permissively licensed**: Apache 2.0. Designed for nationwide adoption by manufacturing operators, systems integrators, and the broader industrial cybersecurity ecosystem.

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                    CENTRALIZED FUSION LAYER                        │
│  ┌─────────────┐  ┌──────────────┐  ┌───────────────────────────┐  │
│  │ Cross-Domain │  │ RAG Decision │  │ Human-in-the-Loop         │  │
│  │ Telemetry    │  │ Engine       │  │ Containment Agents        │  │
│  │ Correlation  │  │              │  │ (Policy-Constrained)      │  │
│  └──────┬───────┘  └──────┬───────┘  └─────────────┬─────────────┘  │
│         │                 │                        │                │
│         └─────────────────┴────────────────────────┘                │
│                           │                                         │
│              ┌────────────┴────────────┐                            │
│              │  Unified Telemetry Bus  │  (>1M events/sec)          │
│              └────────────┬────────────┘                            │
└───────────────────────────┼─────────────────────────────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
┌───────┴────────┐  ┌───────┴────────┐  ┌───────┴────────┐
│  EDGE NODE A   │  │  EDGE NODE B   │  │  EDGE NODE N   │
│  (Factory 1)   │  │  (Factory 2)   │  │  (Factory N)   │
│                │  │                │  │                │
│ ┌────────────┐ │  │ ┌────────────┐ │  │ ┌────────────┐ │
│ │ Edge AI    │ │  │ │ Edge AI    │ │  │ │ Edge AI    │ │
│ │ Inference  │ │  │ │ Inference  │ │  │ │ Inference  │ │
│ └─────┬──────┘ │  │ └─────┬──────┘ │  │ └─────┬──────┘ │
│       │        │  │       │        │  │       │        │
│ ┌─────┴──────┐ │  │ ┌─────┴──────┐ │  │ ┌─────┴──────┐ │
│ │ Agentless  │ │  │ │ Agentless  │ │  │ │ Agentless  │ │
│ │ OT Collect │ │  │ │ OT Collect │ │  │ │ OT Collect │ │
│ │ (TAP/SPAN) │ │  │ │ (TAP/SPAN) │ │  │ │ (TAP/SPAN) │ │
│ └────────────┘ │  │ └────────────┘ │  │ └────────────┘ │
└────────────────┘  └────────────────┘  └────────────────┘
        │                   │                   │
   ┌────┴────┐         ┌────┴────┐         ┌────┴────┐
   │ OT/ICS  │         │ OT/ICS  │         │ OT/ICS  │
   │ Network │         │ Network │         │ Network │
   └─────────┘         └─────────┘         └─────────┘
```

## Platform Components

### Core Application

The Boundless application provides the Agentic RAG runtime — document ingestion, entity extraction (via [xtracto](https://github.com/goleap/xtracto)), retrieval pipelines, and LLM-powered analysis for manufacturing OT environments.

→ See [Getting Started](#getting-started) below for setup and running instructions.

### Architectural Specifications

#### 1. Open AI Evaluation Toolkit for OT Environments
Standardized testing and validation framework for AI models operating in safety-critical OT environments. Includes adversarial robustness testing, false-positive rate benchmarking against production OT baselines, and safety constraint verification.

→ [Specification: `docs/evaluation-toolkit-spec.md`](docs/evaluation-toolkit-spec.md)

#### 2. Vendor-Neutral OEM Interoperability Protocol (VNOIP)
Protocol specification enabling security telemetry exchange across heterogeneous OT vendor environments without requiring vendor-specific agents or proprietary APIs.

→ [Specification: `docs/specs/oem-interoperability-protocol.md`](docs/specs/oem-interoperability-protocol.md)

#### 3. NIST SP 800-82r3 Alignment Mapping
Control-by-control mapping between this platform's capabilities and NIST SP 800-82r3 security controls, enabling manufacturing operators to demonstrate compliance through platform adoption.

→ [Mapping: `docs/nist-800-82r3-alignment.md`](docs/nist-800-82r3-alignment.md)

#### 4. Edge Inference Engine Design
Architectural specification for on-premises AI inference capable of anomaly detection and threat classification on OT telemetry without transmitting raw traffic off-site.

→ [Design: `docs/edge-inference-design.md`](docs/edge-inference-design.md)

#### 5. RAG Decision Engine for OT Threat Analysis
Architecture for Retrieval-Augmented Generation pipelines that synthesize OT telemetry, threat intelligence feeds, asset inventories, and historical incident data to generate operator-ready incident briefs.

→ [Design: `docs/rag-decision-engine.md`](docs/rag-decision-engine.md)

#### 6. Reference Architectures
Deployment blueprints for common manufacturing environments.

→ [Discrete Manufacturing: `docs/reference-architectures/discrete-manufacturing.md`](docs/reference-architectures/discrete-manufacturing.md)

#### 7. AI Evaluation Report Template
Standardized report format for evaluating AI models against the OT-AI Readiness Score framework.

→ [Template: `docs/evaluations/report-template.md`](docs/evaluations/report-template.md)

## Relationship to Existing CISA Open-Source Ecosystem

Boundless is designed to complement — not replace — CISA's existing open-source OT security tooling:

| CISA Tool | Function | Boundless |
|-----------|----------|-----------|
| **ICSNPP** | ICS protocol parsers for Zeek | Ingests ICSNPP-parsed protocol data as a telemetry source |
| **CSET** | Manual cybersecurity evaluation | Automates continuous evaluation via AI-driven assessment |
| **LME** | Centralized IT log management | Extends to OT-native telemetry with edge processing |
| **vulnrichment** | CVE enrichment data | Feeds RAG decision engine for contextual threat analysis |
| **CWE SIG** | ICS/OT weakness classification | Maps to evaluation toolkit test categories |

## Roadmap

| Phase | Timeline | Deliverables |
|-------|----------|-------------|
| **Phase 1: Foundation** | 2025–2026 | Core RAG application, document extraction pipeline (xtracto), open AI evaluation toolkit v1.0, OEM interoperability protocol spec, NIST alignment mapping |
| **Phase 2: Core Platform** | 2027 | Edge inference engine reference implementation, RAG decision engine for OT threat analysis, reference architectures for discrete and process manufacturing |
| **Phase 3: Validation** | 2028 | Pilot validation with manufacturing systems integrators, performance benchmarks against industry baselines, CISA ICS-CERT community review submission |

## Target Performance Objectives

- **Incident triage time reduction**: 50–60% reduction through RAG-generated operator briefs
- **Containment and response time reduction**: 20–30% reduction through human-in-the-loop agent-assisted workflows
- **False positive rate**: <5% on OT-native traffic baselines (vs. 40–60% typical for IT tools deployed in OT environments)
- **Telemetry throughput**: >1M events/sec at the centralized fusion layer
- **Edge inference latency**: <100ms per classification decision

## Industrial Protocol Support (Planned)

- Modbus TCP/RTU
- OPC-UA (OPC Unified Architecture)
- EtherNet/IP (CIP)
- DNP3 (Distributed Network Protocol)
- PROFINET
- BACnet
- IEC 61850 (power systems)
- IEC 62443 alignment (industrial automation security)

## Who This Is For

- **Manufacturing operators**: Deploy AI-native OT security without vendor lock-in
- **Systems integrators**: Use reference architectures to secure client manufacturing environments at scale
- **OT security practitioners**: Evaluate and harden AI models for safety-critical environments
- **Researchers**: Build on standardized evaluation tooling for OT-specific AI safety research
- **Policy makers**: Reference NIST-aligned implementation guidance for critical manufacturing sector directives

---

## Getting Started

### Prerequisites

- Java 21 (ensure `PATH` and `JAVA_HOME` are configured)
- Gradle (included via wrapper)

### Running the Application

```bash
./gradlew clean bootRun
```

- **UI** (upload and list documents): http://localhost:9000
- **Management/Actuator APIs**: http://localhost:9001/actuator

### Configuration

- To set your ChatPDF API key, update `pdf.api.key.value` in `src/main/resources/application.properties`
- API keys can be created at https://www.chatpdf.com/

### Related Components

- **[xtracto](https://github.com/goleap/xtracto)** — Document extraction pipeline for unstructured sources (PDFs, Office documents, images). Feeds structured data into the Boundless RAG platform.

### Development Notes

- After uploading documents, allow a moment for processing before the list updates
- Application health can be monitored via Spring Actuator at http://localhost:9001/actuator
- Currently uses H2 in-memory database for development; production deployment will use a dedicated database cluster

### Planned Development

- Account/user authentication, handling, and scoping
- Consumer-provided entity extraction instructions
- Fully async and non-blocking processing pipeline
- Parallel multi-document upload and processing
- Configurable data retention policies
- Dedicated database cluster for production deployment

---

## Contributing

This project welcomes contributions from the manufacturing cybersecurity community. See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

## License

Apache License 2.0 — see [LICENSE](LICENSE) for details.

## References

- NIST SP 800-82r3: Guide to Operational Technology (OT) Security (September 2023)
- CISA Critical Manufacturing Sector Risk Management Overview (June 2023)
- CISA Critical Manufacturing Sector Cybersecurity Framework Implementation Guidance (May 2020)
- NSA/CISA/ODNI: Securing the Software Supply Chain — Managing Open-Source Software and SBOM (December 2023)
- Dragos 2025 OT/ICS Cybersecurity Year in Review
- CrowdStrike 2026 Global Threat Report
- Deloitte & MAPI: Smart Factory Cyber Risk Study
- Center for American Progress: Clean and Cyber-Resilient Manufacturing Impact Analysis

## Author

**Diptamay Sanyal** — Principal Engineer, AI & Cybersecurity. Architect of real-time data and AI platforms deployed across federal, enterprise, and critical infrastructure environments. [Forbes Technology Council member](https://councils.forbes.com/profile/Diptamay-Sanyal-Data-AI-Cybersecurity-Leader-Crowdstrike/e1fca19c-78c5-4b84-ac5a-e4a500ee06e2).
