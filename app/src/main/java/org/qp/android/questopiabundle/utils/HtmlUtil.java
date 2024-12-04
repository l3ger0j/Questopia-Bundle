package org.qp.android.questopiabundle.utils;

import static org.qp.android.questopiabundle.utils.StringUtil.isNullOrEmpty;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.util.regex.Pattern;

public final class HtmlUtil {

    private static final Pattern HTML_PATTERN = Pattern.compile("<(\"[^\"]*\"|'[^']*'|[^'\">])*>");

    public static boolean isContainsHtmlTags(String text) {
        return HTML_PATTERN.matcher(text).find();
    }

    public static String getSrcDir(String html) {
        var document = Jsoup.parse(html);
        var imageElement = document.select("img").first();
        if (imageElement == null) return "";
        return imageElement.attr("src");
    }

    /**
     * Remove HTML tags from the <code>html</code> string and return the resulting string.
     */
    public static String removeHtmlTags(String html) {
        if (isNullOrEmpty(html)) return "";

        var result = new StringBuilder();
        var len = html.length();
        var fromIdx = 0;

        while (fromIdx < len) {
            var idx = html.indexOf('<', fromIdx);
            if (idx == -1) {
                result.append(html.substring(fromIdx));
                break;
            }
            result.append(html, fromIdx, idx);
            var endIdx = html.indexOf('>', idx + 1);
            if (endIdx == -1) {
                return Jsoup.clean(html, Safelist.none());
            }
            fromIdx = endIdx + 1;
        }

        return result.toString();
    }
}
