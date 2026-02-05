(
x = {
    var inSig = SinOsc.ar(LFDNoise3.ar(1.5 ! 2).exprange(50, 100));
    var lfo = LFDNoise3.ar(5).linexp(-1, 1, 0.5, 100);
    var sig;
    sig = LPF.ar(
        Fb1({ |in, out|
            // in[1][0] represents previous 2 channel samples from inSig
            // in[0][1] represents current sample from lfo

            // reverse causes cross-feedback of 2 main channels
            // main feedback crosses with helper feedback
            [
                // for main feedback use helper feedback
                (out[1][1] / max(0.001, (in[1][0] - out[1][0].reverse).abs)).tanh,
                // for helper feedback use first channel of main feedback
                (out[1][0][0] + 0.1 / max(0.01, ((in[0][1].abs)))).tanh
            ]
        // outSize [2, 0] has to be passed
        }, [inSig, lfo], [2, 0], [2, 1], 2
    ), 12000) * 0.2;
    // return main feedback
    sig[0]
}.play
)

(
SynthDef(\fmPerc2, {
	var sig, freq, car1, car2, mod1, mod2, mod3;
	var iscale, iEnv1, iEnv2, icurve, index1, index2;
	var m1Ratio, m2Ratio, m3Ratio, env;
	var fb, atk, rel, detune, mix, feedback;
	var mod3_index, car2_gainScale, carRatio;
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
	feedback = \feedback.kr(0);
	//index of modulation
	iEnv1 = EnvGen.kr(Env.perc(\i1atk.kr(0.01), \i1rel.kr(0.1) * rel, icurve)) * index1 * iscale;
	iEnv2 = EnvGen.kr(Env.perc(\i2atk.kr(0.5), \i2rel.kr(1) * rel, icurve)) * index2 * iscale;
	//amplitude envelope
	env = EnvGen.ar(Env.perc(atk, rel, curve: icurve), gate: \gate.kr(1), doneAction:2);
	//modulator/carrier

	mod1 = SinOsc.ar(freq * m1Ratio) * freq * m1Ratio * iEnv1;
	mod2 = SinOsc.ar(freq * m2Ratio) * freq * m2Ratio * iEnv1;
	mod3_index = K2A.ar(freq * m3Ratio * iEnv2);
	car2_gainScale = (index1 * iscale * m1Ratio * index2 * m2Ratio * m3Ratio * freq).reciprocal * env;
	car2_gainScale = K2A.ar(car2_gainScale);
	// carRatio = \carRatio.kr(0.5);
	// carRatio = K2A(0.5);

	sig = Fb1({|in, out|
		var mod1 = in[0][0];
		var mod2 = in[0][1];
		var env = in[0][2];
		var mod3_index = in[0][3];
		var car2_gainscale = in[0][4];
		var carRatio = in[0][5];

		var fb = out[1][0] * feedback;
		// var fb_R = out[1][1] * feedback;
		// fb = Rotate2.ar(out[0] * feedback, env.linlin(0, 1, -1 , 1) * \fbmod.kr(0.5));

		mod3 = SinOsc.ar([freq, freq * detune] * (m3Ratio + mod2 + mod1 + fb)) * mod3_index;
		car1 = SinOsc.ar([freq, freq * detune] * 0.5 + mod3) * env;
		car2 = mod3 * car2_gainScale;
		[mod1+mod2+mod3+car1, car1, car2]
		},
	[mod1, mod2, env, mod3_index, car2_gainScale, carRatio], [2,2,2]);

	sig = Splay.ar([
		SelectX.ar(mix,[sig[1][0], sig[2][0]]),
		SelectX.ar(mix,[sig[1][1], sig[2][1]])
	]);

	sig = (sig * \drive.kr(0).dbamp).tanh;

	sig = LeakDC.ar(sig, coef: 0.995);
	// sig = Balance2.ar(sig[0],sig[1], \pan.kr(0));
	sig = sig * \gain.kr(0).dbamp;
	Out.ar(\out.kr(0), sig);
}).add;
)

Synth(\fmPerc2)

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
	Out.ar(\out.kr(0), sig);
}).add;
)
