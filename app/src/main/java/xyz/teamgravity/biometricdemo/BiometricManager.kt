package xyz.teamgravity.biometricdemo

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

private typealias AndroidBiometricManager = androidx.biometric.BiometricManager
private typealias AndroidAuthenticators = androidx.biometric.BiometricManager.Authenticators

class BiometricManager {

    private val _result = Channel<Result>()
    val result: Flow<Result> = _result.receiveAsFlow()

    ///////////////////////////////////////////////////////////////////////////
    // API
    ///////////////////////////////////////////////////////////////////////////

    fun authenticators(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) AndroidAuthenticators.BIOMETRIC_STRONG or AndroidAuthenticators.DEVICE_CREDENTIAL
        else AndroidAuthenticators.BIOMETRIC_STRONG
    }

    fun show(activity: AppCompatActivity) {
        val manager = AndroidBiometricManager.from(activity)
        val authenticators = authenticators()

        when (manager.canAuthenticate(authenticators)) {
            AndroidBiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                _result.trySend(Result.HardwareNotSupported)
                return
            }

            AndroidBiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                _result.trySend(Result.HardwareUnavailable)
                return
            }

            AndroidBiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                _result.trySend(Result.AuthenticationNotSet)
                return
            }

            else -> Unit
        }

        val prompt = BiometricPrompt.PromptInfo.Builder()
            .setTitle(activity.getString(R.string.biometric_title))
            .setSubtitle(activity.getString(R.string.biometric_subtitle))
            .setDescription(activity.getString(R.string.biometric_description))
            .setAllowedAuthenticators(authenticators)
            .build()

        BiometricPrompt(
            activity,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    _result.trySend(Result.AuthenticationError(errString.toString()))
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    _result.trySend(Result.AuthenticationSuccess)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    _result.trySend(Result.AuthenticationFailed)
                }
            }
        ).authenticate(prompt)
    }

    ///////////////////////////////////////////////////////////////////////////
    // MISC
    ///////////////////////////////////////////////////////////////////////////

    sealed interface Result {
        data object HardwareNotSupported : Result
        data object HardwareUnavailable : Result
        data class AuthenticationError(val message: String) : Result
        data object AuthenticationFailed : Result
        data object AuthenticationSuccess : Result
        data object AuthenticationNotSet : Result
    }
}