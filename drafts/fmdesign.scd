//Quarks.install("JITLibExtensions");
(
s.options.blockSize = 1;
s.options.memSize = 8192 * 16;
s.reboot;
)

(
Ndef(\fmPerc).clear;
Ndef(\fmPerc).ar(2);
)

(
Ndef(\fmPerc, {
	var sig, freq, car1, car2, mod1, mod2, mod3, iscale, iEnv1, iEnv2, icurve, index1, index2, m1Ratio, m2Ratio, m3Ratio, env, fb, atk, rel;
	freq = \freq.kr(60);
	index1 = \index1.kr(1);
	index2 = \index2.kr(2);
	iscale = \iscale.kr(0.04);
	icurve = \icurve.kr(-8);
	atk = \atk.kr(0);
	rel = \rel.kr(8);

	m1Ratio = \m1Ratio.kr(2);
	//m2Ratio = pow(2, m1Ratio);
	//m2Ratio = pow(m1Ratio, 2);
	m2Ratio = \m2Ratio.kr(10);
	m3Ratio = \m3Ratio.kr(4);

	//index of modulation
	iEnv1 = EnvGen.kr(Env.perc(\i1atk.kr(0.01), \i1rel.kr(0.1) * rel, icurve)) * index1 * iscale;
	iEnv2 = EnvGen.kr(Env.perc(\i2atk.kr(0.5), \i2rel.kr(1) * rel, icurve)) * index2 * iscale;
	//amplitude envelope
	env = EnvGen.kr(Env.perc(atk, rel, curve: icurve), doneAction:2);
	//modulator/carrier
	fb = LocalIn.ar(1) * \feedback.kr(1);
	mod1 = SinOscFB.ar(freq * m1Ratio, \mod1fb.kr(0)) * freq * m1Ratio * iEnv1;
	mod2 = SinOscFB.ar(freq * m2Ratio, \mod2fb.kr(0)) * freq * m2Ratio * iEnv1;
	mod3 = SinOscFB.ar(freq * (m3Ratio + mod2 + mod1 + fb), \mod3b.kr(0)) * freq * m3Ratio * iEnv2;
	car1 = SinOscFB.ar(freq * \cRatio.kr(0.5) + mod3, \carfb.kr(0)) * env;
	car2 = (mod3) * (index1 * iscale * m1Ratio * index2 * m2Ratio * m3Ratio * freq).reciprocal * env;
	sig = SelectX.ar(\mix.kr(0.5),[car1, car2]);
	sig = (sig * \drive.kr(0).dbamp).tanh;
	LocalOut.ar(LeakDC.ar(mod1+mod2+mod3+car1, coef: 0.995));
	sig = LeakDC.ar(sig, coef: 0.995);
	sig = sig * \gain.kr(0).dbamp;
	sig = Pan2.ar(sig, \pan.kr(0));

}).play;
)

