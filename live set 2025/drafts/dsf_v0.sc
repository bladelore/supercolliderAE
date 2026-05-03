(
SynthDef(\fftStretch_magAbove_stereo, {
    var sig;
    var buf = \buf.kr(0 ! 2);
    var bufFrames = BufFrames.kr(buf[0]);
    var analysis = \analysis.kr(0 ! 4);
    var filter = \filter.kr(20);
    var startsamp = wrap(\pos.kr(0) * bufFrames, 0, bufFrames);
    var endsamp = wrap(startsamp + (\len.kr(1) * bufFrames), 0, bufFrames);

    var chains = 2.collect { |i|
        var chain, pos;
        chain = BufFFTTrigger(analysis.copyRange(i*2, i*2+1), 0.5, [0, 1], 2);
        pos = Phasor.ar(rate: \rate.kr(1), start: startsamp, end: endsamp);
        chain = BufFFT_BufCopy(chain, buf[i], pos, BufRateScale.kr(buf[i]) * \pitchRatio.kr(1));
        chain = BufFFT(chain);
        // chain = PV_MagAbove(chain, filter);
        chain = PV_MagGate(chain, \thresh.kr(50), \remove.kr(0));
        chain = PV_Diffuser(chain, chain > (-1));
        Mix(BufIFFT(chain, 0))
    };

    sig = chains * 0.8;
    sig = sig * \gain.kr(0).dbamp;
    sig = sig * \amp.kr(1);
    sig = sig.sanitize;
    Out.ar(\out.kr(0), sig);
}).add;
)

(
    a = "/Users/aelazary/Desktop/Samples etc./matchstick burning/match impulse.wav";
    a = Buffer.read(s, a);
    AdditiveWavetable.analyse(s, a, numPartials: 64, windowSize: 512,
        action: { |wt|
        ~wt = wt;
        ~wt.loadBuffers(s);
    });
)

(
    Ndef(\additiveWavetablePad).clear;

    Ndef(\additiveWavetablePad, {
        var sig;
        var bufs = \wt.kr(0 ! 4);
        var scale = Scale.minor.tuning_(\just);

        var phase = Phasor.ar(0, BufFrames.kr(bufs[0]) / (s.sampleRate * \rate.kr(5)));
        // var phase = LFNoise2.ar(BufFrames.kr(~wt.freqBuf) / (s.sampleRate * 10)).linlin(-1, 1, 0, 1);

        sig = AdditiveReader(bufs[0], bufs[1], bufs[2], bufs[3], 12)
            .readPhase(phase, startFrame: \startFrame.kr(200), endFrame: \endFrame.kr(201))
            .transpose(\transpose.kr(-12))
            .hpFilter(LFNoise2.ar(0.1).exprange(5, 4000))
            .spectralTilt(LFNoise2.ar(0.1).range(6, -6))
            .ampSlew(\ampAtk.kr(10), \ampRel.kr(10))
            .ampAbove(\ampThresh.kr(0.6))
            .quantizePartials(scale, \quantize.kr(0.8), 30.midicps)
            .ampSlew(\ampAtk.kr(10), \ampRel.kr(10))
            .hpFilter(\hpFilter.kr(50))
            .lpFilter(\lpFilter.kr(1000))
            // .ampNormalise(-50, \flatten.kr(0))
            .limiter
            .oscBank(randomPhase: 1)
            .air(amount: 1, speed: \airSpeed.kr(0.8), min: 0.1, max: 0.9)
            .render
        ;

        sig = sig * 0.01;
        sig = sig * \amp.kr(1).lag;
        sig = sig * \gain.kr(0).dbamp;
        // sig = sig.sanitize;
    });
)



