(
SynthDef(\morphFollow, {
	var fftsize = 2048;
	var in1, in2, envfollow, sig;
	var chain_L, chain2_L, chain_R, chain2_R;

	in1 = InFeedback.ar(\inbus_A.kr(0), 2);
	in2 = InFeedback.ar(\inbus_B.kr(0), 2);

	chain_L = FFT(LocalBuf(fftsize), in1[0]);
	chain_R = FFT(LocalBuf(fftsize), in1[1]);
	chain2_L = FFT(LocalBuf(fftsize), in2[0]);
	chain2_R = FFT(LocalBuf(fftsize), in2[1]);

	envfollow = Amplitude.ar(in1, \atk.kr(0.01), \rel.kr(1));

	chain_L = chain_L.pvcalc2(chain2_L, fftsize, { |mags, phases, mags2, phases2|
		[
			mags * mags2 + envfollow,
			phases2 + phases

		]
	}, frombin: 0, tobin: 125, zeroothers: 0);

	chain_R = chain_R.pvcalc2(chain2_R, fftsize, { |mags, phases, mags2, phases2|
		[
			mags * mags2 + envfollow,
			phases2 + phases

		]
	}, frombin: 0, tobin: 125, zeroothers: 0);

	sig = IFFT([chain_L, chain_R]);
	sig = sig * \gain.kr(0).dbamp;
	sig = sig * \amp.kr(1);
	Out.ar(\out.kr(0), sig);
}).add;

SynthDef(\cepstralMorph, {|buf1, buf2|
	var in, in2, chain, chain2, cepsch, cepsch2, sig;
	var fftsize, fftbufc, fftbufm, cepbufc, cepbufm, envc, envm;
	fftsize = 2048;
	//fft buffers
	fftbufc = LocalBuf(fftsize);
	fftbufm = LocalBuf(fftsize);
	//cepstrum buffers
	cepbufc = LocalBuf(fftsize * 0.5);
	cepbufm = LocalBuf(fftsize * 0.5);
	//spectral envelope buffers
	envc = LocalBuf(fftsize);
	envm = LocalBuf(fftsize);
	// 1. STFT of signal
	// 2. smooth spectral envelope
	// get cepstrum of modulating signal
	in = PlayBuf.ar(1, buf1, BufRateScale.kr(buf1), 1, 0, 1);
	chain = FFT(fftbufc, in);
	cepsch = Cepstrum(cepbufm, chain);
	// get cepstrum of carrier signal
	in2 = PlayBuf.ar(1, buf2, BufRateScale.kr(buf2), 1, 0, 1);
	chain2 = FFT(fftbufm, in2);
	cepsch2 = Cepstrum(cepbufc, chain2);
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
	chain2 = chain2.pvcalc2(envc, fftsize, {|mags, phases, mags2, phases2|
		[mags / (mags2 + 1e-8), (phases - phases2).wrap2(-pi, pi)]
	}, frombin: 0, tobin: 256, zeroothers: 0);
	// 4. multiply flattened spectral carrier frame with smooth spectral envelope
	// of modulator
	chain2 = chain2.pvcalc2(envm, fftsize, {|mags, phases, mags2, phases2|
		[mags * mags2, (phases + phases2).wrap2(-pi, pi)]
	}, frombin: 0, tobin: 256, zeroothers: 0);

	sig = Pan2.ar(IFFT(chain2));
	sig = sig.sanitize;
	Out.ar(\out.kr(0), sig);
}).add;

SynthDef(\cepstralMorph_fx, {|buf1, buf2|
	var in1, in2, inMix1, inMix2, swap, chain, chain2, cepsch, cepsch2, sig;
	var fftsize, fftbufc, fftbufm, cepbufc, cepbufm, envc, envm, envfollow;
	fftsize = 2048;
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
	in1 = InFeedback.ar(\inbus_A.kr(0), 2);
	in2 = InFeedback.ar(\inbus_B.kr(0), 2);

	swap = \swap.kr(0);
	inMix1 = SelectX.ar(swap, [in1, in2]);
	inMix2 = SelectX.ar(1-swap, [in1, in2]);

	chain = FFT(fftbufc, inMix1);
	cepsch = Cepstrum(cepbufm, chain);
	// get cepstrum of carrier signal
	chain2 = FFT(fftbufm, inMix2);
	cepsch2 = Cepstrum(cepbufc, chain2);

	envfollow = Amplitude.ar(inMix2, \atk.kr(0.01), \rel.kr(1));
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

	sig = Pan2.ar(IFFT(chain2));
	sig = sig.sanitize;
	// sig = sig = SelectX.ar(\drywet.kr(1),[inMix1+inMix2,sig]);
	sig = sig = SelectX.ar(\drywet.kr(1),[in1,sig]);
	sig = sig * \gain.kr(0).dbamp;
	sig = sig * \amp.kr(1);
	Out.ar(\out.kr(0), sig);
}).add;

~makeSpec = {|file, dict, fftSize, overlaps=2|
	// var fftQuant = 2**round(log2(fftSize));
	~clearAnalysisDict.(dict);
	dict.put(\filepath, file);
	dict.put(\file, Buffer.readChannel(s, file, channels: [0]));
	dict.put(\fftSize, fftSize);
	dict.put(\overlaps, overlaps);
	dict.put(\analysis, Array.fill(overlaps, {Buffer.alloc(s, fftSize)}));
};

SynthDef(\fftStretch_mono, {|buf, analysis=#[0,1], fftSize|
	var sig;
	var chain;
	var pos;
	var startsamp, endsamp;
	var bufFrames = BufFrames.kr(buf);

	chain = BufFFTTrigger(analysis, 0.5,[0,1], 2);

	startsamp = (\pos.kr(0) * bufFrames);
	endsamp = startsamp + (\len.kr(1) * bufFrames);

	pos = Phasor.ar(
		rate: \rate.kr(1),
		start: startsamp,
		end: endsamp,
	);

    chain = BufFFT_BufCopy(chain, buf, pos, BufRateScale.kr(buf) * \pitchRatio.kr(1));
    chain = BufFFT(chain);
	chain = PV_Diffuser(chain, chain>(-1));
	// chain = PV_MagAbove(chain, MouseX.kr(0, 10));

    sig = Mix(BufIFFT(chain, 0)).dup*0.8;
	sig = sig * \gain.kr(0).dbamp;
	sig = sig * \amp.kr(1);
	sig = sig.sanitize;
	Out.ar(\out.kr(0), sig);
}).add;

SynthDef(\fftStretch_magFilter_mono, {|buf, analysis=#[0,1], fftSize|
	var sig;
	var chain;
	var pos;
	var startsamp, endsamp;
	var bufFrames = BufFrames.kr(buf);


	chain = BufFFTTrigger(analysis, 0.5,[0,1], 2);

	startsamp = (\pos.kr(0) * bufFrames);
	endsamp = startsamp + (\len.kr(1) * bufFrames);

	pos = Phasor.ar(
		rate: \rate.kr(1),
		start: startsamp,
		end: endsamp,
	);

    chain = BufFFT_BufCopy(chain, buf, pos, BufRateScale.kr(buf) * \pitchRatio.kr(1));
    chain = BufFFT(chain);

	chain = PV_MagGate(chain, \thresh.kr(50), \remove.kr(0));
	chain = PV_Diffuser(chain, chain>(-1));

    sig = Mix(BufIFFT(chain, 0)).dup*0.8;

	sig = sig * \gain.kr(0).dbamp;
	sig = sig * \amp.kr(1);
	sig = sig.sanitize;
	Out.ar(\out.kr(0), sig);
}).add;


SynthDef(\fftStretch_magAbove_mono, {|buf, analysis=#[0,1], fftSize|
	var sig;
	var chain;
	var pos;
	var startsamp, endsamp;
	var bufFrames = BufFrames.kr(buf);

	chain = BufFFTTrigger(analysis, 0.5,[0,1], 2);

	startsamp = (\pos.kr(0) * bufFrames);
	endsamp = startsamp + (\len.kr(1) * bufFrames);

	pos = Phasor.ar(
		rate: \rate.kr(1),
		start: startsamp,
		end: endsamp,
	);

    chain = BufFFT_BufCopy(chain, buf, pos, BufRateScale.kr(buf) * \pitchRatio.kr(1));
    chain = BufFFT(chain);

	chain = PV_MagAbove(chain, \filter.kr(20));
	chain = PV_Diffuser(chain, chain>(-1));

    sig = Mix(BufIFFT(chain, 0)).dup*0.8;

	sig = Balance2.ar(sig[0], sig[1], \pan.kr.(0));

	sig = sig * \gain.kr(0).dbamp;
	sig = sig * \amp.kr(1);
	sig = sig.sanitize;
	Out.ar(\out.kr(0), sig);
}).add;

SynthDef(\fftStretch_magBelow_mono, {|buf, analysis=#[0,1], fftSize|
	var sig;
	var chain;
	var pos;
	var startsamp, endsamp;
	var bufFrames = BufFrames.kr(buf);


	chain = BufFFTTrigger(analysis, 0.5,[0,1], 2);

	startsamp = (\pos.kr(0) * bufFrames);
	endsamp = startsamp + (\len.kr(1) * bufFrames);

	pos = Phasor.ar(
		rate: \rate.kr(1),
		start: startsamp,
		end: endsamp,
	);

    chain = BufFFT_BufCopy(chain, buf, pos, BufRateScale.kr(buf) * \pitchRatio.kr(1));
    chain = BufFFT(chain);

	chain = PV_MagBelow(chain, \filter.kr(20));
	chain = PV_Diffuser(chain, chain>(-1));

    sig = Mix(BufIFFT(chain, 0)).dup*0.8;

	sig = sig * \gain.kr(0).dbamp;
	sig = sig * \amp.kr(1);
	sig = sig.sanitize;
	Out.ar(\out.kr(0), sig);
}).add;

SynthDef(\fftStretch_magBelow_mono, {|buf, analysis=#[0,1], fftSize|
	var sig;
	var chain;
	var pos;
	var startsamp, endsamp;
	var bufFrames = BufFrames.kr(buf);


	chain = BufFFTTrigger(analysis, 0.5,[0,1], 2);

	startsamp = (\pos.kr(0) * bufFrames);
	endsamp = startsamp + (\len.kr(1) * bufFrames);

	pos = Phasor.ar(
		rate: \rate.kr(1),
		start: startsamp,
		end: endsamp,
	);

    chain = BufFFT_BufCopy(chain, buf, pos, BufRateScale.kr(buf) * \pitchRatio.kr(1));
    chain = BufFFT(chain);

	chain = PV_MagBelow(chain, \filter.kr(20));
	chain = PV_Diffuser(chain, chain>(-1));

    sig = Mix(BufIFFT(chain, 0)).dup*0.8;

	sig = sig * \gain.kr(0).dbamp;
	sig = sig * \amp.kr(1);
	sig = sig.sanitize;
	Out.ar(\out.kr(0), sig);
}).add;

SynthDef(\fftStretchLive_mono, {|buf, analysis=#[0,1], fftSize|
	var sig;
	var pos;
	var ptr, prev, current;
	var startsamp, endsamp;
	var chain;
	var bufFrames = BufFrames.kr(buf);

	var feedback = (LocalIn.ar(1) * \feedback.ar(0) * 0.9);
	var in = InFeedback.ar(\inbus.kr(0), 1);

	ptr = Phasor.ar(0, \recRate.kr(1), 0, bufFrames);

	prev = BufRd.ar(1, buf, ptr);
	current = XFade2.ar(in + feedback, prev, \overdub.kr(0));
	BufWr.ar(current, buf, ptr);

	chain = BufFFTTrigger(analysis, 0.5,[0,1], 2);

	startsamp = (\pos.kr(0) * bufFrames);
	endsamp = startsamp + (\len.kr(1) * bufFrames);

	pos = Phasor.ar(
		rate: \rate.kr(1),
		start: startsamp,
		end: endsamp,
	);

    chain = BufFFT_BufCopy(chain, buf, pos, BufRateScale.kr(buf) * \pitchRatio.kr(1));
    chain = BufFFT(chain);
	chain = PV_Diffuser(chain, chain>(-1));

    sig = Mix(BufIFFT(chain, 0)).dup*0.8;
	sig = sig * \gain.kr(0).dbamp;
	sig = sig * \amp.kr(1);
	sig = sig.sanitize;
	Out.ar(\out.kr(0), sig);
}).add;

SynthDef(\fftStretchLive_mono, {|buf, analysis=#[0,1], fftSize|
	var sig;
	var pos;
	var ptr, prev, current;
	var startsamp, endsamp;
	var chain, fb;
	var bufFrames = BufFrames.kr(buf);

	var feedback = (LocalIn.ar(1) * \feedback.ar(0) * 0.9);
	var in = InFeedback.ar(\inbus.kr(0), 1);
	var comb_phase_mod;

	ptr = Phasor.ar(0, \recRate.kr(1), 0, bufFrames);

	prev = BufRd.ar(1, buf, ptr);
	current = XFade2.ar(in + feedback, prev, \overdub.kr(0));
	BufWr.ar(current, buf, ptr);

	chain = BufFFTTrigger(analysis, 0.5,[0,1], 2);

	startsamp = (\pos.kr(0) * bufFrames);
	endsamp = startsamp + (\len.kr(1) * bufFrames);

	pos = Phasor.ar(
		rate: \rate.kr(1),
		start: startsamp,
		end: endsamp,
	);

	chain = BufFFT_BufCopy(chain, buf, pos, BufRateScale.kr(buf) * \pitchRatio.kr(1));
	chain = BufFFT(chain);
	chain = PV_MagGate(chain, \thresh.kr(50), \remove.kr(0));
	chain = PV_Diffuser(chain, chain>(-1));

	sig = Mix(BufIFFT(chain, 0)).dup*0.8;

	fb = sig.sum;
	fb = LeakDC.ar(fb, 0.995);
	LocalOut.ar(fb);

	sig = sig * \gain.kr(0).dbamp;
	sig = sig * \amp.kr(1);
	sig = sig.sanitize;
	Out.ar(\out.kr(0), sig);
}).add;

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
	var comb_phase_mod;
    
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
	// overlap = max(overlap, tFreq);

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
    
    chain = PV_Diffuser(chain, chain>(-1));

	comb_phase_mod = \comb_phase_mod.kr(0);
    chain = PV_RectComb(
        chain,
        \num_teeth.kr(32),
        \comb_phase.kr(0) + TRand.kr(comb_phase_mod.neg, comb_phase_mod, voices[\triggers]), 
        \comb_width.kr(0),
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

	sig = sig.blend(in, 1 - \drywet.kr(1));

    sig = sig.softclip;
	sig = sig * \gain.kr(0).dbamp;
	sig = sig * \amp.kr(1).lag;
    
    sig = sig.sanitize;
	Out.ar(\out.kr(0), sig);
}).add;
)