package com.joel.expressive_fab

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.HorizontalRuler
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.layout
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.ParentDataModifierNode
import androidx.compose.ui.node.SemanticsModifierNode
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMaxBy
import androidx.compose.ui.util.fastSumBy
import com.joel.expressive_fab.extraFunctions.FabMenuExpandDirection
import com.joel.expressive_fab.extraFunctions.expressiveRipple
import com.joel.expressive_fab.token.FabBaselineTokensApp
import com.joel.expressive_fab.token.FabMenuBaselineTokensApp
import com.joel.expressive_fab.token.FabPrimaryContainerTokensApp
import com.joel.expressive_fab.token.MotionSchemeKeyTokensApp
import com.joel.expressive_fab.token.ShapeKeyTokensApp
import kotlinx.coroutines.launch
import kotlin.math.hypot
import kotlin.math.roundToInt

/**
 * A layout that displays an expressive floating action button menu containing multiple menu items
 * which expand or collapse based on the [expanded] state.
 *
 * @param expanded Whether the menu items are currently expanded or collapsed.
 * @param expandDirection The direction in which the menu items expand relative to the button (e.g. [FabMenuExpandDirection.ABOVE] or [FabMenuExpandDirection.BELOW]).
 * @param button A composable representing the main trigger button for the menu.
 * @param modifier The modifier to be applied to the layout.
 * @param horizontalAlignment The horizontal alignment of the menu items relative to the button.
 * @param content The content of the menu, consisting of [ExpressiveFabMenuItem] declarations.
 */
@Composable
fun ExpressiveFabMenu(
    expanded: Boolean,
    expandDirection: FabMenuExpandDirection,
    button: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable FloatingActionButtonMenuScope.() -> Unit,
) {
    var buttonHeight by remember { mutableIntStateOf(0) }
    val focusRequester = remember { FocusRequester() }

    // Measure and layout menu items and the trigger button.
    // Handles relative placement based on expandDirection (ABOVE or BELOW) and horizontal alignment.
    Layout(
        modifier = modifier.padding(horizontal = 0.dp),
        content = {
            FloatingActionButtonMenuItemColumn(
                modifier = Modifier.focusRequester(focusRequester),
                expanded = expanded,
                expandDirection = expandDirection,
                horizontalAlignment = horizontalAlignment,
                buttonHeight = { buttonHeight },
                content = content,
            )

            Box(
                Modifier.onKeyEvent {
                    if (
                        expanded &&
                        it.type == KeyEventType.KeyDown &&
                        ((it.key == Key.Tab && !it.isShiftPressed) ||
                                it.key == Key.DirectionDown ||
                                it.key == Key.NumPadDirectionDown)
                    ) {
                        focusRequester.requestFocus()
                        return@onKeyEvent true
                    }
                    return@onKeyEvent false
                }
            ) {
                button()
            }
        },
    ) { measureables, constraints ->
        val menuItemsPlaceable = measureables[0].measure(constraints)
        val buttonPaddingBottom = FabMenuButtonPaddingBottom.roundToPx()

        var buttonPlaceable: Placeable? = null
        val suggestedWidth: Int
        val suggestedHeight: Int

        if (measureables.size > 1) {
            buttonPlaceable = measureables[1].measure(constraints)
            buttonHeight = buttonPlaceable.height

            suggestedWidth = maxOf(buttonPlaceable.width, menuItemsPlaceable.width)
            suggestedHeight = maxOf(buttonPlaceable.height + buttonPaddingBottom, menuItemsPlaceable.height)
        } else {
            suggestedWidth = menuItemsPlaceable.width
            suggestedHeight = menuItemsPlaceable.height
        }

        val width = minOf(suggestedWidth, constraints.maxWidth)
        val height = minOf(suggestedHeight, constraints.maxHeight)

        layout(width, height) {
            val menuItemsX = horizontalAlignment.align(menuItemsPlaceable.width, width, layoutDirection)

            if (expandDirection == FabMenuExpandDirection.ABOVE) {
                menuItemsPlaceable.place(menuItemsX, 0)
                if (buttonPlaceable != null) {
                    val buttonX = horizontalAlignment.align(buttonPlaceable.width, width, layoutDirection)
                    val buttonY = height - buttonPlaceable.height - buttonPaddingBottom
                    buttonPlaceable.place(buttonX, buttonY)
                }
            } else { // Below
                menuItemsPlaceable.place(menuItemsX, buttonHeight + buttonPaddingBottom)
                if (buttonPlaceable != null) {
                    val buttonX = horizontalAlignment.align(buttonPlaceable.width, width, layoutDirection)
                    val buttonY = 0
                    buttonPlaceable.place(buttonX, buttonY)
                }
            }
        }
    }
}