(

    ~break = Dictionary();
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./NEW sample lib/LAPerc-SampleLibrary/LAPerc-MarchingPerc.wav", ~break, 0.31, \centroid, chans: 2);

    // ~specBuff = Dictionary();
    // ~makeSpec.("/Users/aelazary/Desktop/Samples etc./Silent Hill/Ambience/Silent Hill Origins/Drowning/efxl_pad_082.wav", ~specBuff, 16384*2, 2);
    ~specBuff = SpecBuf(s, "/Users/aelazary/Desktop/Samples etc./Silent Hill/Ambience/Silent Hill Origins/Drowning/efxl_pad_082.wav", 16384*2, overlaps: 2, numChannels: 2);

    // Ndef(\dsf).clear;
    Ndef(\dsf, {
        var fb, fbAmt, fc, fm, rate, lpf, modStereo, atk, dec, tFreq, tPhase, a0, a0Curve, beta, theta, denom, numer, dispMix, env, sig, verbTime, verbDamp;
        
        fbAmt = \fb.kr(0).(linexp(0,1,0.01, 1) - 0.01).lag;
        fb = Select.ar(fbAmt > 0, [DC.ar(1 ! 2), LocalIn.ar(2) * fbAmt]);

        fc  = \fc.kr(150).lag;
        fm = \fm.kr(4890).lag2;
        tFreq = \tFreq.kr(80).lag;
        rate = \rate.kr(0.5);
        a0Curve = \curve.kr(0).lag;
        lpf = \lpf.kr(20000).lag;
        dispMix = \disperserMix.kr(0).lag;
        modStereo = \modStereo.kr(240).lag;

        fm = [fm - (modStereo * 0.5), fm + (modStereo * 0.5)];
        
        tPhase = Phasor.ar(0, SampleDur.ir / tFreq);
        a0 = Phasor.ar(0, SampleDur.ir * rate);
        
        atk = (1 / rate) * \atk.kr(0);
        dec = (1 / rate) * \dec.kr(0.9);

        env = IEnvGen.ar(
            Env([0, 1, 0], [atk * rate, dec * rate], \envCurve.kr(0)),
            a0
        );

        a0 = (1 - a0).lincurve(0, 1, 0, 0.9, a0Curve);

        a0 = LPF.ar(a0, lpf);
        
        beta  = 2pi * fm * tPhase;

        theta = Phasor.ar(0, fc / SampleRate.ir * 2pi, 0, 2pi) + (fb * 2pi);

        denom = (1 + a0.squared) - (2 * a0 * cos(beta));
        numer = (1 - a0.squared) * sin(theta - beta);

        sig = numer / (denom + 1e-10);

        sig = Disperser.ar(
            input: sig,
            freq: tFreq,
            resonance: 0,
            mix: dispMix,
            feedback: 0
        );

        sig = sig.blend(sig.distort, \distort.kr(0).lag) * 0.1;

        sig = sig * LPF.ar(env, lpf);

        sig = SelectX.ar(\pitchMix.kr(0), [sig,

        PitchShift.ar(sig,
            pitchRatio: \pitchRatio.kr(1),
            windowSize: \windowSize.kr(0.01),
            pitchDispersion: \pitchDispersion.kr(1),
            timeDispersion: \timeDispersion.kr(0.1))]
        );

        verbTime = LFNoise2.kr(0.02, 0.1, 1.03);
        verbDamp = LFNoise2.kr(0.05).range(0.0, 0.7);
        sig = MiVerb.ar(sig, verbTime, \verbMix.kr(0.01).lag, verbDamp, 0.1);

        LocalOut.ar(sig);

        sig = sig.sanitize;

        Splay.ar(sig, \spread.kr(1));
    });

)

Ndef(\additiveWavetablePad).set(\wt, ~wt.asControlInput).play(~bus1);

~specBuff.file

Slicer(\break).buffer

// 1. Is the Slicer loaded?
Slicer(\break).postln;

// 2. Is the buffer valid?
Slicer(\break).buffer.postln;

// 3. Is the dict populated?
Slicer(\break).dict.postln;

// 4. Test a slice directly
Slicer(\break).getSlice(0).postln;

90*2

k.gui;

