(
SynthDef(\specSlicer, { |buf, gate=1, slice_A=#[0, 1], slice_B=#[0, 1], oneshot=1|
	var bufFrames, trigRate, t, dur, offset; 
    var startsamp_A, endsamp_A, sampsDur_A;
    var startsamp_B, endsamp_B, sampsDur_B;
    var rate;
    var phasor_A, line_A, phasor_select_A;
    var phasor_B, line_B, phasor_select_B;
	var in1, in2, inMix1, inMix2, swap, chain, chain2, cepsch, cepsch2, sig;
	var fftsize, fftbufc, fftbufm, cepbufc, cepbufm, envc, envm, envfollow;
	var chain_L, chain2_L, chain_R, chain2_R, env;

	bufFrames = BufFrames.ir(buf);

	offset = \offset.kr(0);
	startsamp_A = (slice_A[0] + offset);
	endsamp_A = (slice_A[1] + offset);
	sampsDur_A = endsamp_A - startsamp_A;

    startsamp_B = (slice_B[0] + offset);
	endsamp_B = (slice_B[1] + offset);
	sampsDur_B = endsamp_B - startsamp_B;

	rate = \rate.kr(1);    
    // A
	line_A = Line.ar(
			start: startsamp_A,
			end: endsamp_A,
			dur: (sampsDur_A / s.sampleRate) * BufRateScale.kr(buf) * rate.reciprocal
	);

	phasor_A = Phasor.ar(
		rate: BufRateScale.kr(buf) * rate,
		start: startsamp_A,
		end: endsamp_A,
		resetPos: startsamp_A
	);

    //B

    line_B = Line.ar(
			start: startsamp_B,
			end: endsamp_B,
			dur: (sampsDur_B / s.sampleRate) * BufRateScale.kr(buf) * rate.reciprocal
	);
    
	phasor_B = Phasor.ar(
		rate: BufRateScale.kr(buf) * rate,
		start: startsamp_B,
		end: endsamp_B,
		// trig: gate,
		resetPos: startsamp_B
	);

    phasor_select_A = Select.ar(oneshot, [phasor_A, line_A]);
    phasor_select_B = Select.ar(oneshot, [phasor_B, line_B]);

    fftsize = 4096;
	//fft buffers
	fftbufc = LocalBuf(fftsize.dup(2), 1);
	fftbufm = LocalBuf(fftsize.dup(2), 1);
	//cepstrum buffers
	cepbufc = LocalBuf((fftsize * 0.5).dup(2), 1);
	cepbufm = LocalBuf((fftsize * 0.5).dup(2), 1);
	//spectral envelope buffers
	envc = LocalBuf(fftsize.dup(2), 1);
	envm = LocalBuf(fftsize.dup(2), 1);
	// 1. STFT of signal
	// 2. smooth spectral envelope
	// get cepstrum of modulating signal
	in1 = BufRd.ar(2, buf, phasor_select_A);
	in2 = BufRd.ar(2, buf, phasor_select_B);

	swap = \swap.kr(0);
	inMix1 = SelectX.ar(swap, [in1, in2]);
	inMix2 = SelectX.ar(1-swap, [in1, in2]);

	chain = FFT(fftbufc, inMix1);
	cepsch = Cepstrum(cepbufm, chain);
	// get cepstrum of carrier signal
	chain2 = FFT(fftbufm, inMix2);
	cepsch2 = Cepstrum(cepbufc, chain2);

	envfollow = Amplitude.ar(inMix2, \atk_follow.kr(0.01), \rel_follow.kr(1));
	// PV_BrickWall can act as a low-pass filter, or here, as a wol-pass lifter...
	// ...in practical terms, produces a smoothed version of the spectrum
	// get smooth version of modulator
	cepsch = PV_BrickWall(cepsch, -0.95);
	ICepstrum(cepsch, envm);
	// get smoothed version of carrier
	cepsch2 = PV_BrickWall(cepsch2, -0.95);
	ICepstrum(cepsch2, envc);
	// 3. divide spectrum of each carrier frame by
	// smooth spectral envelope (to flatten)
	chain2[0] = chain2[0].pvcalc2(envc[0], fftsize, {|mags, phases, mags2, phases2|
		[mags / (mags2 + 1e-8) + envfollow, (phases - phases2).wrap2(-pi, pi)]
	}, frombin: 0, tobin: 256, zeroothers: 0);
	// 4. multiply flattened spectral carrier frame with smooth spectral envelope
	// of modulator
	chain2[0] = chain2[0].pvcalc2(envm[0], fftsize, {|mags, phases, mags2, phases2|
		[mags * mags2, (phases + phases2).wrap2(-pi, pi)]
	}, frombin: 0, tobin: 256, zeroothers: 0);

	chain2[1] = chain2[1].pvcalc2(envc[1], fftsize, {|mags, phases, mags2, phases2|
		[mags / (mags2 + 1e-8) + envfollow, (phases - phases2).wrap2(-pi, pi)]
	}, frombin: 0, tobin: 256, zeroothers: 0);

	chain2[1] = chain2[1].pvcalc2(envm[1], fftsize, {|mags, phases, mags2, phases2|
		[mags * mags2, (phases + phases2).wrap2(-pi, pi)]
	}, frombin: 0, tobin: 256, zeroothers: 0);

    chain2 = PV_MagSmooth(chain2, 1 - \smooth.kr(1).range(0.00001, 0.9999));
    chain2 = PV_Compander(chain2, 1, 2.0, 1);

	sig = Pan2.ar(IFFT(chain2));
	sig = sig.sanitize;
	// sig = sig = SelectX.ar(\drywet.kr(1),[inMix1+inMix2,sig]);
	sig = sig = SelectX.ar(\drywet.kr(1),[in1,sig]);

    env = EnvGen.kr(Env.perc(attackTime: \atk.kr(0.01), releaseTime: \rel.kr(0.5), level: 1.0), gate, doneAction: oneshot.linlin(0,1,0,2));
    env = Select.kr(oneshot, [1, env]);
    sig = sig * env;
    sig = sig * \gain.kr(0).dbamp;
    sig = sig * \amp.kr(0);
	Out.ar(\out.kr(0), sig);
}).add;
)