@Composable
private fun FloatingActionButtonMenuItemColumn(
    modifier: Modifier,
    expanded: Boolean,
    expandDirection: FabMenuExpandDirection,
    horizontalAlignment: Alignment.Horizontal,
    buttonHeight: () -> Int,
    content: @Composable FloatingActionButtonMenuScope.() -> Unit,
) {
    var itemCount by remember { mutableIntStateOf(0) }
    var itemsNeedVerticalScroll by remember { mutableStateOf(false) }
    var originalConstraints: Constraints? = null
    var staggerAnim by remember { mutableStateOf<Animatable<Int, AnimationVector1D>?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Set up stagger animation specs with a bouncier spring damping ratio
    var staggerAnimSpec: FiniteAnimationSpec<Int> = MotionSchemeKeyTokensApp.SlowEffects.value()
    if (staggerAnimSpec is SpringSpec<Int>) {
        staggerAnimSpec = spring(
            stiffness = staggerAnimSpec.stiffness,
            dampingRatio = 0.3f,
            visibilityThreshold = 1,
        )
    }

    // Measure and layout column of menu items with staggered animation and scroll handling
    Layout(
        modifier = modifier
            .clipToBounds()
            .semantics {
                isTraversalGroup = true
                traversalIndex = -0.9f
            }
            .layout { measurable, constraints ->
                originalConstraints = constraints
                val placeable = measurable.measure(constraints)
                layout(placeable.width, placeable.height) { placeable.place(0, 0) }
            }
            .then(
                if (itemsNeedVerticalScroll)
                    Modifier.verticalScroll(state = rememberScrollState(), enabled = expanded)
                else Modifier
            ),
        content = {
            val scope = remember(horizontalAlignment, expandDirection) {
                object : FloatingActionButtonMenuScope {
                    override val horizontalAlignment: Alignment.Horizontal
                        get() = horizontalAlignment
                    override val expandDirection: FabMenuExpandDirection
                        get() = expandDirection
                }
            }
            content(scope)
        },
    ) { measurables, constraints ->
        itemCount = measurables.size

        val targetItemCount = if (expanded) itemCount else 0
        staggerAnim = staggerAnim?.also {
            if (it.targetValue != targetItemCount) {
                coroutineScope.launch {
                    it.animateTo(targetValue = targetItemCount, animationSpec = staggerAnimSpec)
                }
            }
        } ?: Animatable(targetItemCount, Int.VectorConverter)

        val placeables = measurables.fastMap { measurable -> measurable.measure(constraints) }
        val width = placeables.fastMaxBy { it.width }?.width ?: 0

        val verticalSpacing = FabMenuItemSpacingVertical.roundToPx()
        val verticalSpacingHeight = if (placeables.isNotEmpty()) verticalSpacing * (placeables.size - 1) else 0

        val isExpandUp = expandDirection == FabMenuExpandDirection.ABOVE

        val bottomPadding = if (isExpandUp) {
            val currentButtonHeight = buttonHeight()
            if (currentButtonHeight > 0)
                currentButtonHeight + FabMenuButtonPaddingBottom.roundToPx() + 10
            else 0
        } else {
            10
        }

        val height = placeables.fastSumBy { it.height } + verticalSpacingHeight + bottomPadding

        val yPositions = IntArray(placeables.size)
        var rulerValue: Float


        if (isExpandUp) {
            rulerValue = height.toFloat()
            var currentY = height - bottomPadding

            placeables.fastForEachIndexed { index, placeable ->
                currentY -= placeable.height
                yPositions[index] = currentY

                if (index < (staggerAnim?.value ?: 0)) {
                    rulerValue = currentY.toFloat()
                }
                currentY -= verticalSpacing
            }
        } else { // Below
            rulerValue = -bottomPadding.toFloat()
            var currentY = 0

            placeables.fastForEachIndexed { index, placeable ->
                yPositions[index] = currentY

                if (index < (staggerAnim?.value ?: 0)) {
                    rulerValue = currentY.toFloat()
                }
                currentY += placeable.height + verticalSpacing
            }
        }

        val finalHeight = if (placeables.fastAny { item -> item.isVisible }) height else 0
        itemsNeedVerticalScroll = finalHeight > (originalConstraints?.maxHeight ?: Int.MAX_VALUE)

        layout(width, finalHeight, rulers = { MenuItemRuler provides rulerValue }) {
            placeables.fastForEachIndexed { index, placeable ->
                val x = horizontalAlignment.align(placeable.width, width, layoutDirection)
                placeable.place(x, yPositions[index])
            }
        }
    }
}

/**
 * Scope interface for [FloatingActionButtonMenuScope] providing alignment and expand direction properties.
 */
interface FloatingActionButtonMenuScope {
    val horizontalAlignment: Alignment.Horizontal
    val expandDirection: FabMenuExpandDirection
}

/**
 * An individual menu item used within a [ExpressiveFabMenu].
 *
 * @param onClick Callback to be invoked when this menu item is clicked.
 * @param text The text label to display inside the menu item.
 * @param icon The icon to display inside the menu item.
 * @param modifier The modifier to be applied to this menu item.
 * @param containerColor The background color of the menu item container.
 * @param contentColor The content (text and icon) color of the menu item.
 */
@Composable
fun FloatingActionButtonMenuScope.ExpressiveFabMenuItem(
    onClick: () -> Unit,
    text: @Composable () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = contentColorFor(containerColor),
) {
    var widthAnim by remember { mutableStateOf<Animatable<Float, AnimationVector1D>?>(null) }
    var alphaAnim by remember { mutableStateOf<Animatable<Float, AnimationVector1D>?>(null) }

    val widthSpring: FiniteAnimationSpec<Float> = spring(
        stiffness = Spring.StiffnessLow,
        dampingRatio = 0.7f
    )
    val alphaSpring: FiniteAnimationSpec<Float> = MotionSchemeKeyTokensApp.FastEffects.value()
    val coroutineScope = rememberCoroutineScope()

    var isVisible by remember { mutableStateOf(false) }
    val isExpandUp = expandDirection == FabMenuExpandDirection.ABOVE

    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
        Surface(
            modifier = modifier.itemVisible { isVisible }.layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                layout(placeable.width, placeable.height) {
                    val rulerCurrent = MenuItemRuler.current(Float.POSITIVE_INFINITY)

                    val target = if (isExpandUp) {
                        if (rulerCurrent <= 0) 1f else 0f
                    } else {
                        if (rulerCurrent >= 0) 1f else 0f
                    }

                    widthAnim = widthAnim?.also {
                        if (it.targetValue != target) {
                            coroutineScope.launch { it.animateTo(target, widthSpring) }
                        }
                    } ?: Animatable(target, Float.VectorConverter)

                    val tempAlphaAnim = alphaAnim?.also {
                        if (it.targetValue != target) {
                            coroutineScope.launch { it.animateTo(target, alphaSpring) }
                        }
                    } ?: Animatable(target, Float.VectorConverter)

                    alphaAnim = tempAlphaAnim
                    isVisible = tempAlphaAnim.value != 0f

                    if (isVisible) {
                        placeable.placeWithLayer(0, 0) { alpha = tempAlphaAnim.value }
                    }
                }
            },
            shape = FabMenuBaselineTokensApp.ListItemContainerShape.fabMenuBaselineValue(),
            color = containerColor,
            contentColor = contentColor,
            onClick = onClick,
        ) {
            Row(
                Modifier.layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    val width = (placeable.width * maxOf((widthAnim?.value ?: 0f), 0f)).roundToInt()

                    layout(width, placeable.height) {
                        val x = horizontalAlignment.align(placeable.width, width, layoutDirection)
                        placeable.placeWithLayer(x, 0)
                    }
                }
                    .sizeIn(minWidth = FabMenuItemMinWidth, minHeight = FabMenuItemHeight)
                    .padding(
                        start = FabMenuItemContentPaddingStart,
                        end = FabMenuItemContentPaddingEnd,
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(
                    FabMenuItemContentSpacingHorizontal,
                    Alignment.CenterHorizontally,
                ),
            ) {
                icon()
                CompositionLocalProvider(
                    LocalTextStyle provides MaterialTheme.typography.titleMedium,
                    content = text,
                )
            }
        }
    }
}

