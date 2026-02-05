//TO DO:
//OTT RIPOFF
//MAP ALL MIDI SECTIONS
//PHYSMOD SYNTH SECTION
//GUITAR / CONTACT MIC SECTION
//SPECTRAL SECTIONS
// ADDITIVE TUNINGx
//FIX TEMPO CLOCK CHANGES

(
    Ndef(\droneA, {
        var chain, sig;
        var ampDrift;
        var randomPhase=0;
        chain = ~initHarmonicsChain.(harmonics: 4, sidebands: 3, freq: 60);

		chain = ~padSynthDistribution.(
			chain, 
			harmonicRatio: \pad_ratio.kr(1, spec: ControlSpec(0.125, 2)),
			bw: \pad_bw.kr(1, spec: ControlSpec(0.5, 6000))  + 1e-4,
			bwScale: \pad_bwScale.kr(2, spec: ControlSpec(0.5, 2)) + 1e-4,
			bwSkew:  \pad_bwSkew.kr(4, spec: ControlSpec(-1, 4)) + 1e-4,
			stretch: 1,
			windowSkew: \pad_winSkew.kr(0.5, spec: ControlSpec(0, 1)),
		);

		chain[\freqs] = chain[\freqs] + 0.00001;

        chain = ~addLimiter.(chain);
    
        sig = SinOsc.ar(
            freq: chain[\freqs],
            phase: ({ Rand(0, 2pi) } ! chain[\numPartials]),
            mul: chain[\amps]
        );
		
		// sig = ~air.(
		// 	sig, 
		// 	chain, 
		// 	amount: \air_amount.kr(1, spec: ControlSpec(0, 1)),
		// 	speed: \air_speed.kr(0.8, spec: ControlSpec(0, 1)),
		// 	min: \air_min.kr(0.1, spec: ControlSpec(0, 1)),
		// 	max: \air_max.kr(1, spec: ControlSpec(0, 1)),
		// );
		
        sig = ~stereoSpread.(
			sig, 
			chain, 
			amount: SinOsc.ar(0.5).unipolar, 
			ramp: \ramp.kr(1, spec: ControlSpec(0.1, 1)), 
			saw: LFNoise2.ar(3), 
			cycles: \cycles.kr(1, spec: ControlSpec(0.1, 1))
		);
		
        // sig = ~simplePan.(sig, chain, amount: SinOsc.ar(0.2).unipolar, ramp: 1);
        
        sig = Balance2.ar(sig[0], sig[1]).sum;

        sig = sig * -24.dbamp;

		sig = sig.sanitize;

        sig = Compander.ar(sig, sig,
            thresh: 0.5,
            slopeBelow: 1,
            slopeAbove: 0.1,
            clampTime:  0.01,
            relaxTime:  0.01
        );

		sig = Balance2.ar(sig[0], sig[1], \pan.kr(0));
    });

    // x = ~makeNodeProxy.(Ndef(\droneA));
    // x.makeGui;
    
)

(
    ~sliceBuf = Dictionary();
    ~sliceBuf2 = Dictionary();
    ~sliceBuf3 = Dictionary();
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Missing Sounds 2016/04-Hobble_Break_126_PL_1.WAV", ~sliceBuf, 0.1, \centroid);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./radioaporee/12011108amlampostwiresa33road.wav", ~sliceBuf2, 0.1, \centroid);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./radioaporee/CityCountryMeworkumdrache.wav", ~sliceBuf3, 0.2, \crest);
)

(
    ~sliceBuf = Dictionary();
    ~sliceBuf2 = Dictionary();
    ~sliceBuf3 = Dictionary();
    ~sliceBuf4 = Dictionary();
    ~sliceBuf5 = Dictionary();

    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./radioaporee/sventojibridgecontact07261150.wav", ~sliceBuf, 0.1, \centroid);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./radioaporee/RomssavahjohkaUnderBridge.wav", ~sliceBuf2, 0.2, \flatness);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./radioaporee/kusnieriunaiwatetower.wav", ~sliceBuf3, 0.2, \centroid);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./radioaporee/cordedamarageportdajaccioLR.wav", ~sliceBuf4, 0.2, \centroid);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Silent Hill/Drum Loops/Silent Hill 2/Block Mind/loop 9 0013.wav", ~sliceBuf5, 0.2, \centroid);
)



(
    ~specBuff=Dictionary();
    ~specBuff2=Dictionary();
    // ~makeSpec.("/Users/aelazary/Desktop/Samples etc./NEW sample lib/mother and daughter singing o magnum mysterium-sfLDOVcK7nU.wav", ~specBuff, 16384, 2)
    ~makeSpec.("/Users/aelazary/Desktop/Samples etc./NEW sample lib/backyard oct 11 2018.wav", ~specBuff, 16384, 2);
    ~makeSpec.("/Users/aelazary/Desktop/Samples etc./Field recs/Brush metal bowl small 2.wav", ~specBuff2, 16384, 2);
)

//guitar 1 //mic 2
(
    SynthDef(\input, {
        var sig;
        sig = SoundIn.ar(\in.kr(0)!2);
        sig = DCompressor.ar(sig, threshold: -4);
        sig = sig * \gain.kr(1);
        sig = sig * \amp.kr(1);
        Out.ar(\out.kr(0), sig);
    }).add;
)

