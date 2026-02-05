(
    ~maxGrains = 25;
    ~fftSize = 4096*8;
    if (~specBuf.notNil) {~specBuf.do { |b| if (b.notNil) { b.free } };};
    ~specBuf = Array.fill(~maxGrains, {Buffer.alloc(s, ~fftSize)});
    ~specBuf.do{|item| item.zero};

    ~bufA = Buffer.alloc(s, ~fftSize);
    ~bufB = Buffer.alloc(s, s.sampleRate * 0.01);
    ~bufC = Buffer.alloc(s, s.sampleRate * 0.025);

    ~specBuff_A=Dictionary();
    ~sewer=Dictionary();
    
    ~makeSpec.("/Users/aelazary/Desktop/Samples etc./feedback cymbals/feedback cymbals-4.wav", ~specBuff_A, 16384, 2);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Field recs/Vienna Sewer 3.wav", ~sewer, 0.3, \centroid, chans: [0,1]);
)


//IPF
(
    SynthDef(\ipfPerc, {
    |f0=60, beta=0.3, g_init=0.6, modRate=1, modStereo=100, 
     verbMix=0.1, amp=1, gate=1, sweep=8|

    var trig, g_prev, g, su, safeVal, g_out, md, freq, sig;
    var verb_time, verb_damp;
    var alpha;
    var pitchEnv = (1 + (sweep * Env.perc(0.0, 0.13, curve: -4).ar)) * XLine.ar(1, 0.6 , sweep.reciprocal);

    f0 = f0 * pitchEnv;

    alpha = SinOsc.ar(\alphaRate.kr(1)).linlin(-1, 1, 0.01, 9);
    // alpha = XLine.ar(\alphaStart.kr(0.1), \alphaEnd.kr(0.1), \alphaRate.kr(1));

    trig = Impulse.ar(f0);
    g_prev = LocalIn.ar(2);
    g = Select.ar((trig > 0), [K2A.ar(g_init), g_prev]);

    su = beta * exp(g - g_prev);
    safeVal = ((g - su) / alpha).max(0.00001);
    g_out = g - log(safeVal);

    LocalOut.kr(g_out);

    md = [modRate - (modStereo * 0.5), modRate + (modStereo * 0.5)];
    freq = f0 / (1 + g_out * md).max(0.001);

    sig = SinOsc.ar(freq);
    sig = sig * g_out;
    sig = sig.tanh;

    verb_time = LFNoise2.kr(0.3, 0.1, 1.03);
    verb_damp = LFNoise2.kr(0.2).range(0, 0.7);
    sig = MiVerb.ar(sig, verb_time, verbMix, verb_damp, 0.1);
    sig = Compander.ar(sig, sig,
        thresh: 1,
        slopeBelow: 1,
        slopeAbove: 1,
        clampTime: 0.01,
        relaxTime: 0.01
    );

    sig = sig * EnvGen.kr(Env.perc(\atk.kr(0.01), \dec.kr(1)), gate: gate, doneAction:2);
    // sig = SelectX.ar(1, [sig, Resonz.ar(sig.sanitize, freq: 50, mul: 1)]);
    
    sig = sig * \gain.kr(0).dbamp;
    sig = LeakDC.ar(sig, 0.995);
    sig = sig.sanitize;

    Out.ar(\out.kr(0), sig);
}).add;

//IPF perc phase
SynthDef(\ipfPerc_phase, {
    |f0=60, alphaStart=0.1, alphaEnd=1, alphaRate=1, beta=0, modRate=10, modStereo=50, index=0.01, verbMix=0.1, gate=1|
    
    var trig, g_state, g_out, g_prev, dg, md;
    var su, safeVal;
    var alpha;
    var op1, op2, phaseShift;
    var sig;
    var verb_time, verb_damp;
    var sweep = \sweep.kr(2);
    var pitchEnv = (1 + (sweep * Env.perc(0.0, 0.13, curve: -4).ar)) * XLine.ar(1, 0.6 , sweep.reciprocal);

    f0 = f0 * pitchEnv;
    
    g_state = LocalIn.ar(2);
    g_prev = g_state[1];

    alpha = SinOsc.ar(alphaRate).linexp(-1, 1, alphaStart.max(0.001), alphaEnd);
    // alpha = XLine.ar(alphaStart, alphaEnd , alphaRate);
    
    su = beta * exp(g_state[0] - g_prev);
    safeVal = ((g_state[0] - su) / alpha).max(0.00001);
    g_out = g_state[0] - log(safeVal);
    
    dg = (g_out - g_prev).abs;
    
    md = [modRate - (modStereo * 0.5), modRate + (modStereo * 0.5)];
    phaseShift = dg * md / f0;
    
    op1 = SinOsc.ar(f0, phaseShift * 2pi * f0) * g_out * index;
    op2 = SinOsc.ar(f0 * op1);
    
    sig = op2.tanh;
    
    LocalOut.ar([g_out, g_state[0]]);
    
    verb_time = LFNoise2.kr(0.3, 0.1, 1.03);
    verb_damp = LFNoise2.kr(0.2).range(0, 0.7);
    sig = MiVerb.ar(sig, verb_time, verbMix, verb_damp, 0.1);
    sig = Compander.ar(sig, sig,
        thresh: 1,
        slopeBelow: 1,
        slopeAbove: 1,
        clampTime: 0.01,
        relaxTime: 0.01
    );
    
    sig = sig * \amp.kr(1);

    sig = sig * EnvGen.kr(Env.perc(\atk.kr(0.01), \dec.kr(1)), gate: gate, doneAction:2);

    sig = sig * \gain.kr(0).dbamp;
    sig = LeakDC.ar(sig, 0.995);
    sig = sig.sanitize;
    
    Out.ar(\out.kr(0), sig);
}).add;
)

