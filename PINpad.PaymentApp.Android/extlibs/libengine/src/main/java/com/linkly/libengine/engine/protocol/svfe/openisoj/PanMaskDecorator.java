package com.linkly.libengine.engine.protocol.svfe.openisoj;

import com.linkly.libengine.engine.protocol.svfe.openisoj.fieldvalidator.IFieldValidator;
import com.linkly.libengine.engine.protocol.svfe.openisoj.formatter.IFormatter;
import com.linkly.libengine.engine.protocol.svfe.openisoj.lengthformatters.ILengthFormatter;

public class PanMaskDecorator implements IFieldDescriptor {

    private IFieldDescriptor _decoratedFieldDescriptor;

    public PanMaskDecorator(IFieldDescriptor decoratedFieldDescriptor) {
        _decoratedFieldDescriptor = decoratedFieldDescriptor;
    }

    public String display(String prefix, int fieldFieldNr, String value)
            throws Exception {
        return _decoratedFieldDescriptor.display(prefix, fieldFieldNr,
                IsoUtils.maskPan(value));
    }

    public Adjuster getAdjuster() {
        return _decoratedFieldDescriptor.getAdjuster();
    }

    public IFormatter getFormatter() {
        return _decoratedFieldDescriptor.getFormatter();
    }

    public ILengthFormatter getLengthFormatter() {
        return _decoratedFieldDescriptor.getLengthFormatter();
    }

    public int getPackedLenghth(String value) throws Exception {
        return _decoratedFieldDescriptor.getPackedLenghth(value);
    }

    public IFieldValidator getValidator() {
        return _decoratedFieldDescriptor.getValidator();
    }

    public byte[] pack(int fieldNumber, String value) throws Exception {
        return _decoratedFieldDescriptor.pack(fieldNumber, value);
    }

    public UnpackObject unpack(int fieldNumber, byte[] data, int offset) {
        return _decoratedFieldDescriptor.unpack(fieldNumber, data, offset);
    }

    public String display(String prefix, String fieldNrPrefix,
                          int fieldFieldNr, String value) throws Exception {
        return display(prefix, fieldFieldNr, value);
    }

}
