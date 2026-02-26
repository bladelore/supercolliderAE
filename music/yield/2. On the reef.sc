Safety(s).enable;
(
    b = Buffer.read(s,"/Users/aelazary/Desktop/Samples etc./NEW sample lib/1030 Rave Stabs/100 percent.wav");
    
    c = Buffer.read(s,"/Users/aelazary/Desktop/Samples etc./EchoThiefImpulseResponseLibrary/Brutalism/GeiselLibrary.wav");
        
    d = Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./EchoThiefImpulseResponseLibrary/Miscellaneous/MillsArtMuseum.wav");
)

(
    ~birdBuf = Dictionary();
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Field recs/Bell birds.wav", ~birdBuf, 0.3, \crest);
    ~breakBuf = Dictionary();
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Silent Hill/Drum Loops/Silent Hill 3/Dance With Night Wind/Slayed 01.WAV", ~breakBuf, 0.1, \crest);
)

(
    ~busArr = [
        ~vocoder_out=Bus.audio(s,2),
        ~reverb_out=Bus.audio(s,2),
        
        ~fb2_out=Bus.audio(s,2),
        ~drum_out=Bus.audio(s,2),
        ~sines_out=Bus.audio(s,2),
        ~sines2_out=Bus.audio(s,2),
        ~breakSliced_out=Bus.audio(s,2),
        ~tape_out=Bus.audio(s,2),
        ~grainSlicer_out=Bus.audio(s,2),
    ]
)
(
    ~recorders = ~recordBuses.value(
        ~busArr,
        Platform.recordingsDir +/+ "on the reef_end/%.wav"
    );
)

