(
SynthDef(\hh, {|gate=1 out=0|
	var sig;
	sig = WhiteNoise.ar([0.8, 0.7]);
	sig = BPF.ar(sig, (\freq.kr(1000) + ExpRand(4000, 8000)), 0.8);
	sig = CombL.ar(sig, 0.01, LFNoise1.kr(1).range(0.01, 0.1), 0.01);
	sig = sig * Env.perc(\atk.kr(0.001), \dec.kr(0.1), curve: -4).ar(Done.freeSelf);
	sig = sig * \gain.kr(0).dbamp;
	sig = LeakDC.ar(in: sig, coef: 0.995);
	sig = Pan2.ar(sig, \pan.kr(0));
	sig = sig * \amp.kr(1);
	OffsetOut.ar(out, Splay.ar(sig));
}).add;

SynthDef(\kick2, {|gate=1|
	var sig;
	sig = SinOsc.ar(\freq.kr(55) * (1 + (\sweep.kr(2) * Env.perc(0.0, 0.13, curve: -4).ar)) * XLine.ar(1, 0.6 ,1));
	sig = sig + (SinOsc.ar(XLine.ar(4000, 50, 0.01)) * Env.perc(0.0001, 0.01).ar * -5.dbamp);
	sig = sig + (BPF.ar(WhiteNoise.ar, 10120, 0.3) * Env.perc(0.001, 0.03).ar * -8.dbamp);
	sig = sig + (BPF.ar(Hasher.ar(Sweep.ar), 10120, 0.3) * Env.perc(0.001, 0.03).ar * -8.dbamp);
	sig = sig * (1 + (4 * Env.perc(0.01, 0.2).ar));
	sig = sig + (GVerb.ar(sig, \roomsize.kr(8), \reverbtime.kr(1), spread: 16) * -30.dbamp);
	sig = sig * EnvGen.kr(Env.perc(\atk.kr(0.04), \dec.kr(0.2), 0.6, 0.1), gate, doneAction: Done.freeSelf) * \gain.kr(-15).dbamp;
	sig = ((sig * 5.dbamp)).tanh;

	sig = sig * \gain.kr(-15).dbamp;

	sig = Compander.ar(sig, sig,
		thresh: 0.5,
		slopeAbove: 0.5,
		clampTime: 0.01,
		relaxTime: 1,
	);

	sig = Pan2.ar(sig, \pan.kr(0));
	sig = LeakDC.ar(in: sig, coef: 0.995);
	sig = sig * \amp.kr(1);
	Out.ar(\out.kr(0), sig);
}).add;

SynthDef(\clap, {
	| amp = 0.1, pan = 0, dur = 1|
	var env1, env2, sig, noise1, noise2;

	// noise 1 - 4 short repeats
	env1 = EnvGen.ar(
		Env.new(
			[0, 1, 0, 0.9, 0, 0.7, 0, 0.5, 0],
			[0.001, 0.009, 0, 0.008, 0, 0.01, 0, 0.03],
			[0, -3, 0, -3, 0, -3, 0, -4]
		)
	);

	noise1 = WhiteNoise.ar(env1);
	noise1 = HPF.ar(noise1, 600);
	noise1 = LPF.ar(noise1, XLine.kr(7200, 4000, 0.03));
	noise1 = BPF.ar(noise1, 1620, 3);

	// noise 2 - 1 longer single
	env2 = EnvGen.ar(Env.new([0, 1, 0], [0.02, 0.18], [0, -4]), doneAction:2);

	noise2 = PinkNoise.ar(env2);
	noise2 = HPF.ar(noise2, 1000);
	noise2 = LPF.ar(noise2, 7600);
	noise2 = BPF.ar(noise2, 1230, 0.7, 0.7);

	sig = noise1 + noise2;
	sig = sig * 2;
	sig = RLPF.ar(sig, \hpf.kr(8000));
	sig = sig.softclip;

	Out.ar(\out.kr(0), Pan2.ar(sig,pan,amp));
	//By Nathan Ho aka Snappizz
	//http://sccode.org/1-523
}).add;

SynthDef(\simpleSub, {|gate=1|
	var sig, freq, pitchEnv, atk, dec, sweep, fb, ratio, drive, index;

	freq = \freq.kr(40);
	atk = \atk.kr(0.05);
	dec = \dec.kr(2);

	drive = \drive.kr(-10);
	sweep = \sweep.kr(1.5);

	pitchEnv = (1 + (sweep * Env.perc(0.0, 0.13, curve: -4).ar)) * XLine.ar(1, 0.6, dec);

	sig = SinOsc.ar(freq * pitchEnv);
	// sig = sig * EnvGen.kr(Env.perc(atk, dec, 1, 1), gate);
	sig = sig * EnvGen.kr(Env.perc(atk, dec, 1, 1), gate, doneAction: 2);
	// sig = sig * EnvGen.kr(Env.adsr(atk, 1, 1, 4), gate, doneAction: 2);


	sig = (sig * drive.neg.dbamp).tanh * drive.abs.dbamp;

	sig = Compander.ar(sig, sig,
		thresh: 0.5,
		slopeAbove: 0.5,
		clampTime: 0.01,
		relaxTime: 1,
	);

	sig = sig * \gain.kr(-6).dbamp;
	sig = Pan2.ar(sig, \pan.kr(0));
	sig = LeakDC.ar(in: sig, coef: 0.995);
	sig = sig * \amp.kr(1);
	// DetectSilence.ar(sig, doneAction: 2);
	Out.ar(\out.kr(0), sig);
}).add;

SynthDef(\AnalogBassDrum, {
	var trig = Trig.kr(1 ! 2, dur: 0.001);
	var dec = \decay.kr(10);
	var sig = AnalogBassDrum.ar(
		trig: trig,
		infsustain: 0,
		accent: \accent.kr(0.25),
		freq: \freq.kr(50),
		tone: \tone.kr(0.5),
		decay: dec,
		attackfm: \attackfm.kr(0.3),
		selffm: \selffm.kr(0.11)
	);

	sig = sig * EnvGen.kr(Env.perc(\atk.kr(0.01), dec), gate: \gate.kr(1), doneAction:2);
	sig = Pan2.ar(sig, \pan.kr(0));
	sig = sig * \gain.kr(0).dbamp;
	sig = sig * \amp.kr(1);
	Out.ar(\out.kr(0), sig);
}).add;
)