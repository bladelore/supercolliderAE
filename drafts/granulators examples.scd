// =====================================================================
// SuperCollider Workspace
// =====================================================================
s.boot;
b = Buffer.readChannel(s, "/Users/asherelazary/Desktop/Samples etc./Field recs/Rock song.wav", channels:[0]);

b.play;
b.plot;
b.sampleRate;
b.numFrames;

s.boot;

Ndef(\granulator).clear;
Ndef(\granulator).ar(2);
Ndef(\granulator).set(\bufnum, b.bufnum);

(
Ndef(\granulator, { |bufnum, tFreq=20, overlap=2, rate=1, posRate=1|
	var phasor, gran, env;
	var bufFrames = BufFrames.ir(bufnum);
	var t = Impulse.ar(tFreq);
	var polarity;
	phasor = Phasor.ar(
		rate: posRate * BufRateScale.kr(bufnum),
		start: 0.0,
		end: bufFrames,
	);

	phasor = (phasor / bufFrames);

	polarity = Demand.ar(t, 0, Drand([-1, 1] * 0.999, inf));

	gran = GrainBuf.ar(
		numChannels: 1,
		trigger: t,
		dur: tFreq.reciprocal * overlap,
		sndbuf: bufnum,
		rate: rate,
		pos: phasor,
		// pos: Integrator.ar( K2A.ar(posRate) ) / BufFrames.ir(bufnum), // withoout phasor
		interp: 2,
		pan: 0;,
		envbufnum: -1,  // Hann window
		maxGrains: 512,
		mul: polarity
	);

	gran

}).play;

Ndef(\granulator).addSpec(
	\overlap, [0.001, 40, \exp],
	\rate, [-1, 1, s.sampleRate.reciprocal],
	\posRate, [0, 1, s.sampleRate.reciprocal],
	// \rate, [0.9, 1, s.sampleRate.reciprocal],
	// \posRate, [0.9, 1, s.sampleRate.reciprocal],
	\tFreq, \widefreq,
	// \delay, [0.0, 30 / s.sampleRate, 1/ s.sampleRate]
	// \delay, [0.0, 0.1, 1/ s.sampleRate]
).edit;
)

(
Ndef(\granulator1, { |bufnum, overlap=0.1, tFreq=20, posRate=0.5, rate=1|
	var bufrdPhasor, granPhasor;
	var bufrd, tgrains, grainbuf;
	var env;
	var bufFrames = BufFrames.ir(bufnum);
	var t = Impulse.ar(tFreq);
	var gDur = overlap / tFreq;
	var polarity;

	granPhasor = Phasor.ar(
		rate: (1 / bufFrames * BufRateScale.kr(bufnum)) / posRate,
		start: 0.0,
		end: 1,
	);

	polarity = Demand.ar(t, 0, Drand([-1,1], inf));

	tgrains = TGrains.ar(
		numChannels: 2,
		trigger: t,
		bufnum: bufnum,
		rate: rate,
		centerPos: (granPhasor * BufDur.kr(bufnum)) + (overlap * 0.50001),
		dur: gDur,
		pan: 0,
		amp: polarity,
		interp: 1
	);

	tgrains;

}).play;

Ndef(\granulator1).addSpec(
	\overlap, [0.001, 40, \exp],
	\rate, [-1, 1, s.sampleRate.reciprocal],
	\posRate, [0, 1, s.sampleRate.reciprocal],
	// \rate, [0.9, 1, s.sampleRate.reciprocal],
	// \posRate, [0.9, 1, s.sampleRate.reciprocal],
	\tFreq, \widefreq,
	// \delay, [0.0, 30 / s.sampleRate, 1/ s.sampleRate]
	// \delay, [0.0, 0.1, 1/ s.sampleRate]
).edit;
)