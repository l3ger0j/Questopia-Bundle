package org.qp.android.questopiabundle;

public interface HtmlProcessor {

    String removeHtmlTags(String html);

    boolean isContainsHtmlTags(String text);

    String getSrcDir(String html);

}
