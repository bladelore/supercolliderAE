(
SynthDef(\grainSlicer, { |buf, gate=1, slice=#[0, 1]|
	var bufFrames, trigRate, t, dur, offset, startsamp, endsamp, sampsDur, granPhasor;
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

	granPhasor = Phasor.ar(
		rate: (1 / bufFrames * BufRateScale.kr(buf)) * \posRate.kr(0.5),
		start: startsamp,
		end: endsamp,
		//trig: gate,
		resetPos: startsamp
	);

	granPhasor = Wrap.ar(granPhasor + (\pos.kr(0) * sampsDur), startsamp, endsamp);

	polarityProb = Drand([-1 + Diwhite(0, \polarityMod.kr(1), inf), 1], inf) * -1;
	polarityProb = (polarityProb * 2 - 1);
	polarity = Demand.ar(t, 0, polarityProb);

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

	grains = grains * EnvGen.kr(Env.adsr(\atk.kr(0.04), 0.2, 0.6, \dec.kr(0.2)), gate, doneAction: Done.freeSelf);
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

	polarityProb = Drand([-1 + Diwhite(0, \polarityMod.kr(1), inf), 1], inf) * -1;
	polarityProb = (polarityProb * 2 - 1);
	polarity = Demand.ar(t, 0, polarityProb);

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
	grains = grains * \amp.kr(1);
	grains = grains.sanitize;
	Out.ar(\out.kr(0), grains);
}).add;

SynthDef(\segPlayer, {|buf, slice = #[0, 1]|
	var offset, startsamp, endsamp, numChans, sig, pan, panned, env;
	//slice index
	offset = \offset.kr(0);
	startsamp = slice[0] + offset;
	endsamp = slice[1] + offset;
	//channels and sampler
	numChans = buf.numChannels;
	sig = PlayBuf.ar(numChans, buf, BufRateScale.ir(buf) * \rate.kr(1), 0, startsamp, loop: 0);
	//handle channels
	pan = \pan.kr(0);
	panned = case
	{numChans == 1} {Pan2.ar(sig, pan)}
	{numChans == 2} {Balance2.ar(sig[0], sig[1], pan)}
	{numChans > 2} { var splay = Splay.ar(sig); Balance2.ar(splay[0], splay[1], pan); } ;
	//envelope
	env = EnvGen.kr(Env.perc(\atk.kr(0.01), \rel.kr(0.5), curve: \curve.kr(-4)), gate: \gate.kr(1), doneAction: 2);
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
)