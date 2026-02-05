(
SynthDef(\grainSlicer_mono_new, { |buf, slice = #[0, 1]|
	var bufFrames, t, dur, offset, startsamp, endsamp, sampsDur, granPhasor;
	var polarity, polarityProb, pan, grains, env;

	bufFrames = BufFrames.ir(buf);
	t = Impulse.ar( \trigRate.kr(100));
	dur = \trigRate.kr(100).reciprocal * \overlap.kr(1) * \rate.kr(1).reciprocal;

	offset = \offset.kr(0);
	startsamp = (slice[0] + offset) / bufFrames;
	endsamp = (slice[1] + offset) / bufFrames;
	sampsDur = endsamp - startsamp;
	endsamp.poll;

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

SynthDef(\grainSlicer_mono, { |bufnum|
	var polarity, polarityProb, pan, grains, env;
	var bufFrames = BufFrames.ir(bufnum);
	var t = Impulse.ar( \trigRate.kr(100));
	var dur = \trigRate.kr(100).reciprocal * \overlap.kr(1) * \rate.kr(1).reciprocal;

	var this_idx = Select.kr(\slice.kr(0), ~featuresSorted);
	var startsamp = (Select.kr(this_idx, ~onsetArr) / bufFrames);
	var endsamp = (Select.kr(this_idx + 1, ~onsetArr) / bufFrames);
	var sampsDur = endsamp - startsamp;

	var granPhasor = Phasor.ar(
		rate: (1 / bufFrames * BufRateScale.kr(bufnum)) * \posRate.kr(0.5),
		start: startsamp,
		end: endsamp,
		//trig: gate,
		resetPos: startsamp
	);
	endsamp.poll;
	// granPhasor.poll;
	granPhasor = Wrap.ar(granPhasor + (\pos.kr(0) * sampsDur), startsamp, endsamp);

	polarityProb = Drand([-1 + Diwhite(0, \polarityMod.kr(1), inf), 1], inf) * -1;
	polarityProb = (polarityProb * 2 - 1);
	polarity = Demand.ar(t, 0, polarityProb);

	pan = Demand.ar(t, 0, Drand([-1, 0, 1] * \panMod.kr(1), inf));

	grains = TGrains.ar(
		numChannels: 2,
		trigger: t,
		bufnum: bufnum,
		rate: \rate.kr(1),
		centerPos: (granPhasor * BufDur.kr(bufnum)) + (dur * 0.5001),
		dur: dur,
		pan: pan,
		amp: polarity,
		interp: 2
	);

	//grains = grains * EnvGen.kr(Env.adsr(\atk.kr(0.04), 0.2, 0.6, \dec.kr(0.2)), gate, doneAction: Done.freeSelf);
	grains = Balance2.ar(grains[0], grains[1], \pan_master.kr(0)) * \gain.kr(0).dbamp;
	grains = grains * \amp.kr(1);
	grains = grains.sanitize;
	Out.ar(\out.kr(0), grains);
}).add;
)

Synth(\grainSlicer_mono_new, [\buf, a.at(\file), slice: ~getSlice.(0, a)]);
Synth(\grainSlicer_mono, [\buf, ~src, slice: 0]);


(
Pdef(\grainPattern, Pmono(\grainSlicer_mono) <> Pbind(
	\amp, 1,
	\dur, Pwhite(0.25, 1, inf) * 2,
	\dec, Pkey(\dur) * 1,
	\buf, a.at(\file),
	\slice, ~pGetSlice.(i, a, Pseries(0, 1, inf).wrap(0, 32).stutter(1)),
	\overlap, 100,
	)
).play(t);
)

(
Pdef(\grainPattern, Pmono(\grainSlicer_mono) <> Pbind(
	\amp, 1,
	\dur, Pwhite(0.25, 1, inf) * 2,
	\dec, Pkey(\dur) * 1,
	\bufnum, ~src.bufnum,
	\slice, (Pseries(0, 1, inf).wrap(0, 32).stutter(1)).trace,
	\overlap, 100
	)
).play(t);
)