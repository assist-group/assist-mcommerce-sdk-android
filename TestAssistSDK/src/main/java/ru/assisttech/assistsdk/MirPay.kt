package ru.assisttech.assistsdk

import android.content.Context
import android.content.Intent
import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSSigner
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.util.Base64
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jose4j.jwk.JsonWebKeySet
import org.jose4j.jwk.PublicJsonWebKey
import ru.nspk.mirpay.sdk.api.payment.MirPayPaymentClientFactory
import ru.nspk.mirpay.sdk.api.payment.MirPayPaymentResultExtractor
import ru.nspk.mirpay.sdk.data.model.payment.MerchantToken
import ru.nspk.mirpay.sdk.data.model.payment.PaymentToken
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec

abstract class MirPay(
    jwksString: String,
    privateKey: String,
    private val merchantId: String,
) {

    private val signer: JWSSigner
    private var certs: List<Base64>

    init {
        val jwks = JsonWebKeySet(jwksString)
        val jwk = jwks.jsonWebKeys[0] as PublicJsonWebKey
        certs = jwk.certificateChain.map {
            Base64.encode(it.encoded)
        }
        val pk = jwk.privateKey ?: getPrivateKeyFromString(privateKey)
        signer = RSASSASigner(pk)
    }

    abstract fun doWithToken(result: String)

    abstract fun runMirPay(intent: Intent)

    private fun getPaymentToken(orderNumber: String, amount: Int): PaymentToken {
        val signedJWT = SignedJWT(
            JWSHeader.Builder(JWSAlgorithm.PS256)
                .type(JOSEObjectType.JWT)
                .x509CertChain(certs)
                .build(),
            JWTClaimsSet.Builder()
                .claim("iat", System.currentTimeMillis()) // Для MirPay нужны именно миллисекунды
                .issuer(getIssFromMerchantId(merchantId))
                .claim("orderId", orderNumber)
                .claim("sum", amount)
                .claim("cur", 643)
                .claim("media", "ISDK")
                .build()
        )
        signedJWT.sign(signer)
        return PaymentToken(signedJWT.serialize())
    }

    private fun getMerchantToken(): MerchantToken {
        val signedJWT = SignedJWT(
            JWSHeader.Builder(JWSAlgorithm.PS256)
                .type(JOSEObjectType.JWT)
                .x509CertChain(certs)
                .build(),
            JWTClaimsSet.Builder()
                .claim("iat", System.currentTimeMillis()) // Для MirPay нужны именно миллисекунды
                .issuer(getIssFromMerchantId(merchantId))
                .build()
        )
        signedJWT.sign(signer)
        return MerchantToken(signedJWT.serialize())
    }

    @Throws(NoSuchAlgorithmException::class, InvalidKeySpecException::class)
    private fun getPrivateKeyFromString(pkcs8Pem: String): PrivateKey {
        var pkcs8Pem = pkcs8Pem
        pkcs8Pem = pkcs8Pem.replace("-----BEGIN RSA PRIVATE KEY-----", "")
        pkcs8Pem = pkcs8Pem.replace("-----END RSA PRIVATE KEY-----", "")
        pkcs8Pem = pkcs8Pem.replace("\\s+".toRegex(), "")
        val pkcs8EncodedBytes: ByteArray = org.bouncycastle.util.encoders.Base64.decode(pkcs8Pem)
        val keySpec = PKCS8EncodedKeySpec(pkcs8EncodedBytes)
        val kf: KeyFactory = KeyFactory.getInstance("RSA")
        return kf.generatePrivate(keySpec)
    }

    private fun getIssFromMerchantId(merchantId: String): String {
        var result = merchantId
        do {
            result = "0$result"
        } while (result.length != 15)
        return result
    }

    fun pay(context: Context, orderNumber: String, amount: Int) {
        val merchantToken = getMerchantToken()
        val paymentToken = getPaymentToken(orderNumber, amount)
        GlobalScope.launch {
            val client = MirPayPaymentClientFactory.create(context, merchantToken)
            val intent = client.createPaymentIntent(paymentToken)
            client.disconnect()
            runMirPay(intent)
        }
    }

    fun setResult(intent: Intent) {
        doWithToken(MirPayPaymentResultExtractor.extract(intent).value)
    }
}