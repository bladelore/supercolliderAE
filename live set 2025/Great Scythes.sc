(
    ~break = Dictionary();
    ~ice = Dictionary();
    ~scythe = Dictionary();
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Missing Sounds 2016/01-Andatra_Break_127_PL_1.WAV", ~break, 0.1, \centroid, chans: 2);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Film Sounds/ice-dispenser-crushed-ice.wav", ~ice, 0.1, \centroid, chans: 2);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./scythes/Sharpen the scythe.wav", ~scythe, 0.1, \centroid, chans: 2);
)

(
    ~busOut = {|busArr|
        var busChans = busArr;
        busChans = busArr.collect({|bus, i| (bus.index + (0..bus.numChannels-1))});
        busChans.flatten;
    };

    Ndef(\xyz,{
        SinOsc.ar([440, 444]) * 0.1;
    }).play(~busOut.([~bus1, ~bus4]));
)

(
   SynthDef(\scythe,{|gate=1|
        var sig;
        var combEnv = \combEnv.kr(2);
        var pitchEnv = \pitchEnv.kr(0.01);
        var pitchMod = \pitchMod.kr(10);
        var baseFreq = \freq.kr(50);
        var pitchNoise = \pitchNoise.kr(0);
        var filter;
        var filterEnv = \filterEnv.kr(1);
        var noiseEnv = \noiseEnv.kr(0.1);
        var shifterRatio = \shifterRatio.kr(1);
        var ratios = \ratios.kr(1 ! 5);

        var filterHi = \filterHi.kr(15000);
        var filterLo = \filterLo.kr(50);

        sig = Pulse.ar(
            (baseFreq * ratios * XLine.ar(pitchMod, 1, pitchEnv)) + (WhiteNoise.ar(1) * 1000 * pitchNoise);, 
            XLine.ar(0, 1, pitchEnv) * (1..5) * \pulseWidth.kr(0.5)
        );

        // (XLine.ar(1, 0.01, pitchNoise) - 0.01)

        filterEnv = EnvGen.ar(Env(levels: [filterLo, filterHi, filterLo], times: (filterEnv) ! 3, curve: 8), gate: Impulse.ar(0));
        //
        sig = CombC.ar(sig, 0.01, XLine.kr(0.0001, 1, combEnv / (1..5)), 0.5);
        // sig = sig.tanh;
        sig = HPF.ar(sig, filterEnv);
        
        sig = RLPF.ar(sig, filterEnv, 1);
        
        // sig = sig + (BPF.ar(sig, (0..20).normalize.linexp(0, 1, 50, 20000), 0.01).sum * 0.dbamp);
        
        sig = sig + PinkNoise.ar(noiseEnv);
        sig = VASEM12.ar(sig, XLine.ar(5000, 50, pitchEnv), res: 1, transition: SinOsc.ar(0.1 * (1..5)));
        // sig = VASEM12.ar(sig, XLine.ar(50, 5000, pitchEnv), res: 1, transition: Line.ar(1, 0, pitchEnv));
        
        sig = sig + (PitchShift.ar(sig, pitchRatio: [1, 1.5, 2] * shifterRatio, windowSize: \shifterWindow.kr(0.2), timeDispersion: 0.001, pitchDispersion: 0.05).sum * \shifterMix.kr(1));
        
        sig = Disperser.ar(
            input: sig,
            freq: baseFreq * ratios,
            resonance: \resonance.kr(0.5),
            mix: 1,
            feedback: 1
        );

        sig = sig * EnvGen.ar(Env.perc(\atk.kr(0.1), \rel.kr(1), level: 1, curve: -2), gate, doneAction: 2);

        sig = Splay.ar(sig, 1);

        

        sig = Rotate2.ar(sig[0], sig[1], LFSaw.ar(\rotate.kr(0.01)));

        // sig = HPF.ar(sig, \hpf_out.kr(50));

        sig = sig.tanh;
        sig = RLPF.ar(sig, \lpf.kr(10000));
        sig = sig * \amp.kr(1);
        sig = sig * \gain.kr(1).dbamp;
        sig = sig.sanitize;

        Out.ar(\out.kr(0), sig);
    }).add;

    SynthDef(\scythe_vosim,{|gate=1|
        var sig;
        var combEnv = \combEnv.kr(2);
        var pitchEnv = \pitchEnv.kr(0.01);
        var pitchMod = \pitchMod.kr(10);
        var baseFreq = \freq.kr(50);
        var pitchNoise = \pitchNoise.kr(0);
        var filter;
        var filterEnv = \filterEnv.kr(1);
        var noiseEnv = \noiseEnv.kr(0.1);
        var shifterRatio = \shifterRatio.kr(1);
        var ratios = \ratios.kr(1 ! 5);

        var filterHi = \filterHi.kr(15000);
        var filterLo = \filterLo.kr(50);

        sig = VOSIM.ar(
            Impulse.ar((baseFreq * ratios * XLine.ar(pitchMod, 1, pitchEnv)) + (WhiteNoise.ar(1) * 1000 * pitchNoise)), 
            ratios * baseFreq * 10 * \pulseWidth.kr(0.5),
            3, 0.99
        );

        sig = sig.tanh;

        filterEnv = EnvGen.ar(Env(levels: [filterLo, filterHi, filterLo], times: (filterEnv) ! 3, curve: 8), gate: Impulse.ar(0));
        //
        sig = CombC.ar(sig, 0.01, XLine.kr(0.0001, 1, combEnv / (1..5)), 0.5);
        
        sig = sig + (BPF.ar(sig, (0..20).normalize.linexp(0, 1, 50, 20000), 0.01).sum * 0.dbamp);
        
        sig = sig + PinkNoise.ar(noiseEnv);
        sig = VASEM12.ar(sig, XLine.ar(5000, 50, pitchEnv), res: 1, transition: SinOsc.ar(0.1 * (1..5)));
        sig = VASEM12.ar(sig, XLine.ar(50, 5000, pitchEnv), res: 1, transition: Line.ar(1, 0, pitchEnv));
        
        sig = sig + (PitchShift.ar(sig, pitchRatio: [1, 1.5, 2] * shifterRatio, windowSize: \shifterWindow.kr(0.2), timeDispersion: 0.001, pitchDispersion: 0.05).sum * \shifterMix.kr(1));
        
        // sig = Disperser.ar(
        //     input: sig,
        //     freq: baseFreq * ratios,
        //     resonance: \resonance.kr(0.5),
        //     mix: 1,
        //     feedback: 1
        // );

        sig = sig * EnvGen.ar(Env.perc(\atk.kr(0.1), \rel.kr(1), level: 1, curve: -2), gate, doneAction: 2);

        sig = Splay.ar(sig, 1);

        sig = Rotate2.ar(sig[0], sig[1], LFSaw.ar(\rotate.kr(0.01)));

        sig = HPF.ar(sig, \hpf_out.kr(50));

        sig = sig.tanh;
        sig = sig * \amp.kr(1);
        sig = sig * \gain.kr(1).dbamp;
        sig = sig.sanitize;

        Out.ar(\out.kr(0), sig);
    }).add;


    // Pdef(\scytheParams,
    //     Pbind(
    //         \instrument, \scythe,
    //         \gain, -24,
    //         \amp, ~slider.(0),
    //         \atk, ~slider.(1).linlin(0, 1, 0, 2),
    //         \rel, ~slider.(2).linlin(0, 1, 0.1, 2),
    //         \freq, 47.midicps,
    //         \shifterRatio, 1,

    //         \ratios, [[1, 3, 2.95, 3.97, 2]] * 0.5,
    //         // \ratios, [[1.005, 1.001, 1.01, 1.001, 1.02]] * 0.5,
    //         // \ratios, [[2, 3, 7, 5, 1.5]] * 0.5,
    //         // \ratios, [[1.0, 1.5, 1.75, 2.25, 5]] * 0.5,
            
    //         // \filterEnv, ~slider.(4) * Pxrand([0.1, 2, 0.5, 1.5, 0.7], inf),
    //         \filterEnv, ~slider.(4) * 5,
    //         \combEnv, ~slider.(5).linexp(0, 1, 0.001, 2),
    //         \pitchEnv, ~slider.(6).linexp(0, 1, 0.01, 4),
    //         // \pitchMod, 20,
    //         \pitchNoise, ~slider.(7).linexp(0,1,0.01,1) - 0.01,
            
    //         \shifterMix, ~knob.(0),
    //         \shifterWindow, ~knob.(1),
    //         \noiseEnv, ~knob.(2),
    //         \pulseWidth, ~knob.(3).linlin(0,1,0.01,0.99),
    //         \resonance, ~knob.(4),
    //         \rotate, ~knob.(5).linexp(0,1,0.01,15) - 0.01,
    //         // \resonance, 0.75,
    //         \filterLo, 50,
    //         \filterHi, 15000,
    //         \out, [~bus1, ~convolve_A, ~miVerb]
    //     )
    // );
    
    // Pdef(\scythe,
    //     Pdef(\scytheParams) <>
    //     // ~filterBeat.(key: Pkey(\eventcount), beat:[2], reject: 0) <>
    //     Pdef(\p1)
    // ).play(t);
)

