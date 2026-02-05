(
~mainout = 0;
~longverb = Bus.audio(s,2);
~longverb2 = Bus.audio(s,2);
~delay = Bus.audio(s,2);
~chorus = Bus.audio(s,2);

~modDelay=Bus.audio(s,2);
~mod1_in = Bus.audio(s, 2);
~mod1_out = Bus.control(s, 1);

~modal=Bus.audio(s,2);
~resonator=Bus.audio(s,2);
)

FormantTable.keys

(
SynthDef(\padKlank, {
	var f = 40;
	var n = 8;
	var harmonicRatio = 2;
	var bwScale = 0.5;
	var bw = 1;

	var freqs = Array.newClear(n);
	var amps = Array.newClear(n);
	var ringtimes = Array.newClear(n);

	var powN, relF, hf, bw_ring;
	var in, impulse, env, modal;

	var formant, f_table, f_freq,f_amp,f_q;
	f_table = FormantTable.get(\bassA);
	f_freq = f_table[0];
	f_amp = f_table[1];
	f_q = f_table[2];

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
	env = Amplitude.ar(in, \atk.kr(0.01), \rel.kr(0.5));
	impulse = GaussTrig.ar(freq: \impulse.kr(20000), dev: \dev.kr(0.01)) ;
	impulse = impulse * PinkNoise.ar([1,1]) * \inGain.kr(-16).dbamp;
	modal = DynKlank.ar(`[freqs * \stretch.kr(1, 0.01), nil, ringtimes * env], impulse, 10, \freqoffset.kr(10) * env, \decayscale.kr(10))
	* n.sqrt.reciprocal * 0.5;
	// modal = LeakDC.ar(modal, coef: 0.995);
	formant = Splay.ar(BBandPass.ar(modal, f_freq, f_q) * f_amp.dup * env * (12).dbamp, 0.5);
	modal = SelectX.ar(\formantMix.kr(0.5),[modal, formant]);
	// modal = LPF.ar(modal, 6000);
	modal = DCompressor.ar(modal, ratio: 4, threshold: -60, attack: 0.1, release: 1, makeup: 0, automakeup: 1);
	modal = modal.softclip;
	modal = modal.sanitize;

	modal = SelectX.ar(\drywet.kr(1),[in ,modal]);
	modal = modal * \gain.kr(-16).dbamp;

Out.ar(\out.kr(0), modal);
}).add;


SynthDef(\percKlank, {
var f = 5;
var n = 16;
var harmonicRatio = 0.4;
//var stretch = 1;
var bwScale = 1;
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
	hf.postln;
	//scale ring time to frequency
	bw_ring = (pow(2, (bw / 1200)) - clip(bwScale, -1 , 1)) * pow(relF, bwScale);
	ringtimes[i] = bw_ring;

};

env = EnvGen.kr(Env.perc(\atk.kr(0.01), \rel.kr(1), curve: -8), gate: \gate.kr(1), doneAction:2);

impulse = GaussTrig.ar(freq: \impulse.kr(20000), dev: \dev.kr(0.01)) * 0.1 ;
impulse = impulse * PinkNoise.ar([1,1]);
modal = DynKlank.ar(`[freqs * \stretch.kr(1, 0.01), nil, ringtimes * env], impulse * env, 1, \freqoffset.kr(100) * env, \decayscale.kr(10)) * n.sqrt.reciprocal * 0.5;
modal = modal.softclip;
modal = modal * \gain.kr(0).dbamp;
modal = LeakDC.ar(modal, coef: 0.995);
modal = modal.sanitize;
Out.ar(\out.kr(0), modal);
}).add;


(
Pdef(\player1,
	Ppar([
/*		Pbindf(
			Pdef(\perc),
			\out, [~comp]
		),*/

		Pbindf(
			Pdef(\highPerc),
			\out, [~comp, ~modal]
		),


		Pdef(\comp),
		])
)
)

)

(
Pdef(\highPerc,
	// Pmono(\fmPerc_mono) <>
	Pbind(
		\instrument, \fmPerc,
		\freq, 23.midicps,
		// \dur, Pwhite(0.5, 0.125, inf),
		//\dur, 0.5,
		\dec, Pkey(\dur) * 1,
		\atk, ~pmodenv.(Pwhite(0.01, 0.05, inf), Pkey(\dec)),
		// \atk, 0.02,
		// \rel, ~pmodenv.(Pwhite(0.5, 0.125, inf), Pkey(\dec)),
		// \rel, Pkey(\dur) * 0.5,
		\rel, 0.5,
		\feedback, ~pmodenv.(Pwhite(0, 1,inf), Pkey(\dec)),
		\fbmod, ~pmodenv.(Pwhite(0, 1,inf), Pkey(\dec)),

		\carRatio, 2,
		\m1Ratio, 1,
		\m2Ratio, 7,
		\m3Ratio, 9,
		//
		\i1atk, ~pmodenv.(Pwhite(0.5, 0.1), Pkey(\dec)),
		\i2atk, ~pmodenv.(Pwhite(0.8, 0.01), Pkey(\dec)),
		\i1rel, ~pmodenv.(Pwhite(0, 1), Pkey(\dec)),
		\i2rel, ~pmodenv.(Pwhite(0, 0.5), Pkey(\dec)),

		\index1, ~pmodenv.(Pkey(\groupdelta) + 1e4, Pkey(\dec)),
		\index2, ~pmodenv.(1 - Pkey(\groupdelta) + 1e4, Pkey(\dec)),
		\iscale, ~pmodenv.((1 - Pkey(\cycledelta)) + 1e-4, Pkey(\dec)),
		\spread,~pmodenv.(Pwhite(10, 30, inf), Pkey(\dec)),
		\mix, ~pmodenv.(Pwhite(0, 1), Pkey(\dec)),
		// \mix, 1,
		//\drive, 0,
		\drive, ~pmodenv.(Pwhite(0, 16), Pkey(\dec)),
		\gain, -6,
	) <> ~makeSubdivision.(
		Pseq([1.5, 1.5, 1, 1], 1),
		Pseq(#[4, 2, 6], 1)
	)
);

Pdef(\kick,
	Pbind(
	\dec, Pkey(\dur) * 0.5,
	\freq, 60,
		// \dec, 0.5,
	\fb, 1,
		// \index, ~pmodenv.((1 - Pkey(\groupdelta)), Pkey(\dec)),
	\ratio, 4,
	\drive, 0,
	\sweep, 1,
	\gain, -6
	)
	// <> ~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[1, 4])
	// <> ~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[1, 3, 4])
	// <> Pbind(\timingOffset, Pkey(\groupcount) / Pkey(\groupdelta))
	<> Pbind(\timingOffset, Pkey(\groupcount) / Pkey(\groupdelta))
	<> ~makeSubdivision.(
		Pseq([1, 1, 1, 1], 1),
		Pseq(#[4, 4, 4], 1)
	)
	<> Pbind(\instrument, \fmKick),
).play(t);

Pdef(\perc,
	Pbind(
	\dec, Pkey(\dur) * 1,

	)
	<> ~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[1,3])
	// <> ~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[1, 3, 4])
	<> ~makeSubdivision.(
		Pseq([1.5, 1.5, 0.75, 1], 1),
		Pseq(#[4, 2, 6], 1)
	)
	<> Pbind(\instrument, \percKlank),
);

)

Pdef(\player1).play(t,quant:[1,0]);