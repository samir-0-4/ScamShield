package com.phantomtrace.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "phantomtrace.risk")
public class RiskAssessmentProperties {
    private int verifiedMaliciousWeight = 40;
    private int credentialOtpWeight = 25;
    private int upfrontPaymentWeight = 20;
    private int impersonationWeight = 15;
    private int urgencyWeight = 10;
    private int unusualUrlWeight = 10;

    public int getVerifiedMaliciousWeight() { return verifiedMaliciousWeight; }
    public void setVerifiedMaliciousWeight(int value) { verifiedMaliciousWeight = value; }
    public int getCredentialOtpWeight() { return credentialOtpWeight; }
    public void setCredentialOtpWeight(int value) { credentialOtpWeight = value; }
    public int getUpfrontPaymentWeight() { return upfrontPaymentWeight; }
    public void setUpfrontPaymentWeight(int value) { upfrontPaymentWeight = value; }
    public int getImpersonationWeight() { return impersonationWeight; }
    public void setImpersonationWeight(int value) { impersonationWeight = value; }
    public int getUrgencyWeight() { return urgencyWeight; }
    public void setUrgencyWeight(int value) { urgencyWeight = value; }
    public int getUnusualUrlWeight() { return unusualUrlWeight; }
    public void setUnusualUrlWeight(int value) { unusualUrlWeight = value; }
}
