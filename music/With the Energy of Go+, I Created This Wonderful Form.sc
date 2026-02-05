(
{
var sig, chain, file, buf, ampDrift, source;
var lfos, combOffset, combDensity, combPeak, combSkew, warpSpectrum, inharmonicity, freq;
var harmonicRatio, ampScale, ampSkew, stretch, oddLevel, evenLevel;
var partialDrift, partialDriftFreq, partialDriftMD;
var phaseMD;
var polarity;

// file = "/Users/aelazary/Desktop/Samples etc./spirographshr/Video by spirographshr [C9X6OXxIIXK].wav";
file = "/Users/aelazary/Desktop/Samples etc./tape recs/tape in 0002 [2024-04-29 164022].aif";
// file = "/Users/aelazary/Desktop/Samples etc./spannerGuitar/spannerDrumGuitar-7.wav";

// buf = Buffer.readChannel(s, file, channels: [0]);
// source = PlayBuf.ar(1, buf, loop: 1);
source = SoundIn.ar(\in.kr(0)!2);

//dietcv modulation for comb filter
lfos = 6.collect{ |i|
    SinOsc.kr(\modMF.kr(0.1, spec: ControlSpec(0.1, 3)), Rand(0, 2pi));
};

combOffset = \combOffset.kr(0.8, spec: ControlSpec(0, 1));
combOffset = combOffset * (2 ** (lfos[0] * \combOffsetMD.kr(1, spec: ControlSpec(0, 2))));

combDensity = \combDensity.kr(0.1, spec: ControlSpec(0, 1));
combDensity = combDensity * (2 ** (lfos[1] * \combDensityMD.kr(0.1, spec: ControlSpec(0, 2))));

combPeak = \combPeak.kr(1, spec: ControlSpec(1, 10));
combPeak = combPeak * (2 ** (lfos[2] * \combPeakMD.kr(1, spec: ControlSpec(0, 2))));

combSkew = \combSkew.kr(0.5, spec: ControlSpec(0.01, 0.99));
combSkew = combSkew * (2 ** (lfos[3] * \combSkewMD.kr(1, spec: ControlSpec(0, 2))));

warpSpectrum = \warpSpec.kr(0.5, spec: ControlSpec(0, 1));
warpSpectrum = warpSpectrum * (2 ** (lfos[4] * \warpSpecMD.kr(0.1, spec: ControlSpec(0, 2))));

//generator funcs
//freq does nothing here
chain = ~initChain.(numPartials: 50, freq: 440);
//order 0 or 1
chain = ~extractSines.(chain, source, freqLag: 0.01, ampLag: 0.01, order: 1, transpose: -36);
//mod funcs
// chain = ~warpFrequencies.(chain, warpSpectrum);
// chain = ~partialDetune.(chain, amount: SinOsc.ar(0.05).linlin(-1,1,0.01, 0.3), partial_select: SinOsc.ar(0.05).linlin(-1,1,2, 8), mode: -1);
// chain = ~addCombFilter.(chain, combOffset, combDensity, combSkew, combPeak);

// chain = ~stiffString.(chain, 0.2);
// chain = ~centroid.(chain, amount: SinOsc.ar(0.05).unipolar, targetFreq: 30);


// chain = ~notchFilter.(chain, SinOsc.ar(0.5).linlin(-1,1, 15000, 1000), 10000);
// chain = ~addLimiter.(chain);
// chain = ~quantizePartials.(chain, Scale.minor);

chain = ~quantizePartials.(chain, Scale.major, 1, 30.midicps);

polarity = ({ Rand(-1, 1) } ! chain[\numPartials]);

sig = LFSaw.ar(
    freq: chain[\freqs],
    iphase: ({ Rand(0, 1) } ! chain[\numPartials]),
    mul: chain[\amps]
);

// sig = SinOsc.ar(
//     freq: chain[\freqs],
//     phase: ({ Rand(0, 2pi) } ! chain[\numPartials]) * SinOsc.ar(0.1).unipolar,
//     mul: chain[\amps]
// );

sig = sig * polarity;

//one of these
// sig = ~simplePan.(sig, chain, amount: SinOsc.ar(1).unipolar, ramp: 1);
// sig = ~autoPan.(sig, chain, amount: SinOsc.ar(1).unipolar, ramp: SinOsc.ar(3).unipolar , saw: LFSaw.ar(0.5), cycles: SinOsc.ar(0.1).unipolar);
// sig = ~stereoSpread.(sig, chain, amount: SinOsc.ar(0.5).unipolar, ramp: 0.5, saw: SinOsc.ar(3).unipolar, cycles: 1);
// sig = ~air.(sig, chain, amount: 1, speed: 0.5, min: 0.1, max: 0.9);
sig = sig[0,2..].sum + ([-1,1] * sig[1,3..].sum);

sig = sig * 0.dbamp;
}.play;
)

