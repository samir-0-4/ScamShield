import { useEffect } from "react";
import { Link } from "react-router-dom";
import {
  ArrowRight,
  ArrowUpRight,
  ShieldCheck,
  Zap,
  Database,
  Lock,
  ScanSearch,
  Network,
  Sparkles,
} from "lucide-react";
import Button from "../components/common/Button";
import Badge from "../components/common/Badge";
import { useAuth } from "../hooks/useAuth";

const SectionHeading = ({ eyebrow, title, description, align = "center" }) => (
  <div
    className={[
      "flex flex-col gap-3",
      align === "center"
        ? "mx-auto max-w-2xl items-center text-center"
        : "max-w-xl items-start text-left",
    ].join(" ")}
  >
    <span className="inline-flex items-center gap-2 rounded-full border border-white/10 bg-white/[0.03] px-3 py-1 font-mono text-[10px] font-semibold uppercase tracking-[0.2em] text-cyan-300">
      {eyebrow}
    </span>
    <h2 className="text-2xl font-extrabold tracking-[-0.02em] text-white sm:text-3xl lg:text-4xl">
      {title}
    </h2>
    <p className="text-sm leading-relaxed text-slate-400 sm:text-base">
      {description}
    </p>
  </div>
);

const Chip = ({ children }) => (
  <span className="rounded-full border border-white/[0.08] bg-white/[0.03] px-2.5 py-1 font-mono text-[10px] font-medium tracking-wide text-slate-400">
    {children}
  </span>
);

const Card = ({ children, className = "" }) => (
  <div
    className={[
      "group relative flex h-full flex-col rounded-3xl border border-white/[0.07]",
      "bg-gradient-to-b from-white/[0.045] to-white/[0.015] p-6",
      "shadow-[0_1px_0_0_rgba(255,255,255,0.06)_inset,0_20px_50px_-30px_rgba(0,0,0,0.9)]",
      "transition-all duration-300",
      "hover:-translate-y-1 hover:border-white/[0.14] hover:from-white/[0.07] hover:to-white/[0.025]",
      className,
    ].join(" ")}
  >
    {children}
  </div>
);