(
    ~sliceBufA = Dictionary();
    ~sliceBufB = Dictionary();
    // ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Missing Sounds 2016/04-Hobble_Break_126_PL_1.WAV", ~sliceBuf, 0.3, \crest, chans: 2);
    // ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./contact mics/bow mic.wav", ~sliceBufA, 0.9, \centroid, chans: 2);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./contact mics/guitar chain.wav", ~sliceBufA, 0.9, \centroid, chans: 2);
    // ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./contact mics/skateboard 1.wav", ~sliceBufA, 0.9, \centroid, chans: 2);


    // ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Splice packs/VISIONIST_labeled_processed/VISIONIST_tonal/VISIONIST_melody/VISIONIST_melody_loops/VISIONIST_melody_loop_piano_123_Emin.wav", ~sliceBufB, 0.3, \centroid, chans: 2);
    // ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Silent Hill/Ambience/Silent Hill 4/Pulsating Ambience/MusicFX 10.wav", ~sliceBufB, 0.1, \centroid, chans: 2);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Silent Hill/Ambience/Silent Hill 4/Fortunate Sleep - Cat Scratchism Mix/FEEDIES 1.wav", ~sliceBufB, 0.1, \centroid, chans: 2);
    // ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Silent Hill/Ambience/Silent Hill 4/Subway Moan/Eerie-Edition-CD1_31.WAV", ~sliceBufB, 0.1, \centroid, chans: 2);
    // ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Silent Hill/Ambience/Silent Hill 2/Day of Night/SLEEPCYCL2.wav", ~sliceBufB, 0.1, \centroid, chans: 2);
    // ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Silent Hill/Ambience/Silent Hill Origins/The Wicked End/33 Futhswatering.wav", ~sliceBufB, 0.1, \centroid, chans: 2);
)

(
Pmono(
    \specSlicer,
    \dur, 4,
    \buf, ~sliceBufA.at(\file),
    \amp, 1,
    \rate, 1,
    \oneshot, 0,
    \sliceStart, 0,
	\gate, ~button.(0),
    \swap, ~pmodenv.(Pwhite(0, 1, inf), 0.25),
    \smooth, ~pmodenv.(Pwhite(0, 1, inf), 1),
    \slice_A, ~pGetSlice.((Pseries(1, 32, inf).wrap(0, 32) + Pkey(\sliceStart)), ~sliceBufA),
    \slice_B, ~pGetSlice.((Pseries(1, 32, inf).wrap(0, 32) + Pkey(\sliceStart) + 1), ~sliceBufA),
    \out, ~multiband
).play(t);

Pdef(\multiband).play(t);
)

