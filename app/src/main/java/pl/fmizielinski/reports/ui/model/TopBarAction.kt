package pl.fmizielinski.reports.ui.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import pl.fmizielinski.reports.R

enum class TopBarAction(
    @StringRes val nameResId: Int,
    @DrawableRes val iconResId: Int,
    val requirePermission: String? = null,
) {
    REGISTER(
        nameResId = R.string.common_button_register,
        iconResId = R.drawable.ic_person_add_24dp,
    ),
    PHOTO(
        nameResId = R.string.common_button_register,
        iconResId = R.drawable.ic_add_a_photo_24dp,
        requirePermission = android.Manifest.permission.CAMERA,
    ),
}
