package com.joel.expressive_fab.extraFunctions

import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.createRippleModifierNode
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorProducer
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DelegatingNode
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

fun expressiveRipple(
    bounded: Boolean = true,
    radius: Dp = Dp.Unspecified,
    color: Color = Color.Unspecified,
    focusRingShape: Shape = CircleShape,
    focusRingColor: Color = Color.Unspecified,
    focusRingStrokeWidth: Dp = 3.dp,
    focusRingInset: Dp = 2.dp,
): IndicationNodeFactory = ExpressiveRippleIndicationNodeFactory(
    bounded = bounded,
    radius = radius,
    color = color,
    focusRingShape = focusRingShape,
    focusRingColor = focusRingColor,
    focusRingStrokeWidth = focusRingStrokeWidth,
    focusRingInset = focusRingInset,
)

private data class ExpressiveRippleIndicationNodeFactory(
    val bounded: Boolean,
    val radius: Dp,
    val color: Color,
    val focusRingShape: Shape,
    val focusRingColor: Color,
    val focusRingStrokeWidth: Dp,
    val focusRingInset: Dp,
) : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode =
        ExpressiveRippleNode(
            interactionSource = interactionSource,
            bounded = bounded,
            radius = radius,
            color = color,
            focusRingShape = focusRingShape,
            focusRingColor = focusRingColor,
            focusRingStrokeWidth = focusRingStrokeWidth,
            focusRingInset = focusRingInset,
        )
}

private class ExpressiveRippleNode(
    interactionSource: InteractionSource,
    bounded: Boolean,
    radius: Dp,
    color: Color,
    private val focusRingShape: Shape,
    private val focusRingColor: Color,
    private val focusRingStrokeWidth: Dp,
    private val focusRingInset: Dp,
) : DelegatingNode(), DrawModifierNode, CompositionLocalConsumerModifierNode {

    private val interactionSource = interactionSource
    private var isFocused by mutableStateOf(false)

    private val rippleNode = delegate(
        createRippleModifierNode(
            interactionSource = interactionSource,
            bounded = bounded,
            radius = radius,
            color = ColorProducer { color },
            rippleAlpha = {
                RippleAlpha(
                    draggedAlpha = 0.16f,
                    focusedAlpha = 0f,
                    hoveredAlpha = 0.08f,
                    pressedAlpha = 0.10f,
                )
            },
        )
    )

    override fun onAttach() {
        coroutineScope.launch {
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is FocusInteraction.Focus -> isFocused = true
                    is FocusInteraction.Unfocus -> isFocused = false
                }
            }
        }
    }

    override fun ContentDrawScope.draw() {

        drawContent()

        if (isFocused) {
            val ringColor = if (focusRingColor.isSpecified) {
                focusRingColor
            } else {
                currentValueOf(LocalContentColor)
            }

            inset(focusRingInset.toPx()) {
                val outline = focusRingShape.createOutline(size, layoutDirection, this)
                drawOutline(
                    outline = outline,
                    color = ringColor,
                    style = Stroke(width = focusRingStrokeWidth.toPx()),
                )
            }
        }
    }
}