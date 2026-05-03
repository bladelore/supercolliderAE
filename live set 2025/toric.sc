
(
    a = Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./NEW sample lib/LAPerc-SampleLibrary/LAPerc-TempleBowls.wav");
    b = Buffer.read(s,"/Users/aelazary/Desktop/Samples etc./Silent Hill/Drum Loops/Silent Hill Origins/Snowblind/05 drums02.wav");

    // a = Buffer.read(s,"/Users/aelazary/Desktop/Samples etc./NEW sample lib/LAPerc-SampleLibrary/LAPerc-BellPlate3.wav");
    // a = Buffer.read(s,"/Users/aelazary/Desktop/Samples etc./NEW sample lib/LAPerc-SampleLibrary/LAPerc-Cajon3.wav");
    // a = Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./hollywood edge - foley sound library/FSL-05/FSL-05-Card Shoe Clicks; Dealer Card Shoe Clicks. - Dealing Cards.wav");
    // a = Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./hollywood edge - foley sound library/FSL-03/FSL-03-Wood Staff Hits, Multiple; Multiple Light Wood Staff Impacts With Handling. - Wood Hits.wav");

    AdditiveSines.analyse(s, a, numPartials: 32, windowSize: 2048, slicerThreshold: 0.3, order: 1, detectionThreshold: -90, action: { |wt| ~wt1 = wt; ~wt1.loadBuffers(s);});
    AdditiveSines.analyse(s, b, numPartials: 32, windowSize: 2048, slicerThreshold: 0.3, order: 1, detectionThreshold: -90, action: { |wt| ~wt2 = wt; ~wt2.loadBuffers(s);});

    ~break = Dictionary();
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./NEW sample lib/LAPerc-SampleLibrary/LAPerc-MarchingPerc.wav", ~break, 0.30, \centroid, chans: 2);

    ~specBuff = SpecBuf(s, "/Users/aelazary/Desktop/Samples etc./Silent Hill/Ambience/Silent Hill Origins/Drowning/efxl_pad_082.wav", 16384*2, overlaps: 2, numChannels: 2);
    // ~specBuff = SpecBuf(s, "/Users/aelazary/Desktop/Samples etc./guitar samples/electric 25-3-4.wav", 16384*2, overlaps: 2, numChannels: 2);


    // ~sample = Dictionary();
    // ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./NEW sample lib/LAPerc-SampleLibrary/LAPerc-PianoPercussion.wav", ~sample, 0.6, \centroid, chans: 2);
)

k.gui

~break

