(
SynthDef(\grainSlicer_new, { |buf, gate=1, slice=#[0, 1]|
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

SynthDef(\grainSlicer, { |bufnum, gate=1|
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

	grains = grains * EnvGen.kr(Env.adsr(\atk.kr(0.04), 0.2, 0.6, \dec.kr(0.2)), gate, doneAction: Done.freeSelf);
	grains = Balance2.ar(grains[0], grains[1], \pan_master.kr(0)) * \gain.kr(0).dbamp;
	grains = grains * \amp.kr(1);
	Out.ar(\out.kr(0), grains);
}).add;
)

(
Pdef(\grainMod, Pbind(
	\amp, 1,
	\bufnum, ~src.bufnum,
	\slice, Pkey(\cyclecount).stutter(Pwrand([1, 4, 2], [2, 1, 0.5].normalizeSum, inf)),
	\overlap, 50,
	\rate, 1
	) <> ~makeSubdivision.(
		Pseq([2, 1, 1, 1, 1], 1),
		Pseq(#[5, 5, 4], 1)
	) <> Pbind(\instrument, \grainSlicer)
).play(t);
)

(
Pdef(\grainMod, Pbind(
	\amp, 1,
	\bufnum, a.at(\file),
	\slice, ~pGetSlice.(Pkey(\cyclecount).stutter(Pwrand([1, 4, 2], [2, 1, 0.5].normalizeSum, inf)), a),
	\overlap, 50,
	\rate, 1
	) <> ~makeSubdivision.(
		Pseq([2, 1, 1, 1, 1], 1),
		Pseq(#[5, 5, 4], 1)
	) <> Pbind(\instrument, \grainSlicer)
).play(t);
)
