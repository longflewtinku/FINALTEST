package com.linkly.libengine.engine.protocol.svfe.openisoj;

import com.linkly.libengine.engine.protocol.svfe.openisoj.fieldvalidator.IFieldValidator;
import com.linkly.libengine.engine.protocol.svfe.openisoj.formatter.IFormatter;
import com.linkly.libengine.engine.protocol.svfe.openisoj.lengthformatters.ILengthFormatter;

public interface IFieldDescriptor {
    public String display(String prefix, int fieldFieldNr, String value) throws Exception;

    public String display(String prefix, String fieldNrPrefix, int fieldFieldNr, String value) throws Exception;

    public Adjuster getAdjuster();

    public IFormatter getFormatter();

    public ILengthFormatter getLengthFormatter();

    public int getPackedLenghth(String value) throws Exception;

    public IFieldValidator getValidator();

    public byte[] pack(int fieldNumber, String value) throws Exception;

    public UnpackObject unpack(int fieldNumber, byte[] data, int offset);
}
