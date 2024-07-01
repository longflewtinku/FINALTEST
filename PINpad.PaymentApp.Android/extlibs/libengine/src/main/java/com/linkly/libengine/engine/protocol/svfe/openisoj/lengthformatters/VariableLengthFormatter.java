package com.linkly.libengine.engine.protocol.svfe.openisoj.lengthformatters;

import com.linkly.libengine.engine.protocol.svfe.openisoj.formatter.AsciiFormatter;
import com.linkly.libengine.engine.protocol.svfe.openisoj.formatter.IFormatter;

public class VariableLengthFormatter implements ILengthFormatter {

    private int _lengthIndicator;
    private int _maxLength;
    private IFormatter _lengthFormatter;

    private int _lengthOfLength;

    public VariableLengthFormatter(int lengthIndicator, int maxLength) throws Exception {
        this(lengthIndicator, maxLength, new AsciiFormatter());
    }

    public VariableLengthFormatter(int lengthIndicator, int maxLength, IFormatter lengthFormatter) throws Exception {
        _lengthIndicator = lengthIndicator;
        _maxLength = maxLength;
        _lengthFormatter = lengthFormatter;
        _lengthOfLength = _lengthFormatter.getPackedLength(_lengthIndicator);

    }

    public String getDescription() {
        int places = (int) Math.log10(_maxLength);
        return "L" + (1 + places) + "Var";
    }

    public int getLengthOfField(byte[] msg, int offset) throws Exception {
        int len = _lengthOfLength;
        byte[] lenData = new byte[len];
        System.arraycopy(msg, offset, lenData, 0, len);
        String lenStr = _lengthFormatter.getString(lenData);
        return Integer.parseInt(lenStr);
    }

    public int getLengthOfLengthIndicator() {
        return _lengthOfLength;
    }

    public String getMaxLength() {
        return ".." + _maxLength;
    }

    public boolean isValidLength(int packedLength) {
        return packedLength <= _maxLength;
    }

    public int pack(byte[] msg, int length, int offset) throws Exception {
        byte[] header = _lengthFormatter.getBytes( length, getLengthOfLengthIndicator(), '0' );
        System.arraycopy(header, 0, msg, offset, header.length);
        return offset + header.length;
    }
}
