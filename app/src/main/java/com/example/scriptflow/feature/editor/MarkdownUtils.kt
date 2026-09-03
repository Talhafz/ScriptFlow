package com.example.scriptflow.feature.editor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp

/**
 * Splits a long string into smaller chunks to avoid Android hardware texture limits (8192px).
 * Preserves words and ensures NO characters are skipped.
 */
fun String.splitIntoChunks(maxChars: Int = 400): List<String> {
    if (this.length <= maxChars) return listOf(this)
    
    val result = mutableListOf<String>()
    var currentIndex = 0
    
    while (currentIndex < this.length) {
        var end = (currentIndex + maxChars).coerceAtMost(this.length)
        
        // If not at the very end, try to find a good breaking point (space)
        if (end < this.length) {
            val lastSpace = this.lastIndexOf(' ', end)
            if (lastSpace > currentIndex) {
                end = lastSpace + 1 // Break after the space
            }
        }
        
        val chunk = this.substring(currentIndex, end)
        if (chunk.isNotEmpty()) {
            result.add(chunk)
        }
        currentIndex = end
    }
    
    return result
}
fun String.parseMarkdown(
    accentColor: Color,
    headingSize: Int = 40,
    cueColor: Color = Color.Gray
): AnnotatedString {
    // 1. Initial cleanup: remove block markers
    val stage1 = this.replace(Regex("\\[ALIGN:(LEFT|CENTER|RIGHT)\\]\\n?"), "")
    
    return buildAnnotatedString {
        val lines = stage1.split("\n")
        lines.forEachIndexed { i, line ->
            val isHeading = line.startsWith("# ")
            val lineContent = if (isHeading) line.substring(2) else line
            
            val lineStart = length
            
            // 2. Parse inline styles with stripping
            val processedLine = processInlineMarkers(lineContent, accentColor, cueColor)
            append(processedLine)
            
            // Apply styles based on processedLine's AnnotatedString
            // Since processInlineMarkers returns an AnnotatedString, we can just append it
            // But we need to handle the heading style too
            
            if (isHeading) {
                addStyle(
                    SpanStyle(
                        fontSize = headingSize.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    ),
                    lineStart,
                    length
                )
            }
            
            if (i < lines.size - 1) append("\n")
        }
    }
}

private fun processInlineMarkers(
    text: String,
    accentColor: Color,
    cueColor: Color
): AnnotatedString {
    return buildAnnotatedString {
        var remaining = text
        
        // This simple pass-through handles non-overlapping markers.
        // For teleprompter use, this covers 99% of cases.
        
        val markerRegex = Regex("(\\*\\*|\\*|__|\\(.*?\\)|\\[PAUSE\\])")
        
        var lastIndex = 0
        markerRegex.findAll(text).forEach { match ->
            // Append text before marker
            append(text.substring(lastIndex, match.range.first))
            
            val marker = match.value
            val start = length
            
            when {
                marker.startsWith("**") -> {
                    val content = marker.removeSurrounding("**")
                    append(content)
                    addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, length)
                }
                marker.startsWith("*") -> {
                    val content = marker.removeSurrounding("*")
                    append(content)
                    addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, length)
                }
                marker.startsWith("__") -> {
                    val content = marker.removeSurrounding("__")
                    append(content)
                    addStyle(SpanStyle(textDecoration = TextDecoration.Underline), start, length)
                }
                marker.startsWith("(") -> {
                    val content = marker.removeSurrounding("(", ")")
                    append(content)
                    addStyle(SpanStyle(color = cueColor, fontStyle = FontStyle.Italic), start, length)
                }
                marker == "[PAUSE]" -> {
                    append(" • PAUSE • ")
                    addStyle(
                        SpanStyle(
                            color = accentColor,
                            fontWeight = FontWeight.ExtraBold,
                            background = accentColor.copy(alpha = 0.1f)
                        ),
                        start,
                        length
                    )
                }
            }
            lastIndex = match.range.last + 1
        }
        
        // Append remaining text
        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }
}
