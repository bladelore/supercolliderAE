(
    t.tempo = 120/60;
    a = Conductor.new("Witch", t);
    a.skipTo("custom");
    a.modalityListener(k, \tr, \fwd);

    a.debug;

    x = {
        a.label;
        
            Pdef(\test, Pbind(\dur, 1)).play(t);
            
        a.wait;

        a.label;

            Pdef(\test).stop;

            "starting tempo change".postln;
            a.rampTempo(240/60, 8, \exp);

        a.wait;

        a.label("custom");

            Pdef(\test).play(t);

            a.rampTempo(120/60, 8, \exp);
            "-> skipped to here".postln;

        a.wait;

        a.label("test");

            "...continuing".postln;

        a.wait;

        a.debug;

    }.fork
)

(
    a = Conductor.new("names");
    // should return a0..z0, a1..z1, etc
    (26*2).do{a.label};
)

MIDIdef.all.postln;

97.asAscii

.fastForward