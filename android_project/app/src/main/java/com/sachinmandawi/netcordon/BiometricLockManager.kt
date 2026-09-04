package com.sachinmandawi.netcordon

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BiometricLockManager {

    private const val TAG = "BiometricLockManager"

    /**
     * Checks if biometric hardware is present and enrolled or device credential (PIN/pattern) exists.
     */
    fun canAuthenticate(context: Context): Boolean {
        return try {
            val biometricManager = BiometricManager.from(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
                biometricManager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
            } else {
                val isBiometricSuccess = @Suppress("DEPRECATION") biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS
                val km = context.getSystemService(Context.KEYGUARD_SERVICE) as? android.app.KeyguardManager
                val isDeviceSecure = km?.isDeviceSecure == true
                isBiometricSuccess || isDeviceSecure
            }
        } catch (e: Exception) {
            Log.w(TAG, "canAuthenticate check failed: ${e.message}")
            false
        }
    }

    /**
     * Triggers BiometricPrompt supporting Biometrics (Fingerprint/Face) and Device PIN/Pattern.
     */
    fun authenticate(
        activity: FragmentActivity,
        title: String = "NetCordon Security Shield",
        subtitle: String = "Authenticate to unlock firewall controls",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            if (activity.isFinishing || activity.isDestroyed) return

            val executor = ContextCompat.getMainExecutor(activity)
            val callback = object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    try {
                        onSuccess()
                    } catch (e: Exception) {
                        Log.e(TAG, "onSuccess callback error", e)
                    }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    try {
                        onError(errString.toString())
                    } catch (e: Exception) {
                        Log.e(TAG, "onError callback error", e)
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // Fingerprint not recognized; system keeps prompt open
                }
            }

            val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                promptInfoBuilder.setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or
                            BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
            } else {
                @Suppress("DEPRECATION")
                promptInfoBuilder.setDeviceCredentialAllowed(true)
            }

            val promptInfo = promptInfoBuilder.build()
            val biometricPrompt = BiometricPrompt(activity, executor, callback)
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initiate biometric prompt", e)
            onError(e.localizedMessage ?: "Biometric prompt error")
        }
    }
}