(
    {
        var chain, sig;
        var ampDrift;
        var randomPhase=0;
        chain = ~initHarmonicsChain.(harmonics: 8, sidebands: 4, freq: 60);
        chain = ~padSynthDistribution.(
            chain, 
            harmonicRatio: 1,
            bw: 5000,
            bwScale: 1,
            bwSkew:  0,
            stretch: 1,
            windowSkew: 0.5
        );
    
        //use one of these
        // chain = ~partialDetune.(chain, amount: 1, partial_select: 2, mode: 1);
        // chain = ~stiffString.(chain, amount: 0.1);
        // chain = ~centroid.(chain, amount: SinOsc.ar(0.05).unipolar, targetFreq: 500);
        // chain = ~reverse.(chain, amount: 0.01);
        // chain = ~highPassFilter.(chain, 20000, 2);
        chain = ~notchFilter.(chain, SinOsc.ar(0.5).linlin(-1,1,50,1000), 2000);
        chain = ~addLimiter.(chain);
    
        ampDrift = LFNoise2.ar(2 ! chain[\numPartials]) * 0.1;
        
        sig = SinOsc.ar(
            freq: chain[\freqs],
            phase: ({ Rand(0, 2pi) } ! chain[\numPartials]) * SinOsc.ar(0.1).unipolar,
            mul: chain[\amps]
        );
        
        //use one of these
        
        //simple
        // sig = ~simplePan.(sig, chain, amount: SinOsc.ar(1).unipolar, ramp: 1);
        // chain[\freqs] = chain[\freqs]+0.00001;
        //these two are quite similar
        // sig = ~autoPan.(sig, chain, amount: SinOsc.ar(1).unipolar, ramp: SinOsc.ar(3).unipolar , saw: LFSaw.ar(0.5).unipolar, cycles: SinOsc.ar(0.1).unipolar);
        // sig = ~stereoSpread.(sig, chain, amount: SinOsc.ar(0.5).unipolar, ramp: 0.5, saw: SinOsc.ar(0.9).unipolar, cycles: 1);
        
        //the best one...
        sig = ~air.(sig, chain, amount: 1, speed: 0.8, min: 0.1, max: 0.9);
        sig = Balance2.ar(sig[0], sig[1]).sum;
        // sig = sig[0,2..].sum + ([-1,1] * sig[1,3..].sum);
        sig = sig * -30.dbamp;
    }.play;
)

