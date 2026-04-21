package com.kinforgework.cplaneta.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WorkerLauncherServices {

    @Value("${app.podman-api-host}")
    private String PODMAN_API_HOST;

    public void invoke() {
        String podmanHost = PODMAN_API_HOST;
        try {
            ProcessBuilder pb;
            pb = new ProcessBuilder(
                    "curl", "--unix-socket", podmanHost, "-X", "POST",
                    "http://localhost/v4.0.0/libpod/containers/em-worker/start"
            );
            pb.start();
            log.info("Señal enviada a Podman usando: {}", podmanHost);
        } catch (Exception e) {
            log.error("Error al contactar con Podman: " + e.getMessage());
        }
    }
}