(
    var player;
    var tempo = 75/60;

    // ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./wand recs/speaker2.wav", ~voxSlice, 0.5, \centroid, chans: 2);

    /////
    player = Conductor(\player, t);

    t.tempo = tempo;
    player.quant_(0);

    player.listen((type: \modality, device: k, key: \tr, button: \fwd));

    x = {
        player.label;

            Pdef(\cut1,
                Pbind(
                    \instrument, \segPlayer2,
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
            ).play(t);

            Pdef(\specSample,
                Pmono(\fftStretch_magAbove_stereo,
                    \dur, 0.01,
                    \amp, 1,
                    \gain, -20,
                    \buf, [~specBuff.file], \analysis, [~specBuff.analysisBufs], \fftSize, ~specBuff.fftSize,
                    \pitchRatio, 5.midiratio,
                    \len, ~knob.(3),
                    \rate, ~knob.(4),
                    \filter, 1 - ~knob.(5),
                    \pos, ~knob.(6),
                    \remove, ~knob.(7),
                    \thresh, 500,
                    \pan, ~pmodenv.(Pwhite(-0.6, 0.6), 3, 1, \sin),
                    \out, ~bus3
                )
            ).play(t);

            // Ndef(\additiveWavetablePad).set(\wt, ~wt.asControlInput).play(~bus4);
            // Ndef(\additiveWavetablePad).set(\transpose, -12, \gain, 0);
            // Ndef(\additiveWavetablePad)[999] = \pset -> Pbind(
            //     \amp, 1,
            //     \dur, 0.01, 
            //     \airSpeed, 0.5,
            //     \startFrame, 3,
            //     \endFrame, Pkey(\startFrame) + 1,
            //     \ampThresh, 0,
            //     // \flatten, 1,
            // );

            Pdef(\p1,
                ~makeSubdivision.(
                    PlaceAll([1.5, 1.5, 1] * 1, inf),
                    PlaceAll([[2, 1], 2, 2], inf)
                )
            );

            Pdef(\additivePerc,
                Pbind(
                    \wt, [~wt.asControlInput],
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
                    \slice, ~wt.pGetSlice(Pseries(0, 1, inf).wrap(0, 16)).stutter(1),
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

            // Ndef(\dsf).quant = 0;

            Ndef(\dsf).play;

            Ndef(\dsf).set(
                \fc, 50,
                \tfreq, 200
            );

            Ndef(\dsf)[999] = \pset -> Pbind(
                \amp, 1,
                \dur, 0.01,
                \atk, 0,
                \envCurve, 0,
                // \tFreq, ~slider.(0).linexp(0,1,5,20000).lag(0.1),
                // \rate, 10.68,
                \rate, tempo * Pstep([4, 2, 1, 2, 8], 2, inf),
                // \rate, tempo * 8,
                // \rate, tempo * Pstep([8, 16], 0.5, inf),
                // \rate, ~slider.(1).linexp(0.001,1,0.01,12),
                \fm, ~slider.(2).linexp(0,1,0.001,12000).lag(0.1),
                \curve, ~slider.(3).linlin(0,1,-4,4),
                \modStereo, ~slider.(4).linlin(0,1,0,1000),
                \lpf, ~slider.(5).linlin(0,1,5,20000),
                \distort, ~slider.(6).linexp(0,1,0.01,1) - 0.01,
                \fb, ~slider.(7),

                \verbMix, ~knob.(0).linexp(0,1,0.001, 1).lag(0.1) - 0.001,
                \disperserMix, ~knob.(1),

                \pitchMix, ~knob.(2),
                \pitchRatio, 2,
                \windowSize, 0.01,
                \pitchDispersion, 0.05,
                \timeDispersion, 0.1,
                // \resonance, ~knob.(3)
            );

        player.wait;

        player.label;

            // Ndef(\dsf).set(
            //     \fc, 80,
            //     \tfreq, 50,
            // );

            Ndef(\dsf)[999] = \pset -> Pbind(
                \amp, 1,
                \dur, 0.01,
                \atk, 0,
                \envCurve, 0,
                // \tFreq, ~slider.(0).linexp(0,1,5,20000).lag(0.1),
                // \rate, 10.68,
                // \rate, tempo * Pstep([4, 2, 1, 2, 8], 2, inf),
                // \rate, tempo * 8,
                \rate, tempo * Pstep([8, 16], 0.5, inf),
                // \rate, ~slider.(1).linexp(0.001,1,0.01,12),
                \fm, ~slider.(2).linexp(0,1,0.001,12000).lag(0.1),
                \curve, ~slider.(3).linlin(0,1,-4,4),
                \modStereo, ~slider.(4).linlin(0,1,0,1000),
                \lpf, ~slider.(5).linlin(0,1,5,20000),
                \distort, ~slider.(6).linexp(0,1,0.01,1) - 0.01,
                \fb, ~slider.(7),

                \verbMix, ~knob.(0).linexp(0,1,0.001, 1).lag(0.1) - 0.001,
                \disperserMix, ~knob.(1),

                \pitchMix, ~knob.(2),
                \pitchRatio, 2,
                \windowSize, 0.01,
                \pitchDispersion, 0.05,
                \timeDispersion, 0.1,
                // \resonance, ~knob.(3)
            );

        player.wait;

        player.label;

            Ndef(\dsf).set(
                \rate, 12,
                \fc, 80,
                \tfreq, 120,
            );

        player.wait;

            Ndef(\dsf).stop;

    }.fork;
)

Pdef(\voxSlice).stop;

Pdef(\voxSlice).play;


 // Ndef(\additiveWavetablePad).fadeTime = 8;

// Ndef(\additiveWavetablePad).set(\wt, ~wt.asControlInput).play(~bus1);

// Ndef(\additiveWavetablePad).set(\transpose, -12, \gain, 0);

// Ndef(\additiveWavetablePad)[999] = \pset -> Pbind(
//     \amp, 1,
//     \dur, 0.01, 
//     \airSpeed, ~knob.(5),
//     \startFrame, ~knob.(6).linlin(0,1,0,~wt.numFrames - 1),
//     \endFrame, Pkey(\startFrame) + 1,
//     \ampThresh, ~knob.(7).linlin(0,1,0.6,0.1)
// );