package ru.assisttech.sdk.network;

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Random;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class SSLContextFactory {

    private final String TAG = "SSL_CONTEXT_FACTORY";
    private Context ctx;
    private KeyStore keyStore;
    private Certificate caAssist;
    private Certificate caTPaysecure;
    private Certificate caBel;
    private Certificate caPaysecure;

    public SSLContextFactory(Context context) throws SSLContextFactoryException {
        ctx = context;
        loadCertificates();
        createKeystore();
    }

    void loadCertificates() throws SSLContextFactoryException {
        // Load CAs from an InputStream
        // (could be from a resource or ByteArrayInputStream or ...)
        InputStream caInputAssist = null;
        InputStream caInputTPaysecure = null;
        InputStream caInputBel = null;
        InputStream caInputPaysecure = null;
        try {
            try {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");

                try {
                    caInputAssist = new BufferedInputStream(ctx.getResources().getAssets().open("assist.ru.crt"));
                    caInputTPaysecure = new BufferedInputStream(ctx.getResources().getAssets().open("t.paysecure.ru.crt"));
                    caInputBel = new BufferedInputStream(ctx.getResources().getAssets().open("paysec.by.crt"));
                    caInputPaysecure = new BufferedInputStream(ctx.getResources().getAssets().open("paysecure.ru.crt"));

                    caAssist = cf.generateCertificate(caInputAssist);
                    Log.d(TAG, "caAssist=" + ((X509Certificate) caAssist).getSubjectDN());

                    caTPaysecure = cf.generateCertificate(caInputTPaysecure);
                    Log.d(TAG, "caTPaysecure=" + ((X509Certificate) caTPaysecure).getSubjectDN());

                    caBel = cf.generateCertificate(caInputBel);
                    Log.d(TAG, "caBel=" + ((X509Certificate) caBel).getSubjectDN());

                    caPaysecure = cf.generateCertificate(caInputPaysecure);
                    Log.d(TAG, "caPaysecure=" + ((X509Certificate) caPaysecure).getSubjectDN());
                } finally {
                    if (caInputAssist != null) caInputAssist.close();
                    if (caInputTPaysecure != null) caInputTPaysecure.close();
                    if (caInputBel != null) caInputBel.close();
                    if (caInputPaysecure != null) caInputPaysecure.close();
                }

            } catch (CertificateException e) {
                throw new SSLContextFactoryException("Error while loading certificate factory.", e);
            }

        } catch (IOException e) {
            throw new SSLContextFactoryException("Error while loading certificates.", e);
        }
    }

    void createKeystore() throws SSLContextFactoryException {
        // Create a KeyStore containing our trusted CAs
        String keyStoreType = KeyStore.getDefaultType();
        try {
            keyStore = KeyStore.getInstance(keyStoreType);
        } catch (KeyStoreException e) {
            throw new SSLContextFactoryException("Error while loading instance of keystore.", e);
        }
        try {
            keyStore.load(null, null);
        } catch (Exception e) {
            throw new SSLContextFactoryException("Error while loading empty keystore.", e);
        }
        try {
            keyStore.setCertificateEntry("assist.ru", caAssist);
            keyStore.setCertificateEntry("t.paysecure.ru", caTPaysecure);
            keyStore.setCertificateEntry("paysec.by", caBel);
            keyStore.setCertificateEntry("paysecure.ru", caPaysecure);
        } catch (KeyStoreException e) {
            throw new SSLContextFactoryException("Error while setting certificates to keystore.", e);
        }
    }

    public boolean addCertificate(Certificate cert) {
        try {
            String certName = "cert_" + String.valueOf(new Random().nextInt());
            keyStore.setCertificateEntry(certName, cert);
            return true;

        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        return false;
    }

    public SSLContext createSSLContext() throws SSLContextFactoryException {

        SSLContext context = null;
        // Create a TrustManager that trusts the CAs in our KeyStore
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();

        try {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);

            tmf.init(keyStore);
            // Create an SSLContext that uses our TrustManager
            context = SSLContext.getInstance("TLS");
            context.init(null, tmf.getTrustManagers(), null);

        } catch (Exception e) {
            throw new SSLContextFactoryException("Error while creating SSL context.", e);
        }
        return context;
    }
}
