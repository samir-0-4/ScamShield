package com.phantomtrace.enums;

public enum EvidenceType {
    TEXT_MESSAGE,
    URL,
    EMAIL,
    IMAGE,
    PDF,
    /** Legacy values retained so existing rows remain readable across the migration. */
    WHATSAPP_MESSAGE,
    PAYMENT_SCREENSHOT,
    QR_CODE,
    PDF_DOCUMENT,
    RECRUITMENT_MESSAGE,
    OTHER
}