(
    var player; 
    var tempo = 75/60;

    ///
    player = Conductor(\player, t);
    
    t.tempo = tempo;
    player.quant_(0);
    player.targetSection_(nil);

    player.listen((type: \modality, device: k, key: \tr, button: \fwd));
    ///
    x = {

        player.label;

            Pdef(\specSample,
                Pmono(\fftStretch_magFilter_stereo,
                    \dur, 0.01,
                    \amp, 1,
                    \gain, -10,
                    \buf, [~specBuff.file], \analysis, [~specBuff.analysisBufs], \fftSize, [~specBuff.fftSize],
                    // \pitchRatio, Pstep(Pseq([5, 0].midiratio, inf), 16, inf),
                    \pitchRatio, 1,
                    \len, ~knob.(4),
                    \rate, ~knob.(5) * 4,
                    // \rate, 0,
                    \pos, ~knob.(6),
                    
                    \remove, ~knob.(7).linexp(0,1,0.01, 1) - 0.01,
                    // \remove, ~pmodenv.(Pexprand(0.001, 0.1), 3, inf, \sin),
                    \pan, ~pmodenv.(Pwhite(-0.6, 0.6), 3, inf, \sin),
                    // \thresh, PmodEnv(Pseq([500, 10, 500], 1), 3, [0.5, -0.5]).loop,
                    // \pos, PmodEnv(Pseq([0,1], 1), 10, inf).loop,
                    \thresh, 50,
                    \amp, PmodEnv(Pseq([0.1, 2, 0.1], 1), 3, [0.5, -0.5]).loop,
                    \out, ~bus3
                )
            ).play(t, quant: 1);

        player.wait;

        player.label;

            Pdef(\cut1,
                Pbind(
                    \instrument, \segPlayerADSR,
                    // \amp, Pkey(\groupdelta),
                    \dur, Pseq([Rest(2), 2], inf),
                    // \dur, 0.25,
                    \amp, 1,
                    \atk, 1,
                    \dec, 0.1,
                    \sus, 0,
                    \rel, 0,
                    // \rel, Pkey(\dur) * Pseq([0.5, 0.5, 2, 0.5], inf),
                    \buf, ~break.at(\file),
                    // \rate, Pseq([1, 1, 2, 1], inf),
                    \pitchMix, 0.5,
                    \pitchDispersion, 0.01,
                    \timeDispersion, 0.5,
                    \rate, 1,
                    \oneshot, 1,
                    \gain, -6,
                    \pan, Pwhite(-0.6, 0.6, inf),
                    \slice, ~pGetSlice.(Pseries(0, 1, inf).wrap(0, 16) + 10, ~break),
                    \out, ~bus4
                )
            ).play(t, quant: 1);

        player.wait;

        player.label;

            Ndef(\dsf).clear;
            Ndef(\dsf, \dsf);
            Ndef(\dsf).set(\fc, 50, \tfreq, 200);
            Ndef(\dsf)[999] = \pset -> Pbind(
                \amp, 1,
                \dur, 0.01,
                \atk, 0,
                \envCurve, 0,
                
                // \tFreq, ~slider.(0).linexp(0,1,5,20000).lag(0.1),
                \rate, tempo * Pstep([4, 2, 1, 3, 8], 2, inf),
                // \rate, tempo * Pstep([4, 2, 1, 2, 8], 2, inf),
                \fm, ~slider.(2).linexp(0,1,0.001,12000),
                \curve, ~slider.(3).linlin(0,1,-4,4),
                \modStereo, ~slider.(4).linlin(0,1,0,1000),
                \lpf, ~slider.(5).linlin(0,1,5,20000),
                \distort, ~slider.(6).linexp(0,1,0.01,1) - 0.01,
                \fb, ~slider.(7),
                \verbMix, ~knob.(0).linexp(0,1,0.001, 1) - 0.001,
                \disperserMix, ~knob.(1),
                \pitchMix, ~knob.(2),

                \pitchRatio, 2,
                \windowSize, 0.1,
                \pitchDispersion, 0.05,
                \timeDispersion, 0.1,
                // \resonance, ~knob.(3)
            );

            Ndef(\dsf).play(~bus1);

        player.wait;

        player.label;

            Ndef(\dsf).stop;

            Pdef(\p1,
                ~makeSubdivision.(
                    PlaceAll([1.5, 1.5, 1] * 1, inf),
                    PlaceAll([[2, 1], 2, 2], inf)
                )
            );

            Pdef(\additivePerc,
                Pbind(
                    \wt, [~wt1.asControlInput],
                    \instrument, \additivePerc,
                    \dec, Pwrand([0.1, 1], [0.5, 0.5], inf),
                    \sus, 0,
                    \rel, 0,
                    \transpose, 0,
                    \oneshot, 1,
                    \sliceStart, Pstep([0, 5, 2], 1, inf),
                    \ampAtk, 0,
                    \ampRel, 0,
                    // \ampThresh, Prand([0.2, 0, 0.1], inf),
                    \ampThresh, 0,
                    \flatten, Pwrand([0, 1], [0.5, 0.5], inf).stutter(2),
                    \odd, 1,
                    \even, 1,
                    \rate, 0.5,
                    \lpf, 20000,
                    \tilt, 0,
                    \quantize, 0,
                    \spread, 1,
                    \slice, ~wt1.pGetSlice(Pseries(0, 1, inf).wrap(0, 16)).stutter(1),
                    \resFreq, 60,
                    \resDistort, 1,
                    \resAmp, 1,
                    \resDec, 0.5,
                    \gain, 40,
                    \amp, 1,
                    \pan, Pwhite(-0.5, 0.5,inf),
                    \out, ~bus2
                )
                <> ~filterBeat.(key: Pkey(\cyclecount), beat:[2, 5], reject: 1)
                <> Pdef(\p1)
            ).play(t);

        player.wait;

        player.label;

            Ndef(\dsf)[999] = \pset -> Pbind(
                \amp, 1,
                \dur, 0.01,
                \atk, 0,
                \envCurve, 0,
                \rate, tempo * Pstep([8, 16], 0.5, inf),
                \fm, ~slider.(2).linexp(0,1,0.001,12000),
                \curve, ~slider.(3).linlin(0,1,-4,4),
                \modStereo, ~slider.(4).linlin(0,1,0,1000),
                \lpf, ~slider.(5).linlin(0,1,5,20000),
                \distort, ~slider.(6).linexp(0,1,0.01,1) - 0.01,
                \fb, ~slider.(7),
                \verbMix, ~knob.(0).linexp(0,1,0.001, 1) - 0.001,
                \disperserMix, ~knob.(1),
                \pitchMix, ~knob.(2),

                \pitchRatio, 2,
                \windowSize, 0.1,
                \pitchDispersion, 0.05,
                \timeDispersion, 0.1,
                // \resonance, ~knob.(3)
            );

            Ndef(\dsf).play(~bus1);

        player.wait;

        player.label;

            Ndef(\dsf)[999] = \pset -> Pbind(
                \amp, 1,
                \dur, 0.01,
                \atk, 0,
                \envCurve, 0,
                // \rate, tempo * Pstep([8, 16], 0.5, inf),
                \rate, tempo * 8,
                \fm, ~slider.(2).linexp(0,1,0.001,12000),
                \curve, ~slider.(3).linlin(0,1,-4,4),
                \modStereo, ~slider.(4).linlin(0,1,0,1000),
                \lpf, ~slider.(5).linlin(0,1,5,20000),
                \distort, ~slider.(6).linexp(0,1,0.01,1) - 0.01,
                \fb, ~slider.(7),
                \verbMix, ~knob.(0).linexp(0,1,0.001, 1) - 0.001,
                \disperserMix, ~knob.(1),
                \pitchMix, ~knob.(2),

                \pitchRatio, 2,
                \windowSize, 0.1,
                \pitchDispersion, 0.05,
                \timeDispersion, 0.1,
                // \resonance, ~knob.(3)
            );

        player.wait;

        player.label;

            Pdef(\p1,
                ~makeSubdivision.(
                    PlaceAll([1, 1, 1, 1] * 0.5, inf),
                    PlaceAll([[4, 1], 2, 4, 2,], inf)
                )
            );

            Pdef(\additivePerc,
                Pbind(
                    \wt, [~wt1.asControlInput],
                    \instrument, \additivePerc,

                    // \dur, Pwrand([1, 0.5, 2, 3], [1, 2, 0.25, 0.125].normalizeSum, inf).stutter(4),
                    // \dur, Pseq([0.25, Rest(0.25), 0.5, Rest(0.25), 0.25, 0.25, 0.25, 0.75, 0.5, 0.25], inf),
                    // \dur, Pseq([0.25, 0.5, 0.25, 0.25], inf),
                    // \dur, 0.125,
                    // \atk, 0.02,
                    // \atk, Pwrand([0.1, 0.05], [1, 0.25].normalizeSum, inf),
                    \dec, Pwrand([0.1, 1], [0.5, 0.5], inf),
                    // \sus, Pwrand([0, 0.1], [0.5, 0.5], inf),
                    \sus, 0,
                    \rel, 0,
                    \transpose, 0,
                    \oneshot, 1,
                    \sliceStart, Pstep([0, 5, 2], 1, inf),
                    \ampAtk, 0,
                    \ampRel, 0,
                    // \ampThresh, Prand([0.2, 0, 0.1], inf),
                    \ampThresh, 0,
                    \flatten, Pwrand([0, 1], [0.5, 0.5], inf).stutter(2),
                    \odd, 1,
                    \even, 1,
                    \rate, 0.5,
                    \lpf, 20000,
                    \tilt, 0,
                    \quantize, 0,
                    \spread, 1,
                    \slice, ~wt1.pGetSlice(Pseries(0, 1, inf).wrap(0, 16)).stutter(1),
                    \resFreq, 60,
                    \resDistort, 1,
                    \resAmp, 1,
                    \resDec, 0.5,
                    \gain, 40,
                    \amp, 1,
                    \pan, Pwhite(-0.5, 0.5,inf),
                    \out, ~bus2
                )
                <> ~filterBeat.(key: Pkey(\cyclecount), beat:[2, 5], reject: 1)
                <> Pdef(\p1)
            ).play(t);

        player.wait;

        player.label;

            Pdef(\p1,
                ~makeSubdivision.(
                    PlaceAll([1, 1, 1, 1] * 0.5, inf),
                    PlaceAll([[4, 1], 2, 4, 2,], inf)
                )
            );

            Pdef(\additivePerc,
                Pbind(
                    \wt, [~wt2.asControlInput],
                    \instrument, \additivePerc,

                    // \dur, Pwrand([1, 0.5, 2, 3], [1, 2, 0.25, 0.125].normalizeSum, inf).stutter(4),
                    // \dur, Pseq([0.25, Rest(0.25), 0.5, Rest(0.25), 0.25, 0.25, 0.25, 0.75, 0.5, 0.25], inf),
                    // \dur, Pseq([0.25, 0.5, 0.25, 0.25], inf),
                    // \dur, 0.125,
                    // \atk, 0.02,
                    // \atk, Pwrand([0.1, 0.05], [1, 0.25].normalizeSum, inf),
                    \dec, Pwrand([0.1, 1], [0.5, 0.5], inf),
                    // \sus, Pwrand([0, 0.1], [0.5, 0.5], inf),
                    \sus, 0,
                    \rel, 0.5,
                    \transpose, 0,
                    \oneshot, 1,
                    \sliceStart, Pstep([0, 5, 2], 1, inf),
                    \ampAtk, 0,
                    \ampRel, 0,
                    // \ampThresh, Prand([0.2, 0, 0.1], inf),
                    \ampThresh, 0,
                    \flatten, Pwrand([0, 1], [0.5, 0.5], inf).stutter(2),
                    \odd, 1,
                    \even, 1,
                    \rate, 0.5,
                    \lpf, 20000,
                    \tilt, Pwrand([0, 6], [0.5, 0.5], inf),
                    \quantize, 0,
                    \spread, 1,
                    \slice, ~wt2.pGetSlice(Pseries(0, 1, inf).wrap(0, 16)).stutter(1),
                    \resFreq, 40,
                    \resDistort, 1,
                    \resAmp, 1,
                    \resDec, 0.5,
                    \gain, 40,
                    \amp, 1,
                    \pan, Pwhite(-0.5, 0.5,inf),
                    \out, ~bus2
                )
                // <> ~filterBeat.(key: Pkey(\cyclecount), beat:[2, 5], reject: 1)
                <> Pdef(\p1)
            ).play(t);

        player.wait;

        player.label;

            Pdef(\additivePerc).stop;

        player.wait;

        player.label;

            Pdef(\cut1).stop;

        player.wait;

        player.label(\end);

            Ndef(\dsf).stop(fadeTime: 10);

    }.fork;
)

Pdef(\specSample).stop;