//sine tracker tuned
(
SynthDef(\sineTracker_fx, {|in, scale|
    var sig, chain, file, src, follower;
    var partialDrift, partialDriftFreq, partialDriftMD, phaseMD;
    var ampDrift;

    var fbIn;
    
    //get fb
    // fbIn = LocalIn.ar(2) * \feedback.kr(0.5);
    // src = InFeedback.ar(\inbus.kr(0), 2) + fbIn;
        
    // follower = Amplitude.ar(src, \env_atk.kr(0.01), \env_rel.kr(0.1));
    //generator funcs
    chain = ~initChain.(numPartials: 25, freq: 60);
    //order 0 or 1

    chain = ~makeStretchedHarmonicSeries.(chain, inharmonicity: 1);

    // chain = ~extractSines_smooth.(
    //     chain, 
    //     src, 
    //     freqLag: \freqLag.kr(0.01), 
    //     ampLag: \ampLag.kr(0.1), 
    //     order: \order.kr(1), 
    //     transpose: \transpose.kr(0), 
    //     winSize: 512, 
    //     fftSize: 4096, 
    //     hopSize: -1, 
    //     thresh: 0.001
    // );

    // chain.poll();

    // chain = ~quantizePartials.(chain, scale, strength: \strength.kr(1));
    chain = ~addLimiter.(chain);
    
    sig = SinOsc.ar(
        freq: chain[\freqs],
        phase: ({ Rand(0, 2pi) } ! chain[\numPartials]) * SinOsc.ar(0.1).unipolar,
        mul: chain[\amps]
    );

    // sig = ~stereoSpread.(
	// 		sig, 
	// 		chain, 
	// 		amount: SinOsc.ar(0.5).unipolar, 
	// 		ramp: \ramp.kr(1, spec: ControlSpec(0.1, 1)), 
	// 		saw: LFNoise2.ar(3), 
	// 		cycles: \cycles.kr(1, spec: ControlSpec(0.1, 1))
	// );
    

    sig = sig[0,2..].sum + ([-1,1] * sig[1,3..].sum);
    
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
    // LocalOut.ar(sig.sanitize);
    
    sig = HPF.ar(sig, 100);

    sig = Compander.ar(sig, sig,
        thresh: -6.dbamp,
        slopeBelow: 1,
        slopeAbove: 0.1,
        clampTime:  0.01,
        relaxTime:  0.01
    );

    sig = Balance2.ar(sig[0], sig[1], \pan.kr(0));
    sig = sig * \gain.kr(0).dbamp;
    sig = sig * \amp.kr(1);
    Out.ar(\out.kr(0), sig);
}).add;
)

(
// Pdef(\player).clear;
Pdef(\player).fadeTime = 8;
Pbindef(\player).play(t);
)

Ndef.clear;

(
Ndef.clear;
Ndef(\drone).fadeTime = 5;
Ndef(\drone).play;
)

Ndef(\drone).stop;
Pdef(\player).stop;

(
Ndef(\drone, {
    var chain, sig;
    var ampDrift;
    var randomPhase=0;
    chain = ~initHarmonicsChain.(harmonics: 8, sidebands: 4, freq: 31.midicps);

    chain = ~padSynthDistribution.(
        chain, 
        harmonicRatio: \pad_ratio.kr(1, spec: ControlSpec(0.125, 2)),
        bw: \pad_bw.kr(5, spec: ControlSpec(0.5, 6000))  + 1e-4,
        bwScale: \pad_bwScale.kr(1, spec: ControlSpec(0.5, 2)) + 1e-4,
        bwSkew:  \pad_bwSkew.kr(0, spec: ControlSpec(-1, 4)) + 1e-4,
        stretch: 1,
        windowSkew: \pad_winSkew.kr(0.5, spec: ControlSpec(0, 1)),
    );

    chain[\freqs] = chain[\freqs] + 0.00001;
    
    // chain = ~partialDetune.(chain, amount: SinOsc.ar(0.05).linlin(-1,1,0.01, 0.3), partial_select: SinOsc.ar(0.05).linlin(-1,1,2, 8), mode: -1);
    // chain = ~hpFilter.(chain, freq: 400, qFactor: 2);
    chain = ~notchFilter.(chain, SinOsc.ar(0.5).linlin(-1,1, 15000, 1000), 1000);
    // chain = ~lpFilter.(chain, freq: 10000, qFactor: 8);
    
    // chain = ~morphFilter.(chain, SinOsc.ar(0.5).linlin(-1,1, 15000, 1000), order: 2, morph: SinOsc.ar(0.3).unipolar);

    chain = ~addLimiter.(chain);

    sig = SinOsc.ar(
        freq: chain[\freqs],
        phase: ({ Rand(0, 2pi) } ! chain[\numPartials]),
        mul: chain[\amps]
    );

    // sig = ~stereoSpread.(sig, chain, amount: 1, ramp: 1, saw: LFNoise2.ar(3), cycles: SinOsc.ar(0.5).unipolar);
    sig = ~simplePan.(sig, chain, amount: SinOsc.ar(0.2).unipolar, ramp: 1);
    
    sig = Balance2.ar(sig[0], sig[1]).sum;

    sig = sig * -20.dbamp;

    sig = Compander.ar(sig, sig,
        thresh: 0.5,
        slopeBelow: 1,
        slopeAbove: 0.1,
        clampTime:  0.01,
        relaxTime:  0.01
    );

    sig = sig * \amp.kr(1)
});

Ndef(\drone)[999] = \pset -> Pbind(
    \dur, 0.01, 
    \amp, ~slider.(0).lag(0.1),
    // \pad_winSkew, ~slider.(1),   
    // \pad_winSkew, 0.5,
);

Ndef(\drone)[1] = \filter -> { |in| PitchShift.ar(in, 0.25, \pitch.kr(3), \pitchdispersion.kr(0.01), \timedispersion.kr(0.5))};
Ndef(\drone)[3] = \filter -> {|in| Out.ar( ~convolve_B , in)};
)

(

)



