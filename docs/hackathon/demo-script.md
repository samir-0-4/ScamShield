# PhantomTrace Demo Script — 2:45 Video

> ⚠️ **All URLs, domains, and evidence in this demo use SYNTHETIC data created for demonstration purposes only. No real malicious infrastructure is visited or verified.**

---

## Pre-Demo Setup Checklist

- [ ] Backend running: `http://localhost:8080` (health check passes)
- [ ] Frontend running: `http://localhost:5173`
- [ ] Demo account ready (run `./scripts/demo-scenario.sh` or use seed account)
- [ ] Browser: dark mode, 1920×1080, tabs closed except PhantomTrace
- [ ] Screen recording started, mic tested

---

## 0:00 – 0:20 | Hook

**[Show a mock SMS notification on screen — use screenshot or phone prop]**

> **Narrator (calm, direct):**
> "You just got this text. Your bank account is locked. You need to click a link — right now — or your account gets suspended permanently."
>
> "What do you do?"

**[Pause 2 seconds. Let it land.]**

> "You could paste that link into a URL scanner. It'll tell you: clean or malicious. One answer, one URL."
>
> "But phishing campaigns don't work in isolation. Neither should your investigation."

---

## 0:20 – 0:40 | Problem

**[Show split-screen: three evidence items — SMS, email, another message — each isolated]**

> **Narrator:**
> "Traditional tools see this — three separate scans. Three separate verdicts. Zero connection between them."
>
> "The attacker knows this. They rotate domains, spoof display names, vary the message — but always reuse the same infrastructure. The same TLD pattern. The same brand impersonation."
>
> "Miss the pattern, and you miss the campaign."

---

## 0:40 – 1:00 | Solution Intro — Create Investigation

**[Open PhantomTrace dashboard. Already logged in as demo analyst.]**

> **Narrator:**
> "This is PhantomTrace. Instead of scanning one URL, you open an investigation."

**[Click "New Investigation". Type name.]**

| Field | Value |
|---|---|
| **Investigation Name** | Operation Ghost Invoice |
| **Category** | Phishing |
| **Description** | HDFC bank impersonation campaign — multiple evidence items |

**[Click "Create Investigation".]**

> "One case. Everything goes in here."

---

## 1:00 – 1:40 | Upload Evidence 1 — Phishing SMS

**[In the evidence workspace, click "Add Evidence" → "Text / SMS".]**

> **Narrator:**
> "First piece: the SMS. I paste the raw text."

**[Paste the following synthetic SMS text into the evidence field:]**

```
URGENT: Your HDFC account has been temporarily locked due to suspicious activity. 
Please verify your identity immediately at: http://hdfc-verify-secure.tk/login?session=a1b2c3d4
Failure to verify within 2 hours will result in permanent account suspension. Reply STOP to opt out.
```

**[Click "Submit Evidence".]**

> "Watch what happens next — no AI yet."

**[Indicators panel appears. Highlight each indicator:]**

> "PhantomTrace extracts indicators **deterministically** — with regex, not guesswork:
> - URL: `http://hdfc-verify-secure.tk/login?session=a1b2c3d4`
> - Domain: `hdfc-verify-secure.tk`
> - Urgency language: 'account locked', 'verify immediately', '2 hours'
> - Brand impersonation: 'HDFC'
> These are facts. No AI involved yet."

**[Click "Run Threat Intelligence".]**

> "Now we query VirusTotal and URLhaus."

**[Show threat intel results panel. If live results appear, read them. If unavailable:]**

> "Provider is unavailable right now — PhantomTrace shows that explicitly. It never pretends a service is clean when it can't reach it."

---

## 1:40 – 2:00 | Upload Evidence 2 — Phishing Email → Campaign Signal

**[Click "Add Evidence" → "Email".]**

> **Narrator:**
> "Second piece: an email from the same campaign."

**[Enter the following synthetic email details:]**