private val MenuItemRuler = HorizontalRuler()

/**
 * A toggleable floating action button that animates between checked and unchecked states,
 * supporting container size, corner radius, and color transitions.
 *
 * @param checked Whether the toggle floating action button is currently checked.
 * @param onCheckedChange Callback invoked when the checked state changes.
 * @param modifier The modifier to be applied to the button.
 * @param containerColor Function returning the container color based on the checked progress (0.0 to 1.0).
 * @param contentAlignment Alignment of the content inside the button container.
 * @param containerSize Function returning the container size based on the checked progress.
 * @param containerCornerRadius Function returning the container corner radius based on the checked progress.
 * @param content The content composable (typically an icon) inside the button.
 */
@Composable
fun ToggleFloatingActionButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: (Float) -> Color = ToggleFloatingActionButtonDefaults.containerColor(),
    contentAlignment: Alignment = Alignment.TopEnd,
    containerSize: (Float) -> Dp = ToggleFloatingActionButtonDefaults.containerSize(),
    containerCornerRadius: (Float) -> Dp =
        ToggleFloatingActionButtonDefaults.containerCornerRadius(),
    content: @Composable ToggleFloatingActionButtonScope.() -> Unit,
) {
    val checkedProgress =
        animateFloatAsState(
            targetValue = if (checked) 1f else 0f,
            animationSpec = MotionSchemeKeyTokensApp.FastSpatial.value(),
        )
    ToggleFloatingActionButton(
        checked,
        onCheckedChange,
        { checkedProgress.value },
        modifier,
        containerColor,
        contentAlignment,
        containerSize,
        containerCornerRadius,
        content,
    )
}

