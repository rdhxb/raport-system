package rdhxb.common.base;

public final class PeselMasker {

    private static final int VISIBLE_DIGITS = 5;
    private static final int PESEL_LENGTH = 11;

    private PeselMasker() {
    }

    public static String mask(String pesel){
        if (pesel == null){
            return null;
        }
        if (pesel.length() != PESEL_LENGTH){
            return "invalid";
        }

        return pesel.substring(0, VISIBLE_DIGITS) + "*".repeat(PESEL_LENGTH - VISIBLE_DIGITS);
    }
}
