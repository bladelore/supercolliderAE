(
    t.tempo = 120/60;
    a = Conductor.new(name: "Witch", clock: t);
    a.targetSection_("test");
    // MIDIIn.connectAll;
    // Usage:
    // a.listen((type: \midiNote, note: 60));
    // a.listen((type: \midiCC, cc: 0));
    a.listen((type: \modality, device: k, key: \tr, button: \fwd));
    a.clearListeners;

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

a.debug

k.gui

(
    a = Conductor.new("names");
    // should return a0..z0, a1..z1, etc
    (26*2).do{a.label};
)

MIDIdef.all.postln;

97.asAscii

.fastForward