(
Pbind(
    \instrument, \specSlicer,
    \dur, 2,
    \buf, ~sliceBufA.at(\file),
    \rate, 0.5,
    \oneshot, 1,
    \sliceStart, 50,
    \swap, ~pmodenv.(Pwhite(0, 1, inf), 0.25),
    \smooth, ~pmodenv.(Pwhite(0, 1, inf), 1),
    \atk, 0.5,
    \rel, 1,
    \amp, 1,
    // \slice_A, ~pGetSlice.(9, ~sliceBuf),
    // \slice_B, ~pGetSlice.(1, ~sliceBuf),
    \slice_A, ~pGetSlice.((Pseries(1, 32, inf).wrap(0, 32) + Pkey(\sliceStart)), ~sliceBufA),
    \slice_B, ~pGetSlice.((Pseries(1, 32, inf).wrap(0, 32) + Pkey(\sliceStart) + 1), ~sliceBufA),
    \out, ~multiband
).play(t);

Pdef(\multiband).play(t);
// Pdef(\miVerb).play(t);
)

(
var rampOneShot = { |trigIn, duration, cycles = 1|
	var trig = Trig1.ar(trigIn, SampleDur.ir);
	var hasTriggered = PulseCount.ar(trig) > 0;
	var phase = Sweep.ar(trig, 1 / duration).clip(0, cycles);
	phase * hasTriggered;
};
// line=rampOneShot.(gate, sampsDur * posRate, 1);

SynthDef(\warpSlicer, { |buf, gate=1, slice=#[0, 1], oneshot=1|
    var bufFrames, offset, startsamp, endsamp, sampsDur, posRate, phasor, line, phasor_select;
	var polarity, polarityProb, pan, sig, env, doneType;

	bufFrames = BufFrames.ir(buf);

	offset = \offset.kr(0);
	startsamp = (slice[0] + offset) / bufFrames;
	endsamp = (slice[1] + offset) / bufFrames;
	sampsDur = endsamp - startsamp;

	posRate = \posRate.kr(1);

	line=rampOneShot.(gate, sampsDur * posRate, 1);

	phasor = Phasor.ar(
		trig: DC.ar(0),
		rate: BufRateScale.kr(buf) * posRate * SampleDur.ir / BufDur.kr(buf),
		start: \posLo.kr(0),
		end: \posHi.kr(1)
	);

	phasor = Phasor.ar(
		rate: (1 / bufFrames * BufRateScale.kr(buf)) * posRate,
		start: startsamp,
		end: endsamp,
		// trig: gate,
		resetPos: startsamp
	);

	phasor_select = Select.ar(oneshot, [phasor, line]);

	phasor_select = Wrap.ar(phasor_select + (\pos.kr(0) * sampsDur), startsamp, endsamp);

    sig = WarpZ.ar(2, buf, phasor_select, \pitch.kr(1), \windowSize.kr(0.2), -1, \overlaps.kr(8), \windowRandRatio.kr(0), 4, 0.2, 0.1);

    env = EnvGen.kr(Env.perc(attackTime: \atk.kr(0.01), releaseTime: \rel.kr(0.5), level: 1.0), gate, doneAction: oneshot.linlin(0,1,0,2));
    env = Select.kr(oneshot, [1, env]);    
    sig = sig * env;
    sig = sig * \gain.kr(0).dbamp;
    sig = sig * \amp.kr(0);

    Out.ar(\out.kr(0), sig);
}).add;
)

(
Pmono(
    \warpSlicer,
    \dur, 1,
    \buf, ~sliceBufB.at(\file),
    \posRate, ~pmodenv.(Pwhite(0.5, 2, inf), 0.5),
	\posRate, 0.01,
    \oneshot, 0,
    \sliceStart, 0,
    \pitch, 0.5,
    \windowSize, ~pmodenv.(Pwhite(0.2, 1, inf), Pkey(\dur)),
	\windowSize, 0.2,
    \overlaps, 6, 
    \windowRandRatio, 0,
    \rel, 5,
    \amp, 2,
    \out, ~multiband,
    \slice, ~pGetSlice.((Pseries(1, 32, inf).wrap(0, 32) + Pkey(\sliceStart)), ~sliceBufB),
).play(t);

Pmono(
    \specSlicer,
    \dur, 4,
    \buf, ~sliceBufA.at(\file),
    \amp, 1,
    \rate, 2,
    \oneshot, 0,
    \sliceStart, 280,
    \swap, ~pmodenv.(Pwhite(0, 1, inf), 0.25),
    \smooth, ~pmodenv.(Pwhite(0, 1, inf), 1),
    \slice_A, ~pGetSlice.((Pseries(1, 32, inf).wrap(0, 32) + Pkey(\sliceStart)), ~sliceBufA),
    \slice_B, ~pGetSlice.((Pseries(1, 32, inf).wrap(0, 32) + Pkey(\sliceStart) + 1), ~sliceBufA),
    \out, ~multiband
).play(t);

Pdef(\multiband).play(t);
)