(
Ndef(\fmPerc, {
	var sig, freq, car1, car2, mod1, mod2, mod3;
	var iscale, iEnv1, iEnv2, icurve, index1, index2;
	var m1Ratio, m2Ratio, m3Ratio, env;
	var fb, atk, rel, detune, mix;
	//ctrl
	freq = \freq.kr(60);
	index1 = \index1.kr(0.5);
	index2 = \index2.kr(2);
	iscale = \iscale.kr(0.2);
	icurve = \icurve.kr(-12);
	atk = \atk.kr(4);
	rel = \rel.kr(9);
	detune = 2**(\detune.kr(10) / 1200);
	mix = \mix.kr(0.5);
	m1Ratio = \m1Ratio.kr(9);
	//m2Ratio = pow(2, m1Ratio);
	//m2Ratio = pow(m1Ratio, 2);
	m2Ratio = \m2Ratio.kr(1);
	m3Ratio = \m3Ratio.kr(0.5);
	//index
	iEnv1 = EnvGen.kr(Env.perc(\i1atk.kr(0.01), \i1rel.kr(0.1) * rel, icurve)) * index1 * iscale;
	iEnv2 = EnvGen.kr(Env.perc(\i2atk.kr(0.02), \i2rel.kr(1) * rel, icurve)) * index2 * iscale;
	//amplitude envelope
	env = EnvGen.kr(Env.perc(atk, rel, curve: icurve), doneAction:2);
	//modulator/carrier
	fb = LocalIn.ar(2) * \feedback.kr(0.08);
	//fb = Rotate2.ar(fb[0], fb[1], LFNoise2.ar(0.25) * \fbmod.kr(1));
	fb = Rotate2.ar(fb[0], fb[1], env.linlin(0, 1, -1 , 1) * \fbmod.kr(1));
	mod1 = SinOscFB.ar(freq * m1Ratio, \mod1fb.kr(0)) * freq * m1Ratio * iEnv1;
	mod2 = SinOscFB.ar(freq * m2Ratio, \mod2fb.kr(0)) * freq * m2Ratio * iEnv1;
	mod3 = SinOscFB.ar([freq, freq * detune] * (m3Ratio + mod2 + mod1 + fb), \mod3b.kr(0)) * freq * m3Ratio * iEnv2;
	car1 = SinOscFB.ar([freq, freq * detune] * \carRatio.kr(1) + mod3, \carfb.kr(0)) * env;
	car2 = mod3 * (index1 * iscale * m1Ratio * index2 * m2Ratio * m3Ratio * freq).reciprocal * env;
	//mix between
	sig = Splay.ar([SelectX.ar(mix ,[car1[0], car2[0]]), SelectX.ar(mix,[car1[1], car2[1]])]);
	//drive
	sig = (sig * \drive.kr(0).dbamp).tanh;
	//fb
	LocalOut.ar(LeakDC.ar(mod1+mod2+mod3+car1, coef: 0.995));
	sig = LeakDC.ar(sig, coef: 0.995);
	sig = Balance2.ar(sig[0],sig[1], \pan.kr(0));
}).play;
)

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
	rel = \rel.kr(1);
	detune = 2**(\detune.kr(20) / 1200);
	mix = \mix.kr(0.5);
	m1Ratio = \m1Ratio.kr(2);
	//m2Ratio = pow(2, m1Ratio);
	//m2Ratio = pow(m1Ratio, 2);
	m2Ratio = \m2Ratio.kr(10);
	m3Ratio = \m3Ratio.kr(4);
	//index of modulation
	iEnv1 = EnvGen.kr(Env.perc(\i1atk.kr(0.01), \i1rel.kr(0.1) * rel, icurve)) * index1 * iscale;
	iEnv2 = EnvGen.kr(Env.perc(\i2atk.kr(0.5), \i2rel.kr(1) * rel, icurve)) * index2 * iscale;
	//amplitude envelope
	env = EnvGen.kr(Env.perc(atk, rel, curve: icurve), gate: \gate.kr(1), doneAction:2);
	//modulator/carrier
	fb = LocalIn.ar(2) * \feedback.kr(1);
	fb = Rotate2.ar(fb[0], fb[1], LFNoise2.ar(0.25) * \fbmod.kr(0.25));
	//fb = Rotate2.ar(fb[0], fb[1], env.linlin(0, 1, -1 , 1) * \fbmod.kr(0.5));
	mod1 = SinOscFB.ar(freq * m1Ratio, \mod1fb.kr(0)) * freq * m1Ratio * iEnv1;
	mod2 = SinOscFB.ar(freq * m2Ratio, \mod2fb.kr(0)) * freq * m2Ratio * iEnv1;
	mod3 = SinOscFB.ar([freq, freq * detune] * (m3Ratio + mod2 + mod1 + fb), \mod3b.kr(0)) * freq * m3Ratio * iEnv2;
	car1 = SinOscFB.ar([freq, freq * detune] * \carRatio.kr(0.5) + mod3, \carfb.kr(0)) * env;
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
	Out.ar(\out.kr(0), sig);
}).add;

SynthDef(\fmPerc_mono, {
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
	mod3 = SinOscFB.ar([freq, freq * detune] * (m3Ratio + mod2 + mod1 + fb), \mod3b.kr(0)) * freq * m3Ratio * iEnv2;
	car1 = SinOscFB.ar([freq, freq * detune] * \carRatio.kr(1) + mod3, \carfb.kr(0));
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
	Out.ar(\out.kr(0), sig);
}).add;

)