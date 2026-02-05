(
Ndef(\padKlank).clear;
Ndef(\padKlank).ar(2);
)
(
Ndef(\padKlank, {
var f = 60;
var n = 32;
var harmonicRatio = 0.75;
//var stretch = 1;
var bwScale = 0.5;
var bw = 100;

var freqs = Array.newClear(n);
var amps = Array.newClear(n);
var ringtimes = Array.newClear(n);

var powN, relF, hf, bw_ring;

var impulse, fb, env, modal;

n.do{|i|
	//get harmonic integers
	powN = pow(i + 1, harmonicRatio);
	//relF = (powN * (1.0 + (powN - 1) * stretch));
	relF = (powN * (1.0 + (powN - 1)));
	//harmonic frequency
	hf = (relF * f);
	freqs[i] = hf;
	//scale ring time to frequency
	bw_ring = (pow(2, (bw / 1200)) - clip(bwScale, -1 , 1)) * pow(relF, bwScale);
	ringtimes[i] = bw_ring;

};

impulse = GaussTrig.ar(freq: \impulse.kr(20000), dev: \dev.kr(1)) * 0.07 ;
impulse = impulse * PinkNoise.ar([1,1]);
modal = DynKlank.ar(`[freqs * \stretch.kr(1).lag(lagTime:0.01), nil, ringtimes], impulse, 1, \freqoffset.kr(0), \decayscale.kr(1)) * n.sqrt.reciprocal * 0.5;
//modal = modal.softclip;
modal = modal * \gain.kr(-8).dbamp;
modal = LeakDC.ar(modal, coef: 0.995);
modal = modal.sanitize;
modal

}).play;

Ndef(\padKlank).addSpec(
	\fb, [0,1],
	\impulse, [1, 20000],
	\dev, [0.01, 100, \exp],
	\decayscale, [0.01, 100, \exp],
	\freqoffset, [0, 100],
).edit;
)

(
Ndef(\padKlank, {
var f = 60;
var n = 32;
var harmonicRatio = 0.5;
//var stretch = 1;
var bwScale = 1;
var bw = 10;

var freqs = Array.newClear(n);
var amps = Array.newClear(n);
var ringtimes = Array.newClear(n);

var powN, relF, hf, bw_ring;

var impulse, fb, env, modal;

n.do{|i|
	//get harmonic integers
	powN = pow(i + 1, harmonicRatio);
	//relF = (powN * (1.0 + (powN - 1) * stretch));
	relF = (powN * (1.0 + (powN - 1)));
	//harmonic frequency
	hf = (relF * f);
	freqs[i] = hf;
	//scale ring time to frequency
	bw_ring = (pow(2, (bw / 1200)) - clip(bwScale, -1 , 1)) * pow(relF, bwScale);
	ringtimes[i] = bw_ring;

};

env = EnvGen.kr(Env.perc(\atk.kr(0.01), \rel.kr(0.5), curve: -8), gate: \gate.kr(1), doneAction:2);

impulse = GaussTrig.ar(freq: \impulse.kr(20000), dev: \dev.kr(1)) * 0.07 ;
impulse = impulse * PinkNoise.ar([1,1]);
modal = DynKlank.ar(`[freqs * \stretch.kr(1).lag(lagTime:0.01), nil, ringtimes * env], impulse * env, 1, \freqoffset.kr(0), \decayscale.kr(1)) * n.sqrt.reciprocal * 0.5;
//modal = modal.softclip;
modal = modal * \gain.kr(-8).dbamp;
modal = LeakDC.ar(modal, coef: 0.995);
modal = modal.sanitize;
modal

}).play;

Ndef(\padKlank).addSpec(
	\fb, [0,1],
	\impulse, [1, 20000],
	\dev, [0.01, 100, \exp],
	\decayscale, [0.01, 100, \exp],
	\freqoffset, [0, 100],
).edit;
)