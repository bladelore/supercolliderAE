
//MUST INIT
Scale.all.put(\homayoun, Scale([0, 1.5, 4, 5, 7, 7.5, 10]))
Scale.at(\homayoun)

(
// Ndef.clear;
Ndef(\s1).fadeTime = 0;
Ndef(\s1).play;
// Pdef(\multiband).play;
)

(
Ndef(\s1, {
var sig, chain, buf, ampDrift, source, fb, octaveSelect;
var phaseMD;
var polarity;
var cutoff;

source = SoundIn.ar(\in.kr(0)!2);
fb = LocalIn.ar(2) * \feedback.kr(0);
source = source + fb;
//generator funcs
//freq does nothing here
chain = ~initChain.(numPartials: 50, freq: 440);
//order 0 or 1
octaveSelect = Select.kr(\octave.kr(0).range(0, 4), [-48, -36, -24, -12, 0]);
// chain = ~extractSines_smooth.(chain, source, freqLag: \freqLag.kr(0.01), ampLag: \ampLag.kr(1), order: 0, transpose: octaveSelect, thresh: \thresh.kr(0));
chain = ~extractSines_smooth.(chain, source, freqLag: \freqLag.kr(0.01), ampLag: \ampLag.kr(1), order: 0, transpose: octaveSelect, thresh: \thresh.kr(0));
// chain = ~extractSines.(chain, source, freqLag: \freqLag.kr(0.01), ampLag: \ampLag.kr(1), order: 0, transpose: octaveSelect);

cutoff = \lpf.kr(0).range(10, 10000).linexp(10,1000,10,10000);
// cutoff.poll();
chain = ~lpFilter.(chain, cutoff, qFactor:4);
chain = ~quantizePartials.(chain, Scale.homayoun, \quantize.kr(1), 60.midicps);
chain = ~addLimiter.(chain);

polarity = ({ Rand(-1, 1) } ! chain[\numPartials]);

sig = SinOsc.ar(
    freq: chain[\freqs],
    phase: ({ Rand(0, 2pi) } ! chain[\numPartials]) * SinOsc.ar(0.1).unipolar,
    mul: chain[\amps]
);

sig = sig * polarity;

//one of these
sig = ~air.(sig, chain, amount: 1, speed: 0.5, min: 0.1, max: 0.9);
sig = Balance2.ar(sig[0], sig[1]).sum;

sig = HPF.ar(sig, 100);

sig = Compander.ar(sig, sig,
    thresh: 0.25,
    slopeBelow: 1,
    slopeAbove: 0.1,
    clampTime:  0.01,
    relaxTime:  0.01
);

LocalOut.ar(sig.sanitize);

sig = sig * \amp.kr(1)
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

// Ndef(\s1)[1] = \filter -> { |in| PitchShift.ar(in, 0.25, \pitch.kr(1), \pitchdispersion.kr(0.01), \timedispersion.kr(0.5))};
)

(
Ndef(\s1, {
	var sig, chain, file, buf, sample, src, follower;
	var partialDrift, partialDriftFreq, partialDriftMD, phaseMD;
	var ampDrift;
	var fbIn;

	// file = "/Users/aelazary/Desktop/Samples etc./spirographshr/Video by spirographshr [C9X6OXxIIXgfK].wav";
	file = "/Users/aelazary/Desktop/Samples etc./tape recs/tape in 0002 [2024-04-29 164022].aif";
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
	chain = ~extractSines.(chain, src, freqLag: \freqLag.kr(0.01), ampLag: \ampLag.kr(1), order: 0, transpose: 0, winSize: 1024, fftSize: 4096, hopSize: 4);
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

(
	~break = Dictionary();
    ~bow = Dictionary();
    ~guitar = Dictionary();
	~skate = Dictionary();
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Missing Sounds 2016/04-Hobble_Break_126_PL_1.WAV", ~break, 0.3, \crest, chans: 2);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./contact mics/bow mic.wav", ~bow, 0.9, \centroid, chans: 2);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./contact mics/guitar chain.wav", ~guitar, 0.9, \centroid, chans: 2);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./contact mics/skateboard 1.wav", ~skate, 0.9, \centroid, chans: 2);

    ~piano = Dictionary();
    ~shPad1 = Dictionary();
	~shPad2 = Dictionary();
	~shPad3 = Dictionary();
	~shPad4 = Dictionary();
	~shPad5 = Dictionary();

    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Splice packs/VISIONIST_labeled_processed/VISIONIST_tonal/VISIONIST_melody/VISIONIST_melody_loops/VISIONIST_melody_loop_piano_123_Emin.wav", ~piano, 0.3, \centroid, chans: 2);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Silent Hill/Ambience/Silent Hill 4/Pulsating Ambience/MusicFX 10.wav", ~shPad1, 0.1, \centroid, chans: 2);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Silent Hill/Ambience/Silent Hill 4/Fortunate Sleep - Cat Scratchism Mix/FEEDIES 1.wav", ~shPad2, 0.1, \centroid, chans: 2);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Silent Hill/Ambience/Silent Hill 4/Subway Moan/Eerie-Edition-CD1_31.WAV", ~shPad3, 0.1, \centroid, chans: 2);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Silent Hill/Ambience/Silent Hill 2/Day of Night/SLEEPCYCL2.wav", ~shPad4, 0.1, \centroid, chans: 2);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Silent Hill/Ambience/Silent Hill Origins/The Wicked End/33 Futhswatering.wav", ~shPad5, 0.1, \centroid, chans: 2);
)

