package pl.fmizielinski.reports.android

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContract

class CustomActivityResultContracts private constructor() {

    class OpenAppSettings : ActivityResultContract<Unit, Boolean>() {
        override fun createIntent(context: Context, input: Unit): Intent {
            return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Boolean =
            resultCode == Activity.RESULT_OK
    }
}