( 
// g.free;
// g = Group.new(RootNode(Server.default), \addToTail);

// Synth(\comp,
//     [
//         \ratio: 6,
//         \thresh, -40,
//         \atk, 0.1,
//         \rel, 1000,
//         \makeup, 0,
//         \automakeup, 0
//     ],
//     target: g,   
//     addAction: \addToTail,
// );

t = TempoClock.new(180/60).permanent_(true).schedAbs(0, {t.beatsPerBar_(4)});
Pdef(\player,
    Pspawner({| sp |
    var sectionLength = 29;
    //fx const
    sp.par(
        Pbindf(
            Pdef(\miVerb),
            \time, 0.01,
            \damp, 0.6,
            \hp, 0.0,
            \freeze, 0,
            \diff, 0.9,
            \gain, -18,
            \out, [~mainout, ~reverb_out]
        )
    );
    /////////////////////////////
    \a.postln;   
    Pdef(\p2,
        ~makeSubdivision.(
            PlaceAll([1, 1, 1, 4], inf),
            PlaceAll([4, 4, 4, 4], inf)
        )
    );
   
    Pdef(\fb2Mod,
        PmonoArtic(\fb2,
            \amp, 0.05,
            \gain, 0,
            // \buf, Pseq([b, d], inf).stutter(3),
            \buf, b,
            \window, 4096,
            \impulse, 5.0,
            // \impulse, ~pmodenv.(Pexprand(5, 20000, inf), Pkey(\dec)),
    
            // \window, Pseq([4096, 1024, 512], inf).stutter(3),
            \atk, 0.1,
            \dec, 0.4,
            // \dec, Pkey(\dur),
            \sustainTime, Pkey(\dur),
            // \exciter, 0.6,
            // \density, ~pmodenv.(1 - Pkey(\groupdelta), Pkey(\dec)),
            \exciter, ~pmodenv.(1 - Pkey(\groupdelta), Pkey(\dec)),
            \feedback, ~pmodenv.(Pseq([0.2, 0.4, 0.8],inf), Pseq([4, 8, 2, 4],inf)),
    
            \time, ~pmodenv.(Pseq([0.005, 0.001, 0.010],inf) * 1 * Pkey(\groupdelta), Pkey(\dec)),
            \damp, ~pmodenv.(Pseq([1, 0.1],inf), Pwhite(0.01, 0.4, inf)),
            // \damp, ~pmodenv.(Pkey(\groupdelta) * 1, Pkey(\dec)),
    
            \filter, ~pmodenv.(Pseq([500, 20000],inf), Pseq([1, 1, 2], inf)),
    
            \delay2, ~pmodenv.(Pseq([0, 1, 0],inf), Pseq([0.5, 0.25], inf)),
    
            \rev, ~pmodenv.(Pexprand(0.1, 10, inf), Pseq([2, 4], inf)),
            // \pan, ~pmodenv.(Pwhite(-0.8, 0.8, inf), Pseq([0.5, 0.25],inf)),
        )
    );
    
    sp.par(
        Pbindf(
            Pdef(\fb2Seq,
                Pdef(\fb2Mod)
                // <> ~filterBeat.(key: Pkey(\eventcount), beat:[1, 3, 4], mod: 5)
                // <> ~pSkew.(Pdef(\p2), key: Pkey(\eventcount), group: [1, 2], skew: [-1, 1], curve: \exp)
                <> ~pSkew.(Pdef(\p2), key: Pkey(\cyclecount), group: [1, 2], skew: [-0.5, 0.5], curve: \exp)
                <> Pdef(\p2)
                // <> Pbind(\instrument, \fb2)
            ),
            \out, [~mainout, ~miVerb, ~fb2_out]
        ).finDur(28)
    );
    
    sp.wait(sectionLength);
    ///////////////////////////////////
    \b.postln;
    sectionLength = 32;

    // Pdef(\p2,
    //     ~makeSubdivision.(
    //         PlaceAll([1, 1, 1, Pseq([3,2,1], inf)], inf),
    //         PlaceAll([4, 4, 4, 4], inf)
    //     )
    // );

    Pdef(\p2,
        ~makeSubdivision.(
            PlaceAll([1, 1, 1, 4], inf),
            PlaceAll([4, 4, 4, 4], inf)
        )
    );
    
    Pdef(\fb2Mod,
        PmonoArtic(\fb2,
            \amp, 0.05,
            \gain, 0,
            \buf, Pseq([b, d], inf).stutter(3),
            // \buf, b,
            \window, 4096,
            \impulse, 5.0,
            // \impulse, ~pmodenv.(Pexprand(5, 20000, inf), Pkey(\dec)),
    
            \window, Pseq([4096, 1024, 512], inf).stutter(3),
            \atk, 0.1,
            \dec, 0.4,
            // \dec, Pkey(\dur),
            \sustainTime, Pkey(\dur),
            // \exciter, 0.6,
            // \density, ~pmodenv.(1 - Pkey(\groupdelta), Pkey(\dec)),
            \exciter, ~pmodenv.(1 - Pkey(\groupdelta), Pkey(\dec)),
            \feedback, ~pmodenv.(Pseq([0.2, 0.4, 0.8],inf), Pseq([4, 8, 2, 4],inf)),
    
            \time, ~pmodenv.(Pseq([0.005, 0.001, 0.010],inf) * 1 * Pkey(\groupdelta), Pkey(\dec)),
            \damp, ~pmodenv.(Pseq([1, 0.1],inf), Pwhite(0.01, 0.4, inf)),
            // \damp, ~pmodenv.(Pkey(\groupdelta) * 1, Pkey(\dec)),
    
            \filter, ~pmodenv.(Pseq([500, 20000],inf), Pseq([1, 1, 2], inf)),
    
            \delay2, ~pmodenv.(Pseq([0, 1, 0],inf), Pseq([0.5, 0.25], inf)),
    
            \rev, ~pmodenv.(Pexprand(0.1, 10, inf), Pseq([2, 4], inf)),
            // \pan, ~pmodenv.(Pwhite(-0.8, 0.8, inf), Pseq([0.5, 0.25],inf)),
        )
    );
    
    sp.par(
        Pbindf(
            Pdef(\fb2Seq,
                Pdef(\fb2Mod)
                // <> ~filterBeat.(key: Pkey(\eventcount), beat:[1, 3, 4], mod: 5)
                // <> ~pSkew.(Pdef(\p2), key: Pkey(\eventcount), group: [1, 2], skew: [-1, 1], curve: \exp)
                <> ~pSkew.(Pdef(\p2), key: Pkey(\cyclecount), group: [1, 2], skew: [-0.5, 0.5], curve: \exp)
                <> Pdef(\p2)
                <> Pbind(\instrument, \fb2)
            ),
            \out, [~mainout, ~miVerb, ~fb2_out]
        ).finDur(28)
    );
    
    sp.wait(sectionLength);
    ///////////////////////////////////

    \c.postln;
    sectionLength = 72; 

    Pdef(\p2,
        ~makeSubdivision.(
            PlaceAll([1, 1, 1, 1, 2, 2, 2, 2,] * 2, inf),
            PlaceAll([4, 4, 4, 4], inf)
        )
    );

    sp.par(
        Pmono(\grainSlicer_mono,
            \amp, 1,
            \buf, ~birdBuf.at(\file),
            \overlap, 100,
            \trigRate, Pseg(Pseq([10, 1000,], inf), 4, 'exp' , inf),
            \slice, ~pGetSlice.(30, ~birdBuf),
            \gain, 6,
            \posRate, 1,
            \rate, 0.75,
            \out, [~convolve_B, ~miVerb]
        ).finDur(sectionLength)
    );
    
    Pdef(\fb2Mod,
        PmonoArtic(\fb2,
            \amp, 0.05,
            \gain, 0,
            \buf, Pseq([b, d], inf).stutter(3),
            // \buf, c,
            // \window, 4096,
            \impulse, 5.0,
            // \impulse, ~pmodenv.(Pexprand(5, 2000, inf), Pkey(\dec)),
    
            \window, Pseq([4096, 1024, 512], inf).stutter(3),
            \atk, 0.1,
            \dec, 0.2,
            // \dec, Pkey(\dur),
            \sustainTime, Pkey(\dur),
            // \exciter, 0.6,
            // \density, ~pmodenv.(1 - Pkey(\groupdelta), Pkey(\dec)),
            \exciter, ~pmodenv.(1 - Pkey(\groupdelta), Pkey(\dec)),
            \feedback, ~pmodenv.(Pseq([0.2, 0.4, 0.8],inf), Pseq([4, 8, 2, 4],inf)),
    
            \time, ~pmodenv.(Pseq([0.005, 0.001, 0.010],inf) * 1 * Pkey(\groupdelta), Pkey(\dec)),
            // \damp, ~pmodenv.(Pseq([1, 0.1],inf), Pwhite(0.01, 0.4, inf)),
            // \damp, ~pmodenv.(1 - Pkey(\groupdelta) * 1, Pkey(\dec)),
    
            \filter, ~pmodenv.(Pseq([500, 20000],inf), Pseq([1, 1, 2], inf)),
    
            \delay2, ~pmodenv.(Pseq([0, 1, 0],inf), Pseq([0.5, 0.25], inf)),
    
            \rev, ~pmodenv.(Pexprand(0.1, 10, inf), Pseq([2, 4], inf)),
            // \density, 1.0,
            // \bias, 10000.0,
    
            // \pan, ~pmodenv.(Pwhite(-0.8, 0.8, inf), Pseq([0.5, 0.25],inf)),
        )
    );
    
    sp.par(
        Pbindf(
            Pdef(\fb2Seq,
                Pdef(\fb2Mod)
                // <> ~filterBeat.(key: Pkey(\eventcount), beat:[1, 3, 4], mod: 5)
                // <> ~pSkew.(Pdef(\p2), key: Pkey(\eventcount), group: [1, 2], skew: [-1, 1], curve: \exp)
                <> ~pSkew.(Pdef(\p2), key: Pkey(\cyclecount), group: [1, 2], skew: [-0.5, 0.5], curve: \exp)
                <> Pdef(\p2)
                // <> Pbind(\instrument, \fb2)
            ),
            \out, [~mainout, ~convolve_A, ~fb2_out]
        ).finDur(sectionLength)
    );

    sp.par(
        Pdef(\drum, 
            Pbind(
                // \fmPerc2,
                \freq, Pseq([40, 60, 40], inf).stutter(12),
                \atk, 0.04,
                \dec, Pkey(\groupdelta).lincurve(0, 1, 0.2, 0.1),
                // \dec, 0.5,
                // \rel, 0.01,
                \fb, Pseq([0, 2, 0], inf).stutter(3),
                \pulseWidth, Pkey(\groupdelta) + 0.25,
                // \index, (Pkey(\groupdelta).lincurve(1, 0, 3, 4) * 0.4).stutter(4),
                \ratio, 2,
                \sweep, 8.0,
                \spread, 10,
                // \noise, Pseq([0, 2, 0], inf).stutter(3),
                // \drive, 0,
                \feedback, -2,
                // \feedback, ((1-Pkey(\groupdelta)).linlin(0,1,3,4)).stutter(4),
                \fbmod, 0,
                \lofreq, 500.0,
                \lodb, 10.0,
                \midfreq, 1200.0,
                \middb, -12.0,
                \hifreq, 7000.0,
                \hidb, 10.0,
                \gain, -12.0,
                \pan, 0.0,
                // \amp, 1,
                \amp, Pkey(\groupdelta).lincurve(0, 1, 1, 0.1),
                \out, [~mainout, ~miVerb, ~drum_out]
            )
            <> ~filterBeat.(key: Pkey(\cyclecount), beat:[1, 4, 5], mod: 3)
            // <> ~pSkew.(Pdef(\p2), key: Pkey(\eventcount), group: [1, 2], skew: [-1, 1], curve: \exp)
            <> ~pSkew.(Pdef(\p2), key: Pkey(\cyclecount), group: [1, 2], skew: [-0.25, 0.5], curve: \exp)
            <> Pdef(\p2)
            <> Pbind(\instrument, \fmPerc2)
        ).finDur(sectionLength)
    );

    sp.par(
        Pbindf(
            Pdef(\morph),
            \gain, -12,
            \atk, 0,
            \rel, 100,
            // \swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([1/2], inf), 1, \sin),
            // \swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([1/3], inf), 1, \sin),
            \swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([4], inf), 1, \sin),
            // \swap, 0,
            // \swap, 1,
            \out, [~mainout, ~miVerb, ~vocoder_out]
        ).finDur(sectionLength)
    );

    sp.wait(sectionLength);
    ///////////////////////////////

    \d.postln;
    sectionLength = 80;
    
    sp.par(
        PfadeIn(
            Pmono(\driftingSines_mono2,
                \lfoFreq, 0.002,
                \freq, 60 * [0, 7, 11].midiratio,
                \pitchDev, [1, 1.5],
                \numHarm, 4,
                \gain, 0.0,
                \amp, 1.0,
                \out, [~sines_out, ~miVerb]
            )
        ,8).finDur(sectionLength)
    );

    Pdef(\p2,
        ~makeSubdivision.(
            PlaceAll([1.5, 1.5, 1, 1]*2, inf),
            PlaceAll([4, 4, 4, 4], inf)
        )
    );

    sp.par(
        Pmono(\grainSlicer_mono,
            \amp, 1,
            \buf, ~birdBuf.at(\file),
            \overlap, 100,
            \trigRate, Pseg(Pseq([10, 1000,], inf), 4, 'exp' , inf),
            \slice, ~pGetSlice.(30, ~birdBuf),
            \gain, 6,
            \posRate, 1,
            \rate, 0.75,
            \out, [~convolve_B, ~miVerb]
        ).finDur(sectionLength)
    );

      Pdef(\fb2Mod,
        PmonoArtic(\fb2,
            \amp, 0.05,
            \gain, 0,
            \buf, Pseq([b, d], inf).stutter(3),
            // \buf, c,
            // \window, 4096,
            \impulse, 5.0,
            // \impulse, ~pmodenv.(Pexprand(5, 2000, inf), Pkey(\dec)),
    
            \window, Pseq([4096, 1024, 512], inf).stutter(3),
            \atk, 0.1,
            \dec, 0.2,
            // \dec, Pkey(\dur),
            \sustainTime, Pkey(\dur),
            // \exciter, 0.6,
            // \density, ~pmodenv.(1 - Pkey(\groupdelta), Pkey(\dec)),
            \exciter, ~pmodenv.(1 - Pkey(\groupdelta), Pkey(\dec)),
            \feedback, ~pmodenv.(Pseq([0.2, 0.4, 0.8],inf), Pseq([4, 8, 2, 4],inf)),
    
            \time, ~pmodenv.(Pseq([0.005, 0.001, 0.010],inf) * 1 * Pkey(\groupdelta), Pkey(\dec)),
            \damp, ~pmodenv.(Pseq([1, 0.1],inf), Pwhite(0.01, 0.4, inf)),
            // \damp, ~pmodenv.(1 - Pkey(\groupdelta) * 1, Pkey(\dec)),
    
            \filter, ~pmodenv.(Pseq([500, 20000],inf), Pseq([1, 1, 2], inf)),
    
            \delay2, ~pmodenv.(Pseq([0, 1, 0],inf), Pseq([0.5, 0.25], inf)),
    
            \rev, ~pmodenv.(Pexprand(0.1, 10, inf), Pseq([2, 4], inf)),
            // \density, 1.0,
            // \bias, 10000.0,
    
            // \pan, ~pmodenv.(Pwhite(-0.8, 0.8, inf), Pseq([0.5, 0.25],inf)),
        )
    );
    
    sp.par(
        Pbindf(
            Pdef(\fb2Seq,
                Pdef(\fb2Mod)
                // <> ~filterBeat.(key: Pkey(\eventcount), beat:[1, 4], mod: 6)
                // <> ~pSkew.(Pdef(\p2), key: Pkey(\eventcount), group: [1, 2], skew: [-1, 1], curve: \exp)
                <> ~pSkew.(Pdef(\p2), key: Pkey(\cyclecount), group: [1, 2], skew: [-0.5, 0.5], curve: \exp)
                <> Pdef(\p2)
                // <> Pbind(\instrument, \fb2)
            ),
            \out, [~mainout, ~convolve_A, ~fb2_out]
        ).finDur(sectionLength)
    );

    sp.par(
        Pdef(\drum, 
            Pbind(
                // \fmPerc2,
                \freq, Pseq([40, 60, 40], inf).stutter(4),
                \atk, 0.04,
                \dec, Pkey(\groupdelta).lincurve(0, 1, 0.2, 0.1),
                // \dec, 0.5,
                // \rel, 0.01,
                \fb, Pseq([0, 2, 0], inf).stutter(3),
                \pulseWidth, Pkey(\groupdelta) + 0.25,
                // \index, (Pkey(\groupdelta).lincurve(1, 0, 3, 4) * 0.4).stutter(4),
                \ratio, 2,
                \sweep, 8.0,
                \spread, 10,
                // \noise, Pseq([0, 2, 0], inf).stutter(3),
                // \drive, 0,
                \feedback, -2,
                // \feedback, ((1-Pkey(\groupdelta)).linlin(0,1,3,4)).stutter(4),
                \fbmod, 0,
                \lofreq, 500.0,
                \lodb, 10.0,
                \midfreq, 1200.0,
                \middb, -12.0,
                \hifreq, 7000.0,
                \hidb, 10.0,
                \gain, -12.0,
                \pan, 0.0,
                // \amp, 1,
                \amp, Pkey(\groupdelta).lincurve(0, 1, 1, 0.1),
                \out, [~mainout, ~miVerb, ~drum_out]
            )
            <> ~filterBeat.(key: Pkey(\cyclecount), beat:[1, 4, 6], mod: 6)
            // <> ~pSkew.(Pdef(\p2), key: Pkey(\eventcount), group: [1, 2], skew: [-1, 1], curve: \exp)
            <> ~pSkew.(Pdef(\p2), key: Pkey(\cyclecount), group: [1, 2], skew: [-0.5, 0.5], curve: \exp)
            <> Pdef(\p2)
            <> Pbind(\instrument, \fmPerc2)
        ).finDur(sectionLength)
    );

    sp.par(
        Pbindf(
            Pdef(\morph),
            \gain, -12,
            \atk, 0,
            \rel, 100,
            // \swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([1/2], inf), 1, \sin),
            // \swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([1/3], inf), 1, \sin),
            \swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([4], inf), 1, \sin),
            // \swap, 0,
            // \swap, 1,
            \out, [~mainout, ~vocoder_out]
        ).finDur(sectionLength)
    );
    
    sp.wait(sectionLength);
    // /////////////////////////
    \e.postln;
    sectionLength = 128;
    
    sp.par(
        Pmono(\driftingSines_mono2,
            \lfoFreq, 0.001,
            \freq, 60 * 18.midiratio,
            \pitchDev, 13,
            \numHarm, 12,
            \gain, -18,
            \amp, 1.0,
            \out, [~sines_out, ~miVerb]
            // \out, [~convolve_B]
        ).finDur(sectionLength)
    );

    sp.par(
        Pmono(\driftingSines_mono2,
            \lfoFreq, 0.001,
            \freq, 60 * [0, 7, 11].midiratio,
            \pitchDev, [2, 1.5],
            \numHarm, 4,
            \gain, 0.0,
            \amp, 1.0,
            \out, [~sines2_out, ~miVerb]
            // \out, [~convolve_B]
        ).finDur(sectionLength)
    );

    sp.par(
        Pmono(\grainSlicer_mono,
            \amp, 1,
            \buf, ~birdBuf.at(\file),
            \overlap, 100,
            \trigRate, Pseg(Pseq([10, 1000,], inf), 4, 'exp' , inf),
            \slice, ~pGetSlice.(30, ~birdBuf),
            \gain, 6,
            \posRate, 1,
            \rate, 1,
            \out, [~convolve_B, ~miVerb]
        ).finDur(sectionLength)
    );

    Pdef(\p2,
        ~makeSubdivision.(
            PlaceAll([1.5, 1.5, 1, 1] * 2, inf),
            PlaceAll([4, 4, 2, 4], inf)
        )
    );

    Pdef(\fb2Mod,
        PmonoArtic(\fb2,
            \amp, 0.05,
            \gain, 0,
            \buf, Pseq([b, d], inf).stutter(3),
            // \buf, c,
            // \window, 4096 * 2,
            \impulse, 5.0,
            // \impulse, ~pmodenv.(Pexprand(5, 25, inf), Pseq([4,4],inf)),
    
            \window, Pseq([4096, 1024, 512], inf).stutter(3),
            \atk, 0.1,
            \dec, 0.5,
            // \dec, Pkey(\dur),
            \sustainTime, Pkey(\dur),
            // \exciter, 0.6,
            // \density, ~pmodenv.(1 - Pkey(\groupdelta), Pkey(\dec)),
            \exciter, ~pmodenv.(1 - Pkey(\groupdelta), Pkey(\dec)),
            \feedback, ~pmodenv.(Pseq([0.2, 0.4, 0.8],inf), Pseq([4, 8, 2, 4],inf)),
    
            \time, ~pmodenv.(Pseq([0.005, 0.001, 0.010],inf) * 1 * Pkey(\groupdelta), Pkey(\dec)),
            \damp, ~pmodenv.(Pseq([1, 0],inf), Pwhite(0.01, 0.4, inf)),
            // \damp, ~pmodenv.(1 - Pkey(\groupdelta) * 1, Pkey(\dec)),
    
            \filter, ~pmodenv.(Pseq([500, 20000],inf), Pseq([1, 1, 2], inf)),
    
            \delay2, ~pmodenv.(Pseq([0, 1, 0],inf), Pseq([0.5, 0.25], inf)),
    
            \rev, ~pmodenv.(Pexprand(0.1, 10, inf), Pseq([2, 4], inf)),
            // \density, 1.0,
            // \bias, 10000.0,
            // \pan, ~pmodenv.(Pwhite(-0.8, 0.8, inf), Pseq([0.5, 0.25],inf)),
        )
    );

    sp.par(
        Pbindf(
            Pdef(\fb2Seq,
                Pdef(\fb2Mod)
                // <> ~filterBeat.(key: Pkey(\eventcount), beat:[1, 4], mod: 6)
                // <> ~pSkew.(Pdef(\p2), key: Pkey(\eventcount), group: [1, 3], skew: [0.5], curve: \exp)
                // <> ~pSkew.(Pdef(\p2), key: Pkey(\cyclecount), group: [1, 2], skew: [-0.5, 0.5], curve: \exp)
                <> Pdef(\p2)
                // <> Pbind(\instrument, \fb2)
            ),
            \out, [~mainout, ~convolve_B, ~fb2_out]
        ).finDur(sectionLength)
    );

    sp.par(
        Pdef(\drum, 
            Pbind(
                // \fmPerc2,
                \freq, Pseq([40, 60, 40] * 1, inf).stutter(3),
                \atk, 0.04,
                \dec, Pkey(\groupdelta).lincurve(0, 1, 0.2, 0.1),
                // \dec, 0.5,
                // \rel, 0.01,
                \fb, Pseq([0, 2, 0], inf).stutter(2),
                \pulseWidth, Pkey(\groupdelta) + 0.25,
                \index, (Pkey(\groupdelta).lincurve(1, 0, 3, 4) * 0.4).stutter(2),
                \ratio, 2,
                \sweep, 8.0,
                \spread, 20,
                \noise, Pseq([0, 2, 0], inf).stutter(2),
                // \drive, 0,
                \feedback, -2,
                \feedback, ((1-Pkey(\groupdelta)).linlin(0,1,3,4)).stutter(2),
                \fbmod, 0,
                \lofreq, 500.0,
                \lodb, 10.0,
                \midfreq, 1200.0,
                \middb, -12.0,
                \hifreq, 7000.0,
                \hidb, 10.0,
                \gain, -12.0,
                \pan, 0.0,
                // \amp, 1,
                \amp, Pkey(\groupdelta).lincurve(0, 1, 1, 0.1),
                \out, [~mainout, ~convolve_A, ~drum_out]
            )
            <> ~filterBeat.(key: Pkey(\eventcount), beat:[1, 2, 4])
            // <> ~pSkew.(Pdef(\p2), key: Pkey(\eventcount), group: [1, 3], skew: [-0.5], curve: \exp)
            // <> ~pSkew.(Pdef(\p2), key: Pkey(\cyclecount), group: [1, 2], skew: [-0.25, 0.5], curve: \exp)
            <> Pdef(\p2)
            <> Pbind(\instrument, \fmPerc2)
        ).finDur(sectionLength)
    );

    Pdef(\p3,
        ~makeSubdivision.(
            PlaceAll([1, 1, 1, 1], inf),
            PlaceAll([4, 4, 4, 4], inf)
        )
    );

    sp.par(
        Pdef(\breakSliced,
            Pbind(
                \amp, 1,
                \atk, 0.01,
                \rel, 0.125,
                \buf, ~breakBuf.at(\file),
                \rate, 1,
                \oneshot, 1,
                \gain, -3,
                \sliceStart, 3,
                \slice, ~pGetSlice.(Pseries(1, 1, inf).wrap(0, 32) + Pkey(\sliceStart), ~breakBuf),
                \pitchMix, 0.9,
                \pitchRatio, 2,
                \windowSize, 0.01,
                \pitchDispersion, 1.0,
                \timeDispersion, 0.1,

                \out, [~mainout, ~miVerb, ~breakSliced_out],
                
            )
            // <> Pbind(\timingOffset, Pkey(\groupcount) / Pkey(\groupdelta))
            // <> ~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[1, 3])
            // <> ~filterBeat.(key: Pkey(\eventcount), beat:[1,3,4])
            <> ~pSkew.(Pdef(\p3), key: Pkey(\eventcount), group: [1, 2], skew: [0.75], curve: \exp)
            // <>Pbind(\amp, Pseq([0.1, 0.7], inf))
            <> Pdef(\p3)
            <> Pbind(\instrument, \segPlayer)
        ).finDur(sectionLength)
    );

    sp.par(
        Pbindf(
            Pdef(\morph),
            \gain, -12,
            \atk, 0,
            \rel, 100,
            // \swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([1/2], inf), 1, \sin),
            // \swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([1/3], inf), 1, \sin),
            // \swap, ~pmodenv.(Pseq([1, 0],inf), Pseq([4], inf), 1, \linear),
            // \swap, 0,
            // \swap, 1,
            \out, [~mainout, ~vocoder_out]
        ).finDur(sectionLength)
    );

    sp.wait(sectionLength);
    ///////////////////////
    \f.postln;
    sectionLength = 80;

    sp.par(
        Pmono(\driftingSines_mono2,
            \lfoFreq, 0.001,
            \freq, 60 * [4, 7, 11].midiratio,
            \pitchDev, [2, 1.5],
            \numHarm, 4,
            \gain, 0.0,
            \amp, 1.0,
            \out, [~sines_out, ~miVerb]
            // \out, [~convolve_B]
        ).finDur(sectionLength)
    );

    sp.par(
        Pmono(\grainSlicer_mono,
            \amp, 1,
            \buf, ~birdBuf.at(\file),
            \overlap, 100,
            \trigRate, Pseg(Pseq([10, 1000,], inf), 4, 'exp' , inf),
            \slice, ~pGetSlice.(30, ~birdBuf),
            \gain, 6,
            \posRate, 0.1,
            \rate, 0.5,
            \out, [~convolve_B, ~miVerb]
        ).finDur(sectionLength)
    );

    Pdef(\p2,
        ~makeSubdivision.(
            PlaceAll([1.5, 1.5, 1, 2, 2], inf),
            PlaceAll([2, 4, 2, 4], inf)
        )
    );

    Pdef(\fb2Mod,
        PmonoArtic(\fb2,
            \amp, 0.05,
            \gain, 0,
            \buf, Pseq([b, d], inf).stutter(3),
            // \buf, c,
            // \window, 4096 * 2,
            \impulse, 5.0,
            // \impulse, ~pmodenv.(Pexprand(5, 25, inf), Pseq([4,4],inf)),
    
            \window, Pseq([4096, 1024, 512] * 0.5, inf).stutter(3),
            \atk, 0.1,
            \dec, 0.2,
            // \dec, Pkey(\dur),
            \sustainTime, Pkey(\dur),
            // \exciter, 0.6,
            // \density, ~pmodenv.(1 - Pkey(\groupdelta), Pkey(\dec)),
            \exciter, ~pmodenv.(1 - Pkey(\groupdelta), Pkey(\dec)),
            \feedback, ~pmodenv.(Pseq([0.2, 0.4, 0.8],inf), Pseq([4, 8, 2, 4],inf)),
    
            \time, ~pmodenv.(Pseq([0.005, 0.001, 0.010],inf) * 1 * Pkey(\groupdelta), Pkey(\dec)),
            \damp, ~pmodenv.(Pseq([1, 0],inf), Pwhite(0.01, 0.4, inf)),
            // \damp, ~pmodenv.(1 - Pkey(\groupdelta) * 1, Pkey(\dec)),
    
            \filter, ~pmodenv.(Pseq([500, 20000],inf), Pseq([1, 1, 2], inf)),
    
            \delay2, ~pmodenv.(Pseq([0, 1, 0],inf), Pseq([0.5, 0.25], inf)),
    
            \rev, ~pmodenv.(Pexprand(0.1, 10, inf), Pseq([2, 4], inf)),
            // \density, 1.0,
            // \bias, 10000.0,
            // \pan, ~pmodenv.(Pwhite(-0.8, 0.8, inf), Pseq([0.5, 0.25],inf)),
        )
    );

    sp.par(
        Pbindf(
            Pdef(\fb2Seq,
                Pdef(\fb2Mod)
                // <> ~filterBeat.(key: Pkey(\eventcount), beat:[1, 4], mod: 6)
                // <> ~pSkew.(Pdef(\p2), key: Pkey(\eventcount), group: [1, 3], skew: [0.5], curve: \exp)
                // <> ~pSkew.(Pdef(\p2), key: Pkey(\cyclecount), group: [1, 2], skew: [-0.5, 0.5], curve: \exp)
                <> Pdef(\p2)
                // <> Pbind(\instrument, \fb2)
            ),
            \out, [~mainout, ~convolve_B, ~fb2_out]
        ).finDur(sectionLength)
    );

    sp.par(
        Pdef(\drum, 
            Pbind(
                // \fmPerc2,
                \freq, Pseq([40, 60, 40] * 1, inf).stutter(3),
                \atk, 0.04,
                \dec, Pkey(\groupdelta).lincurve(0, 1, 0.2, 0.1),
                // \dec, 0.5,
                // \rel, 0.01,
                \fb, Pseq([0, 2, 0], inf).stutter(2),
                \pulseWidth, Pkey(\groupdelta) + 0.25,
                \index, (Pkey(\groupdelta).lincurve(1, 0, 3, 4) * 0.4).stutter(2),
                \ratio, 2,
                \sweep, 8.0,
                \spread, 20,
                \noise, Pseq([0, 2, 0], inf).stutter(2),
                // \drive, 0,
                \feedback, -2,
                \feedback, ((1-Pkey(\groupdelta)).linlin(0,1,3,4)).stutter(2),
                \fbmod, 0,
                \lofreq, 500.0,
                \lodb, 10.0,
                \midfreq, 1200.0,
                \middb, -12.0,
                \hifreq, 7000.0,
                \hidb, 10.0,
                \gain, -12.0,
                \pan, 0.0,
                // \amp, 1,
                \amp, Pkey(\groupdelta).lincurve(0, 1, 1, 0.1),
                \out, [~mainout, ~convolve_A, ~drum_out]
            )
            // <> ~filterBeat.(key: Pkey(\eventcount), beat:[1, 2, 4])
            // <> ~pSkew.(Pdef(\p2), key: Pkey(\eventcount), group: [1, 3], skew: [-0.5], curve: \exp)
            // <> ~pSkew.(Pdef(\p2), key: Pkey(\cyclecount), group: [1, 2], skew: [-0.25, 0.5], curve: \exp)
            <> Pdef(\p2)
            <> Pbind(\instrument, \fmPerc2)
        ).finDur(sectionLength)
    );

    Pdef(\p3,
        ~makeSubdivision.(
            PlaceAll([1, 1, 1, 0.5, 0.5], inf),
            PlaceAll([4, 4, 4, 4], inf)
        )
    );

    sp.par(
        Pdef(\breakSliced,
            Pbind(
                \amp, 1,
                \atk, 0.01,
                \rel, 0.115,
                \buf, ~breakBuf.at(\file),
                \rate, 2,
                \oneshot, 1,
                \gain, -3,
                \sliceStart, 3,
                \slice, ~pGetSlice.(Pseries(1, 1, inf).wrap(0, 32) + Pkey(\sliceStart), ~breakBuf).stutter(Pseq([1,1,1,3], inf)),
                \pitchMix, 0.9,
                \pitchRatio, 1,
                \windowSize, 0.01,
                \pitchDispersion, 1.0,
                \timeDispersion, 0.1,
                \out, [~mainout, ~miVerb, ~breakSliced_out],
                
            )
            // <> Pbind(\timingOffset, Pkey(\groupcount) / Pkey(\groupdelta))
            // <> ~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[2, 3, 4])
            // <> ~filterBeat.(key: Pkey(\eventcount), beat:[1,3,4])
            <> ~pSkew.(Pdef(\p3), key: Pkey(\eventcount), group: [1, 2], skew: [0.75], curve: \exp)
            // <>Pbind(\amp, Pseq([0.1, 0.7], inf))
            <> Pdef(\p3)
            <> Pbind(\instrument, \segPlayer)
        ).finDur(sectionLength)
    );

    sp.par(
        Pbindf(
            Pdef(\morph),
            \gain, -12,
            \atk, 0,
            \rel, 100,
            \swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([1/2], inf), 1, \sin),
            // \swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([1/3], inf), 1, \sin),
            // \swap, ~pmodenv.(Pseq([1, 0],inf), Pseq([4], inf), 1, \linear),
            // \swap, 0,
            // \swap, 1,
            \out, [~mainout, ~vocoder_out]
        ).finDur(sectionLength)
    );

    sp.wait(sectionLength);
    /////////////////////////
    \g.postln;
    sectionLength = 94+16;

    sp.par(
        Pmono(\driftingSines_mono2,
            \lfoFreq, 0.001,
            \freq, 60 * 18.midiratio,
            \pitchDev, 13,
            \numHarm, 12,
            \gain, -18,
            \amp, 1.0,
            \out, [~sines_out, ~miVerb]
            // \out, [~convolve_B]
        ).finDur(sectionLength)
    );

    sp.par(
        Pmono(\driftingSines_mono2,
            \lfoFreq, 0.001,
            \freq, 60 * [0, 7, 11].midiratio,
            \pitchDev, [2, 1.5] * 2.5,
            \numHarm, 4,
            \gain, 0.0,
            \amp, 1.0,
            \out, [~sines2_out, ~miVerb]
            // \out, [~convolve_B]
        ).finDur(sectionLength)
    );

    sp.par(
        Pmono(\grainSlicer_mono,
            \amp, 1,
            \buf, ~birdBuf.at(\file),
            \overlap, 100,
            \trigRate, Pseg(Pseq([10, 1000,], inf), 4, 'exp' , inf),
            \slice, ~pGetSlice.(30, ~birdBuf),
            \gain, 6,
            \posRate, 0.1,
            \rate, 0.5,
            \out, [~convolve_B, ~miVerb]
        ).finDur(sectionLength)
    );

    Pdef(\p2,
        ~makeSubdivision.(
            PlaceAll([1, 1, 1, 0.5, 0.5, 1.5, 1.5, 0.5, 0.5], inf),
            PlaceAll([2, 2, 2, 0, 1], inf)
        )
    );

    Pdef(\fb2Mod,
        PmonoArtic(\fb2,
            \amp, 0.05,
            \gain, 0,
            \buf, Pseq([b, d], inf).stutter(3),
            // \buf, c,
            // \window, 4096 * 2,
            \window, 512,
            \impulse, 5.0,
            // \impulse, ~pmodenv.(Pexprand(5, 25, inf), Pseq([4,4],inf)),
    
            // \window, Pseq([4096, 1024, 512] * 0.5, inf).stutter(3),
            \atk, 0.1,
            // \dec, 0.2,
            \dec, Pkey(\dur),
            \sustainTime, Pkey(\dur),
            // \exciter, 0.6,
            // \density, ~pmodenv.(1 - Pkey(\groupdelta), Pkey(\dec)),
            \exciter, ~pmodenv.(1 - Pkey(\groupdelta), Pkey(\dec)),
            \feedback, ~pmodenv.(Pseq([0.2, 0.4, 0.8],inf), Pseq([4, 8, 2, 4],inf)),
    
            \time, ~pmodenv.(Pseq([0.005, 0.001, 0.010],inf) * 1 * Pkey(\groupdelta), Pkey(\dec)),
            // \damp, ~pmodenv.(Pseq([1, 0],inf), Pwhite(0.01, 0.4, inf)),
            // \damp, ~pmodenv.(1 - Pkey(\groupdelta) * 1, Pkey(\dec)),
    
            \filter, ~pmodenv.(Pseq([500, 20000],inf), Pseq([1, 1, 2], inf)),
    
            \delay2, ~pmodenv.(Pseq([0, 1, 0],inf), Pseq([0.5, 0.25], inf)),
    
            \rev, ~pmodenv.(Pexprand(0.1, 10, inf), Pseq([2, 4], inf)),
            // \density, 1.0,
            // \bias, 10000.0,
            // \pan, ~pmodenv.(Pwhite(-0.8, 0.8, inf), Pseq([0.5, 0.25],inf)),
        )
    );

    sp.par(
        Pbindf(
            Pdef(\fb2Seq,
                Pdef(\fb2Mod)
                <> ~filterBeat.(key: Pkey(\eventcount), beat:[1, 4], mod: 6)
                // <> ~pSkew.(Pdef(\p2), key: Pkey(\eventcount), group: [1, 3], skew: [0.5], curve: \exp)
                // <> ~pSkew.(Pdef(\p2), key: Pkey(\cyclecount), group: [1, 2], skew: [-0.5, 0.5], curve: \exp)
                <> Pdef(\p2)
                // <> Pbind(\instrument, \fb2)
            ),
            \out, [~mainout, ~convolve_B, ~fb2_out]
        ).finDur(sectionLength)
    );

    sp.par(
        Pdef(\drum, 
            Pbind(
                // \fmPerc2,
                \freq, Pseq([40, 60, 40] * 1, inf).stutter(3),
                \atk, 0.04,
                \dec, Pkey(\groupdelta).lincurve(0, 1, 0.2, 0.1),
                // \dec, 0.5,
                // \rel, 0.01,
                \fb, Pseq([0, 2, 0], inf).stutter(4),
                \pulseWidth, Pkey(\groupdelta) + 0.25,
                \index, (Pkey(\groupdelta).lincurve(1, 0, 3, 4) * 0.4).stutter(4),
                \ratio, 2,
                \sweep, 8.0,
                \spread, 20,
                \noise, Pseq([0, 2, 0], inf).stutter(2),
                // \drive, 0,
                \feedback, -2,
                \feedback, ((1-Pkey(\groupdelta)).linlin(0,1,3,4)).stutter(4),
                \fbmod, 0,
                \lofreq, 500.0,
                \lodb, 10.0,
                \midfreq, 1200.0,
                \middb, -12.0,
                \hifreq, 7000.0,
                \hidb, 10.0,
                \gain, -12.0,
                \pan, 0.0,
                // \amp, 1,
                \amp, Pkey(\groupdelta).lincurve(0, 1, 1, 0.1),
                \out, [~mainout, ~drum_out]
            )
            // <> ~filterBeat.(key: Pkey(\eventcount), beat:[1, 2, 4])
            // <> ~pSkew.(Pdef(\p2), key: Pkey(\eventcount), group: [1, 3], skew: [-0.5], curve: \exp)
            // <> ~pSkew.(Pdef(\p2), key: Pkey(\cyclecount), group: [1, 2], skew: [-0.25, 0.5], curve: \exp)
            <> Pdef(\p2)
            <> Pbind(\instrument, \fmPerc2)
        ).finDur(sectionLength)
    );

    Pdef(\p3,
        ~makeSubdivision.(
            PlaceAll([1, 1, 1, 1], inf),
            PlaceAll([4, 4, 4, 4], inf)
        )
    );

    sp.par(
        Pdef(\breakSliced,
            Pbind(
                \amp, 1,
                \atk, 0.01,
                \rel, 0.115,
                \buf, ~breakBuf.at(\file),
                \rate, 1,
                \oneshot, 1,
                \gain, 0,
                \sliceStart, 5,
                \slice, ~pGetSlice.(Pseries(1, 1, inf).wrap(0, 32) + Pkey(\sliceStart), ~breakBuf),
                \pitchMix, 0.9,
                \pitchRatio, 2,
                \windowSize, 0.01,
                \pitchDispersion, 1.0,
                \timeDispersion, 0.1,
                \out, [~mainout, ~miVerb, ~breakSliced_out],
                
            )
            // <> Pbind(\timingOffset, Pkey(\groupcount) / Pkey(\groupdelta))
            // <> ~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[2, 3, 4])
            // <> ~filterBeat.(key: Pkey(\eventcount), beat:[1,3,4])
            <> ~pSkew.(Pdef(\p3), key: Pkey(\eventcount), group: [1, 2], skew: [0.75], curve: \exp)
            // <>Pbind(\amp, Pseq([0.1, 0.7], inf))
            <> Pdef(\p3)
            <> Pbind(\instrument, \segPlayer)
        ).finDur(sectionLength)
    );

    sp.par(
        Pbind(
            \instrument, \rongsinator,
            \dur, 3,
            \pan, 0.0,
            \sustainTime, 0.02,
            \f0, 240 * 4 * 19.midiratio,
            \structure, 1,
            \brightness, 0.2,
            \damping, 0.4,
            \accent, 0.9,
            \harmonicstretch, 0.5,
            \position, 0.15,
            \loss, 0.15,
            \atk, 0.25,
            \rel, 2,
            // \gate, 1.0,
            \gain, -18,
            \amp, 1.0,
            \out, [~miVerb, ~convolve_B],
        ).finDur(sectionLength)
    );

    sp.par(
        Pbindf(
            Pdef(\morph),
            \gain, -12,
            \atk, 0,
            \rel, 100,
            // \swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([1/2], inf), 1, \sin),
            // \swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([1/3], inf), 1, \sin),
            \swap, ~pmodenv.(Pseq([1, 0],inf), Pseq([4], inf), 1, \linear),
            // \swap, 0,
            // \swap, 1,
            \out, [~mainout, ~vocoder_out]
        ).finDur(sectionLength)
    );
    
    sp.wait(sectionLength);
    ///////////////////////////
    \h.postln;
    sectionLength = 256;

    sp.par(
        Pbindf(
            Pdef(\tape),
            \pregain, 0.0,
            \dcOffset, 0.0,
            \sigmoid, ~pmodenv.(Pseq([0.1, 1],inf), Pseq([32], inf), inf, \linear),
            \postgain, -12,
            // \autogain, 1.0,
            \drywet, 1,
            \amp, 1.0,
            \out, [~mainout, ~miVerb, ~tape_out]
        )
    );

    sp.par(
        Pmono(\driftingSines_mono2,
            \lfoFreq, 0.001,
            \freq, 60 * 18.midiratio,
            \pitchDev, 13,
            \numHarm, 12,
            \gain, -18,
            \amp, 1.0,
            \out, [~tape]
            // \out, [~convolve_B]
        )
    );

    sp.par(
        Pmono(\driftingSines_mono2,
            \lfoFreq, 0.001,
            \freq, 60 * [0, 7, 11].midiratio,
            \pitchDev, [2, 1.5],
            \numHarm, 6,
            \gain, 0.0,
            \amp, 1.0,
            \out, ~tape
            // \out, [~convolve_B]
        )
    );

    sp.par(
        Pmono(\grainSlicer_mono,
            \amp, 1,
            \buf, ~birdBuf.at(\file),
            \overlap, 100,
            \trigRate, Pseg(Pseq([10, 1000,], inf), 4, 'exp' , inf),
            \slice, ~pGetSlice.(30, ~birdBuf),
            \gain, 6,
            \posRate, 0.1,
            \rate, 0.5,
            \out, [~mainout, ~miVerb, ~grainSlicer_out]
        )
    );

    sp.par(
        PfadeOut(
            Pbind(
                \instrument, \rongsinator,
                \dur, 3,
                \pan, 0.0,
                \sustainTime, 0.02,
                \f0, 240 * 4 * 19.midiratio,
                \structure, 1,
                \brightness, 0.2,
                \damping, 0.4,
                \accent, 0.9,
                \harmonicstretch, 0.5,
                \position, 0.15,
                \loss, 0.15,
                \atk, 0.25,
                \rel, 2,
                // \gate, 1.0,
                \gain, -18,
                \amp, 1.0,
                \out, [~miVerb],
            ), 64
        )
    );
})
).play(t);
)

