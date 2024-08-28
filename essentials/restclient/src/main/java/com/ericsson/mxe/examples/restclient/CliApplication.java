package com.ericsson.mxe.examples.restclient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.DataInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;


@SpringBootApplication
public class CliApplication implements CommandLineRunner {

    private static final Logger logger = LogManager.getLogger(CliApplication.class);

    @Value("security.disableSslCertVerification")
    private static boolean disableSslCertVerification;

    @Value("classpath:elephant.jpg")
    private Resource img;

    @Autowired
    private ModelEndpoint modelEndpoint;

    public static void main(String[] args) {
        disableSSLCertificate();
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_GLOBAL);
        SpringApplication app = new SpringApplication(CliApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.run(args);
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("App has started up...");
    }

    @Scheduled(fixedRate = 45000)
    public void predict() throws IOException {
        logger.info("Sending data for prediction.");
        modelEndpoint.send(readData());
        logger.info("Data sent.");
    }

    public byte[] readData() throws IOException {
        byte[] data = new byte[(int) img.contentLength()];
        try (DataInputStream in = new DataInputStream(img.getInputStream())) {
            in.readFully(data);
            return data;
        }
    }

    private static void disableSSLCertificate() {
        if(!disableSslCertVerification) {
            return;
        }
        TrustManager[] trustManagers = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
        };

        try {
            final SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustManagers, null);
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            logger.error(e);
        }
    }
}