(
Pmono(
    \warpSlicer,
    \dur, 1,
    \buf, ~sliceBufB.at(\file),
    \posRate, ~pmodenv.(Pwhite(0.5, 2, inf), 0.5),
	\posRate, 1,
    \oneshot, 0,
    \sliceStart, 0,
    \pitch, 1,
    \windowSize, ~pmodenv.(Pwhite(0.2, 1, inf), Pkey(\dur)),
	\windowSize, 0.2,
    \overlaps, 8, 
    \windowRandRatio, 0.5,
    \rel, 5,
    \amp, 2,
    \out, ~multiband,
    \slice, ~pGetSlice.((Pseries(1, 32, inf).wrap(0, 32) + Pkey(\sliceStart)), ~sliceBufB),
).play(t);

// Pmono(
//     \specSlicer,
//     \dur, 4,
//     \buf, ~sliceBufA.at(\file),
//     \amp, 1,
//     \rate, 2,
//     \oneshot, 0,
//     \sliceStart, 280,
//     \swap, ~pmodenv.(Pwhite(0, 1, inf), 0.25),
//     \smooth, ~pmodenv.(Pwhite(0, 1, inf), 1),
//     \slice_A, ~pGetSlice.((Pseries(1, 32, inf).wrap(0, 32) + Pkey(\sliceStart)), ~sliceBufA),
//     \slice_B, ~pGetSlice.((Pseries(1, 32, inf).wrap(0, 32) + Pkey(\sliceStart) + 1), ~sliceBufA),
//     \out, ~multiband
// ).play(t);

Pdef(\multiband).play(t);
)


b.stop;

(
Pbind(
    \instrument, \warpSlicer,
    \dur, 0.25,
    \buf, ~sliceBufA.at(\file),
    \posRate, 1,
    \oneshot, 1,
    \sliceStart, 4,
    \pitch, 1,
    \windowSize, 0.1,
    \overlaps, 16, 
    \windowRandRatio, 0,
    \amp, 1,
    \atk, 0.01,
    \rel, 0.25,
	\gate, ~button.(0),
    \slice, ~pGetSlice.((Pseries(1, 32, inf).wrap(0, 32) + Pkey(\sliceStart)), ~sliceBufA).stutter(3),
    // \slice, ~pGetSlice.(1, ~sliceBuf).stutter(3),
    \out, ~multiband
).play(t);

Pdef(\multiband).play(t);
)

z = Group();
Pdef(\player).clear;
Pdef(\player).fadeTime = 8;
Pbindef(\player).play(t);
Pdef(\player).stop;
Pdef(\multiband).play(t);
z.release(5);

(
Pdef(\player,
	Pspawner({| sp |

		sp.par(
			Pmono(
			\warpSlicer,
			\dur, 1,
			\buf, ~sliceBufB.at(\file),
			\posRate, ~pmodenv.(Pwhite(0.5, 2, inf), 0.5),
			\posRate, 1,
			\oneshot, 0,
			\sliceStart, 0,
			\pitch, 1,
			\windowSize, ~pmodenv.(Pwhite(0.2, 1, inf), Pkey(\dur)),
			\windowSize, 0.2,
			\overlaps, 8, 
			\windowRandRatio, 0.5,
			\rel, 5,
			\amp, 2,
			\out, ~multiband,
			\slice, ~pGetSlice.((Pseries(1, 32, inf).wrap(0, 32) + Pkey(\sliceStart)), ~sliceBufB),
		)
		);

		sp.par(
			Pmono(
				\specSlicer,
				\dur, 4,
				\buf, ~sliceBufA.at(\file),
				\amp, 1,
				\rate, 2,
				\oneshot, 0,
				\sliceStart, 280,
				\swap, ~pmodenv.(Pwhite(0, 1, inf), 0.25),
				\smooth, ~pmodenv.(Pwhite(0, 1, inf), 1),
				\slice_A, ~pGetSlice.((Pseries(1, 32, inf).wrap(0, 32) + Pkey(\sliceStart)), ~sliceBufA),
				\slice_B, ~pGetSlice.((Pseries(1, 32, inf).wrap(0, 32) + Pkey(\sliceStart) + 1), ~sliceBufA),
				\out, ~multiband,
			)
		);

		

	})
);
)

