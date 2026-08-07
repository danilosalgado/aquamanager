package com.aquamanager.modules.auth.application.dto;

public record Setup2FaResponse(String secret, String qrCodeDataUri) {
}
