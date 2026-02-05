(
SynthDef(\liveGrain_mono, { |bufnum|
	var polarity, polarityProb, pan, grains, fb;
	var ptr, prev, current;
	//bufFrames
	var bufFrames = BufFrames.ir(bufnum);
	//in
	var feedback = (LocalIn.ar(1) * \feedback.ar(0) * 0.9).softclip;
	// var in = SoundIn.ar(0);
	var in = InFeedback.ar(\inbus.kr(0), 1);
	//trigger and duration
	var trigRate = \trigRate.kr(100);
	var t = Impulse.ar(trigRate);
	var dur = trigRate.reciprocal * \overlap.kr(1) * \rate.kr(1).reciprocal;
	//sample bounds
	var startsamp = \startsamp.kr(0) * bufFrames;
	var endsamp = \endsamp.kr(1) * bufFrames;
	var sampsDur = endsamp - startsamp;
	//readback phasor
	var granPhasor = Phasor.ar(
		rate: (1 / bufFrames * BufRateScale.kr(bufnum)) * \posRate.kr(1),
		start: startsamp,
		end: endsamp,
		//trig: gate,
		resetPos: startsamp
	);
	//overdub input
	ptr = Phasor.ar(0, \recRate.kr(1), 0, bufFrames);
	prev = BufRd.ar(1, bufnum, ptr);
	current = XFade2.ar(in + feedback, prev, \overdub.kr(0));
	BufWr.ar(current, bufnum, ptr);

	granPhasor = Wrap.ar(granPhasor + (\pos.kr(0) * sampsDur), startsamp, endsamp);
	//polarity modulation (REDO)
	polarityProb = Drand([-1 + Diwhite(0, \polarityMod.kr(1), inf), 1], inf) * -1;
	polarityProb = (polarityProb * 2 - 1);
	polarity = Demand.ar(t, 0, polarityProb);
	pan = Demand.ar(t, 0, Drand([-1, 0, 1] * \panMod.kr(1), inf));
	//grain output
	grains = TGrains.ar(
		numChannels: 2,
		trigger: t,
		bufnum: bufnum,
		rate: \rate.kr(1),
		centerPos: (granPhasor * BufDur.kr(bufnum)) + (dur * 0.5001),
		dur: dur,
		pan: pan,
		amp: polarity,
		interp: 4
	);

	grains = Balance2.ar(grains[0], grains[1], \pan_master.kr(0));

	//overdub feedback
	fb = grains.sum * 0.5;
	fb = LeakDC.ar(fb, 0.995);
	LocalOut.ar(fb);

	grains = grains * \gain.kr(0).dbamp;

	Out.ar(\out.kr(0), grains);
}).add;
)

b = Buffer.alloc(s, s.sampleRate * 0.1);
b.free

(
Pmono(\liveGrain_mono,
	\inbus, ~grain,
	\bufnum, b,
	\startsamp, 0,
	\endsamp, 1,
	\overlap, 200,
	\trigRate, 500,
	\posRate, 0.25,
	\rate, 1,
	\polarityMod, 1,
	\overdub, 0.5,
	\feedback, 0.25
);
)
