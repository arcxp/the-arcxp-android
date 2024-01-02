package com.arcxp.thearcxp.utils

import androidx.annotation.StringRes
import com.arcxp.thearcxp.R

enum class AnsTypes(val type: String) {
    VIDEO("video"),
    GALLERY("gallery"),
    STORY("story"),
    LINK("interstitial_link"),
    IMAGE("image"),
    TEXT("text");
}

enum class PasswordRequirement(
    @StringRes val label: Int
) {
    UPPERCASE_LETTER(R.string.password_requirement_uppercase),
    LOWERCASE_LETTER(R.string.password_requirement_lowercase),
    SPECIAL_CHARACTER(R.string.password_requirement_special),
    NUMBER(R.string.password_requirement_number),
    SIX_CHARACTERS(R.string.password_requirement_characters)
}
