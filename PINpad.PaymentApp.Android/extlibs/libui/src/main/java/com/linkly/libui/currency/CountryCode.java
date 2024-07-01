package com.linkly.libui.currency;

public class CountryCode {
    private String alphaCode;
    private String numCode;
    private int decimals;
    private String symbol;

    private String majWord;
    private String majWordPlural;
    private String minWord;
    private String minWordPlural;

    public CountryCode(CountryCode c) {
        this.alphaCode = c.alphaCode;
        this.numCode = c.numCode;
        this.decimals = c.decimals;
    }

    public CountryCode(String alphaCode, String numCode, String dec, String display) {
        this.alphaCode = alphaCode;
        this.numCode = numCode;
        this.decimals = Integer.valueOf(dec);
    }

    public CountryCode(String alphaCode, String numCode, String dec, String display, String symbol) {
        this.alphaCode = alphaCode;
        this.numCode = numCode;
        this.decimals = Integer.valueOf(dec);
        this.symbol = symbol;
    }

    public CountryCode(String alphaCode, String numCode, String dec, String display, String symbol, String majWord, String majWordPlural, String minWord, String minWordPlural) {
        this.alphaCode = alphaCode;
        this.numCode = numCode;
        this.decimals = Integer.valueOf(dec);
        this.symbol = symbol;
        this.majWord = majWord;
        this.majWordPlural = majWordPlural;
        this.minWord = minWord;
        this.minWordPlural = minWordPlural;
    }


    public String getAlphaCode() {
        return this.alphaCode;
    }

    public String getNumCode() {
        return this.numCode;
    }

    public int getDecimals() {
        return this.decimals;
    }

    public String getSymbol() {
        return this.symbol;
    }

    public String getMajWord() {
        return this.majWord;
    }

    public String getMajWordPlural() {
        return this.majWordPlural;
    }

    public String getMinWord() {
        return this.minWord;
    }

    public String getMinWordPlural() {
        return this.minWordPlural;
    }

    public void setDecimals(int decimals) {
        this.decimals = decimals;
    }

    public void setMajWord(String majWord) {
        this.majWord = majWord;
    }

    public void setMajWordPlural(String majWordPlural) {
        this.majWordPlural = majWordPlural;
    }

    public void setMinWord(String minWord) {
        this.minWord = minWord;
    }

    public void setMinWordPlural(String minWordPlural) {
        this.minWordPlural = minWordPlural;
    }
}
