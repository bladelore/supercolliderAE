(
    ~sliceBuf = Dictionary();
    ~sliceBuf2 = Dictionary();
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Missing Sounds 2016/04-Hobble_Break_126_PL_1.WAV", ~sliceBuf, 0.1, \centroid);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./radioaporee/12011108amlampostwiresa33road.wav", ~sliceBuf2, 0.1, \centroid);
)

(
    ~specBuff=Dictionary();
    ~specBuff2=Dictionary();
    // ~makeSpec.("/Users/aelazary/Desktop/Samples etc./NEW sample lib/mother and daughter singing o magnum mysterium-sfLDOVcK7nU.wav", ~specBuff, 16384, 2)
    ~makeSpec.("/Users/aelazary/Desktop/Samples etc./NEW sample lib/backyard oct 11 2018.wav", ~specBuff, 16384, 2);
    ~makeSpec.("/Users/aelazary/Desktop/Samples etc./Field recs/Brush metal bowl small 2.wav", ~specBuff2, 16384, 2);
)

(
    Pdef(\kick, 
        Pbind(
            // \dur, 1,
            \instrument, Prand([\fmPerc2, \fmPerc3], inf),
            \freq, Pseq([60.0], inf),
            \atk, 0.01,
            \dec, Pkey(\dur) * 0.5,
            \fb, 0.4,
            \index1, 1,
            \index2, 2,
            \ratio1, 0.5,
            \ratio2, 2,
            \drive, 0,
            \drivemix, 0,
            \sweep, 8.0,
            \spread, 20.0,
            \noise, 0.25,
            \feedback, Pkey(\cycledelta) * 1,
            \fbmod, 1,
            \pulseWidth, 1 - Pkey(\groupdelta).linlin(0,1, 0.1, 0.99),
            \lofreq, 500.0,
            \lodb, 10.0,
            \midfreq, 1200.0,
            \middb, 0.0,
            \hifreq, 7000.0,
            \hidb, 30.0,
            \gain, -20.0,
            \pan, 0.0,
            // \hpf, 
            \amp, Pkey(\groupdelta).linlin(0, 1, 1, 0.3),
            // \amp, 1,
            \out, [~mainout]
        )
    );

    Pdef(\cut1,
        Pbind(
            \instrument, \segPlayer,
            \amp, 1,
            \atk, 0.01,
            // \amp, Pkey(\groupdelta).linexp(0, 1, 1, 0.3),
            \rel, Pkey(\dur) * 1,
            \curve, Pkey(\groupdelta).linlin(0,1, -4,0),
            \buf, ~sliceBuf.at(\file),
            \rate, 1,
            \oneshot, 1,
            // \gain, -6,
            // \sliceStart, 12,
            // \sliceStart, 15,
            \sliceStart, 20,
            // \sliceStart, 27,
            // \sliceStart, 44,
            \slice, ~pGetSlice.((Pseries(1, 1, inf).wrap(0, 1) + Pkey(\sliceStart)), ~sliceBuf).stutter(4),
            \pan, ~pmodenv.(Pwhite(-0.25, 0.25, inf), Pkey(\dur)),
            \pitchMix, 0.5,
            // \pitchRatio, 2,
            \windowSize, 0.01,
            \pitchDispersion, 0.01,
            \timeDispersion, 0.05,
            \out, [~mainout],              
        )
    );

    Pdef(\cut2,
        Pbind(
            \instrument, \segPlayer,
            \amp, 1,
            // \amp, Pkey(\groupdelta).linexp(0, 1, 1, 0.3),
            \atk, 0.01,
            \rel, Pkey(\dur) * 0.5,
            \curve, Pkey(\groupdelta).linlin(0,1, -4,0),
            \buf, ~sliceBuf2.at(\file),
            \rate, 1,
            \oneshot, 1,
            \gain, 9,
            // \sliceStart, 128,
            \sliceStart, 300,
            \slice, ~pGetSlice.((Pseries(1, 1, inf).wrap(0, 3) + Pkey(\sliceStart)), ~sliceBuf2),
            \pitchMix, 0.5,
            \pitchRatio, 1,
            \windowSize, 0.05,
            \pitchDispersion, 0.01,
            \timeDispersion, 0.05,
            \out, [~mainout],              
        )
    );

    Pdef(\synth,
        Pbind(
            \scale, Scale.major,
            \dur, Pseq([1.5, 1.5, 1, 1, 1, 1, 1, 1, 1] * 0.5, inf),
            // \scale, Scale.minor,
            \root, 0, 
            \ctranspose, -2,
            // \mtranspose, 1,
            \degree, Pseq([3, 2, 1], inf),
            \octave, 4,
    
            \detune, 0.5,
            \envDepth, -0.5,
    
            \atk, 0.25,
            \dec, 0.4,
            \sus, 0.1,
            \rel, 1,
            
            \filterAtk, 0.1,
            \filterDec, 1,
            \pitchEnv, 1,
    
            \driftRate, 2.0,
            \drift, 1.0,
            \lfoShape, 1,
            \lfoFreq, 1,
    
            \lfoToWidth, 0.5,
            \lfoToShapeAmount, 0,
            \lfoToFilter, 1,
            \lfoToMorph, 0.5,
    
            \filter, 50.0,
            \filter2, 5000.0,
            \width, 1,
            \shape, 1,
            \morph, 0.5,
            \gain, -12.0,
            \amp, 1.0,
            \pan, ~pmodenv.(Pwhite(-0.25, 0.25), Pkey(\dur), 1, \sin),
            // pan, -1,
            \out, [~mainout],
            \instrument, \pulsePluck
        )
    )
)