@Composable
private fun ToggleFloatingActionButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    checkedProgress: () -> Float,
    modifier: Modifier = Modifier,
    containerColor: (Float) -> Color = ToggleFloatingActionButtonDefaults.containerColor(),
    contentAlignment: Alignment = Alignment.TopEnd,
    containerSize: (Float) -> Dp = ToggleFloatingActionButtonDefaults.containerSize(),
    containerCornerRadius: (Float) -> Dp =
        ToggleFloatingActionButtonDefaults.containerCornerRadius(),
    content: @Composable ToggleFloatingActionButtonScope.() -> Unit,
) {
    val initialSize = remember(containerSize) { containerSize(0f) }
    Box(Modifier.size(initialSize), contentAlignment = contentAlignment) {
        val density = LocalDensity.current
        
        // Calculate the ripple radius based on the initial size of the FAB
        val fabRippleRadius =
            remember(initialSize) {
                with(density) {
                    val fabSizeHalf = initialSize.toPx() / 2
                    hypot(fabSizeHalf, fabSizeHalf).toDp()
                }
            }
            
        // Build the animated shape using dynamic corner radius based on checked progress
        val shape =
            remember(density, checkedProgress, containerCornerRadius) {
                GenericShape { size, _ ->
                    val radius = with(density) { containerCornerRadius(checkedProgress()).toPx() }
                    addRoundRect(RoundRect(size.toRect(), CornerRadius(radius)))
                }
            }
            
        Box(
            modifier
                .graphicsLayer {
                    this.shadowElevation = FabShadowElevation.toPx()
                    this.shape = shape
                    this.clip = true
                }
                .drawBehind {
                    val radius = with(density) { containerCornerRadius(checkedProgress()).toPx() }
                    drawRoundRect(
                        color = containerColor(checkedProgress()),
                        cornerRadius = CornerRadius(radius),
                    )
                }
                .toggleable(
                    value = checked,
                    onValueChange = onCheckedChange,
                    interactionSource = null,
                    indication = expressiveRipple(radius = fabRippleRadius, focusRingShape = shape),
                )
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    val sizePx = containerSize(checkedProgress()).roundToPx()
                    layout(sizePx, sizePx) {
                        placeable.place(
                            (sizePx - placeable.width) / 2,
                            (sizePx - placeable.height) / 2,
                        )
                    }
                }
        ) {
            val scope =
                remember(checkedProgress) {
                    object : ToggleFloatingActionButtonScope {
                        override val checkedProgress: Float
                            get() = checkedProgress()
                    }
                }
            content(scope)
        }
    }
}