(
    ~specBuff=Dictionary();
    ~specBuff2=Dictionary();
	~specBuff3=Dictionary();
    
    ~makeSpec.("/Users/aelazary/Desktop/Samples etc./Silent Hill/Ambience/Silent Hill 2/Day of Night/SLEEPCYCL2.wav", ~specBuff, 16384, 2);
    ~makeSpec.("/Users/aelazary/Desktop/Samples etc./Silent Hill/Ambience/Silent Hill Origins/The Wicked End/33 Futhswatering.wav", ~specBuff2, 16384, 2);
	~makeSpec.("/Users/aelazary/Desktop/Samples etc./NEW sample lib/mother and daughter singing o magnum mysterium-sfLDOVcK7nU.wav", ~specBuff, 16384, 2)
)

Ndef(\s1).release(5);
Ndef(\s1).release(5);

Pdef(\player).clear;
Pdef(\player).fadeTime = 8;
Pbindef(\player).play(t);
Pdef(\multiband).play;



Pdef(\player).stop;


//to do 
//download Homayoun sounds
//sub
//pulsar
//guitars

(
SynthDef(\input, {
    var sig;
    sig = SoundIn.ar(\in.kr(0)!2);
    sig = sig * \gain.kr(1);
	sig = sig * \amp.kr(1);
    Out.ar(\out.kr(0), sig);
}).add;
)

// {SinOsc.ar(440!2)*0.1}.play
Synth(\input, [\in, 1])

(
    ~sliceBuf = Dictionary();
    ~sliceBuf2 = Dictionary();
    ~sliceBuf3 = Dictionary();
    ~sliceBuf4 = Dictionary();
    ~sliceBuf5 = Dictionary();
    ~sliceBuf6 = Dictionary();
    ~sliceBuf7 = Dictionary();
    
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./radioaporee/sventojibridgecontact07261150.wav", ~sliceBuf, 0.1, \centroid);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./radioaporee/RomssavahjohkaUnderBridge.wav", ~sliceBuf2, 0.2, \flatness);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./radioaporee/sventojibridgecontact07261150.wav", ~sliceBuf3, 0.2, \centroid);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Field recs/Rock song.wav", ~sliceBuf4, 0.2, \centroid);    
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./radioaporee/kusnieriunaiwatetower.wav", ~sliceBuf5, 0.2, \centroid);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./radioaporee/cordedamarageportdajaccioLR.wav", ~sliceBuf6, 0.2, \centroid);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Silent Hill/Drum Loops/Silent Hill 2/Block Mind/loop 9 0013.wav", ~sliceBuf7, 0.2, \centroid);
)

(
    b = Buffer.read(s,"/Users/aelazary/Desktop/Samples etc./NEW sample lib/1030 Rave Stabs/100 percent.wav");
    c = Buffer.read(s,"/Users/aelazary/Desktop/Samples etc./EchoThiefImpulseResponseLibrary/Brutalism/GeiselLibrary.wav");
    d = Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./EchoThiefImpulseResponseLibrary/Miscellaneous/MillsArtMuseum.wav");
)