Pdict

(
    ~bufA = Buffer.alloc(s, 512);
    ~bufB = Buffer.alloc(s, s.sampleRate * 0.01);
    ~bufC = Buffer.alloc(s, s.sampleRate * 0.025);
    ~fftSize = 512;
    ~analysisFX = Array.fill(2, {Buffer.alloc(s, ~fftSize)});
)


    //a x2 seq

    //base a
    // Pdef(\p1,
    //     ~makeSubdivision.(
    //         PlaceAll([1, 1, 1, 1], inf),
    //         PlaceAll([4, 2, 3, 2], inf)
    //     )	
    // );

    // Pdef(\p1,
    //     ~makeSubdivision.(
    //         PlaceAll([1, 1, 1, 1, 1.5, 1.5, 1], inf),
    //         PlaceAll([4, 2, 3, 2, 2, 2, 4], inf)
    //     )
    // );

    // Pdef(\p1,
    //     ~makeSubdivision.(
    //         PlaceAll([1, 1, 1, 1, 1.5, 1.5, 1], inf),
    //         PlaceAll([4, 2, 3, 2, 4, 4, 4], inf)
    //     )	
    // );

    // // a x2 seq phased
    // Pdef(\p1,
        //     ~makeSubdivision.(
    //         PlaceAll([1, 1, 1, 1, 1.5, 1.5, 1], inf),
    //         PlaceAll([4, 2, 3, 2, 4, 4, 4, 1], inf)
    //     )	
    // );

    // Pdef(\p1_transform,
    //     ~filterBeat.(key: Pkey(\eventcount), beat:[3, 1], reject: 1) <>
    //     ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [1, 2, 3], skew: [-1, 1], curve: \exp) <>
    //     ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [2, 4], skew: [1, -1], curve: \exp) <>
    //     ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [1, 2, 3], skew: [-1, 1, -1] * 0.25, curve: \exp) <>
    //     ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [1, 3], skew: [0.5, -1, -0.5], curve: \sine) <>
    //     Pdef(\p1)
    // );

~serumVst = VSTPluginController(Synth(\vsti)).open("Serum");
~serumVst.loadPreset("distortedHit");

~serumVst.editor
~serumVst.savePreset("distortedHit");

