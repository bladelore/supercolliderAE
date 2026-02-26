t.schedAbs(t.nextBar, {t.tempo_(165/60)});
Pdef(\fb1Patt).play(t, quant:[1,0]);

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
	var n = 4;
	var harmonicRatio = 1;
	var bwScale = 0.5;
	var bw = 0.001;

	var freqs = Array.newClear(n);
	var amps = Array.newClear(n);
	var ringtimes = Array.newClear(n);

	var powN, relF, hf, bw_ring;
	var in, impulse, env, modal;

	var formant, f_table, f_freq,f_amp,f_q;
	f_table = FormantTable.get(\bassU);
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
	modal = DynKlank.ar(`[freqs * \stretch.kr(1, 0.01), nil, ringtimes * env], impulse, 1, \freqoffset.kr(10) * env, \decayscale.kr(1))
	* n.sqrt.reciprocal * 0.5;
	// modal = LeakDC.ar(modal, coef: 0.995);
	formant = Splay.ar(BBandPass.ar(modal, f_freq, f_q) * f_amp.dup * (12).dbamp, 0.5);
	modal = SelectX.ar(\formantMix.kr(1),[modal, formant]);
	// modal = LPF.ar(modal, 6000);
	modal = DCompressor.ar(modal, ratio: 4, threshold: -60, attack: 0.1, release: 1, makeup: 0, automakeup: 1);
	modal = modal.softclip;
	modal = modal.sanitize;

	modal = SelectX.ar(\drywet.kr(1),[in ,modal]);
	modal = modal * \gain.kr(-16).dbamp;

Out.ar(\out.kr(0), modal);
}).add;

// SynthDef


Pdef(\modal,
	Pmono(\padKlank,
		\inbus, ~modal,
		\addAction, \addToTail,
		\callback, { Pdefn(\fxid, ~id) },
	)
);
)

(
Pdef(\player,
	Ppar([
		Pbindf(
			Pdef(\fb1Patt),
			\out, [~mainout, ~modal]
		),

/*		Pbindf(
			Pdef(\modal),
			\out, [~mainout]
		),*/
		])
).play(t);
)

(
Pdef(\fb1Patt, Pmono(\fb1_mono) <> Pbind(

	\dur, Pwhite(0.125, 0.5, inf).stutter(4),
	//\legato, ~nearestDelta.(~pattern, Pkey(\bardelta), 0.25, 0).max(0.5),
	//\atk, ~nearestDelta.(~pattern, Pkey(\groupdelta),  0.125, 1).linexp(0, 1, 0.1, 0.3),
	//\dec, ~pmodenv.(Pseq([1, 0.5, 0.25],inf), Pseq([2,1,0.5],inf)),
	//\dec, Pkey(\dur) * 1,
	\dec, Pkey(\dur) * 2,
	//\tempo, ~pmodenv.(Pwhite(0,1,inf).linexp(0, 1, 200/60, 60/60), Pkey(\dec)),
	//\dec, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0.2, 1).linexp(0, 1, 1, 0.2).clip(0.01, 2) * 1,
	//\gain, ~nearestDelta.(~pattern, Pwhite(1, 0, inf), 0.125, 0).clip(0, 1).ampdb + dbamp(6),
//	\gain, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0, 0.5).clip(0, 1).ampdb,
	\feedback, ~pmodenv.(Pseq([0.2, 0.4, 0.8] * 0.5,inf), Pkey(\dec)),
	\time, ~pmodenv.(Pseq(([0.005, 0.001, 0.010] * 100),inf), Pkey(\dec)),
	//\time, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 0.01, 0.05, -8), Pkey(\dec), 1, Pseq([\step, \sine, -3],inf)),
	\res, ~pmodenv.(Pseq([1, 0],inf), Pseq([0.25, 0.25], inf)),
	\damp, ~pmodenv.(Pseq([0.1, 0.5],inf), Pkey(\dec)),
	//\damp, ~pmodenv.(Pwhite(1, 0.5, inf).lincurve(0, 1, 0.1, 1, -4), Pkey(\dec)),
	//\damp, 0.5,
	//\filter, 100,
	//\filter, ~pmodenv.(Pwhite(1, 0, inf).lincurve(0,1,2000,500,-4), Pkey(\dec)),
	//\res, 0.5,
	\filter, ~pmodenv.(Pseq([30, 100],inf), Pkey(\dec)),
	//\filter, ~pmodenv.(Pseq([60, 20000],inf), Pkey(\dec)),
	//\delay2, ~pmodenv.(Pwhite(1, 0, inf), Pkey(\dec)),
	\exciter, ~pmodenv.(Pwhite(1, 0, inf).lincurve(0, 1, 0, 1, -8), Pkey(\dec), 1, Pseq([\sine],inf)),
	//\exciter, 0,
	//\impulse, ~pmodenv.(Pwhite(20000, 200,inf), Pkey(\dec)),
	//\spont, ~pmodenv.(Pseq([60,30],inf), Pkey(\dec)),
	//\boost,  ~pmodenv.(Pseq([20000,200],inf), Pkey(\dec)),
	//\restore, ~pmodenv.(Pseq([1000, 20000],inf), Pkey(\dec)),
	\dist,  ~pmodenv.(Pseq([16, 32],inf), Pkey(\dec)),
	//\dist, 64,
	\rev, ~pmodenv.(Pexprand(0.1, 4, inf), Pkey(\dec)),
	\pan, ~pmodenv.(Pwhite(-0.3, 0.3, inf), Pkey(\dec)),
	\gain, -6,
	)
)
);

(
Pdef(\fb1Patt, Pmono(\fb1_mono) <> Pbind(
	\dur, Pwhite(0.125, 0.5, inf).stutter(4),
	//\legato, ~nearestDelta.(~pattern, Pkey(\bardelta), 0.25, 0).max(0.5),
	//\atk, ~nearestDelta.(~pattern, Pkey(\groupdelta),  0.125, 1).linexp(0, 1, 0.1, 0.3),
	//\dec, ~pmodenv.(Pseq([1, 0.5, 0.25],inf), Pseq([2,1,0.5],inf)),
	//\dec, Pkey(\dur) * 1,
	\dec, Pkey(\dur) * 1,
	//\dec, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0.2, 1).linexp(0, 1, 1, 0.2).clip(0.01, 2) * 1,
	//\gain, ~nearestDelta.(~pattern, Pwhite(1, 0, inf), 0.125, 0).clip(0, 1).ampdb + dbamp(6),
//	\gain, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0, 0.5).clip(0, 1).ampdb,
	\feedback, ~pmodenv.(Pseq([0.2, 0.4, 0.8] * 0.5,inf), Pkey(\dec)),
	\time, ~pmodenv.(Pseq(([0.005, 0.001, 0.010] * 1),inf), Pkey(\dec)),
	//\time, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 0.01, 0.05, -8), Pkey(\dec), 1, Pseq([\step, \sine, -3],inf)),
	\res, ~pmodenv.(Pseq([1, 0],inf), Pseq([0.25, 0.25], inf)),
	\damp, ~pmodenv.(Pseq([0.1, 1],inf), Pkey(\dec)),
	//\damp, ~pmodenv.(Pwhite(1, 0.5, inf).lincurve(0, 1, 0.1, 1, -4), Pkey(\dec)),
	//\damp, 0.5,
	//\filter, 100,
	//\filter, ~pmodenv.(Pwhite(1, 0, inf).lincurve(0,1,2000,500,-4), Pkey(\dec)),
	//\res, 0.5,
	\filter, ~pmodenv.(Pseq([30, 100],inf), Pkey(\dec)),
	//\filter, ~pmodenv.(Pseq([60, 20000],inf), Pkey(\dec)),
	//\delay2, ~pmodenv.(Pwhite(1, 0, inf), Pkey(\dec)),
	\exciter, ~pmodenv.(Pwhite(1, 0, inf).lincurve(0, 1, 0, 1, -8), Pkey(\dec), 1, Pseq([\sine],inf)),
	//\exciter, 0,
	// \impulse, ~pmodenv.(Pwhite(20000, 200,inf), Pkey(\dec)),
	// \spont, ~pmodenv.(Pseq([60,30],inf), Pkey(\dec)),
	//\boost,  ~pmodenv.(Pseq([20000,200],inf), Pkey(\dec)),
	//\restore, ~pmodenv.(Pseq([1000, 20000],inf), Pkey(\dec)),
	\dist,  ~pmodenv.(Pseq([16, 32],inf), Pkey(\dec)),
	//\dist, 64,
	\rev, ~pmodenv.(Pexprand(0.1, 4, inf), Pkey(\dec)),
	\pan, ~pmodenv.(Pwhite(-0.3, 0.3, inf), Pkey(\dec)),
	\gain, -6,
	)
)
);

(
Pdef(\fb1Patt, Pmono(\fb1_mono) <> Pbind(
	\dur, Pwhite(0.125, 0.5, inf).stutter(4),
	//\legato, ~nearestDelta.(~pattern, Pkey(\bardelta), 0.25, 0).max(0.5),
	//\atk, ~nearestDelta.(~pattern, Pkey(\groupdelta),  0.125, 1).linexp(0, 1, 0.1, 0.3),
	//\dec, ~pmodenv.(Pseq([1, 0.5, 0.25],inf), Pseq([2,1,0.5],inf)),
	//\dec, Pkey(\dur) * 1,
	\dec, Pkey(\dur) * 1,
	//\dec, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0.2, 1).linexp(0, 1, 1, 0.2).clip(0.01, 2) * 1,
	//\gain, ~nearestDelta.(~pattern, Pwhite(1, 0, inf), 0.125, 0).clip(0, 1).ampdb + dbamp(6),
//	\gain, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0, 0.5).clip(0, 1).ampdb,
	\feedback, ~pmodenv.(Pseq([0.2, 0.4, 0.8] * 0.5,inf), Pkey(\dec)),
	\time, ~pmodenv.(Pseq(([0.005, 0.001, 0.010] * 1),inf), Pkey(\dec)),
	//\time, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 0.01, 0.05, -8), Pkey(\dec), 1, Pseq([\step, \sine, -3],inf)),
	\res, ~pmodenv.(Pseq([1, 0],inf), Pseq([0.25, 0.25], inf)),
	\damp, ~pmodenv.(Pseq([0.1, 1],inf), Pkey(\dec)),
	//\damp, ~pmodenv.(Pwhite(1, 0.5, inf).lincurve(0, 1, 0.1, 1, -4), Pkey(\dec)),
	//\damp, 0.5,
	//\filter, 100,
	//\filter, ~pmodenv.(Pwhite(1, 0, inf).lincurve(0,1,2000,500,-4), Pkey(\dec)),
	//\res, 0.5,
	\filter, ~pmodenv.(Pseq([30, 100],inf), Pkey(\dec)),
	//\filter, ~pmodenv.(Pseq([60, 20000],inf), Pkey(\dec)),
	//\delay2, ~pmodenv.(Pwhite(1, 0, inf), Pkey(\dec)),
	\exciter, ~pmodenv.(Pwhite(1, 0, inf).lincurve(0, 1, 0, 1, -8), Pkey(\dec), 1, Pseq([\sine],inf)),
	//\exciter, 0,
	// \impulse, ~pmodenv.(Pwhite(20000, 200,inf), Pkey(\dec)),
	\spont, ~pmodenv.(Pseq([60,30],inf), Pkey(\dec)),
	//\boost,  ~pmodenv.(Pseq([20000,200],inf), Pkey(\dec)),
	//\restore, ~pmodenv.(Pseq([1000, 20000],inf), Pkey(\dec)),
	\dist,  ~pmodenv.(Pseq([16, 32],inf), Pkey(\dec)),
	//\dist, 64,
	\rev, ~pmodenv.(Pexprand(0.1, 4, inf), Pkey(\dec)),
	\pan, ~pmodenv.(Pwhite(-0.3, 0.3, inf), Pkey(\dec)),
	\gain, -6,
	)
)
);


(
Pdef(\fb1Patt,
	Pbind(
		// \dur, Pwhite(0.125, 0.5, inf).stutter(4),
		//\legato, ~nearestDelta.(~pattern, Pkey(\bardelta), 0.25, 0).max(0.5),
		\dec, Pkey(\dur) * 1,
		\feedback, ~pmodenv.(Pseq([0.2, 0.4, 0.8] * 0.5,inf), Pkey(\dec)),
		\time, ~pmodenv.(Pseq(([0.005, 0.001, 0.010] * 1),inf), Pkey(\dec)),
		// \time, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 0.01, 0.05, -8), Pkey(\dec), 1, Pseq([\step, \sine, -3],inf)),
		// \res, ~pmodenv.(Pseq([1, 0],inf), Pseq([0.25, 0.25], inf)),
		// \damp, ~pmodenv.(Pseq([0.1, 1],inf), Pkey(\dec)),
		\damp, ~pmodenv.(Pwhite(1, 0.5, inf).lincurve(0, 1, 0.1, 1, -4), Pkey(\dec)),
		//\damp, 0.5,
		//\filter, 100,
		//\filter, ~pmodenv.(Pwhite(1, 0, inf).lincurve(0,1,2000,500,-4), Pkey(\dec)),
		//\res, 0.5,
		// \filter, ~pmodenv.(Pseq([30, 100],inf), Pkey(\dec)),
		\filter, ~pmodenv.(Pseq([60, 100],inf), Pkey(\dec)),
		// \delay2, ~pmodenv.(Pwhite(1, 0, inf), Pkey(\dec)),
		\exciter, ~pmodenv.(Pwhite(1, 0, inf).lincurve(0, 1, 0, 1, -8), Pkey(\dec)),
		// \exciter, 0,
		// \impulse, ~pmodenv.(Pwhite(20000, 200,inf), Pkey(\dec)),
		// \spont, ~pmodenv.(Pseq([60,30],inf), Pkey(\dec)),
		//\boost,  ~pmodenv.(Pseq([20000,200],inf), Pkey(\dec)),
		//\restore, ~pmodenv.(Pseq([1000, 20000],inf), Pkey(\dec)),
		\dist,  ~pmodenv.(Pseq([16, 32],inf), Pkey(\dec)),
		//\dist, 64,
		\rev, ~pmodenv.(Pexprand(0.1, 4, inf), Pkey(\dec)),
		\pan, ~pmodenv.(Pwhite(-0.3, 0.3, inf), Pkey(\dec)),
		\gain, -6,
	)
	// <> Pbind(\stretch, Pkey(\groupdelta).linlin(0, 1, 1, 2))
	<> ~makeSubdivision.(
		Pseq([3, 1], 1),
		Pseq([4, 5, 3, 6, 2], 1)
	)
	// <> Pbind(\instrument, \fb1)
	<> Pmono(\fb1_mono)
)
);

(
Pdef(\fb1Patt, Pmono(\fb1_mono) <> Pbind(
	\dur, Pwhite(0.125, 0.5, inf),
	//\legato, ~nearestDelta.(~pattern, Pkey(\bardelta), 0.25, 0).max(0.5),
	//\atk, ~nearestDelta.(~pattern, Pkey(\groupdelta),  0.125, 1).linexp(0, 1, 0.1, 0.3),
	//\dec, ~pmodenv.(Pseq([1, 0.5, 0.25],inf), Pseq([2,1,0.5],inf)),
	//\dec, Pkey(\dur) * 1,
	\dec, Pkey(\dur) * 2,
	//\dec, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0.2, 1).linexp(0, 1, 1, 0.2).clip(0.01, 2) * 1,
	//\gain, ~nearestDelta.(~pattern, Pwhite(1, 0, inf), 0.125, 0).clip(0, 1).ampdb + dbamp(6),
//	\gain, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0, 0.5).clip(0, 1).ampdb,
	\feedback, ~pmodenv.(Pseq([0.2, 0.4, 0.8],inf), Pkey(\dec)),
	\time, ~pmodenv.(Pseq(([0.005, 0.001, 0.010] * 1),inf), Pkey(\dec)),
	//\time, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 0.01, 0.05, -8), Pkey(\dec), 1, Pseq([\step, \sine, -3],inf)),
	//\res, ~pmodenv.(Pseq([1, 0],inf), Pseq([0.25, 0.25], inf)),
	\damp, ~pmodenv.(Pseq([0.5, 1],inf), Pkey(\dec)),
	//\damp, ~pmodenv.(Pwhite(1, 0.5, inf).lincurve(0, 1, 0.1, 1, -4), Pkey(\dec)),
	//\damp, 0.5,
	//\filter, 100,
	//\filter, ~pmodenv.(Pwhite(1, 0, inf).lincurve(0,1,2000,500,-4), Pkey(\dec)),
	//\res, 0.5,
	//\filter, ~pmodenv.(Pseq([30, 100],inf), Pkey(\dec)),
	\filter, ~pmodenv.(Pwhite(5000, 20000,inf), Pkey(\dec)),
	\delay2, ~pmodenv.(Pwhite(1, 0, inf), Pkey(\dec)),
	\exciter, ~pmodenv.(Pwhite(1, 0, inf).lincurve(0, 1, 0, 1, -8), Pkey(\dec), 1, Pseq([\sine],inf)),
	// \exciter, 0,
	//\impulse, ~pmodenv.(Pwhite(20000, 200,inf), Pkey(\dec)),
	//\spont, ~pmodenv.(Pseq([60,30],inf), Pkey(\dec)),
	//\boost,  ~pmodenv.(Pseq([20000,200],inf), Pkey(\dec)),
	//\restore, ~pmodenv.(Pseq([1000, 20000],inf), Pkey(\dec)),
	\dist,  ~pmodenv.(Pseq([16, 32] * 2,inf), Pkey(\dec)),
	//\dist, 64,
	// \rev, ~pmodenv.(Pexprand(0.1, 4, inf), Pkey(\dec)),
	\rev, 0,
	\pan, ~pmodenv.(Pwhite(-0.3, 0.3, inf), Pkey(\dec)),
	\gain, -6,
	)
)
);