(
Pdef(\player,
	Pspawner({| sp |
		var pad;
		var beat;

		sp.par(Pbind(\instrument, \rest, \tempo, 150/60));

		pad = sp.par(
			Pmono(
				\warpSlicer,
				\amp, ~slider.(0),
				\dur, 0.01,
				\buf, ~shPad2.at(\file),
				// \posRate, ~pmodenv.(Pwhite(0.5, 2, inf), 0.5),
				// \posRate, ~knob.(6).linlin(0,1,0.2,2),
				\posRate, 0.01,
				\oneshot, 0,
				\sliceStart, ~knob.(5).linlin(0,1,0,300),
				\pitch, -21.midiratio,
				// \windowSize, ~pmodenv.(Pwhite(0.2, 1, inf), Pkey(\dur)),
				\windowSize, ~knob.(6).linlin(0,1,0.1,0.999),
				\overlaps, 8, 
				\windowRandRatio, ~knob.(7).linlin(0,1,0,0.999),
				\rel, 5,
				\out, ~bus1,
				\slice, ~pGetSlice.((Pseries(1, 32, inf).wrap(0, 32) + Pkey(\sliceStart)), ~shPad2),
			)
		);

		sp.par(
			Pbind(
				\instrument, \specSlicer,
				\amp, ~slider.(1),
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
				\buf, ~bow.at(\file),
				\rate, ~knob.(4).linlin(0,1,0.25,2),
				\oneshot, 1,
				\swap, ~knob.(2),
				\smooth, ~knob.(3),
				\slice_A, ~pGetSlice.((Pseries(1, 32, inf).wrap(0, 32) + ~knob.(0).linlin(0,1,0,300).floor), ~bow),
				\slice_B, ~pGetSlice.((Pseries(1, 32, inf).wrap(0, 32) + ~knob.(1).linlin(0,1,0,300).floor + 1), ~bow),
				\out, ~bus2,
			)
		);

	})
);
)

(
Pdef(\player,
	Pspawner({| sp |

		sp.par(Pbind(\instrument, \rest, \tempo, 60/60));

		sp.par(
			Pmono(
				\warpSlicer,
				\amp, ~slider.(0),
				\dur, 0.01,
				\buf, ~shPad1.at(\file),
				// \posRate, ~pmodenv.(Pwhite(0.5, 2, inf), 0.5),
				// \posRate, ~knob.(6).linlin(0,1,0.2,2),
				\posRate, 0.01,
				\oneshot, 0,
				\sliceStart, ~knob.(5).linlin(0,1,0,300),
				\pitch, -10.midiratio,
				// \windowSize, ~pmodenv.(Pwhite(0.2, 1, inf), Pkey(\dur)),
				\windowSize, ~knob.(6).linlin(0,1,0.1,0.999),
				\overlaps, 8, 
				\windowRandRatio, ~knob.(7).linlin(0,1,0,0.999),
				\rel, 5,
				\out, ~roar,
				\slice, ~pGetSlice.((Pseries(1, 32, inf).wrap(0, 32) + Pkey(\sliceStart)), ~shPad1),
			)
		);

		sp.par(
			Pbind(
				\instrument, \specSlicer,
				\amp, ~slider.(1),
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
				\buf, ~skate.at(\file),
				// \rate, ~knob.(4).linlin(0,1,0.25,2),
				\rate, 1,
				\oneshot, 1,
				\swap, ~knob.(2),
				\smooth, ~knob.(3),
				\slice_A, ~pGetSlice.((Pseries(1, 32, inf).wrap(0, 32) + ~knob.(0).linlin(0,1,0,300).floor), ~skate),
				\slice_B, ~pGetSlice.((Pseries(1, 32, inf).wrap(0, 32) + ~knob.(1).linlin(0,1,0,300).floor + 1), ~skate),
				\out, ~roar,
			)
		);

		sp.par(
			Pbindf(
				Pdef(\roar),
				\dur, 0.01,
				\drive, 5.0,
				\tone, ~pmodenv.(Pseq([-0.99, 0.9], inf), 3, 1, \sine),
				\toneFreq, 500.0,
				//changed 1.0
				\toneComp, 1.0,
				\drywet, ~slider.(6),
				\bias, 0,
				// \filterFreq, 6000,
				\filterLoHi, ~pmodenv.(Pseq([0, 0.75], inf), 4, 1, \sine),
				\filterBP, 0,
				\filterRes, 0.3,
				\filterBW, 0.5,
				\filterPre, 1.0,
				\feedAmt, 7.0,
				\feedFreq, 50.0,
				\feedBW, 0.1,
				\feedDelay, 0.1,
				\feedGate, 0.05,
				\gain, -9.0,
				\amp, 1.0,
				\out, ~mainout
			)
		);

	})
);
)

