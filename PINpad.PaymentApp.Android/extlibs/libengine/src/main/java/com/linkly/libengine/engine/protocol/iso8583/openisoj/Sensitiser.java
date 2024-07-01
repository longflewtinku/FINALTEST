package com.linkly.libengine.engine.protocol.iso8583.openisoj;

/**
 * An interface describing how a field sensitiser works
 *
 * @author John Oxley &lt;john.oxley@gmail.com&gt;
 */
public interface Sensitiser {
    String sensitise(String data);
}
