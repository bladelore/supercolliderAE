(
SynthDef(\granulator, { |bufnum, tFreq=20, gate=1|
	var polarity, polarityProb, pan, grains;
	var bufFrames = BufFrames.ir(bufnum);
	var t = Impulse.ar(tFreq);
	var dur = tFreq.reciprocal * \overlap.kr(1);

	var granPhasor = Phasor.ar(
		rate: (1 / bufFrames * BufRateScale.kr(bufnum)) * \posRate.kr(0.5),
		start: 0.0,
		end: 1,
		trig: gate,
		resetPos: 0.0
	);

	granPhasor = Wrap.ar(granPhasor + \pos.kr(0), 0, 1);

	polarityProb = Drand([-1 + Diwhite(0, \polarityMod.kr(0), inf), 1], inf) * -1;
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
		interp: 4
	);

	grains = grains * EnvGen.kr(Env.perc(\atk.kr(0.01), \dec.kr(0.1), curve: -8), gate, doneAction: Done.freeSelf);
	grains = Balance2.ar(grains[0], grains[1], \pan_master.kr(0));
	Out.ar(\out.kr(0), grains);

}).add;
)

~grainbuf = Buffer.readChannel(s, "/Users/asherelazary/Desktop/Samples etc./Field recs/Rock song.wav", channels:[0]);