(
Pdef(\player,
	Pspawner({| sp |
		var pad;
		var beat;
		var comp;

		sp.par(Pbind(\instrument, \rest, \tempo, 150/60));

		pad = sp.par(
			Pmono(
				\warpSlicer,
				\amp, ~slider.(0),
				\dur, 0.01,
				\buf, ~shPad5.at(\file),
				// \posRate, ~pmodenv.(Pwhite(0.5, 2, inf), 0.5),
				// \posRate, ~knob.(6).linlin(0,1,0.2,2),
				\posRate, 0.01,
				\oneshot, 0,
				\sliceStart, ~knob.(5).linlin(0,1,0,300),
				\pitch, -12.midiratio,
				// \windowSize, ~pmodenv.(Pwhite(0.2, 1, inf), Pkey(\dur)),
				\windowSize, ~knob.(6).linlin(0,1,0.1,0.999),
				\overlaps, 8, 
				\windowRandRatio, ~knob.(7).linlin(0,1,0,0.999),
				\rel, 5,
				\out, ~bus1,
				\slice, ~pGetSlice.((Pseries(1, 32, inf).wrap(0, 32) + Pkey(\sliceStart)), ~shPad5),
			)
		);

		sp.par(
			Pbind(
				\instrument, \warpSlicer,
				\amp, ~slider.(1),
				\speed, ~slider.(2).linlin(0, 1, 0.25, 16).ceil.reciprocal * 4,
				\chance, ~slider.(3),
				\atk, ~slider.(4).linlin(0,1,0.01,2),
				\rel, ~slider.(5).linlin(0,1,0.25,2),
				\pitch, ~slider.(6).linlin(0,1,0.25,2),
				\dur, Pfunc({|ev|
					var val;
					var chance = ev[\chance].coin;
					var speed = ev[\speed];
					if(chance == true) {val = speed;}{ val = Rest(speed)};
					val;
				}),
				\buf, ~break.at(\file),
				// \sliceStart, ~knob.(1).linlin(0,1,0,300),
				\slice, ~pGetSlice.((Pseries(1, 32, inf).wrap(0, 32) + ~knob.(0).linlin(0,1,0,300).floor), ~break),
				\windowRandRatio, ~knob.(1).linlin(0,1,0,0.999),
				\windowSize, ~knob.(1).linlin(0,1,0.1,0.999),
				\overlaps, 8,
				\posRate, 1,
				\oneshot, 1,
				\out, ~bus2,
			)
		);

	})
);
)

