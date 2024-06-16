package im.angry.openeuicc.util

import android.util.Log
import im.angry.openeuicc.core.EuiccChannel
import net.typeblog.lpac_jni.LocalProfileAssistant
import net.typeblog.lpac_jni.LocalProfileInfo

val LocalProfileInfo.displayName: String
    get() = nickName.ifEmpty { name }


val LocalProfileInfo.isEnabled: Boolean
    get() = state == LocalProfileInfo.State.Enabled

val List<LocalProfileInfo>.operational: List<LocalProfileInfo>
    get() = filter {
        it.profileClass == LocalProfileInfo.Clazz.Operational
    }

val List<EuiccChannel>.hasMultipleChips: Boolean
    get() = distinctBy { it.slotId }.size > 1

/**
 * Disable the current active profile if any. If refresh is true, also cause a refresh command.
 * See EuiccManager.waitForReconnect()
 */
fun LocalProfileAssistant.disableActiveProfile(refresh: Boolean): Boolean =
    profiles.find { it.isEnabled }?.let {
        Log.i("LPAUtils", "Disabling active profile ${it.iccid}")
        disableProfile(it.iccid, refresh)
    } ?: true

/**
 * Disable the active profile, return a lambda that reverts this action when called.
 * If refreshOnDisable is true, also cause a eUICC refresh command. Note that refreshing
 * will disconnect the eUICC and might need some time before being operational again.
 * See EuiccManager.waitForReconnect()
 */
fun LocalProfileAssistant.disableActiveProfileWithUndo(refreshOnDisable: Boolean): () -> Unit =
    profiles.find { it.isEnabled }?.let {
        disableProfile(it.iccid, refreshOnDisable)
        return { enableProfile(it.iccid) }
    } ?: { }