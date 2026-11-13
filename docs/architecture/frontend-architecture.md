# PhantomTrace — Frontend Architecture

## Technology Stack & Design System

- **Framework**: React 18 with Vite bundling
- **Routing**: React Router v6
- **Styling**: Tailwind CSS
- **State Management**: React Context API for lightweight global auth/session state, localized component state for UI controls
- **Graph Visualization**: React Flow (`@xyflow/react`)
- **Analytics & Visuals**: Recharts and Lucide React icons
- **HTTP Client**: Centralized Axios instance with request/response interceptors

## Directory Hierarchy & Purpose

```
frontend/src/
├── assets/          # Static branding, SVG icons, and illustration resources
├── components/      # Pure, presentational, and reusable design components
│   ├── common/      # Buttons, modals, badges, cards, inputs
│   ├── layout/      # AppShell, Navbar, Sidebar, Footer
│   ├── graph/       # Custom React Flow nodes and edges
│   └── ui/          # Generic primitive UI elements
├── features/        # Business logic encapsulated by domain
│   ├── authentication/
│   ├── investigations/
│   ├── evidence-upload/
│   ├── threat-analysis/
│   ├── evidence-graph/
│   ├── ai-agents/
│   └── reports/
├── pages/           # High-level route views (Landing, Dashboard, Investigation, etc.)
├── services/        # HTTP API services, auth token storage, client config
├── context/         # AuthContext, InvestigationContext
├── hooks/           # Custom reusable hooks (useAuth, useInvestigation, useGraph)
├── routes/          # Route definitions and ProtectedRoute guards
└── utils/           # Formatters, date parsers, epistemic status helpers
```

## Graph Visualization Strategy

The Evidence Relationship Graph rendered with React Flow is completely decoupled from the rest of the layout:
- Custom node types: `EvidenceNode`, `ThreatIndicatorNode`, `ActorNode`, `TransactionNode`.
- Custom edges: `VerifiedEdge` (solid line, green/blue), `InferenceEdge` (dashed line, amber), `HypotheticalEdge` (dotted line, gray).
- Graph data transforms are located strictly in `features/evidence-graph/` to ensure performance and testability.

