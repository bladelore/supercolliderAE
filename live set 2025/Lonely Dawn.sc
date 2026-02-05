(
    ~sewer = Dictionary();
    ~break = Dictionary();
    ~break2 = Dictionary();
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Field recs/Vienna Sewer 3.wav", ~sewer, 0.3, \centroid, chans: 2);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Silent Hill/Drum Loops/Silent Hill 4/Wounded Warsong/090GRAVE.WAV", ~break, 0.1, \centroid, chans: 2);
)

(
var sample = ~sewer;
var sample2 = ~break;
t = TempoClock.new(147/60).permanent_(true).schedAbs(0, {t.beatsPerBar_(4)});

    Pdef(\specSample,
        Pbind(
            \instrument, \specSlicer,
            \amp, 0.2,
            \gain, -12,
            \atk, Pwhite(4, 1),
            \rel, Pwhite(2, 4, inf),
            \rate, Prand([0.5, 1], inf),
            \oneshot, 1,
            \swap, ~pmodenv.(Pwhite(-1, 1, inf), Pkey(\dur)),
            \pan, ~pmodenv.(Pwhite(-1, 1, inf), Pkey(\dur)),
            \smooth, ~pmodenv.(Pwhite(0, 2, inf), Pkey(\dur)),
            \buf, sample.at(\file),
            \offset, Pstep(Pwhite(0, 100,inf).round, 16),
            \slice_A, ~pGetSlice.((Pseries(1, 64, inf).stutter(1) + Pkey(\offset)), sample),
            \slice_B, ~pGetSlice.((Pseries(1, 64, inf).stutter(1) + Pkey(\offset)) + 1, sample),
            \out, ~bus2,
        )
    );

    Pdef(\cut2, 
        Pbind(
            \instrument, \segPlayer,
            \amp, 1,
            \atk, 0,
            // \amp, Pkey(\groupdelta).linexp(0, 1, 1, 0.3),
            \rel, Pkey(\dur),
            // \curve, Pkey(\groupdelta).linlin(0,1, -4,0),
            \buf, sample.at(\file),
            \rate, 1,
            \oneshot, 1,
            \gain, -6,
            \sliceStart, 0,
            \slice, ~pGetSlice.((Pseries(1, 32, inf) + Pkey(\sliceStart)), sample),
            \pan, ~pmodenv.(Pwhite(-0.5, 0.5, inf), Pkey(\dur)),
            \pitchMix, 0.5,
            \pitchRatio, 0.5,
            \windowSize, 0.01,
            \pitchDispersion, 0.01,
            \timeDispersion, 0.5,
            \out, ~bus2              
        )
    );

    Pdef(\cut1,
        Pbind(
            \instrument, \segPlayer,
            \amp, 1,
            \atk, 0,
            // \amp, Pkey(\groupdelta).linexp(0, 1, 1, 0.3),
            \rel, Pkey(\dur),
            // \curve, Pkey(\groupdelta).linlin(0,1, -4,0),
            \buf, sample2.at(\file),
            \rate, 0.5,
            \oneshot, 1,
            \gain, -6,
            \sliceStart, 0,
            \slice, ~pGetSlice.((Pseries(1, 32, inf) + Pkey(\sliceStart)), sample2).stutter(4),
            \pan, ~pmodenv.(Pwhite(-0.5, 0.5, inf), Pkey(\dur)),
            \pitchMix, 0.5,
            // \pitchRatio, 2,
            \windowSize, 0.01,
            \pitchDispersion, 0.01,
            \timeDispersion, 0.5,
            \out, ~bus2              
        )
    );

    Pdef(\kick,
        Pbind(
            \instrument, \ipfPerc,
            // \dur, 1,
            \f0, 60,
            \atk, 0,
            \dec, 1,
            \alphaRate, Pkey(\groupdelta).linexp(0,10,1,20),
            \beta, Pkey(\groupdelta).linlin(0,1,1,0),
            \g_init, 1,
            \modRate, 16,
            \modStereo, 1,
            \amp, 1,
            \sweep, 16,
            \verbMix, 0.1,
            \out, ~bus2
        )
    );

    Pdef(\kick2, 
        Pbind(
            // \dur, 1,
            \instrument, Pseq([\simpleSub, \kick2], inf),
            \freq, Pseq([40.0], inf),
            \atk, 0.1,
            \dec, Pkey(\dur),
            \fb, 0.7,
            \index1, 0.1,
            \index2, 0.2,
            \ratio1, 2,
            \ratio2, 3,
            \drive, 1,
            \drivemix, 1,
            \sweep, 8.0,
            \spread, 20.0,
            \noise, Pwhite(0, 0.01),
            \feedback, 0,
            \fbmod, 1,
            \pulseWidth, 0.5,
            \lofreq, 500.0,
            \lodb, 10.0,
            \midfreq, 1200.0,
            \middb, 0.0,
            \hifreq, 7000.0,
            \hidb, 30.0,
            \gain, 0.0,
            \pan, 0.0,
            // \hpf, 
            \amp, Pkey(\groupdelta).linlin(0,1,1,0.3),
            // \amp, 1,
            \out, ~bus2
        )
    );

    Pdef(\kick3,
        Pbind(
            \instrument, \ipfPerc_phase,
            // \dur, 1,
            \f0, Pseq([60, 50], inf).stutter(9),
            \atk, 0,
            \dec, Pkey(\dur) * 2,
            \index, 0.0001,
            // \alphaRate, ~pmodenv.(Pseq([0,20]),  Pseq([2, 1, 0.5, inf]), inf, \sine),
            \beta, Pkey(\groupdelta).linlin(0,1,1,0.1),
            \alphaRate, 4,
            \alpha_start, 0.1,
            // \beta, 1,
            \g_init, 0.1,
            \modRate, 1,
            \modStereo, 1,
            \amp, Pkey(\groupdelta).linexp(0,1,1,0.3),
            \sweep, 16,
            \verbMix, 0.3,
            \out, ~bus2
        )
    );

    Pdef(\empty, Pbind(\instrument, \rest));

    ~new_advance.();
    //routine
    x = {

            \a.postln;

            b = Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./feedback cymbals/feedback cymbals-5.wav");
            Ndef(\sample,
                {
                    var sig;
                    var buf = \buf.kr(0);
                    var pos = \pos.kr(0) * BufFrames.kr(buf);
                    var harmL, percL, residualL;
                    var harmR, percR, residualR;
                    var fluid;
                    var sinesL, sinesR, sines;

                    sig = PlayBuf.ar(2, buf, startPos: pos, rate: \rate.kr(0) * BufRateScale.kr(buf), loop: \loop.kr(0), trigger: Impulse.kr(0) + Changed.kr(pos + buf));
                    # harmL, percL, residualL = FluidHPSS.ar(sig[0], 17, 31, maskingMode:1);
                    # harmR, percR, residualR = FluidHPSS.ar(sig[1], 17, 31, maskingMode:1);

                    # sinesL, residualL = FluidSines.ar(sig[0],detectionThreshold:-40,minTrackLen:15);
                    # sinesR, residualR = FluidSines.ar(sig[1],detectionThreshold:-40,minTrackLen:15);

                    fluid = [harmL, harmR];
                    sines = [sinesL, sinesR];
                    
                    sig = SelectX.ar(\source.kr(1), [sines, sig]) * \gain.kr(0).dbamp;
                });

            Ndef(\sample).set(\buf, b, \pos, 0, \rate, 1, \loop, 1, \gain, 0).play(~bus4);
            Ndef(\sample)[999] = \pset -> Pbind(\source, ~knob.(3), \dur, 0.01);
            
        ~advance.wait;    

            Pdef(\p1,
                ~makeSubdivision.(
                    PlaceAll([1, 1, 1, 1], inf),
                    PlaceAll([4, 4, 4, 4], inf)
                )
            );

            Pdef(\p2,
                ~makeSubdivision.(
                    PlaceAll([1, 0.5, 1] * 4, inf),
                    PlaceAll([4, 4, 4, 4], inf)
                )
            );

            Pdef(\chance_mod,
                    Pbind(\dur, Pfunc({|ev|
                        var val;
                        var chance = ev[\chance].coin;
                        if(chance == true) {val = ev[\dur];}{ val = Rest(ev[\dur])};
                        val;
                    })) <>
                Pbind(\chance, ~slider.(0))  
            );

            Pdef(\speed_mod,
                Pbind(\dur, Pfunc(
                {|ev|
                    var val;
                    val = ev[\dur] * ev[\speed];
                    val;
                })) <>
                Pbind(\speed, ~slider.(1).linlin(0, 1, 0.25, 16).ceil.reciprocal * 4)
            );

            Pdef(\perc,
                //map chance and speed ratio
                Pdef(\chance_mod) <>
                Pdef(\speed_mod) <>

                Pbind(\verbMix, ~slider.(2)) <>

                Pbind(\rate, ~slider.(3).linlin(0, 1, 0.5, 4)) <>
                Pbind(\f0, ~slider.(3).linexp(0, 1, 10, 120)) <>
                Pbind(\freq, ~slider.(3).linexp(0, 1, 10, 120)) <>

                Pbind(\dec, ~slider.(4).linexp(0, 1, 0.2, 2)) <>
                Pbind(\rel, ~slider.(4).linexp(0, 1, 0.2, 2)) <>

                Pswitch1([Pdef(\kick),  Pbind(\dur, 1, \dec, Pkey(\dur)) <> Pdef(\specSample), Pdef(\cut2)], PlaceAll([3, 2, 2, 1, 2, 1, 2, 1] - 1, inf)) <>
                Pdef(\p1)
            ).play(t);

        ~advance.wait;

            \b.postln;

            Pdef(\perc,
                //map chance and speed ratio
                Pdef(\chance_mod) <>
                Pdef(\speed_mod) <>

                Pbind(\verbMix, ~slider.(2)) <>

                Pbind(\rate, ~slider.(3).linlin(0, 1, 0.5, 4)) <>
                Pbind(\f0, ~slider.(3).linexp(0, 1, 10, 120)) <>
                Pbind(\freq, ~slider.(3).linexp(0, 1, 10, 120)) <>

                Pbind(\dec, ~slider.(4).linexp(0, 1, 0.2, 2)) <>
                Pbind(\rel, ~slider.(4).linexp(0, 1, 0.2, 2)) <>

                Pswitch1([Pdef(\kick),  Pbind(\dur, 1, \dec, Pkey(\dur)) <> Pdef(\kick2), Pdef(\cut2)], PlaceAll([3, 2, 2, 1, 2, 1, 2, 1] - 1, inf)) <>
                Pdef(\p1)
            ).play(t);

        ~advance.wait;

            \c.postln;
        
            Pdef(\perc).stop;

            Ndef(\droneA).clear;
            Ndef(\droneA).fadeTime = 7;
            Ndef(\droneA, {
                var chain, sig;
                var ampDrift;
                var scale = Scale.major(\partch);
                var verb_time, verb_damp;

                chain = ~initHarmonicsChain.(harmonics: 4, sidebands: 3, freq: 60);
                chain = ~padSynthDistribution.(
                    chain, 
                    harmonicRatio: 1.5,
                    bw: 5000,
                    bwScale: 1,
                    bwSkew:  0,
                    stretch: 1,
                    windowSkew: \windowSkew.kr(0.5).lag2
                );

                chain = ~notchFilter.(chain, SinOsc.ar(\filterLFO.kr(0.5)).linlin(-1,1,50,1000), 2000);
                chain = ~quantizePartials.(chain, scale, \quantize.kr(1), 60.midicps);
                chain = ~addLimiter.(chain);
            
                ampDrift = LFNoise2.ar(2 ! chain[\numPartials]) * \ampDrift.kr(0.05);

                sig = SinOsc.ar(
                    freq: chain[\freqs] * 0.25,
                    phase: ({ Rand(0, 2pi) } ! chain[\numPartials]) * SinOsc.ar(0.1).unipolar,
                    mul: chain[\amps] + ampDrift
                );
                
        
                sig = sig[0,2..].sum + ([-1,1] * sig[1,3..].sum);

                verb_time = LFNoise2.kr(0.3, 0.1, 1.03);
            
                verb_damp = LFNoise2.kr(0.2).range(0, 0.7);

                sig = MiVerb.ar(sig, verb_time, \verbMix.kr(0.1).lag, verb_damp, 0.1, mul: 0.5);

                sig = sig * -20.dbamp;

                sig = Compander.ar(sig, sig,
                    thresh: 0.5,
                    slopeBelow: 1,
                    slopeAbove: 0.1,
                    clampTime:  0.01,
                    relaxTime:  0.01
                );
            });

            Ndef(\droneA)[999] = \pset -> Pbind(
                \dur, 0.01,
                \filterLFO, ~knob.(0).linexp(0,1,0.25, 2),
                \ampDrift, ~knob.(1).linexp(0,1,0.001, 0.5) - 0.001,
                \windowSkew, ~knob.(2),
                \drive, 20,
            );

            Ndef(\droneA).mold(2).play(~bus3);

            Pdef(\fills,
                Pbind(
                    \atk, 0, 
                    \rel, ~slider.(4).linexp(0, 1, 0.2, 0.7), 
                    \rate, ~slider.(3).linlin(0, 1, 1, 3)
                ) <>
                Pdef(\specSample) <>
                ~filterBeat.(key: Pkey(\eventcount), beat:[3], mod: 3, reject: 1) <>
                ~filterBeat.(key: Pkey(\cyclecount), beat:[1, 3, 4, 5], mod: 10, reject: 1) <>
                Pdef(\p1)
            ).play(t);
        
        ~advance.wait;

            \d.postln;

            Pdef(\perc,
                Pbind(\verbMix, ~slider.(2)) <>
                Pbind(\rate, ~slider.(3).linlin(0, 1, 0.5, 4)) <>
                Pbind(\f0, ~slider.(3).linexp(0, 1, 10, 120)) <>
                Pbind(\freq, ~slider.(3).linexp(0, 1, 10, 120)) <>
                Pbind(\dec, ~slider.(4).linexp(0, 1, 0.2, 4)) <>
                Pbind(\rel, ~slider.(4).linexp(0, 1, 0.2, 4)) <>

                Pswitch1([Pdef(\kick),  Pbind(\dur, 1, \dec, Pkey(\dur)) <> Pdef(\kick2), Pdef(\cut2)], PlaceAll([3, 2, 2, 1, 2, 1, 2, 1] - 1, inf)) <>
                // ~pFade.(Pdef(\p1), Pdef(\p2), 32, 'lin', loop:1)
                Pdef(\p2)
            ).play(t);

        ~advance.wait;

            \e.postln;

            Ndef(\droneA, {
                var chain, sig;
                var ampDrift;
                var scale = Scale.major(\partch);
                var verb_time, verb_damp;

                chain = ~initHarmonicsChain.(harmonics: 3, sidebands: 3, freq: 60);
                chain = ~padSynthDistribution.(
                    chain, 
                    harmonicRatio: 1,
                    bw: 5000,
                    bwScale: 1.5,
                    bwSkew:  0,
                    stretch: 1,
                    windowSkew: 0.5
                );

                chain = ~notchFilter.(chain, SinOsc.ar(\filterLFO.kr(0.5)).linlin(-1,1,50,1000), 2000);
                chain = ~quantizePartials.(chain, scale, \quantize.kr(1), 60.midicps);
                chain = ~addLimiter.(chain);
            
                ampDrift = LFNoise2.ar(2 ! chain[\numPartials]) * \ampDrift.kr(0.05);

                sig = SinOsc.ar(
                    freq: chain[\freqs],
                    phase: ({ Rand(0, 2pi) } ! chain[\numPartials]) * SinOsc.ar(0.1).unipolar,
                    mul: chain[\amps] + ampDrift
                );
                
                sig = sig[0,2..].sum + ([-1,1] * sig[1,3..].sum);

                verb_time = LFNoise2.kr(0.3, 0.1, 1.03);
            
                verb_damp = LFNoise2.kr(0.2).range(0, 0.7);

                sig = MiVerb.ar(sig, verb_time, \verbMix.kr(0.1).lag, verb_damp, 0.1, mul: 0.5);

                sig = sig * -20.dbamp;

                sig = Compander.ar(sig, sig,
                    thresh: 0.5,
                    slopeBelow: 1,
                    slopeAbove: 0.1,
                    clampTime:  0.01,
                    relaxTime:  0.01
                );
            });

            Ndef(\droneA)[999] = \pset -> Pbind(
                \dur, 0.01,
                \filterLFO, ~knob.(0).linexp(0,1,0.25, 2),
                \ampDrift, ~knob.(1).linexp(0,1,0.001, 0.5) - 0.001,
                \windowSkew, ~knob.(2),
                \drive, 20,
            );

        ~advance.wait;

            \f.postln;

            Pdef(\fills,
                Pbind(
                    \atk, 0,
                    \rel, ~slider.(4).linexp(0, 1, 0.2, 0.7), 
                    \rate, ~slider.(3).linlin(0, 1, 1, 2)
                ) <>
                Pdef(\specSample) <>
                Pdef(\p1)
            ).play(t);

        ~advance.wait;

            \g.postln;

            Pdef(\perc,
                // Pbind(\verbMix, ~slider.(2)) <>
                // Pbind(\dec, ~slider.(3).linexp(0, 1, 0.2, 4)) <>
                // Pbind(\rel, ~slider.(4).linexp(0, 1, 0.2, 4)) <>
                Pswitch1([Pdef(\kick),  Pbind(\dur, 1, \dec, Pkey(\dur)) <> Pdef(\kick2), Pdef(\kick3)], PlaceAll([3, 2, 2, 1, 2, 1, 2, 1] - 1, inf)) <>
                Pdef(\p2)
            ).play(t);

        ~advance.wait;

            \h.postln;

            Ndef(\droneA).play;
            Ndef(\droneA, {
                var chain, sig;
                var ampDrift;
                var scale = Scale.major(\partch);
                var verb_time, verb_damp;

                chain = ~initHarmonicsChain.(harmonics: 5, sidebands: 3, freq: 60);
                chain = ~padSynthDistribution.(
                    chain, 
                    harmonicRatio: 1,
                    bw: 5000,
                    bwScale: 1,
                    bwSkew:  0,
                    stretch: 1,
                    windowSkew: \windowSkew.kr(0.5).lag2
                );

                chain = ~notchFilter.(chain, SinOsc.ar(\filterLFO.kr(0.5)).linlin(-1,1,50,1000), 2000);
                chain = ~quantizePartials.(chain, scale, \quantize.kr(1), 60.midicps);
                chain = ~addLimiter.(chain);

                ampDrift = LFNoise2.ar(2 ! chain[\numPartials]) * \ampDrift.kr(0.05);

                sig = VOSIM.ar(
                    trig: Impulse.ar(chain[\freqs] * 0.25), 
                    freq: chain[\freqs],
                    nCycles: 3, 
                    decay: 0.99,
                    mul: chain[\amps] + ampDrift
                );

                sig = sig[0,2..].sum + ([-1,1] * sig[1,3..].sum);

                verb_time = LFNoise2.kr(0.3, 0.1, 1.03);

                verb_damp = LFNoise2.kr(0.2).range(0, 0.7);

                sig = MiVerb.ar(sig, verb_time, \verbMix.kr(0.1).lag, verb_damp, 0.1, mul: 0.5);

                sig = sig * -20.dbamp;

                sig = Compander.ar(sig, sig,
                    thresh: 0.5,
                    slopeBelow: 1,
                    slopeAbove: 0.1,
                    clampTime:  0.01,
                    relaxTime:  0.01
                );
            });

            Ndef(\droneA)[999] = \pset -> Pbind(
                \dur, 0.01,
                \filterLFO, ~knob.(0).linexp(0,1,0.25, 2),
                \ampDrift, ~knob.(1).linexp(0,1,0.001, 0.5) - 0.001,
                \windowSkew, ~knob.(2),
                \drive, 20,
            );

            Pdef(\perc,
                // Pbind(\verbMix, ~slider.(2)) <>
                // Pbind(\dec, ~slider.(3).linexp(0, 1, 0.2, 4)) <>
                // Pbind(\rel, ~slider.(4).linexp(0, 1, 0.2, 4)) <>
                Pswitch1([Pdef(\kick),  Pbind(\dur, 1, \dec, Pkey(\dur)) <> Pdef(\kick2), Pdef(\kick3)], PlaceAll([3, 2, 2, 1, 2, 1, 2, 1, 3, 1] - 1, inf)) <>
                Pdef(\p2)
            ).play(t);

        ~advance.wait;

            \i.postln;

            Pdef(\fills).stop;

            Pdef(\perc,
                Pbind(\verbMix, ~slider.(2)) <>
                Pbind(\dec, ~slider.(3).linexp(0, 1, 0.2, 4)) <>
                Pbind(\rel, ~slider.(4).linexp(0, 1, 0.2, 4)) <>
                Pswitch1([Pbind(\dur, 1, \dec, Pkey(\dur)) <> Pdef(\kick), Pbind(\dur, 1, \dec, Pkey(\dur)) <> Pdef(\kick3), Pdef(\cut2)], PlaceAll([3, 2, 2, 1, 2, 1, 2, 1, 3, 3, 3, 3] - 1, inf)) <>
                Pdef(\p1)
            ).play(t);

        ~advance.wait;

            \j.postln;

            Pdef(\p3,
                ~makeSubdivision.(
                    PlaceAll([1, 1, 1, 1] * 2, inf),
                    PlaceAll([4, 4, 4, 4], inf)
                )
            );

            Pdef(\perc,
                Pbind(\verbMix, ~slider.(2)) <>
                Pbind(\dec, ~slider.(3).linexp(0, 1, 0.2, 4)) <>
                Pbind(\rel, ~slider.(4).linexp(0, 1, 0.2, 4)) <>
                Pbind(\rate, ~slider.(5).linlin(0, 1, 0.5, 4)) <>
                Pbind(\f0, ~slider.(5).linexp(0, 1, 10, 120)) <>
                Pbind(\freq, ~slider.(5).linexp(0, 1, 10, 120)) <>
                Pswitch1([Pbind(\dur, 2, \dec, Pkey(\dur)) <> Pdef(\empty),  Pbind(\dur, 1, \dec, Pkey(\dur)) <> Pdef(\kick2), Pdef(\cut2)], PlaceAll([3, 2, 2, 1, 2, 1, 2, 1, 3, 3, 3, 3] - 1, inf)) <>
                Pdef(\p3)
            ).play(t);

        ~advance.wait;
            
            \end.postln;

            Pdef(\perc).stop;

    }.fork(t);
)

(
    Ndef(\sample).stop(fadeTime: 10);
    Ndef(\droneA).stop(fadeTime: 10);
)