# 3–5 Minute Demo Script

Opening:

> People receive suspicious messages, payment requests and links every day. Most
> tools examine one link at a time. ScamShield investigates the entire situation
> by connecting the clues.

1. Open the landing page and introduce multi-evidence investigation.
2. Register or log in with the synthetic demo account.
3. Create **Remote Job Offer Investigation** in the **Job Scam** category.
4. Submit the synthetic job-offer message and the reserved `demo.invalid` URL.
5. Open results and show deterministic extraction: payment language, urgency,
   URL, and domain indicators.
6. Run analysis when Groq is configured. If unavailable, point out the explicit
   unavailable state rather than inventing a finding.
7. Show threat intelligence provider status; unavailable providers are not shown
   as clean.
8. Show the risk assessment and explain score, category, confidence, and missing
   information separately.
9. Open the relationship graph. Click the message node and the `contains URL`
   edge to show provenance and explanation. This is the signature feature.
10. Generate the report and use **Print / Save PDF**.

Close by emphasizing verified, inferred, and unknown labels and the limitation
that heuristic risk is not a proven fraud probability.
