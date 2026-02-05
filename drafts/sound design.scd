//Quarks.install("JITLibExtensions");
s.options.blockSize
s.options.blockSize = 1;
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
	var snd, impulse, fb, time;
	fb = LocalIn.ar(1) * \feedback.kr(0.5);

	impulse = Dust.ar(\impulse.kr(10000));
	impulse = HairCell.ar(impulse, spontaneousrate: \spont.kr(1), boostrate: \boost.kr(2000), restorerate: (1-fb) * \restore.kr(100), loss: 1 - fb);
	impulse = OnePole.ar(impulse, (1 - \damp.kr(0.5)));

	time = \time.kr(0.1) - ControlRate.ir.reciprocal;
	snd = DelayC.ar(fb+ impulse, 2, 0.01);
	snd = snd  + fb;
	snd = RLPFD.ar(snd, ffreq: \filter.kr(440), res: 0, dist: 0);
	snd = (snd * \dist.kr(36).dbamp).tanh;
	//snd = AllpassC.ar(snd, 2, \time.kr(0.1) - ControlRate.ir.reciprocal);
	snd = SelectX.ar(\exciter.kr(0.7),[snd, impulse]);
	snd = OnePole.ar(snd, (1 - \damp.kr(0.5)));
	snd = LeakDC.ar(in: snd, coef: 0.995);
	snd = snd * EnvGen.kr(Env.perc(\atk.kr(0.01), \dec.kr(0.1), curve: -8), \gate.kr(1));
	snd = snd ! 2;
	snd = snd + (NHHall.ar(snd, rt60: \rev.kr(0.1)) * -5.dbamp);
	LocalOut.ar(snd.sum);
	DetectSilence.ar(snd, doneAction: Done.freeSelf);
	snd;
}).play;

Ndef(\fb1).addSpec(
	\damp,[0, 1],
	\time,[0.01, 0.5, \exp],
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

~relF = {|n, harmonicRatio, stretch|
	var powN = pow(n + 1, harmonicRatio);
	var relF = (powN * (1.0 + (powN - 1) * stretch));
	relF;
};

)

(
var f = 60;
var n = 20;
var harmonicRatio = 1;
var stretch = 1;
var bwScale = 2;
var bw = 5;

var freqs = Array.newClear(n);
var amps = Array.newClear(n);
var ringtimes = Array.newClear(n);

var powN, relF, hf, bw_ring;

n.do{|i|
	//get harmponics
	powN = pow(i + 1, harmonicRatio);
	relF = (powN * (1.0 + (powN - 1) * stretch));
	//harmonic frequency
	hf = relF * f;
	freqs[i] = hf;
	//var bw_hz = (pow(2, (bw / 1200)) - clip(bwScale,-1 , 1)) * f * pow(relF, bwScale);
	bw_ring = (pow(2, (bw / 1200)) - clip(bwScale,-1 , 1)) * pow(relF, bwScale);
	ringtimes[i] = bw_ring;
	//var bwi = bw_hz / (2 * 44100);
	//var fi = (f * relF)/44100;
};
freqs;
{ DynKlank.ar(
	`[freqs, nil, ringtimes],
	GaussTrig.ar(freq: 440, dev: 3) * 0.07) ! 2

}.play;
//
)

(
Ndef(\padKlank, {
var f = 60;
var n = 20;
var harmonicRatio = 1;
var stretch = 1;
var bwScale = 2;
var bw = 5;

var freqs = Array.newClear(n);
var amps = Array.newClear(n);
var ringtimes = Array.newClear(n);

var powN, relF, hf, bw_ring, var modal;

n.do{|i|
	//get harmponics
	powN = pow(i + 1, harmonicRatio);
	relF = (powN * (1.0 + (powN - 1) * stretch));
	//harmonic frequency
	hf = relF * f;
	freqs[i] = hf;
	//var bw_hz = (pow(2, (bw / 1200)) - clip(bwScale,-1 , 1)) * f * pow(relF, bwScale);
	bw_ring = (pow(2, (bw / 1200)) - clip(bwScale,-1 , 1)) * pow(relF, bwScale);
	ringtimes[i] = bw_ring;
	//var bwi = bw_hz / (2 * 44100);
	//var fi = (f * relF)/44100;
};

//freqs = Control.names([\freqs]).kr(freqs);
//amps = Control.names([\amps]).kr(a);
//ringtimes = Control.names([\ringtimes]).kr(amps);

freqs.postln;

modal = DynKlank.ar(
	`[freqs, nil, ringtimes],
	GaussTrig.ar(freq: 440, dev: 3) * 0.07);

modal ! 2;

}).play;
)
	