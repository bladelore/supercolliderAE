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

/*(
a=Dictionary();
~makeSpec.("C:/Users/Asher/Desktop/Samples etc/Field recs/Bell birds.wav", a, 16384, 2)
)*/

// Synth(\fftStretch_magFilter_mono, [buf: a.at(\file), analysis: a.at(\analysis), fftSize: a.at(\fftsize), rate: 0.4, filter: 2])

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

    chain = BufFFT_BufCopy(chain, buf, pos, BufRateScale.kr(buf));
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

    chain = BufFFT_BufCopy(chain, buf, pos, BufRateScale.kr(buf));
    chain = BufFFT(chain);

	// chain = PV_MagAbove(chain, \filter.kr(20));
	chain = PV_MagGate(chain, \thresh.kr(50), \remove.kr(0));
	// chain = PV_Compander(chain, 1, 0.1, 1.0);
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

    chain = BufFFT_BufCopy(chain, buf, pos, BufRateScale.kr(buf));
    chain = BufFFT(chain);

	chain = PV_MagAbove(chain, \filter.kr(20));
	// chain = PV_MagGate(chain, \thresh.kr(50), \remove.kr(0));
	// chain = PV_Compander(chain, 1, 0.1, 1.0);
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

    chain = BufFFT_BufCopy(chain, buf, pos, BufRateScale.kr(buf));
    chain = BufFFT(chain);

	chain = PV_MagBelow(chain, \filter.kr(20));
	// chain = PV_MagGate(chain, \thresh.kr(50), \remove.kr(0));
	// chain = PV_Compander(chain, 1, 0.1, 1.0);
	chain = PV_Diffuser(chain, chain>(-1));


    sig = Mix(BufIFFT(chain, 0)).dup*0.8;

	sig = sig * \gain.kr(0).dbamp;
	sig = sig * \amp.kr(1);
	sig = sig.sanitize;
	Out.ar(\out.kr(0), sig);
}).add;

)



