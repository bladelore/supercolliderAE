(
    ~bus1 = Bus.new(rate: 'audio', index: 0, numChannels: 2, server: s);
    ~bus2 = Bus.new(rate: 'audio', index: 2, numChannels: 2, server: s);
    ~bus3 = Bus.new(rate: 'audio', index: 4, numChannels: 2, server: s);
    ~bus4 = Bus.new(rate: 'audio', index: 6, numChannels: 2, server: s);
)

(
    SynthDef(\input, {
        var sig;
        sig = SoundIn.ar(\in.kr(0)!2);
        sig = sig * \gain.kr(1).dbamp;
        sig = sig * \amp.kr(1);
        Out.ar(\out.kr(0), sig);
    }).add;
)

//spectral grains guitar
(
    ~maxGrains = 25;
    ~fftSize = 4096*8;
    ~bufA = Buffer.alloc(s, ~fftSize);
    ~bufB = Buffer.alloc(s, s.sampleRate * 0.01);
    ~bufC = Buffer.alloc(s, s.sampleRate * 0.025);
    ~specBuf = Array.fill(~maxGrains, {Buffer.alloc(s, ~fftSize)});
    ~specBuf.do{|item| item.zero};
)

(
SynthDef(\spectralGrains1, {|srcbuf, fftSize=4096|
    var numChannels = 25;
    var reset, events, voices, grainWindows;
    var overlap, overlapMod, tFreq, tFreqMod, posRate, posRateMod, pitchRatio, pitchMod;
    var trig, pos, chain, accumChain;
    var polarity, sig;
    var feedback, in, ptr, prev, current, fbOut;

    var freq, wipe, offset, low, high;
    var fbBuf, delayBuf;
    var modulator;
    
    feedback = (LocalIn.ar(1) * \feedback.ar(0) * 0.9);
	in = InFeedback.ar(\inbus.kr(0), 1);

    ptr = Phasor.ar(0, \recRate.kr(1), 0, BufFrames.kr(srcbuf));
	prev = BufRd.ar(1, srcbuf, ptr);
	current = XFade2.ar(in + feedback, prev, \overdub.kr(0));
	BufWr.ar(current, srcbuf, ptr);
    
    reset = Trig1.ar(\reset.tr(0), SampleDur.ir);

    tFreqMod = LFDNoise3.ar(\tFreqMF.kr(1));
    tFreq = \tFreq.kr(10) * (2 ** (tFreqMod * \tFreqMD.kr(0)));

    events = SchedulerCycle.ar(tFreq, reset);

    overlapMod = LFDNoise3.ar(\overlapMF.kr(2));
    overlap = \overlap.kr(1) * (2 ** (overlapMod * \overlapMD.kr(0)));

    voices = VoiceAllocator.ar(
        numChannels: numChannels,
        trig: events[\trigger],
        rate: events[\rate] / overlap,
        subSampleOffset: events[\subSampleOffset],
    );

    grainWindows = TukeyWindow.ar(
        phase: voices[\phases],
        skew: \windowSkew.kr(0.5),
        width: \windowWidth.kr(0.5)
    );

    posRateMod = LFDNoise3.ar(\posRateMF.kr(0.3));
    posRate = \posRate.kr(0.1) * (1 + (posRateMod * \posRateMD.kr(0)));

    pos = Phasor.ar(
        trig: DC.ar(0),
        rate: posRate * BufRateScale.kr(srcbuf) * SampleDur.ir / BufDur.kr(srcbuf),
        start: \posLo.kr(0),
        end: \posHi.kr(1)
    );

    pos = Latch.ar(pos, voices[\triggers]) * BufFrames.kr(srcbuf);

    pitchMod = LFDNoise3.ar(\pitchMod.kr(1)) * \pitchModDepth.kr(0);
    pitchRatio = (\midipitch.kr(0) + pitchMod).midiratio;
    
    //fft
    chain = BufFFTTrigger2(\specbuf.kr(0 ! numChannels), voices[\triggers]);
    chain = BufFFT_BufCopy(chain, srcbuf, pos, BufRateScale.kr(srcbuf) * pitchRatio);
    chain = BufFFT(chain, wintype: 0);
    
    // chain = PV_Cutoff(chain, \cutoff.kr(0));
    chain = PV_Diffuser(chain, chain>(-1));

    chain = PV_RectComb(
        chain,
        \num_teeth.kr(32),
        // TRand.kr(0, 32, voices[\triggers]),
        // TRand.kr(0, 1, voices[\triggers]),
        \comb_phase.kr(0), 
        \comb_width.kr(0),
        // TRand.kr(0, 1, voices[\triggers]), 
    );

    chain = PV_MagAbove(chain, \spectralFilter.kr(0.1).linexp(0.001, 1, 0.001, 300));

    chain = PV_Compander(chain, TExpRand.kr(0.001, \companderMD.kr(10), voices[\triggers]), 0.1, 1.0);
    
    accumChain = LocalBuf(fftSize);
    accumChain = PV_AccumPhase(accumChain, chain);
    accumChain = PV_BinGap(
        accumChain,
        TRand.kr(0, 1000, voices[\triggers]),
        TRand.kr(0, 1000, voices[\triggers])
    );
    chain = PV_CopyPhase(chain, accumChain);
    
    sig = BufIFFT(chain, wintype: 0);
    
    //set polarity w polarityMod 0-1
    polarity = ~multiVelvet.(voices[\triggers], \density.kr(1), 1 - \polarityMod.kr(1));
    sig = sig * grainWindows * polarity;
    sig = Mix(Pan2.ar(sig, TRand.kr(-1, 1, voices[\triggers])));

    // overdub feedback
	fbOut = sig.sum * 0.5;
	fbOut = LeakDC.ar(fbOut, 0.995);
	LocalOut.ar(fbOut);

	sig = sig * \gain.kr(0).dbamp;
	sig = sig * \amp.kr(1);
    sig = sig.softclip;
	Out.ar(\out.kr(0), sig);
}).add;
)

