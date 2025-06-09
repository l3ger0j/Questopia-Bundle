package org.qp.android.questopiabundle.utils

import org.jsoup.Jsoup
import org.jsoup.safety.Safelist
import org.qp.android.questopiabundle.utils.StringUtil.isNullOrEmpty
import java.util.regex.Pattern

object HtmlUtil {
    private val HTML_PATTERN: Pattern = Pattern.compile("<(\"[^\"]*\"|'[^']*'|[^'\">])*>")

    fun isContainsHtmlTags(text: String): Boolean {
        return HTML_PATTERN.matcher(text).find()
    }

    fun getSrcDir(html: String): String {
        val document = Jsoup.parse(html)
        val imageElement = document.select("img").first() ?: return ""
        return imageElement.attr("src")
    }

    /**
     * Remove HTML tags from the `html` string and return the resulting string.
     */
    fun removeHtmlTags(html: String): String {
        if (isNullOrEmpty(html)) return ""

        val result = StringBuilder()
        val len = html.length
        var fromIdx = 0

        while (fromIdx < len) {
            val idx = html.indexOf('<', fromIdx)
            if (idx == -1) {
                result.append(html.substring(fromIdx))
                break
            }
            result.append(html, fromIdx, idx)
            val endIdx = html.indexOf('>', idx + 1)
            if (endIdx == -1) {
                return Jsoup.clean(html, Safelist.none())
            }
            fromIdx = endIdx + 1
        }

        return result.toString()
    }
}