(
Pdef(\player,
    Pspawner({|sp|
        var grain, drum, morph, filter, reverb, tape, contact;

        grain = sp.par(
            Pdef(\bridge,
                Pmono(
                    \grainSlicer_mono,
                    \dur, 0.01,       
                    \amp, ~slider.(2).lincurve(0,1,0,1,-4),
                    \buf, ~sliceBuf2.at(\file),
                    \sliceStart, ~knob.(0).linlin(0,1,0,200).floor,
                    \overlap, ~knob.(1).linlin(0,1,10,500),
                    \trigRate, ~knob.(2).linexp(0,1,100,10000),
                    \slice, ~pGetSlice.(
                        (Pseries(1, 1, inf).wrap(0, 100) + Pkey(\sliceStart)).stutter(8), 
                        ~sliceBuf2
                    ),
                    \posRate,~knob.(3).linlin(0,1,0,2),
                    \rate, ~knob.(4).linlin(0,1,0.5,2),
                    \gain, 0,
                    \atk, 1,
                    // \dec, 0.1,
                    \dec, 2,
                    \out, [~convolve_A]
                )
            )
        );

        sp.par(
            Pdef(\mic1,
                Pmono(\input,
                    \in, 1,
                    \amp, ~slider.(1).lincurve(0,1,0,1,-4),
                    \dur, 0.1,
                    \out, [~convolve_A],
                )
            )
        );

        morph = sp.par(
            Pbind(
                \dur, 0.01,
                \gain, 0,
                \atk, 10,
                \rel, 100,
                // \swap, ~pmodenv.(Pseq([1, 0],inf), Pseq([1.5], inf), 1, \sine),
                \swap, ~slider.(7).lincurve(0,1,0,1,-4),
                \out, [~mainout]
            ) <>
            Pdef(\morph)
        );

        tape = sp.par(
            Pbindf(
                Pdef(\tape),
                \pregain, 0.0,
                \dcOffset, 0,
                // \dcOffset, 0.,
                \sigmoid, 1,
                \postgain, -6,
                \autogain, 0.0,
                \drywet, 1,
                \amp, 1.0,
                \out, [~mainout]
            )
        );
    
        filter = sp.par(
            Pbindf(
                Pdef(\filter)
                <>
                Pbind(
                    \blend, ~pmodenv.(Pseq([-1, 1], inf), 1.5),
                    // \freq, Pseg(Pseq([300, 800],inf), 4, 'lin' , inf),
                    \freq, 500,
                    \res, 0.5,
                    \out, [~mainout]
                )
            )
        );
        
        reverb = sp.par(
            Pbindf(
                Pdef(\miVerb),
                \time, 1,
                \damp, 0.1,
                \hp, 0.125,
                \freeze, 0,
                \diff, 0.9,
                \gain, 0,
                \out, ~mainout,
                \dur, 0.01,
            )
        );
    })
);
)
(
Ndef(\drone, {
    var chain, sig;
    var ampDrift;   
    var randomPhase=0;
    chain = ~initHarmonicsChain.(harmonics: 5, sidebands: 5, freq: 31.midicps);

    chain = ~padSynthDistribution.(
        chain, 
        harmonicRatio: \pad_ratio.kr(1, spec: ControlSpec(0.125, 2)),
        bw: \pad_bw.kr(5000, spec: ControlSpec(0.5, 6000))  + 1e-4,
        bwScale: \pad_bwScale.kr(2, spec: ControlSpec(0.5, 2)) + 1e-4,
        bwSkew:  \pad_bwSkew.kr(4, spec: ControlSpec(-1, 4)) + 1e-4,
        stretch: 1,
        windowSkew: \pad_winSkew.kr(0.5, spec: ControlSpec(0, 1)),
    );

    chain[\freqs] = chain[\freqs] + 0.00001;

    chain = ~addLimiter.(chain);

    sig = SinOsc.ar(
        freq: chain[\freqs],
        phase: ({ Rand(0, 2pi) } ! chain[\numPartials]),
        mul: chain[\amps]
    );

    sig = ~stereoSpread.(
        sig, 
        chain, 
        amount: 1, 
        ramp: \ramp.kr(1, spec: ControlSpec(0.1, 1)), 
        saw: LFNoise2.ar(3), 
        cycles: \cycles.kr(1, spec: ControlSpec(0.1, 1))
    );

    // sig = ~air.(
    // 	sig, 
    // 	chain, 
    // 	amount: \air_amount.kr(1, spec: ControlSpec(0, 1)),
    // 	speed: \air_speed.kr(0.8, spec: ControlSpec(0, 1)),
    // 	min: \air_min.kr(0.1, spec: ControlSpec(0, 1)),
    // 	max: \air_max.kr(1, spec: ControlSpec(0, 1)),
    // );
    
    // sig = ~simplePan.(sig, chain, amount: SinOsc.ar(1).unipolar, ramp: 1);
    
    sig = Balance2.ar(sig[0], sig[1]).sum;

    sig = sig * -24.dbamp;

    // sig = sig.sanitize;

    sig = Compander.ar(sig, sig,
        thresh: 0.5,
        slopeBelow: 1,
        slopeAbove: 0.1,
        clampTime:  0.01,
        relaxTime:  0.01
    );

    // Splay.ar(sig);

    // sig = Balance2.ar(sig[0], sig[1], \pan.kr(0));
});

Ndef(\drone)[1] = \filter -> { |in| PitchShift.ar(in, 0.25, \pitch.kr(2), \pitchdispersion.kr(0.01), \timedispersion.kr(0.5))};
Ndef(\drone)[3] = \filter -> {|in| Out.ar( ~convolve_B , in)};

Ndef(\drone)[999] = \pset -> Pbind(
    \dur, 0.01, 
    \amp, ~slider.(0).lag(0.1),
    \pad_winSkew, ~slider.(1),
    \pad_winSkew, 0.5,
);
)

