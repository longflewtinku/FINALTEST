package com.linkly.libengine.engine.protocol.iso8583.openisoj;

import com.linkly.libengine.engine.protocol.iso8583.openisoj.exceptions.FieldDescriptorException;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.exceptions.FieldFormatException;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.exceptions.FieldUnpackException;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.fieldvalidator.BcdFieldValidator;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.fieldvalidator.FieldValidators;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.fieldvalidator.HexFieldValidator;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.fieldvalidator.IFieldValidator;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.fieldvalidator.NumericFieldValidator;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.formatter.AsciiFormatter;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.formatter.BcdFormatter;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.formatter.BinaryFormatter;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.formatter.Formatters;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.formatter.IFormatter;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.lengthformatters.FixedLengthFormatter;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.lengthformatters.ILengthFormatter;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.lengthformatters.VariableLengthFormatter;

public class FieldDescriptor implements IFieldDescriptor {

    protected ILengthFormatter lengthFormatter;
    protected IFieldValidator validator;
    protected IFormatter formatter;
    protected Adjuster adjuster;
    private Sensitiser sensitiser;

    public FieldDescriptor(ILengthFormatter lengthFormatter, IFieldValidator validator) throws Exception {
        this(lengthFormatter, validator, new AsciiFormatter(), null, null);
    }

    public FieldDescriptor(ILengthFormatter lengthFormatter, IFieldValidator validator, Adjuster adjuster)
            throws Exception {
        this(lengthFormatter, validator, new AsciiFormatter(), adjuster, null);
    }

    public FieldDescriptor(ILengthFormatter lengthFormatter, IFieldValidator validator, IFormatter formatter,
                           Adjuster adjuster, Sensitiser sensitiser) {
        if (formatter instanceof BinaryFormatter && !(validator instanceof HexFieldValidator)) {
            throw new FieldDescriptorException("A Binary field must have a hex validator");
        }

        if (formatter instanceof BcdFormatter && !(validator instanceof NumericFieldValidator) && !(validator instanceof BcdFieldValidator)) {
            throw new FieldDescriptorException("A BCD field must have a numeric or BCD validator");
        }

        this.lengthFormatter = lengthFormatter;
        this.validator = validator;
        this.formatter = formatter;
        this.adjuster = adjuster;
        this.sensitiser = sensitiser;
    }

    public static IFieldDescriptor getAsciiAlphaNumeric(int length) throws Exception {
        return new FieldDescriptor(new FixedLengthFormatter(length), FieldValidators.getAnsp(),
                new PadRightSetAdjuster(length, ' '));
    }

    public static IFieldDescriptor getAsciiAmount(int length) throws Exception {
        return new FieldDescriptor(new FixedLengthFormatter(length), FieldValidators.getRev87Amount(),
                new PadLeftSetAdjuster(length, '0'));
    }

    public static IFieldDescriptor getAsciiAns(int length) throws Exception {
        return new FieldDescriptor(new FixedLengthFormatter(length), FieldValidators.getAns(), null);
    }

    public static IFieldDescriptor getAsciiFixed(int length, IFieldValidator validator) throws Exception {
        return new FieldDescriptor(new FixedLengthFormatter(length), validator, Formatters.getAscii(), null, null);
    }

    public static IFieldDescriptor getAsciiLLCharacter(int maxLength) throws Exception {
        return getAsciiVar(2, maxLength, FieldValidators.getAns());
    }

    public static IFieldDescriptor getAsciiLLLBinary(int length) throws Exception {
        return new FieldDescriptor(new VariableLengthFormatter(3, length), FieldValidators.getHex(),
                Formatters.getBinary(), null, null);
    }

    public static IFieldDescriptor getAsciiLLLCharacter(int maxLength) throws Exception {
        return getAsciiVar(3, maxLength, FieldValidators.getAns());
    }



    public static IFieldDescriptor getAsciiLLLNumeric(int maxLength) throws Exception {
        return getAsciiVar(3, maxLength, FieldValidators.getN());
    }

    public static IFieldDescriptor getAsciiLLNumeric(int maxLength) throws Exception {
        return getAsciiVar(2, maxLength, FieldValidators.getN());
    }

    public static IFieldDescriptor getAsciiN(int length) throws Exception {
        return new FieldDescriptor(new FixedLengthFormatter(length), FieldValidators.getN(), null);
    }

    public static IFieldDescriptor getAsciiNumeric(int length) throws Exception {
        return new FieldDescriptor(new FixedLengthFormatter(length), FieldValidators.getN(), new PadLeftSetAdjuster(
                length, '0'));
    }

    public static IFieldDescriptor getAsciiVar(int lengthIndicator, int maxLength, IFieldValidator validator)
            throws Exception {
        return new FieldDescriptor(new VariableLengthFormatter(lengthIndicator, maxLength), validator, null);
    }

    public static IFieldDescriptor getBcdFixed(int length) throws Exception {
        return new FieldDescriptor(new FixedLengthFormatter(length), FieldValidators.getN(), Formatters.getBcdFixed(), null, null);
    }

    public static IFieldDescriptor getExpiryFixed(int length) throws Exception {
        return new FieldDescriptor(new FixedLengthFormatter(length), FieldValidators.getBcd(), Formatters.getBcdFixed(), null, null);
    }

