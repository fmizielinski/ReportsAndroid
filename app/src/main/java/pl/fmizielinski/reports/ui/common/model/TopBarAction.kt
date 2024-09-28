package pl.fmizielinski.reports.ui.common.model

import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import pl.fmizielinski.reports.R

enum class TopBarAction(
    @StringRes val nameResId: Int,
    @DrawableRes val iconResId: Int,
    val requirePermissions: List<String> = emptyList(),
) {

    REGISTER(
        nameResId = R.string.common_button_register,
        iconResId = R.drawable.ic_person_add_24dp,
    ),
    PHOTO(
        nameResId = R.string.common_button_takePhoto,
        iconResId = R.drawable.ic_add_a_photo_24dp,
        requirePermission = android.Manifest.permission.CAMERA,
    ),
    FILES(
        nameResId = R.string.common_button_addFile,
        iconResId = R.drawable.ic_folder_24dp,
        requirePermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            listOf(
                android.Manifest.permission.READ_MEDIA_IMAGES,
                android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(android.Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            listOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        },
    ),
    LOGOUT(
        nameResId = R.string.common_button_logout,
        iconResId = R.drawable.ic_logout_24dp,
    );

    constructor(
        nameResId: Int,
        iconResId: Int,
        requirePermission: String?,
    ) : this(nameResId, iconResId, requirePermissions = listOfNotNull(requirePermission))
}
