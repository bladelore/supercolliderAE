(
    ~break = Dictionary();
    ~break2 = Dictionary();
    ~shPad2 = Dictionary();
    ~specBuff=Dictionary(); 
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./radioaporee/CityCountryMeworkumdrache.wav", ~break, 0.3, \centroid, chans: 2);
    // ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Missing Sounds 2016/FAI_172_E_BassLoop_12.wav", ~break2, 0.1, \centroid, chans: 2);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Missing Sounds 2016/DJF_174_C_CREEPY_PLUCKS.wav", ~break2, 0.1, \centroid, chans: 2);
    // ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Missing Sounds 2016/PHA_140_C_Synthloop_08.wav", ~break2, 0.1, \centroid, chans: 0);
    // ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Prism samples/Audio 0003 [2024-12-19 184904].aif", ~break2, 0.3, \centroid, chans: 2);
    ~makeSpec.("/Users/aelazary/Desktop/Samples etc./Silent Hill/Ambience/Silent Hill 4/Fortunate Sleep - Cat Scratchism Mix/FEEDIES 1.wav", ~specBuff, 16384, 2);
    ~roar_A=Bus.audio(s,2);
    ~roar_B=Bus.audio(s,2);
)

k.gui

(
    var sample = ~break;
    var sample2 = ~break2;

    t = TempoClock.new(137/60).permanent_(true).schedAbs(0, {t.beatsPerBar_(4)});

    ~new_advance.();

    x = {
            
        Ndef(\roar_A).clear;
        Ndef(\roar_A, \roar)
        .set(
            \inbus, ~roar_A,
            \drive, 6.0,
            \toneFreq, 500.0,
            \toneComp, 1,
            \drywet, 1,
            \bias, 0,
            \filterFreq, 50,
            \filterBP, 0,
            \filterRes, 0.3,
            \filterBW, 0.5,
            \filterPre, 1.0,
            \feedAmt, 9.0,
            \feedFreq, 50.0,
            \feedBW, 0.1,
            \feedDelay, 0.1,
            \feedGate, 0.05,
            \gain, 6.0,
            \amp, 1.0,
        );

        Ndef(\lfo1, { SinOsc.ar(t.tempo / \rate.kr(2), pi).linlin(-1, 1, -0.99, 0.75) });
        Ndef(\lfo2, { SinOsc.ar(t.tempo / \rate.kr(1), pi).linlin(-1, 1, 0, 1) });

        Ndef(\lfo1)[999] = \pset -> Pbind(\rate, ~knob.(0).linlin(0, 1, 1, 32).ceil.reciprocal * 8, \dur, 0.01);
        Ndef(\lfo2)[999] = \pset -> Pbind(\rate, ~knob.(1).linlin(0, 1, 1, 32).ceil.reciprocal * 8, \dur, 0.01);

        Ndef(\roar_A).map(
            \tone, Ndef(\lfo1),
            \filterLoHi, Ndef(\lfo2)
        );

        Ndef(\roar_B).clear;
        Ndef(\roar_B, \roar)
        .set(
            \inbus, ~roar_B,
            \drive, 6.0,
            \toneFreq, 500.0,
            \toneComp, 0,
            \drywet, 1,
            \bias, 0,
            \filterFreq, 50,
            \filterBP, 0,
            \filterRes, 0.3,
            \filterBW, 0.5,
            \filterPre, 1.0,
            \feedAmt, 9.0,
            \feedFreq, 50.0,
            \feedBW, 0.1,
            \feedDelay, 0.1,
            \feedGate, 0.05,
            \gain, 6.0,
            \amp, 1.0,
        );

        Ndef(\roar_B).map(
            \tone, Ndef(\lfo1),
            \filterLoHi, Ndef(\lfo2)
        );

        Ndef(\roar_A).play(~bus1);
        Ndef(\roar_B).play(~bus2);

        Pdef(\reso, (
            Pbind(\out, ~bus1, \atk, 0.01, \dec, 1, \distort, 1, \freq, 50, \inGain, -20, \gain, 0) <> 
            Pdef(\resonator)
        )).play;

        Ndef(\verb, \miVerb)
        .set(
            \amp, 1,
            \time, 0.9,
            \timeMod, 0.4,
            \hp, 0.1,
            \damp, 0.1,
            \dampMod, 1,
            \inbus, ~miVerb
        ).play(~bus4);

        //////////////////////////////////////////////////////////////////////////////////
        \a.postln;  

            Pdef(\p1,
                ~makeSubdivision.(
                    PlaceAll([1, 2, 2], inf),
                    PlaceAll([4, 2, 2, 4], inf)
                )
            );

            Pdef(\fmString,
                Pbind(
                    \instrument, \fmString,
                    \midiPitch, 42.midicps(),
                    \pitchLag, 3,
                    \atk, 0,
                    \rel, 1,
                    \fb, -1,
                    \filter, 500,
                    \fuzz, 0,
                    \subharmonic, 3,
                    \exciterAttack, 3000,
                    \exciterRelease, Pseq([1000, 3000, 3000], inf),
                    \gain, 0,
                    \out, [~roar_B, ~miVerb], 
                ) 
                <> ~filterBeat.(key: Pkey(\cyclecount), beat:[2], mod: 3, reject: 1)
                <> ~filterBeat.(key: Pkey(\groupcount), beat:[1, 3], mod: 3, reject: 0)
                <> Pdef(\p1)
            ).play(t);

        ~advance.wait;

            \b.postln;

            b = Buffer.read(s, "/Users/aelazary/Projects/soundthread/outfile_2025-11-12_15-39-26.wav");
            Ndef(\sample).fadeTime = 15;
            Ndef(\sample, {
                    var sig;
                    var buf = \buf.kr(0);
                    sig = PlayBuf.ar(2, buf, startPos: \pos.kr(0) * BufFrames.kr(buf), rate: \rate.kr(0), loop: \loop.kr(0));
                    sig = sig * \gain.kr(0).dbamp;
            });
            Ndef(\sample).set(\buf, b, \pos, 0, \rate, 1, \loop, 1, \gain, 0).play(~bus3);

            Pdef(\cut2,
                Pbind(
                    \instrument, \segPlayer,
                    \amp, 1,
                    \atk, 0.5,
                    \rate, 1,
                    \rel, Pkey(\dur),
                    \buf, sample2.at(\file),
                    \oneshot, 1,
                    \gain, 0,
                    \stutterPat, 1,
                    \sliceStart, 26,
                    \stutterRange, Pstep(Pseq([3, 6], inf), 1, inf),
                    \slice, ~pGetSlice.((Pseries(0, Pkey(\stutterRange), inf) + Pkey(\sliceStart)), sample2).stutter(Pkey(\stutterPat)),
                    \pitchMix, 0.5,
                    \pitchRatio, 1,
                    \windowSize, 0.05,
                    \pitchDispersion, 0.01,
                    \timeDispersion, 0.5,
                    \gain, -24,
                    \pan, 0,
                    \out, [~bus1],
                
                ) <>
                ~filterBeat.(key: Pkey(\cyclecount), beat:[1], mod: 3, reject: 0) <>
                Pdef(\p1)
            ).play(t);

        ~advance.wait;
            
            \c.postln;

            Pdef(\fmString,
                Pbind(
                    \instrument, \fmString,
                    \midiPitch, 42.midicps(),
                    // \midiPitch, 79.midicps(),
                    // \midiPitch, Pseq([78, 79, 78, 81, 42].midicps, inf),
                    \pitchLag, 3,
                    \atk, 0,
                    \rel, 1,
                    \fb, -1,
                    \filter, 800,
                    \fuzz, 0,
                    \subharmonic, 3,
                    \exciterAttack, 3000,
                    \exciterRelease, 1000,
                    \exciterRelease, Pseq([1000, 3000, 3000], inf),
                    \gain, 0,
                    \out, [~roar_B, ~miVerb],
                ) 
                <> ~filterBeat.(key: Pkey(\cyclecount), beat:[2], mod: 3, reject: 1)
                <> Pdef(\p1)
            ).play(t);

            Pdef(\cut1,
                Pbind(
                    \instrument, \segPlayer,
                    // \amp, 1 - Pkey(\groupdelta),
                    \amp, 1,
                    \atk, 0,
                    // \rel, Pkey(\dur) * 1.5,
                    \rel, Pkey(\dur) * Pseq([0.5, 0.5, 1.5], inf),
                    \buf, sample.at(\file),
                    \rate, Pseq([2, 2, 2, 2, 2, 1], inf),
                    \oneshot, 1,
                    \gain, 0,
                    \stutterPat, Pstep(Pseq([1, 2], inf), Pseq([8, 1], inf), inf),
                    \sliceStart, Pstep(Pseq([64, 128], inf), 3, inf),
                    // \stutterPat, 1,
                    \stutterRange, Pstep(Pseq([32, 6], inf), 1, inf),
                    // \stutterRange, 32,
                    \slice, ~pGetSlice.((Pseries(1, Pkey(\stutterRange), inf) + Pkey(\sliceStart)), sample).stutter(Pkey(\stutterPat)),
                    // \slice, ~pGetSlice.((Pseries(1, 32, inf) + Pkey(\sliceStart)), sample),
                    \pitchMix, 0.5,
                    \pitchRatio, 1,
                    \windowSize, 0.01,
                    \pitchDispersion, 0.01,
                    \timeDispersion, 0.5,
                    \pan, 0,
                    \out, [~roar_A, ~resonator]
                )
            );

            Pdef(\perc, 
                Pdef(\cut1) <>
                ~filterBeat.(key: Pkey(\cyclecount), beat:[1], mod: 3, reject: 0) <>
                Pdef(\p1)
            ).play(t);

        ~advance.wait;

        \d.postln;
            
            Pdef(\cut1,
                Pbind(
                    \instrument, \segPlayer,
                    // \amp, 1 - Pkey(\groupdelta),
                    \amp, 1,
                    \atk, 0,
                    // \rel, Pkey(\dur) * 0.5,
                    \rel, Pkey(\dur) * Pseq([0.5, 0.5, 1.5], inf),
                    \buf, sample.at(\file),
                    \rate, Pseq([2, 2, 2, 2, 2, 1], inf),
                    \oneshot, 1,
                    \gain, 6,
                    \stutterPat, Pstep(Pseq([1, 2], inf), Pseq([8, 1], inf), inf),
                    \sliceStart, Pstep(Pseq([64, 128], inf), 3, inf),
                    // \stutterPat, 1,
                    \stutterRange, Pstep(Pseq([32, 6], inf), 1, inf),
                    // \stutterRange, 32,
                    \slice, ~pGetSlice.((Pseries(1, Pkey(\stutterRange), inf) + Pkey(\sliceStart)), sample).stutter(Pkey(\stutterPat)),
                    // \slice, ~pGetSlice.((Pseries(1, 32, inf) + Pkey(\sliceStart)), sample),
                    \pitchMix, 0.5,
                    \pitchRatio, 1,
                    \windowSize, 0.01,
                    \pitchDispersion, 0.01,
                    \timeDispersion, 0.5,
                    \pan, 0,
                    \out, [~roar_A, ~resonator, ~miVerb]
                )
            );

            Pdef(\p1,
                ~makeSubdivision.(
                    PlaceAll([1, 1, 2], inf),
                    PlaceAll([2, 4, 4, 4], inf)
                )
            );

            Pdef(\perc, 
                Pdef(\cut1) <>
                ~filterBeat.(key: Pkey(\cyclecount), beat:[1], mod: 3, reject: 0) <>
                Pdef(\p1)
            ).play(t);
            
        ~advance.wait;

            \e.postln;
            Pdef(\perc).stop;

            Pdef(\fmString,
                Pbind(
                    \instrument, \fmString,
                    // \midiPitch, 54.midicps(),
                    // \midiPitch, 79.midicps(),
                    \midiPitch, 42.midicps,
                    // \midiPitch, Pseq([78, 79, 78, 81, 54].midicps, inf),
                    \pitchLag, 3,
                    \atk, 0.1,
                    \rel, 1,
                    \fb, -1,
                    \filter, 1000,
                    \fuzz, 0.05,
                    // \fuzz, 0.5,
                    \subharmonic, 1,
                    \exciterAttack, 3000,
                    \exciterRelease, 1000,
                    \exciterRelease, Pseq([1000, 3000, 3000], inf),
                    \gain, 0,
                    \out, [~roar_B], 
                ) 
                // <> ~filterBeat.(key: Pkey(\cyclecount), beat:[2], mod: 3, reject: 1)
                <> Pdef(\p1)
            ).play(t);

        ~advance.wait;

            \f.postln;

            Pdef(\pad,
                Pmono(\fftStretch_magAbove_mono,
                    \dur, 0.01,
                    \amp, 0.5,
                    \gain, 6,
                    \buf, ~specBuff.at(\file),
                    \analysis, [~specBuff.at(\analysis)],
                    \fftSize, ~specBuff.at(\fftSize),
                    \rate, ~knob.(5),
                    \pitchRatio, -1.midiratio,
                    \pos, 0.15,
                    // \pos, 0.25,
                    // \pos, 0.01,
                    // \pos, 0.55,
                    // \pos, 0.7,
                    \filter, ~knob.(6),
                    \pos, ~knob.(7),
                    \len, 0.2,
                    \pan, ~pmodenv.(Pwhite(-0.6, 0.6), 3, 1, \sin),
                    \out, [~bus3, ~miVerb]
                )
            ).play(t);

            Pdef(\cut1,
                Pbind(
                    \instrument, \segPlayer,
                    // \amp, 1 - Pkey(\groupdelta),
                    \amp, 1,
                    \atk, 0,
                    // \rel, Pkey(\dur) * 0.5,
                    \rel, Pkey(\dur) * Pseq([0.5, 0.5, 1.5], inf),
                    \buf, sample.at(\file),
                    \rate, Pseq([2, 2, 2, 2, 2, 1], inf),
                    \oneshot, 1,
                    \gain, 6,
                    \stutterPat, Pstep(Pseq([1, 2], inf), Pseq([8, 1], inf), inf),
                    \sliceStart, Pstep(Pseq([64, 128], inf), 3, inf),
                    // \stutterPat, 1,
                    \stutterRange, Pstep(Pseq([32, 6], inf), 1, inf),
                    // \stutterRange, 32,
                    \slice, ~pGetSlice.((Pseries(1, Pkey(\stutterRange), inf) + Pkey(\sliceStart)), sample).stutter(Pkey(\stutterPat)),
                    // \slice, ~pGetSlice.((Pseries(1, 32, inf) + Pkey(\sliceStart)), sample),
                    \pitchMix, 0.5,
                    \pitchRatio, 1,
                    \windowSize, 0.05,
                    \pitchDispersion, 0.01,
                    \timeDispersion, 0.5,
                    \pan, 0,
                    \out, [~roar_A, ~resonator]
                )
            );

            Pdef(\perc, 
                Pdef(\cut1) <>
                ~filterBeat.(key: Pkey(\cyclecount), beat:[1, 3], mod: 3, reject: 0) <>
                Pdef(\p1)
            ).play(t);

        ~advance.wait;
        
            \g.postln;

            Pdef(\kickParams,
                Pbind(
                    \instrument, \fmPerc3,
                    \freq, 80,
                    \atk, 0,
                    \dec, 0.2,
                    \fb, 0,
                    \index1, 1,
                    \index2, 2,
                    \ratio1, 4,
                    \ratio2, 8,
                    \drive, 0,
                    \drivemix, 0,
                    \sweep, 16.0,
                    \spread, 0.0,
                    \noise, 0,
                    \fbmod, 0.1,
                    \pulseWidth, 1 - Pkey(\groupdelta).linlin(0,1,0.1,0.99),
                    \lofreq, 500.0,
                    \lodb, 3.0,
                    \midfreq, 1200.0,
                    \middb, 0.0,
                    \hifreq, 7000.0,
                    \hidb, 6.0,
                    \gain, -17.0,
                    \pan, 0.0,
                    // \hpf, 
                    \amp, Pkey(\groupdelta).linlin(0, 1, 1, 0.3),
                    \out, [~roar_A, ~resonator]
                )
            );

            Pdef(\kick, 
                Pdef(\kickParams)
                <> ~filterBeat.(key: Pkey(\eventcount), beat:[1], reject: 0)
                <> Pdef(\p1)
            ).play(t);

            Pdef(\p2,
                ~makeSubdivision.(
                    PlaceAll([1, 1, 1, 1], inf),
                    PlaceAll([4, 4, 4, 4], inf)
                )
            );

            Pdef(\fmString,
                Pbind(
                    \instrument, \fmString,
                    // \midiPitch, 54.midicps(),
                    // \midiPitch, 79.midicps(),
                    \midiPitch, 42.midicps,
                    // \midiPitch, Pseq([78, 79, 78, 81, 54].midicps, inf),
                    \pitchLag, 3,
                    \atk, 0,
                    \rel, 1,
                    \fb, -1,
                    \filter, 1000,
                    \fuzz, 0.15,
                    // \fuzz, 0.5,
                    \subharmonic, 1,
                    \exciterAttack, 3000,
                    \exciterRelease, 1000,
                    \exciterRelease, Pseq([1000, 3000, 3000], inf),
                    \gain, 0,
                    \out, [~roar_B], 
                ) 
                // <> ~filterBeat.(key: Pkey(\cyclecount), beat:[2], mod: 3, reject: 1)
                <> Pdef(\p1)
            ).play(t);

            Pdef(\p1,
                ~makeSubdivision.(
                    PlaceAll([1.5, 1.5, 1], inf),
                    PlaceAll([3, 2, 4], inf)
                )
            );

            Pdef(\perc, 
                Pdef(\cut1) <>
                ~filterBeat.(key: Pkey(\cyclecount), beat:[2], mod: 3, reject: 1) <>
                Pdef(\p1)
            ).play(t);

        ~advance.wait;

            \h.postln;

            Pdef(\fmString_hat,
                Pbind(
                    \instrument, \fmString,
                    \midiPitch, 60.midicps(),
                    // \midiPitch, 79.midicps(),
                    // \midiPitch, Pseq([78, 79, 78, 81, 42].midicps, inf),
                    \pitchLag, 3,
                    \atk, 0,
                    \rel, 0.5,
                    \fb, 1,
                    \filter, 800,
                    \fuzz, 1,
                    \subharmonic, 1.1,
                    \exciterAttack, 0,
                    \exciterRelease, 100,
                    // \exciterRelease, Pseq([1000, 3000, 3000] * 10, inf),
                    \gain, 20,
                    \out, [~bus3],
                ) 
                <> ~filterBeat.(key: Pkey(\eventcount), beat:[1], reject: 1)
                <> Pdef(\p2)
            ).play(t);

        ~advance.wait;

            \i.postln;

            Pdef(\fmString_high,
                Pbind(
                    \instrument, \fmString,
                    // \midiPitch, 54.midicps(),
                    // \midiPitch, 79.midicps(),
                    \midiPitch, Pseq([78, 79, 78, 81, Rest(42)].midicps, inf),
                    \pitchLag, 3,
                    \atk, 0,
                    \rel, 1,
                    \fb, -1,
                    \filter, 1000,
                    \fuzz, 0,
                    \subharmonic, 3,
                    \exciterAttack, 3000,
                    \exciterRelease, 1000,
                    \exciterRelease, Pseq([1000, 3000, 3000, 6000] * 2, inf),
                    \gain, 0,
                    \out, [~roar_B, ~miVerb], 
                ) 
                <> ~filterBeat.(key: Pkey(\cyclecount), beat:[2], mod: 3, reject: 1)
                <> Pdef(\p1)
            ).play(t);

            Pdef(\p1,
                ~makeSubdivision.(
                    PlaceAll([1, 1, 2], inf),
                    PlaceAll([2, 4, 4, 4], inf)
                )
            );

            Pdef(\perc,
                Pdef(\cut1) <>
                // ~filterBeat.(key: Pkey(\cyclecount), beat:[2], mod: 3, reject: 1) <>
                Pdef(\p1)
            ).play(t);

        ~advance.wait;

            \j.postln;

            Pdef(\kickParams,
                Pbind(
                    \instrument, \fmPerc3,
                    \freq, 80,
                    \atk, 0,
                    \dec, 0.2,
                    \fb, 0,
                    \index1, 1,
                    \index2, 2,
                    \ratio1, 4,
                    \ratio2, 8,
                    \drive, 0,
                    \drivemix, 0,
                    \sweep, 16.0,
                    \spread, 0.0,
                    \noise, 0,
                    \fbmod, 0.1,
                    \pulseWidth, 1 - Pkey(\groupdelta).linlin(0,1,0.1,0.99),
                    \lofreq, 500.0,
                    \lodb, 3.0,
                    \midfreq, 1200.0,
                    \middb, 0.0,
                    \hifreq, 7000.0,
                    \hidb, 6.0,
                    \gain, -17.0,
                    \pan, 0.0,
                    // \hpf, 
                    \amp, Pkey(\groupdelta).linlin(0, 1, 1, 0.3),
                    \out, [~roar_A, ~resonator]
                )
            );

            // Pdef(\pad,
            //     Pmono(\fftStretch_magAbove_mono,
            //         \dur, 0.01,
            //         \amp, 0.5,
            //         \gain, 6,
            //         \buf, ~specBuff.at(\file),
            //         \analysis, [~specBuff.at(\analysis)],
            //         \fftSize, ~specBuff.at(\fftSize),
            //         \rate, ~knob.(5),
            //         \pitchRatio, -8.midiratio,
            //         // \pos, 0.25,
            //         // \pos, 0.01,
            //         // \pos, 0.55,
            //         // \pos, 0.7,
            //         \filter, ~knob.(6),
            //         \pos, ~knob.(7),
            //         \len, 0.2,
            //         \pan, ~pmodenv.(Pwhite(-0.6, 0.6), 3, 1, \sin),
            //         \out, [~tape, ~miVerb]
            //     )
            // ).play(t);

            // Ndef(\tape, \tape).set(
            //     \inbus, ~tape,
            //     \pregain, 5.0,
            //     \dcOffset, 0,
            //     // \dcOffset, 0.,
            //     \sigmoid, 0.8,
            //     \postgain, -18,
            //     \autogain, 0.0,
            //     \drywet, 1,
            //     \amp, 1.0,
            // ).play(~bus3);

            Pdef(\p1,
                ~makeSubdivision.(
                    PlaceAll([1, 1, 2, 3], inf),
                    PlaceAll([2, 4, 4, 4, 4, 2], inf)
                )
            );

            Pdef(\p2,
                ~makeSubdivision.(
                    PlaceAll([1.5, 1.5, 1.5, 1.5, 1], inf),
                    PlaceAll([2, 2, 2, 2, 2], inf)
                )
            );

            Pdef(\kick, 
                Pdef(\kickParams)
                <> ~filterBeat.(key: Pkey(\cyclecount), beat:[3], reject: 1)
                <> Pdef(\p2)
            ).play(t);

            Pdef(\fmString_high,
                Pbind(
                    \instrument, \fmString,
                    // \midiPitch, 54.midicps(),
                    // \midiPitch, 79.midicps(),
                    \midiPitch, Pseq([79, 78, 81, 82, 41].midicps, inf),
                    \pitchLag, 3,
                    \atk, 0,
                    \rel, 1,
                    \fb, -1,
                    \filter, 1000,
                    \fuzz, 0.05,
                    \subharmonic, 1,
                    \exciterAttack, 3000,
                    \exciterRelease, 1000,
                    \exciterRelease, Pseq([1000, 3000, 3000] * 3, inf),
                    \gain, 0,
                    \out, [~roar_B], 
                ) 
                // <> ~filterBeat.(key: Pkey(\cyclecount), beat:[2], mod: 3, reject: 1)
                <> Pdef(\p1)
            ).play(t);

        ~advance.wait;

            \i.postln;

            // Ndef(\sample).stop;

            Pdef(\pad).stop;

            Ndef(\lfo1, { SinOsc.ar(t.tempo / 1, pi).linlin(-1, 1, -0.99, 0.75) });
            Ndef(\lfo2, { SinOsc.ar(t.tempo / 3, pi).linlin(-1, 1, 0, 1) });


            // Ndef(\roar_A).set(\toneComp, 1);
            // Ndef(\roar_B).set(\toneComp, 1);

            Pdef(\fmString).stop;
            Pdef(\perc).stop;
            Pdef(\cut2).stop;

        ~advance.wait;

            // Ndef(\roar_A).set(\toneComp, 0);
            // Ndef(\roar_B).set(\toneComp, 0);

            // Ndef(\lfo1, { SinOsc.ar(t.tempo / \rate.kr(0.5), pi).linlin(-1, 1, -0.99, 0.75) });
            // Ndef(\lfo1)[999] = \pset -> Pbind(\rate, ~knob.(0).linlin(0, 1, 1, 32).ceil.reciprocal * 8, \dur, 0.01);
            
            // Ndef(\lfo2, { SinOsc.ar(t.tempo / \rate.kr(0.25), pi).linlin(-1, 1, 0, 1) });
            // Ndef(\lfo2)[999] = \pset -> Pbind(\rate, ~knob.(1).linlin(0, 1, 1, 32).ceil.reciprocal * 8, \dur, 0.01);

            // Ndef(\roar_B).map(
            //     \tone, Ndef(\lfo1),
            //     \filterLoHi, Ndef(\lfo2)
            // );

            Pdef(\fmString_high,
                Pbind(
                    \instrument, \fmString,
                    // \midiPitch, 54.midicps(),
                    // \midiPitch, 79.midicps(),
                    \midiPitch, Pseq([86, 81, 82, 79, 42].midicps, inf),
                    \pitchLag, 3,
                    \atk, 0,
                    \rel, 3,
                    \fb, -1,
                    \filter, 2000,
                    \fuzz, 0,
                    \subharmonic, 1,
                    \exciterAttack, 0,
                    // \exciterRelease, 1000,
                    \exciterRelease, Pseq([1000, 3000, 3000] * 3, inf),
                    \gain, 0,
                    \out, ~roar_B, 
                ) 
                <> ~filterBeat.(key: Pkey(\cyclecount), beat:[2], mod: 3, reject: 1)
                <> Pdef(\p1)
            ).play(t);

            Pdef(\kick).stop;
            Pdef(\fmString_hat).stop;
            
            Pdef(\p1,
                ~makeSubdivision.(
                    PlaceAll([1, 1, 2, 3], inf),
                    PlaceAll([2, 4, 4, 4, 4, 2], inf)
                )
            );

            Ndef(\lfo1, { SinOsc.ar(t.tempo / 4, pi).linlin(-1, 1, -0.99, 0.75) });
            Ndef(\lfo2, { SinOsc.ar(t.tempo / 3, pi).linlin(-1, 1, 0, 1) });

            Pdef(\fmString_high,
                Pbind(
                    \instrument, \fmString,
                    // \midiPitch, 54.midicps(),
                    // \midiPitch, 79.midicps(),
                    \midiPitch, Pseq([91, 86, 81, 82, 79, 42].midicps, inf),
                    \pitchLag, 3,
                    \atk, 0,
                    \rel, 2,
                    \fb, -1,
                    \filter, 5000,
                    \fuzz, 0,
                    \subharmonic, 1,
                    \exciterAttack, 0,
                    \exciterRelease, 100,
                    // \exciterRelease, Pseq([1000, 3000, 3000] * 3, inf),
                    \gain, 0,
                    \out, [~roar_B, ~miVerb], 
                ) 
                <> ~filterBeat.(key: Pkey(\cyclecount), beat:[2], mod: 3, reject: 1)
                <> Pdef(\p1)
            ).play(t);

        ~advance.wait;

            Pdef(\fmString_high2,
                Pbind(
                    \instrument, \fmString,
                    // \midiPitch, 54.midicps(),
                    // \midiPitch, 79.midicps(),
                    \midiPitch, Prand(([91, 86, 81, 82, 79, 42, 75] - 12).midicps, inf),
                    \pitchLag, 3,
                    \atk, 0,
                    \rel, 2,
                    \fb, 1,
                    \filter, 5000,
                    \fuzz, 0,
                    \subharmonic, 1,
                    \exciterAttack, 0,
                    \exciterRelease, 100,
                    \exciterRelease, Pseq([1000, 3000, 3000] * 3, inf),
                    \gain, 0,
                    \out, [~roar_A, ~miVerb], 
                ) 
                <> ~filterBeat.(key: Pkey(\cyclecount), beat:[1], mod: 5, reject: 1)
                <> Pdef(\p1)
            ).play(t);

        ~advance.wait;

            Pdef(\fmString_high2,
                Pbind(
                    \instrument, \fmString,
                    // \midiPitch, 54.midicps(),
                    // \midiPitch, 79.midicps(),
                    \midiPitch, Prand(([91, 86, 81, 82, 79, 42, 74] - 12).midicps, inf),
                    \pitchLag, 3,
                    \atk, 0,
                    \rel, 2,
                    \fb, 1,
                    \filter, 5000,
                    \fuzz, 0,
                    \subharmonic, 1,
                    \exciterAttack, 0,
                    \exciterRelease, 100,
                    \exciterRelease, Pseq([1000, 3000, 3000] * 3, inf),
                    \gain, 0, 
                    \out, [~roar_A, ~miVerb], 
                ) 
                <> ~filterBeat.(key: Pkey(\cyclecount), beat:[1], mod: 5, reject: 1)
                <> Pdef(\p1)
            ).play(t);

        ~advance.wait;

            Pdef(\fmString_high2,
                Pbind(
                    \instrument, \fmString,
                    // \midiPitch, 54.midicps(),
                    // \midiPitch, 79.midicps(),
                    \midiPitch, Prand(([91, 86, 81, 82, 79, 42, 74] - 12).midicps, inf),
                    \pitchLag, 3,
                    \atk, 0,
                    \rel, 2,
                    \fb, 1,
                    \filter, 5000,
                    \fuzz, 0.5,
                    \subharmonic, 1,
                    \exciterAttack, 0,
                    \exciterRelease, 100,
                    \exciterRelease, Pseq([1000, 3000, 3000] * 3, inf),
                    \gain, 0,
                    \out, [~roar_A, ~miVerb], 
                ) 
                <> ~filterBeat.(key: Pkey(\cyclecount), beat:[1], mod: 5, reject: 1)
                <> Pdef(\p1)
            ).play(t);

            Pdef(\fmString_high,
                Pbind(
                    \instrument, \fmString,
                    \midiPitch, 54.midicps(),
                    // \midiPitch, 79.midicps(),
                    \midiPitch, Pseq([91, 86, 81, 82, 79, 42].midicps, inf),
                    \pitchLag, 3,
                    \atk, 0.5,
                    \rel, 2,
                    \fb, -1,
                    \filter, 1000,
                    \fuzz, 0,
                    \subharmonic, 2,
                    \exciterAttack, 0,
                    // \exciterRelease, 100,
                    \exciterRelease, Pseq([1000, 3000, 3000] * 10, inf),
                    \gain, 0,
                    \out, [~roar_B, ~miVerb], 
                ) 
                <> ~filterBeat.(key: Pkey(\cyclecount), beat:[2], mod: 3, reject: 1)
                <> Pdef(\p1)
            ).play(t);

        ~advance.wait;

            Pdef(\fmString_high).stop;

            Pdef(\fmString_high2,
                Pbind(
                    \instrument, \fmString,
                    // \midiPitch, 54.midicps(),
                    // \midiPitch, 79.midicps(),
                    \midiPitch, Prand(([91, 86, 81, 82, 79, 42, 74] - 12).midicps, inf),
                    \pitchLag, 3,
                    \atk, 0.5,
                    \rel, 2,
                    \fb, -1,
                    \filter, 2000,
                    \fuzz, 0.5,
                    \subharmonic, 2,
                    \exciterAttack, 0,
                    \exciterRelease, 100,
                    \exciterRelease, Pseq([1000, 3000, 3000] * 3, inf),
                    \gain, 0,
                    \out, [~roar_A, ~miVerb], 
                ) 
                <> ~filterBeat.(key: Pkey(\cyclecount), beat:[1], mod: 5, reject: 1)
                <> Pdef(\p1)
            ).play(t);
        
    }.fork(t);
)

(
    Pdef(\fmString_high2).stop;
    Ndef(\sample).stop(fadeTime: 10);
    Ndef(\miVerb).stop(fadeTime: 10);
)