Server.killAll()


(
SynthDef(
    \sinPluck,
    {
        var env = EnvGen.kr(Env.perc(\atk.kr(0.01), 0.5), doneAction: 2);
        var freq = \freq.kr(440);
        // var sig = SyncSaw.ar(freq, [freq * 1.5, freq]) * env;
        var sig = SinOscFB.ar(freq.lag(100), \fb.kr(0)) * env;
        sig = RLPF.ar(sig, \lpf.kr(300) + ((env + 1) * 1200), 2);
        // sig = RLPF.ar(sig, 75, 2);
        // sig = (sig * 3).clip();
        // sig = (sig * \clip.kr(0.5)).tanh();
        sig = Pan2.ar(sig, \pan.kr(0));
        sig = sig * \gain.kr(0).dbamp;
        sig = sig * \amp.kr(1);
        Out.ar(\out.kr(0), sig);
    }
).add;

SynthDef(
    \sawPluck,
    { |freq = 440, syncMult = 1.5|
        var env = EnvGen.kr(Env.perc(\atk.kr(0.01), 0.3), doneAction: 2);
        // EnvGen.kr(Env.perc(0.01, 0.3), doneAction: 2);
        var sig = SyncSaw.ar(freq, [freq * syncMult, freq]) * env;
        sig = RLPF.ar(sig, \lpf.kr(300) + ((env + 1) * 1200), 2);
        sig = Balance2.ar(sig[0], sig[1], \pan.kr(0));
        sig = sig * \gain.kr(0).dbamp;
        sig = sig * \amp.kr(1);
        Out.ar(0, sig);
    }
).add;
)

Pan2

120/t.tempo

(
x.stop;
x = {
    var sectionLength, reverb, sample, bowl, backyard, backyard2, stretch, kick, cut1, cut2, cut3, cut4, roar, morph, fftStretchLive, fftStretchLive2, synth, serum, grain;
    var verb, drum, filter, sineTracker;

    // reverb =
    // Pbindf(
    //     Pdef(\miVerb),
    //     \time, 0.4,
    //     \damp, 0.6,
    //     \hp, 0.2,
    //     \freeze, 0,
    //     \diff, 0.5,
    //     \gain, 0,
    //     \out, [~mainout]
    // );
    // reverb = reverb.play(t);

    filter = 
    Pbindf(
        Pdef(\filter),
        \blend, ~pmodenv.(Pseq([-0.5, 0.75], inf), 2, 1, \sine),
        // \freq, Pseg(Pseq([300, 800],inf), 4, 'lin' , inf),
        // \freq, ~pmodenv.(Pseq([50, 2000], inf), 8),
        \res, 0.5,
        \out, [~mainout]
    );
    filter = filter.play(t);
    
    a = Pbind(
        \instrument, Pseq([\sinPluck, \sinPluck], inf),
        \ctranspose, -1,
        \scale, Scale.chromatic.tuning_(\just),
        \amp, Pseq([0.5, 0.25, 0.25, 0.5, 0.25, 0.25, 0.5] * 0.2, inf),
        \atk, Pseq([0.1, 0.5], inf) * 1,
        \lpf, Pseq([300, 70, 300], inf),
        \syncMult, 0.5,
        // \fb, Pwhite(0,0.2,inf),
        // \clip, ~pmodenv.(Pseq([1, 2], inf), 4, 1, \sine),
        \octave, 4,
        \strum, Pseq([0.01, 0, 0, 0, 0.01], inf),
        // \strum, 0.01,
        \degree, Pseq([\Cm9_G, \Gm, \Cm_g, \Cm_eb, \Ebsus2, \Fsus4, \Cm9_G, \Fm9].chordProg, inf).stutter(28),
        \dur, 4/16,
        \pan, -1,
        \out, [~mainout, ~miVerb]
    ).play;

    b = Pbind(
        \instrument, Pseq([\sinPluck], inf),
        \ctranspose, -1,
        \scale, Scale.chromatic,
        // \clip, ~pmodenv.(Pseq([1, 2], inf), 6, 1, \sine),
        // \fb, Pwhite(0,0.2,inf),
        \amp, Pseq([0.5, 0.25, 0.25] * 0.2, inf),
        \atk, Pseq([0.05, 0.1, 0.2] * 1, inf),
        \lpf, Pseq([300, 80, 300, 30], inf),
        \syncMult, 0.5,
        // \strum, 0.01,
        \strum, Pseq([0.01, 0, 0, 0.01], inf),
        \octave, 4,
        \degree, Pseq([\Cm9_G, \Gm, \Cm_g, \Cm_eb, \Ebsus2, \Fsus4, \Cm9_G, \Fm9].chordProg, inf).stutter(24),
        // \dur, Pseq([2/16], inf).stutter(16),
        \dur, 3/16 * 4,
        \pan, 1,
        \out, [~mainout, ~miVerb]
    ).play;
}.fork;
)

(   
    a = Pbind(
        \instrument, \sinPluck,
        \scale, Scale.chromatic,
        \octave, 7,
        // \strum, 0.02,
        \degree, Pseq([\Csus2, \Cm9_G, \Gm, \Cm_g, \Cm_eb, \Ebsus2, \Fsus4, \Cm9_G, \Gm9].chordProg, inf).stutter(32),
        \dur, 2/16,
        \pan, -1,
    ).play;

    a = Pbind(
        \instrument, \sinPluck,
        \scale, Scale.chromatic,
        // \strum, 0.02,
        \octave, 7,
        \degree, Pseq([\Csus2, \Cm9_G, \Gm, \Cm_g, \Cm_eb, \Ebsus2, \Fsus4, \Cm9_G, \Gm9].chordProg, inf).stutter(24),
        \dur, 4/16,
        \pan, 1,
    ).play;
)

    // b = Pbind(
    //     \instrument, \sawPluck,
    //     \scale, Scale.chromatic,
    //     \octave, 4,
    //     \syncMult, 1.5,
    //     \degree, Pseq([\Csus2, \Cm9_G, \Gm, \Cm_g, \Cm_eb, \Ebsus2, \Fsus4, \Cm9_G, \Gm9].chordProg, inf).stutter(8),
    //     \dur, 5/8,
    // ).play;
)

ChordSymbol.shapes.keys