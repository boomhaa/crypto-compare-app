package com.example.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object Dimensions {

    // Spacing System (base 4dp)
    object Spacing {
        val xxs: Dp = 4.dp
        val xs: Dp = 8.dp
        val sm: Dp = 12.dp
        val md: Dp = 16.dp
        val lg: Dp = 24.dp
        val xl: Dp = 32.dp
        val xxl: Dp = 48.dp
        val xxxl: Dp = 64.dp
    }

    // Padding
    object Padding {
        // Screen padding
        val screenHorizontal: Dp = 24.dp
        val screenVertical: Dp = 16.dp
        val screen: Dp = 24.dp

        // Card padding
        val cardSmall: Dp = 12.dp
        val cardMedium: Dp = 16.dp
        val cardLarge: Dp = 20.dp

        // Button padding
        val buttonHorizontal: Dp = 24.dp
        val buttonVertical: Dp = 16.dp

        // Input padding
        val inputHorizontal: Dp = 16.dp
        val inputVertical: Dp = 16.dp

        // List item padding
        val listItemHorizontal: Dp = 16.dp
        val listItemVertical: Dp = 12.dp
    }

    // Border Radius
    object Radius {
        val xs: Dp = 4.dp
        val sm: Dp = 8.dp
        val md: Dp = 12.dp
        val lg: Dp = 16.dp
        val xl: Dp = 20.dp
        val xxl: Dp = 28.dp
        val xxxl: Dp = 40.dp
        val full: Dp = 9999.dp // круглый

        // Specific elements
        val button: Dp = 16.dp
        val input: Dp = 12.dp
        val card: Dp = 16.dp
        val cardLarge: Dp = 20.dp
        val badge: Dp = 6.dp
        val icon: Dp = 12.dp
    }

    // Component Heights
    object Height {
        val button: Dp = 56.dp
        val buttonSmall: Dp = 44.dp
        val buttonLarge: Dp = 64.dp

        val input: Dp = 56.dp
        val inputSmall: Dp = 44.dp

        val topBar: Dp = 60.dp
        val bottomNav: Dp = 60.dp
        val tabBar: Dp = 48.dp

        val listItem: Dp = 72.dp
        val listItemSmall: Dp = 56.dp
        val listItemLarge: Dp = 88.dp

        val divider: Dp = 1.dp
    }

    // Component Widths
    object Width {
        val buttonMin: Dp = 120.dp
        val dialogMin: Dp = 280.dp
        val dialogMax: Dp = 560.dp
    }

    // Icon Sizes
    object IconSize {
        val xs: Dp = 12.dp
        val sm: Dp = 16.dp
        val md: Dp = 24.dp
        val lg: Dp = 32.dp
        val xl: Dp = 48.dp
        val xxl: Dp = 64.dp

        // Specific use cases
        val button: Dp = 20.dp
        val input: Dp = 20.dp
        val navigation: Dp = 24.dp
        val crypto: Dp = 48.dp
        val cryptoLarge: Dp = 64.dp
    }

    // Avatar Sizes
    object Avatar {
        val xs: Dp = 24.dp
        val sm: Dp = 32.dp
        val md: Dp = 40.dp
        val lg: Dp = 56.dp
        val xl: Dp = 80.dp
        val xxl: Dp = 120.dp
    }

    // Elevation (для Card, Button shadows)
    object Elevation {
        val none: Dp = 0.dp
        val xs: Dp = 2.dp
        val sm: Dp = 4.dp
        val md: Dp = 8.dp
        val lg: Dp = 12.dp
        val xl: Dp = 16.dp
        val xxl: Dp = 24.dp
    }

    // Border Width
    object Border {
        val thin: Dp = 1.dp
        val medium: Dp = 2.dp
        val thick: Dp = 4.dp

        // Specific elements
        val input: Dp = 1.dp
        val inputFocused: Dp = 2.dp
        val card: Dp = 1.dp
        val divider: Dp = 1.dp
    }

    // Gaps (для Row, Column)
    object Gap {
        val xs: Dp = 4.dp
        val sm: Dp = 8.dp
        val md: Dp = 12.dp
        val lg: Dp = 16.dp
        val xl: Dp = 24.dp

        // Common use cases
        val listItems: Dp = 12.dp
        val gridItems: Dp = 12.dp
        val cardContent: Dp = 12.dp
        val formFields: Dp = 16.dp
    }

    // Min Touch Target Size (Accessibility)
    object TouchTarget {
        val min: Dp = 48.dp
        val recommended: Dp = 56.dp
    }

    // Specific Component Sizes
    object Crypto {
        // Crypto Icon размеры
        val iconSmall: Dp = 32.dp
        val iconMedium: Dp = 48.dp
        val iconLarge: Dp = 64.dp

        // Card heights
        val cardHeight: Dp = 72.dp
        val cardHeightExpanded: Dp = 120.dp

        // Chart heights
        val chartSmall: Dp = 100.dp
        val chartMedium: Dp = 200.dp
        val chartLarge: Dp = 300.dp
        val chartFull: Dp = 400.dp
    }

    // Bottom Sheet
    object BottomSheet {
        val peekHeight: Dp = 80.dp
        val dragHandleHeight: Dp = 4.dp
        val dragHandleWidth: Dp = 32.dp
    }

    // Dialog
    object Dialog {
        val minWidth: Dp = 280.dp
        val maxWidth: Dp = 560.dp
        val padding: Dp = 24.dp
    }

    // Snackbar
    object Snackbar {
        val height: Dp = 56.dp
        val margin: Dp = 16.dp
    }

    // Badge
    object Badge {
        val size: Dp = 20.dp
        val sizeLarge: Dp = 24.dp
        val padding: Dp = 6.dp
    }

    // Loading Indicator
    object Loading {
        val sizeSmall: Dp = 16.dp
        val sizeMedium: Dp = 24.dp
        val sizeLarge: Dp = 48.dp
    }

    // FAB (Floating Action Button)
    object Fab {
        val size: Dp = 56.dp
        val sizeSmall: Dp = 40.dp
        val sizeLarge: Dp = 96.dp
        val margin: Dp = 16.dp
    }
}

/**
 * Grid System для layout
 */
object Grid {
    val columns: Int = 4
    val gutter: Dp = 16.dp
    val margin: Dp = 16.dp
}

/**
 * Breakpoints для responsive design
 */
object Breakpoints {
    val compact: Dp = 600.dp  // Phone
    val medium: Dp = 840.dp   // Tablet
    val expanded: Dp = 1240.dp // Desktop
}

/**
 * Animation Durations (in milliseconds)
 */
object AnimationDuration {
    const val instant: Int = 0
    const val fast: Int = 150
    const val normal: Int = 250
    const val slow: Int = 400
    const val verySlow: Int = 600

    // Specific animations
    const val buttonPress: Int = 150
    const val screenTransition: Int = 300
    const val dialogFade: Int = 200
    const val ripple: Int = 250
    const val priceUpdate: Int = 300
}

/**
 * Z-Index values for layers
 */
object ZIndex {
    const val background: Int = 0
    const val content: Int = 1
    const val card: Int = 2
    const val appBar: Int = 10
    const val bottomNav: Int = 10
    const val fab: Int = 15
    const val snackbar: Int = 20
    const val dialog: Int = 30
    const val tooltip: Int = 40
    const val modal: Int = 50
}