/**
 * Contains default values and helper functions for [ToggleFloatingActionButton], including
 * color interpolations, size/corner radius animations, and icon animations.
 */
object ToggleFloatingActionButtonDefaults {

    @Composable
    fun containerColor(
        initialColor: Color = MaterialTheme.colorScheme.primaryContainer,
        finalColor: Color = MaterialTheme.colorScheme.primary,
    ): (Float) -> Color = { progress -> lerp(initialColor, finalColor, progress) }

    fun containerSize(initialSize: Dp, finalSize: Dp = FabFinalSize): (Float) -> Dp = { progress ->
        lerp(initialSize, finalSize, progress)
    }

    fun containerSize() = containerSize(FabInitialSize)

    fun containerCornerRadius(
        initialSize: Dp,
        finalSize: Dp = FabFinalCornerRadius,
    ): (Float) -> Dp = { progress -> lerp(initialSize, finalSize, progress) }

    fun containerCornerRadius() = containerCornerRadius(FabInitialCornerRadius)

    @Composable
    fun iconColor(
        initialColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
        finalColor: Color = MaterialTheme.colorScheme.onPrimary,
    ): (Float) -> Color = { progress -> lerp(initialColor, finalColor, progress) }

    fun iconSize(initialSize: Dp, finalSize: Dp = FabFinalIconSize): (Float) -> Dp = { progress ->
        lerp(initialSize, finalSize, progress)
    }

    fun iconSize() = iconSize(FabInitialIconSize)

    @Composable
    fun Modifier.animateIcon(
        checkedProgress: () -> Float,
        color: (Float) -> Color = iconColor(),
        size: (Float) -> Dp = iconSize(),
    ) =
        this.layout { measurable, _ ->
            val sizePx = size(checkedProgress()).roundToPx()
            val placeable = measurable.measure(Constraints.fixed(sizePx, sizePx))
            layout(sizePx, sizePx) { placeable.place(0, 0) }
        }
            .drawWithCache {
                val layer = obtainGraphicsLayer()
                layer.apply {
                    record { drawContent() }
                    this.colorFilter = ColorFilter.tint(color(checkedProgress()))
                }

                onDrawWithContent { drawLayer(graphicsLayer = layer) }
            }
}


/**
 * Scope interface for [ToggleFloatingActionButton] providing the current [checkedProgress].
 */
interface ToggleFloatingActionButtonScope {
    val checkedProgress: Float
}

@Stable
private fun Modifier.itemVisible(isVisible: () -> Boolean) =
    this then MenuItemVisibleElement(isVisible = isVisible)

