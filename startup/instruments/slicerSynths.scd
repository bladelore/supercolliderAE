(

SynthDef(\samplePlayer, {|buf, loop=1|
	var sig, sample, pan, panned, numChans;

	numChans = buf.numChannels;

	sig = PlayBuf.ar(2, buf, loop: loop, rate: \rate.kr(1), startPos: \startPos.kr(0));

	pan = \pan.kr(0);
	panned = case
	{numChans == 1} {Pan2.ar(sig, pan)}
	{numChans == 2} {Balance2.ar(sig[0], sig[1], pan)}
	{numChans > 2} { var splay = Splay.ar(sig); Balance2.ar(splay[0], splay[1], pan); } ;

	sig = sig * \amp.kr(1);
	sig = sig * \gain.kr(0).dbamp;
	Out.ar(\out.kr(0), sig);
}).add;

SynthDef(\grainSlicer, { |buf, gate=1, slice=#[0, 1],oneshot=0|
	var bufFrames, trigRate, t, dur, offset, startsamp, endsamp, sampsDur, posRate, phasor, line, granPhasor;
	var polarity, polarityProb, pan, grains;

	bufFrames = BufFrames.ir(buf);
	trigRate = \trigRate.kr(100);
	t = Impulse.ar(trigRate);
	// t = HPZ1.ar(HPZ1.ar(Phasor.ar(rate: trigRate * SampleDur.ir))) > 1e-5;
	dur = trigRate.reciprocal * \overlap.kr(1) * \rate.kr(1).reciprocal;

	offset = \offset.kr(0);
	startsamp = (slice[0] + offset) / bufFrames;
	endsamp = (slice[1] + offset) / bufFrames;
	sampsDur = endsamp - startsamp;

	posRate = \posRate.kr(0.5);

	line= Line.ar(
			start: startsamp,
			end: endsamp,
			dur: (sampsDur / s.sampleRate) * BufRateScale.kr(buf) * posRate.reciprocal
	);


	phasor = Phasor.ar(
		rate: (1 / bufFrames * BufRateScale.kr(buf)) * posRate,
		start: startsamp,
		end: endsamp,
		// trig: gate,
		resetPos: startsamp
	);

	granPhasor = Select.ar(oneshot, [phasor, line]);

	granPhasor = Wrap.ar(granPhasor + (\pos.kr(0) * sampsDur), startsamp, endsamp);

	// polarityProb = Drand([-1 + Diwhite(0, \polarityMod.kr(1), inf), 1], inf) * -1;
	// polarityProb = (polarityProb * 2 - 1);
	// polarity = Demand.ar(t, 0, polarityProb);

	polarity = ~velvet.(t, \density.kr(1), 1 - \polarityMod.kr(1));
	
	pan = Demand.ar(t, 0, Drand([-1, 0, 1] * \panMod.kr(1), inf));

	grains = TGrains.ar(
		numChannels: 2,
		trigger: t,
		bufnum: buf,
		rate: \rate.kr(1),
		centerPos: (granPhasor * BufDur.kr(buf)) + (dur * 0.5001),
		dur: dur,
		pan: pan,
		amp: polarity,
		interp: 2
	);

	grains = grains * EnvGen.ar(Env.adsr(\atk.kr(0.04), 0.2, 0.6, \dec.kr(0.2)), gate, doneAction: Done.freeSelf);
	grains = Balance2.ar(grains[0], grains[1], \pan_master.kr(0)) * \gain.kr(0).dbamp;
	grains = grains * \amp.kr(1);
	Out.ar(\out.kr(0), grains);
}).add;

SynthDef(\grainSlicer_mono, { |buf, slice = #[0, 1]|
	var bufFrames, t, dur, offset, startsamp, endsamp, sampsDur, granPhasor;
	var polarity, polarityProb, pan, grains, env;

	bufFrames = BufFrames.ir(buf);
	t = Impulse.ar( \trigRate.kr(100));
	dur = \trigRate.kr(100).reciprocal * \overlap.kr(1) * \rate.kr(1).reciprocal;

	offset = \offset.kr(0);
	startsamp = (slice[0] + offset) / bufFrames;
	endsamp = (slice[1] + offset) / bufFrames;
	sampsDur = endsamp - startsamp;

	granPhasor = Phasor.ar(
		rate: (1 / bufFrames * BufRateScale.kr(buf)) * \posRate.kr(0.5),
		start: startsamp,
		end: endsamp,
		//trig: gate,
		resetPos: startsamp
	);

	granPhasor = Wrap.ar(granPhasor + (\pos.kr(0) * sampsDur), startsamp, endsamp);

	// polarityProb = Drand([-1 + Diwhite(0, \polarityMod.kr(1), inf), 1], inf) * -1;
	// polarityProb = (polarityProb * 2 - 1);
	// polarity = Demand.ar(t, 0, polarityProb);

	polarity = ~velvet.(t, \density.kr(1), 1 - \polarityMod.kr(1));

	pan = Demand.ar(t, 0, Drand([-1, 0, 1] * \panMod.kr(1), inf));

	grains = TGrains.ar(
		numChannels: 2,
		trigger: t,
		bufnum: buf,
		rate: \rate.kr(1),
		centerPos: (granPhasor * BufDur.kr(buf)) + (dur * 0.5001),
		dur: dur,
		pan: pan,
		amp: polarity,
		interp: 2
	);

	grains = Balance2.ar(grains[0], grains[1], \pan_master.kr(0)) * \gain.kr(0).dbamp;
	grains = Compander.ar(grains, grains,
		thresh: 0.5,
		slopeAbove: 0.5,
		clampTime: 0.1,
		relaxTime: 1,
	);
	grains = grains * \amp.kr(1);
	grains = grains.sanitize;
	Out.ar(\out.kr(0), grains);
}).add;

SynthDef(\segPlayer, {|buf, slice = #[0, 1]|
	var offset, startsamp, endsamp, sig, numChans, pan, panned, env;
	//slice index
	offset = \offset.kr(0);
	startsamp = slice[0] + offset;
	endsamp = slice[1] + offset;
	//channels and sampler
	// numChans = buf.numChannels;
	numChans = 2;
	// numChans = \stereo.ir(1);
	sig = PlayBuf.ar(numChans, buf, BufRateScale.ir(buf) * \rate.kr(1), 0, startsamp, loop: 0);
	//handle channels
	pan = \pan.kr(0);
	panned = case
	{numChans == 1} {Pan2.ar(sig, pan)}
	{numChans == 2} {Balance2.ar(sig[0], sig[1], pan)}
	{numChans > 2} { var splay = Splay.ar(sig); Balance2.ar(splay[0], splay[1], pan); } ;
	//envelope
	env = EnvGen.ar(Env.perc(\atk.kr(0.01), \rel.kr(0.5), curve: \curve.kr(-4)), gate: \gate.kr(1), doneAction: 2);
	//out
	sig = panned * env * \gain.kr(0).dbamp;

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

SynthDef(\segPlayer_stereo, {|buf, slice = #[0, 1]|
	var offset, startsamp, endsamp, sig, numChans, pan, panned, env;
	//slice index
	offset = \offset.kr(0);
	startsamp = slice[0] + offset;
	endsamp = slice[1] + offset;
	//channels and sampler
	numChans = 2;
	// numChans = \stereo.ir(1);
	sig = PlayBuf.ar(numChans, buf, BufRateScale.ir(buf) * \rate.kr(1), 0, startsamp, loop: 0);
	//handle channels
	pan = \pan.kr(0);
	panned = case
	{numChans == 1} {Pan2.ar(sig, pan)}
	{numChans == 2} {Balance2.ar(sig[0], sig[1], pan)}
	{numChans > 2} { var splay = Splay.ar(sig); Balance2.ar(splay[0], splay[1], pan); } ;
	//envelope
	env = EnvGen.ar(Env.perc(\atk.kr(0.01), \rel.kr(0.5), curve: \curve.kr(-4)), gate: \gate.kr(1), doneAction: 2);
	//out
	sig = panned * env * \gain.kr(0).dbamp;

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


~rampOneShot = { |trigIn, duration, cycles = 1|
	var trig = Trig1.ar(trigIn, SampleDur.ir);
	var hasTriggered = PulseCount.ar(trig) > 0;
	var phase = Sweep.ar(trig, 1 / duration).clip(0, cycles);
	phase * hasTriggered;
};

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
			dur: (sampsDur_A / BufSampleRate.kr(buf)) * rate.reciprocal
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
			dur: (sampsDur_B / BufSampleRate.kr(buf)) * rate.reciprocal
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
	sig = Balance2.ar(sig[0], sig[1], \pan.kr(0));
    sig = sig * env;
    sig = sig * \gain.kr(0).dbamp;
    sig = sig * \amp.kr(0);
	Out.ar(\out.kr(0), sig);
}).add;

SynthDef(\warpSlicer, { |buf, gate=1, slice=#[0, 1], oneshot=1|
    var bufFrames, offset, startsamp, endsamp, sampsDur, posRate, phasor, line, phasor_select;
	var polarity, polarityProb, pan, sig, env, doneType;

	bufFrames = BufFrames.ir(buf);

	offset = \offset.kr(0);
	startsamp = (slice[0] + offset) / bufFrames;
	endsamp = (slice[1] + offset) / bufFrames;
	sampsDur = endsamp - startsamp;

	posRate = \posRate.kr(1);

	line=~rampOneShot.(gate, sampsDur * posRate, 1);

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