(
    ~getTriangle = { |phase, skew|
        phase = phase.linlin(0, 1, skew.neg, 1 - skew);
        phase.bilin(0, skew.neg, 1 - skew, 1, 0, 0);
    };
    
    ~scurve = { |x, curve|
        var v1 = x - (curve * x);
        var v2 = curve - (2 * curve * x.abs) + 1;
        v1 / v2;
    };
    
    ~sigmoidBipolar = { |x, shape|
        var shapeBipolar = shape * 2 - 1;
        var xBipolar = x * 2 - 1;
        ~scurve.(xBipolar, shapeBipolar) * 0.5 + 0.5;
    };
    
    ~sigmoidUnipolar = { |x, shape|
        var shapeBipolar = shape * 2 - 1;
        ~scurve.(x, shapeBipolar);
    };
    
    ~sigmoidBlended = { |x, shape, mix|
        var unipolar = ~sigmoidUnipolar.(x, shape);
        var bipolar = ~sigmoidBipolar.(x, shape);
        unipolar * (1 - mix) + (bipolar * mix);
    };

    ~getTrapezoid = { |phase, duty, shape|
        var offset = phase - (1 - duty);
        var steepness = 1 / (1 - shape);
        var trapezoid = (offset * steepness + (1 - duty)).clip(0, 1);
        var pulse = offset > 0;
        Select.ar(shape |==| 1, [trapezoid, pulse]);
    };

    SynthDef(\trapezoidFB, {
        var sig, impulse, fb, modEnv1, del2;
        var phase, triangle;
        var time = \freq.kr(50) - ControlRate.ir.reciprocal;
        var damp = (1 - \damp.kr(0.01));
        var damp_env;
        var gate = \gate.kr(1);
        fb = LocalIn.ar(2) * \feedback.kr(0.9);
        fb = Rotate2.ar(fb[0], fb[1], LFNoise2.ar(0.25) * \fbmod.kr(0));
        
        phase = Phasor.ar(0, time / SampleRate.ir);
        triangle = ~getTriangle.(phase, (\skew.ar(1) * 0.01 * (fb)).wrap(-1,1));
        sig = ~getTrapezoid.(triangle, \shape.kr(0), (\duty.ar(0) * 0.01 * (fb)).wrap(-1,1));
        sig = sig.linlin(0, 1, -1, 1);

        sig = AllpassC.ar(sig, 2, 1/time, \ap_fb.ar(0) * (1-fb));

        damp_env = EnvGen.ar(Env.perc(\atk.kr(0.01), \dec.kr(1), curve: -8), gate) * \dampScale.ar(0);
        sig = OnePole.ar(sig, (damp + damp_env).clip(-1,1));
        sig = sig.sanitize;
        LocalOut.ar(sig);

        sig = SelectX.ar(\drywet.kr(0.5), [
            sig, 
            GVerb.ar(sig, roomsize: \roomsize.kr(10), revtime: 1, damping: 0.5, inputbw: 0.5, spread: 15, drylevel: 1)
        ]);

        Compander.ar(sig, sig,
            thresh: 0.1,
            slopeBelow: 0.1,
            slopeAbove: 1,
            clampTime:  0.01,
            relaxTime:  0.01
        ) * 0.1;
        // sig = HPF.ar(sig, \hpf.kr(30));
        sig = sig * EnvGen.kr(Env.adsr(0, 0, 1, 1), gate, doneAction: 2);
        sig = sig * \gain.kr(0).dbamp;
        sig = sig * \amp.kr(0.4);
        Out.ar(\out.kr(0), sig);
    }).add;
)

Safety(s).disable