//drums next
(
    Pdef(\kick, 
        Pbind(
            \dur, 1,
            \instrument, \fmPerc3,
            \freq, Pseq([60.0, 90], inf),
            \atk, 0.01,
            \dec, Pkey(\dur) * 1,
            \fb, 0.4,
            \index1, 1,
            \index2, 2,
            \ratio1, 0.5,
            \ratio2, 2,
            \drive, 0,
            \drivemix, 0,
            \sweep, 8.0,
            \spread, 20.0,
            \noise, 0.25,
            \feedback, Pkey(\cycledelta) * 0.25,
            \fbmod, 1,
            \pulseWidth, 1 - Pkey(\groupdelta).linlin(0,1,0.1,0.99),
            \lofreq, 500.0,
            \lodb, 10.0,
            \midfreq, 1200.0,
            \middb, 0.0,
            \hifreq, 7000.0,
            \hidb, 30.0,
            \gain, -17.0,
            \pan, 0.0,
            // \hpf, 
            \amp, Pkey(\groupdelta).linlin(0, 1, 1, 0.3),
            \out, [~mainout]
    
        )
    );
    
    Pdef(\snare, 
        Pbind(
            \instrument, \rim1,
            // \gate, 1.0,
            \freq, 50.0,
            \atk, 0.03,
            \dec, Pkey(\dur) * 0.5,
            \fb, 10.0,
            \index, 4.0,
            \ratio, 16.0,
            \sweep, 8.0,
            \spread, 20.0,
            \noise, 1.0,
            \roomsize, 3.0, 
            \reverbtime, 5.0,
            \gain, -20.0,
            \pan, 0.0,
            \amp, Pkey(\groupdelta).linlin(0, 1, 1, 0.3),
            // \amp, 0
        )
    );
    
    Pdef(\rim, 
        Pbind(
            \instrument, \rim1,
            // \gate, 1.0,
            \freq, 60 + Pwhite(-3, 3, inf),
            \atk, 0.03,
            \dec, 0.1,
            \fb, 0.0,
            \index, 4.0,
            \ratio, 16,
            \sweep, 128.0,
            \spread, Pkey(\groupdelta).linlin(0,1,1,20),
            \noise, 1.0,
            \roomsize, 3.0,
            \reverbtime, 5.0,
            \gain, -20.0,
            \pan, 0.0,
            \amp, Pkey(\groupdelta).linlin(0, 1, 0.8, 0.3),
            // \amp, 0
            // \out, [~mainout, ~miVerb]
        )
    );
    
    Pdef(\hh, 
        Pbind(
            \instrument, \hh2,
            \freq, 1200.0,
            \fb, 1 - Pkey(\groupdelta),
            \spread, 20.0,
            \index, 0.75,
            \pulseWidth, Pkey(\groupdelta),
            \atk, 0.001,
            \dec, Pkey(\dur),
            \gain, -5.0,
            \pan, 0.0,
            // \amp, 1,
            \amp, Pkey(\groupdelta),
    
            // \out, [~mainout, ~miVerb]
        )
    );
    
    Pdef(\sub, 
        Pbind(
            \instrument, \simpleSub,
            \freq, Pseq([39.0], inf),
            \atk, 0.05,
            \dec, Pkey(\dur) * 2,
            // \dec, Pkey(\dur) * 0.5,
            \drive, -10.0,
            \sweep, 8.5,
            \gain, -12.0,
            \pan, 0.0,
            \amp, 1.0,
        );
    );
)

t = TempoClock.new(130/60).permanent_(true).schedAbs(0, {t.beatsPerBar_(4)});

(
    t = TempoClock.new(150/60).permanent_(true).schedAbs(0, {t.beatsPerBar_(4)});
    Pdef(\player,   
    Pspawner({|sp|
        var drum, morph, filter, reverb, sineTracker, backyard;

        backyard = sp.par(
            Pdef(\backyard,
                    Pmono(\fftStretch_magAbove_mono,
                        \tempo, 130/60,
                        \dur, 0.1,
                        \amp, 1,
                        \gain, 16,
                        \buf, ~specBuff.at(\file),
                        \analysis, [~specBuff.at(\analysis)],
                        \fftSize, ~specBuff.at(\fftSize),
                        \rate, 0.01,

                        // \pos, 0.15,
                        // \pos, 0.25,
                        \pos, 0.01,
                        // \pos, 0.55,
                        // \pos, 0.7,

                        \filter, 1,
                        \pos, Pstep(Pseq([0.7, 0.55], inf), 4),
                        \len, 0.2,
                        \pan, ~pmodenv.(Pwhite(-0.6, 0.6), 3, 1, \sin),
                        \out, [~filter, ~miVerb]
                    )
                )
        );

        Pdef(\p1,
        	~makeSubdivision.(
        		PlaceAll([1, 1, 1, 1,] * 1, inf),
        		PlaceAll([4, 2, 2, 4], inf)
        	)
        );

        drum = sp.par(
            Pdef(\drum,
                Pbind(\out, [~mainout]) <>
                Pswitch1([Pdef(\kick), Pdef(\hh), Pdef(\rim), Pdef(\snare),], Pseq([1, 4, 2, 1, 2, 2, 2, 3] - 1, inf)) 
                // <> ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [1, 2], skew: [1], curve: \exp)
                // <> ~filterBeat.(key: Pkey(\groupcount), beat:[2, 3], reject: 1)
                <> Pdef(\p1)
            )
        );

        // morph = sp.par(
        //     Pbind(
        //         \gain, 0,
        //         \atk, 10,
        //         \rel, 100,
        //         \swap, ~slider.(7).lincurve(0,1,0,1,-4),
        //         \out, [~mainout, ~miVerb]
        //     ) <>
        //     Pdef(\morph)
        // );
      
        filter = sp.par(
            Pbindf(
                Pdef(\filter)
                <>
                Pbind(
                    \blend, ~pmodenv.(Pseq([-1, 1], inf), 4),
                    // \freq, Pseg(Pseq([300, 800],inf), 4, 'lin' , inf),
                    \freq, 500,
                    \res, 0.5,
                    \out, [~mainout]
                )
            )
        );
        
        reverb = sp.par(
            Pbindf(
                Pdef(\miVerb),
                \time, 1,
                \damp, 0.99,
                \hp, 0.125,
                \freeze, 0,
                \diff, 0.1,
                \gain, 0,
                \out, ~mainout
            )
        );
    
        // sineTracker = sp.par(
        //     Pdef(\sinetracker)
        //     <>
        //     Pbind(
        //         \feedback, 0;,
        //         \env_atk, 0.01,
        //         \env_rel, 0.5,
        //         \freqLag, 0.1,
        //         \ampLag, 0.01,
        //         \order, 1,
        //         \transpose, 0.0,
        //         \pan, 0.0,
        //         \gain, -12.0,
        //     )
        // );
    
    })
    )
)

