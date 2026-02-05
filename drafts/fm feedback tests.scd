(
//Server.default.options.device = "headphonesSc";
Server.scsynth;
s.options.blockSize = 64;
s.options.hardwareBufferSize = 4096;
// s.options.hardwareBufferSize = nil;
s.options.memSize = 256000‬‬;
s.reboot;
)

{
var x, y, rate, a, mod, b;
	b = LocalBuf(1);
	rate = SampleRate.ir;
	a = Dwhite(-1, 1, inf) * Drand([0.2, 0.2, 1, 10], inf);
	x = Dbufrd(b); // read from buffer
	x = x + a;
	x = x.fold2(1.0);
	y = Dbufwr(x, b); // write to buffer
	Duty.ar(1 / rate, 0, y) * 0.1.dup;
}.scope;
)

(
SynthDef(\fmPerc, {
	var sig, freq, car1, car2, mod1, mod2, mod3;
	var iscale, iEnv1, iEnv2, icurve, index1, index2;
	var m1Ratio, m2Ratio, m3Ratio, env;
	var fb, fbOut, atk, rel, detune, mix;
	//ctrl
	freq = \freq.kr(60);
	index1 = \index1.kr(1);
	index2 = \index2.kr(2);
	iscale = \iscale.kr(0.04);
	icurve = \icurve.kr(-8);
	atk = \atk.kr(0);
	rel = \rel.kr(8);
	detune = 2**(\detune.kr(20) / 1200);
	mix = \mix.kr(0.5);
	m1Ratio = \m1Ratio.kr(2);
	m2Ratio = \m2Ratio.kr(10);
	m3Ratio = \m3Ratio.kr(4);
	//index of modulation
	iEnv1 = EnvGen.kr(Env.perc(\i1atk.kr(0.01), \i1rel.kr(0.1) * rel, icurve)) * index1 * iscale;
	iEnv2 = EnvGen.kr(Env.perc(\i2atk.kr(0.5), \i2rel.kr(1) * rel, icurve)) * index2 * iscale;
	//amplitude envelope
	env = EnvGen.kr(Env.perc(atk, rel, curve: icurve), gate: \gate.kr(1), doneAction:2);
	//modulator/carrier
	fb = LocalIn.ar(2) * \feedback.kr(0);
	fb = Rotate2.ar(fb[0], fb[1], env.linlin(0, 1, -1 , 1) * \fbmod.kr(0.5));
	mod1 = SinOsc.ar(freq * m1Ratio) * freq * m1Ratio * iEnv1;
	mod2 = SinOsc.ar(freq * m2Ratio) * freq * m2Ratio * iEnv1;
	mod3 = SinOsc.ar([freq, freq * detune] * (m3Ratio + mod2 + mod1 + fb)) * freq * m3Ratio * iEnv2;
	car1 = SinOsc.ar([freq, freq * detune] * \carRatio.kr(0.5) + mod3) * env;

	car2 = mod3 * (index1 * iscale * m1Ratio * index2 * m2Ratio * m3Ratio * freq).reciprocal * env;

	//mix between
	sig = Splay.ar([SelectX.ar(mix ,[car1[0], car2[0]]), SelectX.ar(mix,[car1[1], car2[1]])]);
	//drive
	sig = (sig * \drive.kr(0).dbamp).tanh;
	//fb
	fbOut = DelayC.ar(LeakDC.ar(mod1+mod2+mod3+car1), delaytime: (s.options.blockSize) / s.sampleRate);
	// LocalOut.ar(LeakDC.ar(fbOut, coef: 0.995));
	// LocalOut.ar(LeakDC.ar(mod1+mod2+mod3+car1, coef: 0.995));

	sig = LeakDC.ar(sig, coef: 0.995);
	sig = Balance2.ar(sig[0],sig[1], \pan.kr(0));
	sig = sig * \gain.kr(0).dbamp;
	Out.ar(\out.kr(0), sig);
}).add;
)

(s.options.blockSize - 1) / s.sampleRate

Synth(\fmPerc)

(s.options.blockSize - 1) / s.sampleRate
64/44100

(
Pdef(\fmPercPatt3,
	// Pmono(\fmPerc_mono) <>
	Pbind(
		\instrument, \fmPerc,
		// \freq, Pwrand([23, 24].midicps, [0.8, 0.2], inf),
		\freq, 23.midicps,
		// \freq, 30,
		\dur, Pwhite(0.5, 0.125, inf) * 2,
		//\dur, 0.5,
		\dec, Pkey(\dur) * 1,
		\atk, ~pmodenv.(Pwhite(0.01, 0.5, inf), Pkey(\dec)),
		// \rel, ~pmodenv.(Pwhite(0.5, 0.125, inf), Pkey(\dec)),
		\rel, 0.2,
		\feedback, ~pmodenv.(Pwhite(0, 1,inf), Pkey(\dec)),
		\fbmod, ~pmodenv.(Pwhite(0, 1,inf), Pkey(\dec)),
		\carRatio, 1,
		\m1Ratio, 1,
		\m2Ratio, 7,
		\m3Ratio, 9,
		\index1, ~pmodenv.(Pexprand(0.01, 10, inf), Pkey(\dec)),
		\index2, ~pmodenv.(Pexprand(0.01, 10, inf), Pkey(\dec)),
		\iscale, ~pmodenv.(Pexprand(1, 0.1, inf), Pkey(\dec)),
		\spread,~pmodenv.(Pwhite(10, 30, inf), Pkey(\dec)),
		\mix, ~pmodenv.(Pwhite(0, 1), Pkey(\dec)),
		// \mix, 0,
		// \drive, 16,
		\drive, ~pmodenv.(Pwhite(0, 16), Pkey(\dec)),
		\gain, -24,
	).play(t);
);
)