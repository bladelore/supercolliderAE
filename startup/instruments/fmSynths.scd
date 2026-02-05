(
SynthDef(\fmPerc, {
	var sig, freq, car1, car2, mod1, mod2, mod3;
	var iscale, iEnv1, iEnv2, icurve, index1, index2;
	var m1Ratio, m2Ratio, m3Ratio, env;
	var fb, atk, rel, detune, mix;
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

/*	mod1 = SinOscFB.ar(freq * m1Ratio, \mod1fb.kr(0)) * freq * m1Ratio * iEnv1;
	mod2 = SinOscFB.ar(freq * m2Ratio, \mod2fb.kr(0)) * freq * m2Ratio * iEnv1;
	mod3 = SinOscFB.ar([freq, freq * detune] * (m3Ratio + mod2 + mod1 + fb), \mod3fb.kr(0)) * freq * m3Ratio * iEnv2;
	car1 = SinOscFB.ar([freq, freq * detune] * \carRatio.kr(0.5) + mod3, \carfb.kr(0)) * env;*/

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
	LocalOut.ar(LeakDC.ar(mod1+mod2+mod3+car1, coef: 0.995));

	sig = LeakDC.ar(sig, coef: 0.995);
	sig = Balance2.ar(sig[0],sig[1], \pan.kr(0));
	sig = sig * \gain.kr(0).dbamp;
	sig = sig * \amp.kr(1);
	sig = sig.sanitize;
	Out.ar(\out.kr(0), sig);
}).add;

SynthDef(\fmPerc_mono, {
	var sig, freq, car1, car2, mod1, mod2, mod3;
	var iscale, iEnv1, iEnv2, icurve, index1, index2;
	var m1Ratio, m2Ratio, m3Ratio;
	var fb, atk, rel, detune, mix, lag;
	//ctrl
	lag = \lagP.kr(1);
	freq = \freq.kr(60);
	index1 = \index1.kr(1);
	index2 = \index2.kr(2);
	iscale = \iscale.kr(0.04);
	icurve = \icurve.kr(-8);
	atk = \atk.kr(0);
	rel = \rel.kr(1);
	detune = 2**(\spread.kr(20) / 1200);
	mix = \mix.kr(0.5);
	m1Ratio = \m1Ratio.kr(2, lag);
	//m2Ratio = pow(2, m1Ratio);
	//m2Ratio = pow(m1Ratio, 2);
	m2Ratio = \m2Ratio.kr(10, lag);
	m3Ratio = \m3Ratio.kr(4, lag);
	//ext mod idx
	iEnv1 = index1 * iscale;
	iEnv2 = index2 * iscale;
	//modulator/carrier
	fb = LocalIn.ar(2) * \feedback.kr(1);
	fb = Rotate2.ar(fb[0], fb[1], LFNoise2.ar(0.25) * \fbmod.kr(0));

/*	mod1 = SinOscFB.ar(freq * m1Ratio, \mod1fb.kr(0)) * freq * m1Ratio * iEnv1;
	mod2 = SinOscFB.ar(freq * m2Ratio, \mod2fb.kr(0)) * freq * m2Ratio * iEnv1;
	mod3 = SinOscFB.ar([freq, freq * detune] * (m3Ratio + mod2 + mod1 + fb), \mod3b.kr(0)) * freq * m3Ratio * iEnv2;
	car1 = SinOscFB.ar([freq, freq * detune] * \carRatio.kr(1) + mod3, \carfb.kr(0));*/

	mod1 = SinOsc.ar(freq * m1Ratio) * freq * m1Ratio * iEnv1;
	mod2 = SinOsc.ar(freq * m2Ratio) * freq * m2Ratio * iEnv1;
	mod3 = SinOsc.ar([freq, freq * detune] * (m3Ratio + mod2 + mod1 + fb)) * freq * m3Ratio * iEnv2;
	car1 = SinOsc.ar([freq, freq * detune] * \carRatio.kr(1, lag) + mod3);

	car2 = mod3 * (index1 * iscale * m1Ratio * index2 * m2Ratio * m3Ratio * freq).reciprocal;
	//mix between
	sig = Splay.ar([SelectX.ar(mix ,[car1[0], car2[0]]), SelectX.ar(mix,[car1[1], car2[1]])]);
	//drive
	sig = (sig * \drive.kr(0).dbamp).tanh;
	//fb
	LocalOut.ar(LeakDC.ar(mod1+mod2+mod3+car1, coef: 0.995));

	sig = Balance2.ar(sig[0],sig[1], \pan.kr(0));
	sig = sig * \gain.kr(0).dbamp;
	sig = LeakDC.ar(sig, coef: 0.995);
	sig = sig * \amp.kr(1);
	sig = sig.sanitize;
	Out.ar(\out.kr(0), sig);
}).add;

SynthDef(\fmPerc_mono2, {
	var sig, freq, car1, car2, mod1, mod2, mod3;
	var iscale, iEnv1, iEnv2, icurve, index1, index2;
	var m1Ratio, m2Ratio, m3Ratio;
	var fb, atk, rel, detune, mix;
	//ctrl
	freq = \freq.kr(60);
	index1 = \index1.kr(1);
	index2 = \index2.kr(2);
	iscale = \iscale.kr(0.04);
	icurve = \icurve.kr(-8);
	atk = \atk.kr(0);
	rel = \rel.kr(1);
	detune = 2**(\spread.kr(20) / 1200);
	mix = \mix.kr(0.5);
	m1Ratio = \m1Ratio.kr(2);
	//m2Ratio = pow(2, m1Ratio);
	//m2Ratio = pow(m1Ratio, 2);
	m2Ratio = \m2Ratio.kr(10);
	m3Ratio = \m3Ratio.kr(4);
	//ext mod idx
	iEnv1 = index1 * iscale;
	iEnv2 = index2 * iscale;
	//modulator/carrier
	fb = LocalIn.ar(2) * \feedback.kr(0);
	fb = Rotate2.ar(fb[0], fb[1], LFNoise2.ar(0.25) * \fbmod.kr(0));
	mod1 = SinOscFB.ar(freq * m1Ratio, \mod1fb.kr(0)) * freq * m1Ratio * iEnv1;
	mod2 = SinOscFB.ar(freq * m2Ratio, \mod2fb.kr(0)) * freq * m2Ratio * iEnv1;
	mod3 = SinOscFB.ar([freq, freq * detune] * m3Ratio, \mod3b.kr(0)) * freq * m3Ratio * iEnv2;
	car1 = SinOscFB.ar([freq, freq * detune] * (\carRatio.kr(1) + mod1 + mod2 + mod3 + fb), \carfb.kr(0));
	car2 = mod3 * (index1 * iscale * m1Ratio * index2 * m2Ratio * m3Ratio * freq).reciprocal;
	//mix between
	sig = Splay.ar([SelectX.ar(mix ,[car1[0], car2[0]]), SelectX.ar(mix,[car1[1], car2[1]])]);
	//drive
	sig = (sig * \drive.kr(0).dbamp).tanh;
	//fb
	LocalOut.ar(LeakDC.ar(mod1+mod2+mod3+car1, coef: 0.995));
	sig = LeakDC.ar(sig, coef: 0.995);
	sig = Balance2.ar(sig[0],sig[1], \pan.kr(0));
	sig = sig * \gain.kr(0).dbamp;
	sig = sig * \amp.kr(1);
	Out.ar(\out.kr(0), sig);
}).add;

SynthDef(\fmKick, {|gate=1|
	var sig, freq, pitchEnv, atk, dec, sweep, fb, ratio, drive, index;

	freq = \freq.kr(60);
	atk = \atk.kr(0.04);
	dec = \dec.kr(0.4);
	fb = \fb.kr(1);
	index = \index.kr(1);
	ratio = \ratio.kr(2);
	drive = \drive.kr(0);
	sweep = \sweep.kr(8);

	pitchEnv = (1 + (sweep * Env.perc(0.0, 0.13, curve: -4).ar)) * XLine.ar(1, 0.6 , sweep.reciprocal);

	sig = SinOsc.ar(freq * pitchEnv) * index;
	sig = SinOscFB.ar(freq * ratio * sig, fb) * EnvGen.kr(Env.perc(atk, dec, -4));
	sig = sig * EnvGen.kr(Env.perc(atk, dec, 1, 1), gate);

	sig = (sig * drive.neg.dbamp).distort * drive.dbamp;
	sig = SelectX.ar(0.5, [sig, (Ringz.ar(sig, freq, 0.4) * -30.dbamp)]);

	sig = Compander.ar(sig, sig,
		thresh: 0.5,
		slopeAbove: 0.5,
		clampTime: 0.01,
		relaxTime: 1,
	);

	sig = sig * \gain.kr(-20).dbamp;
	sig = Pan2.ar(sig, \pan.kr(0));
	sig = LeakDC.ar(in: sig, coef: 0.995);
	sig = sig * \amp.kr(1);
	DetectSilence.ar(sig, doneAction: 2);
	Out.ar(\out.kr(0), sig);
}).add;
)