package com.prolearn.codecraftfront.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prolearn.codecraftfront.ui.theme.NeonCyan
import com.prolearn.codecraftfront.ui.theme.NeonGreen
import com.prolearn.codecraftfront.ui.theme.NeonOrange
import com.prolearn.codecraftfront.ui.theme.NeonPurple

private val DslKeywords = setOf("move", "turn", "collect", "left", "right", "up", "down")

private val KeywordRegex = Regex("\\b(${DslKeywords.joinToString("|")})\\b")
private val NumberRegex = Regex("\\b\\d+\\b")
private val PunctuationRegex = Regex("[()\\[\\]{}]")
private val CommentRegex = Regex("//[^\\n]*")

@Composable
fun CodeEditorField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    minLines: Int = 8,
    label: String? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) NeonCyan
        else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
        label = "editorBorder",
    )

    val codeColor = MaterialTheme.colorScheme.onSurface
    val gutterColor = MaterialTheme.colorScheme.onSurfaceVariant

    val visualTransformation = remember {
        DslSyntaxTransformation(
            baseColor = codeColor,
            keywordColor = NeonGreen,
            numberColor = NeonOrange,
            punctuationColor = NeonPurple,
            commentColor = gutterColor,
        )
    }

    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    width = 1.5.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(14.dp),
                )
                .defaultMinSize(minHeight = (minLines * 22).dp)
                .padding(vertical = 10.dp),
        ) {
            LineNumbersGutter(
                text = value.text,
                color = gutterColor,
                modifier = Modifier.padding(horizontal = 10.dp),
            )
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                interactionSource = interactionSource,
                cursorBrush = SolidColor(NeonCyan),
                visualTransformation = visualTransformation,
                textStyle = LocalTextStyle.current.merge(
                    TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        color = codeColor,
                    ),
                ),
                keyboardOptions = KeyboardOptions(
                    autoCorrect = false,
                    capitalization = KeyboardCapitalization.None,
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
            )
        }
    }
}

@Composable
private fun LineNumbersGutter(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val lineCount = remember(text) {
        if (text.isEmpty()) 1 else text.count { it == '\n' } + 1
    }
    Column(
        modifier = modifier.width(28.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Top,
    ) {
        for (i in 1..lineCount) {
            Text(
                text = i.toString(),
                color = color,
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.End,
                    lineHeight = 22.sp,
                ),
            )
        }
    }
}

private class DslSyntaxTransformation(
    private val baseColor: Color,
    private val keywordColor: Color,
    private val numberColor: Color,
    private val punctuationColor: Color,
    private val commentColor: Color,
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val source = text.text
        val builder = AnnotatedString.Builder(source)

        builder.addStyle(SpanStyle(color = baseColor), 0, source.length)

        addSpans(builder, source, CommentRegex, SpanStyle(color = commentColor))
        addSpans(builder, source, PunctuationRegex, SpanStyle(color = punctuationColor, fontWeight = FontWeight.Bold))
        addSpans(builder, source, NumberRegex, SpanStyle(color = numberColor, fontWeight = FontWeight.Bold))
        addSpans(builder, source, KeywordRegex, SpanStyle(color = keywordColor, fontWeight = FontWeight.Bold))

        return TransformedText(builder.toAnnotatedString(), OffsetMapping.Identity)
    }

    private fun addSpans(
        builder: AnnotatedString.Builder,
        source: String,
        regex: Regex,
        style: SpanStyle,
    ) {
        regex.findAll(source).forEach { match ->
            builder.addStyle(style, match.range.first, match.range.last + 1)
        }
    }
}