(
t = TempoClock.new(100/60).permanent_(true).schedAbs(0, {t.beatsPerBar_(4)});
x = {
    var ipf, specGrains, additive;
    var specBuff = ~specBuff_A;
    var sample = ~sewer;
    var morph;

    ~bufA.zero;
    ~bufB.zero;
    ~specBuf.do{|item| item.zero};

    morph = (
        Pbind(
            \gain, 0,
            \atk, 10,
            \rel, 100,
            // \swap, ~pmodenv.(Pseq([1, 0],inf), Pseq([2], inf), 1, \sine),
            // \swap, 1,
            \out, [~bus3]
        ) <>
        Pdef(\morph)
    ).play(t);

    Pdef(\bell,
        Pmono(\fftStretch_magFilter_mono,
            \dur, 0.1,
            \amp, ~pmodenv.(Pseq([0.1, 1], inf), 4, 1, \exp),
            \gain, -12,
            \buf, specBuff.at(\file),
            \analysis, [specBuff.at(\analysis)],
            \fftSize, specBuff.at(\fftSize),
            \rate, 0.5,

            \thresh, 10,
            \remove, 0,

            \thresh, ~pmodenv.(Pseq([10, 100], inf), 8, 1, \sin),
            \remove, ~pmodenv.(Pseq([1, 10], inf), 6, 1, \sin),

            // \pos, 0.4,
            \pos, ~pmodenv.(Pseq([0.4, 0.45], inf), Pseq([8,1],inf), 1, \exp),
            \len, 0.01,
            \pan, ~pmodenv.(Pwhite(-0.6, 0.6), 3, 1, \sin),
            \pitchRatio, 0.75 * 0.25,
            // \pitchRatio, 0.5,
            \out, [~convolve_B, ~bus3]
        )
    ).play(t);

    1.wait;
    
    Pdef(\p1,
        ~makeSubdivision.(
            PlaceAll([1, 1, 1, 1], inf),
            PlaceAll([4, 4, 4, 4], inf)
        )
    );

    Pdef(\p1_transform,
        ~filterBeat.(key: Pkey(\cyclecount), beat:[1, 4, 5, 9], mod: 3, reject: 1) <>
        ~pSkew.(Pdef(\p1), key: Pkey(\cyclecount), group: [1, 4], skew: [1], curve: \exp) <>
        Pdef(\p1)
    );

    Pdef(\ipf1,
        Pbind(
            \instrument, \ipfPerc,
            \f0, 69*8,
            \atk, 0,
            \dec, 1,
            \beta, Pkey(\groupdelta).linlin(0,1,1,0.1),
            \alphaRate, Pkey(\groupdelta).linexp(0,10,1,20),
            \beta, ~pmodenv.(Pwhite(0.2, 0.0, inf),  Pseq([2, 1, 0.5, inf])),
            \g_init, 1,
            \modRate, 16,
            \modStereo, ~pmodenv.(Pwhite(1, 4, inf),  Pseq([2, 1, 0.5, inf])),
            \amp, 1,
            \sweep, 16,
            \verbMix, 0.3,
            \out, [~bus2, ~spectralGrains, ~resonator, ~convolve_A]
        )
    );


    Pdef(\reso, (
        Pbind(\out, ~bus2, \atk, 0.01, \dec, 2, \distort, 0.5, \freq, 49, \inGain, -40, \gain, -12) <> 
        Pdef(\resonator)
    )).play;
    
    \a.postln;
    Pdef(\ipf, (Pdef(\ipf1) <> Pdef(\p1_transform))).play(t);

    Pdef(\specGrains, (
        Pbind(
            \srcbuf, ~bufA,
            \specbuf, [~specBuf],
            \fftSize, ~fftSize,
            \amp, 1,
            \dur, 0.1,
            \posRate, 1,
            \tFreq, 10,
            \tFreqMD, 3,
            \overlap, 12,
            \overdub, 0.5,
            \feedback, 0,
            \midipitch, 12,
            \spectralFilter, 0,
            \gain, 12,
            \out, [~bus1]
        ) <> Pdef(\spectralGrains1)
    )).play(t);

    3.wait;

    \pause.postln;
    Pdef(\ipf).stop(t);
    4.wait;

    \b.postln;
    Pbindef(\ipf1, Pbind(\f0, 60, \modRate, 3));

    Pdef(\p1_transform,
        ~pSkew.(Pdef(\p1), key: Pkey(\cyclecount), group: [1, 4], skew: [-1], curve: \exp) <>
        Pdef(\p1).finDur(6)
    );

    Pdef(\ipf).play(t);
    6.wait;

    \pause.postln;
    Pdef(\ipf).stop(t);
    4.wait;

    \c.postln;
    Pdef(\specGrains).stop;

    // ~bufB.zero;
    
    Pdef(\specGrains, (
        Pbind(
            \srcbuf, ~bufB,
            \specbuf, [~specBuf],
            \fftSize, ~fftSize,
            \amp, 1,
            \dur, 0.1,
            \posRate, 0.5,
            \tFreq, 200,
            \tFreqMD, 1,
            \overlap, 12,
            \overdub, 0.5,
            \feedback, 0,
            \midipitch, 0,
            \spectralFilter, 0.1,
            \gain, 0,
            \out, ~bus1
        ) <> Pdef(\spectralGrains1)
    )).play(t);
    
    Pdef(\ipf2,
        Pbind(
            \instrument, \ipfPerc,
            // \dur, 1,
            \f0, 69,
            \atk, ~pmodenv.(Pwhite(0, 0.1, inf),  0.5),
            \dec, 1,
            \beta, Pkey(\groupdelta).linlin(0,1,1,0.1),
            \alphaRate, 32,
            \g_init, 1,
            \modRate, ~pmodenv.(Pseq([16, 3], inf),  4),
            \modStereo, ~pmodenv.(Pwhite(1, 4, inf),  Pseq([2, 1, 0.5, inf])),
            \sweep, 16,
            // \verbMix, ~pmodenv.(Pwhite(0.1, 0.8, inf),  0.5),
            \verbMix, 0.1,
            \out, [~bus2, ~spectralGrains, ~resonator]
        )
    );

    Pdef(\p1_transform,
        ~filterBeat.(key: Pkey(\groupcount), beat:[1, 3], mod: 3, reject: 0) <>
        ~pSkew.(Pdef(\p1), key: Pkey(\cyclecount), group: [1, 2, 3], skew: [-1, 1, -1, 1], curve: \exp) <>
        Pdef(\p1)
    );

    Pdef(\ipf, (Pdef(\ipf2) <> Pdef(\p1_transform))).play(t);

    8.wait;

    \pause.postln;
    Pdef(\ipf).stop(t);
    4.wait;
    
    Pdef(\ipf2,
        Pbind(
            \instrument, \ipfPerc,
            // \dur, 1,
            \f0, 69,
            \atk, ~pmodenv.(Pwhite(0, 0.1, inf),  0.5),
            \dec, 1,
            \beta, Pkey(\groupdelta).linlin(0,1,1,0.1),
            \alphaRate, 32,
            \g_init, 1,
            \modRate, ~pmodenv.(Pseq([16, 3], inf),  4),
            \modStereo, ~pmodenv.(Pwhite(1, 4, inf),  Pseq([2, 1, 0.5, inf])),
            \sweep, 16,
            \verbMix, ~pmodenv.(Pwhite(0.1, 0.8, inf),  0.5),
            // \verbMix, 0.1,
            \out, [~bus2, ~spectralGrains, ~resonator]
        )
    );

    Pdef(\ipf, (Pdef(\ipf2) <> Pdef(\p1_transform))).play(t);

    \d1.postln;

    16.wait;

    \d2.postln;
    Pdef(\ipf).stop;

    Ndef(\sample, { PlayBuf.ar(2, sample.at(\file), startPos: 200000, rate: 1) * -12.dbamp;}).set(\out, ~bus4);
    Ndef(\sample).play;

    16.wait;

    \pause.postln;

    Ndef(\sample).stop;

    2.wait;

    Ndef(\sample, { PlayBuf.ar(2, sample.at(\file), startPos: 200000, rate: 0.5) * -12.dbamp;}).set(\out, ~bus4);
    Ndef(\sample).play;

    16.wait;

    Ndef(\sample).stop;

    2.wait;

    \d3.postln;

    Pdef(\bell).stop;
    Pdef(\bell,
        Pmono(\fftStretch_magFilter_mono,
            \dur, 0.1,
            // \amp, ~pmodenv.(Pseq([0.1, 1], inf), 0.05, 1, \sin),
            \gain, -3,
            \buf, specBuff.at(\file),
            \analysis, [specBuff.at(\analysis)],
            \fftSize, specBuff.at(\fftSize),
            \rate, 3,
            // \filter, 0,
            \pos, 0.4,
            \pos, ~pmodenv.(Pseq([0.4, 0.5], inf), 3, 1, \sin),
            \len, 0.02,
            \pan, ~pmodenv.(Pseq([0,1], inf), 0.5, 1, \sin),
            \pitchRatio, 0.25,
            \thresh, ~pmodenv.(Pseq([10, 100], inf), 8, 1, \sin),
            \remove, ~pmodenv.(Pseq([1, 10], inf), 6, 1, \sin),
            \out, [~spectralGrains]
        )
    ).play(t);

    Ndef(\sample, { PlayBuf.ar(2, sample.at(\file), startPos: 200000, rate: 1);}).set(~bus1);
    Ndef(\sample).play;

    Pdef(\p2,
        ~makeSubdivision.(
            PlaceAll([2, 1.5, 1.5] * 1.5, inf),
            PlaceAll([4, 3, 4, 4], inf)
        )
    );

    Pdef(\specSample,
        Pbind(
            \instrument, \specSlicer,
            \amp, 0.2,
            \atk, Pwhite(4, 1),
            \rel, Pwhite(2, 4, inf),
            // \dur, 0.25,
            \rate, 1 - Pkey(\cycledelta),
            \rate, Prand([0.5, 1], inf),
            \oneshot, 1,
            \swap, ~pmodenv.(Pwhite(-1, 1, inf), Pkey(\dur)),
            \pan, ~pmodenv.(Pwhite(-1, 1, inf), Pkey(\dur)),
            \smooth, ~pmodenv.(Pwhite(0, 2, inf), Pkey(\dur)),
            \buf, sample.at(\file),
            \offset, Pstep(Pwhite(0, 100,inf).round, 16),
            \slice_A, ~pGetSlice.((Pseries(1, 64, inf).stutter(1) + Pkey(\offset)), sample),
            \slice_B, ~pGetSlice.((Pseries(1, 64, inf).stutter(1) + Pkey(\offset)) + 1, sample),
            \out, [~bus2, ~spectralGrains],
        )
    );

    Pdef(\ipf3,
        Pbind(
            \instrument, \ipfPerc_phase,
            // \dur, 1,
            // \f0, Pstep([60, 60*2], 16, inf),
            \f0, ~pmodenv.(Pstep([60, 60*2], 16, inf), 8),
            \index, Pwhite(0.0001, 0.0003, inf),
            \atk, 0,
            \dec, 1,
            \alphaRate, Pkey(\groupdelta).linexp(0,10,1,20),
            // \alphaRate, 4,
            \beta, ~pmodenv.(Pwhite(0.1, 0.2, inf),  Pseq([2, 1, 0.5, inf])),
            \g_init, 0.4,
            \modRate, 10,
            \modStereo, 1,
            \amp, 1,
            \sweep, 16,
            \verbMix, 0.1,
            \gain, 0,
            \out, ~bus2
        )
    );

    Pdef(\p1_transform,
        // ~filterBeat.(key: Pkey(\cyclecount), beat:[1, 3], mod: 3, reject: 1) <>
        // Pbind(\f0, 500)<>
        ~pSkew.(Pdef(\p2), key: Pkey(\eventcount), group: [1, 2, 3], skew: [-1, 1, -1, 1], curve: \exp) <>
        Pdef(\p2)
    );

    Pdef(\ipf,
            Pswitch1([Pdef(\ipf3), Pdef(\ipf1), Pdef(\ipf2), Pdef(\specSample)], PlaceAll([1, 4, 4] - 1, inf)) <>
            Pdef(\p1_transform)
    ).play(t);

    32.wait;

    \e.postln;

    Ndef(\sample).stop(fadeTime: 10);

    Pdef(\specGrains).stop;

    ~bufB.zero;
    
    Pdef(\specGrains).stop;
    Pdef(\specGrains, (
        Pbind(
            \srcbuf, ~bufB,
            \specbuf, [~specBuf],
            \fftSize, ~fftSize,
            \amp, 1,
            \dur, 0.1,
            \posRate, 0.5,
            \tFreq, 200,
            \tFreqMD, 0,
            \overlap, 12,
            \overdub, 0.5,
            \feedback, 0,
            \midipitch, 12,
            \spectralFilter, 0.1,
            \gain, 0,
            \out, ~bus1
        )
        <> Pdef(\spectralGrains1)
    )).play(t);

    Pbindef(\ipf3, \out, [~bus2, ~spectralGrains]);

    32.wait;

    \f.postln;

    Pdef(\bell).stop;
        Pdef(\bell,
            Pmono(\fftStretch_magBelow_mono,
                \dur, 0.1,
                \amp, ~pmodenv.(Pseq([0.4, 1], inf), 4, 1, \sin),
                \gain, -3,
                \buf, specBuff.at(\file),
                \analysis, [specBuff.at(\analysis)],
                \fftSize, specBuff.at(\fftSize),
                \rate, 0.5,
                \filter, 40,
                \pos, 0.4,
                // \pos, ~pmodenv.(Pwhite(0.4, 0.5, inf), 0.1, 1, \sin),
                \len, 0.01,
                \pan, ~pmodenv.(Pwhite(-0.6, 0.6), 3, 1, \sin),
                \pitchRatio, 0.5,
                \out, [~bus3, ~spectralGrains]
            )
    ).play(t);

    Pdef(\specGrains).stop;
    
    ~bufC.zero;

    Pdef(\specGrains, (
        Pbind(
            \srcbuf, ~bufC,
            \specbuf, [~specBuf],
            \fftSize, ~fftSize,
            \amp, 1,
            \dur, 0.1,
            \posRate, 0.1,
            \tFreq, 10,
            \tFreqMD, 0,
            \overlap, 12,
            \overdub, 0.5,
            \feedback, 0,
            \midipitch, 12,
            \companderMD, 1,
            \spectralFilter, 0.05,
            \gain, 6,
            \out, ~bus1
        ) <> Pdef(\spectralGrains1)
    )).play(t);

    Pdef(\ipf).stop;

    Pdef(\ipf4,
        Pbind(
            \instrument, \ipfPerc_phase,
            // \dur, 1,
            \f0, Pstep([60], 8, inf),
            \index, 0.0001*0.5,
            // \atk, Pkey(\cycledelta).linlin(0,1,0.3,0),
            \atk, 0.2,
            \dec, Pkey(\dur),
            \alphaRate, Pkey(\groupdelta).linexp(0,10,1,20),
            \beta, Pkey(\groupdelta).linlin(0,1,1,0.1),
            \alphaRate, 4,
            \g_init, 0.4,
            \modRate, 10,
            \modStereo, 1,
            \amp, 1,
            \sweep, 16,
            \verbMix, 0.1,
            \gain, 0,
            \out, [~bus2, ~spectralGrains]
        )
    );

    Pdef(\p2,
        ~makeSubdivision.(
            PlaceAll([1, 1, 1] * 2, inf),
            PlaceAll([4, 2, 4, 2, 1], inf)
        )
    );

    Pdef(\p1_transform,
        // ~filterBeat.(key: Pkey(\cyclecount), beat:[1, 4, 5, 9], mod: 3, reject: 1) <>
        ~pSkew.(Pdef(\p2), key: Pkey(\cyclecount), group: [1, 2, 3, 4], skew: [-1, 1, -1, 1], curve: \exp) <>
        // Pswitch1([Pdef(\p2), Pdef(\p1)], Pstep(Pseq([0, 1], 32)))
        Pdef(\p2)
    );

    Pdef(\ipf, (Pdef(\ipf4) <> Pdef(\p1_transform))).play(t);

    30.wait;

    Pbindef(\ipf4, \sweep, ~pmodenv.(Pseq([0, 16], inf), 6, 1, \sin));

    Pdef(\specSample,
        Pbind(
            \instrument, \specSlicer,
            \amp, 0.2,
            \gain, -6,
            \atk, 0,
            // \dur, 0.25,
            \rel, 0.25,
            \rate, 1 - Pkey(\cycledelta) * 0.5,
            // \rate, Prand([0.5, 1], inf),
            \oneshot, 1,
            \swap, ~pmodenv.(Pwhite(-1, 1, inf), Pkey(\dur)),
            \pan, ~pmodenv.(Pwhite(-1, 1, inf), Pkey(\dur)),
            \smooth, ~pmodenv.(Pwhite(0, 2, inf), Pkey(\dur)),
            \buf, sample.at(\file),
            \offset, Pstep(Pwhite(0, 100,inf).round, 16),
            \slice_A, ~pGetSlice.((Pseries(1, 64, inf).stutter(1) + Pkey(\offset)), sample),
            \slice_B, ~pGetSlice.((Pseries(1, 64, inf).stutter(1) + Pkey(\offset)) + 1, sample),
            \out, [~bus2, ~spectralGrains],
        ) <> 
        
        // ~pSkew.(Pdef(\p2), key: Pkey(\cyclecount), group: [1, 2, 3, 4], skew: [-1, 1, -1, 1], curve: \exp) <>
        ~makeSubdivision.(
            PlaceAll([1, 1, 1], inf),
            PlaceAll([4, 4, 4], inf)
        )
    );

    Pdef(\specSample).play(t);

    64.wait;

    Pdef(\bell,
        Pmono(\fftStretch_magAbove_mono,
            \dur, 0.1,
            \amp, ~pmodenv.(Pseq([0.8, 1], inf), 4, 1, \sin),
            \gain, 0,
            \buf, specBuff.at(\file),
            \analysis, [specBuff.at(\analysis)],
            \fftSize, specBuff.at(\fftSize),
            \rate, 1,
            \filter, 0.5,
            \pos, 0.5,
            \len, 0.2,
            \pan, ~pmodenv.(Pwhite(-0.6, 0.6), 3, 1, \sin),
            \out, [~bus3]
        )
    ).play(t);

    16.wait;

    Pdef(\bell).stop;
    Pdef(\bell,
        Pmono(\fftStretch_magAbove_mono,
            \dur, 0.1,
            \amp, 1,
            \gain, 0,
            \buf, specBuff.at(\file),
            \analysis, [specBuff.at(\analysis)],
            \fftSize, specBuff.at(\fftSize),
            \rate, 1,
            \filter, 0.5,
            \pos, 0.5,
            \len, 0.2,
            \pan, ~pmodenv.(Pwhite(-0.6, 0.6), 3, 1, \sin),
            \out, [~spectralGrains]
        )
    ).play(t);

    Pdef(\specGrains).stop;
    
    ~bufC.zero;

    Pdef(\specGrains, (
        Pbind(
            \srcbuf, ~bufA,
            \specbuf, [~specBuf],
            \fftSize, ~fftSize,
            \amp, 1,
            \dur, 0.1,
            \posRate, 1,
            \tFreq, 400,
            // \tFreqMD, 1,
            \overlap, 12,
            \overdub, 0,
            \feedback, 0,
            \midipitch, -12,
            \companderMD, 10,
            \spectralFilter, 0.5,
            \gain, 6,
            \out, ~bus1
        ) 
        <> Pdef(\spectralGrains1)
    )).play(t);

    Pdef(\specSample).stop;

    16.wait;

    Pdef(\ipf).stop;

    Ndef(\droneA).clear;
    Ndef(\droneA).play;
    Ndef(\droneA).fadeTime = 20;
    
    Ndef(\droneA, {
        var chain, sig;
        var ampDrift;
        chain = ~initHarmonicsChain.(harmonics: 4, sidebands: 4, freq: 60);
        chain = ~padSynthDistribution.(
            chain, 
            harmonicRatio: 2,
            bw: 5000,
            bwScale: 1,
            bwSkew:  0,
            stretch: 1,
            windowSkew: 0.5
        );
    
        chain = ~notchFilter.(chain, SinOsc.ar(0.5).linlin(-1,1,50,1000), 2000);
        chain = ~addLimiter.(chain);
    
        ampDrift = LFNoise2.ar(2 ! chain[\numPartials]) * 0.1;
        sig = LFSaw.ar(
            freq: chain[\freqs],
            iphase: ({ Rand(0, 2pi) } ! chain[\numPartials]) * SinOsc.ar(0.1).unipolar,
            mul: chain[\amps]
        );
        
        sig = sig[0,2..].sum + ([-1,1] * sig[1,3..].sum);

        sig = sig * -30.dbamp;

        sig = Compander.ar(sig, sig,
            thresh: 0.5,
            slopeBelow: 1,
            slopeAbove: 0.1,
            clampTime:  0.01,
            relaxTime:  0.01
        );
    }).set(\out, ~bus3);

    64.wait;

    Pdef(\specGrains).stop;

}.fork(t);
)