(
    Ndef(\drone, {
        var chain, sig;
        var ampDrift;
        var randomPhase=0;
        chain = ~initHarmonicsChain.(harmonics: 4, sidebands: 5, freq: 60);
    
        chain = ~padSynthDistribution.(
            chain, 
            harmonicRatio: \pad_ratio.kr(1, spec: ControlSpec(0.125, 2)),
            bw: \pad_bw.kr(5000, spec: ControlSpec(0.5, 6000))  + 1e-4,
            bwScale: \pad_bwScale.kr(2, spec: ControlSpec(0.5, 2)) + 1e-4,
            bwSkew:  \pad_bwSkew.kr(4, spec: ControlSpec(-1, 4)) + 1e-4,
            stretch: 1,
            windowSkew: \pad_winSkew.kr(0.5, spec: ControlSpec(0, 1)),
        );
    
        chain[\freqs] = chain[\freqs] + 0.00001;
    
        chain = ~addLimiter.(chain);
    
        sig = SinOsc.ar(
            freq: chain[\freqs],
            phase: ({ Rand(0, 2pi) } ! chain[\numPartials]),
            mul: chain[\amps]
        );
    
        // sig = ~stereoSpread.(
        //     sig, 
        //     chain, 
        //     amount: 1, 
        //     ramp: \ramp.kr(1, spec: ControlSpec(0.1, 1)), 
        //     saw: LFNoise2.ar(3), 
        //     cycles: \cycles.kr(1, spec: ControlSpec(0.1, 1))
        // );
    
        sig = ~air.(
            sig, 
            chain, 
            amount: \air_amount.kr(1, spec: ControlSpec(0, 1)),
            speed: \air_speed.kr(0.8, spec: ControlSpec(0, 1)),
            min: \air_min.kr(0.1, spec: ControlSpec(0, 1)),
            max: \air_max.kr(1, spec: ControlSpec(0, 1)),
        );
        
        // sig = ~simplePan.(sig, chain, amount: SinOsc.ar(1).unipolar, ramp: 1);
        
        sig = Balance2.ar(sig[0], sig[1]).sum;
    
        sig = sig * -24.dbamp;
    
        // sig = sig.sanitize;
    
        sig = Compander.ar(sig, sig,
            thresh: 0.5,
            slopeBelow: 1,
            slopeAbove: 0.1,
            clampTime:  0.01,
            relaxTime:  0.01
        );
    
        // Splay.ar(sig);
    
        // sig = Balance2.ar(sig[0], sig[1], \pan.kr(0));
    });
    
    Ndef(\drone)[1] = \filter -> { |in| PitchShift.ar(in, 0.25, \pitch.kr(0.5), \pitchdispersion.kr(0.01), \timedispersion.kr(0.5))};
    Ndef(\drone)[3] = \filter -> {|in| Out.ar( ~convolve_B , in)};
    
    Ndef(\drone)[999] = \pset -> Pbind(
        \dur, 0.01, 
        \amp, ~slider.(0).lag(0.1),
        \pad_winSkew, ~slider.(1),
        \pad_winSkew, 0.5,
    );
)

//do later

(
    Ndef(\drone, \trapezoidFB);

    Ndef(\drone)[999] = \pset -> Pbind(
        \dur, 0.01,
        \amp, ~slider.(0).lag(0.1),
        \freq, 30, 
        \damp, ~slider.(1).lag(0.1),
        \feedback, ~slider.(2).lag(0.1), 
        \ap_fb, ~slider.(3).lag(0.1).linlin(0,1,),  
        \shape, 0.5,
        \duty, 0.4,  
        \fbmod, 1,
        \drywet, 0.1,
        \atk, 0.01,
        \dec, 0.5,
        \dampScale, 0.01,
        \gain, -30
    );
)

(
    b = Buffer.read(s,"/Users/aelazary/Desktop/Samples etc./NEW sample lib/1030 Rave Stabs/100 percent.wav");
    
    // b = Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./NEW sample lib/1030 Rave Stabs/LASER ARMY.wav");
    c = Buffer.read(s,"/Users/aelazary/Desktop/Samples etc./EchoThiefImpulseResponseLibrary/Brutalism/GeiselLibrary.wav");
        
    d = Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./EchoThiefImpulseResponseLibrary/Miscellaneous/MillsArtMuseum.wav");
)

(
    ~birdBuf = Dictionary();
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Field recs/Bell birds.wav", ~birdBuf, 0.3, \crest);
    ~breakBuf = Dictionary();
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Silent Hill/Drum Loops/Silent Hill 3/Dance With Night Wind/Slayed 01.WAV", ~breakBuf, 0.1, \crest);
)

t.tempo.postln

t.tempo_(180/60);

Pdef.clear

(
    // Pdef(\player).clear;
    Pdef(\player).fadeTime = 8;
    Pbindef(\player).play(t);
)

