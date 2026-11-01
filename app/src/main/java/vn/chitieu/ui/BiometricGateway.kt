package vn.chitieu.ui

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

enum class BiometricOutcome { SUCCESS, USE_PIN, UNAVAILABLE, LOCKED_OUT }

/** A single activity-owned prompt; no authentication callbacks retain a disposed screen. */
interface BiometricGateway {
    val inProgress: Boolean
    fun available(): Boolean
    fun bind(result: (BiometricOutcome) -> Unit): () -> Unit
    fun authenticate()
    fun cancel()
}

val LocalBiometricGateway = staticCompositionLocalOf<BiometricGateway?> { null }

/** Survives rotation, but never persists an authentication grant across process death. */
class BiometricSession : ViewModel() {
    var inProgress = false
    var pending: BiometricOutcome? = null
}

class AndroidBiometricGateway(private val activity: FragmentActivity) : BiometricGateway {
    private val session = ViewModelProvider(activity)[BiometricSession::class.java]
    private var listener: ((BiometricOutcome) -> Unit)? = null
    override val inProgress get() = session.inProgress
    // Constructed in Activity.onCreate, reattaching callbacks to AndroidX's retained prompt.
    private val prompt = BiometricPrompt(activity, ContextCompat.getMainExecutor(activity),
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                if (session.inProgress) finish(BiometricOutcome.SUCCESS)
            }
            override fun onAuthenticationError(code: Int, message: CharSequence) {
                if (!session.inProgress) return
                finish(when (code) {
                    BiometricPrompt.ERROR_LOCKOUT, BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> BiometricOutcome.LOCKED_OUT
                    BiometricPrompt.ERROR_HW_UNAVAILABLE, BiometricPrompt.ERROR_HW_NOT_PRESENT,
                    BiometricPrompt.ERROR_NO_BIOMETRICS -> BiometricOutcome.UNAVAILABLE
                    else -> BiometricOutcome.USE_PIN
                })
            }
            // An unrecognised finger/face leaves retries and feedback to the system prompt.
        })

    override fun available() = runCatching { BiometricManager.from(activity).canAuthenticate(
        BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS }.getOrDefault(false)

    override fun bind(result: (BiometricOutcome) -> Unit): () -> Unit {
        listener = result
        session.pending?.let { session.pending = null; result(it) }
        return { if (listener === result) listener = null }
    }

    override fun authenticate() {
        if (session.inProgress) return
        if (!available()) { finish(BiometricOutcome.UNAVAILABLE); return }
        session.inProgress = true
        try {
            prompt.authenticate(BiometricPrompt.PromptInfo.Builder()
                .setTitle("Mở khoá ChiTieu")
                .setSubtitle("Xác nhận bằng vân tay hoặc khuôn mặt đã đăng ký")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .setNegativeButtonText("Dùng PIN").build())
        } catch (_: Exception) { finish(BiometricOutcome.UNAVAILABLE) }
    }

    private fun finish(outcome: BiometricOutcome) {
        session.inProgress = false
        val callback = listener
        if (callback == null) session.pending = outcome else callback(outcome)
    }

    override fun cancel() {
        val wasActive = session.inProgress
        session.inProgress = false
        session.pending = null
        if (wasActive) prompt.cancelAuthentication()
    }
}
