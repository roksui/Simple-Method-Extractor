package com.sptracer.util;

public class Version implements Comparable<Version> {

    public static final Version UNKNOWN_VERSION = of("1.0.0");

    private final int[] numbers;

    public static Version of(String version) {
        return new Version(version);
    }

    private Version(String version) {
        int indexOfDash = version.indexOf('-');
        int indexOfFirstDot = version.indexOf('.');
        if (indexOfDash > 0 && indexOfDash < indexOfFirstDot) {
            version = version.substring(indexOfDash + 1);
        }
        indexOfDash = version.indexOf('-');
        int indexOfLastDot = version.lastIndexOf('.');
        if (indexOfDash > 0 && indexOfDash > indexOfLastDot) {
            version = version.substring(0, indexOfDash);
        }
        final String[] parts = version.split("\\.");
        int[] tmp = new int[parts.length];
        int validPartsIndex = 0;
        for (String part : parts) {
            try {
                tmp[validPartsIndex] = Integer.valueOf(part);
                validPartsIndex++;
            } catch (NumberFormatException numberFormatException) {
                // continue
            }
        }
        numbers = new int[validPartsIndex];
        if (numbers.length > 0) {
            System.arraycopy(tmp, 0, numbers, 0, numbers.length);
        }
    }

    @Override
    public int compareTo(Version another) {
        final int maxLength = Math.max(numbers.length, another.numbers.length);
        for (int i = 0; i < maxLength; i++) {
            final int left = i < numbers.length ? numbers[i] : 0;
            final int right = i < another.numbers.length ? another.numbers[i] : 0;
            if (left != right) {
                return left < right ? -1 : 1;
            }
        }
        return 0;
    }
}
