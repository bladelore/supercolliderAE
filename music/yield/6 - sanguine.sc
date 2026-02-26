Pdef.clear;

(
    ~sampleBuf = Dictionary();
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Field recs/Candle wrappers.wav", ~sampleBuf, 0.3, \centroid);
)

(
    ~recorders = ~recordBuses.value(
        ~busArr,
        Platform.recordingsDir +/+ "sanguine/%.wav"
    );
)

(
    ~busArr = [
        ~reverb_out=Bus.audio(s,2),
        ~vocoder_out=Bus.audio(s,2),
        ~tape_out=Bus.audio(s,2),
        ~grainslicer_out=Bus.audio(s,2),
    ]
)

(
t = TempoClock.new(95/60).permanent_(true).schedAbs(0, {t.beatsPerBar_(4)});

Pdef(\player,
Pspawner({| sp |
    var sectionLength;

    //fx const
    sp.par(
        Pbindf(
            Pdef(\miVerb),
            \time, 1,
            \damp, 0.99,
            \hp, 0.125,
            \freeze, 0,
            \diff, 0.9,
            \gain, 0,
            \out, [~mainout, ~reverb_out]
        )
    );

    Pdef(\aMod,
        Pbind(
            \scale, Scale.minor.tuning_(\just),
            // \scale, Scale.minor,
            \root, -4, 
            // \mtranspose, 1,
            \degree, (Pkey(\eventcount).stutter(3) - Pkey(\groupcount).stutter(3)),
            // \degree, (Pkey(\eventcount).stutter(2) - (Pkey(\groupcount).stutter(3))),
            // \degree, (Pkey(\eventcount).stutter(3) - Pkey(\groupcount).stutter(2)),
            \octave, Pkey(\groupcount).wrap(1,3) + 3,
            \detune, 0.5,
            \envDepth, 100,

            \atk, 0.01,
            \dec, 0.4,
            \sus, 0.1,
            \rel, 4,
            
            \filterAtk, 0.01,
            \filterDec, 1,
            \pitchEnv, 0,

            \driftRate, 2.0,
            \drift, 1.0,
            \lfoShape, 1,
            \lfoFreq, 1,

            \lfoToWidth, 0.5,
            \lfoToShapeAmount, 1,
            \lfoToFilter, 1,
            \lfoToMorph, 0.5,

            \filter, 25.0,
            \filter2, 5000.0,
            \width, 1,
            \shape, 0,
            \morph, 0.5,
            \gain, -6.0,
            \amp, 1.0,
            \pan, ~pmodenv.(Pwhite(-0.25, 0.25), Pkey(\dur), 1, \sin),
            // pan, -1,
            \out, [~tape]
        )
    );

    Pdef(\bMod,
        Pbind(
            \scale, Scale.minor.tuning_(\just),
            // \scale, Scale.minor,
            \root, -4,
            // \mtranspose, -2,
            \degree, (Pkey(\eventcount).stutter(3) - Pkey(\groupcount).stutter(3)) + 4,
            // \degree, (Pkey(\eventcount).stutter(2) - (Pkey(\groupcount).stutter(3))) + 4;,
            // \degree, (Pkey(\eventcount).stutter(3) - Pkey(\groupcount).stutter(2)) + 4,
            \octave, Pkey(\groupcount).wrap(1, 3) + 3,
            \detune, 0.5,
            \envDepth, 0,

            \atk, 0.01,
            \dec, 0.4,
            \sus, 0.1,
            \rel, 4,

            \filterAtk, 0.01,
            \filterDec, 0.1,

            \driftRate, 2.0,
            \drift, 1.0,
            \lfoShape, 1,
            \lfoFreq, 1,

            \lfoToWidth, 1.0,
            \lfoToShapeAmount, 1.0,
            \lfoToFilter, 1,
            \lfoToMorph, 1.0,

            \filter, 25.0,
            \filter2, 5000.0,
            \width, 0.5,
            \shape, 1,
            \morph, 0.5,
            \gain, -6.0,
            \amp, 1.0,
            \out, 0.0,
            // \pan, ~pmodenv.(Pwhite(-0.8, 0.8), Pkey(\dur), 1, \sin),
            // \pan,
            \out, [~tape, ~convolve_A]
        )
    );
    // ///////////////////////////
    sectionLength = 0;
    \a.postln;

    sp.par(
        Pbindf(
            Pdef(\tape),
            \pregain, 0.0,
            \dcOffset, 0.1,
            // \dcOffset, 0.,
            \sigmoid, 0.1,
            \postgain, -6,
            // \autogain, 1.0,
            \drywet, 1,
            \amp, 1.0,
            \out, [~convolve_B]
        ).finDur(sectionLength)
    );

    sp.par(
        Pmono(\grainSlicer_mono,
            \amp, 1,
            \buf, ~sampleBuf.at(\file),
            \overlap, 100,
            \trigRate, Pseg(Pseq([10, 1000,], inf), 4, 'exp' , inf),
            \slice, ~pGetSlice.(1, ~sampleBuf),
            \gain, -6,
            \posRate, 1,
            \rate, 0.75,
            \out, [~convolve_A, ~mainout, ~grainslicer_out]
        ).finDur(sectionLength)
    );

    Pdef(\p1,
        ~makeSubdivision.(
            PlaceAll([Prand([1.5, 1], inf), 2, 6], inf),
            PlaceAll([2, [4, 2], 0], inf)
        )
    );

    // Pdef(\p1,
    //     ~makeSubdivision.(
    //         PlaceAll([Prand([1.5, 1], inf), 2, 1, 1], inf),
    //         PlaceAll([2, [4, 2], 1, 2, 2], inf)
    //     )
    // );

    sp.par(
        Pdef(\a, 
            Pdef(\aMod)
            // <> ~filterBeat.(key: Pkey(\eventcount), beat:[1, 3, 4])
            <> ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [1, 3], skew: [-0.5], curve: \exp)
            <> Pdef(\p1)
            <> Pbind(\instrument, \pulsePluck)
        ).finDur(sectionLength)
    );

    Pdef(\p2,
        ~makeSubdivision.(
            PlaceAll([Prand([1.5, 1], inf), 2, 6] * 0.5, inf),
            PlaceAll([2, [4, 2], 0], inf)
        )
    );

    // Pdef(\p2,
    //     ~makeSubdivision.(
    //         PlaceAll([Prand([1.5, 1], inf), 2, 1] * 0.5, inf),
    //         PlaceAll([2, [4, 2], 1], inf)
    //     )
    // );
    
    // sp.par(
    //     Pdef(\b, 
    //         Pdef(\bMod)
    //         // <> ~filterBeat.(key: Pkey(\eventcount), beat:[1, 4])
    //         <> ~pSkew.(Pdef(\p2), key: Pkey(\eventcount), group: [1, 3], skew: [-0.5], curve: \exp)
    //         <> Pdef(\p2)
    //         <> Pbind(\instrument, \pulsePluck)
    //     ).finDur(sectionLength)
    // );

    sp.par(
        Pbindf(
            Pdef(\morph),
            \gain, 0,
            \atk, 10,
            \rel, 100,
            // \swap, ~pmodenv.(Pseq([1, 0],inf), Pseq([1], inf), 1, \sin),
            // \swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([1/3], inf), 1, \sin),
            // \swap, ~pmodenv.(Pseq([0.1, 0.5],inf), Pseq([4], inf), 1, \sin),
            // \swap, 0,
            \swap, 1,
            \out, [~mainout, ~miVerb, ~vocoder_out]
        ).finDur(sectionLength)
    );

    sp.wait(sectionLength);
    ///////////////////////////
    \b.postln;
    sectionLength = 36;

    sp.par(
        Pbindf(
            Pdef(\tape),
            \pregain, 0.0,
            \dcOffset, 0.1,
            // \dcOffset, 0.,
            \sigmoid, 0.1,
            \postgain, -6,
            // \autogain, 1.0,
            \drywet, 0.5,
            \amp, 1.0,
            \out, [~convolve_B]
        ).finDur(sectionLength)
    );

    sp.par(
        Pmono(\grainSlicer_mono,
            \amp, 1,
            \buf, ~sampleBuf.at(\file),
            \overlap, 100,
            \trigRate, Pseg(Pseq([10, 1000,], inf), 4, 'exp' , inf),
            \slice, ~pGetSlice.(1, ~sampleBuf),
            \gain, -6,
            \posRate, 1,
            \rate, 0.75,
            \out, [~convolve_A, ~mainout, ~grainslicer_out]
        ).finDur(sectionLength)
    );

    Pdef(\p1,
        ~makeSubdivision.(
            PlaceAll([Prand([1.5, 1], inf), 2, 6], inf),
            PlaceAll([2, [4, 2], 0], inf)
        )
    );

    // Pdef(\p1,
    //     ~makeSubdivision.(
    //         PlaceAll([Prand([1.5, 1], inf), 2, 1, 1], inf),
    //         PlaceAll([2, [4, 2], 1, 2, 2], inf)
    //     )
    // );

    sp.par(
        Pdef(\a, 
            Pdef(\aMod)
            // <> ~filterBeat.(key: Pkey(\eventcount), beat:[1, 3, 4])
            <> ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [1, 3], skew: [-0.5], curve: \exp)
            <> Pdef(\p1)
            <> Pbind(\instrument, \pulsePluck)
        ).finDur(sectionLength)
    );

    Pdef(\p2,
        ~makeSubdivision.(
            PlaceAll([Prand([1.5, 1], inf), 2, 6] * 0.5, inf),
            PlaceAll([2, [4, 2], 0], inf)
        )
    );

    // Pdef(\p2,
    //     ~makeSubdivision.(
    //         PlaceAll([Prand([1.5, 1], inf), 2, 1] * 0.5, inf),
    //         PlaceAll([2, [4, 2], 1], inf)
    //     )
    // );
    
    sp.par(
        Pdef(\b, 
            Pdef(\bMod)
            // <> ~filterBeat.(key: Pkey(\eventcount), beat:[1, 4])
            <> ~pSkew.(Pdef(\p2), key: Pkey(\eventcount), group: [1, 3], skew: [-0.5], curve: \exp)
            <> Pdef(\p2)
            <> Pbind(\instrument, \pulsePluck)
        ).finDur(sectionLength)
    );

    sp.par(
        Pbindf(
            Pdef(\morph),
            \gain, 0,
            \atk, 10,
            \rel, 100,
            // \swap, ~pmodenv.(Pseq([1, 0],inf), Pseq([1], inf), 1, \sin),
            // \swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([1/3], inf), 1, \sin),
            // \swap, ~pmodenv.(Pseq([0.1, 0.5],inf), Pseq([4], inf), 1, \sin),
            // \swap, 0,
            // \swap, 1,
            \out, [~mainout, ~miVerb, ~vocoder_out]
        ).finDur(sectionLength)
    );
    sp.wait(sectionLength);
    /////////////////////////////
    \c.postln;
    sectionLength = 36;

    sp.par(
        Pbindf(
            Pdef(\tape),
            \pregain, 0.0,
            \dcOffset, 0.1,
            // \dcOffset, 0.,
            \sigmoid, 0.1,
            \postgain, -6,
            // \autogain, 1.0,
            \drywet, 1,
            \amp, 1.0,
            \out, [~convolve_B]
        ).finDur(sectionLength)
    );

    sp.par(
        Pmono(\grainSlicer_mono,
            \amp, 1,
            \buf, ~sampleBuf.at(\file),
            \overlap, 100,
            \trigRate, Pseg(Pseq([10, 1000,], inf), 4, 'exp' , inf),
            \slice, ~pGetSlice.(30, ~sampleBuf),
            \gain, -6,
            \posRate, 1,
            \rate, 0.75,
            \out, [~convolve_A, ~mainout, ~grainslicer_out]
        ).finDur(sectionLength)
    );

    Pdef(\p1,
        ~makeSubdivision.(
            PlaceAll([Prand([1.5, 1], inf), 2, 6], inf),
            PlaceAll([2, [4, 2], 0], inf)
        )
    );

    // Pdef(\p1,
    //     ~makeSubdivision.(
    //         PlaceAll([Prand([1.5, 1], inf), 2, 1, 1], inf),
    //         PlaceAll([2, [4, 2], 1, 2, 2], inf)
    //     )
    // );

    sp.par(
        Pdef(\a, 
            Pdef(\aMod)
            // <> ~filterBeat.(key: Pkey(\eventcount), beat:[1, 3, 4])
            <> ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [1, 3], skew: [-0.5], curve: \exp)
            <> Pdef(\p1)
            <> Pbind(\instrument, \pulsePluck)
        ).finDur(sectionLength)
    );

    Pdef(\p2,
        ~makeSubdivision.(
            PlaceAll([Prand([1.5, 1], inf), 2, 6] * 0.5, inf),
            PlaceAll([2, [4, 2], 0], inf)
        )
    );

    // Pdef(\p2,
    //     ~makeSubdivision.(
    //         PlaceAll([Prand([1.5, 1], inf), 2, 1] * 0.5, inf),
    //         PlaceAll([2, [4, 2], 1], inf)
    //     )
    // );
    
    sp.par(
        Pdef(\b, 
            Pdef(\bMod)
            // <> ~filterBeat.(key: Pkey(\eventcount), beat:[1, 4])
            <> ~pSkew.(Pdef(\p2), key: Pkey(\eventcount), group: [1, 3], skew: [-0.5], curve: \exp)
            <> Pdef(\p2)
            <> Pbind(\instrument, \pulsePluck)
        ).finDur(sectionLength)
    );

    sp.par(
        Pbindf(
            Pdef(\morph),
            \gain, 0,
            \atk, 10,
            \rel, 100,
            // \swap, ~pmodenv.(Pseq([1, 0],inf), Pseq([1], inf), 1, \sin),
            // \swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([1/3], inf), 1, \sin),
            // \swap, ~pmodenv.(Pseq([0.1, 0.5],inf), Pseq([4], inf), 1, \sin),
            // \swap, 0,
            // \swap, 0,
            \out, [~mainout, ~miVerb, ~vocoder_out]
        ).finDur(sectionLength)
    );
    sp.wait(sectionLength);

    ///////////////////////////
    \d.postln;
    sectionLength = 36;

    sp.par(
        Pbindf(
            Pdef(\tape),
            \pregain, 0.0,
            \dcOffset, 0.1,
            // \dcOffset, 0.,
            \sigmoid, 0.1,
            \postgain, -6,
            // \autogain, 1.0,
            \drywet, 1,
            \amp, 1.0,
            \out, [~convolve_B]
        ).finDur(sectionLength)
    );

    sp.par(
        Pmono(\grainSlicer_mono,
            \amp, 1,
            \buf, ~sampleBuf.at(\file),
            \overlap, 100,
            \trigRate, Pseg(Pseq([10, 1000,], inf), 4, 'exp' , inf),
            \slice, ~pGetSlice.(30, ~sampleBuf),
            \gain, -6,
            \posRate, 1,
            \rate, 0.75,
            \out, [~convolve_A, ~mainout, ~grainslicer_out]
        ).finDur(sectionLength)
    );

    Pdef(\p1,
        ~makeSubdivision.(
            PlaceAll([Prand([1.5, 1], inf), 2, 6], inf),
            PlaceAll([2, [4, 2], 0], inf)
        )
    );

    // Pdef(\p1,
    //     ~makeSubdivision.(
    //         PlaceAll([Prand([1.5, 1], inf), 2, 1, 1], inf),
    //         PlaceAll([2, [4, 2], 1, 2, 2], inf)
    //     )
    // );

    sp.par(
        Pdef(\a, 
            Pdef(\aMod)
            // <> ~filterBeat.(key: Pkey(\eventcount), beat:[1, 3, 4])
            <> ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [1, 3], skew: [-0.5], curve: \exp)
            <> Pdef(\p1)
            <> Pbind(\instrument, \pulsePluck)
        ).finDur(sectionLength)
    );

    Pdef(\p2,
        ~makeSubdivision.(
            PlaceAll([Prand([1.5, 1], inf), 2, 6] * 0.5, inf),
            PlaceAll([2, [4, 2], 0], inf)
        )
    );

    // Pdef(\p2,
    //     ~makeSubdivision.(
    //         PlaceAll([Prand([1.5, 1], inf), 2, 1] * 0.5, inf),
    //         PlaceAll([2, [4, 2], 1], inf)
    //     )
    // );

    sp.par(
        Pdef(\b, 
            Pbindf(Pdef(\bMod), \degree, (Pkey(\eventcount).stutter(2) - (Pkey(\groupcount).stutter(3))) + 4)
            // <> ~filterBeat.(key: Pkey(\eventcount), beat:[1, 4])
            <> ~pSkew.(Pdef(\p2), key: Pkey(\eventcount), group: [1, 3], skew: [-0.5], curve: \exp)
            <> Pdef(\p2)
            <> Pbind(\instrument, \pulsePluck)
        ).finDur(sectionLength)
    );

    sp.par(
        Pbindf(
            Pdef(\morph),
            \gain, 0,
            \atk, 10,
            \rel, 100,
            \swap, ~pmodenv.(Pseq([1, 0],inf), Pseq([1], inf), 1, \sin),
            // \swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([1/3], inf), 1, \sin),
            // \swap, ~pmodenv.(Pseq([0.1, 0.5],inf), Pseq([4], inf), 1, \sin),
            // \swap, 0,
            // \swap, 0,
            \out, [~mainout, ~miVerb, ~vocoder_out]
        ).finDur(sectionLength)
    );
    sp.wait(sectionLength);

    /////////////////////////////
    \e.postln;
    sectionLength = 36;

    sp.par(
        Pbindf(
            Pdef(\tape),
            \pregain, 0.0,
            \dcOffset, 0.1,
            // \dcOffset, 0.,
            \sigmoid, 0.1,
            \postgain, -6,
            // \autogain, 1.0,
            \drywet, 1,
            \amp, 1.0,
            \out, [~convolve_B, ~mainout, ~tape_out],
        ).finDur(sectionLength)
    );

    sp.par(
        Pmono(\grainSlicer_mono,
            \amp, 1,
            \buf, ~sampleBuf.at(\file),
            \overlap, 100,
            \trigRate, Pseg(Pseq([10, 1000,], inf), 4, 'exp' , inf),
            \slice, ~pGetSlice.(5, ~sampleBuf),
            \gain, -6,
            \posRate, 1,
            \rate, 0.75,
            \out, [~convolve_A, ~mainout, ~grainslicer_out]
        ).finDur(sectionLength)
    );

    Pdef(\p1,
        ~makeSubdivision.(
            PlaceAll([Prand([1.5, 1], inf), 2, 1, 1], inf),
            PlaceAll([2, [4, 2], 1, 2, 2], inf)
        )
    );

    Pdef(\p1,
        ~makeSubdivision.(
            PlaceAll([Prand([1.5, 1], inf), 2, 6], inf),
            PlaceAll([2, [4, 2], 0, 1], inf)
        )
    );

    sp.par(
        Pdef(\a, 
            Pbindf(
                Pdef(\aMod), 
                // \degree, (Pkey(\eventcount).stutter(3) - (Pkey(\groupcount).stutter(3))),
                \degree, (Pkey(\eventcount).stutter(2) - (Pkey(\groupcount).stutter(3))),
                \mtranspose, -2,
                \lfoShape, 2
            )
            // <> ~filterBeat.(key: Pkey(\eventcount), beat:[1, 3, 4])
            <> ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [1, 3], skew: [-0.5], curve: \exp)
            <> Pdef(\p1)
            <> Pbind(\instrument, \pulsePluck)
        ).finDur(sectionLength)
    );

    Pdef(\p2,
        ~makeSubdivision.(
            PlaceAll([Prand([1.5, 1], inf), 2, 6] * 0.5, inf),
            PlaceAll([2, [4, 2], 0], inf)
        )
    );

    // Pdef(\p2,
    //     ~makeSubdivision.(
    //         PlaceAll([Prand([1.5, 1], inf), 2, 1] * 0.5, inf),
    //         PlaceAll([2, [4, 2], 1], inf)
    //     )
    // );

    sp.par(
        Pdef(\b, 
            Pbindf(
                Pdef(\bMod), 
                \degree, (Pkey(\eventcount).stutter(2) - (Pkey(\groupcount).stutter(3))) + 4,
                \lfoShape, 2,
                \mainout, ~tape
            )
            // <> ~filterBeat.(key: Pkey(\eventcount), beat:[1, 4])
            <> ~pSkew.(Pdef(\p2), key: Pkey(\eventcount), group: [1, 3], skew: [-0.5], curve: \exp)
            <> Pdef(\p2)
            <> Pbind(\instrument, \pulsePluck)
        ).finDur(sectionLength)
    );

    sp.par(
        Pbindf(
            Pdef(\morph),
            \gain, 0,
            \atk, 10,
            \rel, 100,
            // \swap, ~pmodenv.(Pseq([1, 0],inf), Pseq([1], inf), 1, \sin),
            // \swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([1/3], inf), 1, \sin),
            // \swap, ~pmodenv.(Pseq([0.1, 0.5],inf), Pseq([4], inf), 1, \sin),
            // \swap, 0,
            // \swap, 0,
            \out, [~mainout, ~miVerb, ~vocoder_out]
        ).finDur(sectionLength)
    );
    sp.wait(sectionLength);

    \f.postln;
    sectionLength = 36;

    sp.par(
        Pbindf(
            Pdef(\tape),
            \pregain, 0.0,
            \dcOffset, 0.1,
            // \dcOffset, 0.,
            \sigmoid, 0.1,
            \postgain, -6,
            // \autogain, 1.0,
            \drywet, 1,
            \amp, 1.0,
            \out, [~convolve_B, ~mainout, ~tape_out]
        ).finDur(sectionLength)
    );

    sp.par(
        Pmono(\grainSlicer_mono,
            \amp, 1,
            \buf, ~sampleBuf.at(\file),
            \overlap, 100,
            \trigRate, Pseg(Pseq([10, 1000,], inf), 4, 'exp' , inf),
            \slice, ~pGetSlice.(25, ~sampleBuf),
            \gain, -6,
            \posRate, 1,
            \rate, 0.75,
            \out, [~convolve_A, ~mainout, ~grainslicer_out]
        ).finDur(sectionLength)
    );

    Pdef(\p1,
        ~makeSubdivision.(
            PlaceAll([Prand([1.5, 1], inf), 2, 1, 1], inf),
            PlaceAll([2, [4, 2], 1, 2, 2], inf)
        )
    );

    Pdef(\p1,
        ~makeSubdivision.(
            PlaceAll([Prand([1.5, 1], inf), 2, 6], inf),
            PlaceAll([2, [4, 2], 0, 1], inf)
        )
    );

    sp.par(
        Pdef(\a, 
            Pbindf(
                Pdef(\aMod), 
                // \degree, (Pkey(\eventcount).stutter(3) - (Pkey(\groupcount).stutter(3))),
                \degree, (Pkey(\eventcount).stutter(2) - (Pkey(\groupcount).stutter(3))),
                \mtranspose, -2,
                \lfoShape, 2
            )
            // <> ~filterBeat.(key: Pkey(\eventcount), beat:[1, 3, 4])
            <> ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [1, 3], skew: [-0.5], curve: \exp)
            <> Pdef(\p1)
            <> Pbind(\instrument, \pulsePluck)
        ).finDur(sectionLength)
    );

    Pdef(\p2,
        ~makeSubdivision.(
            PlaceAll([Prand([1.5, 1], inf), 2, 6] * 0.5, inf),
            PlaceAll([2, [4, 2], 0], inf)
        )
    );

    // Pdef(\p2,
    //     ~makeSubdivision.(
    //         PlaceAll([Prand([1.5, 1], inf), 2, 1] * 0.5, inf),
    //         PlaceAll([2, [4, 2], 1], inf)
    //     )
    // );

    sp.par(
        Pdef(\b, 
            Pbindf(
                Pdef(\bMod), 
                \degree, (Pkey(\eventcount).stutter(2) - (Pkey(\groupcount).stutter(3))) + 4,
                \lfoShape, 2,
                \mtranspose, 7,
                \mainout, ~tape
            )
            // <> ~filterBeat.(key: Pkey(\eventcount), beat:[1, 4])
            <> ~pSkew.(Pdef(\p2), key: Pkey(\eventcount), group: [1, 3], skew: [-0.5], curve: \exp)
            <> Pdef(\p2)
            <> Pbind(\instrument, \pulsePluck)
        ).finDur(sectionLength)
    );

    sp.par(
        Pbindf(
            Pdef(\morph),
            \gain, 0,
            \atk, 10,
            \rel, 100,
            \swap, ~pmodenv.(Pseq([1, 0],inf), Pseq([1], inf), 1, \sin),
            // \swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([1/3], inf), 1, \sin),
            // \swap, ~pmodenv.(Pseq([0.1, 0.5],inf), Pseq([4], inf), 1, \sin),
            // \swap, 0,
            // \swap, 0,
            \out, [~mainout, ~miVerb, ~vocoder_out]
        ).finDur(sectionLength)
    );
    sp.wait(sectionLength);
    ///////////
    \g.postln;
    sp.par(
        Pmono(\grainSlicer_mono,
            \amp, 1,
            \buf, ~sampleBuf.at(\file),
            \overlap, 100,
            \trigRate, Pseg(Pseq([10, 1000,], inf), 4, 'exp' , inf),
            \slice, ~pGetSlice.(1, ~sampleBuf),
            \gain, -6,
            \posRate, 1,
            \rate, 0.75,
            \out, [~mainout, ~grainslicer_out]
        )
    );
})
).play(t);
)

(
Ndef(\sweetie, { 
    var mod = SinOsc.kr(0.1);
    var sig = HarmonicOsc.ar(
        freq: mod.linexp(-1.0,1.0,10,1000), 
        firstharmonic: 3,
        amplitudes: Array.rand(16, 0.1,1.0).normalizeSum
    );

    Pan2.ar(sig, mod);
}).play;
)