export const LandingPage = () => {
  const { isAuthenticated } = useAuth();

  useEffect(() => {
    if (typeof window === "undefined") return;
    if (
      typeof window.matchMedia === "function" &&
      window.matchMedia("(prefers-reduced-motion: reduce)").matches
    ) {
      return;
    }
    if (typeof IntersectionObserver === "undefined") return;

    const revealElements = document.querySelectorAll("[data-scroll-reveal]");
    if (!revealElements.length) return;

    const windowHeight =
      window.innerHeight || document.documentElement.clientHeight;

    const observer = new IntersectionObserver(
      (entries, obs) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            const target = entry.target;
            target.style.willChange = "transform, opacity";
            target.classList.add("revealed");
            const handleAnimationEnd = () => {
              target.style.willChange = "auto";
              target.removeEventListener("animationend", handleAnimationEnd);
            };
            target.addEventListener("animationend", handleAnimationEnd);
            obs.unobserve(target);
          }
        });
      },
      {
        threshold: 0.15,
        rootMargin: "0px 0px -10% 0px",
      },
    );

    revealElements.forEach((el) => {
      const rect = el.getBoundingClientRect();
      if (rect.top < windowHeight * 0.9) {
        return;
      }
      observer.observe(el);
    });

    return () => observer.disconnect();
  }, []);

  const primaryHref = isAuthenticated ? "/investigations/new" : "/register";
  const secondaryHref = isAuthenticated ? "/dashboard" : "/login";

  return (
    <div className="relative isolate w-full min-w-0">
      <div
        aria-hidden="true"
        className="pointer-events-none absolute inset-0 -z-10 overflow-hidden"
      >
        <div className="absolute -top-40 left-1/2 h-[520px] w-[860px] -translate-x-1/2 rounded-full bg-cyan-600/20 blur-[140px]" />
        <div className="absolute top-[38%] -right-32 h-[420px] w-[420px] rounded-full bg-violet-600/15 blur-[140px]" />
        <div className="absolute bottom-[8%] -left-40 h-[380px] w-[560px] rounded-full bg-cyan-500/10 blur-[140px]" />
      </div>

      <section className="pb-16 pt-10 sm:pb-20 sm:pt-14 lg:pb-28 lg:pt-20">
        <div className="grid grid-cols-1 items-center gap-10 lg:grid-cols-12 lg:gap-14">
          <div className="min-w-0 space-y-8 lg:col-span-7">
            <div className="space-y-5">
              <span className="inline-flex items-center gap-2.5 rounded-full border border-white/10 bg-white/[0.04] px-3.5 py-1.5 font-mono text-[10px] font-semibold uppercase tracking-[0.18em] text-slate-300 backdrop-blur">
                <span className="h-1.5 w-1.5 animate-pulse rounded-full bg-emerald-400 shadow-[0_0_10px_rgba(52,211,153,0.9)]" />
                Autonomous Threat Forensics Engine
              </span>

              <h1 className="hero-enter-headline text-4xl font-extrabold leading-[1.05] tracking-[-0.03em] text-white sm:text-5xl lg:text-6xl">
                Phantom<span className="bg-gradient-to-r from-cyan-400 via-teal-300 to-cyan-300 bg-clip-text text-transparent">Trace</span>
              </h1>
              <p className="mt-3 text-lg font-medium text-slate-300 sm:text-xl">
                AI-powered digital threat forensics — not sure if it&apos;s a scam?{" "}
                <span className="text-cyan-400 font-semibold">Investigate before you trust.</span>
              </p>
            </div>

            <p className="hero-enter-subline max-w-[58ch] text-base leading-relaxed text-slate-400 sm:text-lg">
              Ingest suspicious messages, evasive URLs, QR codes, and documents.
              Cross-correlate forensic clues and calculate threats with audited
              intelligence.
            </p>

            <div className="hero-enter-cta flex flex-wrap items-center gap-4">
              <Link to={primaryHref}>
                <Button size="lg" className="px-7 font-semibold shadow-lg">
                  <span>
                    {isAuthenticated
                      ? "Start Investigation"
                      : "Open Free Investigation"}
                  </span>
                  <ArrowRight className="ml-1.5 h-4 w-4" />
                </Button>
              </Link>

              <Link
                to={secondaryHref}
                className="editorial-link group inline-flex items-center gap-2 rounded-full border border-white/10 px-5 py-2.5 text-sm font-semibold text-slate-300 transition-colors hover:border-white/25 hover:text-white"
              >
                <span>
                  {isAuthenticated ? "Go to Dashboard" : "Sign in as Analyst"}
                </span>
                <ArrowUpRight className="h-3.5 w-3.5 transition-transform duration-200 group-hover:-translate-y-0.5 group-hover:translate-x-0.5" />
              </Link>
            </div>

            <div className="grid max-w-lg grid-cols-3 divide-x divide-white/[0.07] overflow-hidden rounded-2xl border border-white/[0.07] bg-white/[0.02] backdrop-blur">
              <div className="px-4 py-4">
                <p className="font-mono text-xl font-bold text-white sm:text-2xl">
                  Open
                </p>
                <p className="mt-1 font-mono text-[10px] uppercase tracking-wider text-slate-500">
                  Source Principles
                </p>
              </div>
              <div className="px-4 py-4">
                <p className="font-mono text-xl font-bold text-cyan-400 sm:text-2xl">
                  Zero
                </p>
                <p className="mt-1 font-mono text-[10px] uppercase tracking-wider text-slate-500">
                  Plaintext Secrets
                </p>
              </div>
              <div className="px-4 py-4">
                <p className="font-mono text-xl font-bold text-emerald-400 sm:text-2xl">
                  Audited
                </p>
                <p className="mt-1 font-mono text-[10px] uppercase tracking-wider text-slate-500">
                  Epistemic Truth
                </p>
              </div>
            </div>
          </div>

          <div className="min-w-0 lg:col-span-5">
            <div className="relative">
              <div
                aria-hidden="true"
                className="absolute -inset-4 rounded-[36px] bg-gradient-to-br from-cyan-500/25 via-violet-500/10 to-transparent blur-2xl"
              />

              <div className="relative rounded-[28px] border border-white/[0.09] bg-white/[0.045] p-1.5 shadow-[0_30px_80px_-30px_rgba(0,0,0,0.95)] backdrop-blur-xl">
                <div className="space-y-4 rounded-[22px] bg-[#070a12]/95 p-5">
                  <div className="flex items-center justify-between border-b border-white/[0.07] pb-3.5">
                    <div className="flex items-center gap-2.5">
                      <span
                        aria-hidden="true"
                        className="h-2 w-2 animate-pulse rounded-full bg-rose-500 shadow-[0_0_10px_rgba(244,63,94,0.9)]"
                      />
                      <span className="font-mono text-[11px] font-bold tracking-[0.12em] text-slate-100">
                        CASE #PT-2026-9941
                      </span>
                    </div>
                    <Badge variant="CRITICAL">Critical Risk (92/100)</Badge>
                  </div>

                  <div className="space-y-2 rounded-2xl border border-white/[0.06] bg-black/40 p-3.5">
                    <div className="flex items-center justify-between">
                      <span className="font-mono text-[10px] font-semibold uppercase tracking-[0.14em] text-slate-500">
                        Ingested Evidence
                      </span>
                      <span className="font-mono text-[10px] font-semibold uppercase tracking-[0.14em] text-cyan-400">
                        SMS Payload
                      </span>
                    </div>
                    <p className="text-xs leading-relaxed text-slate-200">
                      &ldquo;Your electricity connection will be disconnected
                      tonight at 9:30 PM due to unpaid bill. Call 9876543210 or
                      visit bit.ly/power-bill-update immediately.&rdquo;
                    </p>
                  </div>

                  <div className="space-y-2">
                    <p className="font-mono text-[10px] font-semibold uppercase tracking-[0.14em] text-slate-500">
                      Extracted Indicators (3)
                    </p>

                    <div className="space-y-1.5">
                      {[
                        {
                          value: "bit.ly/power-bill-update",
                          badge: "VERIFIED",
                          label: "Redirect URL",
                          mono: true,
                        },
                        {
                          value: "+91 98765 43210",
                          badge: "INFERENCE",
                          label: "Prepaid SIM",
                          mono: true,
                        },
                        {
                          value: "Urgency: tonight at 9:30 PM",
                          badge: "CRITICAL",
                          label: "High Pressure",
                          mono: false,
                        },
                      ].map((row) => (
                        <div
                          key={row.value}
                          className="flex items-center justify-between gap-3 rounded-xl border border-white/[0.06] bg-white/[0.03] px-3 py-2.5 transition-colors hover:border-white/[0.14] hover:bg-white/[0.05]"
                        >
                          <span
                            className={[
                              "truncate text-[11px] text-slate-200",
                              row.mono ? "font-mono" : "",
                            ].join(" ")}
                          >
                            {row.value}
                          </span>
                          <Badge variant={row.badge}>{row.label}</Badge>
                        </div>
                      ))}
                    </div>
                  </div>

                  <div className="flex items-center justify-between border-t border-white/[0.07] pt-3">
                    <span className="flex items-center gap-1.5 font-mono text-[10px] font-semibold text-emerald-400">
                      <span className="h-1.5 w-1.5 rounded-full bg-emerald-400" />
                      Deterministic Link Proven
                    </span>
                    <span className="font-mono text-[10px] text-slate-500">
                      Verified at 01:04 UTC
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section
        data-scroll-reveal
        className="section-scroll-reveal border-t border-white/[0.06] py-12 sm:py-16 lg:py-20"
      >
        <SectionHeading
          eyebrow="Operational Workflow"
          title="How PhantomTrace works"
          description="From raw, untrusted communications to verified evidence dossiers in three audited stages."
        />

        <div className="mt-10 grid grid-cols-1 gap-5 sm:mt-12 md:grid-cols-3 lg:gap-6">
          {[
            {
              step: "01",
              icon: ScanSearch,
              title: "Ingest Evidence",
              description:
                "Submit suspicious text messages, email headers, raw URLs, payment requests, or file payloads (PNG, JPG, PDF) into an isolated case container with zero execution risk.",
            },
            {
              step: "02",
              icon: Network,
              title: "Deterministic Extraction",
              description:
                "Automated regex and pattern engines extract network indicators and urgency markers. Evasive redirect paths are unmasked safely against external threat intelligence.",
            },
            {
              step: "03",
              icon: Sparkles,
              title: "Epistemic Synthesis",
              description:
                "AI agents cross-correlate indicators into an interactive relationship graph, distinguishing verified facts from machine inferences with an audited risk score.",
            },
          ].map(({ step, icon: Icon, title, description }) => (
            <Card key={step}>
              <div className="flex items-start justify-between">
                <div className="flex h-11 w-11 items-center justify-center rounded-2xl border border-cyan-400/25 bg-cyan-500/10 text-cyan-300 shadow-[0_0_24px_-6px_rgba(59,130,246,0.7)] transition-transform duration-300 group-hover:scale-105">
                  <Icon className="h-5 w-5" />
                </div>
                <span className="font-mono text-3xl font-bold leading-none text-white/[0.07] transition-colors duration-300 group-hover:text-white/[0.14]">
                  {step}
                </span>
              </div>

              <h3 className="mt-5 text-base font-semibold text-slate-100 transition-colors group-hover:text-cyan-300">
                {title}
              </h3>
              <p className="mt-2 text-sm leading-relaxed text-slate-400">
                {description}
              </p>
            </Card>
          ))}
        </div>
      </section>

      <section
        data-scroll-reveal
        className="section-scroll-reveal border-t border-white/[0.06] py-12 sm:py-16 lg:py-20"
      >
        <SectionHeading
          eyebrow="System Specifications"
          title="Platform capabilities"
          description="Engineered to eliminate blind spots across multi-vector social engineering and financial fraud."
        />

        <div className="mt-10 grid grid-cols-1 gap-5 sm:mt-12 md:grid-cols-2 lg:gap-6">
          <Card>
            <div className="flex items-start justify-between">
              <div className="flex h-12 w-12 items-center justify-center rounded-2xl border border-cyan-500/30 bg-cyan-500/12 text-cyan-400 shadow-[0_0_24px_-6px_rgba(59,130,246,0.8)] transition-transform duration-300 group-hover:scale-105">
                <Database className="h-5 w-5" />
              </div>
              <ArrowUpRight className="h-4 w-4 text-slate-600 transition-all duration-300 group-hover:-translate-y-0.5 group-hover:translate-x-0.5 group-hover:text-cyan-400" />
            </div>

            <h3 className="mt-5 text-lg font-bold tracking-tight text-white">
              Multi-Vector Ingestion &amp; Parsing
            </h3>
            <p className="mt-2 text-sm leading-relaxed text-slate-400">
              Consolidate disparate clues from a single campaign. A scam may
              begin with a WhatsApp greeting, route through an evasive redirect
              URL, and terminate in an upfront payment request. PhantomTrace
              ingests text, images, and documents into a unified case container.
            </p>

            <div className="mt-5 flex flex-wrap gap-1.5">
              <Chip>SMS</Chip>
              <Chip>Email headers</Chip>
              <Chip>Raw URLs</Chip>
              <Chip>QR codes</Chip>
              <Chip>PDF / PNG / JPG</Chip>
            </div>
          </Card>

          <Card>
            <div className="flex items-start justify-between">
              <div className="flex h-12 w-12 items-center justify-center rounded-2xl border border-emerald-500/30 bg-emerald-500/12 text-emerald-400 shadow-[0_0_24px_-6px_rgba(16,185,129,0.8)] transition-transform duration-300 group-hover:scale-105">
                <ShieldCheck className="h-5 w-5" />
              </div>
              <ArrowUpRight className="h-4 w-4 text-slate-600 transition-all duration-300 group-hover:-translate-y-0.5 group-hover:translate-x-0.5 group-hover:text-emerald-400" />
            </div>

            <h3 className="mt-5 text-lg font-bold tracking-tight text-white">
              Epistemic Rigor &amp; Zero Hallucination
            </h3>
            <p className="mt-2 text-sm leading-relaxed text-slate-400">
              Every forensic signal is transparently audited. Verified facts
              extracted deterministically from raw evidence are strictly
              segregated from AI inferences and unknown intelligence gaps.
              Missing threat feeds are never falsely labeled clean.
            </p>

            <div className="mt-5 flex flex-wrap gap-1.5">
              <Chip>Verified</Chip>
              <Chip>Inference</Chip>
              <Chip>Unknown gaps</Chip>
              <Chip>Audited score</Chip>
            </div>
          </Card>
        </div>
      </section>

      <section
        data-scroll-reveal
        className="section-scroll-reveal border-t border-white/[0.06] py-12 sm:py-16 lg:py-20"
      >
        <div className="grid grid-cols-1 gap-8 lg:grid-cols-12 lg:gap-12">
          <div className="lg:col-span-4">
            <div className="lg:sticky lg:top-24">
              <SectionHeading
                align="left"
                eyebrow="Security Architecture"
                title="Evidence treated as hostile input"
                description="Digital evidence is isolated and audited across every system boundary."
              />
            </div>
          </div>

          <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:col-span-8 lg:gap-6">
            <Card>
              <div className="flex h-12 w-12 items-center justify-center rounded-2xl border border-amber-500/30 bg-amber-500/12 text-amber-400 shadow-[0_0_24px_-6px_rgba(245,158,11,0.8)] transition-transform duration-300 group-hover:scale-105">
                <Lock className="h-5 w-5" />
              </div>

              <h3 className="mt-5 text-base font-bold text-slate-100">
                Host Sandboxing &amp; Zero Plaintext
              </h3>
              <p className="mt-2 text-sm leading-relaxed text-slate-400">
                Files and payloads are stored strictly as static blobs. No
                uploaded script, binary, or executable is ever executed on host
                systems. Prompt inputs are structured with rigid delimiters to
                neutralize injection attacks.
              </p>

              <div className="mt-5 flex flex-wrap gap-1.5">
                <Chip>No execution</Chip>
                <Chip>Static blobs</Chip>
                <Chip>Delimited prompts</Chip>
              </div>
            </Card>

            <Card>
              <div className="flex h-12 w-12 items-center justify-center rounded-2xl border border-cyan-500/30 bg-cyan-500/12 text-cyan-400 shadow-[0_0_24px_-6px_rgba(59,130,246,0.8)] transition-transform duration-300 group-hover:scale-105">
                <Zap className="h-5 w-5" />
              </div>

              <h3 className="mt-5 text-base font-bold text-slate-100">
                Epistemic Bounds &amp; Tenant Isolation
              </h3>
              <p className="mt-2 text-sm leading-relaxed text-slate-400">
                Stateless JWT authentication backed by BCrypt hashing isolates
                tenant investigations. External threat intelligence providers
                remain server-side, with network failures surfaced honestly as
                unknown statuses rather than false negatives.
              </p>

              <div className="mt-5 flex flex-wrap gap-1.5">
                <Chip>JWT + BCrypt</Chip>
                <Chip>Tenant isolation</Chip>
                <Chip>Server-side feeds</Chip>
              </div>
            </Card>
          </div>
        </div>
      </section>

      <section
        data-scroll-reveal
        className="section-scroll-reveal border-t border-white/[0.06] py-12 sm:py-16 lg:py-20"
      >
        <div className="relative mx-auto max-w-4xl overflow-hidden rounded-[32px] border border-white/[0.09] bg-gradient-to-br from-cyan-600/15 via-teal-600/10 to-cyan-500/10 px-6 py-12 text-center sm:px-12 sm:py-16">
          <div
            aria-hidden="true"
            className="pointer-events-none absolute -top-32 left-1/2 h-72 w-[620px] -translate-x-1/2 rounded-full bg-cyan-500/25 blur-[110px]"
          />
          <div
            aria-hidden="true"
            className="pointer-events-none absolute -bottom-40 right-0 h-72 w-72 rounded-full bg-cyan-400/15 blur-[110px]"
          />

          <div className="relative z-10 mx-auto max-w-xl space-y-4">
            <h3 className="text-2xl font-extrabold leading-snug tracking-[-0.02em] text-white sm:text-4xl">
              Ready to unmask a digital threat?
            </h3>
            <p className="text-sm leading-relaxed text-slate-300 sm:text-base">
              Create an isolated investigation dossier in seconds and let the AI
              pipeline extract verified intelligence.
            </p>

            <div className="flex justify-center pt-3">
              <Link to={primaryHref}>
                <Button size="lg" className="px-9 font-semibold shadow-xl">
                  <span>
                    {isAuthenticated ? "Start Case Now" : "Create Free Account"}
                  </span>
                  <ArrowRight className="ml-2 h-4 w-4" />
                </Button>
              </Link>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
};

export default LandingPage;