(
Pdef(\player,
    Pspawner({| sp |
        
        var sectionLength, sample, drum;
            
            sp.par(
                Pdef(\guitar,
                    Pmono(\input,
                        \in, 0,
                        \amp, 1,
                        \dur, 0.1,
                        \gain, -12,
                        \out, [~spectralGrains, ~convolve_A],
                    )
                )
            );

            sp.par(
                Pbindf(
                    Pdef(\spectralGrains1),
                    \srcbuf, ~bufA,
                    \specbuf, [~specBuf],
                    \fftSize, ~fftSize,
                    \amp, 1,
                    \dur, 0.1,
                    // \posRate, 0.01,
                    \posRate, ~knob.(0).linlin(0,1,0.01,1),
                    \tFreq, ~knob.(1).clip(0.001,1).linexp(0.001,1,10,1000),
                    // \cutoff, ~knob.(2).linlin(0,1,-0.5,0.25), 
                    \spectralFilter, 1 - ~knob.(2),
                    \num_teeth, ~knob.(3).linlin(0,1,1,32),

                    \comb_phase, ~knob.(4),
                    \comb_width, ~knob.(5),
                    
                    \windowSkew, ~slider.(0),
                    
                    // \posRateMD, ~slider.(0).linlin(0,1,0,10),

                    // \tFreq, 10,
                    
                    // \tFreqMD, ~slider.(1).linlin(0,1,0,10),

                    // \overlap, ~knob.(2).clip(0.001,1).linexp(0.001,1,1,36),
                    // \overlapMD, ~slider.(2).linlin(0,1,0,10),
                    \overlap, 4,

                    
                    \companderMD, ~knob.(6).linexp(0,1,0.01,10),
                    

                    \overdub, ~knob.(7),
                    \midipitch, -12,

                    \gain, 24,
                    \out, ~convolve_B
                )
            );

            sp.par(
                Pbind(
                    \gain, 0,
                    \atk, 10,
                    \rel, 100,
                    // \swap, ~pmodenv.(Pseq([1, 0],inf), Pseq([2], inf), 1, \sine),
                    \swap, ~slider.(7),
                    \out, [~bus1]
                ) <>
                Pdef(\morph)
            );
            
    })).play(t);
)

PV_SpectralMap

