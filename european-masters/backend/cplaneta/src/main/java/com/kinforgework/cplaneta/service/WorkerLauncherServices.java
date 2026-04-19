package com.kinforgework.cplaneta.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WorkerLauncherServices {

    @Value("${app.podman-api-host}")
    private String PODMAN_API_HOST;

    public void iniciarWorker() {
        // Si no hay variable, por defecto intenta el socket de Linux
        String podmanHost = PODMAN_API_HOST;
        if (podmanHost == null) {
            podmanHost = "http://localhost/v4.0.0/libpod";
        }

        try {
            ProcessBuilder pb;
            if (podmanHost.startsWith("http")) {
                // Modo TCP (Para tu Windows)
                pb = new ProcessBuilder("curl", "-X", "POST", podmanHost + "/containers/em-worker/start");
            } else {
                // Modo Socket (Para el Servidor Linux)
                pb = new ProcessBuilder("curl", "--unix-socket", podmanHost, "-X", "POST", "http://localhost/v4.0.0/libpod/containers/em-worker/start");
            }

            pb.start();
            log.info("Señal enviada a Podman usando: {}", podmanHost);
        } catch (Exception e) {
            log.error("Error al contactar con Podman: " + e.getMessage());
        }
    }
}
