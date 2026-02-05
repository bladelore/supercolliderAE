//Quarks.install("JITLibExtensions");
s.options.blockSize = 1;
s.options.memSize = 8192 * 16;
s.reboot;

(
Ndef(\fb1).clear;
Ndef(\fb1).ar(2);
)

b = Buffer.alloc(s, 512, 1, { |buf| buf.chebyMsg([1,0,1,1,0,1])});
b.plot;

(
Ndef(\fb1).clear;
Ndef(\fb1).ar(2);
)
(
Ndef(\fb1, {
	var snd, impulse, fb;
	var polarityProb;
	fb = LocalIn.ar(1) * \feedback.kr(0.5);
	fb = OnePole.ar(fb, (1 - \damp.kr(0.5)));
	impulse = Dust.ar(\impulse.kr(10000));

	snd = DelayC.ar(fb, 2, \time.kr(0.1) - ControlRate.ir.reciprocal);
	snd = snd + fb + impulse;
	//snd = RLPFD.ar(snd, ffreq: \filter.kr(440), res: \res.kr(0), dist: 0);
	snd =RLPF.ar(snd, freq: \filter.kr(440), rq: \res.kr(0));
	snd = (snd * \dist.kr(0).dbamp).tanh;
	//snd = AllpassC.ar(snd, 2, \time.kr(0.1) - ControlRate.ir.reciprocal);
	snd = SelectX.ar(\exciter.kr(0),[snd, impulse]);
	snd = OnePole.ar(snd, (1 - \damp.kr(0.5)));
	//
	snd = LeakDC.ar(in: snd, coef: 0.995);
	snd = snd * EnvGen.kr(Env.perc(\atk.kr(0.01), \dec.kr(0.1), curve: -8), \gate.kr(1));
	snd = snd ! 2;
	snd = snd + (NHHall.ar(snd, rt60: \rev.kr(0.1)) * -5.dbamp);
	DetectSilence.ar(snd, doneAction: Done.freeSelf);
	LocalOut.ar(snd.sum);
	snd;
}).play;

Ndef(\fb1).addSpec(
	\impulse,[0, 44100,s.sampleRate.reciprocal],
	\damp,[0, 1],
	\time,[0.01, 2, \exp],
	\feedback,[0,1],
	\filter,[1, 20000, \exp],
	\res,[0,5],
	\dist,[0,64],
	\exciter,[0,1],
	\polarityMod,[0,1]

).edit;
)

(
Ndef(\fb1, {
	var snd, impulse, fb;
	var polarityProb;
	fb = LocalIn.ar(1) * \feedback.kr(0.5);
	fb = OnePole.ar(fb, (1 - \damp.kr(0.5)));
	impulse = GaussTrig.ar(freq: \impulse.kr(440), dev: 100);

	snd = DelayC.ar(fb + impulse, 2, \time.kr(0.1) - ControlRate.ir.reciprocal);
	snd = snd + fb;
	//snd = RLPFD.ar(snd, ffreq: \filter.kr(440), res: \res.kr(0), dist: 0);
	snd =RLPF.ar(snd, freq: \filter.kr(440), rq: \res.kr(0));
	snd = (snd * \dist.kr(0).dbamp).tanh;
	//snd = AllpassC.ar(snd, 2, \time.kr(0.1) - ControlRate.ir.reciprocal);
	snd = SelectX.ar(\exciter.kr(0),[snd, impulse]);
	snd = OnePole.ar(snd, (1 - \damp.kr(0.5)));
	//
	snd = LeakDC.ar(in: snd, coef: 0.995);
	snd = snd * EnvGen.kr(Env.perc(\atk.kr(0.01), \dec.kr(0.1), curve: -8), \gate.kr(1));
	snd = snd ! 2;
	snd = snd + (NHHall.ar(snd, rt60: \rev.kr(0.1)) * -5.dbamp);
	DetectSilence.ar(snd, doneAction: Done.freeSelf);
	LocalOut.ar(snd.sum);
	snd;
}).play;

Ndef(\fb1).addSpec(
	\impulse,[0, 44100,s.sampleRate.reciprocal],
	\damp,[0, 1],
	\time,[0.01, 2, \exp],
	\feedback,[0,1],
	\filter,[1, 20000, \exp],
	\res,[0,5],
	\dist,[0,64],
	\exciter,[0,1],
).edit;
)