(

Pdef(\player,
	Pspawner({| sp |
		sp.par(
			Pmono(
			\warpSlicer,
			\dur, 1,
			\buf, ~sliceBufB.at(\file),
			\posRate, ~pmodenv.(Pwhite(0.5, 2, inf), 0.5),
			\posRate, 0.01,
			\oneshot, 0,
			\sliceStart, 0,
			\pitch, 0.25,
			\windowSize, ~pmodenv.(Pwhite(0.2, 1, inf), Pkey(\dur)),
			\windowSize, 0.2,
			\overlaps, 8, 
			\windowRandRatio, 0.5,
			\rel, 5,
			\amp, 2,
			\out, ~multiband,
			\slice, ~pGetSlice.((Pseries(1, 32, inf).wrap(0, 32) + Pkey(\sliceStart)), ~sliceBufB),
		)
		);

		sp.par(
			Pmono(
				\specSlicer,
				\dur, 4,
				\buf, ~sliceBufA.at(\file),
				\amp, 1,
				\rate, 0.5,
				\oneshot, 0,
				\sliceStart, 280,
				\swap, ~pmodenv.(Pwhite(0, 1, inf), 0.25),
				\smooth, ~pmodenv.(Pwhite(0, 1, inf), 1),
				\slice_A, ~pGetSlice.((Pseries(1, 32, inf).wrap(0, 32) + Pkey(\sliceStart)), ~sliceBufA),
				\slice_B, ~pGetSlice.((Pseries(1, 32, inf).wrap(0, 32) + Pkey(\sliceStart) + 1), ~sliceBufA),
				\out, ~multiband,
			)
		);
	})
);
)

(
var rampOneShot = { |trigIn, duration, cycles = 1|
	var trig = Trig1.ar(trigIn, SampleDur.ir);
	var hasTriggered = PulseCount.ar(trig) > 0;
	var phase = Sweep.ar(trig, 1 / duration).clip(0, cycles);
	phase * hasTriggered;
};

SynthDef(\sliceDesigner, {|buf, gate=1, slice = #[0, 1], rev=1, revTime=1|
	var offset, startsamp, endsamp, sampsDur, sig, numChans, pan, panned, env, line, revLine, rate, revSig, bufFrames;
	//slice index
	bufFrames = BufFrames.ir(buf);
	offset = \offset.kr(0);
	startsamp = (slice[0] + offset);
	endsamp = (slice[1] + offset);
	sampsDur = endsamp - startsamp;
	//channels and sampler
	numChans = buf.numChannels;
	// numChans = \stereo.ir(1);
	// sig = PlayBuf.ar(numChans, buf, BufRateScale.ir(buf) * \rate.kr(1), 0, startsamp, loop: 0);
	rate = \rate.kr(0);
	line=rampOneShot.(gate, sampsDur, 1) * sampsDur;
	line.poll;
	// line=DelayL.ar(line, 10, sampsDur*revTime);
	revLine=rampOneShot.(gate, sampsDur*revTime, 1);

	sig = BufRd.ar(2, buf, line);
	revSig = BufRd.ar(2, buf, revLine) * rev;
	//handle channels
	pan = \pan.kr(0);
	panned = case
	{numChans == 1} {Pan2.ar(sig, pan)}
	{numChans == 2} {Balance2.ar(sig[0], sig[1], pan)}
	{numChans > 2} { var splay = Splay.ar(sig); Balance2.ar(splay[0], splay[1], pan); } ;
	//envelope
	env = EnvGen.ar(Env.perc(\atk.kr(0.01), \rel.kr(0.5), curve: \curve.kr(-4)), gate: gate, doneAction: 2);
	//out
	// sig = panned * env * \gain.kr(0).dbamp;

	sig = SelectX.ar(\pitchMix.kr(0), [sig,
	PitchShift.ar(sig,
		pitchRatio: \pitchRatio.kr(1),
		windowSize: \windowSize.kr(0.01),
		pitchDispersion: \pitchDispersion.kr(1),
		timeDispersion: \timeDispersion.kr(0.1))]
	);
	sig = sig * \amp.kr(1);
	Out.ar(\out.kr(0), sig);
}).add;
)
(
	
Pbind(
    \instrument, \sliceDesigner,
    \dur, 1,
	\amp, 1,
    \buf, ~sliceBufA.at(\file),
	\sliceStart, 4,
    \slice, ~pGetSlice.((Pseries(1, 32, inf).wrap(0, 32) + Pkey(\sliceStart)), ~sliceBufA).stutter(3),
    // \slice, ~pGetSlice.(1, ~sliceBuf).stutter(3),
    \out, ~mainout
).play(t);

)