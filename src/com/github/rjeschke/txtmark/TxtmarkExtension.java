package com.github.rjeschke.txtmark;

/**
 * Externally-defined markdown extensions for Txtmark.
 *
 * Created by billf on 11/7/16.
 */
public abstract class TxtmarkExtension<ContextType> {

    public TxtmarkExtension() {
    }

    /**
     * If block is something this extension wants to handle, emit it to out and
     * return true.
     *
     * @param emitter  The current emitter, which can be useful for e.g. extensionEmitText()
     * @param context  The path to the root of the site being generated, e.g. "../"
     */
    public boolean emitIfHandled(Emitter<ContextType> emitter, StringBuilder out, Block block,
                                 ContextType context) {
        return false;
    }
}