(
Pdef(\player,
	Pspawner({| sp |

		sp.par(Pbind(	\instrument, \rest, \tempo, 60/60));

		sp.par(
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

		sp.par(
			Pmono(\fftStretch_magAbove_mono,
				\dur, 0.01,
				\amp, ~slider.(0),
				\gain, 8,
				\buf, ~specBuff.at(\file),
				\analysis, [~specBuff.at(\analysis)],
				\fftSize, ~specBuff.at(\fftSize),
				\rate, ~knob.(5),

				// \pos, 0.15,
				// \pos, 0.25,
				// \pos, 0.01,
				// \pos, 0.55,
				// \pos, 0.7,

				\filter, ~knob.(6),
				\pos, ~knob.(7),
				\len, 0.2,
				\pan, ~pmodenv.(Pwhite(-0.6, 0.6), 3, 1, \sin),
				\out, [~multiband, ~reverb]
			)
        );

		sp.par(
			Pbind(
				\instrument, \specSlicer,
				\amp, ~slider.(1),
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
				\buf, ~skate.at(\file),
				// \rate, ~knob.(4).linlin(0,1,0.25,2),
				\rate, 1,
				\oneshot, 1,
				\swap, ~knob.(2),
				\smooth, ~knob.(3),
				\slice_A, ~pGetSlice.((Pseries(1, 32, inf).wrap(0, 32) + ~knob.(0).linlin(0,1,0,300).floor), ~guitar),
				\slice_B, ~pGetSlice.((Pseries(1, 32, inf).wrap(0, 32) + ~knob.(1).linlin(0,1,0,300).floor + 1), ~guitar),
				\out, ~multiband,
			)
		);
	})
);
)

Pdef(\player).stop;
Pdef(\player).play(t);

(

Pdef(\player,
Pspawner({| sp |
    var sectionLength;
    		//fx const
		sp.par(
			Pbindf(
				Pdef(\miVerb),
                \dur, 0.1,
				\time, 0.6,
				\damp, 0.6,
				\hp, 0.0,
				\freeze, 0,
				\diff, 0.9,
				\gain, -24,
				\out, ~mainout
			)
		);
        
        sp.par(
            Pdef(\mic1,
                Pmono(\input,
                    \in, 0,
                    \amp, ~slider.(0).lincurve(0,1,0,1,-4),
                    \out, [~convolve_B],
                    \dur, 0.1
                )
            )
        );

        sp.par(
            Pmono(\padKlank_mono,
                \dur, 0.1,
                \f, 15,
                \harmonicRatio, 0.75,
                \bw, 1,
                \bwScale, 1,
                \stretch, 1,
                \detuneRate, 800,
                \detuneDepth, 1,
                \impulse, 20000,
                \dev, 1,
                \amp, ~slider.(2).lincurve(0,1,0,1,-4),
                \gain, -24,
            )
        );

        sp.par(
			Pbindf(
				Pmono(\convolved_pulsar_mono),
                \dur, 0.1,
				\buf, b,
				\window, 4096,
				\triggerRate, ~knob.(3).linlin(0,1,3,20),
				\fluxMF, 1.5,
				\fluxMD, 1,
				\grainFreq, 300,
				\overlap, 1,
				\pmRatio, 2,
				\pmIndex, 100,
				\density, ~knob.(4) - 0.01,
				\polarityMod, 1,
				\gain, 0,
                \amp, ~slider.(3).lincurve(0,1,0,1,-4),
				\out, [~convolve_A]
			)
		);

        Pdef(\fb1mod,
            Pbind(
                \dur, 0.1,
                \amp, ~slider.(4).lincurve(0,1,0,0.5,-4),
                \dec, Pkey(\dur) * 2,
                \feedback, ~pmodenv.(Pseq([0.2, 0.4, 0.8] * 0.5,inf), Pkey(\dec)),
                \time, ~pmodenv.(Pseq(([0.005, 0.001, 0.010] * 100),inf), Pkey(\dec)),
                \damp, ~pmodenv.(Pseq([0.1, 1],inf), Pkey(\dec)),
                \exciter, ~pmodenv.(Pwhite(1, 0, inf).lincurve(0, 1, 0, 1, -8), Pkey(\dec), 1, Pseq([\sine],inf)),
                \impulse, ~pmodenv.(Pwhite(20000, 200,inf), Pkey(\dec)),
                \spont, ~pmodenv.(Pseq([60,1000],inf), Pkey(\dec)),
                \boost,  ~pmodenv.(Pseq([20000,200],inf), Pkey(\dec)),
                \restore, 5,
                \dist,  ~pmodenv.(Pseq([16, 32],inf), Pkey(\dec)),
                \rev, ~pmodenv.(Pexprand(0.1, 4, inf), Pkey(\dec)),
                \pan, ~pmodenv.(Pwhite(-0.3, 0.3, inf), Pkey(\dec)),
                // \gain, -12,
            )
        );

        sp.par(
            Pbindf(
                Pdef(\seq3,
                    // Pbind(\lag, 16)
                    Pmono(\fb1_mono)
                    <> Pdef(\fb1mod) 
                ),
                \out, [~convolve_A, ~miVerb]
            )
        );

		sp.par(
			Pbindf(
				Pdef(\morph),
                \dur, 0.1,
				\gain, 0,
				\atk, 500,
				\rel, 100,
				\swap, ~slider.(1).linexp(0,1,0.1,1),
                \drywet, ~knob.(7),
				\out, [~pitchShift]
			)
		);

        sp.par(
			Pbindf(
				Pdef(\pitchShift),
                \dur, 0.1,
                \pitchRatio, 2.0,
                \windowSize, ~knob.(5),
                \pitchDispersion, ~slider.(6),
                \timeDispersion, ~slider.(7),
                \drywet, ~knob.(6),
                \gain, 0.0,
				\out, [~mainout, ~miVerb]
			)
		);
})
)
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
                \out, [~mainout]
            )
        );

        // sp.par(
        //     Pbindf(
        //         Pdef(\morph),
        //         \dur, 0.1,
        //         \gain, 0,
        //         \atk, 500,
        //         \rel, 100,
        //         \swap, ~slider.(6).linexp(0,1,0.1,1),
        //         \drywet, ~slider.(7),
        //         \out, [~mainout]
        //     )
        // );

    })
);
)


Pdef(\player).clear
Pdef(\player).play

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
                        \in, 0,
                        \amp, ~slider.(2).lincurve(0,1,0,1,-4),
                        \dur, 0.1,
                        \out, [~fftStretchLive],
                    )
                )
            );
            
            sp.par(
                Pdef(\guitar,
                    Pmono(\input,
                        \in, 1,
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

            // sp.par(
            //     Pbindf(
            //         Pdef(\morph),
            //         \dur, 0.1,
            //         \gain, 0,
            //         \atk, 500,
            //         \rel, 100,
            //         \swap, ~slider.(6).linexp(0,1,0.1,1),
            //         \drywet, ~slider.(7),
            //         \out, [~mainout]
            //     )
            // );
    })
    )
)