| Field | Value |
|---|---|
| **From** | security-noreply@hdfc-secure-alerts.tk |
| **Subject** | [ACTION REQUIRED] Suspicious Login Detected on Your Account |
| **Body** | Dear Customer, We detected an unauthorized login attempt from IP 185.220.101.47. Please verify your identity at hdfc-secure-alerts.tk/verify to prevent account suspension. |

**[Click "Submit Evidence".]**

> "Look at the indicators panel."

**[Extracted indicators:]**
- Domain: `hdfc-secure-alerts.tk`
- IP: `185.220.101.47`
- Email: `security-noreply@hdfc-secure-alerts.tk`
- Brand impersonation: 'HDFC'
- Urgency: 'account suspension'

**[A "Campaign Signal" or "Shared Indicator" badge appears in the UI.]**

> "PhantomTrace detected a shared pattern: `.tk` TLD and 'HDFC' brand appear across both evidence items. This is a campaign signal — `POSSIBLE` confidence."
>
> "Two pieces of evidence. One investigation. The connection is already visible."

---

## 2:00 – 2:20 | Graph View

**[Click "Investigation Graph" or the graph icon in the nav.]**

> **Narrator:**
> "Here's the relationship graph."

**[React Flow canvas shows nodes. Walk through the layout:]**

> "These are evidence nodes — the SMS and the email. These are indicator nodes — URLs, domains, emails."
>
> "The edge here says `CONTAINS_URL` — the SMS contained this URL."
> "This edge says `SHARES_INDICATOR` — both evidence items reference an HDFC-branded `.tk` domain."

**[Click on the shared domain node.]**

> "Click any node to see its provenance sidebar. Source: extracted from Evidence 1 and Evidence 2. Confidence: `POSSIBLE`."
>
> "Every connection is evidence-backed. PhantomTrace adds no hardcoded nodes."

---

## 2:20 – 2:40 | Risk Score + Report

**[Click "Risk Assessment". Run assessment if not already computed.]**

> **Narrator:**
> "Risk assessment."

**[Show score breakdown. Read the signals:]**

> "Score: HIGH — but let's look at why:
> - Unusual URL structure: +10 (`.tk` TLD, session parameter)
> - Brand impersonation: +15 (HDFC referenced across evidence)
> - Urgency language: +10
> - Credential request: +25
>
> These weights are configurable. The score is a heuristic — not a verdict."

**[Click "Generate Report". Wait for report to compile.]**

> "One click generates a full investigation report: executive summary, all evidence, extracted indicators, threat intelligence results, graph relationships, and remediation recommendations."

**[Click the browser Print button — or show the print preview.]**

> "Print → Save as PDF. No server-side PDF generation needed."

---

## 2:40 – 2:45 | Close

**[Return to the investigation summary view. Graph visible in background.]**

> **Narrator:**
> "One investigation. All evidence. Clear answers."
>
> "PhantomTrace doesn't tell you what to think. It shows you the evidence, what's verified, what's inferred, and what's unknown — so you can decide."
>
> "That's PhantomTrace."

---

## Fallback Notes

If Groq AI analysis is unavailable during the demo:
> "AI analysis is unavailable right now — PhantomTrace shows this explicitly rather than showing empty or fabricated results. The deterministic extraction, threat intelligence, graph, and risk engine all work independently of the AI pipeline."

If VirusTotal/URLhaus is rate-limited:
> "The threat feed is rate-limited. The UI shows an explicit 'unavailable' state with the timestamp of the last successful lookup — not a false 'clean' verdict."

---

## Synthetic Data Reference

| Item | Value | Note |
|---|---|---|
| SMS domain | `hdfc-verify-secure.tk` | SYNTHETIC — `.tk` is a real TLD but this domain is fabricated |
| Email domain | `hdfc-secure-alerts.tk` | SYNTHETIC |
| IP | `185.220.101.47` | Publicly known Tor exit node — used as an example indicator |
| Investigation name | Operation Ghost Invoice | Fictional investigation name |
| Brand | HDFC | Real brand name used only as the impersonated target in the synthetic scenario |
