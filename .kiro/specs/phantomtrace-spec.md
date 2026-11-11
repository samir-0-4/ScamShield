# Specification: PhantomTrace Platform Core

## 1. Goal
Provide a unified, multi-modal scam and digital threat investigation platform that empowers users to submit suspicious communications, files, and indicators, constructing an interactive relationship graph.

## 2. Evidence Types
- WhatsApp chat exports and screenshots
- Phishing emails (EML, raw headers, text)
- URLs, domains, and IP addresses
- Payment receipts and fraudulent transaction proofs
- QR codes (decoded URI/payload)
- PDF attachments and suspicious documents
- Recruitment/job offer scam messages

## 3. Key Technical Invariants
- Backend: Spring Boot 3.x, Java 21, Spring Data JPA, PostgreSQL.
- Frontend: React 18, Vite, Tailwind CSS, React Flow, Recharts, Lucide React.
- AI Layer: Groq API with OpenAI-compatible client abstraction.
- Intelligence Feeds: VirusTotal API, URLhaus API.
- Graph Engine: React Flow frontend visualization fed by backend node-link projection DTOs.

