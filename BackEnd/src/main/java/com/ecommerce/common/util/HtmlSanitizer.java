package com.ecommerce.common.util;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

public final class HtmlSanitizer {
    private static final String SANITIZER_BASE_URI = "https://sanitizer.local";

    private static final Safelist SAFE = Safelist.relaxed()
            .addTags("h1", "h2", "h3", "h4", "h5", "h6")
            // Quill commonly uses class-based alignment: <p class="ql-align-center">
            // Allow style so image sizing/float/layout survives round-trip. If you need stricter control later,
            // introduce a CSS sanitizer instead of stripping the attribute here.
            .addAttributes(":all", "class", "style")
            // Allow basic <figure> wrapper (closer to CMS/Word semantics)
            .addTags("figure", "figcaption")
            .addAttributes("figure", "class")
            // Images: allow safe attributes; allow style for width/height/float if you choose to persist it
            .addAttributes("img", "src", "alt", "title", "class", "style", "width", "height")
            .addAttributes("a", "href", "title", "target", "rel")
            .addProtocols("a", "href", "http", "https")
            .preserveRelativeLinks(true);

    private HtmlSanitizer() {}

    public static String sanitize(String html) {
        if (html == null) return null;
        return Jsoup.clean(html, SANITIZER_BASE_URI, SAFE);
    }
}

