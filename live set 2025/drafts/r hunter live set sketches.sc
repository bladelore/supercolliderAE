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
    ~maxGrains = 50;
    ~fftSize = 4096*8;
    ~bufA = Buffer.alloc(s, ~fftSize);
    ~bufB = Buffer.alloc(s, s.sampleRate * 0.01);
    ~bufC = Buffer.alloc(s, s.sampleRate * 0.025);
    ~specBuf = Array.fill(~maxGrains, {Buffer.alloc(s, ~fftSize)});
    ~specBuf.do{|item| item.zero};
)

~test.()

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
                        \out, [~spectralGrains],
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

                    \posRate, ~knob.(0),
                    \posRateMD, ~slider.(0).linlin(0,1,0,10),

                    \tFreq, ~knob.(1).clip(0.001,1).linexp(0.001,1,10,1000),
                    \tFreqMD, ~slider.(1).linlin(0,1,0,10),

                    \overlap, ~knob.(2).clip(0.001,1).linexp(0.001,1,1,36),
                    \overlapMD, ~slider.(2).linlin(0,1,0,10),

                    
                    \companderMD, ~knob.(5).linexp(0,1,0.01,10),
                    \spectralFilter, 1 - ~knob.(6),

                    \overdub, ~knob.(7),
                    \midipitch, 0,

                    \gain, 0,
                    \out, ~bus1
                )
            );
            
        })).play(t);
)

(
	~break = Dictionary();
    ~bow = Dictionary();
    ~guitar = Dictionary();
	~skate = Dictionary();
    ~cymbals = Dictionary();
    ~cymbals2 = Dictionary();
    
    // ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Missing Sounds 2016/04-Hobble_Break_126_PL_1.WAV", ~break, 0.3, \crest, chans: 2);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./NEW sample lib/Jungle riddims/music is so special.wav", ~break, 0.3, \crest, chans: 2);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./contact mics/bow mic.wav", ~bow, 0.9, \centroid, chans: 2);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./contact mics/guitar chain.wav", ~guitar, 0.9, \centroid, chans: 2);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./contact mics/skateboard 1.wav", ~skate, 0.9, \centroid, chans: 2);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./feedback cymbals/feedback cymbals-2.wav", ~cymbals, 0.4, \centroid, chans: 2);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./feedback cymbals/feedback cymbals-4.wav", ~cymbals2, 0.4, \centroid, chans: 2);
)

//
(
Pdef(\perc,
	Pspawner({| sp |
		var pad;
		var beat;
        var sample = ~cymbals2;
		// sp.par(Pbind(\instrument, \rest, \tempo, 150/60));

		sp.par(
			Pbind(
				\instrument, \specSlicer,
				\amp, 1,
				\speed, ~slider.(2).linlin(0, 1, 0.25, 16).ceil.reciprocal * 4,
				\chance, ~slider.(3),
				\atk, ~slider.(4).linlin(0,1,0.01,2),
				\rel, ~slider.(5).linlin(0,1,0.25,2),
				\dur, Pfunc({|ev|
					var val;
					var chance = ev[\chance].coin;
					var speed = ev[\speed];
					if(chance == true) {val = speed;}{ val = Rest(speed)};
					val;
				}),
				\buf, sample.at(\file),
				\rate, ~knob.(4).linlin(0,1,0.25,2),
				\oneshot, 1,
				\swap, ~knob.(2),
				\smooth, ~knob.(3),
				\slice_A, ~pGetSlice.((Pseries(1, 32, inf).wrap(0, 32) + ~knob.(0).linlin(0,1,0,300).floor), sample),
				\slice_B, ~pGetSlice.((Pseries(1, 32, inf).wrap(0, 32) + ~knob.(1).linlin(0,1,0,300).floor + 1), sample),
				\out, ~bus2,
			)
		);

	})
    ).play(t);
)

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
    });
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
        \ampLag, ~slider.(1),
        \freqLag, ~slider.(2),
        \quantize, ~slider.(3),
        \feedback, ~slider.(4),
        \octave, ~slider.(5),
        \lpf, ~slider.(6),
        \thresh, ~slider.(7),
    );

    )
)