(
t = TempoClock.new(180/60).permanent_(true).schedAbs(0, {t.beatsPerBar_(4)});

Pdef(\player,
Pspawner({|sp|
    var drum, morph, filter, reverb, sineTracker, backyard;

    backyard = sp.par(
        Pdef(\backyard,
                Pmono(\fftStretch_magAbove_mono,
                    \tempo, 150/60,
                    \dur, 0.1,
                    \amp, 1,
                    \gain, 16,
                    \buf, ~specBuff.at(\file),
                    \analysis, [~specBuff.at(\analysis)],
                    \fftSize, ~specBuff.at(\fftSize),
                    \rate, 0.01,

                    \pos, 0.15,
                    \pos, 0.25,
                    // \pos, 0.01,
                    // \pos, 0.55,
                    // \pos, 0.7,

                    \filter, 1,
                    // \pos, Pstep(Pseq([0.15, 0.17], inf), 4),
                    \len, 0.2,
                    \pan, ~pmodenv.(Pwhite(-0.6, 0.6), 3, 1, \sin),
                    \out, [~convolve_B, ~miVerb]
                )
            )
    );

    sp.par(
        Pdef(\mic1,
            Pmono(\input,
                \in, 1,
                \amp, ~slider.(1).lincurve(0,1,0,1,-4),
                \dur, 0.1,
                \out, [~convolve_A],
            )
        )
    );

    morph = sp.par(
        Pbind(
            \gain, 0,
            \atk, 10,
            \rel, 100,
            \swap,  ~slider.(7).lincurve(0,1,0,1,-4),
            \out, [~mainout]
        ) <>
        Pdef(\morph)
    );

    reverb = sp.par(
        Pbindf(
            Pdef(\miVerb),
            \time, 1,
            \damp, 0.99,
            \hp, 0.125,
            \freeze, 0,
            \diff, 0.1,
            \gain, 0,
            \out, ~mainout
        )
    );

    Pdef(\p2,
        ~makeSubdivision.(
            PlaceAll([2, 2, 3, 2], inf),
            PlaceAll([4, 4, 4, 4, 1], inf)
        )
    );

    Pdef(\fb2Mod,
        PmonoArtic(\fb2,
            \amp, 0.05,
            \gain, 0,
            \buf, Pseq([b, d, c], inf).stutter(3),
            // \buf, d,
            // \window, 4096,
            // \impulse, 5.0,
            \impulse, ~pmodenv.(Pexprand(5, 20000, inf), Pkey(\dec)),

            \window, Pseq([4096, 1024, 512], inf).stutter(3),
            \atk, 0.1,
            \dec, 0.4,
            // \dec, Pkey(\dur),
            \sustainTime, Pkey(\dur),
            // \exciter, 0.6,
            \density, ~pmodenv.(1 - Pkey(\groupdelta), Pkey(\dec)),
            \exciter, ~pmodenv.(1 - Pkey(\groupdelta), Pkey(\dec)),
            \feedback, ~pmodenv.(Pseq([0.2, 0.4, 0.8],inf), Pseq([4, 8, 2, 4],inf)),

            \time, ~pmodenv.(Pseq([0.005, 0.001, 0.010],inf) * 1 * Pkey(\groupdelta), Pkey(\dec)),
            \damp, ~pmodenv.(Pseq([1, 0.1],inf), Pwhite(0.01, 0.4, inf)),
            \damp, ~pmodenv.(Pkey(\groupdelta) * 1, Pkey(\dec)),

            \filter, ~pmodenv.(Pseq([500, 20000],inf), Pseq([1, 1, 2], inf)),

            \delay2, ~pmodenv.(Pseq([0, 1, 0],inf), Pseq([0.5, 0.25], inf)),

            \rev, ~pmodenv.(Pexprand(0.1, 10, inf), Pseq([2, 4], inf)),
            \pan, ~pmodenv.(Pwhite(-0.8, 0.8, inf), Pseq([0.5, 0.25],inf)),
        )
    );
    
    drum = sp.par(
        Pbindf(
            Pdef(\fb2Seq,
                Pdef(\fb2Mod)
                <> ~filterBeat.(key: Pkey(\eventcount), beat:[1, 3, 4], mod: 5)
                // <> ~pSkew.(Pdef(\p2), key: Pkey(\eventcount), group: [1, 2], skew: [-1, 1], curve: \exp)
                <> ~pSkew.(Pdef(\p2), key: Pkey(\cyclecount), group: [1, 2], skew: [-0.5, 0.5], curve: \exp)
                <> Pdef(\p2)
            ),
            \out, [~convolve_A]
        )
    );

    Pdef(\p3,
        ~makeSubdivision.(
            PlaceAll([1, 1, 1, 1], inf),
            PlaceAll([4, 4, 4, 4], inf)
        )
    );

    sp.par(
        Pdef(\breakSliced,
            Pbind(
                \amp, 1,
                \atk, 0.01,
                \rel, 0.1,
                \buf, ~breakBuf.at(\file),
                \rate, 1,
                \oneshot, 1,
                \gain, 0,
                \sliceStart, 5,
                \slice, ~pGetSlice.(Pseries(1, 1, inf).wrap(0, 32) + Pkey(\sliceStart), ~breakBuf),
                \pitchMix, 0.9,
                \pitchRatio, 2,
                \windowSize, 0.01,
                \pitchDispersion, 1.0,
                \timeDispersion, 0.1,
                \out, [~convolve_B],
                
            )
            // <> Pbind(\timingOffset, Pkey(\groupcount) / Pkey(\groupdelta))
            <> ~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[2, 3, 4])
            // <> ~filterBeat.(key: Pkey(\eventcount), beat:[1,3,4])
            <> ~pSkew.(Pdef(\p3), key: Pkey(\eventcount), group: [1, 2], skew: [0.75], curve: \exp)
            // <>Pbind(\amp, Pseq([0.1, 0.7], inf))
            <> Pdef(\p3)
            <> Pbind(\instrument, \segPlayer)
        )
    );

})
);
)

t = TempoClock.new(180/60).permanent_(true).schedAbs(0, {t.beatsPerBar_(4)});