(
g.free;
g = Group.new(RootNode(Server.default), \addToTail);

Synth(\comp,
    [
        \ratio: 6,
        \thresh, -40,
        \atk, 0.1,
        \rel, 1000,
        \makeup, 0,
        \automakeup, 0
    ],
    target: g,
    addAction: \addToTail,
);

t = TempoClock.new(152/60).permanent_(true).schedAbs(0, {t.beatsPerBar_(4)});

Pdef(\player).stop;
Pdef(\player,
Pspawner({| sp |
    var sectionLength, bowl, backyard, stretch, kick, cut1, cut2, roar, morph, fftStretchLive, synth, serum;
    var verb, drum;
    //fxconst
    roar = sp.par(
        Pbindf(
            Pdef(\roar),
            \dur, 0.01,
            \instrument, \roar,
            \drive, 9.0,
            \tone, ~pmodenv.(Pseq([-0.99, 0.75], inf), 0.5),
            // \tone, -0.4,
            \toneFreq, 500.0,
            \toneComp, 1.0,
            \drywet, 0.8,
            \bias, 0,
            \filterFreq, 4000.0,
            \filterLoHi, ~pmodenv.(Pseq([0, 0.75], inf), 1),
            \filterBP, 0,
            \filterRes, 0.3,
            \filterBW, 0.5,
            \filterPre, 1.0,
            \feedAmt, 7.0,
            \feedFreq, 50.0,
            \feedBW, 0.1,
            \feedDelay, 0.1,
            \feedGate, 0.05,
            \gain, -9.0,
            \amp, 1.0,
            \out, [~mainout]
        )
    );

    morph = sp.par(
        Pbind(
            \gain, 0,
            \atk, 10,
            \rel, 100,
            \swap, ~pmodenv.(Pseq([1, 0],inf), Pseq([1.5], inf), 1, \sine),
            \out, [~mainout, ~miVerb]
        ) <>
        Pdef(\morph)
    );

    fftStretchLive = sp.par(
        Pbindf(
            Pdef(\fftStretchLive),
            \buf, ~bufA,
            \amp, 1,
            \analysis, [~analysisFX],
            \fftSize, ~fftSize,
            \recRate, ~pmodenv.(Pseq([1.5, 0.25], inf), 0.5),
            \len, 0,
            \thresh, 10,
            \remove, 10,
            \rate, 1,
            \pos, 0,
            \overdub, 0,
            \feedback, 0,
            \gain, -12,
            \dur, 0.1,
            \out, [~mainout]
        )
    );

    sp.par(
        Pbindf(
            Pdef(\miVerb),
            \time, 1,
            \damp, 0.99,
            \hp, 0.125,
            \freeze, 0,
            \diff, 0.1,
            \gain, 0,
            \out, ~mainout
        )
    );
    
    ////////////////////////
    \intro.postln;
    synth = sp.par(
        Pbind(\out, [~convolve_B]) <>
        Pdef(\synth)
    );

    sp.wait(28);

    \a.postln;
    sectionLength = 24;
    //p1 intro phased
    Pdef(\p1,
        ~makeSubdivision.(
            PlaceAll([1, 1, 1, Rest(4)], inf),
            PlaceAll([4, 2, 3], inf)
        )	
    );

    Pdef(\p1_transform,
        ~filterBeat.(key: Pkey(\eventcount), beat:[3, 1], reject: 1) <>
        ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [2, 4], skew: [1, -1], curve: \exp) <>
        Pdef(\p1)
    );

    cut2 = sp.par(
        Pbind(\out, [~roar]) <>
        Pdef(\cut2) <>
        Pdef(\p1_transform)
    );

    kick = sp.par(
        Pbind(\out, [~roar, ~convolve_B]) <>
        Pdef(\kick)  <>
            ~filterBeat.(key: Pkey(\groupcount), beat:[1, 3], mod: 3) <>
            ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [1, 2, 3], skew: [-1, 1, -1] * 0.25, curve: \exp) <>
            Pdef(\p1)
    );

    sp.wait(sectionLength);
    sp.suspend(cut2);
    sp.suspend(kick);

    //////////////////////////
    \b.postln;
    sectionLength = 36;
    
    Pdef(\p1_transform,
        ~filterBeat.(key: Pkey(\eventcount), beat:[3, 1], reject: 1) <>
        ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [2, 4], skew: [1, -1], curve: \exp) <>
        Pdef(\p1)
    );
    
    sp.suspend(cut1);
    sp.suspend(cut2);
    sp.suspend(kick);

    Pbindef(\cut1, \sliceStart, 12);

    cut2 = sp.par(
        Pbind(\out, [~roar]) <>
        Pdef(\cut2) <>
        Pdef(\p1_transform)
    );

    kick = sp.par(
        Pbind(\out, [~roar]) <>
        Pdef(\kick)  <>
            ~filterBeat.(key: Pkey(\groupcount), beat:[1, 3], mod: 3) <>
            ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [1, 2, 3], skew: [-1, 1, -1] * 0.25, curve: \exp) <>
            Pdef(\p1)
    );
    
    sp.wait(sectionLength);
    sp.suspend(cut1);
    sp.suspend(cut2);
    sp.suspend(kick);
    sp.wait(8);
    //////////////////////
    \c.postln;
    sectionLength = 50;
    
    // sp.suspend(morph);
    // morph = sp.par(
    //     Pbind(
    //         \gain, 0,
    //         \atk, 10,
    //         \rel, 100,
    //         \swap, ~pmodenv.(Pseq([1, 0],inf), Pseq([2], inf), 1, \sine),
    //         \out, [~mainout]
    //     ) <>
    //     Pdef(\morph)
    // );

    Pdef(\p1,
        ~makeSubdivision.(
            PlaceAll([1, 1, 1, 4], inf),
            PlaceAll([4, 2, 3], inf)
        )	
    );

    Pdef(\p1_transform,
        ~filterBeat.(key: Pkey(\eventcount), beat:[1, 3], reject: 1) <>
        // ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [1, 2, 3], skew: [-1, 1, -1] * 0.25, curve: \exp) <>
        ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [2, 4], skew: [1, -1], curve: \exp) <>
        Pdef(\p1)
    );
    
    sp.suspend(cut1);
    sp.suspend(cut2);
    sp.suspend(kick);
    cut1 = sp.par(
        Pbind(\out, [~roar]) <>
        Pdef(\cut1) <>
        Pdef(\p1_transform)
    );

    Pbindef(\cut1, \sliceStart, 27);

    cut2 = sp.par(
        Pbind(\out, [~roar]) <>
        Pdef(\cut2) <>
        Pdef(\p1_transform)
    );

    kick = sp.par(
        Pbind(\out, [~roar, ~convolve_A]) <>
        Pdef(\kick)  <>
            ~filterBeat.(key: Pkey(\groupcount), beat:[1, 3], mod: 3) <>
            ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [1, 2, 3], skew: [-1, 1, -1] * 0.25, curve: \exp) <>
            Pdef(\p1)
    );
    
    sp.suspend(synth);
    synth = sp.par(
        Pbind(\out, [~miVerb]) <>
        Pbind(\amp, ~pmodenv.(Pseq([1, 0.4], inf), 2)) <>
        Pdef(\synth)
    );

    bowl = sp.par(
        Pbind(\out, [~convolve_B]) <>
        Pdef(\bowl,
            Pmono(\fftStretch_mono,
                \amp, 1,
                \gain, 9,
                \buf, ~specBuff2.at(\file),
                \analysis, [~specBuff2.at(\analysis)],
                \fftSize, ~specBuff2.at(\fftSize),
                \rate, 1,
                // \pos, 0.25,
                \pos, 0.1,
                \len, 0.25,
                \out, [~mainout]
            )
        )
    );
    
    sp.wait(sectionLength);
    //////////////////////////
    \d.postln;
    sectionLength = 38;
    sp.suspend(cut1);
    sp.suspend(cut2);
    sp.suspend(synth);
    sp.suspend(bowl);
    sp.suspend(morph);
    
    Pdef(\p1,
        ~makeSubdivision.(
            PlaceAll([1, 1, 1, 4], inf),
            PlaceAll([4, 2, 3], inf)
        )	
    );

    serum = sp.par(
        Pbind(
            \type, \vst_midi,
            \vst, ~serumVst, // the VSTPluginController instance
            \midicmd, \noteOn, // the default, can be omitted
            \chan, 0, // MIDI channel (default: 0)
            \midinote, 44,
            \dur, Pseq([1.5, Rest(16-1.5)], inf),
            \amp, 1
        )
    );

    Pdef(\p1_transform,
        ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [2, 4], skew: [1, -1], curve: \exp) <>
        Pdef(\p1)
    );

    cut1 = sp.par(
        Pbind(\out, [~roar]) <>
        Pdef(\cut1) <>
        Pdef(\p1_transform)
    );


    synth = sp.par(
        Pbind(\out, [~convolve_A]) <>
        Pdef(\synth)
    );
    
    bowl = sp.par(
        Pbind(\out, [~convolve_B]) <>
        Pdef(\bowl,
            Pmono(\fftStretch_mono,
                \amp, 1,
                \gain, 9,
                \buf, ~specBuff2.at(\file),
                \analysis, [~specBuff2.at(\analysis)],
                \fftSize, ~specBuff2.at(\fftSize),
                \rate, 1,
                \pos, 0.2,
                \len, 0.1,
                \out, [~mainout]
            )
        )
    );

    Pbindef(\cut1, \sliceStart, 12);

    cut2 = sp.par(
        Pbind(\out, [~roar]) <>
        Pdef(\cut2) <>
        Pdef(\p1_transform)
    );

    sp.wait(sectionLength);
    //////////////////////////
    \e.postln;
    sectionLength = 36;

    sp.suspend(morph);
    sp.suspend(cut1);
    sp.suspend(cut2);
    sp.suspend(kick);
    sp.suspend(bowl);
    sp.suspend(synth);

    // morph = sp.par(
    //         Pbind(
    //             \gain, 0,
    //             \atk, 10,
    //             \rel, 100,
    //             \swap, ~pmodenv.(Pseq([0.95, 0],inf), Pseq([1], inf), 1, \sine),
    //             \out, [~mainout]
    //         ) <>
    //         Pdef(\morph)
    // );

    Pdef(\p1,
        ~makeSubdivision.(
            PlaceAll([1, 1, 1, 1, 1.5, 1.5, 1], inf),
            PlaceAll([4, 2, 3, 2, 4, 4, 4], inf)
        )
    );

    Pdef(\p1_transform,
        ~filterBeat.(key: Pkey(\eventcount), beat:[1, 3], reject: 1) <>
        ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [1, 2, 3], skew: [-1, 1, -1] * 0.25, curve: \exp) <>
        Pdef(\p1)
    );

    cut1 = sp.par(
        Pbind(\out, [~roar]) <>
        Pdef(\cut1) <>
        Pdef(\p1_transform)
    );

    Pbindef(\cut1, \sliceStart, 20);

    cut2 = sp.par(
        Pbind(\out, [~roar]) <>
        Pdef(\cut2) <>
        Pdef(\p1_transform)
    );


    synth = sp.par(
        Pbind(\out, [~convolve_A]) <>
        Pdef(\synth)
    );
    

    bowl = sp.par(
        Pbind(\out, [~convolve_B]) <>
        Pdef(\bowl,
            Pmono(\fftStretch_mono,
                \amp, 1,
                \gain, 9,
                \buf, ~specBuff2.at(\file),
                \analysis, [~specBuff2.at(\analysis)],
                \fftSize, ~specBuff2.at(\fftSize),
                \rate, 1,
                \pos, 0.7,
                \len, 0.1,
                \out, [~mainout]
            )
        )
    );

    kick = sp.par(
        Pbind(\out, [~roar]) <>
        Pdef(\kick)  <>
            ~filterBeat.(key: Pkey(\groupcount), beat:[1, 3], mod: 3) <>
            ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [1, 2, 3], skew: [-1, 1, -1] * 0.25, curve: \exp) <>
            Pdef(\p1)
    );
    
    sp.wait(sectionLength);

    sectionLength = 32;
    \f.postln;
    backyard = sp.par(
        PfadeIn(
            Pdef(\backyard,
                    Pmono(\fftStretch_mono,
                        \amp, 1,
                        \gain, 18,
                        \buf, ~specBuff.at(\file),
                        \analysis, [~specBuff.at(\analysis)],
                        \fftSize, ~specBuff.at(\fftSize),
                        \rate, 0.2,
                        // \pos, 0.15,
                        // \pos, 0.25,
                        \pos, 0.2,
                        // \pos, 0.55,
                        // \pos, 0.1,
                        \len, 0.05,
                        \out, ~mainout
                    )
                )
        , 16)
    );

    sp.wait(sectionLength);
    sp.suspend(serum);

    serum = sp.par(
        Pbind(
            \type, \vst_midi,
            \vst, ~serumVst, // the VSTPluginController instance
            \midicmd, \noteOn, // the default, can be omitted
            \chan, 0, // MIDI channel (default: 0)
            \midinote, Pseq([43, 44], inf),
            \dur, Pseq([1.5, Rest(16-1.5)], inf),
            \amp, 1
        )
    );
    
    sp.wait(sectionLength);

    \g.postln;
    sectionLength = 64;
    
    sp.suspend(cut1);
    sp.suspend(cut2);
    sp.suspend(synth);
    sp.suspend(bowl);
    sp.suspend(kick);
    
    Pdef(\p1,
        ~makeSubdivision.(
            PlaceAll([1, 1, 1, 1], inf),
            PlaceAll([4, 4, 4, 4], inf)
        )
    );

    kick = sp.par(
        Pbind(\out, [~roar]) <>
        Pdef(\kick)  <>
            // ~filterBeat.(key: Pkey(\groupcount), beat:[1, 3], mod: 3) <>
            // ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [1, 2, 3], skew: [-1, 1, -1] * 0.25, curve: \exp) <>
            Pdef(\p1)
    );



})
).play(t);
)