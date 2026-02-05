(
    ~mainout = 0;
    ~longverb = Bus.audio(s,2);
    ~nhverb = Bus.audio(s,2);
    ~miVerb = Bus.audio(s,2);
    ~delay = Bus.audio(s,2);
    ~chorus = Bus.audio(s,2);
    
    ~modDelay=Bus.audio(s,2);
    ~mod1_in = Bus.audio(s, 2);
    ~mod1_out = Bus.control(s, 1);
    
    ~modal=Bus.audio(s,2);
    ~formant=Bus.audio(s,2);
    ~grain=Bus.audio(s,2);
    ~tape=Bus.audio(s,2);
    ~pitchShift=Bus.audio(s,2);
    ~filter=Bus.audio(s,2);
    ~eq=Bus.audio(s,2);
    
    ~bufA = Buffer.alloc(s, s.sampleRate * 0.1);
    ~bufB = Buffer.alloc(s, s.sampleRate * 0.01);
    ~bufC = Buffer.alloc(s, s.sampleRate * 0.025);
    
    ~convolve_A=Bus.audio(s,2);
    ~convolve_B=Bus.audio(s,2);
    
    ~morph_A=Bus.audio(s,2);
    ~morph_B=Bus.audio(s,2);
    
    //fx routing
    Pdef(\modal, Pmono(\padKlank, \inbus, ~modal, \addAction, \addToTail, \amp, 1, \callback, { Pdefn(\fxid, ~id) }));
    Pdef(\formant, Pmono(\formantBank, \inbus, ~formant, \addAction, \addToTail, \amp, 1, \callback, { Pdefn(\fxid, ~id) }));
    Pdef(\grain, Pmono(\liveGrain_mono, \inbus, ~grain, \bufnum, ~bufA, \addAction, \addToTail, \amp, 1, \callback, { Pdefn(\fxid, ~id) }));
    Pdef(\tape, Pmono(\tape, \inbus, ~tape, \addAction, \addToTail, \amp, 1, \callback, { Pdefn(\fxid, ~id) }));
    Pdef(\pitchShift, Pmono(\pitchShift, \inbus, ~pitchShift, \addAction, \addToTail, \amp, 1, \callback, { Pdefn(\fxid, ~id) }));
    Pdef(\filter, Pmono(\vasem12, \inbus, ~filter, \addAction, \addToTail, \amp, 1, \callback, { Pdefn(\fxid, ~id) }));
    Pdef(\eq, Pmono(\EQstack, \inbus, ~eq, \addAction, \addToTail, \amp, 1, \callback, { Pdefn(\fxid, ~id) }));
    
    Pdef(\morph,
        Pmono(\cepstralMorph_fx,
            \amp, 1,
            \inbus_A, ~convolve_A,
            \inbus_B, ~convolve_B,
            \addAction, \addToTail,
            \callback, { Pdefn(\fxid, ~id) },
        )
    );
    
    Pdef(\nhverb,
        Pmono(\nhverb,
            \amp, 1,
            // \dur, 0.01,
            \inbus, ~nhverb,
            \addAction, \addToTail,
            \gain, 0,
            \callback, { Pdefn(\fxid, ~id) },
        )
    );
    
    Pdef(\miVerb,
        Pmono(\miVerb,
            \amp, 1,
            // \dur, 0.01,
            \inbus, ~miVerb,
            \addAction, \addToTail,
            \gain, 0,
            \callback, { Pdefn(\fxid, ~id) },
        )
    );
)

(
    ~specBuff=Dictionary();
    ~specBuff2=Dictionary();
    // ~makeSpec.("/Users/aelazary/Desktop/Samples etc./NEW sample lib/mother and daughter singing o magnum mysterium-sfLDOVcK7nU.wav", ~specBuff, 16384, 2)
    ~makeSpec.("/Users/aelazary/Desktop/Samples etc./spannerGuitar/spannerDrumGuitar-2.wav", ~specBuff, 16384, 2);
    ~makeSpec.("/Users/aelazary/Desktop/Samples etc./spannerGuitar/spannerDrumGuitar-3.wav", ~specBuff2, 16384, 2);
)
// 
(
    ~sliceBuf = Dictionary();
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./NEW sample lib/Drum solos/MY SOLO--665wsfWSJw.wav", ~sliceBuf, 0.5, \crest);
    // ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./NEW sample lib/All The Breaks/Bernard Purdie - Them Changes.wav", ~sliceBuf, 0.2, \centroid);
)

(
    ~breakBuf = Dictionary();
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Old sample lib/Prime Loops The Temple Of Breaks (MULTIFORMAT)/WAV_ACID_The_Temple_Of_Breaks/The Temple Of Breaks/Drum Loops/Krowd_Break_174.wav", ~breakBuf, 0.3, \crest);
    // ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./NEW sample lib/Jungle riddims/champion sound.wav", ~breakBuf, 0.3, \crest);
    // ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Old sample lib/Prime Loops The Temple Of Breaks (MULTIFORMAT)/WAV_ACID_The_Temple_Of_Breaks/The Temple Of Breaks/Drum Loops/Horde_Break_180.wav", ~breakBuf, 0.3, \crest);

)

Pdef.clear;