//melodic cymbals?
(
Pdef(\perc,
	Pspawner({| sp |
		var pad;
		var beat;
        var sample = ~cymbals;

        Pdef(\p1,
            ~makeSubdivision.(
                PlaceAll([1, 1, 1, 1], inf),
                PlaceAll([4, 2, 2, 4], inf)
            )
        );

		sp.par(
            // ~filterBeat.(key: Pkey(\eventcount), beat:[1, 3, 5, 9], mod: 3, reject: 1) <>
			Pbind(
				\instrument, \specSlicer,
				\amp, 0.5,
				\atk, 0.4,
				\rel, 1,
				// \dur, 0.25,
				\rate, 1 - Pkey(\groupdelta) * 2,
				\oneshot, 1,
				\swap, Pkey(\groupdelta),
				\smooth, 1 - Pkey(\groupdelta),
                \buf, sample.at(\file),
				\slice_A, ~pGetSlice.((Pseries(1, 64, inf).wrap(0, 64) + ~knob.(0).linlin(0,1,0,300).floor), sample),
				\slice_B, ~pGetSlice.((Pseries(1, 64, inf).wrap(0, 64) + ~knob.(1).linlin(0,1,0,300).floor + 1), sample),
				\out, ~bus2,
			) <> 
            Pdef(\p1)
		);

	})
    ).play(t);
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

        MiVerb.ar(sig, verb_time, \verbMix.kr(0.1), verb_damp, 0.1, mul: 0.5);

        sig = Compander.ar(sig, sig,
            thresh: 1,
            slopeBelow: 1,
            slopeAbove: 1,
            clampTime:  0.01,
            relaxTime:  0.01
        );

        sig = sig * \amp.kr(1);
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
        \verbMix, ~knob.(1).lag(0.1)
    );
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

        op1 = SinOsc.ar(f0, phaseShift * 2pi * f0) * g_out * index;
        // op1 = VarSaw.ar(f0, 0, phaseShift * 2pi * f0) * g_out * index;

        op2 = SinOsc.ar(f0 * op1);
        // op2 = Blip.ar(f0 * (1 + op1), 5); // bandlimited pulse train
        
        sig = op2;
        sig = sig.tanh;
        sig = sig.sanitize;

        LocalOut.ar([g_out, g_state[0]]);

        verb_time = LFNoise2.kr(0.3, 0.1, 1.03);
        verb_damp = LFNoise2.kr(0.2).range(0, 0.7);
        sig = MiVerb.ar(sig, verb_time, \verbMix.kr(0.1), verb_damp, 0.1);
        sig = Compander.ar(sig, sig,
            thresh: 1,
            slopeBelow: 1,
            slopeAbove: 1,
            clampTime:  0.01,
            relaxTime:  0.01
        );
        
        sig = sig * \amp.kr(1);
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
        \amp, ~knob.(0).linexp(0,1,0.01,1),
        \verbMix, ~knob.(1).lag(0.1)
    );
)

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

    sig = sig * \amp.kr(1);
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
    \amp, ~knob.(0).linexp(0,1,0.01,1),
    \verbMix, ~knob.(1).lag(0.1)
);

)

