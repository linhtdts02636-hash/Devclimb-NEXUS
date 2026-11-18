package com.devclimb.community;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public final class CommunitySlug {

    private static final Pattern NON_SLUG_CHARACTERS = Pattern.compile("[^a-z0-9]+");
    private static final Pattern EDGE_HYPHENS = Pattern.compile("^-+|-+$");

    private CommunitySlug() {
    }

    public static String from(String name) {
        if (name == null) {
            return "";
        }

        String withoutDiacritics = Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replace('đ', 'd');

        String hyphenated = NON_SLUG_CHARACTERS.matcher(withoutDiacritics).replaceAll("-");
        return EDGE_HYPHENS.matcher(hyphenated).replaceAll("");
    }
}