    public static IFieldDescriptor getBcdVar(int lengthIndicator, int maxLength) throws Exception {
        return new FieldDescriptor(new VariableLengthFormatter(lengthIndicator, maxLength, Formatters.getBcdFixed() ),
                FieldValidators.getBcd(), Formatters.getBcdVar(), null, null);
    }

    public static IFieldDescriptor getBinaryFixed(int length) {
        return new FieldDescriptor(new FixedLengthFormatter(length), FieldValidators.getHex(),
                Formatters.getBinary(), null, null);
    }

    public static IFieldDescriptor getBinaryVar(int lengthIndicator, int maxLength, IFormatter lengthFormatter)
            throws Exception {
        return new FieldDescriptor(new VariableLengthFormatter(lengthIndicator, maxLength, lengthFormatter),
                FieldValidators.getHex(), Formatters.getBinary(), null, null);
    }

    public static IFieldDescriptor getPanMask(IFieldDescriptor decoratedFieldDescriptor) {
        return new PanMaskDecorator(decoratedFieldDescriptor);
    }

    public String display(String prefix, int fieldFieldNr, String value) throws Exception {
        String fieldValue = value == null ? "" : value;

        StringBuilder sb = new StringBuilder();

        sb.append(prefix);
        sb.append(IsoUtils.padLeft("" + fieldFieldNr, 3, '0'));
        sb.append("(");
        sb.append(IsoUtils.padLeft("" + fieldValue.length(), 3, '0'));
        sb.append(")");
        sb.append("[");
        if (sensitiser != null) {
            sb.append(sensitiser.sensitise(fieldValue));
        } else {
            sb.append(fieldValue);
        }
        sb.append("]");



        return sb.toString();
    }

    public String display(String prefix, String fieldNrPrefix, int fieldFieldNr, String value) throws Exception {
        String fieldValue = value == null ? "" : value;

        StringBuilder sb = new StringBuilder();

        sb.append(prefix);
        sb.append(fieldNrPrefix);
        sb.append(IsoUtils.padLeft("" + fieldFieldNr, 3, '0'));
        sb.append("(");
        sb.append(IsoUtils.padLeft("" + fieldValue.length(), 3, '0'));
        sb.append(")");
        sb.append("[");
        if (sensitiser != null) {
            sb.append(sensitiser.sensitise(fieldValue));
        } else {
            sb.append(fieldValue);
        }
        sb.append("]");


        return sb.toString();
    }

    public Adjuster getAdjuster() {
        return adjuster;
    }

    public IFormatter getFormatter() {
        return formatter;
    }

    public ILengthFormatter getLengthFormatter() {
        return lengthFormatter;
    }

    public int getPackedLenghth(String value) throws Exception {
        if (lengthFormatter.getLengthOfLengthIndicator() == 0) {
            // fixed width fields are specified in their native
            int fieldLength = Integer.parseInt(lengthFormatter.getMaxLength());
            // fixed width field. e.g. if binary 8 field, source for this would be 16 chars/bytes, then getPackedLength will half this back to 8
            return formatter.getPackedLength(formatter.getSourceFieldLenForPackedLength(fieldLength));
        } else {
            return lengthFormatter.getLengthOfLengthIndicator() + formatter.getPackedLength(value.length());
        }
    }

    public IFieldValidator getValidator() {
        return validator;
    }

    public byte[] pack(int fieldNumber, String value) throws Exception {
        int inputValueLen = value.length();
        if (!lengthFormatter.isValidLength( formatter.getInputLength(inputValueLen))) {
            throw new FieldFormatException("" + fieldNumber, "The field length is not valid");
        }

        if (!validator.isValid(value)) {
            throw new FieldFormatException("" + fieldNumber, "Invalid value for field");
        }

        int lenOfLenInd = lengthFormatter.getLengthOfLengthIndicator();
        int lenOfFieldBytes = formatter.getPackedLength(inputValueLen);

        byte[] field = new byte[lenOfLenInd + lenOfFieldBytes];
        lengthFormatter.pack(field, formatter.getInputLength(inputValueLen), 0);

        byte[] fieldData = formatter.getBytes(value);
        System.arraycopy(fieldData, 0, field, lenOfLenInd, lenOfFieldBytes);
        return field;
    }

    public UnpackObject unpack(int fieldNumber, byte[] data, int offset, boolean validateData) {
        try {
            int lenOfLenInd = lengthFormatter.getLengthOfLengthIndicator();
            int lenOfField = lengthFormatter.getLengthOfField(data, offset);
            int packedLength = lenOfField;
            if (formatter instanceof BcdFormatter ) {
                packedLength = formatter.getPackedLength(packedLength);
            }

            byte[] fieldData = new byte[packedLength];
            System.arraycopy(data, offset + lenOfLenInd, fieldData, 0, packedLength);
            int newOffset = offset + packedLength + lenOfLenInd;

            String value = formatter.getString(fieldData,lenOfField);

            if (validateData) {
                if (!validator.isValid(value)) {
                    throw new FieldFormatException("" + fieldNumber, "Invalid field format, value " + value);
                }

                int length = value.length();

                if (formatter instanceof BinaryFormatter) {
                    length = formatter.getPackedLength(length);
                }
                if (!lengthFormatter.isValidLength(length)) {
                    throw new FieldFormatException("" + fieldNumber, "Field is too long");
                }
            }

            return new UnpackObject(value, newOffset);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            FieldUnpackException fex = new FieldUnpackException("" + fieldNumber, data, offset, e);
            throw fex;
        }

    }

}