private class MenuItemVisibleElement(private val isVisible: () -> Boolean) :
    ModifierNodeElement<MenuItemVisibilityModifier>() {
    override fun create() = MenuItemVisibilityModifier(isVisible)

    override fun update(node: MenuItemVisibilityModifier) {
        node.visible = isVisible
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "itemVisible"
        value = isVisible()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as MenuItemVisibleElement

        return isVisible === other.isVisible
    }

    override fun hashCode(): Int {
        return isVisible.hashCode()
    }
}

private class MenuItemVisibilityModifier(isVisible: () -> Boolean) :
    ParentDataModifierNode, SemanticsModifierNode, Modifier.Node() {

    var visible: () -> Boolean = isVisible

    override fun Density.modifyParentData(parentData: Any?): Any {
        return this@MenuItemVisibilityModifier
    }

    override val shouldClearDescendantSemantics: Boolean
        get() = !visible()

    override fun SemanticsPropertyReceiver.applySemantics() {}
}

private val Placeable.isVisible: Boolean
    get() = (this.parentData as? MenuItemVisibilityModifier)?.visible?.invoke() != false

private val FabInitialSize = FabBaselineTokensApp.ContainerHeight
private val FabInitialCornerRadius = 16.dp
private val FabInitialIconSize = FabBaselineTokensApp.IconSize
private val FabFinalSize = FabMenuBaselineTokensApp.CloseButtonContainerHeight
private val FabFinalCornerRadius = FabFinalSize.div(2)
private val FabFinalIconSize = FabMenuBaselineTokensApp.CloseButtonIconSize
private val FabShadowElevation = FabPrimaryContainerTokensApp.ContainerElevation
private val FabMenuButtonPaddingBottom = 16.dp
private val FabMenuItemMinWidth = FabMenuBaselineTokensApp.ListItemContainerHeight
private val FabMenuItemHeight = FabMenuBaselineTokensApp.ListItemContainerHeight
private val FabMenuItemSpacingVertical = FabMenuBaselineTokensApp.ListItemBetweenSpace
private val FabMenuItemContentPaddingStart = FabMenuBaselineTokensApp.ListItemLeadingSpace
private val FabMenuItemContentPaddingEnd = FabMenuBaselineTokensApp.ListItemTrailingSpace
private val FabMenuItemContentSpacingHorizontal = FabMenuBaselineTokensApp.ListItemIconLabelSpace

@Composable
@ReadOnlyComposable
internal fun <T> MotionSchemeKeyTokensApp.value(): FiniteAnimationSpec<T> {
    return when (this) {
        MotionSchemeKeyTokensApp.FastSpatial -> spring(
            stiffness = Spring.StiffnessMediumLow,
            visibilityThreshold = null
        )
        MotionSchemeKeyTokensApp.SlowSpatial -> spring(
            stiffness = Spring.StiffnessLow
        )
        MotionSchemeKeyTokensApp.DefaultSpatial -> spring(
            stiffness = Spring.StiffnessMedium
        )
        MotionSchemeKeyTokensApp.FastEffects -> spring(
            stiffness = Spring.StiffnessMedium
        )
        MotionSchemeKeyTokensApp.SlowEffects -> spring(
            stiffness = Spring.StiffnessLow
        )
        MotionSchemeKeyTokensApp.DefaultEffects -> spring(
            stiffness = Spring.StiffnessMedium
        )
    }
}

@Composable
@ReadOnlyComposable
internal fun ShapeKeyTokensApp.fabMenuBaselineValue(): Shape {
    return when (this) {
        ShapeKeyTokensApp.CornerExtraSmall -> MaterialTheme.shapes.extraSmall
        ShapeKeyTokensApp.CornerSmall -> MaterialTheme.shapes.small
        ShapeKeyTokensApp.CornerMedium -> MaterialTheme.shapes.medium
        ShapeKeyTokensApp.CornerLarge -> MaterialTheme.shapes.large
        ShapeKeyTokensApp.CornerExtraLarge -> MaterialTheme.shapes.extraLarge
        ShapeKeyTokensApp.CornerFull -> ShapeDefaults.Medium
        else -> MaterialTheme.shapes.medium
    }
}