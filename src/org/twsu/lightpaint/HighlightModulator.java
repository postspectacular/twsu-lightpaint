package org.twsu.lightpaint;

import toxi.color.ReadonlyTColor;
import toxi.color.TColor;
import toxi.math.waves.AbstractWave;
import toxi.math.waves.SineWave;

public class HighlightModulator {

    private AbstractWave wave;
    private ReadonlyTColor c1, c2;

    public HighlightModulator(ReadonlyTColor c1, ReadonlyTColor magenta) {
        this.wave = new SineWave(0, 0.2f, 1f / 3, 2f / 3);
        this.c1 = c1;
        this.c2 = magenta;
    }

    public ReadonlyTColor getOffColor() {
        return c1;
    }

    public ReadonlyTColor getOnColor() {
        return c2;
    }

    public void setOffColor(ReadonlyTColor c1) {
        this.c1 = c1;
    }

    public void setOnColor(ReadonlyTColor c2) {
        this.c2 = c2;
    }

    public TColor updateColor() {
        return c1.copy().blend(c2, wave.update());
    }
}
