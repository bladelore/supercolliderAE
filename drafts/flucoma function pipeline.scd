s.boot;

//functions
(
~clearAnalysisDict = {|dict|
	dict.keysValuesDo{ |key, value|
		if(value.isKindOf(Buffer)){
			value.free;
		};
		dict.removeAt(key);
	};
	dict = ();
};

~getSlices = {|file, dict, threshold|
	dict.put(\filepath, file);
	dict.put(\file, Buffer.readChannel(s, file, channels: [0]));
	dict.put(\indices, Buffer(s));
	//nrt onset slice of source
	FluidBufOnsetSlice.processBlocking(s, dict.at(\file), metric: 9, threshold: threshold, indices: dict.at(\indices), action: {"found slices".postln});
};

~sortSlices = {|measure=\centroid, dict|
	//get
	var indices = dict.at(\indices);
	var file = dict.at(\file);
	var spec, stats, meanfeatures;
	//get and set new analysis buffers
	dict.put(\spec, Buffer(s));
	dict.put(\stats, Buffer(s));
	dict.put(\meanfeatures, Buffer(s));
	//vars
	spec = dict.at(\spec);
	stats = dict.at(\stats);
	meanfeatures = dict.at(\meanfeatures);
	//analysis
	indices.loadToFloatArray(action: {
		arg fa;
		//iterate through adjacent pairs of indices (tuple like)
		fa.doAdjacentPairs{
			arg start, end, i;
			var numSamps = end - start;
			//compute spectral features per fft frame (w selected feature)
			FluidBufSpectralShape.processBlocking(s, file, start, numSamps, features: spec, select:[measure]);
			//buf stats channels: mean std skew kurtosis min median max
			FluidBufStats.processBlocking(s, spec, stats: stats, select:[\mean]);
			FluidBufCompose.processBlocking(s, stats, destination: meanfeatures, destStartFrame: i);
		};
		//get indices
		dict.put(\onsetArr, fa);
		dict.put(\size, fa.size);
		//get INDICES of sorted features
		meanfeatures.loadToFloatArray(action: { arg fa; dict.put(\sortedIndices, fa.order) });
		"done analysis".postln;
	});

};

~analyzeSlices = {|file, dict, thresh, metric|
	~clearAnalysisDict.(dict);
	~getSlices.(file, dict, thresh);
	~sortSlices.(metric, dict);
};


~getSlice = {|slice, dict|
	var sortedIndices = dict.at(\sortedIndices);
	var onsets = dict.at(\onsetArr);
	var startIdx = sortedIndices.wrapAt(slice);
	var endIdx = startIdx + 1;
	var startOnset = onsets.at(startIdx);
	var endOnset = onsets.at(endIdx);
	[startOnset, endOnset];
};

~pGetSlice = {|generator, dict|
	Pcollect ({ |i| ~getSlice.(i, dict).asRef; }, generator);
};
)

//run pipeline
//one sample, sorted low-high by pitch
(
a = Dictionary();
~clearAnalysisDict.(a);
~getSlices.(Platform.resourceDir +/+ "sounds/a11wlk01.wav", a, threshold: 0.2);
~sortSlices.(\centroid, a);
)

//another sample, sorted low-high by volume
(
b = Dictionary();
~clearAnalysisDict.(b);
//or your sample here etc can be diff from first
~getSlices.(Platform.resourceDir +/+ "sounds/a11wlk01.wav", b, threshold: 0.2);
~sortSlices.(\crest, b);
)

//another sample, sorted low-high by 'noisiness'
(
c = Dictionary();
~clearAnalysisDict.(c);
~getSlices.(Platform.resourceDir +/+ "sounds/a11wlk01.wav", c, threshold: 0.2);
~sortSlices.(\flatness, c);
)


//instruments
(
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

SynthDef(\grainSlicer, { |buf, gate=1, slice=#[0, 1]|
	var bufFrames, t, dur, offset, startsamp, endsamp, sampsDur, granPhasor;
	var polarity, polarityProb, pan, grains;

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
)

//normal sampler, a dict
(
Pdef(\seg_pattern,
	PmonoArtic(\segPlayer) <>
	Pbind(
		\amp, 0.5,
		\buf, a.at(\file),
		//lowest to highest
		\overlap, 100,
		\slice, ~pGetSlice.(Pseries(0, 1, inf).wrap(0, 32), a),
		\dur, 0.25
	)
).play(t);
)

//grain slice sampler, b dict
(
Pdef(\seg_pattern,
	PmonoArtic(\grainSlicer) <>
	Pbind(
		\amp, 1,
		\buf, b.at(\file),
		//lowest to highest
		\overlap, 100,
		\slice, ~pGetSlice.(Pseries(0, 1, inf).wrap(0, 32), b),
		\dur, 0.25
	)
).play(t);
)

//grain slice sampler mono, c dict
(
Pdef(\grainPattern, Pmono(\grainSlicer_mono) <> Pbind(
	\amp, 0.25,
	\dur, Pwhite(0.25, 1, inf) * 2,
	\dec, Pkey(\dur) * 1,
	\buf, d.at(\file),
	\slice, ~pGetSlice.(Pseries(0, 1, inf).wrap(0, 32).stutter(1), d),
	\overlap, 100,
	)
).play(t);
)