(
    ~extractSines_smooth = {|chain, sig, freqLag, ampLag, order, transpose, winSize=512, fftSize=4096, hopSize=4, thresh=0|

        var analysis, freqs, amps;
        
        analysis = FluidSineFeature.kr(
            sig, 
            order: order,
            numPeaks: chain[\numPartials], 
            maxNumPeaks: 50, 
            windowSize: winSize, 
            fftSize: fftSize,
            hopSize: hopSize,
        );
        
        transpose = transpose.midiratio;
    
        amps = analysis[1].lag(ampLag, ampLag);
        freqs = Latch.ar(analysis[0], amps > thresh).lag(freqLag, freqLag) + 0.00001 * transpose;
    
        chain[\freqs] = freqs;
        chain[\amps] = amps;
        chain;
    };
    
    ~quantizePartials = {|chain, scale, strength=1, baseFreq=(60.midicps)|
        var freq = chain[\freqs];
        var ratios = scale.ratios;
        var buf = ratios.as(LocalBuf);
        var octave = (freq/baseFreq).log2.floor;
        var position = IndexInBetween.kr(buf, (freq/baseFreq * (2 ** octave.neg)));
        var scaleDegree = position.round;
        var quantizedFreq = baseFreq * Index.kr(buf, scaleDegree) * (2 ** octave);
    
        var distance = (position - scaleDegree).abs;
        var scaledStrength = strength * (1 - distance);
        var outFreq = (quantizedFreq * scaledStrength) + (freq * (1-scaledStrength));
    
        chain[\freqs] = outFreq;
        chain;
    };

    Ndef(\tracking, {
        var sig, chain, file, buf, src, follower;
        var ampDrift;

        var fbIn;
        
        file = "/Users/aelazary/Desktop/Samples etc./tape recs/tape in 0002 [2024-04-29 164022].aif";
        // file = "/Users/aelazary/Desktop/Samples etc./Field recs/Candle roll on table.wav";
        // file = "/Users/aelazary/Desktop/Samples etc./Field recs/Bell birds.wav";
        buf = Buffer.readChannel(s, file, channels: [0]);
        
        //get fb
        fbIn = LocalIn.ar(2) * \feedback.kr(0);
        src = PlayBuf.ar(1, buf, loop: 1) + fbIn;
        chain = ~initChain.(numPartials: 50, freq: 440);
            
        //freq does nothing here
        //order 0 or 1
        chain = ~extractSines_smooth.(
            chain, 
            src, 
            freqLag: 0.1, 
            ampLag: 1, 
            order: 0, 
            transpose: -24, 
            winSize: 512, 
            fftSize: 1024, 
            hopSize: -1, 
            thresh: 0.001
        );

        chain = ~quantizePartials.(chain, scale: Scale.major, strength: 1);
        chain = ~addLimiter.(chain);
        
        sig = SinOsc.ar(
            freq: chain[\freqs],
            phase: ({ Rand(0, 2pi) } ! chain[\numPartials]) * SinOsc.ar(0.1).unipolar,
            mul: chain[\amps]
        );
        
        // sig = sig[0,2..].sum + ([-1,1] * sig[1,3..].sum);
        sig = ~air.(sig, chain, amount: 1, speed: 1, min: 0.1, max: 0.9);
        sig = Balance2.ar(sig[0], sig[1]).sum;
        
        sig = Compander.ar(sig, sig,
            thresh: 0.5,
            slopeBelow: 1,
            slopeAbove: 0.1,
            clampTime:  0.01,
            relaxTime:  0.01
        );
        
        sig=sig.softclip;
        sig = sig.sanitize;

        //send fb
        LocalOut.ar(sig.sanitize);
        
        sig = sig * 0.dbamp;
        sig;
    }).play;
)

(
    ~busArr = [
        ~drone_out=Bus.audio(s,2),
        ~sintracker_out=Bus.audio(s,2),
        ~guitar_out=Bus.audio(s,2),
        ~fbSynth_out=Bus.audio(s,2),    

    ]
)

(
    ~recorders = ~recordBuses.value(
        ~busArr,
        Platform.recordingsDir +/+ "With the energy of Go/%.wav"
    );
)

