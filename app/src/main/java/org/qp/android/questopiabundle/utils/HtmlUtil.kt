package org.qp.android.questopiabundle.utils

import org.jsoup.Jsoup
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
}