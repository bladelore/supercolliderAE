(
SynthDef(\additiveChain, { |out=0, freq=20|
    var sig, lfos;
    var combOffset, combDensity, combPeak, combSkew, warpSpectrum;

    lfos = 6.collect{ |i|
        SinOsc.kr(\modMF.kr(0.1, spec: ControlSpec(0.1, 3)), Rand(0, 2pi));
    };

    combOffset = \combOffset.kr(1, spec: ControlSpec(0, 1));
    combOffset = combOffset * (2 ** (lfos[0] * \combOffsetMD.kr(1, spec: ControlSpec(0, 2))));

    combDensity = \combDensity.kr(1, spec: ControlSpec(0, 1));
    combDensity = combDensity * (2 ** (lfos[1] * \combDensityMD.kr(1, spec: ControlSpec(0, 2))));

    combPeak = \combPeak.kr(5, spec: ControlSpec(1, 10));
    combPeak = combPeak * (2 ** (lfos[2] * \combPeakMD.kr(1, spec: ControlSpec(0, 2))));

    combSkew = \combSkew.kr(0.5, spec: ControlSpec(0.01, 0.99));
    combSkew = combSkew * (2 ** (lfos[3] * \combSkewMD.kr(1, spec: ControlSpec(0, 2))));

    warpSpectrum = \warpSpec.kr(0.5, spec: ControlSpec(0, 1));
    // warpSpectrum = warpSpectrum * (2 ** (lfos[4] * \warpSpecMD.kr(1, spec: ControlSpec(0, 2))));


    sig = AdditiveChain(120, freq) 
        .makeStretchedHarmonicSeries(0.001)
        .spectralTilt(6)
        .limiter
        // .warpFrequencies(warpSpectrum)
        // .combFilter(combOffset, combDensity, combSkew, combPeak)
        .oscBank
        
        // .simplePan(amount: SinOsc.ar(10).unipolar, ramp: 1)
        // .stereoSpread(amount: SinOsc.ar(0.5).unipolar, ramp: LFNoise2.ar(0.4), saw: LFNoise2.ar(3), cycles: LFNoise2.ar(10))
        .autoPan(amount: SinOsc.ar(1).unipolar, ramp: 1 , saw: LFSaw.ar(0.5).unipolar, cycles: SinOsc.ar(0.1).unipolar)
        // .midSideSpread(1, 1)
        // .air(amount: 1, speed: 1, min: 0.1, max: 0.9)
        // .render
        ;

        sig = sig * 0.01;

        sig.poll;

    Out.ar(out, sig);
}).add;

s.sync;

Synth(\additiveChain)
)


(
    a = "/Users/aelazary/Desktop/Samples etc./matchstick burning/match impulse.wav";
    a = Buffer.read(s, a);
    AdditiveWavetable.analyse(s, a, numPartials: 12, windowSize: 512,
        action: { |wt|
        ~wt = wt;
        ~wt.loadBuffers(s);
    });
)

(
SynthDef(\additiveWavetablePad, {
    var sig;
    var bufs = \wt.kr(0 ! 4);
    var scale = Scale.minor.tuning_(\just);

    var phase = Phasor.ar(0, BufFrames.kr(bufs[0]) / (s.sampleRate * 5));
    // var phase = LFNoise2.ar(BufFrames.kr(~wt.freqBuf) / (s.sampleRate * 10)).linlin(-1, 1, 0, 1);

    sig = AdditiveReader(bufs[0], bufs[1], bufs[2], bufs[3], 12)
        .readPhase(phase, startFrame: \startFrame.kr(200), endFrame: \endFrame.kr(201))
        .transpose(-12)
        .hpFilter(LFNoise2.ar(0.1).exprange(5, 4000))
        .spectralTilt(LFNoise2.ar(0.1).range(6, -6))
        .ampSlew(\ampAtk.kr(10), \ampRel.kr(10))
        .ampAbove(\ampThresh.kr(0.1))
        .quantizePartials(scale, \quantize.kr(0.8), 30.midicps)
        .ampSlew(\ampAtk.kr(10), \ampRel.kr(10))
        .hpFilter(500)
        .ampNormalise(-50, \flatten.kr(0))
        .limiter
        .oscBank(randomPhase: 1)
        .air(amount: 1, speed: 0.8, min: 0.1, max: 0.9)
        // .midSideSpread(1, 1, 1)
    // .render
    ;

    sig = sig * 0.01;
    sig = Balance2.ar(sig[0], sig[1], \pan.kr(0));
    sig = sig * \gain.kr(0).dbamp;
    sig = sig * \amp.kr(1);
    sig = sig.sanitize;

    Out.ar(\out.kr(0), sig);
}).add;

s.sync;

Synth(\additiveWavetablePad, [wt: ~wt.asControlInput])
)

(
a = Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./matchstick burning/match impulse.wav");
// a = Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./hollywood edge - foley sound library/FSL-03/FSL-03-Wood Staff Hits, Multiple; Multiple Light Wood Staff Impacts With Handling. - Wood Hits.wav");
AdditiveSines.analyse(s, a, numPartials: 32, windowSize: 256, slicerThreshold: 0.5, order: 0, detectionThreshold: -50,
    action: { |wt|
        ~wt = wt;
        ~wt.loadBuffers(s);
        "done".postln;
    });
)

~wt.freqBuf

