package com.sptracer.configuration.converter;

import com.sptracer.matcher.WildcardMatcher;

public class WildcardMatcherValueConverter implements ValueConverter<WildcardMatcher> {

    @Override
    public WildcardMatcher convert(String s) {
        return WildcardMatcher.valueOf(s);
    }

    @Override
    public String toString(WildcardMatcher value) {
        return value.toString();
    }

    @Override
    public String toSafeString(WildcardMatcher value) {
        return value.toString();
    }
}
