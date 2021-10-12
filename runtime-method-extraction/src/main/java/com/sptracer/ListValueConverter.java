package com.sptracer;

import com.sptracer.configuration.converter.AbstractCollectionValueConverter;
import com.sptracer.configuration.converter.ValueConverter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListValueConverter<T> extends AbstractCollectionValueConverter<List<T>, T> {

    /**
     * Specialized delimiter supporting split by comma which is not enclosed in brackets.
     */
    public static final String COMMA_OUT_OF_BRACKETS = ",(?![^()]*\\))";

    protected final String delimiter;

    public ListValueConverter(ValueConverter<T> valueConverter) {
        super(valueConverter);
        this.delimiter = ",";
    }

    public ListValueConverter(ValueConverter<T> valueConverter, String delimiter) {
        super(valueConverter);
        this.delimiter = delimiter;
    }

    public List<T> convert(String s) {
        if (s != null && s.length() > 0) {
            final ArrayList<T> result = new ArrayList<>();
            for (String split : s.split(delimiter)) {
                result.add(valueConverter.convert(split.trim()));
            }
            return Collections.unmodifiableList(result);
        }
        return Collections.emptyList();
    }
}
