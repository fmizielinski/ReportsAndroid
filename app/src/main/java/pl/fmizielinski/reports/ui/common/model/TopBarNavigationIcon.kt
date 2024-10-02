package pl.fmizielinski.reports.ui.common.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import pl.fmizielinski.reports.R

enum class TopBarNavigationIcon(
    @StringRes val nameResId: Int,
    @DrawableRes val iconResId: Int,
) {

    BACK(
        nameResId = R.string.common_button_back,
        iconResId = R.drawable.ic_arrow_back_24dp,
    ),
    CLOSE(
        nameResId = R.string.common_button_close,
        iconResId = R.drawable.ic_close_24dp,
    );
}
