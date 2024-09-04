package pl.fmizielinski.reports.ui.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import pl.fmizielinski.reports.R

enum class TopBarAction(
    @StringRes val nameResId: Int,
    @DrawableRes val iconResId: Int,
) {
    REGISTER(R.string.common_button_register, R.drawable.ic_person_add_24dp),
    PHOTO(R.string.common_button_register, R.drawable.ic_add_a_photo_24dp),
}
