export const EPISTEMIC_STATUS = {
  VERIFIED: 'VERIFIED',
  AI_INFERENCE: 'AI_INFERENCE',
  UNKNOWN: 'UNKNOWN',
};

export const EVIDENCE_TYPES = {
  TEXT_MESSAGE: 'TEXT_MESSAGE',
  EMAIL: 'EMAIL',
  URL: 'URL',
  IMAGE: 'IMAGE',
  PDF: 'PDF',
};

export const EVIDENCE_TYPE_OPTIONS = [
  { value: EVIDENCE_TYPES.TEXT_MESSAGE, label: 'Text message' },
  { value: EVIDENCE_TYPES.URL, label: 'URL' },
  { value: EVIDENCE_TYPES.EMAIL, label: 'Email' },
  { value: EVIDENCE_TYPES.IMAGE, label: 'Image' },
  { value: EVIDENCE_TYPES.PDF, label: 'PDF' },
];

// Mirrors backend InvestigationStatus
export const INVESTIGATION_STATUS = {
  DRAFT: 'DRAFT',
  PENDING: 'PENDING',
  PROCESSING: 'PROCESSING',
  COMPLETED: 'COMPLETED',
  FAILED: 'FAILED',
};

export const INVESTIGATION_STATUS_LIST = Object.values(INVESTIGATION_STATUS);

// Display metadata for statuses: tailwind classes + labels
export const STATUS_META = {
  DRAFT: { label: 'Draft', badge: 'bg-slate-800/80 text-slate-300 border-slate-700/80', dot: 'bg-slate-400' },
  PENDING: { label: 'Pending', badge: 'bg-amber-500/10 text-amber-300 border-amber-500/30', dot: 'bg-amber-400' },
  PROCESSING: { label: 'Processing', badge: 'bg-sky-500/10 text-sky-300 border-sky-500/30', dot: 'bg-sky-400' },
  COMPLETED: { label: 'Completed', badge: 'bg-emerald-500/10 text-emerald-300 border-emerald-500/30', dot: 'bg-emerald-400' },
  FAILED: { label: 'Failed', badge: 'bg-rose-500/10 text-rose-300 border-rose-500/30', dot: 'bg-rose-400' },
};

// Mirrors backend InvestigationCategory
export const INVESTIGATION_CATEGORIES = {
  JOB_SCAM: 'JOB_SCAM',
  PAYMENT_SCAM: 'PAYMENT_SCAM',
  PHISHING: 'PHISHING',
  INVESTMENT_SCAM: 'INVESTMENT_SCAM',
  SHOPPING_SCAM: 'SHOPPING_SCAM',
  IDENTITY_FRAUD: 'IDENTITY_FRAUD',
  SUSPICIOUS_COMMUNICATION: 'SUSPICIOUS_COMMUNICATION',
  OTHER: 'OTHER',
};

export const INVESTIGATION_CATEGORY_LIST = Object.values(INVESTIGATION_CATEGORIES);

export const CATEGORY_LABELS = {
  JOB_SCAM: 'Job Scam',
  PAYMENT_SCAM: 'Payment Scam',
  PHISHING: 'Phishing',
  INVESTMENT_SCAM: 'Investment Scam',
  SHOPPING_SCAM: 'Shopping Scam',
  IDENTITY_FRAUD: 'Identity Fraud',
  SUSPICIOUS_COMMUNICATION: 'Suspicious Communication',
  OTHER: 'Other',
};

// Mirrors backend SeverityLevel (used for overallRiskLevel)
export const RISK_LEVELS = {
  LOW: 'LOW',
  MEDIUM: 'MEDIUM',
  HIGH: 'HIGH',
  CRITICAL: 'CRITICAL',
  UNKNOWN: 'UNKNOWN',
};

export const RISK_LEVEL_LIST = Object.values(RISK_LEVELS);

export const RISK_META = {
  LOW: { label: 'Low', badge: 'bg-emerald-500/10 text-emerald-300 border-emerald-500/30', dot: 'bg-emerald-400' },
  MEDIUM: { label: 'Medium', badge: 'bg-amber-500/10 text-amber-300 border-amber-500/30', dot: 'bg-amber-400' },
  HIGH: { label: 'High', badge: 'bg-orange-500/10 text-orange-300 border-orange-500/30', dot: 'bg-orange-400' },
  CRITICAL: { label: 'Critical', badge: 'bg-rose-500/10 text-rose-300 border-rose-500/30', dot: 'bg-rose-400' },
  UNKNOWN: { label: 'Unknown', badge: 'bg-slate-800/80 text-slate-400 border-slate-700/80', dot: 'bg-slate-500' },
};

export const categoryLabel = (value) => CATEGORY_LABELS[value] || value || '—';
export const statusMeta = (value) => STATUS_META[value] || STATUS_META.DRAFT;
export const riskMeta = (value) => RISK_META[value] || RISK_META.UNKNOWN;
