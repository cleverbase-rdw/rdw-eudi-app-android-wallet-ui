/*
 * Copyright (c) 2023 European Commission
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work
 * except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied. See the Licence for the specific language
 * governing permissions and limitations under the Licence.
 */

package eu.europa.ec.corelogic.config

import android.content.Context
import eu.europa.ec.corelogic.BuildConfig
import eu.europa.ec.corelogic.config.certificate.CertificateManager
import eu.europa.ec.eudi.wallet.EudiWalletConfig
import eu.europa.ec.eudi.wallet.issue.openid4vci.OpenId4VciManager
import eu.europa.ec.eudi.wallet.transfer.openId4vp.ClientIdScheme
import eu.europa.ec.eudi.wallet.transfer.openId4vp.EncryptionAlgorithm
import eu.europa.ec.eudi.wallet.transfer.openId4vp.EncryptionMethod
import eu.europa.ec.eudi.wallet.transfer.openId4vp.Format
import eu.europa.ec.resourceslogic.R
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

internal class WalletCoreConfigImpl(
    private val context: Context
) : WalletCoreConfig {

    private companion object {
        const val VCI_ISSUER_URL = "https://mdlissuer.azurewebsites.net" // "https://issuer.eudiw.dev"
        const val VCI_CLIENT_ID = "wallet-dev"
        const val AUTHENTICATION_REQUIRED = false
    }

    private var _config: EudiWalletConfig? = null

    override val config: EudiWalletConfig
        get() {
            if (_config == null) {
                _config = EudiWalletConfig {
                    configureDocumentKeyCreation(
                        userAuthenticationRequired = AUTHENTICATION_REQUIRED,
                        userAuthenticationTimeout = 30_000L,
                        useStrongBoxForKeys = true
                    )
                    configureOpenId4Vp {
                        withEncryptionAlgorithms(listOf(EncryptionAlgorithm.ECDH_ES))
                        withEncryptionMethods(
                            listOf(
                                EncryptionMethod.A128CBC_HS256,
                                EncryptionMethod.A256GCM
                            )
                        )

                        withClientIdSchemes(
                            listOf(ClientIdScheme.X509SanDns)
                        )
                        withSchemes(
                            listOf(
                                BuildConfig.OPENID4VP_SCHEME,
                                BuildConfig.EUDI_OPENID4VP_SCHEME,
                                BuildConfig.MDOC_OPENID4VP_SCHEME
                            )
                        )
                        withFormats(
                            Format.MsoMdoc, Format.SdJwtVc.ES256
                        )
                    }

                    configureOpenId4Vci {
                        withIssuerUrl(
                            issuerUrl = context.getSharedPreferences(
                                "IssuerCrudPrefs",
                                Context.MODE_PRIVATE
                            ).getString("selected_issuer", null) ?: VCI_ISSUER_URL
                        )
                        withClientId(clientId = VCI_CLIENT_ID)
                        withAuthFlowRedirectionURI(BuildConfig.ISSUE_AUTHORIZATION_DEEPLINK)
                        withParUsage(OpenId4VciManager.Config.ParUsage.IF_SUPPORTED)
                        withUseDPoPIfSupported(true)
                    }

                    configureReaderTrustStore(
                        getCombinedCertificates(context, getAllCertificates().toIntArray())
                    )
                }
            }
            return _config!!
        }
}

fun getAllCertificates(): List<Int> {
    return R.raw::class.java.fields.mapNotNull { field ->
        try {
            field.getInt(null)
        } catch (e: IllegalAccessException) {
            null // TODO add logging
        }
    }
}

fun getCombinedCertificates(context: Context, resArray: IntArray): List<X509Certificate> {
    return resArray.map {
        context.resources.openRawResource(it).use {
            CertificateFactory.getInstance("X509")
                .generateCertificate(it) as X509Certificate
        }
    } + CertificateManager(context).getStoredCertificates()
}
