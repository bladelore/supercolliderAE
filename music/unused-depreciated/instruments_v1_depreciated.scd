(
//Server.default.options.device = "headphonesSc";
Server.scsynth;
s.options.blockSize = 4;
s.options.hardwareBufferSize = 2048;
// s.options.hardwareBufferSize = nil;
s.options.memSize = 8192 * 16;
s.reboot;
)

{SinOsc.ar() * 0.1}.play

s.options;

s.quit;

(
SynthDef(\fb1, {
	var sig, impulse, fb, modEnv1, del2;
	var time = \time.kr(0.1) - ControlRate.ir.reciprocal;
	var damp = (1 - \damp.kr(0.5));
	var res = (1 - \res.kr(0));

	fb = LocalIn.ar(2) * \feedback.kr(0.5);
	time = time*(1-fb);
	impulse = Dust.ar(\impulse.kr(10000) * (1-fb));
	impulse = HairCell.ar(impulse, spontaneousrate: \spont.kr(1), boostrate: \boost.kr(2000) * fb, restorerate: (1-fb) * \restore.kr(100), loss: 1-fb);
	impulse = OnePole.ar(impulse, damp);
	sig = DelayC.ar(fb + impulse, 2, time);
	sig = sig + fb;
	sig = RLPF.ar(sig, freq: \filter.kr(440), rq: res);
	del2 = AllpassC.ar(sig, 2, time * 0.5);
	sig = SelectX.ar(\delay2.kr(0.8),[sig, del2]);
	sig = (sig * \dist.kr(36).dbamp).tanh;
	sig = SelectX.ar(\exciter.kr(0.7),[sig, impulse]);
	sig = OnePole.ar(sig, damp);
	sig = LeakDC.ar(in: sig, coef: 0.995);
	//sig = sig ! 2;
	sig = sig + (NHHall.ar(sig, rt60: \rev.kr(0.1)) * -5.dbamp);
	sig = sig.sanitize;
//	sig = BHPF.ar(sig, freq: 10);
	LocalOut.ar(sig);
	//DetectSilence.ar(sig, doneAction: Done.freeSelf);
	//sig = sig * EnvGen.kr(Env.perc(\atk.kr(0.01), \dec.kr(0.1), curve: -8), \gate.kr(1));
	sig = sig * EnvGen.kr(Env.perc(\atk.kr(0.01), \dec.kr(0.1), curve: -8), \gate.kr(1), doneAction: Done.freeSelf);
	sig = Balance2.ar(sig[0], sig[1], \pan.kr(0));
	sig = sig * \gain.kr(0).dbamp;
	sig = sig * \amp.kr(1);
	Out.ar(\out.kr(0), sig);
}).add;

SynthDef(\fb1_mono, {
	var sig, impulse, fb, modEnv1, del2;
	var time = \time.kr(0.1) - ControlRate.ir.reciprocal;
	var damp = (1 - \damp.kr(0.5));
	var res = (1 - \res.kr(0));

	fb = LocalIn.ar(2) * \feedback.kr(0.5);
	time = time*(1-fb);
	impulse = Dust.ar(\impulse.kr(10000) * (1-fb));
	impulse = HairCell.ar(impulse, spontaneousrate: \spont.kr(1), boostrate: \boost.kr(2000) * fb, restorerate: (1-fb) * \restore.kr(100), loss: 1-fb);
	impulse = OnePole.ar(impulse, damp);
	sig = DelayC.ar(fb + impulse, 2, time);
	sig = sig + fb;
	sig = RLPF.ar(sig, freq: \filter.kr(440), rq: res);
	del2 = AllpassC.ar(sig, 2, time * 0.5);
	sig = SelectX.ar(\delay2.kr(0.8),[sig, del2]);
	sig = (sig * \dist.kr(36).dbamp).tanh;
	sig = SelectX.ar(\exciter.kr(0.7),[sig, impulse]);
	sig = OnePole.ar(sig, damp);
	sig = LeakDC.ar(in: sig, coef: 0.995);
	//sig = sig ! 2;
	sig = sig + (NHHall.ar(sig, rt60: \rev.kr(0.1)) * -5.dbamp);
	sig = sig.sanitize;
//	sig = BHPF.ar(sig, freq: 10);
	LocalOut.ar(sig);
	sig = Balance2.ar(sig[0], sig[1], \pan.kr(0));
	sig = sig * \gain.kr(0).dbamp;
	sig = sig * \amp.kr(1);
	Out.ar(\out.kr(0), sig);
}).add;

SynthDef(\hh, {|gate=1 out=0|
	var sig;
	sig = WhiteNoise.ar([0.8, 0.7]);
	sig = BPF.ar(sig, (\freq.kr(1000) + ExpRand(4000, 8000)), 0.8);
	sig = CombL.ar(sig, 0.01, LFNoise1.kr(1).range(0.01, 0.1), 0.01);
	sig = sig * Env.perc(\atk.kr(0.001), \dec.kr(0.1), curve: -4).ar(Done.freeSelf);
//	sig = sig * EnvGen.kr(Env.perc(\atk.kr(0.001), \dec.kr(0.5), curve: -4), gate , doneAction: Done.freeSelf);
	//sig = sig * EnvGen.kr(Env.adsr(\atk.kr(0.04), \dec.kr(0.2), 0.6, 0.1), t_gate, doneAction: Done.freeSelf);
	sig = sig * \gain.kr(0).dbamp;
	sig = LeakDC.ar(in: sig, coef: 0.995);
	sig = Pan2.ar(sig, \pan.kr(0));
	// Out.ar(\out.kr(0), sig);
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

SynthDef(\segPlayer, {|bufnum|
	var pan = \pan.kr(0);
	//slice index
	//var this_idx = \slice.kr(0);
	var this_idx = Select.kr(\slice.kr(0), ~featuresSorted);
	var startsamp = Select.kr(this_idx, ~onsetArr);
	var endsamp = Select.kr(this_idx + 1, ~onsetArr);
	//channels and sampler
	var numChans = bufnum.numChannels;
	var sig = PlayBuf.ar(numChans, bufnum, BufRateScale.ir(bufnum) * \rate.kr(1), 0, startsamp, loop: 0);
	//handle channels
	var panned = case
	{numChans == 1} {Pan2.ar(sig, pan)}
	{numChans == 2} {Balance2.ar(sig[0], sig[1], pan)}
	{numChans > 2} { var splay = Splay.ar(sig); Balance2.ar(splay[0], splay[1], pan); } ;
	//envelope
	var env = EnvGen.kr(Env.perc(\atk.kr(0.01), \rel.kr(0.5), curve: \curve.kr(-4)), gate: \gate.kr(1), doneAction:2);
	//out
	sig = panned * env * \gain.kr(0).dbamp;

	sig = SelectX.ar(\pitchMix.kr(0), [sig,
		PitchShift.ar(sig,
			pitchRatio: \pitchRatio.kr(1),
			windowSize: \windowSize.kr(0.01),
			pitchDispersion: \pitchDispersion.kr(1),
			timeDispersion: \timeDispersion.kr(0.1))]
	);
	sig = sig * \amp.kr(1);
	Out.ar(\out.kr(0), sig);
}).add;

SynthDef(\grainSlicer_mono, { |bufnum|
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

	//grains = grains * EnvGen.kr(Env.adsr(\atk.kr(0.04), 0.2, 0.6, \dec.kr(0.2)), gate, doneAction: Done.freeSelf);
	grains = Balance2.ar(grains[0], grains[1], \pan_master.kr(0)) * \gain.kr(0).dbamp;
	grains = grains * \amp.kr(1);
	grains = grains.sanitize;
	Out.ar(\out.kr(0), grains);
}).add;

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
/*	ptr = Phasor.ar(
		rate: (1 / bufFrames * BufRateScale.kr(bufnum)) * \recRate.kr(1),
		start: startsamp,
		end: endsamp,
		//trig: gate,
		resetPos: startsamp
	);*/

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
	grains = grains * \amp.kr(1);
	Out.ar(\out.kr(0), grains);
}).add;

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

SynthDef(\padKlank, {
	var f = 60;
	var n = 16;
	var harmonicRatio = 2;
	var bwScale = 1;
	var bw = 0.1;
	var detune = \detune.kr(5);

	var freqs = Array.newClear(n);
	var amps = Array.newClear(n);
	var ringtimes = Array.newClear(n);

	var powN, relF, hf, bw_ring;
	var in, impulse, env, modal;

	n.do{|i|
		//get harmonic integers
		powN = pow(i + 1, harmonicRatio);
		relF = (powN * (1.0 + (powN - 1)));
		//harmonic frequency
		hf = (relF * f);
		freqs[i] = hf;
		// relF = abs(relF);
		//scale ring time to frequency
		bw_ring = (pow(2, (bw / 1200)) - clip(bwScale, -1 , 1)) * pow(relF, bwScale);
		ringtimes[i] = bw_ring;
	};

	in = InFeedback.ar(\inbus.kr(0), 2);
	env = Amplitude.ar(in, \atk.kr(0.01), \rel.kr(0.1));

	impulse = GaussTrig.ar(freq: \impulse.kr(20000) ! 2, dev: \dev.kr(1)) ;
	impulse = impulse * PinkNoise.ar([1,1]) * \inGain.kr(0).dbamp;

	modal = DynKlank.ar(
		`[freqs + LFNoise2.ar(10 ! n).unipolar(detune),
		nil, ringtimes * env],
		impulse,
		\stretch.kr(2, 0.01),
		\freqoffset.kr(10) * env,
		\decayscale.kr(1)
	)* n.sqrt.reciprocal * 0.5;

	// modal = BHiShelf.ar(modal, 3000, db:-6);
	modal = BHiPass.ar(modal, 100);
	modal = BLowPass4.ar(modal, \lpf.kr(20000));
	modal = DCompressor.ar(modal, ratio: 4, threshold: -60, attack: 0.1, release: 1, makeup: 0, automakeup: 1);
	modal = modal.softclip;
	modal = modal.sanitize;

	modal = SelectX.ar(\drywet.kr(1),[in ,modal]);
	modal = modal * \gain.kr(-16).dbamp;
	modal = modal * \amp.kr(1);

Out.ar(\out.kr(0), modal);
}).add;

SynthDef(\driftingSines_mono, {
	var sig;
	var lfo = LFTri.ar(\lfoFreq.kr(0.01), 1).linlin(-1, 1, 0, 1);
	var freq = \freq.kr(404);
	var freqs = ([freq, freq/2, freq/5, freq/7] + LFNoise2.kr(0.1 ! 4, \pitchDev.kr(15)));
	var amps = ([0.4, 0.3, 0.2, 0.5] + LFNoise2.kr(0.1 ! 4, 0.1));
	var q = 0.8;

	sig = Blip.ar(freqs, \numHarm.kr(0)) * amps;
	sig = BLowPass4.ar(sig, (5000 * lfo), q);
	sig = Splay.ar(sig);
	sig = LeakDC.ar(sig).sanitize;

	sig = sig * 0.25;
	sig = sig * \gain.kr(0).dbamp;
	sig = sig * \amp.kr(1);
	Out.ar(\out.kr(0), sig);
}).add;

SynthDef(\rongsinator, {|pan=0, sustainTime=0.01, f0=100.05, structure=0.5, brightness=0.5, damping=0.75, accent=0.9, harmonicstretch=0.5, position=0.15, loss=0.15|

	var trig = Trig.kr(1, dur: sustainTime);

	var modeNum=1, cosFreq=0.025;
	var sig = Rongs.ar(
		trigger:trig,
		sustain:trig,

		f0:f0,
		structure:structure,
		brightness:brightness,
		damping:damping,
		accent:accent,
		stretch:harmonicstretch,
		position:position,
		loss:loss,
		modeNum:modeNum,
		cosFreq:cosFreq
	);

	sig = sig * EnvGen.kr(Env.perc(\atk.kr(0.1), \rel.kr(1)), gate: \gate.kr(1), doneAction:2);

	// DetectSilence.ar(in: sig, amp: 0.0001, time: 0.1, doneAction: 2);
	sig = Pan2.ar(sig, pan);
	sig = sig * \gain.kr(0).dbamp;
	sig = sig * \amp.kr(1);
	Out.ar(\out.kr(0), sig);
}).add;
)