//Sine tracked cymbals fixed
(
    Ndef(\sineTracker).play;
    Ndef(\sineTracker, {
        var sig, chain, file, buf, sample, src, follower;
        var partialDrift, partialDriftFreq, partialDriftMD, phaseMD;
        var ampDrift;
        var fbIn;
        
        file = "/Users/aelazary/Desktop/Samples etc./feedback cymbals/feedback cymbals-4.wav";
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
        chain = ~extractSines.(chain, src, freqLag: 0.01, ampLag: 0.1, order: 1, transpose: -12, winSize: 1024, fftSize: 4096, hopSize: 4);
        // chain = ~notchFilter.(chain, SinOsc.ar(0.5).linlin(-1,1, 15000, 1000), 1000);
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
        
        sig = sig * 0.dbamp;
    }).set(\out, ~bus2);
)

//Sine tracked cymbals controller
(
    Ndef(\s1).play;
    (
    Ndef(\s1, {
        var sig, chain, file, buf, sample, src, follower;
        var partialDrift, partialDriftFreq, partialDriftMD, phaseMD;
        var ampDrift;
        var fbIn;
        var octaveSelect;

        file = "/Users/aelazary/Desktop/Samples etc./feedback cymbals/feedback cymbals-4.wav";
        buf = Buffer.readChannel(s, file, channels: [0]);
        
        //get fb
        fbIn = LocalIn.ar(2) * \feedback.kr(0.9);
        sample = PlayBuf.ar(1, buf, loop: 1);
        src = sample + fbIn;
            
        follower = Amplitude.ar(src, 0.01, 0.1);
        //generator funcs
        chain = ~initChain.(numPartials: 50, freq: 440);
        //order 0 or 1
        // chain = ~extractSines.(chain, src, freqLag: \freqLag.kr(0.01), ampLag: \ampLag.kr(1), order: 1, transpose: 0, winSize: 1024, fftSize: 4096, hopSize: 4);
        octaveSelect = Select.kr(\octave.kr(0).range(0, 4), [-48, -36, -24, -12, 0]).lag(1);
        chain = ~extractSines_smooth.(chain, src, freqLag: \freqLag.kr(0.01), ampLag: \ampLag.kr(1), order: 0, transpose: octaveSelect, thresh: \thresh.kr(0));
        // chain = ~notchFilter.(chain, SinOsc.ar(0.5).linlin(-1,1, 15000, 1000), 1000);
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

    Ndef(\s1)[999] = \pset -> Pbind(
        \dur, 0.01, 
        \amp, ~slider.(0).lag(0.1),
        \ampLag, ~slider.(1).linexp(0,1,0.001,1),
        \freqLag, ~slider.(2),
        \quantize, ~slider.(3),
        \feedback, ~slider.(4),
        \octave, ~slider.(5),
        \lpf, ~slider.(6),
        \thresh, ~slider.(7),
    );

    )
)

//IPF
(
//IPF multiphonics A
    Ndef(\ipf).play;
    Ndef(\ipf,{
        var trig, g_prev, g, su, safeVal, g_out, md, freq, sig;
        var verb_time, verb_damp;
        
        var f0 = \f0.kr(60);
        var alpha = SinOsc.ar(\alphaRate.kr(0.5)).linlin(-1, 1, 0.01, 9);
        var beta = \beta.kr(0.3);
        var g_init = \g_init.kr(0.6);
        var modRate = \modRate.kr(1);
        var modStereo=\modStereo.kr(100);

        trig = Impulse.ar(f0);
        g_prev = LocalIn.ar(2);
        g = Select.ar((trig > 0), [K2A.ar(g_init), g_prev]);

        su = beta * exp(g - g_prev);
        safeVal = ((g - su) / alpha).max(0.00001);
        g_out = g - log(safeVal);

        LocalOut.kr(g_out);

        // md = [modRate, modRate + modStereo];
        md = [modRate - (modStereo * 0.5), modRate + (modStereo * 0.5)];
        freq = f0 / (1 + g_out * md).max(0.001);

        sig = SinOsc.ar(freq);

        sig = sig * g_out;
        sig = sig.tanh;
        sig = sig.sanitize;

        verb_time = LFNoise2.kr(0.3, 0.1, 1.03);
        
        verb_damp = LFNoise2.kr(0.2).range(0, 0.7);

        sig = MiVerb.ar(sig, verb_time, \verbMix.kr(0.1).lag, verb_damp, 0.1, mul: 0.5);

        sig = Compander.ar(sig, sig,
            thresh: 1,
            slopeBelow: 1,
            slopeAbove: 1,
            clampTime:  0.01,
            relaxTime:  0.01
        );

        sig = sig * \amp.kr(1).lag;
    });

    Ndef(\ipf)[999] = \pset -> Pbind(
        \dur, 0.01,
        \f0, ~slider.(0).linexp(0,1,30,20000),
        \alphaRate, ~slider.(1),
        \beta, ~slider.(2),
        \g_init, ~slider.(3),
        \modRate, ~slider.(4).linexp(0.001,1,1,1000),
        \modStereo, ~slider.(5).linexp(0.001,1,0.001,1000),
        \amp, ~knob.(0).lag(0.01),
        \verbMix, ~knob.(1).lag(2)
    ).trace;
)

(
    //IPF multiphonics B
    Ndef(\ipf).play;
    Ndef(\ipf, {

        var trig, g_state, g_out, g_prev, dg, md;
        var su, safeVal;
        var alpha;
        var op1, op2, phaseShift;
        var sig;
        var verb_time, verb_damp;

        var f0 = \f0.kr(60);
        var alpha_start = \alphaStart.kr(0.1);
        var alpha_end = \alphaEnd.kr(1);
        var alpha_rate = \alphaRate.kr(1);
        var beta = \beta.kr(0.01);
        var modRate = \modRate.kr(10);
        var modStereo=\modStereo.kr(100);
        var index=\index.kr(0.01);

        trig = Impulse.ar(f0);
        g_state = LocalIn.ar(2); // [current g_out, previous g_out]
        g_prev = g_state[1];

        alpha = LFSaw
            .ar(alpha_rate)
            .linexp(-1, 1, alpha_start.max(0.001), alpha_end);

        su = beta * exp(g_state[0] - g_prev);
        safeVal = ((g_state[0] - su) / alpha).max(0.00001);
        g_out = g_state[0] - log(safeVal);

        dg = abs(g_out - g_prev);

        // md = [modRate, modRate + modStereo];
        md = [modRate - (modStereo * 0.5), modRate + (modStereo * 0.5)];
        phaseShift = dg.abs * md / f0;

        // op1 = SinOsc.ar(f0, phaseShift * 2pi * f0) * g_out * index;
        op1 = VarSaw.ar(f0, 0, phaseShift * 2pi * f0) * g_out * index;

        op2 = SinOsc.ar(f0 * op1);
        // op2 = Blip.ar(f0 * (1 + op1), 5); // bandlimited pulse train
        
        sig = op2;
        sig = sig.tanh;
        sig = sig.sanitize;

        LocalOut.ar([g_out, g_state[0]]);

        verb_time = LFNoise2.kr(0.3, 0.1, 1.03);
        verb_damp = LFNoise2.kr(0.2).range(0, 0.7);
        sig = MiVerb.ar(sig, verb_time, \verbMix.kr(0.1).lag, verb_damp, 0.1);
        sig = Compander.ar(sig, sig,
            thresh: 1,
            slopeBelow: 1,
            slopeAbove: 1,
            clampTime:  0.01,
            relaxTime:  0.01
        );
        
        sig = sig * \amp.kr(1).lag;
        sig;
    });

    Ndef(\ipf)[999] = \pset -> Pbind(
        \dur, 0.01,
        \f0, ~slider.(0).linexp(0,1,30,20000).lag(0.1),
        \index, ~slider.(1).linexp(0,1,0.001,1).lag(0.1),
        \alphaRate, ~slider.(2).linexp(0.001,1,0.001,1000),
        \alphaStart, ~slider.(3).clip(0.001,1),
        \alphaEnd, ~slider.(4).clip(0.001,1),
        \beta, ~slider.(5).linexp(0,1,0.001,1),
        \modRate, ~slider.(6).linlin(0.001,1,1,1000),
        \modStereo, ~slider.(7).linlin(0.001,1,0.001,1000),
        \amp, ~knob.(0).linlin(0,1,0,1),
        \verbMix, ~knob.(1).lag(0.1)
    );
)

CondVar

(
// IPF normalized-beta version
Ndef(\ipf).play;

Ndef(\ipf, {
    var trig, g_state, g_out, g_prev, dg, md;
    var su, safeVal;
    var alpha;
    var op1, op2, phaseShift;
    var sig;
    var verb_time, verb_damp;

    var f0         = \f0.kr(4000);
    var alpha_start= \alphaStart.kr(0.1);
    var alpha_end  = \alphaEnd.kr(0.5);
    var alpha_rate = \alphaRate.kr(0.01);
    var beta       = \beta.kr(0.0001);
    var modRate    = \modRate.kr(1);
    var modStereo  = \modStereo.kr(100);
    var index      = \index.kr(0.01);

    // --- recurrence system ---
    trig = Impulse.ar(f0);
    g_state = LocalIn.ar(2); 
    g_prev = g_state[1];

    alpha = LFSaw.ar(alpha_rate).linexp(-1, 1, alpha_start.max(0.001), alpha_end);

    su = (beta / (1 + beta)) * exp(g_state[0] - g_prev);

    safeVal = ((g_state[0] - su) / alpha).max(1e-6);
    g_out = g_state[0] - log(safeVal);

    dg = (g_out - g_prev).abs;

    // md = [modRate, modRate + modStereo];
    md = [modRate - (modStereo * 0.5), modRate + (modStereo * 0.5)];
    phaseShift = dg * md / f0;

    op1 = SinOsc.ar(f0, phaseShift * 2pi * f0) * g_out * index;
    op2 = SinOsc.ar(f0 * op1);

    sig = op2.tanh.sanitize;

    LocalOut.ar([g_out, g_state[0]]);

    verb_time = LFNoise2.kr(0.3, 0.1, 1.03);
    verb_damp = LFNoise2.kr(0.2).range(0, 0.7);
    sig = MiVerb.ar(sig, verb_time, \verbMix.kr(0.1), verb_damp, 0.1);
    sig = Compander.ar(sig, sig,
        thresh: 1,
        slopeBelow: 1,
        slopeAbove: 1,
        clampTime: 0.01,
        relaxTime: 0.01
    );

    sig = sig * \amp.kr(1).lag2(0.1);
    sig;
});

Ndef(\ipf)[999] = \pset -> Pbind(
    \dur, 0.01,
    \f0, ~slider.(0).linexp(0,1,30,20000).lag(0.1),
    \index, ~slider.(1).linexp(0,1,0.001,1).lag(0.1),
    \alphaRate, ~slider.(2).linexp(0.001,1,0.001,1000),
    \alphaStart, ~slider.(3).clip(0.001,1),
    \alphaEnd, ~slider.(4).clip(0.001,1),
    \beta, ~slider.(5).linexp(0,1,0.001,1),
    \modRate, ~slider.(6).linlin(0.001,1,1,1000),
    \modStereo, ~slider.(7).linlin(0.001,1,0.001,1000),
    \amp, ~knob.(0),
    \verbMix, ~knob.(1)
);

)

(
// IPF with memory kernel
Ndef(\ipf).play;

Ndef(\ipf, {
    var memsize = 8;
    var g_state, g_out, g_prev, dg, md;
    
    var su, safeVal;
    var alpha;
    var op1, op2, phaseShift;
    var sig;
    var verb_time, verb_damp;
    var betas, eps, k;

    var f0         = \f0.kr(50);
    var alpha_start= \alphaStart.kr(0.1);
    var alpha_end  = \alphaEnd.kr(1);
    var alpha_rate = \alphaRate.kr(0.1);
    var beta       = \beta.kr(10);
    // var beta     = SinOsc.ar(\beta.kr(0.5)).linexp(-1, 1, 0.01, 1);
    var modRate    = \modRate.kr(100);
    var modStereo  = \modStereo.kr(10);
    var index      = \index.kr(0.01);
    var tau        = \tau.kr(0.2);
    var g_init     = \g_init.kr(0.3);

    g_state = LocalIn.ar(memsize);

    g_state = Array.fill(memsize, { |i|
        var delayTime = ControlDur.ir + (i / SampleRate.ir);
        if (i == 0) { g_state[i] } { DelayN.ar(g_state[i], delayTime, delayTime)}
    });

    g_prev = g_state[1];

    alpha = LFSaw.ar(alpha_rate).linexp(-1, 1, alpha_start.max(0.001), alpha_end);

    su = 0;
    (g_state.size - 1).do { |i|
        k = exp((i + 1).neg / tau);
        su = su + beta * k * exp(g_state[0] - g_state[i + 1]);
    };

    eps = 1e-6;
    safeVal = ((g_state[0] - su) / alpha).max(eps);
    g_out = g_state[0] - log(safeVal);

    dg = (g_out - g_prev).abs;
    md = [modRate - (modStereo * 0.5), modRate + (modStereo * 0.5)];
    phaseShift = dg * md / f0;

    op1 = SinOsc.ar(f0, phaseShift * 2pi * f0) * g_out * index;
    op2 = SinOsc.ar(f0 * op1);
    sig = op2.sanitize;

    LocalOut.ar([g_out * \fb.kr(0).lag2(0.1)] ++ g_state[0..(g_state.size - 2)]);

    verb_time = LFNoise2.kr(0.3, 0.1, 1.03);
    verb_damp = LFNoise2.kr(0.2).range(0, 0.7);
    sig = MiVerb.ar(sig, verb_time, \verbMix.kr(0.1).lag2(0.1), verb_damp, 0.1);
    sig = Compander.ar(sig, sig,
        thresh: 1,
        slopeBelow: 1,
        slopeAbove: 1,
        clampTime: 0.01,
        relaxTime: 0.01
    );

    sig = sig * \amp.kr(1).lag2(0.1);
    sig;
});

Ndef(\ipf)[999] = \pset -> Pbind(
    \dur, 0.01,
    \f0, ~slider.(0).linexp(0,1,30,40000).lag(0.1),
    \index, ~slider.(1).linexp(0,1,0.001,1).lag(0.1),
    \alphaRate, ~slider.(2).linexp(0.001,1,0.001,1000),
    \alphaStart, ~slider.(3).clip(0.001,1),
    \alphaEnd, ~slider.(4).clip(0.001,1),
    \beta, ~slider.(5).linexp(0.001,1,0.001,100),
    \modRate, ~slider.(6).linlin(0.001,1,1,1000),
    \modStereo, ~slider.(7).linexp(0.001,1,0.001,1),
    \amp, ~knob.(0),
    \verbMix, ~knob.(1),
    \tau, ~knob.(2).linexp(0,1,0.001,1),
    \fb, ~knob.(3),
);
)

(
// IPF with state-dependent alpha
Ndef(\ipf).play;

Ndef(\ipf, {
    var trig, g_state, g_out, g_prev, dg, md;
    var su, safeVal;
    var alpha;
    var op1, op2, phaseShift;
    var sig;
    var verb_time, verb_damp;

    // --- parameters ---
    var f0        = \f0.kr(4000);
    var alpha0    = \alpha0.kr(0.02);
    // var alpha0 = SinOsc.ar(\alpha0.kr(0.5)).linexp(-1, 1, 0.01, 9);
    // var gamma     = \gamma.kr(1);
    var gamma     = SinOsc.ar(\gamma.kr(0.5)).linexp(-1, 1, 0.01, 1);
    var beta      = \beta.kr(0.0001);
    // var beta     = SinOsc.ar(\beta.kr(0.0001)).linexp(-1, 1, 0.0001, 1);
    var modRate   = \modRate.kr(1);
    var modStereo = \modStereo.kr(100);
    var index     = \index.kr(0.005);

    // --- trigger per cycle ---
    trig = Impulse.ar(f0);

    // --- hold [current, previous] g values ---
    g_state = LocalIn.ar(2);
    g_prev = g_state[1];


    // --- state-dependent alpha ---
    alpha = (alpha0 * (1 + (gamma * g_state[0]))).max(1e-4);

    // --- normalized beta recurrence ---
    su = (beta / (1 + beta)) * exp(g_state[0] - g_prev);
    safeVal = ((g_state[0] - su) / alpha).max(1e-6);
    g_out = g_state[0] - log(safeVal);

    // --- phase shift modulation ---
    dg = (g_out - g_prev).abs;
    md = [modRate - (modStereo * 0.5), modRate + (modStereo * 0.5)];
    phaseShift = dg * md / f0;

    // --- oscillators ---
    op1 = SinOsc.ar(f0, phaseShift * 2pi * f0) * g_out * index;
    op2 = SinOsc.ar(f0 * op1);

    sig = op2.tanh.sanitize;

    // --- update state ---
    LocalOut.ar([g_out, g_state[0]]);

    // --- verb & dynamics ---
    verb_time = LFNoise2.kr(0.3, 0.1, 1.03);
    verb_damp = LFNoise2.kr(0.2).range(0, 0.7);
    sig = MiVerb.ar(sig, verb_time, \verbMix.kr(0.1), verb_damp, 0.1);

    sig = Compander.ar(sig, sig,
        thresh: 1,
        slopeBelow: 1,
        slopeAbove: 1,
        clampTime: 0.01,
        relaxTime: 0.01
    );

    sig = sig * \amp.kr(1);
    sig;
});

Ndef(\ipf)[999] = \pset -> Pbind(
    \dur, 0.01,
    \f0, ~slider.(0).linexp(0,1,30,20000).lag(2),
    \index, ~slider.(1).linexp(0,1,0.0001,1).lag(2),
    \alpha0, ~slider.(2).linexp(0,1,0.001,1).lag(2),
    \gamma, ~slider.(3).linexp(0,1,0.001,5).lag(2),
    \beta, ~slider.(4).linexp(0.001,1,0.00001,1).lag(2),
    \modRate, ~slider.(5).linlin(0.001,1,0.1,1000),
    \modStereo, ~slider.(6).linlin(0.001,1,0,1),
    \amp, ~knob.(0).linexp(0,1,0.01,1).lag(2),
    \verbMix, ~knob.(1).lag(2)
).trace;
)

(
Ndef(\gutter).clear;

Ndef(\gutter, {
    var sig, freq1, freq2, pitch;
    var mod, omega, damp, rate, gain, soften, gain1, gain2, q1, q2;
    var chain, src;
    var verb_time, verb_damp;
    var envFollower;

    src = SoundIn.ar(0!2).sanitize;
    envFollower = Amplitude.kr(src, attackTime: 0.1, releaseTime: 0.2, mul: 1.0, add: 0.0);

    mod = \mod.kr(0.2, spec:[0,10]);
    omega = \omega.kr(0.0002, spec:ControlSpec(0.0001, 1, \exponential));
    damp = \damp.kr(0.01, spec:ControlSpec(0.0001, 1, \exponential));
    rate = \rate.kr(0.03, spec:[0, 5]);
    gain = \gain.kr(1.4, spec:[0, 3.5]);
    soften = \soften.kr(1, spec:[0, 5]);
    gain1 = \gain1.kr(1.5, spec:[0.0, 2.0, \lin]);
    gain2 = \gain2.kr(1.5, spec:[0.0, 2.0, \lin]);
    q1 = \q.kr(20, spec:ControlSpec(2.5, 800, \exponential)).lag3(1);

    // freq = [56, 174, 194, 97, 139, 52, 363, 118, 353, 629];
    pitch = \pitchShift.kr(0.25, spec: [0.05,2.0]).lag;

    chain = ~initChain.(numPartials: 24, freq: 440);
    // chain = ~extractSines.(chain, src, freqLag: \freqLag.kr(0.01), ampLag: \ampLag.kr(1), order: 1, transpose: 0, winSize: 1024, fftSize: 4096, hopSize: 4, numSines: 24);
    chain = ~extractSines_smooth.(chain, src, freqLag: \freqLag.kr(0.01), ampLag: \ampLag.kr(1), order: 1, transpose: 0, thresh: envFollower, numSines: 24);

    // chain = ~extractSines_smooth.(chain, src, freqLag: \freqLag.kr(0.01), ampLag: \ampLag.kr(1), order: 0, transpose: 0, thresh: \thresh.kr(0), numSines: 24);

    chain = ~addLimiter.(chain);
    freq1 = chain[\freqs] * pitch;
    // freq1 = freq1.clip(20, 20000);
    gain1 = chain[\amps] * gain1;
    // freq1.poll;

    // freq1 = pitch * [ 104.08913805616, 272.0241439869, 142.5394121681, 740.98235420089, 3231.1092775615, 598.48984613932, 564.11122601617, 152.53849023618, 4773.6198870775, 798.26171948236, 729.54452005837, 734.37542510625, 661.89936380362, 133.46101940276, 1715.6115033359, 11658.962024239, 6408.5610397899, 11775.302108311, 857.52846512925, 2020.251581889, 14168.220304686, 192.17654523236, 326.55730188427, 4386.8490423436];
    freq2 = (freq1 * Array.rand(freq1.size, 0.95,1.0)).clip(0, 20000);

    // q = q ! freq1.size;
    // q1 = chain[\amps] * q1;
    q1 = Array.rand(freq1.size, 0.95,1.0) * q1;
    q2 = Array.rand(freq1.size, 0.95,1.0) * q1;

    q1=q1.max(10);
    q2=q2.max(10);

    sig = GutterSynth.ar(
        gamma:         mod,
        omega:         omega,
        c:             damp,
        dt:         rate,
        singlegain: gain,
        smoothing:  soften,
        togglefilters: 1,
        distortionmethod: \distortionmethod.kr(1, spec: [0,4,\lin,1]),
        oversampling: 2,
        enableaudioinput: 0,
        // audioinput: src.exprange(100.0,2500.0),
        audioinput: src,
        // audioinput: SinOsc.ar(SinOsc.ar(LFNoise2.ar(30)*100).exprange(100.0,2500.0)),
        gains1:     gain1,
        gains2:     gain2,
        freqs1:     `freq1,
        qs1:         `q1,
        freqs2:     `freq2,
        qs2:         `q2,
    );

    // sig = Pan2.ar(sig, \pan.kr(0));

    verb_time = LFNoise2.kr(0.3, 0.1, 1.03);
    verb_damp = LFNoise2.kr(0.2).range(0, 0.7);
    sig = MiVerb.ar(sig, verb_time, \verbMix.kr(0.1), verb_damp, 0.1);

    sig = Limiter.ar(sig);
    sig = sig * \amp.kr(1);
    sig = sig.sanitize();
    // src;
    
}).mold(2).play;

Ndef(\gutter)[999] = \pset -> Pbind(
    \dur, 0.01, 
    \amp, ~knob.(0).lag(0.1),
    \verbMix, ~knob.(1).lag(2),

    \omega, ~knob.(2).linlin(0,1, 0.001, 1),
    \mod, ~knob.(3).linlin(0,1,0.01, 10),
    \rate, ~knob.(4).linlin(0,1,0.01, 5),

    \gain, ~slider.(0).linlin(0,1,0,3.5),
    \gain1, ~slider.(1).linlin(0,1,0,2),
    \gain2, ~slider.(2).linlin(0,1,0,2),

    \damp, ~slider.(3).linexp(0,1,0.01, 1),
    \soften, ~slider.(4).linlin(0,1,0,5),
    \q, ~slider.(5).linexp(0,1,2.5,800),
    \pitchShift, ~slider.(6).linlin(0,1,0.05, 2),
    
);
)

Ndef(\gutter).gui;


(
SynthDef(\fftfilterbank, {    
    var size, buf, in, chain, low, high, wipe, freq, sig, both, offset;
    in = WhiteNoise.ar(0.1);
    
    size = (2**11).asInteger;
    buf = LocalBuf(size);
    chain = FFT(buf, in);    
    
    freq = MouseX.kr(500, 5000, 1); // crossover frequency in Hz
    wipe = freq/(s.sampleRate/2.0); // in range [-1,1]
    
    offset = 0.01;
    // Because of this offset, there is a gap in the spectrum.
    // The gap is `(2*offset)*(s.sampleRate/2.0) == 480` Hz wide.
    // Look at the spectrum with `FreqScope.new`.
    low = PV_BrickWall(chain, (-1+wipe) - offset);
    high = PV_BrickWall(chain, wipe + offset);

    // Insert more processing here

    both = PV_Add(low, high);
    sig = IFFT(both);
    Out.ar(0, Pan2.ar(sig, 0.0));
}).add;
)

(\instrument: \fftfilterbank).play;


z = Buffer.read(s, Platform.resourceDir +/+ "sounds/a11wlk01.wav");

(
x = SynthDef(\specMap, {arg sndBuf, freeze = 0;
    var a, b, chain1, chain2, out;
    var buf = Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./Field recs/drain paris audrey.wav");
    a = LocalBuf.new(2048);
    b = LocalBuf.new(2048);
    chain1 = FFT(a, SoundIn.ar(\in.kr(0)!2)); // to be filtered
    chain2 = FFT(b, PlayBuf.ar(2, buf, 1, loop: 1));
    // mouse x to play with floor.
    chain1 = PV_SpectralMap(chain1, chain2, 0, freeze, MouseX.kr(-1, 1), -1);
    out = IFFT(chain1);
    Out.ar(0, out.dup);
}).play(s, [\sndBuf, z, \freeze, 0]) 
)

x.set(\freeze, 1)
x.set(\freeze, 0);

x.free;

z.free;