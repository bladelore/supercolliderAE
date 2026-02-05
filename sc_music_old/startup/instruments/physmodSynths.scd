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

SynthDef(\fb2, {|buf|
	var sig, impulse, fb, modEnv1, del2, trigger;
	var time = \time.kr(0.1) - ControlRate.ir.reciprocal;
	var damp = (1 - \damp.kr(0.5));
	var res = (1 - \res.kr(0));

	fb = LocalIn.ar(2) * \feedback.kr(0.5);
	time = time*(1-fb);

	trigger = Impulse.ar(\impulse.kr(5000));
	impulse = ~velvet.(trigger, density: \density.kr(1) * (1-fb), bias: \bias.kr(0));
	impulse = OnePole.ar(impulse, damp);
	sig = DelayC.ar(fb + impulse, 2, time);
	sig = sig + fb;
	sig = RLPF.ar(sig, freq: \filter.kr(440), rq: res);
	del2 = AllpassC.ar(sig, 2, time * 0.5);
	sig = SelectX.ar(\delay2.kr(0.8),[sig, del2]);
	sig = (sig * \dist.kr(36).dbamp).softclip;
	// sig = sig.softclip;
	sig = SelectX.ar(\exciter.kr(0.7),[sig, impulse]);
	sig = OnePole.ar(sig, damp);
	sig = LeakDC.ar(in: sig, coef: 0.995);
	sig = sig.sanitize;
	sig = Convolution2L.ar(sig, buf, Trig.kr(1, dur: \sustainTime.kr(0.01)), 2**round(log2(\window.kr(512))), 1);
	sig = sig + (NHHall.ar(sig, rt60: \rev.kr(0.1)) * -5.dbamp);
	// sig = sig*0.99;

	LocalOut.ar(sig);
	//DetectSilence.ar(sig, doneAction: Done.freeSelf);
	// sig = sig * EnvGen.kr(Env.perc(\atk.kr(0.01), \dec.kr(0.1), curve: -8), \gate.kr(1));
	sig = sig * EnvGen.kr(Env.perc(\atk.kr(0.01), \dec.kr(1), curve: -8), \gate.kr(1), doneAction: Done.freeSelf);
	sig = Balance2.ar(sig[0], sig[1], \pan.kr(0));

	sig = sig * \gain.kr(0).dbamp;
	sig = sig * \amp.kr(1);
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