(
    Pdef(\kick, 
        Pbind(
            // \dur, 1,
            \instrument, \fmPerc3,
            \freq, 120,
            \atk, 0.05,
            \dec, Pkey(\dur) * 1,
            \fb, 0,
            \index1, 1,
            \index2, 2,
            \ratio1, 0.5,
            \ratio2, 2,
            \drive, 0,
            \drivemix, 0,
            \sweep, 8.0,
            \spread, 20.0,
            \noise, 0.25,
            \feedback, 0,
            \fbmod, 1,
            \pulseWidth, 1 - Pkey(\groupdelta).linlin(0,1,0.01,0.2),
            \lofreq, 500.0,
            \lodb, 10.0,
            \midfreq, 1200.0,
            \middb, 0.0,
            \hifreq, 7000.0,
            \hidb, 30.0,
            \gain, -17.0,
            \pan, 0.0,
            // \hpf, 
            \amp, Pkey(\groupdelta).linlin(0, 1, 1, 0.3),
            \out, [~mainout]
    
        )
    );
    
    Pdef(\snare, 
        Pbind(
            \instrument, \rim2,
            // \gate, 1.0,
            \freq, 50.0,
            \atk, 0.03,
            \dec, Pkey(\dur) * 0.5,
            \fb, 10.0,
            \index, 4.0,
            \ratio, 16.0,
            \sweep, 8.0,
            \spread, 20.0,
            \noise, 1.0,
            \roomsize, 3.0, 
            \reverbtime, 5.0,
            \gain, -20.0,
            \pan, 0.0,
            \amp, Pkey(\groupdelta).linlin(0, 1, 1, 0.3),
            // \amp, 0
        )
    );
    
    Pdef(\rim, 
        Pbind(
            \instrument, \rim1,
            // \gate, 1.0,
            \freq, 60 + Pwhite(-3, 3, inf),
            \atk, 0.03,
            \dec, 0.1,
            \fb, 0.0,
            \index, 3.0,
            \ratio, 16,
            \sweep, 128.0,
            \spread, Pkey(\groupdelta).linlin(0,1,1,20),
            \noise, 1.0,
            \roomsize, 3.0,
            \reverbtime, 5.0,
            \gain, -20.0,
            \pan, 0.0,
            \amp, Pkey(\groupdelta).linlin(0, 1, 0.8, 0.3),
            // \amp, 0
            \out, [~mainout, ~miVerb]
        )
    );
    
    Pdef(\hh, 
        Pbind(
            \instrument, \hh,
            \freq, 1200.0,
            \fb, 1 - Pkey(\groupdelta),
            \spread, 20.0,
            \index, 0.75,
            \pulseWidth, Pkey(\groupdelta),
            \atk, 0.001,
            \dec, Pkey(\dur),
            \gain, -5.0,
            \pan, 0.0,
            // \amp, 1,
            \amp, Pkey(\groupdelta),
    
            // \out, [~mainout, ~miVerb]
        )
    );
    
    Pdef(\sub, 
        Pbind(
            \instrument, \simpleSub,
            \freq, Pseq([39.0], inf),
            \atk, 0.4,
            \dec, Pkey(\dur) * 2,
            // \dec, Pkey(\dur) * 0.5,
            \drive, -10.0,
            \sweep, 8.5,
            \gain, -12.0,
            \pan, 0.0,
            \amp, 1.0,
        );
    );
)

(
~introSample = Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./radioaporee/VandensBokstoTrosas202051.wav");

Pdef(\player,
    Pspawner({|sp|
        var drum, morph, filter, reverb, sineTracker, sample, roar;
    
        sample = sp.par(Pmono(\samplePlayer, \buf, ~introSample, \out, [~mainout, ~sineTracker]));

        morph = sp.par(
            Pbind(
                \tempo, 120/60,
                \gain, 0,
                \atk, 10,
                \rel, 100,
                \swap, ~pmodenv.(Pseq([1, 0],inf), Pseq([1.5], inf), 1, \sine),
                \out, [~mainout]
            ) <>
            Pdef(\morph)
        );
    
        reverb = sp.par(
            Pbindf(
                Pdef(\miVerb),
                \time, 1,
                \damp, 0.99,
                \hp, 0.125,
                \freeze, 0,
                \diff, 0.1,
                \gain, 0,
                \out, ~mainout
            )
        );
    
        // Pdef(\p1,
        //     ~makeSubdivision.(
        //         PlaceAll([[1, 1.5], [1.5, Rest(1.5)], 1, [Rest(0.5), 1]], inf),
        //         PlaceAll([[4, 1], 4, 4], inf)
        //     )
        // );

        Pdef(\p1,
            ~makeSubdivision.(
                PlaceAll([1, 1, 1, 1], inf),
                PlaceAll([2, 4, 1, 4], inf)
            )
        );

        roar = sp.par(
            Pbindf(
                Pdef(\roar),
                \dur, 0.01,
                \drive, 9.0,
                \tone, ~pmodenv.(Pseq([-0.99, 0.9], inf), 1.5),
                \toneFreq, 500.0,
                \toneComp, 1.0,
                \drywet, 0.8,
                \bias, 0,
                \filterFreq, 6000,
                \filterLoHi, ~pmodenv.(Pseq([0, 1], inf), 2),
                \filterBP, 0,
                \filterRes, 1,
                \filterBW, 0.5,
                \filterPre, 1.0,
                \feedAmt, 7.0,
                \feedFreq, 50.0,
                \feedBW, 0.1,
                \feedDelay, 0.1,
                \feedGate, 0.5,
                \gain, 0.0,
                \amp, 1.0,
                // \group, ~morphGroup,
                \out, [~mainout]
            )
        );

        sp.par(
        Pbind(\out, [~roar]) <>
        ~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[4], reject: 1) <>
        Pswitch1([Pdef(\sub), Pdef(\kick), Pdef(\hh), Pdef(\rim)], Pseq([1, 2, 3, 1, 2, 2, 2, 3] - 1, inf)) <>
        Pdef(\p1)
        )
    })
);
)

(
    ~sample = Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./spannerGuitar/spannerDrumGuitar-4.wav");
    
    Pdef(\player,
        Pspawner({|sp|
            var drum, morph, filter, reverb, sineTracker, sample, roar;
        
            sample = sp.par(
                Pmono(
                    \samplePlayer, 
                    \buf, ~sample, 
                    \gain, 30,
                    \amp, ~slider.(1),
                    \dur, 0.01,
                    \out, [~mainout]
                )
            );
    
            morph = sp.par(
                Pbind(
                    \gain, 0,
                    \atk, 10,
                    \rel, 100,
                    \swap, ~pmodenv.(Pseq([1, 0],inf), Pseq([1.5], inf), 1, \sine),
                    \out, [~mainout]
                ) <>
                Pdef(\morph)
            );
        
            reverb = sp.par(
                Pbindf(
                    Pdef(\miVerb),
                    \time, 1,
                    \damp, 0.3,
                    \hp, 0.125,
                    \freeze, 0,
                    \diff, 0.8,
                    \gain, 0,
                    \out, ~mainout
                )
            );
    
            roar = sp.par(
                Pbindf(
                    Pdef(\roar),
                    \dur, 0.01,
                    \drive, 9.0,
                    \tone, ~pmodenv.(Pseq([-0.99, 0.9], inf), 0.5),
                    \toneFreq, 500.0,
                    \toneComp, 1.0,
                    \drywet, 0.8,
                    \bias, 0,
                    // \filterFreq, 50,
                    \filterLoHi, ~pmodenv.(Pseq([0, 1], inf), 1),
                    \filterBP, 0,
                    \filterRes, 1,
                    \filterBW, 0.5,
                    \filterPre, 1.0,
                    \feedAmt, 7.0,
                    \feedFreq, 50.0,
                    \feedBW, 0.1,
                    \feedDelay, 0.1,
                    \feedGate, 0.05,
                    \gain, 0.0,
                    \amp, 1.0,
                    // \group, ~morphGroup,
                    \out, [~mainout]
                )
            );
    
            // sp.par(
            //     Pbind(\out, [~roar]) <>
            //     ~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[4], reject: 1) <>
            //     Pswitch1([Pdef(\sub), Pdef(\kick), Pdef(\hh), Pdef(\rim)], Pseq([1, 2, 3, 1, 2, 2, 2, 3] - 1, inf)) <>
            //     Pdef(\p1)
            // )
        })
    );
)