(

    var sample = ~break;
    var sample2 = ~ice;
    var sample3 = ~scythe;

    t = TempoClock.new(160/60).permanent_(true).schedAbs(0, {t.beatsPerBar_(4)});

    ~new_advance.();

    x = {

        Pdef(\p1,
            ~makeSubdivision.(
                PlaceAll([1.5, 1.5, 1.5, 1.5, 1.5, 1.5, 1] * 0.5, inf),
                PlaceAll([1, 1, 1, 1, 1, 1, 1, 1, 1, 4], inf)
            )
        );

        Pdef(\p2,
            ~makeSubdivision.(
                PlaceAll([1, 1, 0.5], inf),
                PlaceAll([4, 2, 4, 4], inf)
            )
        );



        //ever since I saw the sun
        //i reached for the dirt above
        //beneath the farmland and crops 
        //my fingers dig at the earth
        //wielded by an angry god
        //great scythes will always take
        //great scythes will always know

        Pdef(\kickParams, 
            Pbind(
                // \dur, 1,
                \instrument, \fmKick2,
                \freq, 40.midicps * 0.65,
                // \freq, 40.midicps * 0.75,
                // \freq, 37.midicps,
                \atk, 0,
                \dec, Pkey(\dur) * 0.5,
                // \dec, Pkey(\dur) * Pseq([0.5, 0.5, 2], inf),
                \fb, 0,
                \index1, 1,
                \index2, 1,
                \ratio1, 2,
                \ratio2, 3,
                \drive, 1,
                \drivemix, 0,
                \sweep, 8.0,
                \spread, 20.0,
                \noise, 0,
                \feedback, 0,
                \fbmod, 1,
                \pulseWidth, 0,
                \lofreq, 500.0,
                \lodb, 0.0,
                \midfreq, 1200.0,
                \middb, 0.0,
                \hifreq, 7000.0,
                \hidb, 30.0,
                \gain, -20.0,
                \pan, 0.0,
                // \hpf, 
                \amp, Pkey(\groupdelta).linlin(0,1,1,0.3),
                // \amp, 1,
                \out, ~roar
            )
        );

        Pdef(\cut1,
            Pbind(
                \instrument, \segPlayer,
                // \amp, Pkey(\groupdelta),
                \amp, 1,
                \atk, 0.01,
                \rel, Pkey(\dur) * 0.5,
                // \rel, Pkey(\dur) * Pseq([0.5, 0.5, 2, 0.5], inf),
                \buf, sample.at(\file),
                // \rate, Pseq([1, 1, 2, 1], inf),
                \rate, 0.5,
                \oneshot, 1,
                \gain, -6,
                \sliceStart, 40,
                // \strum, 0.5,
                \stutterPat, Pseq([2, 3], inf),
                // \stutterPat, 1,
                \slice, ~pGetSlice.((Pseries(1, 32, inf) + Pkey(\sliceStart)), sample).stutter(Pkey(\stutterPat)),
                \pitchMix, 0.8,
                // \pitchRatio, 1,
                \windowSize, 0.01,
                \pitchDispersion, 0.01,
                \timeDispersion, 0.5,
                \out, [~bus2]
            )
        );

        // \a.postln;

            b = Buffer.read(s, "/Users/aelazary/Projects/great scythes poem Project/great scythes words.wav");
            c = Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./Film Sounds/ice-dispenser-crushed-ice.wav");

            // Ndef(\sample_A, {var buf = \buf.kr(0); PlayBuf.ar(2, buf, startPos: \pos.kr(0), rate: \rate.kr(1) * BufRateScale.kr(buf), loop: \loop.kr(0)) * \gain.kr(0).dbamp;});
            Ndef(\sample_A, 
                {
                    var sig;
                    var buf = \buf.kr(0);
                    var pos = \pos.kr(0) * BufFrames.kr(buf);
                    sig = PlayBuf.ar(2, buf, startPos: pos, rate: \rate.kr(0) * BufRateScale.kr(buf), loop: \loop.kr(0), trigger: Impulse.kr(0) + Changed.kr(pos + buf));
                    sig = sig * \gain.kr(0).dbamp;
                }
            );

            Ndef(\sample_A).fadeTime = 5;
            Ndef(\sample_A).set(\buf, b, \pos, 0, \rate, 1, \loop, 0, \gain, 12).play(~convolve_B);

            Ndef(\morph, \cepstralMorph_fx).set(\inbus_A, ~convolve_A, \inbus_B, ~convolve_B);
            Ndef(\morph)[999] = \pset -> Pbind(
                \amp, 1,
                \dur, 0.01,
                \gain, -10,
                \atk, 10,
                \rel, 100,
                \swap, ~knob.(7),
                \drywet, 1,
            );
            
            Ndef(\morph).play(~bus3);

            Ndef(\verb, \miVerb)
            .set(
                \amp, 1,
                \time, 0.4,
                \timeMod, 0.4,
                \hp, 0.1,
                \damp, 0.5,
                \dampMod, 1,
                // \diff, 1,
                \inbus, ~miVerb
            ).play(~bus4);

        ~advance.wait;
            
            \b.postln;
            
            Ndef(\sample_A).copy(\sample_B);
            Ndef(\sample_B).fadeTime = 5;
            Ndef(\sample_B).set(\buf, b, \pos, 0, \rate, 1, \loop, 1, \gain, 12).play(~convolve_B);

        ~advance.wait;

            Ndef(\sample_B).set(\buf, b, \pos, 0.62, \rate, 1, \loop, 1, \gain, 12).play(~convolve_B);
        
            \c.postln;


            Pdef(\p3,
                ~makeSubdivision.(
                    PlaceAll([1, 1, 1, 1, 1.5, 1.5, 1], inf),
                    PlaceAll([1, 1, 1, 1, 1, 1, 1, 1, 1, 2], inf)
                )
            );

            Pdef(\scytheParams,
                Pbind(
                    \instrument, \scythe,
                    \gain, -24,
                    \amp, ~slider.(0),
                    \atk, ~slider.(1).linlin(0, 1, 0, 2),
                    \rel, ~slider.(2).linlin(0, 1, 0.1, 2),
                    \freq, 47.midicps,
                    \shifterRatio, 1,

                    // \ratios, [[1, 1, 1, 1, 1]] * 0.5,
                    \ratios, [[1.005, 1.001, 1.01, 1.001, 1.02]] * 0.5,
                    // \ratios, [[2, 3, 7, 5, 1.5]] * 0.5,
                    // \ratios, [[1.0, 1.5, 1.75, 2.25, 5]] * 0.5,
                    
                    // \filterEnv, ~slider.(4) * Pxrand([0.1, 2, 0.5, 1.5, 0.7], inf),
                    \lpf, ~slider.(3).linexp(0,1,50,12000),
                    \filterEnv, ~slider.(4) * 5,
                    \combEnv, ~slider.(5).linexp(0, 1, 0.001, 2),
                    \pitchEnv, ~slider.(6).linexp(0, 1, 0.01, 4),
                    // \pitchMod, 20,
                    \pitchNoise, ~slider.(7).linexp(0,1,0.01,1) - 0.01,
                    
                    \shifterMix, ~knob.(0),
                    \shifterWindow, ~knob.(1),
                    \noiseEnv, ~knob.(2),
                    \pulseWidth, ~knob.(3).linlin(0,1,0.01,0.99),
                    \resonance, ~knob.(4),
                    \rotate, ~knob.(5).linexp(0,1,0.01,15) - 0.01,
                    \filterLo, 50,
                    \filterHi, 15000,
                    \out, [~bus1, ~convolve_A]
                )
            );
            
            Pdef(\scythe,
                Pdef(\scytheParams) <>
                ~filterBeat.(key: Pkey(\eventcount), beat:[2], reject: 0) <>
                Pdef(\p2)
            ).play(t);

        ~advance.wait;

            \d.postln;
            
            Ndef(\sample_A).fadeTime = 10;
            Ndef(\sample_A).set(\buf, c, \pos, 0, \rate, 1, \loop, 1, \gain, 0).play(~convolve_B);

        ~advance.wait;

            \e.postln;

            // Ndef(\sample_B).stop(fadeTime: 10);

            Pdef(\roarFx,
                Pdef(\roar) <>
                Pbind(
                    \dur, 0.01,
                    \drive, 0.0,
                    \tone, ~pmodenv.(Pseq([-0.99, 0.75], inf), 1),
                    \toneFreq, 500.0,
                    \toneComp, 0,
                    \drywet, 1,
                    \bias, 0,
                    \filterFreq, 5000,
                    \filterLoHi, ~pmodenv.(Pseq([0, 1], inf), 0.5),
                    \filterBP, 0,
                    \filterRes, 0.3,
                    \filterBW, 0.5,
                    \filterPre, 1.0,
                    \feedAmt, 9.0,
                    \feedFreq, 50.0,
                    \feedBW, 0.1,
                    \feedDelay, 0.1,
                    \feedGate, 0.05,
                    \gain, 12.0,
                    \amp, 1.0,
                    \out, ~bus2
                )
            ).play(t);

            Pdef(\kick,
                Pdef(\kickParams) <>
                ~filterBeat.(key: Pkey(\groupcount), beat:[1, 3, 5], reject: 0) <>
                Pdef(\p1)
            ).play(t);

        ~advance.wait;

            \f.postln;

            Pdef(\kick,
                Pdef(\kickParams) <>
                Pdef(\p1)
            ).play(t);

        ~advance.wait;
            
            \g.postln;

            Pdef(\perc,
                Pdef(\cut1) <>
                ~filterBeat.(key: Pkey(\cyclecount), beat:[3], mod: 9, reject: 1) <>
                Pdef(\p2)
            ).play(t);

        ~advance.wait;

            \g.postln;

            Pdef(\scytheParams,
                Pbind(
                    \instrument, \scythe,
                    \gain, -24,
                    \amp, ~slider.(0),
                    \atk, ~slider.(1).linlin(0, 1, 0, 2),
                    \rel, ~slider.(2).linlin(0, 1, 0.1, 2),
                    \freq, 47.midicps,
                    \shifterRatio, 1,

                    // \ratios, [[1, 1, 1, 1, 1]] * 0.5,
                    // \ratios, [[1.005, 1.001, 1.01, 1.001, 1.02]] * 0.5,
                    \ratios, [[2, 3, 7, 5, 1.5]] * 0.5,

                    // \ratios, [[1.0, 1.5, 1.75, 2.25, 5]] * 0.5,
                    
                    \filterEnv, ~slider.(4) * 5,
                    \combEnv, ~slider.(5).linexp(0, 1, 0.001, 2),
                    \pitchEnv, ~slider.(6).linexp(0, 1, 0.01, 4),
                    \pitchNoise, ~slider.(7).linexp(0,1,0.01,1) - 0.01,
                    
                    \shifterMix, ~knob.(0),
                    \shifterWindow, ~knob.(1),
                    \noiseEnv, ~knob.(2),
                    \pulseWidth, ~knob.(3).linlin(0,1,0.01,0.99),
                    \resonance, ~knob.(4),
                    \rotate, ~knob.(5).linexp(0,1,0.01,15) - 0.01,
                    \filterLo, 50,
                    \filterHi, 15000,
                    \out, [~bus1, ~convolve_A]
                )
            );

        ~advance.wait;

            Pdef(\perc,
                Pdef(\cut1) <>
                Pdef(\p2)
            ).play(t);

        ~advance.wait;

            Pdef(\cut2,
                Pbind(
                    \instrument, \segPlayer,
                    \amp, 1,
                    \atk, 0.05,
                    \rel, Pkey(\dur) * 0.5,
                    \buf, sample3.at(\file),
                    \rate, 2,
                    \oneshot, 1,
                    \gain, -12,
                    \sliceStart, Pstep(Pseq([64, 128], inf), 3, inf),
                    \stutterPat, 1,
                    \stutterRange, Pstep(Pseq([32, 6], inf), 1, inf),
                    \slice, ~pGetSlice.((Pseries(1, Pkey(\stutterRange), inf) + Pkey(\sliceStart)), sample3).stutter(Pkey(\stutterPat)),
                    \pitchMix, 0.7,
                    \pitchRatio, 1,
                    \windowSize, 0.01,
                    \pitchDispersion, 0.01,
                    \timeDispersion, 0.5,
                    \pan, 0,
                    \out, [~bus2, ~convolve_B]
                )
                <> ~filterBeat.(key: Pkey(\cyclecount), beat:[3], mod: 9, reject: 1)
                <> Pdef(\p2)
            ).play(t);       

        ~advance.wait;

            \octave.postln;

            Pdef(\scytheParams,
                Pbind(
                    \instrument, \scythe,
                    \gain, -24,
                    \amp, ~slider.(0),
                    \atk, ~slider.(1).linlin(0, 1, 0, 2),
                    \rel, ~slider.(2).linlin(0, 1, 0.1, 2),
                    \freq, 47.midicps * 0.5,
                    \shifterRatio, 1,

                    // \ratios, [[1, 1, 1, 1, 1]] * 0.5,
                    // \ratios, [[1.005, 1.001, 1.01, 1.001, 1.02]] * 0.5,
                    \ratios, [[2, 3, 7, 5, 1.5]] * 0.5,
                    // \ratios, [[1.0, 1.5, 1.75, 2.25, 5]] * 0.5,
                    
                    // \filterEnv, ~slider.(4) * Pxrand([0.1, 2, 0.5, 1.5, 0.7], inf),
                    \filterEnv, ~slider.(4) * 5,
                    \combEnv, ~slider.(5).linexp(0, 1, 0.001, 2),
                    \pitchEnv, ~slider.(6).linexp(0, 1, 0.01, 4),
                    // \pitchMod, 20,
                    \pitchNoise, ~slider.(7).linexp(0,1,0.01,1) - 0.01,
                    
                    \shifterMix, ~knob.(0),
                    \shifterWindow, ~knob.(1),
                    \noiseEnv, ~knob.(2),
                    \pulseWidth, ~knob.(3).linlin(0,1,0.01,0.99),
                    \resonance, ~knob.(4),
                    \rotate, ~knob.(5).linexp(0,1,0.01,15) - 0.01,
                    \filterLo, 50,
                    \filterHi, 15000,
                    \out, [~bus1, ~convolve_A]
                )
            );

            Pdef(\roarFx,
                Pdef(\roar) <>
                Pbind(
                    \dur, 0.01,
                    \drive, 0.0,
                    \tone, ~pmodenv.(Pseq([-0.99, 0.75], inf), 1),
                    \toneFreq, 500.0,
                    \toneComp, 1,
                    \drywet, 1,
                    \bias, 0,
                    \filterFreq, 5000,
                    \filterLoHi, ~pmodenv.(Pseq([0, 1], inf), 0.5),
                    \filterBP, 0,
                    \filterRes, 0.3,
                    \filterBW, 0.5,
                    \filterPre, 1.0,
                    \feedAmt, 9.0,
                    \feedFreq, 50.0,
                    \feedBW, 0.1,
                    \feedDelay, 0.1,
                    \feedGate, 0.05,
                    \gain, 12.0,
                    \amp, 1.0,
                    \out, ~bus2
                )
            ).play(t);

        ~advance.wait;

            Pdef(\p2,
                ~makeSubdivision.(
                    PlaceAll([1, 1, 0.5, 2, 1], inf),
                    PlaceAll([4, 2, 4, 4], inf)
                )
            );

            Pdef(\p1,
                ~makeSubdivision.(
                    PlaceAll([1, 1, 1, 1, 1.5, 1.5, 1] * 0.5, inf),
                    PlaceAll([1, 1, 1, 1, 1, 1, 1, 1, 1, 4], inf)
                )
            );

            Pdef(\kick,
                Pdef(\kickParams) <>
                Pdef(\p1)
            ).play(t);

        ~advance.wait;

            \vosim.postln;

            Pdef(\scythe,
                Pbind(
                    \instrument, \scythe_vosimscythes,
                    \gain, -24,
                    \amp, ~slider.(0),
                    \atk, ~slider.(1).linlin(0, 1, 0, 2),
                    \rel, ~slider.(2).linlin(0, 1, 0.1, 2),
                    \freq, 47.midicps * 0.5,
                    \shifterRatio, 1,

                    // \ratios, [[1, 1, 1, 1, 1]] * 0.5,
                    \ratios, [[1.005, 1.001, 1.01, 1.001, 1.02]],
                    // \ratios, [[1, 3, 2.95, 3.97, 2]] * 0.5,
                    // \ratios, [[1.5, 2, 5, 9, 4]] * 0.5,

                    // \ratios, [[1.0, 1.5, 1.75, 2.25, 5]] * 0.5,
                    
                    \filterEnv, ~slider.(4) * 5,
                    \combEnv, ~slider.(5).linexp(0, 1, 0.001, 2),
                    \pitchEnv, ~slider.(6).linexp(0, 1, 0.01, 4),
                    \pitchNoise, ~slider.(7).linexp(0,1,0.01,1) - 0.01,
                    
                    \shifterMix, ~knob.(0),
                    \shifterWindow, ~knob.(1),
                    \noiseEnv, ~knob.(2),
                    \pulseWidth, ~knob.(3).linlin(0,1,0.01,0.99),
                    \resonance, ~knob.(4),
                    \rotate, ~knob.(5).linexp(0,1,0.01,15) - 0.01,
                    \filterLo, 50,
                    \filterHi, 15000,
                    \out, [~bus1, ~convolve_A]
                )
            );

            Pdef(\scythe,
                Pdef(\scytheParams) <>
                ~filterBeat.(key: Pkey(\cyclecount), beat:[3], mod: 3, reject: 1) <>
                Pdef(\p2)
            ).play(t);

        ~advance.wait;

            Pdef(\p1,
                ~makeSubdivision.(
                    PlaceAll([1, 1, 1, 1, 1.5, 1.5, 1] * 2, inf),
                    PlaceAll([1, 1, 1, 1, 1, 1, 1, 1, 1, 4], inf)
                )
            );

            Pdef(\p2,
                ~makeSubdivision.(
                    PlaceAll([1, 1, 0.5] * 2, inf),
                    PlaceAll([4, 2, 4, 4], inf)
                )
            );

            Pdef(\cut2,
                Pbind(
                    \instrument, \segPlayer,
                    \amp, 1,
                    \atk, 0.01,
                    \rel, Pkey(\dur) * 0.5,
                    \buf, sample3.at(\file),
                    \rate, 1,
                    \oneshot, 1,
                    \gain, -12,
                    \sliceStart, Pstep(Pseq([64, 128], inf), 3, inf),
                    \stutterPat, 1,
                    \stutterRange, Pstep(Pseq([32, 6], inf), 1, inf),
                    \slice, ~pGetSlice.((Pseries(1, Pkey(\stutterRange), inf) + Pkey(\sliceStart)), sample3).stutter(Pkey(\stutterPat)),
                    \pitchMix, 0,
                    \pitchRatio, 1,
                    \windowSize, 0.01,
                    \pitchDispersion, 0.01,
                    \timeDispersion, 0.5,
                    \pan, 0,
                    \out, [~bus2, ~convolve_B]
                )
                <> ~filterBeat.(key: Pkey(\cyclecount), beat:[3], mod: 9, reject: 1)
                <> Pdef(\p2)
            ).play(t);

            // Pdef(\cut1).stop;

        ~advance.wait;

            Pdef(\cut2).stop;

            // Pdef(\kick).stop;

        ~advance.wait;

            Pdef(\kick).stop;
            Pdef(\perc).stop;

        ~advance.wait;

            Pdef(\cut2).stop;
            
            d = Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./Silent Hill/Drum Loops/Silent Hill 3/I Want Love/LOW RIDER.wav");
            Ndef(\sample_B).set(\buf, d, \gain, 0, \rate, 0.75).play([~bus2.channels, ~miVerb.channels].flatten,);

        ~advance.wait;

            \end.postln;

            Ndef(\sample_B).stop;

            Pdef(\p3,
                ~makeSubdivision.(
                    PlaceAll([1, 1, 1, 1, 1.5, 1.5, 1] * 0.25, inf),
                    PlaceAll([1, 1, 1, 1, 1, 1, 1, 1, 1, 4], inf)
                )
            );

    }.fork;
)

(
    Pdef(\scythe).stop;
    Ndef(\sample_A).stop(fadeTime: 10);
)

Ndef(\sample_B).stop;