(
{
    var fbSynth;
    Ndef.clear;
    Ndef(\droneA).play;
    Ndef(\droneA).fadeTime = 20;
    Ndef(\sineTracker).play;

    "start".postln;
    Ndef(\droneA, {
        var chain, sig;
        var ampDrift;
        var randomPhase=0;
        chain = ~initHarmonicsChain.(harmonics: 4, sidebands: 5, freq: 50);
        chain = ~padSynthDistribution.(
            chain, 
            harmonicRatio: 1,
            bw: 5000,
            bwScale: 1,
            bwSkew:  0,
            stretch: 1,
            windowSkew: 0.5
        );
    
        chain = ~addLimiter.(chain);
    
        sig = SinOsc.ar(
            freq: chain[\freqs],
            phase: ({ Rand(0, 2pi) } ! chain[\numPartials]),
            mul: chain[\amps]
        );
        sig = ~stereoSpread.(sig, chain, amount: SinOsc.ar(0.5).unipolar, ramp: 1, saw: LFNoise2.ar(3), cycles: 1);
        // sig = ~simplePan.(sig, chain, amount: SinOsc.ar(0.2).unipolar, ramp: 1);
        
        sig = Balance2.ar(sig[0], sig[1]).sum;

        sig = sig * -15.dbamp;

        sig = Compander.ar(sig, sig,
            thresh: 0.5,
            slopeBelow: 1,
            slopeAbove: 0.1,
            clampTime:  0.01,
            relaxTime:  0.01
        );
    });

    Ndef(\sineTracker, {
        var sig, chain, file, buf, sample, src, follower;
        var partialDrift, partialDriftFreq, partialDriftMD, phaseMD;
        var ampDrift;
        var fbIn;
        
        file = "/Users/aelazary/Desktop/Samples etc./tape recs/tape in 0002 [2024-04-29 164022].aif";
        buf = Buffer.readChannel(s, file, channels: [0]);
        
        //get fb
        fbIn = LocalIn.ar(2) * \feedback.kr(0.5);
        sample = PlayBuf.ar(1, buf, loop: 1);
        src = sample + fbIn;
            
        follower = Amplitude.ar(src, 0.01, 0.1);
        //generator funcs
        //freq does nothing here
        chain = ~initChain.(numPartials: 50, freq: 440);
        //order 0 or 1
        chain = ~extractSines.(chain, src, freqLag: 0.01, ampLag: 0.1, order: 1, transpose: 0, winSize: 1024, fftSize: 4096, hopSize: 4);
        chain = ~notchFilter.(chain, SinOsc.ar(0.5).linlin(-1,1, 15000, 1000), 1000);
        
        chain = ~addLimiter.(chain);
        
        sig = SinOsc.ar(
            freq: chain[\freqs],
            phase: ({ Rand(0, 2pi) } ! chain[\numPartials]),
            mul: chain[\amps]
        );
        //one of these
        sig = ~stereoSpread.(sig, chain, amount: SinOsc.ar(0.5).unipolar, ramp: 1, saw: SinOsc.ar(3).unipolar, cycles: 1);
        sig = Balance2.ar(sig[0], sig[1]).sum;
        
        sig = Compander.ar(sig, sig,
            thresh: 0.5,
            slopeBelow: 1,
            slopeAbove: 0.1,
            clampTime:  0.01,
            relaxTime:  0.01
        );
        
        //send fb
        LocalOut.ar(sig.sanitize);

        sig = Select.ar(1, [sample!2, sig]);
        
        sig = sig * -15.dbamp;
    });

    //drone that shit
    180.wait;

    "second drone".postln;
    Ndef(\droneA, {
        var chain, sig;
        var ampDrift;
        chain = ~initHarmonicsChain.(harmonics: 4, sidebands: 9, freq: 50);
        chain = ~padSynthDistribution.(
            chain, 
            harmonicRatio: 1,
            bw: 5000,
            bwScale: 1,
            bwSkew:  0,
            stretch: 1,
            windowSkew: 0.5
        );
    
        // chain = ~notchFilter.(chain, SinOsc.ar(0.5).linlin(-1,1,50,1000), 2000);
        chain = ~addLimiter.(chain);
    
        ampDrift = LFNoise2.ar(2 ! chain[\numPartials]) * 0.1;
        sig = SinOsc.ar(
            freq: chain[\freqs],
            phase: ({ Rand(0, 2pi) } ! chain[\numPartials]) * SinOsc.ar(0.1).unipolar,
            mul: chain[\amps]
        );
        
        sig = sig[0,2..].sum + ([-1,1] * sig[1,3..].sum);

        sig = sig * -15.dbamp;

        sig = Compander.ar(sig, sig,
            thresh: 0.5,
            slopeBelow: 1,
            slopeAbove: 0.1,
            clampTime:  0.01,
            relaxTime:  0.01
        );
    });

    60.wait;

    Ndef(\droneA).fadeTime = 10;

    "spirograph 1".postln;
    Ndef(\sineTracker, {
        var sig, chain, file, buf, sample, src, follower;
        var partialDrift, partialDriftFreq, partialDriftMD, phaseMD;
        var ampDrift;
        var fbIn;

        file = "/Users/aelazary/Desktop/Samples etc./spirographshr/Video by spirographshr [C9X6OXxIIXK].wav";
        buf = Buffer.readChannel(s, file, channels: [0]);
        
        //get fb
        fbIn = LocalIn.ar(2) * \feedback.kr(0.9);
        sample = PlayBuf.ar(1, buf, loop: 1);
        src = sample + fbIn;
            
        follower = Amplitude.ar(src, 0.01, 0.1);
        //generator funcs
        //freq does nothing here
        chain = ~initChain.(numPartials: 50, freq: 440);
        //order 0 or 1
        chain = ~extractSines.(chain, src, freqLag: 0.01, ampLag: 0.01, order: 0, transpose: 0, winSize: 1024, fftSize: 4096, hopSize: 4);
        chain = ~notchFilter.(chain, SinOsc.ar(0.5).linlin(-1,1, 15000, 1000), 1000);
        chain = ~addLimiter.(chain);
        
        sig = SinOsc.ar(
            freq: chain[\freqs],
            phase: ({ Rand(0, 2pi) } ! chain[\numPartials]),
            mul: chain[\amps]
        );

        sig = ~air.(sig, chain, amount: 1, speed: 1, min: 0.1, max: 0.9);
        sig = Balance2.ar(sig[0], sig[1]).sum;

        sig = Compander.ar(sig, sig,
            thresh: 0.25,
            slopeBelow: 1,
            slopeAbove: 0.1,
            clampTime:  0.01,
            relaxTime:  0.01
        );

        LocalOut.ar(sig.sanitize);

        sig = Select.ar(1, [sample!2, sig]);
        
        sig = sig * -15.dbamp;
    });

    30.wait;

    "spirograph 2".postln;
    Ndef(\sineTracker, {
        var sig, chain, file, buf, sample, src, follower;
        var partialDrift, partialDriftFreq, partialDriftMD, phaseMD;
        var ampDrift;
        var fbIn;

        file = "/Users/aelazary/Desktop/Samples etc./spirographshr/Video by spirographshr [C9X6OXxIIXK].wav";
        buf = Buffer.readChannel(s, file, channels: [0]);
        
        //get fb
        fbIn = LocalIn.ar(2) * \feedback.kr(0.9);
        sample = PlayBuf.ar(1, buf, loop: 1);
        src = sample + fbIn;
            
        follower = Amplitude.ar(src, 0.01, 0.1);
        //generator funcs
        //freq does nothing here
        chain = ~initChain.(numPartials: 50, freq: 440);
        //order 0 or 1
        // chain = ~extractSines.(chain, src, freqLag: 0.01, ampLag: 0.01, order: 0, transpose: 0, winSize: 1024, fftSize: 4096, hopSize: 4);
        chain = ~extractSines.(chain, src, freqLag: 0.01, ampLag: 0.1, order: 1, transpose: 0, winSize: 1024, fftSize: 4096, hopSize: 4);

        chain = ~notchFilter.(chain, SinOsc.ar(0.5).linlin(-1,1, 15000, 1000), 1000);
        chain = ~addLimiter.(chain);
        
        sig = SinOsc.ar(
            freq: chain[\freqs],
            phase: ({ Rand(0, 2pi) } ! chain[\numPartials]),
            mul: chain[\amps]
        );

        sig = ~air.(sig, chain, amount: 1, speed: 1, min: 0.1, max: 0.9);
        sig = Balance2.ar(sig[0], sig[1]).sum;

        sig = Compander.ar(sig, sig,
            thresh: 0.25,
            slopeBelow: 1,
            slopeAbove: 0.1,
            clampTime:  0.01,
            relaxTime:  0.01
        );

        LocalOut.ar(sig.sanitize);

        sig = Select.ar(1, [sample!2, sig]);
        
        sig = sig * -15.dbamp;
    });

    60.wait;

    Ndef(\droneA).stop(fadeTime:30);
    Ndef(\sineTracker).fadeTime = 30;
    
    "tape 1".postln;

    Ndef(\sineTracker, {
        var sig, chain, file, buf, sample;
        file = "/Users/aelazary/Desktop/Samples etc./tape recs/tape in 0002 [2024-04-29 164022]-1.aif";
        buf = Buffer.read(s, file);
        //get fb
        sample = PlayBuf.ar(2, buf, loop: 1);
        sample * 6.dbamp;
    });

    20.wait;

    fbSynth = Synth(\trapezoidFB, [
        freq: 30, 
        damp: 0.3, 
        feedback: 1, 
        ap_fb: 1, 
        shape: 0.5,
        duty: 0.4,  
        fbmod: 1,
        drywet: 0.1,
        atk: 0.01,
        dec: 0.5,
        dampScale: 0.01,
        gain: -30
        // hpf: 30
    ]);

    120.wait;

    "prepared guitar".postln;

    Ndef(\guitar_out) <<>.in1 Ndef(\sineTracker, {
        var sig, chain, file, buf, sample;
        file = "/Users/aelazary/Desktop/Samples etc./spannerGuitar/spannerDrumGuitar-7.wav";
        buf = Buffer.read(s, file);
        
        //get fb
        sample = PlayBuf.ar(2, buf, loop: 1);
        sample * 12.dbamp;
        
        
    });

    160.wait;

    fbSynth.release(10);

    "sinetracker".postln;
    Ndef(\sineTracker, {
        var sig, chain, file, buf, src, follower;
        var ampDrift;

        var fbIn;
        
        file = "/Users/aelazary/Desktop/Samples etc./tape recs/tape in 0002 [2024-04-29 164022].aif";
        buf = Buffer.readChannel(s, file, channels: [0]);
        
        //get fb
        fbIn = LocalIn.ar(2) * \feedback.kr(0);
        src = PlayBuf.ar(1, buf, loop: 1) + fbIn;
        
        // src = Compander.ar(src, src,
        //     thresh: 0.5,
        //     slopeBelow: 1,
        //     slopeAbove: 0.1,
        //     clampTime:  0.01,
        //     relaxTime:  0.01
        // );

        chain = ~initChain.(numPartials: 50, freq: 440);
            
        //freq does nothing here
        //order 0 or 1
        chain = ~extractSines_smooth.(
            chain, 
            src, 
            freqLag: 0.1, 
            ampLag: 1, 
            order: 0, 
            transpose: -24, 
            winSize: 512, 
            fftSize: 1024, 
            hopSize: -1, 
            thresh: 0.001
        );

        chain = ~quantizePartials.(chain, scale: Scale.major, strength: 1);
        chain = ~addLimiter.(chain);
        
        sig = SinOsc.ar(
            freq: chain[\freqs ],
            phase: ({ Rand(0, 2pi) } ! chain[\numPartials]) * SinOsc.ar(0.1).unipolar,
            mul: chain[\amps]
        );
        
        sig = sig[0,2..].sum + ([-1,1] * sig[1,3..].sum);
        // sig = ~air.(sig, chain, amount: 1, speed: 1, min: 0.1, max: 0.9);
        // sig = Balance2.ar(sig[0], sig[1]).sum;
        
        sig = Compander.ar(sig, sig,
            thresh: 0.5,
            slopeBelow: 1,
            slopeAbove: 0.1,
            clampTime:  0.01,
            relaxTime:  0.01
        );
        
        sig=sig.softclip;
        sig = sig.sanitize;

        //send fb
        LocalOut.ar(sig.sanitize);
        
        // sig = sig * 12.dbamp;
        sig.softclip;
    });

}.fork(t);
)

(
        x = Synth(\trapezoidFB, [
        freq: 30, 
        damp: 0.3, 
        feedback: 1, 
        ap_fb: 1, 
        shape: 0.5,
        duty: 0.4,  
        fbmod: 1,
        drywet: 0.1,
        atk: 0.01,
        dec: 0.5,
        dampScale: 0.01,
        gain: -30
        // hpf: 30
    ]);
)

Ndef

x.release(1)