Pdef(\player).clear
Pdef(\player).play
(
~bufA = Buffer.alloc(s, s.sampleRate * 0.1);
~bufB = Buffer.alloc(s, s.sampleRate * 0.01);
~bufC = Buffer.alloc(s, s.sampleRate * 0.025);
~sample = Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./radioaporee/SecondShots.wav");

Pdef(\player,
Pspawner({| sp |
    var sectionLength, sample;
        //fx const
        // sample = sp.par(
        //     Pmono(
        //         \samplePlayer, 
        //         \buf, ~sample, 
        //         \gain, 30,
        //         \out, ~mainout
        //     )
        // );

        sp.par(
            Pbindf(
                Pdef(\miVerb),
                \time, 0.6,
                \damp, 0.6,
                \hp, 0.0,
                \freeze, 0,
                \diff, 0.9,
                \gain, 0,
                \dur, 0.1,
                \out, ~mainout
            )
        );

        sp.par(
            Pdef(\mic1,
                Pmono(\input,
                    \in, 1,
                    \amp, ~slider.(2).lincurve(0,1,0,1,-4),
                    \dur, 0.1,
                    \out, [~grain],
                )
            )
        );
        
        sp.par(
            Pdef(\guitar,
                Pmono(\input,
                    \in, 0,
                    \amp, ~slider.(0).lincurve(0,1,0,1,-4),
                    \dur, 0.1,
                    \out, [~grain],
                )
            )
        );

        sp.par(
            Pbindf(
                Pdef(\grain),
                \bufnum, ~bufA,
                \amp, ~slider.(1).lincurve(0,1,0,1,-4),
                \overlap, 20,
                // \overlap, ~pmodenv.(Pseq([5, 10, 20], inf), 20),
                \triggerRate, ~pmodenv.(Pseq([100, 1000, 10000], inf), 6),
                // \triggerRate, 1000,
                \posRate, ~pmodenv.(Pseq([0, 1], inf), 12),
                // \posRate, 2,
                \rate, 1,
                \recRate, 1,
                // \polarityMod, 1,
                \polarityMod,1,
                \overdub, ~knob.(6),
                \feedback, ~knob.(7).linlin(0,1,0,0.5),

                // \feedback, 0,
                \gain, 20,
                \dur, 0.1,
                \out, [~convolve_B, ~mainout]
            )
        );

        sp.par(
            Pbindf(
                Pdef(\morph),
                \dur, 0.1,
                \gain, 0,
                \atk, 500,
                \rel, 100,
                \swap, ~slider.(6).linexp(0,1,0.1,1),
                \drywet, ~slider.(7),
                \out, [~mainout]
            )
        );

    })
);
)

(
    ~bufA = Buffer.alloc(s, s.sampleRate * 0.1);
    ~bufB = Buffer.alloc(s, s.sampleRate * 0.01);
    ~bufC = Buffer.alloc(s, s.sampleRate * 0.025);
    ~fftSize = 16384 * 0.5;
    ~analysisFX = Array.fill(2, {Buffer.alloc(s, ~fftSize)});
    
        Pdef(\player,
        Pspawner({| sp |
            var sectionLength;
                    //fx const
                sp.par(
                    Pbindf(
                        Pdef(\miVerb),
                        \time, 0.6,
                        \damp, 0.6,
                        \hp, 0.0,
                        \freeze, 0,
                        \diff, 0.9,
                        \gain, 0,
                        \dur, 0.1,
                        \out, ~mainout
                    )
                );
    
                sp.par(
                    Pdef(\mic1,
                        Pmono(\input,
                            \in, 1,
                            \amp, ~slider.(2).lincurve(0,1,0,1,-4),
                            \dur, 0.1,
                            \out, [~fftStretchLive],
                        )
                    )
                );
                
                sp.par(
                    Pdef(\guitar,
                        Pmono(\input,
                            \in, 0,
                            \amp, ~slider.(0).lincurve(0,1,0,1,-4),
                            \dur, 0.1,
                            \out, [~fftStretchLive],
        
                        )
                    )
                );
        
                sp.par(
                    Pbindf(
                        Pdef(\fftStretchLive),
                        \buf, ~bufA,
                        \amp, ~slider.(0),
                        \analysis, [~analysisFX],
                        \fftSize, ~fftSize,
                        \recRate, 1,
                        \len, ~knob.(1),
                        \thresh, ~knob.(2).linlin(0,1,0, 10),
                        \remove, ~knob.(3).linlin(0,1,0,100),
                        \rate,  ~knob.(4),
                        // \pos, ~knob.(5),
                        \pos, 0,
                        \overdub, ~knob.(6),
                        \feedback, 0,
                        \gain, 6,
                        \dur, 0.1,
                        \out, [~mainout]
                    )
                );
    
                sp.par(
                    Pbindf(
                        Pdef(\morph),
                        \dur, 0.1,
                        \gain, 0,
                        \atk, 500,
                        \rel, 100,
                        \swap, ~slider.(6).linexp(0,1,0.1,1),
                        \drywet, ~slider.(7),
                        \out, [~mainout]
                    )
                );
        })
        )
)