(
SynthDef(\additivePerc, {
    var sig, env;
    var oneshot  = \oneshot.kr(1);
    var bufs     = \wt.kr(0 ! 4);
    var slice    = \slice.kr(0 ! 2);
    var start    = slice[0];
    var end      = slice[1];
    var rate     = \rate.kr(1);
    var durSamps = (end - start) * bufs[3] * rate;
    var durSecs  = durSamps / SampleRate.ir * rate;
    var phasor   = Phasor.ar(0, 1 / durSamps);
    var line     = Sweep.ar(1, 1 / durSecs).min(1);
    var phase    = Select.ar(oneshot, [phasor, line]);
    var resonator, resFreq, resAtk, resDec;

    var scale = Scale.major.tuning_(\just);

    sig = AdditiveReader(freqBuf: bufs[0], ampBuf: bufs[1], phaseBuf: bufs[2], hopSize: bufs[3], numPartials: 32)
        .readPhase(phase, startFrame: start, endFrame: end)
        .hpFilter(50)
        .lpFilter(\lpf.kr(2000), 4)
        .ampAbove(\ampThresh.kr(0.3))
        .quantizePartials(scale, \quantize.kr(0.8), 60.midicps)
        .ampSlew(\ampAtk.kr(0), \ampRel.kr(0))
        .ampNormalise(0)
        .spectralTilt(\tilt.kr(0))
        .transpose(\transpose.kr(0))        
        .limiter
        .oscBank
        .midSideSpread(\spread.kr(1))
    ;

    sig = sig * 0.01;
    durSecs = (\rel.kr(1) * durSecs).min(2);
    env = Env.perc(attackTime: \atk.kr(0.01), releaseTime: durSecs, level: 1.0);
    env = EnvGen.kr(env, \gate.kr(1), doneAction: oneshot.linlin(0,1,0,2));
    env = Select.kr(oneshot, [1, env]);
    sig = sig * env;

    //resonator
    resFreq = \resFreq.kr(50);
    resAtk = \resAtk.kr(0.01);
    resDec = \resDec.kr(0.25) + resAtk;
    resonator = Formlet.ar(sig * \resInGain.kr(-30).dbamp, resFreq, resAtk, resDec);
    resonator = (resonator * \resDistort.kr(1)).tanh * \resOutGain.kr(-12).dbamp * \resAmp.kr(1);
    sig = sig + resonator;

    sig = sig.sanitize;
    Out.ar(\out.kr(0), sig);
}).add;
)

(
    Pdef(\additivePerc,
        Pbind(
            \wt, [~wt.asControlInput],
            \instrument, \additivePerc,
            // \dur, Pwrand([1, 0.5, 2, 3], [1, 2, 0.25, 0.125].normalizeSum, inf),
            // \dur, Pseq([0.25, Rest(0.25), 0.5, Rest(0.25), 0.25, 0.25, 0.25, 0.75, 0.5, 0.25], inf),
            \dur, 0.25,
            \atk, 0,
            // \atk, Pwrand([0.25, 0, 0.1], [0, 1, 0.25].normalizeSum, inf),
            \rel, 1,
            \transpose, 0,
            \oneshot, 1,
            \sliceStart, Pstep([0, 5, 2], 8, inf),
            // \sliceStart, 0,
            // \ampThresh, Pseq([0.23, 0.2, 0.24], inf),
            \ampThresh, 0.3,
            \ampAtk, 1,
            \ampRel, 1,
            \rate, 1,
            \lpf, 20000,
            \tilt, 6,
            \quantize, 0,
            \spread, 1,
            \slice, ~wt.pGetSlice(Pseries(0, 1, inf).wrap(0, 16) + Pkey(\sliceStart)),
            \resFreq, 40,
            \resDistort, 1,
            \resAmp, 1,
            \resDec, 0.5,
            \out, ~bus1
            // \slice, 2
        )
    ).play(t);
)

(
b = Buffer.read(s, ExampleFiles.child);

    {
    var sig;
	var trig = Dust.kr(2) ! 4;
	
	sig = BufPlayBungee.ar(b,
		speed: {2.0.exprand(0.01)}!4,
		pitch: 0.9,
		position: TRand.kr(0,1,trig), 
		trigPos: trig
	);
	sig = Pan2.ar(sig,TChoose.kr(trig, [-1,1]));
	sig;    
}.play;
)


(
Ndef(\additiveWavetablePad).fadeTime = 8;

Ndef(\additiveWavetablePad).set(\wt, ~wt.asControlInput).play(~bus1);

Ndef(\additiveWavetablePad).set(\transpose, -12, \gain, 0);

Ndef(\additiveWavetablePad)[999] = \pset -> Pbind(
    \amp, 1,
    \dur, 0.01, 
    \airSpeed, ~knob.(5),
    \startFrame, ~knob.(6).linlin(0,1,0,~wt.numFrames - 1),
    \endFrame, Pkey(\startFrame) + 1,
    \ampThresh, ~knob.(7).linlin(0,1,0.6,0.1)
);

            Ndef(\additiveWavetablePad).set(\wt, ~wt.asControlInput).play(~bus4);
            Ndef(\additiveWavetablePad).set(\transpose, -12, \gain, 0);
            Ndef(\additiveWavetablePad)[999] = \pset -> Pbind(
                \amp, 1,
                \dur, 0.01, 
                \airSpeed, 0.5,
                \startFrame, 3,
                \endFrame, Pkey(\startFrame) + 1,
                \ampThresh, 0,
                // \flatten, 1,
            );

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
