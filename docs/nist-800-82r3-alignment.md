# NIST SP 800-82r3 Alignment Mapping

## Framework Capabilities → NIST OT Security Controls

This document maps the AI-Native OT Security Framework's capabilities to the security control families defined in NIST Special Publication 800-82 Revision 3, *Guide to Operational Technology (OT) Security* (September 2023). The mapping enables manufacturing operators to demonstrate how framework adoption contributes to NIST compliance.

### Mapping Legend

- **Direct**: Framework capability directly implements or automates the control
- **Supporting**: Framework capability provides evidence, data, or tooling that supports manual implementation of the control
- **Planned**: Capability is on the roadmap but not yet specified

---

### AC — Access Control

| NIST Control | Control Description | Framework Component | Coverage |
|-------------|-------------------|-------------------|----------|
| AC-2 | Account Management | Asset behavior profiling detects unauthorized account usage on OT assets | Supporting |
| AC-6 | Least Privilege | VNOIP telemetry tracks function code usage per source, detecting privilege escalation patterns | Supporting |
| AC-17 | Remote Access | Edge inference detects anomalous remote access patterns to OT networks | Direct |

### AT — Awareness and Training

| NIST Control | Control Description | Framework Component | Coverage |
|-------------|-------------------|-------------------|----------|
| AT-2 | Literacy Training and Awareness | RAG decision engine generates operator-readable incident briefs that serve as contextual training | Supporting |
| AT-3 | Role-Based Training | Evaluation toolkit report outputs can be used as training materials for OT security teams | Supporting |

### AU — Audit and Accountability

| NIST Control | Control Description | Framework Component | Coverage |
|-------------|-------------------|-------------------|----------|
| AU-2 | Event Logging | VNOIP collectors generate standardized audit trails of all observed OT network events | Direct |
| AU-3 | Content of Audit Records | VNOIP telemetry envelope includes timestamp, source, destination, protocol, function code, and deviation score | Direct |
| AU-6 | Audit Record Review, Analysis, and Reporting | Edge AI inference performs continuous automated analysis; RAG engine generates summary reports | Direct |
| AU-12 | Audit Record Generation | Agentless collection via TAP/SPAN generates comprehensive audit records without modifying OT assets | Direct |

### CA — Assessment, Authorization, and Monitoring

| NIST Control | Control Description | Framework Component | Coverage |
|-------------|-------------------|-------------------|----------|
| CA-2 | Control Assessments | Evaluation toolkit provides standardized AI model assessment methodology for OT environments | Direct |
| CA-7 | Continuous Monitoring | Edge inference + centralized fusion layer provide continuous monitoring of OT security posture | Direct |
| CA-8 | Penetration Testing | Evaluation toolkit adversarial robustness tests simulate penetration testing of AI detection capabilities | Supporting |

### CM — Configuration Management

| NIST Control | Control Description | Framework Component | Coverage |
|-------------|-------------------|-------------------|----------|
| CM-3 | Configuration Change Control | VNOIP baseline deviation scoring detects unauthorized configuration changes on OT assets | Direct |
| CM-8 | System Component Inventory | VNOIP passive asset discovery builds and maintains OT asset inventory without active scanning | Direct |

### CP — Contingency Planning

| NIST Control | Control Description | Framework Component | Coverage |
|-------------|-------------------|-------------------|----------|
| CP-2 | Contingency Plan | RAG decision engine can generate situational briefs during incidents to support contingency activation | Supporting |
| CP-10 | System Recovery and Reconstitution | Human-in-the-loop containment agents assist with guided recovery procedures | Planned |

### IA — Identification and Authentication

| NIST Control | Control Description | Framework Component | Coverage |
|-------------|-------------------|-------------------|----------|
| IA-3 | Device Identification and Authentication | VNOIP collectors identify devices by MAC address, IP, protocol fingerprint, and behavioral signature | Direct |
| IA-4 | Identifier Management | Asset behavior profiles maintain persistent device identity across network changes | Supporting |

### IR — Incident Response

| NIST Control | Control Description | Framework Component | Coverage |
|-------------|-------------------|-------------------|----------|
| IR-4 | Incident Handling | RAG decision engine generates operator-ready incident briefs with recommended containment actions | Direct |
| IR-5 | Incident Monitoring | Centralized fusion layer provides cross-facility incident correlation and monitoring | Direct |
| IR-6 | Incident Reporting | Standardized telemetry format enables automated incident reporting to CISA ICS-CERT | Supporting |

### RA — Risk Assessment

| NIST Control | Control Description | Framework Component | Coverage |
|-------------|-------------------|-------------------|----------|
| RA-3 | Risk Assessment | Evaluation toolkit composite scoring provides quantified risk assessment for AI-driven OT security | Direct |
| RA-5 | Vulnerability Monitoring and Scanning | Passive OT network observation identifies vulnerable assets without active scanning | Direct |

### SA — System and Services Acquisition

| NIST Control | Control Description | Framework Component | Coverage |
|-------------|-------------------|-------------------|----------|
| SA-9 | External System Services | VNOIP vendor-neutral design enables evaluation of third-party OT security services against standardized metrics | Supporting |

### SC — System and Communications Protection

| NIST Control | Control Description | Framework Component | Coverage |
|-------------|-------------------|-------------------|----------|
| SC-7 | Boundary Protection | IT/OT boundary threat detection is a core framework capability | Direct |
| SC-24 | Fail in Known State | Evaluation toolkit safety constraint verification ensures AI models fail-safe | Direct |

### SI — System and Information Integrity

| NIST Control | Control Description | Framework Component | Coverage |
|-------------|-------------------|-------------------|----------|
| SI-3 | Malicious Code Protection | Edge inference engine detects anomalous code execution patterns on OT networks | Direct |
| SI-4 | System Monitoring | Core framework function: continuous AI-driven monitoring of OT environments | Direct |

---

### IEC 62443 Cross-Reference

For organizations aligning to IEC 62443 (Industrial Automation and Control Systems Security), the following cross-reference applies:

| IEC 62443 Zone | NIST SP 800-82r3 Mapping | Framework Component |
|---------------|------------------------|-------------------|
| Zone 0–1 (Safety/Basic Control) | SC-24, SI-4 | Edge inference with SIS non-interference |
| Zone 2 (Supervisory) | AU-2, AU-6, CM-3 | VNOIP telemetry, baseline deviation scoring |
| Zone 3 (Operations/DMZ) | SC-7, AC-17, IR-4 | IT/OT boundary detection, RAG incident briefs |
| Zone 4–5 (Enterprise) | CA-7, IR-5 | Centralized fusion layer, cross-domain correlation |

---

### Status

This mapping is in **draft** status and covers the highest-priority control families. Full coverage of all SP 800-82r3 control families is planned for v1.0.
