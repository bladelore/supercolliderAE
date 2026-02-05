//Quarks.install("JITLibExtensions");

s.boot;
b = Buffer.readChannel(s, "/Users/asherelazary/Desktop/Samples etc./Field recs/Rock song.wav", channels:[0]);
b = Buffer.readChannel(s, "/Users/asherelazary/Desktop/Samples etc./Film Sounds/bath-water-running.wav", channels:[0]);
b = Buffer.readChannel(s, "/Users/asherelazary/Desktop/Samples etc./NEW sample lib/Jungle riddims/jungle warrior.wav", channels:[0]);

(
Ndef(\granulator).clear;
Ndef(\granulator).ar(2);
Ndef(\granulator).set(\bufnum, b.bufnum);
)

(
Ndef(\granulator, { |bufnum, tFreq=20|
	var polarity, polarityProb, pan, grains;
	var bufFrames = BufFrames.ir(bufnum);
	var t = Impulse.ar(tFreq);
	var dur = tFreq.reciprocal * \overlap.kr(4);

	var granPhasor = Phasor.ar(
		rate: (1 / bufFrames * BufRateScale.kr(bufnum)) * \posRate.kr(0.5),
		start: 0.0,
		end: 1,
		//trig: \gate.kr(1),
		resetPos: 0.0
	);

	granPhasor = Wrap.ar(granPhasor + \pos.kr(0), 0, 1);

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
		interp: 4
	);

	//grains = grains * EnvGen.kr(Env.perc(\atk.kr(0.01), \dec.kr(0.1), curve: -8), \gate.kr(1), doneAction: Done.freeSelf);
	Balance2.ar(grains[0], grains[1], \pan_master.kr(0));
}).play;

Ndef(\granulator).addSpec(
	\overlap, [0.001, 40, \exp],
	\rate, [-1, 2, s.sampleRate.reciprocal],
	\pos,[0,1],
	\posRate, [0, 1, s.sampleRate.reciprocal],
	\polarityMod, [0, 1],
	\tFreq, \widefreq,
	\pan_master, [-1, 1]
).edit;
)