(
Ndef(\fb1, {
	var snd, impulse, fb, modEnv1, del2;
	var time = \time.kr(0.1) - ControlRate.ir.reciprocal;
	var damp = (1 - \damp.kr(0.5));

	fb = LocalIn.ar(1) * \feedback.kr(0.5);

	impulse = Dust.ar(\impulse.kr(44100));
	impulse = HairCell.ar(impulse, spontaneousrate: \spont.kr(1), boostrate: \boost.kr(2000), restorerate: (1-fb) * \restore.kr(100), loss: 1 - fb);
	impulse = OnePole.ar(impulse, damp);

	snd = DelayC.ar(fb, 2, time);
	snd = snd + fb + impulse;
	snd = RLPFD.ar(snd, ffreq: \filter.kr(440), res: 0, dist: 0);
	snd = (snd * \dist.kr(36).dbamp).tanh;
	del2 = AllpassC.ar(snd, 2, time);
	del2 = NestedAllpassC.ar(del2, maxdelay1: 0.036, delay1: time, gain1: 0.08, maxdelay2: 0.03, delay2: time * 2, gain2: 0.3, mul: 1.0, add: 0.0);
	snd = SelectX.ar(\delay2.kr(0.8),[snd, del2]);
	snd = SelectX.ar(\exciter.kr(0.0),[snd, impulse]);
	snd = OnePole.ar(snd, damp);
	snd = LeakDC.ar(in: snd, coef: 0.995);

	snd = snd ! 2;
	snd = snd + (NHHall.ar(snd, rt60: \rev.kr(0.1)) * -5.dbamp);
	LocalOut.ar(snd.sum);
	//DetectSilence.ar(snd, doneAction: Done.freeSelf);
	snd = snd * EnvGen.kr(Env.perc(\atk.kr(0.01), \dec.kr(0.1), curve: -8), \gate.kr(1), doneAction: Done.freeSelf);
	snd = snd * \gain.kr(0).dbamp;
	snd = snd.sanitize;
}).play;

Ndef(\fb1).addSpec(
	\damp,[0, 1],
	\time,[0.01, 0.5, \exp],
	\impulse,[1, 44100, \exp],
	\delay2,[0,1],
	\feedback,[0,1],
	\filter,[0, 20000, s.sampleRate.reciprocal],
	\dist,[0,64],
	\exciter,[0,1],
	\boost,[1, 20000, s.sampleRate.reciprocal],
	\spont,[1, 20000],
	\restore,[1, 20000]
).edit;
)
(
Ndef(\fb1, {
	var snd, impulse, fb, modEnv1, del2;
	var time = \time.kr(0.1) - ControlRate.ir.reciprocal;
	var damp = (1 - \damp.kr(0.5));
	var decay = \dec.kr(0.1);
	fb = LocalIn.ar(1) * \feedback.kr(0.5);
	time = time*(1-fb);
	impulse = Dust.ar(\impulse.kr(10000) * (1-fb));
	impulse = HairCell.ar(impulse, spontaneousrate: \spont.kr(1), boostrate: \boost.kr(2000) * fb, restorerate: (1-fb) * \restore.kr(100), loss: 1-fb);
	impulse = OnePole.ar(impulse, damp);
	//snd = LeakDC.ar(in: snd, coef: 0.995);
	snd = DelayC.ar(fb + impulse, 2, time);
	//snd = CombC.ar(fb + impulse, 2, time, decay);
	snd = snd  + fb;
	//snd = RLPFD.ar(snd, ffreq: \filter.kr(440), res: 0, dist: 0);
	snd = RLPF.ar(snd, freq: \filter.kr(440));
	del2 = AllpassC.ar(snd, 2, time * 2);
	snd = SelectX.ar(\delay2.kr(0.8),[snd, del2]);
	snd = (snd * \dist.kr(36).dbamp).tanh;
	snd = SelectX.ar(\exciter.kr(0.7),[snd, impulse]);
	snd = OnePole.ar(snd, damp);
	snd = LeakDC.ar(in: snd, coef: 0.995);
	snd = snd * EnvGen.kr(Env.perc(\atk.kr(0.01), decay, curve: -8), \gate.kr(1));
	snd = snd ! 2;
	snd = snd + (NHHall.ar(snd, rt60: \rev.kr(0.1)) * -5.dbamp);
	snd = snd.sanitize;
	LocalOut.ar(snd.sum);
	DetectSilence.ar(snd, doneAction: Done.freeSelf);
	snd;
}).play;

Ndef(\fb1).addSpec(
	\damp,[0, 1],
	\time,[0.01, 0.5, \exp],
	\impulse,[1, 44100, \exp],
	\delay2,[0,1],
	\feedback,[0,1],
	\filter,[0, 20000, s.sampleRate.reciprocal],
	\dist,[0,64],
	\exciter,[0,1],
	\boost,[1, 20000, s.sampleRate.reciprocal],
	\spont,[1, 20000],
	\restore,[1, 20000]
).edit;
)