(
// IPF with memory kernel
Ndef(\ipf).play;

Ndef(\ipf, {
    var trig, g_state, g_out, g_prev, dg, md;
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

    // --- trigger per cycle ---
    // trig = Impulse.ar(f0);

    trig = Impulse.ar(f0);
    
    // --- hold [current, past7] g-values for memory kernel ---
    g_state = LocalIn.ar(8);
    g_prev = g_state[1];

    // --- alpha modulation ---
    alpha = LFSaw.ar(alpha_rate).linexp(-1, 1, alpha_start.max(0.001), alpha_end);

    // --- memory kernel recurrence ---
    su = 0;
    (g_state.size - 1).do { |i|
        k = exp((i + 1).neg / tau);
        su = su + beta * k * exp(g_state[0] - g_state[i + 1]);
    };

    eps = 1e-6;
    safeVal = ((g_state[0] - su) / alpha).max(eps);
    g_out = g_state[0] - log(safeVal);

    // --- phase shift ---
    dg = (g_out - g_prev).abs;
    // md = [modRate, modRate + modStereo];
    md = [modRate - (modStereo * 0.5), modRate + (modStereo * 0.5)];
    phaseShift = dg * md / f0;

    // --- oscillators ---
    op1 = SinOsc.ar(f0, phaseShift * 2pi * f0) * g_out * index;
    op2 = SinOsc.ar(f0 * op1);

    sig = op2.sanitize;

    g_out = g_out.sanitize;

    // --- update memory (shift values) ---
    LocalOut.ar([g_out] ++ g_state[0..(g_state.size - 2)]);

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
    \f0, ~slider.(0).linexp(0,1,30,40000).lag(0.1),
    \index, ~slider.(1).linexp(0,1,0.001,1).lag(0.1),
    \alphaRate, ~slider.(2).linexp(0.001,1,0.001,1000),
    \alphaStart, ~slider.(3).clip(0.001,1),
    \alphaEnd, ~slider.(4).clip(0.001,1),
    \beta, ~slider.(5).linexp(0.001,1,0.001,100),
    \modRate, ~slider.(6).linlin(0.001,1,1,1000),
    \modStereo, ~slider.(7).linexp(0.001,1,0.001,1),
    \tau, ~knob.(2).linexp(0,1,0.001,0.499),
    \amp, ~knob.(0).linexp(0,1,0.01,1),
    \verbMix, ~knob.(1).lag(0.1)
).trace;
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

//IPF
(
    SynthDef(\ipfPerc, {
    |f0=60, beta=0.3, g_init=0.6, modRate=1, modStereo=100, 
     verbMix=0.1, amp=1, gate=1, sweep=8|

    var trig, g_prev, g, su, safeVal, g_out, md, freq, sig;
    var verb_time, verb_damp;
    var alpha;
    var pitchEnv = (1 + (sweep * Env.perc(0.0, 0.13, curve: -4).ar)) * XLine.ar(1, 0.6 , sweep.reciprocal);

    f0 = f0 * pitchEnv;

    alpha = SinOsc.ar(\alphaRate.kr(1)).linlin(-1, 1, 0.01, 9);
    // alpha = XLine.ar(\alphaStart.kr(0.1), \alphaEnd.kr(0.1), \alphaRate.kr(1));

    trig = Impulse.ar(f0);
    g_prev = LocalIn.ar(2);
    g = Select.ar((trig > 0), [K2A.ar(g_init), g_prev]);

    su = beta * exp(g - g_prev);
    safeVal = ((g - su) / alpha).max(0.00001);
    g_out = g - log(safeVal);

    LocalOut.kr(g_out);

    md = [modRate - (modStereo * 0.5), modRate + (modStereo * 0.5)];
    freq = f0 / (1 + g_out * md).max(0.001);

    sig = SinOsc.ar(freq);
    sig = sig * g_out;
    sig = sig.tanh;

    verb_time = LFNoise2.kr(0.3, 0.1, 1.03);
    verb_damp = LFNoise2.kr(0.2).range(0, 0.7);
    sig = MiVerb.ar(sig, verb_time, verbMix, verb_damp, 0.1);
    sig = Compander.ar(sig, sig,
        thresh: 1,
        slopeBelow: 1,
        slopeAbove: 1,
        clampTime: 0.01,
        relaxTime: 0.01
    );

    sig = sig * EnvGen.kr(Env.perc(\atk.kr(0.01), \dec.kr(1)), gate: gate, doneAction:2);
    // sig = SelectX.ar(1, [sig, Resonz.ar(sig.sanitize, freq: 50, mul: 1)]);
    
    sig = sig * \gain.kr(0).dbamp;
    sig = LeakDC.ar(sig, 0.995);
    sig = sig.sanitize;

    Out.ar(\out.kr(0), sig);
}).add;
)
//IPF perc phase
(
SynthDef(\ipfPerc_phase, {
    |f0=60, alphaStart=0.1, alphaEnd=1, alphaRate=1, beta=0, modRate=10, modStereo=50, index=0.01, verbMix=0.1, gate=1|
    
    var trig, g_state, g_out, g_prev, dg, md;
    var su, safeVal;
    var alpha;
    var op1, op2, phaseShift;
    var sig;
    var verb_time, verb_damp;
    var sweep = \sweep.kr(2);
    var pitchEnv = (1 + (sweep * Env.perc(0.0, 0.13, curve: -4).ar)) * XLine.ar(1, 0.6 , sweep.reciprocal);

    f0 = f0 * pitchEnv;
    
    g_state = LocalIn.ar(2);
    g_prev = g_state[1];

    alpha = SinOsc.ar(alphaRate).linexp(-1, 1, alphaStart.max(0.001), alphaEnd);
    // alpha = XLine.ar(alphaStart, alphaEnd , alphaRate);
    
    su = beta * exp(g_state[0] - g_prev);
    safeVal = ((g_state[0] - su) / alpha).max(0.00001);
    g_out = g_state[0] - log(safeVal);
    
    dg = (g_out - g_prev).abs;
    
    md = [modRate - (modStereo * 0.5), modRate + (modStereo * 0.5)];
    phaseShift = dg * md / f0;
    
    op1 = SinOsc.ar(f0, phaseShift * 2pi * f0) * g_out * index;
    op2 = SinOsc.ar(f0 * op1);
    
    sig = op2.tanh;
    
    LocalOut.ar([g_out, g_state[0]]);
    
    verb_time = LFNoise2.kr(0.3, 0.1, 1.03);
    verb_damp = LFNoise2.kr(0.2).range(0, 0.7);
    sig = MiVerb.ar(sig, verb_time, verbMix, verb_damp, 0.1);
    sig = Compander.ar(sig, sig,
        thresh: 1,
        slopeBelow: 1,
        slopeAPbove: 1,
        clampTime: 0.01,
        relaxTime: 0.01
    );
    
    sig = sig * \amp.kr(1);

    sig = sig * EnvGen.kr(Env.perc(\atk.kr(0.01), \dec.kr(1)), gate: gate, doneAction:2);

    sig = sig * \gain.kr(0).dbamp;
    sig = LeakDC.ar(sig, 0.995);
    sig = sig.sanitize;
    
    Out.ar(\out.kr(0), sig);
}).add;
)

(
    ~maxGrains = 50;
    ~fftSize = 4096*8;
    if (~specBuf.notNil) {~specBuf.do { |b| if (b.notNil) { b.free } };};
    ~specBuf = Array.fill(~maxGrains, {Buffer.alloc(s, ~fftSize)});
    ~specBuf.do{|item| item.zero};
    ~bufA = Buffer.alloc(s, ~fftSize);
    ~bufB = Buffer.alloc(s, s.sampleRate * 0.01);
    ~bufC = Buffer.alloc(s, s.sampleRate * 0.025);
    ~specBuff_A=Dictionary();
    ~sewer=Dictionary();
    ~makeSpec.("/Users/aelazary/Desktop/Samples etc./feedback cymbals/feedback cymbals-4.wav", ~specBuff_A, 16384, 2);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Field recs/Vienna Sewer 3.wav", ~sewer, 0.3, \centroid, chans: [0,1]);
)

(
~advance = Condition.new;
k.elAt(\tr, [\fwd]).do{|sl, buttonIdx| 
    sl.action = {|el|
        if (el.value == 1) {
            ~advance.test = true;
            ~advance.signal;
            ~advance.test = false;
            "next".postln;
        };
    };
};

t = TempoClock.new(100/60).permanent_(true).schedAbs(0, {t.beatsPerBar_(4)});
x = {
    var ipf, specGrains, additive;
    var specBuff = ~specBuff_A;
    var sample = ~sewer;

    ~bufA.zero;
    ~bufB.zero;
    ~specBuf.do{|item| item.zero};

    Pdef(\bell,
        Pmono(\fftStretch_magFilter_mono,
            \dur, 0.1,
            \amp, ~pmodenv.(Pseq([0.1, 1], inf), 4, 1, \exp),
            \gain, -12,
            \buf, specBuff.at(\file),
            \analysis, [specBuff.at(\analysis)],
            \fftSize, specBuff.at(\fftSize),
            \rate, 0.5,
            // \filter, 0,
            
            // \thresh, 100,
            // \remove, 10,

            \thresh, 10,
            \remove, 0,

            \thresh, ~pmodenv.(Pseq([10, 100], inf), 8, 1, \sin),
            \remove, ~pmodenv.(Pseq([1, 10], inf), 6, 1, \sin),

            // \pos, 0.4,
            // \pos, Pstep(Pseq([0.4, 0.45], inf), 16).lag(2),

            \pos, ~pmodenv.(Pseq([0.4, 0.45], inf), Pseq([8,1],inf), 1, \exp),
            \len, 0.01,
            \pan, ~pmodenv.(Pwhite(-0.6, 0.6), 3, 1, \sin),
            \pitchRatio, 0.75 * 0.5,
            // \pitchRatio, 0.5,
            \out, [~bus3]
        )
    ).play(t);

    16.wait;
    
    Pdef(\p1,
        ~makeSubdivision.(
            PlaceAll([1, 1, 1, 1], inf),
            PlaceAll([4, 4, 4, 4], inf)
        )
    );

    Pdef(\p1_transform,
        ~filterBeat.(key: Pkey(\cyclecount), beat:[1, 4, 5, 9], mod: 3, reject: 1) <>
        ~pSkew.(Pdef(\p1), key: Pkey(\cyclecount), group: [1, 4], skew: [1], curve: \exp) <>
        Pdef(\p1)
    );

    Pdef(\ipf1,
        Pbind(
            \instrument, \ipfPerc,
            \f0, 69*3,
            \atk, 0,
            \dec, 1,
            \beta, Pkey(\groupdelta).linlin(0,1,1,0.1),
            \alphaRate, Pkey(\groupdelta).linexp(0,10,1,20),
            \beta, ~pmodenv.(Pwhite(0.2, 0.0, inf),  Pseq([2, 1, 0.5, inf])),
            \g_init, 1,
            \modRate, 16,
            \modStereo, ~pmodenv.(Pwhite(1, 4, inf),  Pseq([2, 1, 0.5, inf])),
            \amp, 1,
            \sweep, 16,
            \verbMix, 0.3,
            \out, [~bus2, ~spectralGrains, ~resonator]
        )
    );


    Pdef(\reso, (
        Pbind(\out, ~bus2, \atk, 0.01, \dec, 2, \distort, 0.5, \freq, 49, \inGain, -40, \gain, -12) <> 
        Pdef(\resonator)
    )).play;
    
    \a.postln;
    Pdef(\ipf, (Pdef(\ipf1) <> Pdef(\p1_transform))).play(t);

    Pdef(\specGrains, (
        Pbind(
            \srcbuf, ~bufA,
            \specbuf, [~specBuf],
            \fftSize, ~fftSize,
            \amp, 1,
            \dur, 0.1,
            \posRate, 1,
            \tFreq, 10,
            \tFreqMD, 3,
            \overlap, 12,
            \overdub, 0.5,
            \feedback, 0,
            \midipitch, 12,
            \spectralFilter, 0,
            \gain, 12,
            \out, ~bus1
        ) <> Pdef(\spectralGrains1)
    )).play(t);

    3.wait;

    \pause.postln;
    Pdef(\ipf).stop(t);
    4.wait;

    \b.postln;
    Pbindef(\ipf1, Pbind(\f0, 60, \modRate, 3));

    Pdef(\p1_transform,
        ~pSkew.(Pdef(\p1), key: Pkey(\cyclecount), group: [1, 4], skew: [-1], curve: \exp) <>
        Pdef(\p1).finDur(6)
    );

    Pdef(\ipf).play(t);
    6.wait;

    \pause.postln;
    Pdef(\ipf).stop(t);
    4.wait;

    \c.postln;
    Pdef(\specGrains).stop;

    // ~bufB.zero;
    
    Pdef(\specGrains, (
        Pbind(
            \srcbuf, ~bufB,
            \specbuf, [~specBuf],
            \fftSize, ~fftSize,
            \amp, 1,
            \dur, 0.1,
            \posRate, 0.5,
            \tFreq, 200,
            \tFreqMD, 1,
            \overlap, 12,
            \overdub, 0.5,
            \feedback, 0,
            \midipitch, 0,
            \spectralFilter, 0.1,
            \gain, 0,
            \out, ~bus1
        ) <> Pdef(\spectralGrains1)
    )).play(t);
    
    Pdef(\ipf2,
        Pbind(
            \instrument, \ipfPerc,
            // \dur, 1,
            \f0, 69,
            \atk, ~pmodenv.(Pwhite(0, 0.1, inf),  0.5),
            \dec, 1,
            \beta, Pkey(\groupdelta).linlin(0,1,1,0.1),
            \alphaRate, 32,
            \g_init, 1,
            \modRate, ~pmodenv.(Pseq([16, 3], inf),  4),
            \modStereo, ~pmodenv.(Pwhite(1, 4, inf),  Pseq([2, 1, 0.5, inf])),
            \sweep, 16,
            // \verbMix, ~pmodenv.(Pwhite(0.1, 0.8, inf),  0.5),
            \verbMix, 0.1,
            \out, [~bus2, ~spectralGrains, ~resonator]
        )
    );

    Pdef(\p1_transform,
        ~filterBeat.(key: Pkey(\groupcount), beat:[1, 3], mod: 3, reject: 0) <>
        ~pSkew.(Pdef(\p1), key: Pkey(\cyclecount), group: [1, 2, 3], skew: [-1, 1, -1, 1], curve: \exp) <>
        Pdef(\p1)
    );

    Pdef(\ipf, (Pdef(\ipf2) <> Pdef(\p1_transform))).play(t);

    8.wait;

    \pause.postln;
    Pdef(\ipf).stop(t);
    4.wait;
    
    Pdef(\ipf2,
        Pbind(
            \instrument, \ipfPerc,
            // \dur, 1,
            \f0, 69,
            \atk, ~pmodenv.(Pwhite(0, 0.1, inf),  0.5),
            \dec, 1,
            \beta, Pkey(\groupdelta).linlin(0,1,1,0.1),
            \alphaRate, 32,
            \g_init, 1,
            \modRate, ~pmodenv.(Pseq([16, 3], inf),  4),
            \modStereo, ~pmodenv.(Pwhite(1, 4, inf),  Pseq([2, 1, 0.5, inf])),
            \sweep, 16,
            \verbMix, ~pmodenv.(Pwhite(0.1, 0.8, inf),  0.5),
            // \verbMix, 0.1,
            \out, [~bus2, ~spectralGrains, ~resonator]
        )
    );

    Pdef(\ipf, (Pdef(\ipf2) <> Pdef(\p1_transform))).play(t);

    \d1.postln;

    16.wait;

    \d2.postln;
    Pdef(\ipf).stop;

    Ndef(\sample, { PlayBuf.ar(2, sample.at(\file), startPos: 200000, rate: 1) * -12.dbamp;}).set(\out, ~bus4);
    Ndef(\sample).play;

    16.wait;

    \pause.postln;

    Ndef(\sample).stop;

    2.wait;

    Ndef(\sample, { PlayBuf.ar(2, sample.at(\file), startPos: 200000, rate: 0.5) * -12.dbamp;}).set(\out, ~bus4);
    Ndef(\sample).play;

    16.wait;

    Ndef(\sample).stop;

    2.wait;

    \d3.postln;

    Pdef(\bell).stop;
    Pdef(\bell,
        Pmono(\fftStretch_magFilter_mono,
            \dur, 0.1,
            // \amp, ~pmodenv.(Pseq([0.1, 1], inf), 0.05, 1, \sin),
            \gain, -3,
            \buf, specBuff.at(\file),
            \analysis, [specBuff.at(\analysis)],
            \fftSize, specBuff.at(\fftSize),
            \rate, 3,
            // \filter, 0,
            \pos, 0.4,
            \pos, ~pmodenv.(Pseq([0.4, 0.5], inf), 3, 1, \sin),
            \len, 0.02,
            \pan, ~pmodenv.(Pseq([0,1], inf), 0.5, 1, \sin),
            \pitchRatio, 0.25,
            \thresh, ~pmodenv.(Pseq([10, 100], inf), 8, 1, \sin),
            \remove, ~pmodenv.(Pseq([1, 10], inf), 6, 1, \sin),
            \out, [~spectralGrains]
        )
    ).play(t);

    Ndef(\sample, { PlayBuf.ar(2, sample.at(\file), startPos: 200000, rate: 1);}).set(~bus1);
    Ndef(\sample).play;

    Pdef(\p2,
        ~makeSubdivision.(
            PlaceAll([2, 1.5, 1.5] * 1.5, inf),
            PlaceAll([4, 3, 4, 4], inf)
        )
    );

    Pdef(\specSample,
        Pbind(
            \instrument, \specSlicer,
            \amp, 0.2,
            \atk, Pwhite(4, 1),
            \rel, Pwhite(2, 4, inf),
            // \dur, 0.25,
            \rate, 1 - Pkey(\cycledelta),
            \rate, Prand([0.5, 1], inf),
            \oneshot, 1,
            \swap, ~pmodenv.(Pwhite(-1, 1, inf), Pkey(\dur)),
            \pan, ~pmodenv.(Pwhite(-1, 1, inf), Pkey(\dur)),
            \smooth, ~pmodenv.(Pwhite(0, 2, inf), Pkey(\dur)),
            \buf, sample.at(\file),
            \offset, Pstep(Pwhite(0, 100,inf).round, 16),
            \slice_A, ~pGetSlice.((Pseries(1, 64, inf).stutter(1) + Pkey(\offset)), sample),
            \slice_B, ~pGetSlice.((Pseries(1, 64, inf).stutter(1) + Pkey(\offset)) + 1, sample),
            \out, [~bus2, ~spectralGrains],
        )
    );

    Pdef(\ipf3,
        Pbind(
            \instrument, \ipfPerc_phase,
            // \dur, 1,
            // \f0, Pstep([60, 60*2], 16, inf),
            \f0, ~pmodenv.(Pstep([60, 60*2], 16, inf), 8),
            \index, Pwhite(0.0001, 0.0003, inf),
            \atk, 0,
            \dec, 1,
            \alphaRate, Pkey(\groupdelta).linexp(0,10,1,20),
            // \alphaRate, 4,
            \beta, ~pmodenv.(Pwhite(0.1, 0.2, inf),  Pseq([2, 1, 0.5, inf])),
            \g_init, 0.4,
            \modRate, 10,
            \modStereo, 1,
            \amp, 1,
            \sweep, 16,
            \verbMix, 0.1,
            \gain, 0,
            \out, ~bus2
        )
    );

    Pdef(\p1_transform,
        // ~filterBeat.(key: Pkey(\cyclecount), beat:[1, 3], mod: 3, reject: 1) <>
        // Pbind(\f0, 500)<>
        ~pSkew.(Pdef(\p2), key: Pkey(\eventcount), group: [1, 2, 3], skew: [-1, 1, -1, 1], curve: \exp) <>
        Pdef(\p2)
    );

    Pdef(\ipf,
            Pswitch1([Pdef(\ipf3), Pdef(\ipf1), Pdef(\ipf2), Pdef(\specSample)], PlaceAll([1, 4, 4] - 1, inf)) <>
            Pdef(\p1_transform)
    ).play(t);

    32.wait;

    \e.postln;

    Ndef(\sample).stop(fadeTime: 10);

    Pdef(\specGrains).stop;

    ~bufB.zero;
    
    Pdef(\specGrains).stop;
    Pdef(\specGrains, (
        Pbind(
            \srcbuf, ~bufB,
            \specbuf, [~specBuf],
            \fftSize, ~fftSize,
            \amp, 1,
            \dur, 0.1,
            \posRate, 0.5,
            \tFreq, 200,
            \tFreqMD, 0,
            \overlap, 12,
            \overdub, 0.5,
            \feedback, 0,
            \midipitch, 12,
            \spectralFilter, 0.1,
            \gain, 0,
            \out, ~bus1
        )
        <> Pdef(\spectralGrains1)
    )).play(t);

    Pbindef(\ipf3, \out, [~bus2, ~spectralGrains]);

    32.wait;

    \f.postln;

    Pdef(\bell).stop;
        Pdef(\bell,
            Pmono(\fftStretch_magBelow_mono,
                \dur, 0.1,
                \amp, ~pmodenv.(Pseq([0.4, 1], inf), 4, 1, \sin),
                \gain, -3,
                \buf, specBuff.at(\file),
                \analysis, [specBuff.at(\analysis)],
                \fftSize, specBuff.at(\fftSize),
                \rate, 0.5,
                \filter, 40,
                \pos, 0.4,
                // \pos, ~pmodenv.(Pwhite(0.4, 0.5, inf), 0.1, 1, \sin),
                \len, 0.01,
                \pan, ~pmodenv.(Pwhite(-0.6, 0.6), 3, 1, \sin),
                \pitchRatio, 0.5,
                \out, [~bus3, ~spectralGrains]
            )
    ).play(t);

    Pdef(\specGrains).stop;
    
    ~bufC.zero;

    Pdef(\specGrains, (
        Pbind(
            \srcbuf, ~bufC,
            \specbuf, [~specBuf],
            \fftSize, ~fftSize,
            \amp, 1,
            \dur, 0.1,
            \posRate, 0.1,
            \tFreq, 10,
            \tFreqMD, 0,
            \overlap, 12,
            \overdub, 0.5,
            \feedback, 0,
            \midipitch, 12,
            \companderMD, 1,
            \spectralFilter, 0.05,
            \gain, 6,
            \out, ~bus1
        ) <> Pdef(\spectralGrains1)
    )).play(t);

    Pdef(\ipf).stop;

    Pdef(\ipf4,
        Pbind(
            \instrument, \ipfPerc_phase,
            // \dur, 1,
            \f0, Pstep([60], 8, inf),
            \index, 0.0001*0.5,
            // \atk, Pkey(\cycledelta).linlin(0,1,0.3,0),
            \atk, 0.2,
            \dec, Pkey(\dur),
            \alphaRate, Pkey(\groupdelta).linexp(0,10,1,20),
            \beta, Pkey(\groupdelta).linlin(0,1,1,0.1),
            \alphaRate, 4,
            \g_init, 0.4,
            \modRate, 10,
            \modStereo, 1,
            \amp, 1,
            \sweep, 16,
            \verbMix, 0.1,
            \gain, 0,
            \out, [~bus2, ~spectralGrains]
        )
    );

    Pdef(\p2,
        ~makeSubdivision.(
            PlaceAll([1, 1, 1] * 2, inf),
            PlaceAll([4, 2, 4, 2, 1], inf)
        )
    );

    Pdef(\p1_transform,
        // ~filterBeat.(key: Pkey(\cyclecount), beat:[1, 4, 5, 9], mod: 3, reject: 1) <>
        ~pSkew.(Pdef(\p2), key: Pkey(\cyclecount), group: [1, 2, 3, 4], skew: [-1, 1, -1, 1], curve: \exp) <>
        // Pswitch1([Pdef(\p2), Pdef(\p1)], Pstep(Pseq([0, 1], 32)))
        Pdef(\p2)
    );

    Pdef(\ipf, (Pdef(\ipf4) <> Pdef(\p1_transform))).play(t);

    30.wait;

    Pbindef(\ipf4, \sweep, ~pmodenv.(Pseq([0, 16], inf), 6, 1, \sin));

    Pdef(\specSample,
        Pbind(
            \instrument, \specSlicer,
            \amp, 0.2,
            \gain, -6,
            \atk, 0,
            // \dur, 0.25,
            \rel, 0.25,
            \rate, 1 - Pkey(\cycledelta) * 0.5,
            // \rate, Prand([0.5, 1], inf),
            \oneshot, 1,
            \swap, ~pmodenv.(Pwhite(-1, 1, inf), Pkey(\dur)),
            \pan, ~pmodenv.(Pwhite(-1, 1, inf), Pkey(\dur)),
            \smooth, ~pmodenv.(Pwhite(0, 2, inf), Pkey(\dur)),
            \buf, sample.at(\file),
            \offset, Pstep(Pwhite(0, 100,inf).round, 16),
            \slice_A, ~pGetSlice.((Pseries(1, 64, inf).stutter(1) + Pkey(\offset)), sample),
            \slice_B, ~pGetSlice.((Pseries(1, 64, inf).stutter(1) + Pkey(\offset)) + 1, sample),
            \out, [~bus2, ~spectralGrains],
        ) <> 
        
        // ~pSkew.(Pdef(\p2), key: Pkey(\cyclecount), group: [1, 2, 3, 4], skew: [-1, 1, -1, 1], curve: \exp) <>
        ~makeSubdivision.(
            PlaceAll([1, 1, 1], inf),
            PlaceAll([4, 4, 4], inf)
        )
    );

    Pdef(\specSample).play(t);

    64.wait;

    Pdef(\bell,
        Pmono(\fftStretch_magAbove_mono,
            \dur, 0.1,
            \amp, ~pmodenv.(Pseq([0.8, 1], inf), 4, 1, \sin),
            \gain, 0,
            \buf, specBuff.at(\file),
            \analysis, [specBuff.at(\analysis)],
            \fftSize, specBuff.at(\fftSize),
            \rate, 1,
            \filter, 0.5,
            \pos, 0.5,
            \len, 0.2,
            \pan, ~pmodenv.(Pwhite(-0.6, 0.6), 3, 1, \sin),
            \out, [~bus3]
        )
    ).play(t);

    16.wait;

    Pdef(\bell).stop;
    Pdef(\bell,
        Pmono(\fftStretch_magAbove_mono,
            \dur, 0.1,
            \amp, 1,
            \gain, 0,
            \buf, specBuff.at(\file),
            \analysis, [specBuff.at(\analysis)],
            \fftSize, specBuff.at(\fftSize),
            \rate, 1,
            \filter, 0.5,
            \pos, 0.5,
            \len, 0.2,
            \pan, ~pmodenv.(Pwhite(-0.6, 0.6), 3, 1, \sin),
            \out, [~spectralGrains]
        )
    ).play(t);

    Pdef(\specGrains).stop;
    
    ~bufC.zero;

    Pdef(\specGrains, (
        Pbind(
            \srcbuf, ~bufA,
            \specbuf, [~specBuf],
            \fftSize, ~fftSize,
            \amp, 1,
            \dur, 0.1,
            \posRate, 1,
            \tFreq, 400,
            // \tFreqMD, 1,
            \overlap, 12,
            \overdub, 0,
            \feedback, 0,
            \midipitch, -12,
            \companderMD, 10,
            \spectralFilter, 0.5,
            \gain, 6,
            \out, ~bus1
        ) 
        <> Pdef(\spectralGrains1)
    )).play(t);

    Pdef(\specSample).stop;

    16.wait;

    Pdef(\ipf).stop;

    Ndef(\droneA).clear;
    Ndef(\droneA).play;
    Ndef(\droneA).fadeTime = 20;
    
    Ndef(\droneA, {
        var chain, sig;
        var ampDrift;
        chain = ~initHarmonicsChain.(harmonics: 4, sidebands: 4, freq: 60);
        chain = ~padSynthDistribution.(
            chain, 
            harmonicRatio: 2,
            bw: 5000,
            bwScale: 1,
            bwSkew:  0,
            stretch: 1,
            windowSkew: 0.5
        );
    
        chain = ~notchFilter.(chain, SinOsc.ar(0.5).linlin(-1,1,50,1000), 2000);
        chain = ~addLimiter.(chain);
    
        ampDrift = LFNoise2.ar(2 ! chain[\numPartials]) * 0.1;
        sig = LFSaw.ar(
            freq: chain[\freqs],
            iphase: ({ Rand(0, 2pi) } ! chain[\numPartials]) * SinOsc.ar(0.1).unipolar,
            mul: chain[\amps]
        );
        
        sig = sig[0,2..].sum + ([-1,1] * sig[1,3..].sum);

        sig = sig * -30.dbamp;

        sig = Compander.ar(sig, sig,
            thresh: 0.5,
            slopeBelow: 1,
            slopeAbove: 0.1,
            clampTime:  0.01,
            relaxTime:  0.01
        );
    }).set(\out, ~bus3);

    64.wait;

    Pdef(\specGrains).stop;

}.fork(t);
)

(
Ndef(\gutterTest).clear;

Ndef(\gutterTest, {
    var sig, freq1, freq2, pitch;
    var mod, omega, damp, rate, gain, soften, gain1, gain2, q1, q2;
    var chain, src;

    src = SoundIn.ar(0!2).sanitize;

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
    chain = ~extractSines_smooth.(chain, src, freqLag: \freqLag.kr(0.01), ampLag: \ampLag.kr(1), order: 0, transpose: 0, thresh: Amplitude.kr(src, attackTime: 0.1, releaseTime: 0.2, mul: 1.0, add: 0.0), numSines: 24);
    chain = ~addLimiter.(chain);
    freq1 = chain[\freqs] * pitch;
    freq1 = freq1.clip(20, 20000);
    gain1 = chain[\amps] * gain1;
    // freq1.poll;

    // freq1 = pitch * [ 104.08913805616, 272.0241439869, 142.5394121681, 740.98235420089, 3231.1092775615, 598.48984613932, 564.11122601617, 152.53849023618, 4773.6198870775, 798.26171948236, 729.54452005837, 734.37542510625, 661.89936380362, 133.46101940276, 1715.6115033359, 11658.962024239, 6408.5610397899, 11775.302108311, 857.52846512925, 2020.251581889, 14168.220304686, 192.17654523236, 326.55730188427, 4386.8490423436];
    freq2 = (freq1 * Array.rand(freq1.size, 0.95,1.0)).clip(0, 20000);

    // q = q ! freq1.size;
    // q1 = chain[\amps] * q1;
    q1 = Array.rand(freq1.size, 0.95,1.0) * q1;
    q2 = Array.rand(freq1.size, 0.95,1.0) * q1;


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
        enableaudioinput: 1,
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

    sig = Pan2.ar(sig, \pan.kr(0));
    sig = Limiter.ar(sig);
    sig = sig.sanitize();
    
}).mold(2).play;
)

Ndef(\gutterTest).gui;