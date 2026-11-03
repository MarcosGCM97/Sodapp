package com.example.sodappcomposse.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.sodappcomposse.R

// Atkinson Hyperlegible (SIL OFL 1.1, license in assets/licenses): every letter and digit is
// distinct (1 l I, 0 O), so prices and names read well outdoors. Bundled, so it works offline.
// It only ships Regular and Bold: Medium resolves to Regular and SemiBold to Bold.
val AtkinsonHyperlegible = FontFamily(
    Font(R.font.atkinson_hyperlegible_regular, FontWeight.Normal),
    Font(R.font.atkinson_hyperlegible_bold, FontWeight.Bold)
)

private fun TextStyle.conFuente() = copy(fontFamily = AtkinsonHyperlegible)

// Material's default sizes and spacing, with the app font in every style
val Typography = Typography().let { base ->
    Typography(
        displayLarge = base.displayLarge.conFuente(),
        displayMedium = base.displayMedium.conFuente(),
        displaySmall = base.displaySmall.conFuente(),
        headlineLarge = base.headlineLarge.conFuente(),
        headlineMedium = base.headlineMedium.conFuente(),
        headlineSmall = base.headlineSmall.conFuente(),
        titleLarge = base.titleLarge.conFuente(),
        titleMedium = base.titleMedium.conFuente(),
        titleSmall = base.titleSmall.conFuente(),
        bodyLarge = base.bodyLarge.conFuente(),
        bodyMedium = base.bodyMedium.conFuente(),
        bodySmall = base.bodySmall.conFuente(),
        labelLarge = base.labelLarge.conFuente(),
        labelMedium = base.labelMedium.conFuente(),
        labelSmall = base.labelSmall.conFuente()
    )
}
