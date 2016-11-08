package com.github.rjeschke.txtmark;

/**
 * Externally-defined markdown extensions for Txtmark.
 *
 * Created by billf on 11/7/16.
 */
public abstract class TxtmarkExtension {

    public TxtmarkExtension() {
    }

    /**
     * If block is something this extension wants to handle, emit it to out and
     * return true.
     *
     * @param emitter  The current emitter, which can be useful for e.g. extensionEmitText()
     */
    public boolean emitIfHandled(Emitter emitter, StringBuilder out, Block block) {
        return false;
    }
}