(
g.free;
g = Group.new(RootNode(Server.default), \addToTail);

/*Synth(\pitchShift,
	[
		\pitchRatio: 0.5,
		\windowSize: 0.05,
		\drywet: 1,
		pitchDispersion: 0.0001,
		timeDispersion: 1,
	],
	target: g,
	addAction: \addToTail,
	);

Synth(\tape,
	[
		\pregain: -8,
		\sigmoid, 0.5,
		\drywet: 0,
		\autogain: 1
		],
		target: g,
		addAction: \addToTail,

);*/

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

t = TempoClock.new(140/60).permanent_(true).schedAbs(0, {t.beatsPerBar_(4)});
Pdef(\player,
    Pspawner({| sp |
    var sectionLength = 128;
    //fx const
    sp.par(
        Pbindf(
            Pdef(\miVerb),
            \time, 0.8,
            \damp, 0.6,
            \hp, 0.0,
            \freeze, 0,
            \diff, 0.9,
            \gain, -6,
            \out, ~mainout
        )
    );

    Pdef(\mod, 
        Pmono(\west,
            \dec, Pkey(\dur) * 1,
            \freq, 30,
            \gate, 1,
            
            // \pitchBendRatio, Pwhite(0.5, 2),
            \glide, 0.01, 
                
            \fm1Ratio, 4, 
            \fm2Ratio, 3,
            \fm1Amount, 0.1, 
            \fm2Amount, 0.1,
            
            \vel, 0.5, 
            \pressure, ~pmodenv.(Pkey(\groupdelta) * 5, Pkey(\dec)), //Pwhite(), 
            // \timbre, Pwhite(0.0,0.75), 
            \timbre, ~pmodenv.(Pwhite(0, 1), Pkey(\dec)),
            \waveShape, 0.25, 
            \waveFolds, ~pmodenv.(1 - Pkey(\groupdelta), Pkey(\dec)), 
            \envType, ~pmodenv.(Pseq([0, 1], inf), 4), 
            // \envtype, 1,
            // \peak, ~pmodenv.(Pwhite(250.0, 4000.0), Pkey(\dec)),
            \peak, ~pmodenv.(Pwhite(250.0, 15000.0), Pkey(\dec)),
            \decay, Pwhite(1, 2),
            // \decay, 1,
            \pan, Pbrown(-0.5,0.5,0.001),
            \amp, 0.5,
            \lfoShape, 0, //Pwhite(), 
            \lfoFreq, Pkey(\dur),
            // \lfoFreq, Pwhite(0.1, 5.0),
            \lfoToWaveShapeAmount, ~pmodenv.((1 - Pkey(\groupdelta)) * 0.25, Pkey(\dec)),
            \lfoToWaveFoldsAmount, ~pmodenv.(Pkey(\groupdelta) * 1, Pkey(\dec)),

            \lfoToReverbMixAmount, Pwhite(), 
            \drift, ~pmodenv.(Pwhite(0, 0.1), Pkey(\dec))
        )
    );

    Pdef(\fb1mod,
        Pbind(
            \amp, 0.7,
            \dec, Pkey(\dur) * 2,
            \feedback, ~pmodenv.(Pseq([0.2, 0.4, 0.8] * 0.5,inf), Pkey(\dec)),
            \time, ~pmodenv.(Pseq(([0.005, 0.001, 0.010] * 100),inf), Pkey(\dec)),
            \damp, ~pmodenv.(Pseq([0.1, 1],inf), Pkey(\dec)),
            \exciter, ~pmodenv.(Pwhite(1, 0, inf).lincurve(0, 1, 0, 1, -8), Pkey(\dec), 1, Pseq([\sine],inf)),
            \impulse, ~pmodenv.(Pwhite(20000, 200,inf), Pkey(\dec)),
            \spont, ~pmodenv.(Pseq([60,1000],inf), Pkey(\dec)),
            \boost,  ~pmodenv.(Pseq([20000,200],inf), Pkey(\dec)),
            \restore, 5,
            \dist,  ~pmodenv.(Pseq([16, 32],inf), Pkey(\dec)),
            \rev, ~pmodenv.(Pexprand(0.1, 4, inf), Pkey(\dec)),
            \pan, ~pmodenv.(Pwhite(-0.3, 0.3, inf), Pkey(\dec)),
            // \gain, -12,
        )
    );

    Pdef(\kickMod,
        Pbind(
            // \dur, 1,
            \freq, 40.0,
            \atk, 0.01,
            \dec, Pkey(\groupcount).wrap(1, 2).linlin(1, 2, 0.2, 0.5),
            \fb, Pkey(\eventcount) * 0.5,
            \index, 1.0,
            \ratio, 1.5,
            \drive, 5.0,
            \sweep, 32.0,
            \spread, 2,
            \lofreq, 500.0,
            \lodb, 10.0,
            \midfreq, 1200.0,
            \middb, -20.0,
            \hifreq, 7000.0,
            \hidb, 30.0,
            \gain, -15.0,
            \pan, 0.0,
            \noise, 0.5,
            \amp, Pkey(\groupcount).wrap(1, 2).linlin(1,2, 1, 0.7, 1),
        )
    );

    \a.postln;      
    Pdef(\p1,
        ~makeSubdivision.(
            PlaceAll([2.5, 1.5, 1.5, Rest(1)], inf),
            PlaceAll([[8, 3], 4, 2, 1, 4], inf)
        )
    );
    
    sp.par(
        Pdef(\west,
            Pdef(\mod) <>
            // ~filterBeat.(key: Pkey(\eventcount), beat:[1, 2, 4]) <>
            ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [1, 2, 5], skew: [2, -1], curve: \exp) <> 
            Pdef(\p1)
            <> Pbind(
                \instrument, \west,
                \out, [~convolve_B]
            )
        ).finDur(sectionLength)
    );

    sp.par(
        Pbindf(
            Pdef(\seq3,
                // Pbind(\lag, 16)
                Pdef(\fb1mod) 
                <> ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [1, 2, 5], skew: [2, -1], curve: \exp)
                <> Pdef(\p1)
                <> Pbind(\instrument, \fb1)
            ),
            \out, [~convolve_A, ~miVerb]
        ).finDur(sectionLength)
    );
    
    sp.par(
        Pmono(\fftStretch_magAbove_mono,
            \amp, 1,
            \gain, 0,
            \buf, ~specBuff2.at(\file),
            \analysis, [~specBuff2.at(\analysis)],
            \fftSize, ~specBuff2.at(\fftSize),
            \rate, 0.1,
            \pos, 0.3,
            // \pos, 0.8,
            \len, 0.1,
            \filter, ~pmodenv.(Pseq([1, 4],inf), Pseq([4, 8, 4], inf), curve: \sine),
            \out, [~convolve_B]
        ).finDur(sectionLength)
    );

    sp.par(
        Pbindf(
            Pmono(\pulsar_mono),
            \triggerRate, 16,
            \fluxMF, 1.5,
            \fluxMD, 0,
            \grainFreq, 15,
            \overlap, 0.5,
            \pmRatio, 100,
            \pmIndex, 0.5,
            \density, 0.1,
            \polarityMod, 0,
            \out, [~convolve_B],
            \lag, 8
        ).finDur(sectionLength)
    );

    sp.par(
        Pbind(\amp, 1, \freq, 30)
        <>~filterBeat.(key: Pkey(\eventcount), beat:[1], mod:4)
        <> ~filterBeat.(key: Pkey(\groupcount), beat:[4], mod: 2)
        <>Pdef(\p1)
        <>Pbind(\instrument, \simpleSub)
    );

    sp.par(
        Pbindf(
            Pdef(\morph),
            \gain, -6,
            \atk, 0,
            \rel, 100,
            // \swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([1/2], inf), 1, \sin),
            // \swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([1/3], inf), 1, \sin),
            \swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([4], inf), 1, \sin),
            // \swap, 0,
            // \swap, 1,
            \out, [~mainout, ~miVerb]
        ).finDur(sectionLength)
    );

    sp.wait(sectionLength);
    
    //////////////////////////////////////////
    \b.postln;

    sectionLength = 100;     
    // sectionLength = 0; 
    Pdef(\p1,
        ~makeSubdivision.(
            PlaceAll([2.5, 1.5, 1.5, Rest(1)], inf),
            PlaceAll([[8, 3], 4, 2, 1, 4], inf)
        )
    );

    // Pdef(\p1,
    //     ~makeSubdivision.(
    //         PlaceAll([1, 1, 1.5, Rest(0.5+8)], inf),
    //         PlaceAll([[4, 1], 0, 1, 2], inf)
    //     )
    // );
    
    sp.par(
        Pdef(\west,
            Pbindf(Pdef(\mod), \pitchBendRatio, Pwhite(0.5, 2)) <>
            ~filterBeat.(key: Pkey(\eventcount), beat:[1, 2, 4]) <>
            ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [1, 2, 5], skew: [2, -1], curve: \exp) <> 
            Pdef(\p1)
            <> Pbind(
                \instrument, \west,
                \out, [~convolve_B]
            )
        ).finDur(sectionLength)
    );

    sp.par(
        Pbindf(
            Pdef(\seq3,
                // Pbind(\lag, 16)
                Pdef(\fb1mod) 
                <> ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [1, 2, 5], skew: [2, -1], curve: \exp)
                <> Pdef(\p1)
                <> Pbind(\instrument, \fb1)
            ),
            \out, [~convolve_A, ~miVerb]
        ).finDur(sectionLength)
    );
    
    //fade out last section pad
    sp.par(
        PfadeOut(
        Pmono(\fftStretch_magAbove_mono,
            \amp, 1,
            \gain, 0,
            \buf, ~specBuff2.at(\file),
            \analysis, [~specBuff2.at(\analysis)],
            \fftSize, ~specBuff2.at(\fftSize),
            \rate, 0.1,
            \pos, 0.3,
            // \pos, 0.8,
            \len, 0.1,
            \filter, ~pmodenv.(Pseq([1, 4],inf), Pseq([4, 8, 4], inf), curve: \sine),
            \out, [~convolve_B]
        ).finDur(sectionLength)
        , 16)
    );
    
    sp.par(
        PfadeIn(
        Pmono(\fftStretch_magAbove_mono,
            \amp, 1,
            \gain, 0,
            \buf, ~specBuff.at(\file),
            \analysis, [~specBuff.at(\analysis)],
            \fftSize, ~specBuff.at(\fftSize),
            \rate, 1,
            \pos, 0.3,
            // \pos, 0.8,
            \len, 0.1,
            \filter, ~pmodenv.(Pseq([1, 4],inf), Pseq([4, 8, 4], inf), curve: \sine),
            \out, [~convolve_B, ~miVerb]
        ).finDur(sectionLength)
        , 12)
    );

    sp.par(
        Pbindf(
            Pmono(\pulsar_mono),
            \triggerRate, 16,
            \fluxMF, 1.5,
            \fluxMD, 0,
            \grainFreq, 15,
            \overlap, 0.5,
            \pmRatio, 100,
            \pmIndex, 0.5,
            \density, 0.9,
            \polarityMod, 0,
            \out, [~convolve_B],
            \lag, 4
        ).finDur(sectionLength)
    );

    sp.par(
        Pbind(\amp, 1, \freq, 30)
        <>~filterBeat.(key: Pkey(\eventcount), beat:[1], mod:4)
        <> ~filterBeat.(key: Pkey(\groupcount), beat:[4], mod: 2)
        <>Pdef(\p1)
        <>Pbind(\instrument, \simpleSub)
    );

    Pdef(\kickP,
        ~makeSubdivision.(
            PlaceAll([0.75, 1.5, 0.25, 2, 1.5, 2], inf),
            PlaceAll([4, 1, 1, 2], inf)
        )        
    );

    Pdef(\kickP2,
        ~makeSubdivision.(
            PlaceAll([1, 1, 1.5, 0.5], inf),
            PlaceAll([[3, 1], 4, 3, 4], inf)
        )
    );
    
    sp.par(
        Pdef(\kickMod)
        // <> ~filterBeat.(key: Pkey(\groupcount), beat:[1, 4])
        <> ~pSkew.(Pdef(\kickP), key: Pkey(\eventcount), group: [1,3,4], skew: [1], curve: \exp)
        <> Pdef(\kickP)
        // <> Pbind(\dur, 1.333)
        <>Pbind(
            \instrument, \fmKick2,
            \out, [~convolve_B, ~grain]
        ).finDur(sectionLength)
    );

    sp.par(
        Pbindf(
            Pdef(\morph),
            \gain, -6,
            \atk, 0,
            \rel, 100,
            // \swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([1/2], inf), 1, \sin),
            // \swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([1/3], inf), 1, \sin),
            \swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([4], inf), 1, \sin),
            // \swap, 0,
            // \swap, 1,
            \out, [~mainout, ~miVerb]
        ).finDur(sectionLength)
    );
    
    sp.wait(sectionLength);
    
   
    ////////////////////////////////////////
    \c.postln;
    sectionLength = 80;
    
    Pdef(\p1,
        ~makeSubdivision.(
            PlaceAll([1, 1, 1.5, Rest(0.5)], inf),
            PlaceAll([[4, 1], 0, 1, 2], inf)
        )
    );

    sp.par(
        Pdef(\west,
            Pbindf(Pdef(\mod), \pitchBendRatio, Pwhite(0.5, 2)) <>
            // ~filterBeat.(key: Pkey(\eventcount), beat:[1, 2, 4]) <>
            ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [1, 2, 5], skew: [2, -1], curve: \exp) <> 
            Pdef(\p1)
            <> Pbind(
                \instrument, \west,
                \out, [~convolve_B]
            )
        ).finDur(sectionLength)
    );


    sp.par(
        Pbindf(
            Pdef(\seq3,
                // Pbind(\lag, 16)
                Pdef(\fb1mod) 
                <> ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [1, 2, 5], skew: [2, -1], curve: \exp)
                <> Pdef(\p1)
                <> Pbind(\instrument, \fb1)
            ),
            \out, [~convolve_A, ~miVerb]
        ).finDur(sectionLength + 8)
    );

    sp.par(
        PfadeOut(
        Pmono(\fftStretch_magAbove_mono,
            \amp, 1,
            \gain, 0,
            \buf, ~specBuff.at(\file),
            \analysis, [~specBuff.at(\analysis)],
            \fftSize, ~specBuff.at(\fftSize),
            \rate, 1,
            \pos, 0.3,
            // \pos, 0.8,
            \len, 0.1,
            \filter, ~pmodenv.(Pseq([1, 4],inf), Pseq([4, 8, 4], inf), curve: \sine),
            \out, [~convolve_B]
        )
        , 24)
    );

    sp.par(
        Pfadein2(
            Pmono(\fftStretch_magAbove_mono,
                \amp, 1,
                \gain, 0,
                \buf, ~specBuff.at(\file),
                \analysis, [~specBuff.at(\analysis)],
                \fftSize, ~specBuff.at(\fftSize),
                \rate, 1,
                \pos, ~pmodenv.(Pseq([0.5, 0.8],inf), Pseq([4, 8, 4], inf), curve: \sine),
                // \pos, 0.8,
                \len, 0.1,
                \filter, ~pmodenv.(Pseq([1, 4],inf), Pseq([4, 8, 4], inf), curve: \sine),
                \out, [~convolve_B]
            ).finDur(sectionLength)
        , 2, 10)
    );

    Pdef(\kickP,
        ~makeSubdivision.(
            PlaceAll([0.75, 1.5, 0.25, 2, 1.5, 2], inf),
            PlaceAll([4, 1, 1, 2], inf)
        )        
    );

    Pdef(\kickP2,
        ~makeSubdivision.(
            PlaceAll([1, 1, 1.5, 0.5], inf),
            PlaceAll([[3, 1], 4, 3, 4], inf)
        )
    );
    
    sp.par(
        Pdef(\kickMod)
        // <> ~filterBeat.(key: Pkey(\groupcount), beat:[1, 4])
        <> ~pSkew.(Pdef(\kickP), key: Pkey(\eventcount), group: [1,3,4], skew: [1], curve: \exp)
        <> Pdef(\kickP)
        // <> Pbind(\dur, 1.333)
        <>Pbind(
            \instrument, \fmKick2,
            \out, [~convolve_B]
        ).finDur(sectionLength)
    );
    
    sp.par(
    Pdef(\drumSliced,
        Pbind(
            \amp, 1,
            \buf, ~sliceBuf.at(\file),
            \sliceStart, 50,
            //lowest to highest
            \overlap, 10,
            // \trigRate, 10,
            \trigRate, Pseg(Pseq([10, 1000,], inf), 4, 'exp' , inf),
            \slice, ~pGetSlice.(Pseries(1, 1, inf).wrap(0, 2) + Pkey(\sliceStart), ~sliceBuf),
            // \gain, 6,
            \posRate, 0.75,
            \rate, 0.5,
            // \out, 
            \gain, -20,
            \atk, 0,
            \dec, 0.1,
            \out, [~convolve_A, ~mainout]
        )
        // <> Pbind(\timingOffset, Pkey(\groupcount) / Pkey(\groupdelta))
        // <> Pbind(\dur, Pseq([Rest(2), 2, Rest(2), 2], inf))
        // <> Pbind(\dur, Pseq([2, 2, 4], inf))
        <> ~pSkew.(Pdef(\kickP), key: Pkey(\eventcount), group: [1, 2, 5], skew: [2, -1], curve: \exp)
        <> Pdef(\kickP)
        <> Pbind(\instrument, \grainSlicer)
    ).finDur(sectionLength)
    );
    
    sp.par(
        Pbindf(
            Pdef(\grain),
            \bufnum, ~bufC,
            \overlap, 1000,
            \trigRate, 500,
            \posRate, 2,
            \rate, 1,
            \recRate, 1,
            \polarityMod, 1,
            \overdub, 0,
            \feedback, 0.0,
            \gain, -12,
            \out, [~convolve_B, ~mainout]
        )
    );

    sp.par(
        Pbindf(
            Pdef(\morph),
            \gain, -6,
            \atk, 0,
            \rel, 100,
            \swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([1/2], inf), 1, \sin),
            // \swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([1/3], inf), 1, \sin),
            // \swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([4], inf), 1, \sin),
            // \swap, 0,
            // \swap, 1,
            \out, [~mainout, ~miVerb]
        ).finDur(sectionLength)
    );

    sp.wait(sectionLength);
 
    ////////////////////////////////////
    \cBreak.postln;
    sectionLength = 52;

    sp.par(
        Pbind(\tempo, 150/60, \instrument, \rest)
    );

    sp.par(
        PfadeOut(
            Pmono(\fftStretch_magAbove_mono,
                \amp, 1,
                \gain, 0,
                \buf, ~specBuff.at(\file),
                \analysis, [~specBuff.at(\analysis)],
                \fftSize, ~specBuff.at(\fftSize),
                \rate, 1,
                \pos, ~pmodenv.(Pseq([0.5, 0.8],inf), Pseq([4, 8, 4], inf), curve: \sine),
                // \pos, 0.8,
                \len, 0.1,
                \filter, ~pmodenv.(Pseq([1, 4],inf), Pseq([4, 8, 4], inf), curve: \sine),
                \out, [~mainout]
            ).finDur(sectionLength)
        , 24)
    );

    Pdef(\kickP,
        ~makeSubdivision.(
            PlaceAll([1.5, 1.5, 1], inf),
            PlaceAll([4, 4, 1, 5, 5, 2], inf)
        )        
    );

    sp.par(
        Pdef(\breakSliced,
            Pbind(
                \amp, 1,
                \atk, 0.01,
                \rel, Pkey(\dur) * 0.5,
                \buf, ~breakBuf.at(\file),
                \rate, 1,
                \oneshot, 1,
                \gain, -9,
                \sliceStart, 60,
                \slice, ~pGetSlice.(Pseries(1, 1, inf).wrap(0, 4) + Pkey(\sliceStart), ~breakBuf),
                \pitchMix, 0.5,
                \pitchRatio, 2,
                \windowSize, 0.05,
                \pitchDispersion, 1.0,
                \timeDispersion, 0.1,
                \out, [~mainout],
                \lag, 8
            )
            // <> Pbind(\timingOffset, Pkey(\groupcount) / Pkey(\groupdelta))
            // <> ~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[1, 3])
            <> ~filterBeat.(key: Pkey(\eventcount), beat:[1,3,4])
            // <> ~pSkew.(Pdef(\kickP), key: Pkey(\eventcount), group: [1, 2], skew: [0.75], curve: \exp)
            // <>Pbind(\amp, Pseq([0.1, 0.7], inf))
            <> Pdef(\kickP)
            <> Pbind(\instrument, \segPlayer)
            ).finDur(40)
    );

    sp.par(
        Pmono(\fftStretch_magAbove_mono,
            \amp, 1,
            \gain, 0,
            \buf, ~specBuff2.at(\file),
            \analysis, [~specBuff2.at(\analysis)],
            \fftSize, ~specBuff2.at(\fftSize),
            \rate, 0.1,
            \pos, 0.3,
            // \pos, 0.8,
            \len, 0.1,
            \filter, ~pmodenv.(Pseq([1, 4],inf), Pseq([4, 8, 4], inf), curve: \sine),
            \out, [~mainout]
        ).finDur(sectionLength)
    );

    sp.wait(sectionLength);
    // // ////////////////////////////////////////
  
    \d.postln;
    sectionLength = 32;

    sp.par(
        Pbind(\tempo, 150/60, \instrument, \rest)
    );

    Pdef(\p1,
        ~makeSubdivision.(
            PlaceAll([1, 1, 1.5, Rest(0.5)], inf),
            PlaceAll([[4, 1], 0, 1, 2], inf)
        )
    );
    
    sp.par(
        Pdef(\west,
            Pdef(\mod) <>
            // ~filterBeat.(key: Pkey(\eventcount), beat:[1, 2, 4]) <>
            ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [1, 2, 5], skew: [2, -1], curve: \exp) <> 
            Pdef(\p1)
            <> Pbind(
                \instrument, \west,
                \out, [~mainout]
            )
        ).finDur(sectionLength)
    );

    Pdef(\kickMod,
        Pbind(
            // \dur, 1,
            \freq, 60.0,
            \atk, 0.01,
            \dec, Pkey(\groupcount).wrap(1, 2).linlin(1, 2, 0.2, 0.5),
            \fb, Pkey(\eventcount) * 0.5,
            \index, 1.0,
            \ratio, 1.5,
            \drive, 5.0,
            \sweep, 32.0,
            \spread, 2,
            \lofreq, 500.0,
            \lodb, 10.0,
            \midfreq, 1200.0,
            \middb, -20.0,
            \hifreq, 7000.0,
            \hidb, 30.0,
            \gain, -15.0,
            \pan, 0.0,
            \noise, 0.5,
            \amp, 0.5
            // \amp, Pkey(\groupcount).wrap(1, 2).linlin(1, 2, 1, 0.7, 1),
        )
    );

    sp.par(
        Pmono(\fftStretch_magAbove_mono,
            \amp, 1,
            \gain, 0,
            \buf, ~specBuff2.at(\file),
            \analysis, [~specBuff2.at(\analysis)],
            \fftSize, ~specBuff2.at(\fftSize),
            \rate, 0.1,
            \pos, 0.2,
            // \pos, ~pmodenv.(Pseq([0.2, 0.8],inf), Pseq([4, 8, 4], inf), curve: \sine),
            // \pos, 0.8,
            \len, 0.1,
            \filter, ~pmodenv.(Pseq([1, 4],inf), Pseq([4, 8, 4], inf), curve: \sine),
            \out, [~convolve_B]
        ).finDur(sectionLength)
    );

    Pdef(\rimMod,
        Pbind(
            \freq, 440.0,
            \atk, 0.04,
            \dec, 0.001,
            \fb, Pkey(\groupcount),
            \index, 4.0,
            \ratio, 2.0,
            \sweep, 8.0,
            \spread, 20.0,
            \noise, 1.0,
            \roomsize, 3.0,
            \reverbtime, 5.0,
            \gain, -20.0,
            \pan, 0.0,
            \amp, Pkey(\groupcount).wrap(0,1).linlin(0,1,0.5,0.8),
            \out, [~grain, ~mainout]
        )
    );

    Pdef(\kickP,
        ~makeSubdivision.(
            PlaceAll([1.5, 1.5, 1], inf),
            PlaceAll([4, 4, 1, 5, 5, 2], inf)
        )        
    );

    Pdef(\kickP2,
        ~makeSubdivision.(
            PlaceAll([1, 1, 1.5, 0.5], inf),
            PlaceAll([[3, 1], 4, 3, 4], inf)
        )
    );

    sp.par(
        Pdef(\kickMod)
        // Pswitch(Pdef(\kickMod), Pdef(\rimMod), Pkey(\groupcount).wrap(0,1))
        <> ~filterBeat.(key: Pkey(\eventcount), beat:[1, 3, 4])
        <> ~pSkew.(Pdef(\kickP), key: Pkey(\eventcount), group: [1, 2], skew: [0.5], curve: \exp)
        <> Pdef(\kickP)
        // <> Pbind(\dur, 1.333)
        <> Pbind(
            \instrument, \fmKick2,
            \out, [~convolve_B]
        ).finDur(sectionLength)
    );
    
    Pdef(\breakP,
        ~pFade.(
            Pdef(\kickP), 
            ~pSkew.(Pdef(\kickP), key: Pkey(\eventcount), group: [1, 2], skew: [0.75], curve: \exp) <> Pdef(\kickP), 
            32, loop: 1
        )
    );
    
    sp.par(
        Pdef(\hh,
            Pbind(
                \freq, 100.0,
                \atk, 0.001,
                \dec, 0.1,
                \gain, -16,
                \pan, 0.0,
                \amp, 1.0,
                \out, [~convolve_B]
            )
            <> ~makeSubdivision.(
                PlaceAll([1.5, 1.5, 1], inf),
                PlaceAll([2,2,1,2], inf)
            )
            <> Pbind(\instrument, \hh)
        ).finDur(sectionLength)
    );

    sp.par(
        Pdef(\breakSliced,
            Pbind(
                \amp, 1,
                \atk, 0.01,
                \rel, Pkey(\dur) * 0.2,
                \buf, ~breakBuf.at(\file),
                \rate, 1,
                \oneshot, 1,
                \gain, -9,
                \sliceStart, 50,
                \slice, ~pGetSlice.(Pseries(1, 1, inf).wrap(0, 4) + Pkey(\sliceStart), ~breakBuf),
                \out, [~convolve_B, ~mainout],
                \pitchMix, 0.5,
                \pitchRatio, 2,
                \windowSize, 0.05,
                \pitchDispersion, 1.0,
                \timeDispersion, 0.1,
                
            )
            // <> Pbind(\timingOffset, Pkey(\groupcount) / Pkey(\groupdelta))
            // <> ~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[1, 3])
            <> ~filterBeat.(key: Pkey(\eventcount), beat:[1,3,4])
            // <> ~pSkew.(Pdef(\kickP), key: Pkey(\eventcount), group: [1, 2], skew: [0.75], curve: \exp)
            // <>Pbind(\amp, Pseq([0.1, 0.7], inf))
            <> Pdef(\kickP)
            <> Pbind(\instrument, \segPlayer)
            ).finDur(sectionLength)
    );
    
    sp.par(
        Pdef(\rim,
            Pdef(\rimMod,
            )
            // <> Pbind(\dur, Pseq([Rest(1.5), 1.5, Rest(1)], inf))
            // <>Pbind(\amp, Pseq([0.1, 0.7], inf))
            <> ~makeSubdivision.(
                PlaceAll([Rest(1.5), 1.5, Rest(1)], inf),
                PlaceAll([1, 1, 1, 2], inf)
            )
            <> Pbind(\instrument, \rim1)
        ).finDur(sectionLength)
    );

    sp.par(
        Pbindf(
            Pdef(\grain),
            \bufnum, ~bufA,
            \overlap, 1000,
            \trigRate, 500,
            \posRate, 2,
            \rate, 1,
            \recRate, 1,
            \polarityMod, 1,
            \overdub, 0,
            \feedback, 0.0,
            \gain, -12,
            \out, [~convolve_A, ~mainout]
        ).finDur(sectionLength)
    );

    sp.par(
        Pbindf(
            Pdef(\morph),
            \gain, -6,
            \atk, 0,
            \rel, 100,
            \swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([1/2], inf), 1, \sin),
            // \swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([1/3], inf), 1, \sin),
            // \swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([2, 1, 1], inf), 1, \sin),
            // \swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([1.5], inf), 1, \sin),
            // \swap, 0,
            // \swap, 1,
            \out, [~mainout, ~miVerb]
        ).finDur(sectionLength)
    );

    sp.wait(sectionLength);
    ////////////////////////////
    \e.postln;
    sectionLength = 64;

    Pdef(\mod, 
        Pmono(\west,
            \dec, Pkey(\dur) * 1,
            \freq, 30,
            \gate, 1,
            
            // \pitchBendRatio, Pwhite(0.5, 2),
            \glide, 0.01, 
                
            \fm1Ratio, 4, 
            \fm2Ratio, 8,
            \fm1Amount, 0.1, 
            \fm2Amount, 0.1,
            
            \vel, 0.5, 
            \pressure, ~pmodenv.(Pkey(\groupdelta) * 5, Pkey(\dec)), //Pwhite(), 
            // \timbre, Pwhite(0.0,0.75), 
            \timbre, ~pmodenv.(Pwhite(0, 1), Pkey(\dec)),
            \waveShape, 0.25, 
            \waveFolds, ~pmodenv.(1 - Pkey(\groupdelta), Pkey(\dec)), 
            \envType, ~pmodenv.(Pseq([0, 1], inf), 4), 
            // \envtype, 1,
            // \peak, ~pmodenv.(Pwhite(250.0, 4000.0), Pkey(\dec)),
            \peak, ~pmodenv.(Pwhite(250.0, 15000.0), Pkey(\dec)),
            \decay, Pwhite(1, 2),
            // \decay, 1,
            \pan, Pbrown(-0.5,0.5,0.001),
            \amp, 0.5,
            \lfoShape, 0, //Pwhite(), 
            \lfoFreq, Pkey(\dur),
            // \lfoFreq, Pwhite(0.1, 5.0),
            \lfoToWaveShapeAmount, ~pmodenv.((1 - Pkey(\groupdelta)) * 0.25, Pkey(\dec)),
            \lfoToWaveFoldsAmount, ~pmodenv.(Pkey(\groupdelta) * 1, Pkey(\dec)),

            \lfoToReverbMixAmount, Pwhite(), 
            \drift, ~pmodenv.(Pwhite(0, 0.1), Pkey(\dec))
        )
    );

    Pdef(\p1,
        ~makeSubdivision.(
            PlaceAll([1, 1, 1.5, Rest(0.5)], inf),
            PlaceAll([[4, 1], 0, 1, 2], inf)
        )
    );
    
    sp.par(
        Pdef(\west,
            Pdef(\mod) <>
            ~filterBeat.(key: Pkey(\eventcount), beat:[1, 2, 4]) <>
            ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [1, 2, 5], skew: [2, -1], curve: \exp) <> 
            Pdef(\p1)
            <> Pbind(
                \instrument, \west,
                \out, [~grain]
            )
        ).finDur(sectionLength)
    );

    Pdef(\kickMod,
        Pbind(
            // \dur, 1,
            \freq, 60.0,
            \atk, 0.01,
            \dec, Pkey(\groupcount).wrap(1, 2).linlin(1, 2, 0.2, 0.5),
            \fb, Pkey(\eventcount) * 0.5,
            \index, 1.0,
            \ratio, 1.5,
            \drive, 5.0,
            \sweep, 32.0,
            \spread, 2,
            \lofreq, 500.0,
            \lodb, 10.0,
            \midfreq, 1200.0,
            \middb, -20.0,
            \hifreq, 7000.0,
            \hidb, 30.0,
            \gain, -15.0,
            \pan, 0.0,
            \noise, 0.5,
            \amp, 0.5
            // \amp, Pkey(\groupcount).wrap(1, 2).linlin(1, 2, 1, 0.7, 1),
        )
    );

    sp.par(
    Pdef(\drumSliced,
        Pbind(
            \amp, 1,
            \buf, ~sliceBuf.at(\file),
            \sliceStart, 1000,
            //lowest to highest
            \overlap, 10,
            // \trigRate, 10,
            \trigRate, Pseg(Pseq([10, 1000,], inf), 4, 'exp' , inf),
            \slice, ~pGetSlice.(Pseries(1, 1, inf).wrap(0, 2) + Pkey(\sliceStart), ~sliceBuf),
            // \gain, 6,
            \posRate, 0.75,
            \rate, 0.5,
            // \out, 
            \gain, -20,
            \atk, 0,
            \dec, 0.1,
            \out, [~convolve_A, ~mainout]
        )
        // <> Pbind(\timingOffset, Pkey(\groupcount) / Pkey(\groupdelta))
        // <> Pbind(\dur, Pseq([Rest(2), 2, Rest(2), 2], inf))
        // <> Pbind(\dur, Pseq([2, 2, 4], inf))
        <> ~pSkew.(Pdef(\kickP), key: Pkey(\eventcount), group: [1, 2, 5], skew: [2, -1], curve: \exp)
        <> Pdef(\kickP)
        <> Pbind(\instrument, \grainSlicer)
    ).finDur(sectionLength)
    );

    sp.par(
        Pdef(\hh,
            Pbind(
                \freq, 100.0,
                \atk, 0.001,
                \dec, 0.1,
                \gain, -16,
                \pan, 0.0,
                \amp, 1.0,
                \out, [~convolve_B]
            )
            <> ~makeSubdivision.(
                PlaceAll([1.5, 1.5, 1], inf),
                PlaceAll([2,2,1,2], inf)
            )
            <> Pbind(\instrument, \hh,)
        ).finDur(sectionLength)
    );

    sp.par(
        Pdef(\breakSliced,
            Pbind(
                \amp, 1,
                \atk, 0.01,
                \rel, Pkey(\dur) * 0.2,
                \buf, ~breakBuf.at(\file),
                \rate, 1,
                \oneshot, 1,
                \gain, -6,
                \sliceStart, 50,
                \slice, ~pGetSlice.(Pseries(1, 1, inf).wrap(0, 4) + Pkey(\sliceStart), ~breakBuf),
                \out, [~convolve_A],
                \pitchMix, 0.4,
                \pitchRatio, 1,
                \windowSize, 0.05,
                \pitchDispersion, 1.0,
                \timeDispersion, 0.1,
                
            )
            // <> Pbind(\timingOffset, Pkey(\groupcount) / Pkey(\groupdelta))
            // <> ~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[1, 3])
            // <> ~filterBeat.(key: Pkey(\eventcount), beat:[1,3,4])
            <> ~pSkew.(Pdef(\kickP), key: Pkey(\eventcount), group: [1, 2], skew: [0.75], curve: \exp)
            // <>Pbind(\amp, Pseq([0.1, 0.7], inf))
            <> Pdef(\kickP)
            <> Pbind(\instrument, \segPlayer)
            ).finDur(sectionLength)
    );

    sp.par(
        Pmono(\fftStretch_magAbove_mono,
            \amp, 1,
            \gain, 0,
            \buf, ~specBuff2.at(\file),
            \analysis, [~specBuff2.at(\analysis)],
            \fftSize, ~specBuff2.at(\fftSize),
            \rate, 0.1,
            \pos, 0.2,
            // \pos, ~pmodenv.(Pseq([0.2, 0.8],inf), Pseq([4, 8, 4], inf), curve: \sine),
            // \pos, 0.8,
            \len, 0.1,
            \filter, ~pmodenv.(Pseq([1, 4],inf), Pseq([4, 8, 4], inf), curve: \sine),
            \out, [~convolve_B]
        ).finDur(sectionLength)
    );

    Pdef(\rimMod,
        Pbind(
            \freq, 440.0,
            \atk, 0.04,
            \dec, 0.001,
            \fb, Pkey(\groupcount),
            \index, 4.0,
            \ratio, 2.0,
            \sweep, 8.0,
            \spread, 20.0,
            \noise, 1.0,
            \roomsize, 3.0,
            \reverbtime, 5.0,
            \gain, -20.0,
            \pan, 0.0,
            \amp, Pkey(\groupcount).wrap(0,1).linlin(0,1,0.5,0.8),
            \out, [~grain, ~mainout]
        )
    );

    Pdef(\kickP,
        ~makeSubdivision.(
            PlaceAll([1.5, 1.5, 1], inf),
            PlaceAll([4, 4, 1, 5, 5, 2], inf)
        )        
    );

    Pdef(\kickP2,
        ~makeSubdivision.(
            PlaceAll([1, 1, 1.5, 0.5], inf),
            PlaceAll([[3, 1], 4, 3, 4], inf)
        )
    );

    sp.par(
        Pdef(\kickMod)
        // Pswitch(Pdef(\kickMod), Pdef(\rimMod), Pkey(\groupcount).wrap(0,1))
        <> ~filterBeat.(key: Pkey(\eventcount), beat:[1, 3, 4])
        <> ~pSkew.(Pdef(\kickP), key: Pkey(\eventcount), group: [1, 2], skew: [0.5], curve: \exp)
        <> Pdef(\kickP)
        // <> Pbind(\dur, 1.333)
        <> Pbind(
            \instrument, \fmKick2,
            \out, [~convolve_B]
        ).finDur(sectionLength)
    );
 
    sp.par(
        Pdef(\rim,
            Pdef(\rimMod,
            )
            // <> Pbind(\dur, Pseq([Rest(1.5), 1.5, Rest(1)], inf))
            // <>Pbind(\amp, Pseq([0.1, 0.7], inf))
            <> ~makeSubdivision.(
                PlaceAll([Rest(1.5), 1.5, Rest(1)], inf),
                PlaceAll([1, 1, 1, 2], inf)
            )
            <> Pbind(\instrument, \rim1)
        ).finDur(sectionLength)
    );

    sp.par(
        Pbindf(
            Pdef(\grain),
            \bufnum, ~bufA,
            \overlap, 1000,
            \trigRate, 500,
            \posRate, 2,
            \rate, 1,
            \recRate, 1,
            \polarityMod, 1,
            \overdub, 0,
            \feedback, 0.0,
            \gain, -12,
            \out, [~convolve_A, ~mainout]
        ).finDur(sectionLength)
    );

    sp.par(
        Pbindf(
            Pmono(\pulsar_mono),
            \triggerRate, 16,
            \fluxMF, 1.5,
            \fluxMD, 1,
            \grainFreq, 32,
            \overlap, 1,
            \pmRatio, 100,
            \pmIndex, 0.5,
            \density, 0.1,
            \polarityMod, 0,
            \out, [~convolve_B, ~miVerb],
        ).finDur(sectionLength)
    );

    sp.par(
        Pbindf(
            Pdef(\morph),
            \gain, -6,
            \atk, 0,
            \rel, 100,
            // \swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([4], inf), 1, \sin),
            // \swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([1/3], inf), 1, \sin),
            \swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([2, 1, 1], inf), 1, \sin),
            // \swap, 0,
            // \swap, 1,
            \out, [~mainout, ~miVerb]
        ).finDur(sectionLength)
    );

    sp.wait(sectionLength);
    //////////////////////////////
    \f.postln;
    sectionLength = 64;

    Pdef(\mod, 
        Pmono(\west,
            \dec, Pkey(\dur) * 1,
            \freq, 30,
            \gate, 1,
            
            // \pitchBendRatio, Pwhite(0.5, 2),
            \glide, 0.01, 
                
            \fm1Ratio, 4, 
            \fm2Ratio, 8,
            \fm1Amount, 0.1, 
            \fm2Amount, 0.1,
            
            \vel, 0.5, 
            \pressure, ~pmodenv.(Pkey(\groupdelta) * 5, Pkey(\dec)), //Pwhite(), 
            // \timbre, Pwhite(0.0,0.75), 
            \timbre, ~pmodenv.(Pwhite(0, 1), Pkey(\dec)),
            \waveShape, 0.25, 
            \waveFolds, ~pmodenv.(1 - Pkey(\groupdelta), Pkey(\dec)), 
            \envType, ~pmodenv.(Pseq([0, 1], inf), 4), 
            // \envtype, 1,
            // \peak, ~pmodenv.(Pwhite(250.0, 4000.0), Pkey(\dec)),
            \peak, ~pmodenv.(Pwhite(250.0, 15000.0), Pkey(\dec)),
            \decay, Pwhite(1, 2),
            // \decay, 1,
            \pan, Pbrown(-0.5,0.5,0.001),
            \amp, 0.5,
            \lfoShape, 0, //Pwhite(), 
            \lfoFreq, Pkey(\dur),
            // \lfoFreq, Pwhite(0.1, 5.0),
            \lfoToWaveShapeAmount, ~pmodenv.((1 - Pkey(\groupdelta)) * 0.25, Pkey(\dec)),
            \lfoToWaveFoldsAmount, ~pmodenv.(Pkey(\groupdelta) * 1, Pkey(\dec)),

            \lfoToReverbMixAmount, Pwhite(), 
            \drift, ~pmodenv.(Pwhite(0, 0.1), Pkey(\dec))
        )
    );

    Pdef(\p1,
        ~makeSubdivision.(
            PlaceAll([1, 1, 1.5, Rest(0.5)], inf),
            PlaceAll([[4, 1], 0, 1, 2], inf)
        )
    );
    
    sp.par(
        Pdef(\west,
            Pdef(\mod) <>
            ~filterBeat.(key: Pkey(\eventcount), beat:[1, 2, 4]) <>
            ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [1, 2, 5], skew: [2, -1], curve: \exp) <> 
            Pdef(\p1)
            <> Pbind(
                \instrument, \west,
                \out, [~grain]
            )
        ).finDur(sectionLength)
    );

    Pdef(\kickMod,
        Pbind(
            // \dur, 1,
            \freq, 60.0,
            \atk, 0.01,
            \dec, Pkey(\groupcount).wrap(1, 2).linlin(1, 2, 0.2, 0.5),
            \fb, Pkey(\eventcount) * 0.5,
            \index, 1.0,
            \ratio, 1.5,
            \drive, 5.0,
            \sweep, 32.0,
            \spread, 2,
            \lofreq, 500.0,
            \lodb, 10.0,
            \midfreq, 1200.0,
            \middb, -20.0,
            \hifreq, 7000.0,
            \hidb, 30.0,
            \gain, -15.0,
            \pan, 0.0,
            \noise, 0.5,
            \amp, 0.5
            // \amp, Pkey(\groupcount).wrap(1, 2).linlin(1, 2, 1, 0.7, 1),
        )
    );

    sp.par(
    Pdef(\drumSliced,
        Pbind(
            \amp, 1,
            \buf, ~sliceBuf.at(\file),
            \sliceStart, 1000,
            //lowest to highest
            \overlap, 10,
            // \trigRate, 10,
            \trigRate, Pseg(Pseq([10, 1000,], inf), 4, 'exp' , inf),
            \slice, ~pGetSlice.(Pseries(1, 1, inf).wrap(0, 2) + Pkey(\sliceStart), ~sliceBuf),
            // \gain, 6,
            \posRate, 0.75,
            \rate, 0.5,
            // \out, 
            \gain, -20,
            \atk, 0,
            \dec, 0.1,
            \out, [~convolve_A, ~mainout]
        )
        // <> Pbind(\timingOffset, Pkey(\groupcount) / Pkey(\groupdelta))
        // <> Pbind(\dur, Pseq([Rest(2), 2, Rest(2), 2], inf))
        // <> Pbind(\dur, Pseq([2, 2, 4], inf))
        <> ~pSkew.(Pdef(\kickP), key: Pkey(\eventcount), group: [1, 2, 5], skew: [2, -1], curve: \exp)
        <> Pdef(\kickP)
        <> Pbind(\instrument, \grainSlicer)
    ).finDur(sectionLength)
    );

    sp.par(
        Pdef(\hh,
            Pbind(
                \freq, 100.0,
                \atk, 0.001,
                \dec, 0.1,
                \gain, -16,
                \pan, 0.0,
                \amp, 1.0,
                \out, [~convolve_B]
            )
            <> ~makeSubdivision.(
                PlaceAll([1.5, 1.5, 1], inf),
                PlaceAll([2,2,1,2], inf)
            )
            <> Pbind(\instrument, \hh,)
        ).finDur(sectionLength)
    );

    sp.par(
        Pdef(\breakSliced,
            Pbind(
                \amp, 1,
                \atk, 0.01,
                \rel, Pkey(\dur) * 0.2,
                \buf, ~breakBuf.at(\file),
                \rate, 1,
                \oneshot, 1,
                \gain, -6,
                \sliceStart, 50,
                \slice, ~pGetSlice.(Pseries(1, 1, inf).wrap(0, 4) + Pkey(\sliceStart), ~breakBuf),
                \out, [~mainout, ~convolve_A, ~grain],
                \pitchMix, 0.4,
                \pitchRatio, 1,
                \windowSize, 0.05,
                \pitchDispersion, 1.0,
                \timeDispersion, 0.1,
                
            )
            // <> Pbind(\timingOffset, Pkey(\groupcount) / Pkey(\groupdelta))
            // <> ~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[1, 3])
            <> ~filterBeat.(key: Pkey(\eventcount), beat:[1,3,4])
            // <> ~pSkew.(Pdef(\kickP), key: Pkey(\eventcount), group: [1, 2], skew: [0.75], curve: \exp)
            // <>Pbind(\amp, Pseq([0.1, 0.7], inf))
            <> Pdef(\kickP)
            <> Pbind(\instrument, \segPlayer)
            ).finDur(sectionLength)
    );

    sp.par(
        Pmono(\fftStretch_magAbove_mono,
            \amp, 1,
            \gain, 0,
            \buf, ~specBuff2.at(\file),
            \analysis, [~specBuff2.at(\analysis)],
            \fftSize, ~specBuff2.at(\fftSize),
            \rate, 0.1,
            \pos, ~pmodenv.(Pseq([0.2, 0.8],inf), Pseq([4, 8, 4], inf), curve: \sine),
            // \pos, 0.8,
            \len, 0.1,
            \filter, ~pmodenv.(Pseq([1, 4],inf), Pseq([4, 8, 4], inf), curve: \sine),
            \out, [~convolve_B]
        ).finDur(sectionLength)
    );

    Pdef(\rimMod,
        Pbind(
            \freq, 440.0,
            \atk, 0.04,
            \dec, 0.001,
            \fb, Pkey(\groupcount),
            \index, 4.0,
            \ratio, 2.0,
            \sweep, 8.0,
            \spread, 20.0,
            \noise, 1.0,
            \roomsize, 3.0,
            \reverbtime, 5.0,
            \gain, -20.0,
            \pan, 0.0,
            \amp, Pkey(\groupcount).wrap(0,1).linlin(0,1,0.5,0.8),
            \out, [~grain, ~mainout]
        )
    );

    Pdef(\kickP,
        ~makeSubdivision.(
            PlaceAll([1.5, 1.5, 1], inf),
            PlaceAll([4, 4, 1, 5, 5, 2], inf)
        )        
    );

    Pdef(\kickP2,
        ~makeSubdivision.(
            PlaceAll([1, 1, 1.5, 0.5], inf),
            PlaceAll([[3, 1], 4, 3, 4], inf)
        )
    );

    sp.par(
        Pdef(\kickMod)
        // Pswitch(Pdef(\kickMod), Pdef(\rimMod), Pkey(\groupcount).wrap(0,1))
        <> ~filterBeat.(key: Pkey(\eventcount), beat:[1, 3, 4])
        <> ~pSkew.(Pdef(\kickP), key: Pkey(\eventcount), group: [1, 2], skew: [0.5], curve: \exp)
        <> Pdef(\kickP)
        // <> Pbind(\dur, 1.333)
        <> Pbind(
            \instrument, \fmKick2,
            \out, [~mainout]
        ).finDur(sectionLength)
    );
 
    sp.par(
        Pdef(\rim,
            Pdef(\rimMod,
            )
            // <> Pbind(\dur, Pseq([Rest(1.5), 1.5, Rest(1)], inf))
            // <>Pbind(\amp, Pseq([0.1, 0.7], inf))
            <> ~makeSubdivision.(
                PlaceAll([Rest(1.5), 1.5, Rest(1)], inf),
                PlaceAll([1, 1, 1, 2], inf)
            )
            <> Pbind(\instrument, \rim1)
        ).finDur(sectionLength)
    );

    sp.par(
        Pbindf(
            Pdef(\grain),
            \bufnum, ~bufA,
            \overlap, 1000,
            \trigRate, 500,
            \posRate, 2,
            \rate, 1,
            \recRate, 1,
            \polarityMod, 1,
            \overdub, 0,
            \feedback, 0.0,
            \gain, -12,
            \out, [~convolve_A, ~mainout]
        ).finDur(sectionLength)
    );

    sp.par(
        Pbindf(
            Pmono(\pulsar_mono),
            \triggerRate, 16,
            \fluxMF, 1.5,
            \fluxMD, 1,
            \grainFreq, 32,
            \overlap, 1,
            \pmRatio, 100,
            \pmIndex, 0.5,
            \density, 0.1,
            \polarityMod, 0,
            \out, [~miVerb],
        ).finDur(sectionLength)
    );

    sp.par(
        Pbindf(
            Pdef(\morph),
            \gain, -6,
            \atk, 0,
            \rel, 100,
            // \swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([4], inf), 1, \sin),
            // \swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([1/3], inf), 1, \sin),
            \swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([2, 1, 1], inf), 1, \sin),
            // \swap, 0,
            // \swap, 1,
            \out, [~mainout, ~miVerb]
        ).finDur(sectionLength)
    );

    sp.wait(sectionLength);

    //////////////////////////////
    \g.postln;
    sectionLength = 32;

    sp.par(
        Pdef(\drumSliced,
            Pbind(
                \amp, 1,
                \buf, ~sliceBuf.at(\file),
                \sliceStart, 1000,
                //lowest to highest
                \overlap, 10,
                // \trigRate, 10,
                \trigRate, Pseg(Pseq([10, 1000,], inf), 4, 'exp' , inf),
                \slice, ~pGetSlice.(Pseries(1, 1, inf).wrap(0, 2) + Pkey(\sliceStart), ~sliceBuf),
                // \gain, 6,
                \posRate, 0.75,
                \rate, 0.5,
                // \out, 
                \gain, -20,
                \atk, 0,
                \dec, 0.1,
                \out, [~convolve_A, ~mainout]
            )
            // <> Pbind(\timingOffset, Pkey(\groupcount) / Pkey(\groupdelta))
            // <> Pbind(\dur, Pseq([Rest(2), 2, Rest(2), 2], inf))
            // <> Pbind(\dur, Pseq([2, 2, 4], inf))
            <> ~pSkew.(Pdef(\kickP), key: Pkey(\eventcount), group: [1, 2, 5], skew: [2, -1], curve: \exp)
            <> Pdef(\kickP)
            <> Pbind(\instrument, \grainSlicer)
        ).finDur(sectionLength)
        );

        sp.par(
            Pmono(\fftStretch_magAbove_mono,
                \amp, 1,
                \gain, 0,
                \buf, ~specBuff2.at(\file),
                \analysis, [~specBuff2.at(\analysis)],
                \fftSize, ~specBuff2.at(\fftSize),
                \rate, 0.1,
                \pos, ~pmodenv.(Pseq([0.2, 0.8],inf), Pseq([4, 8, 4], inf), curve: \sine),
                // \pos, 0.8,
                \len, 0.1,
                \filter, ~pmodenv.(Pseq([1, 4],inf), Pseq([4, 8, 4], inf), curve: \sine),
                \out, [~convolve_B]
            ).finDur(sectionLength)
        );

        sp.par(
            Pbindf(
                Pdef(\morph),
                \gain, -6,
                \atk, 0,
                \rel, 100,
                // \swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([4], inf), 1, \sin),
                // \swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([1/3], inf), 1, \sin),
                \swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([2, 1, 1], inf), 1, \sin),
                // \swap, 0,
                // \swap, 1,
                \out, [~mainout, ~miVerb]
            ).finDur(sectionLength)
        );
    })
).play(t);
)